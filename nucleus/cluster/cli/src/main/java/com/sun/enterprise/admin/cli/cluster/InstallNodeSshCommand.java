/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.cluster.ssh.launcher.SSHException;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.launcher.SSHSession;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.SEVERE;

/**
 * @author Byron Nevins
 */
@Service(name = "install-node-ssh")
@PerLookup
public class InstallNodeSshCommand extends InstallNodeBaseCommand {
    @Param(name = "sshuser", optional = true, defaultValue = "${user.name}")
    private String user;
    @Param(optional = true, defaultValue = "22", name = "sshport")
    int port;
    @Param(optional = true)
    String sshkeyfile;

    //storing password to prevent prompting twice
    private final Map<String, char[]> sshPasswords = new HashMap<>();

    @Override
    String getRawRemoteUser() {
        return user;
    }

    @Override
    int getRawRemotePort() {
        return port;
    }

    @Override
    String getSshKeyFile() {
        return sshkeyfile;
    }

    @Override
    protected void validate() throws CommandException {
        super.validate();
        if (sshkeyfile == null) {
            //if user hasn't specified a key file check if key exists in
            //default location
            File existingKey = SSHUtil.getExistingKeyFile();
            if (existingKey == null) {
                promptPass = true;
            } else {
                sshkeyfile = existingKey.getAbsolutePath();
            }
        } else {
            validateKey(sshkeyfile);
        }

        //we need the key passphrase if key is encrypted
        if (sshkeyfile != null && SSHUtil.isEncryptedKey(new File(sshkeyfile))) {
            sshkeypassphrase = getSSHPassphrase(true);
        }
    }

    @Override
    void copyToHosts(File zipFile, ArrayList<String> binDirFiles) throws CommandException {
        // exception handling is too complicated to mess with in the real method.
        // the idea is to catch everything here and re-throw as one kind
        // the caller is just going to do it anyway so we may as well do it here.
        // And it makes the signature simpler for other subclasses...
        try {
            copyToHostsInternal(zipFile, binDirFiles);
        } catch (CommandException ex) {
            throw ex;
        } catch (IOException e) {
            // Note: CommandException is not printed to logs.
            logger.log(SEVERE,
                "Failed to copy zip file " + zipFile + " and to make binary files " + binDirFiles + " executable.", e);
            throw new CommandException("Failed to copy zip file " + zipFile + " and to make binary files " + binDirFiles
                + " executable. Reason: " + e.getMessage(), e);
        }
    }


    private void copyToHostsInternal(File zipFile, ArrayList<String> binDirFiles)
        throws IOException, CommandException {

        boolean prompt = promptPass;
        for (String host : hosts) {
            File keyFile = getSshKeyFile() == null ? null : new File(getSshKeyFile());
            SSHLauncher sshLauncher = new SSHLauncher(getRemoteUser(), host, getRemotePort(), sshpassword, keyFile, sshkeypassphrase);

            if (getSshKeyFile() != null && !sshLauncher.checkConnection()) {
                // key auth failed, so use password auth
                prompt = true;
            }

            if (prompt) {
                final String sshpass;
                if (sshPasswords.containsKey(host)) {
                    sshpass = String.valueOf(sshPasswords.get(host));
                } else {
                    sshpass = getSSHPassword(host);
                }

                //re-initialize
                sshLauncher = new SSHLauncher(getRemoteUser(), host, getRemotePort(), sshpass, keyFile, sshkeypassphrase);
                prompt = false;
            }

            Path sshInstallDir = Path.of(getInstallDir());
            try (SSHSession session = sshLauncher.openSession(); SFTPClient sftpClient = session.createSFTPClient()) {
                sftpClient.rmDir(sshInstallDir, true);
                if (!sftpClient.exists(sshInstallDir)) {
                    sftpClient.mkdirs(sshInstallDir);
                    if (sshLauncher.getCapabilities().isChmodSupported()) {
                        sftpClient.chmod(sshInstallDir, 0755);
                    }
                }

                final Path remoteZipFile = sshInstallDir.resolve(zipFile.getName());
                logger.info(() -> "Copying " + zipFile + " (" + zipFile.length() + " bytes)" + " to " + host + ":"
                    + sshInstallDir);
                sftpClient.put(zipFile, remoteZipFile);
                logger.finer(() -> "Copied " + zipFile + " to " + host + ":" + remoteZipFile);

                logger.info(() -> "Unpacking " + remoteZipFile + " on " + host + " to " + sshInstallDir);
                session.unzip(remoteZipFile, sshInstallDir);
                logger.finer(() -> "Unpacked " + getArchiveName() + " into " + host + ":" + sshInstallDir);

                logger.info(() -> "Removing " + host + ":" + remoteZipFile);
                sftpClient.rm(remoteZipFile);
                logger.finer(() -> "Removed " + host + ":" + remoteZipFile);

                // zip doesn't retain file permissions, hence executables need
                // to be fixed with proper permissions
                if (sshLauncher.getCapabilities().isChmodSupported()) {
                    logger.info(() -> "Fixing file permissions of all bin files under " + host + ":" + sshInstallDir);
                    try {
                        if (binDirFiles.isEmpty()) {
                            // binDirFiles can be empty if the archive isn't a fresh one
                            searchAndFixBinDirectoryFiles(sshInstallDir, sftpClient);
                        } else {
                            for (String binDirFile : binDirFiles) {
                                sftpClient.chmod(sshInstallDir.resolve(binDirFile), 0755);
                            }
                        }
                        logger.finer(
                            () -> "Fixed file permissions of all bin files under " + host + ":" + sshInstallDir);
                    } catch (SSHException e) {
                        throw new IOException("Could not set permissions on commands in bin directories under "
                            + sshInstallDir + " directory on host " + host + ". Cause: " + e, e);
                    }
                }
            }
        }
    }

    /**
     * Recursively list install dir and identify "bin" directory. Change permissions
     * of files under "bin" directory.
     * @param installDir GlassFish install root
     * @param sftpClient ftp client handle
     * @throws SftpException
     */
    private void searchAndFixBinDirectoryFiles(Path installDir, SFTPClient sftpClient) throws SSHException {
        for (LsEntry directoryEntry : sftpClient.lsDetails(installDir, e -> true)) {
            if (directoryEntry.getAttrs().isDir()) {
                Path subDir = installDir.resolve(directoryEntry.getFilename());
                if (directoryEntry.getFilename().equals("bin")) {
                    fixFilePermissions(subDir, sftpClient);
                } else {
                    searchAndFixBinDirectoryFiles(subDir, sftpClient);
                }
            }
        }
    }

    /**
     * Set permissions of all files under specified directory. Note that this
     * doesn't check the file type before changing the permissions.
     * @param binDir directory where file permissions need to be fixed
     * @param sftpClient ftp client handle
     * @throws SftpException
     */
    private void fixFilePermissions(Path binDir, SFTPClient sftpClient) throws SSHException {
        for (String directoryEntry : sftpClient.ls(binDir, entry -> !entry.getAttrs().isDir())) {
            sftpClient.chmod(binDir.resolve(directoryEntry), 0755);
        }
    }


    @Override
    final void precopy() throws CommandException {
        if (getForce()) {
            return;
        }

        boolean prompt = promptPass;
        for (String host : hosts) {
            File keyFile = getSshKeyFile() == null ? null : new File(getSshKeyFile());
            SSHLauncher sshLauncher = new SSHLauncher(getRemoteUser(), host, getRemotePort(), sshpassword, keyFile, sshkeypassphrase);

            if (keyFile != null && !sshLauncher.checkConnection()) {
                //key auth failed, so use password auth
                prompt = true;
            }

            if (prompt) {
                String sshpass = getSSHPassword(host);
                sshPasswords.put(host, sshpass.toCharArray());
                sshLauncher = new SSHLauncher(getRemoteUser(), host, getRemotePort(), sshpass, keyFile, sshkeypassphrase);
                prompt = false;
            }

            Path sshInstallDir = Path.of(getInstallDir());
            try (SSHSession session = sshLauncher.openSession(); SFTPClient sftpClient = session.createSFTPClient()) {
                if (sftpClient.exists(sshInstallDir)) {
                    checkIfAlreadyInstalled(session, host, sshInstallDir);
                }
            } catch (IOException ex) {
                throw new CommandException(ex);
            }
        }
    }


    /**
     * Determines if GlassFish is installed on remote host at specified location.
     * Uses SSH launcher to execute 'asadmin version'
     *
     * @param host remote host
     */
    private void checkIfAlreadyInstalled(SSHSession session, String host, Path sshInstallDir)
        throws CommandException, SSHException {
        //check if an installation already exists on remote host
        String asadmin = Constants.v4 ? "/lib/nadmin' version --local --terse" : "/bin/asadmin' version --local --terse";
        String cmd = "'" + sshInstallDir + "/" + SystemPropertyConstants.getComponentName() + asadmin;
        int status = session.exec(cmd);
        if (status == 0) {
            throw new CommandException(Strings.get("install.dir.exists", sshInstallDir));
        }
    }
}
