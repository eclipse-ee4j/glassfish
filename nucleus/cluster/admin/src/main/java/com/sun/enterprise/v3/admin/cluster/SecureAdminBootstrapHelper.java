/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.v3.admin.cluster;

import com.jcraft.jsch.SftpATTRS;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.util.cluster.RemoteType;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.file.Path;

import org.glassfish.cluster.ssh.launcher.SSHException;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.launcher.SSHSession;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.sftp.SFTPPath;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_FILENAME_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;

/**
 * Bootstraps the secure admin-related files, either over ssh (copying files from the
 * current runtime environment to the remote system via secure ftp) or locally
 * (using more straightforward file-copying).
 *
 * @author Tim Quinn
 */
public abstract class SecureAdminBootstrapHelper implements AutoCloseable {
    private static final Logger LOG = System.getLogger(SecureAdminBootstrapHelper.class.getName());
    private static final Path DOMAIN_XML_PATH = Path.of("config", "domain.xml");
    private static final Path[] SECURE_ADMIN_FILE_REL_URIS_TO_COPY = new Path[] {
        DOMAIN_XML_PATH,
        Path.of("config", KEYSTORE_FILENAME_DEFAULT),
        Path.of("config", TRUSTSTORE_FILENAME_DEFAULT)
    };
    private static final Path[] SECURE_ADMIN_FILE_DIRS_TO_CREATE = new Path[] {Path.of("config")};

    /**
     * Creates a new helper for delivering files needed for secure admin to the remote instance.
     *
     * @param sshL
     * @param dasInstanceDir directory of the local instance - source for the required files
     * @param remoteNodeDir directory of the remote node on the remote system
     * @param instance name of the instance on the remote node to bootstrap
     * @param node Node from the domain configuration for the target node
     * @return the remote helper
     * @throws BootstrapException
     */
    public static SecureAdminBootstrapHelper getRemoteHelper(
            final SSHLauncher sshL,
            final File dasInstanceDir,
            final String remoteNodeDir,
            final String instance,
            final Node node) throws BootstrapException {

        final RemoteType type;
        try {
            // this also handles the case where node is null
            type = RemoteType.valueOf(node.getType());
        } catch (Exception e) {
            throw new IllegalArgumentException(Strings.get("internal.error", "unknown type"));
        }

        switch (type) {
            case SSH:
                return new SSHHelper(
                        sshL,
                        dasInstanceDir,
                        remoteNodeDir,
                        instance,
                        node);
            default:
                throw new IllegalArgumentException(
                        Strings.get("internal.error", "A new type must have "
                        + "been added --> unknown type: " + type.toString()));
        }
    }

    /**
     * Creates a new helper for delivering files needed for secure admin to
     * the local instance (local meaning on the same node as the DAS).
     *
     * @param existingInstanceDir directory of an existing instance (typically the DAS) from where the files can be copied
     * @param newInstanceDir directory of the new instance to where the files will be copied
     *
     * @return the local helper
     */
    public static SecureAdminBootstrapHelper getLocalHelper(final File existingInstanceDir, final File newInstanceDir) {
        return new LocalHelper(existingInstanceDir, newInstanceDir);
    }

    /**
     * Cleans up any allocated resources.
     */
    protected abstract void mkdirs(Path dir) throws IOException;

    @Override
    public void close() {
        // nothing
    }

    /**
     * Copies the bootstrap files from their origin to their destination.
     * <p>
     * Concrete subclasses implement this differently, depending on exactly
     * how they actually transfer the files.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected abstract void copyBootstrapFiles() throws FileNotFoundException, IOException;

    /**
     * Adjusts the date on the new instance's domain.xml so it looks older
     * than the original one on the DAS.
     * <p>
     * We have copied the domain.xml and a small number of other files to the
     * instance, but not all the files needed for the instance to be fully
     * sync-ed.  The sync logic decides if an instance is up-to-date by comparing
     * the timestamp of the instance's domain.xml with that of the DAS.  If
     * those timestamps match then the sync logic judges the
     * instance to be up-to-date.  When we copy the DAS domain.xml to the instance
     * to deliver the secure admin configuration (so the start-local-instance
     * command and the instance will know how to connect to the DAS) it is left
     * with the same timestamp as the DAS copy.  To make sure sync works when
     * start-local-instance runs, we backdate the instance's copy of domain.xml.
     *
     * @throws com.sun.enterprise.v3.admin.cluster.SecureAdminBootstrapHelper.BootstrapException
     */
    protected abstract void backdateInstanceDomainXML() throws BootstrapException;

    /**
     * Bootstraps the instance for remote admin.
     *
     * @throws BootstrapException
     */
    public void bootstrapInstance() throws BootstrapException {
        try {
            mkdirs();
            copyBootstrapFiles();
            backdateInstanceDomainXML();
        } catch (Exception ex) {
            throw new BootstrapException(ex);
        }
    }

    private void mkdirs() throws IOException {
        for (Path dirPath : SECURE_ADMIN_FILE_DIRS_TO_CREATE) {
            mkdirs(dirPath);
        }
    }

    /**
     * Implements the helper functionality for a remote instance.
     */
    private static abstract class RemoteHelper extends SecureAdminBootstrapHelper {
        final File dasInstanceDir;
        final SFTPPath remoteNodeDir;
        final SFTPPath remoteInstanceDir;

        RemoteHelper(
                final File dasInstanceDir,
                final String remoteNodeDir,
                final String instance,
                final Node node) {
            this.dasInstanceDir = dasInstanceDir;
            this.remoteNodeDir = remoteNodeDirUnixStyle(node, remoteNodeDir);
            this.remoteInstanceDir = this.remoteNodeDir.resolve(instance);
        }

        abstract void writeToFile(final Path remotePath, final File localFile) throws IOException;

        abstract void setLastModified(final Path remotePath, final long when) throws IOException;

        /**
         * Use the node dir if it was specified when the node was created.
         * Otherwise derive it: ${remote-install-dir}/glassfish/${node-name}
         */
        private static SFTPPath remoteNodeDirUnixStyle(final Node node, final String remoteNodeDir) {
            if (remoteNodeDir == null) {
                return SFTPPath.of(node.getInstallDirUnixStyle())
                    .resolve(SFTPPath.ofRelativePath("glassfish", "nodes", node.getName()));
            }
            return SFTPPath.of(remoteNodeDir);
        }


        @Override
        protected void copyBootstrapFiles() throws FileNotFoundException, IOException {
            for (Path fileRelativePath : SECURE_ADMIN_FILE_REL_URIS_TO_COPY) {
                SFTPPath remoteFilePath = remoteInstanceDir.resolve(fileRelativePath);
                try {
                    writeToFile(remoteFilePath, dasInstanceDir.toPath().resolve(fileRelativePath).toFile());
                    LOG.log(Level.DEBUG, "Copied bootstrap file to {0}", remoteFilePath);
                } catch (Exception ex) {
                    LOG.log(Level.DEBUG, "Error copying bootstrap file to " + remoteFilePath, ex);
                    throw new IOException(ex);
                }
            }
        }
    }

    private static class SSHHelper extends RemoteHelper {
        private final SSHSession session;
        private final SFTPClient ftpClient;
        private final SSHLauncher launcher;

        private SSHHelper(
                final SSHLauncher sshLauncher,
                final File dasInstanceDir,
                final String remoteNodeDir,
                final String instance,
                final Node node) throws BootstrapException {
            super(dasInstanceDir, remoteNodeDir, instance, node);
            launcher = sshLauncher;
            try {
                session = launcher.openSession();
            } catch (SSHException e) {
                throw new BootstrapException(e);
            }
            try {
                ftpClient = session.createSFTPClient();
            } catch (SSHException e) {
                if (session != null) {
                    session.close();
                }
                throw new BootstrapException(e);
            }
        }

        @Override
        protected void mkdirs(Path dir) throws IOException {
            SFTPPath remoteDir = remoteInstanceDir.resolve(dir);
            LOG.log(Level.DEBUG, "Trying to create directories for remote path {0}", remoteDir);
            SftpATTRS attrs = ftpClient.lstat(remoteNodeDir);
            if (attrs == null) {
                throw new IOException("Remote path " + remoteNodeDir + " does not exist.");
            }
            int instanceDirPermissions = attrs.getPermissions();
            LOG.log(Level.DEBUG, "Creating remote bootstrap directory " + remoteDir + " with permissions "
                + Integer.toOctalString(instanceDirPermissions));
            ftpClient.mkdirs(remoteDir);
            if (launcher.getCapabilities().isChmodSupported()) {
                ftpClient.chmod(remoteDir, instanceDirPermissions);
            }
        }

        @Override
        public void close() {
            if (ftpClient != null) {
                ftpClient.close();
            }
            if (session != null) {
                session.close();
            }
        }

        @Override
        void writeToFile(final Path remotePath, final File localFile) throws IOException {
            ftpClient.put(localFile, SFTPPath.of(remotePath));
        }

        @Override
        protected void backdateInstanceDomainXML() throws BootstrapException {
            final Path remoteDomainXML = remoteInstanceDir.resolve(DOMAIN_XML_PATH);
            try {
                setLastModified(remoteDomainXML, 0);
            } catch (IOException ex) {
                throw new BootstrapException(ex);
            }
            LOG.log(Level.DEBUG, "Backdated the instance's copy of domain.xml");
        }

        /**
         * Times over ssh are expressed as seconds since 01 Jan 1970.
         */
        @Override
        void setLastModified(final Path path, final long when) throws IOException {
            ftpClient.setTimeModified(SFTPPath.of(path), when);
        }
    }

    /**
     * Implements the helper for a local instance (one co-located with the DAS).
     */
    private static class LocalHelper extends SecureAdminBootstrapHelper {
        private final URI existingInstanceDirURI;
        private final URI newInstanceDirURI;

        private LocalHelper(final File existingInstanceDir, final File newInstanceDir) {
            this.existingInstanceDirURI = existingInstanceDir.toURI();
            this.newInstanceDirURI = newInstanceDir.toURI();
        }

        @Override
        protected void mkdirs(Path dir) {
            final File newDir = Path.of(newInstanceDirURI).resolve(dir).toFile();
            if (!newDir.exists() && !newDir.mkdirs()) {
                throw new RuntimeException(Strings.get("secure.admin.boot.errCreDir", newDir.getAbsolutePath()));
            }
        }

        @Override
        public void copyBootstrapFiles() throws IOException {
            for (Path relativePathToFile : SECURE_ADMIN_FILE_REL_URIS_TO_COPY) {
                final File origin = Path.of(existingInstanceDirURI).resolve(relativePathToFile).toFile();
                final File dest = Path.of(newInstanceDirURI).resolve(relativePathToFile).toFile();
                FileUtils.copy(origin, dest);
            }
        }

        @Override
        protected void backdateInstanceDomainXML() throws BootstrapException {
            final File newDomainXMLFile = Path.of(newInstanceDirURI).resolve(DOMAIN_XML_PATH).toFile();
            if (!newDomainXMLFile.setLastModified(0)) {
                throw new RuntimeException(Strings.get("secure.admin.boot.errSetLastMod", newDomainXMLFile.getAbsolutePath()));
            }
        }
    }

    public static class BootstrapException extends Exception {
        private static final long serialVersionUID = -5488899043810477670L;

        public BootstrapException(final String message, final Exception ex) {
            super(message + "; Cause: " + ex.getMessage(), ex);
        }

        public BootstrapException(final Exception ex) {
            super(ex.getMessage(), ex);
        }

        public BootstrapException(final String msg) {
            super(msg);
        }
    }
}
