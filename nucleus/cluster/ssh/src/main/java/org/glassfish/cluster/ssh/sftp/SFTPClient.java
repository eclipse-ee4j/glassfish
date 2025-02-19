/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cluster.ssh.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.glassfish.cluster.ssh.launcher.RemoteSystemCapabilities;
import org.glassfish.cluster.ssh.launcher.SSHException;
import org.glassfish.cluster.ssh.launcher.SSHSession;

import static java.lang.System.Logger.Level.TRACE;

/**
 * SFTP client.
 *
 * @see SSHSession
 */
public class SFTPClient implements AutoCloseable {
    private static final Logger LOG = System.getLogger(SFTPClient.class.getName());
    /**
     * This is required on Linux based hosts; directories are not in the list on Windows.
     */
    private static final Predicate<LsEntry> PREDICATE_NO_DOTS = p -> !".".equals(p.getFilename())
        && !"..".equals(p.getFilename());

    private final ChannelSftp sftpChannel;

    /**
     * Creates the instance which immediately tries to open the SFTP connection..
     *
     * @param channel
     * @throws SSHException if the connection could not be established, usually because the SSH
     *             server doesn't support SFTP.
     */
    public SFTPClient(ChannelSftp channel) throws SSHException {
        this.sftpChannel = channel;
        try {
            this.sftpChannel.connect();
        } catch (JSchException e) {
            throw new SSHException("Failed to connect to the SFTP server. Is it correctly configured on the server?", e);
        }
    }

    /**
     * Close the SFTP connection and free any resources associated with it.
     * close() should be called when you are done using the SFTPClient
     */
    @Override
    public void close() {
        if (sftpChannel != null) {
            sftpChannel.disconnect();
        }
    }


    /**
     * @return Configured SSH server home directory. Usually user's home directory.
     * @throws SSHException Command failed.
     */
    public SFTPPath getHome() throws SSHException {
        try {
            return SFTPPath.of(sftpChannel.getHome());
        } catch (SftpException e) {
            throw new SSHException("Could not resolve SFTP Home path.", e);
        }
    }


    /**
     * Makes sure that the directory exists, by creating it if necessary.
     * @param path the remote path
     * @throws SSHException Command failed.
     */
    public void mkdirs(SFTPPath path) throws SSHException {
        if (existsDirectory(path)) {
            return;
        }
        SFTPPath current = SFTPPath.ofAbsolutePath();
        for (Path part : path) {
            current = current.resolve(part);
            if (existsDirectory(current)) {
                continue;
            }
            try {
                sftpChannel.mkdir(current.toString());
            } catch (SftpException e) {
                throw new SSHException("Failed to create the directory " + path + '.', e);
            }
        }
    }


    /**
     * @param path
     * @return true if the path exists and is a directory
     * @throws SSHException Command failed.
     */
    public boolean existsDirectory(SFTPPath path) throws SSHException {
        SftpATTRS attrs = stat(path);
        return attrs != null && attrs.isDir();
    }


    /**
     * @param path
     * @return true if the path exists, is a directory and is empty.
     * @throws SSHException Command failed.
     */
    public boolean isEmptyDirectory(SFTPPath path) throws SSHException {
        SftpATTRS attrs = stat(path);
        return attrs != null && attrs.isDir() && ls(path, e -> true).isEmpty();
    }


    /**
     * Recursively deletes the specified directory.
     *
     * @param path
     * @param onlyContent
     * @param exclude
     * @throws SSHException Command failed. Usually some file is not removable or is open.
     */
    public void rmDir(SFTPPath path, boolean onlyContent, SFTPPath... exclude) throws SSHException {
        if (!exists(path)) {
            return;
        }
        // We use recursion while the channel is stateful
        cd(path.getParent());
        List<LsEntry> content = lsDetails(path, p -> true);
        for (LsEntry entry : content) {
            final String filename = entry.getFilename();
            final SFTPPath entryPath = path.resolve(filename);
            if (isExcludedFromDeletion(filename, exclude)) {
                LOG.log(TRACE, "Skipping excluded {0}", entryPath);
                continue;
            }
            if (entry.getAttrs().isDir()) {
                rmDir(entryPath, false, getSubDirectoryExclusions(filename, exclude));
            } else {
                LOG.log(TRACE, "Deleting file {0}", entryPath);
                rm(entryPath);
            }
        }
        if (!onlyContent) {
            try {
                sftpChannel.cd(path.getParent().toString());
                LOG.log(TRACE, "Deleting directory {0}", path);
                sftpChannel.rmdir(path.toString());
            } catch (SftpException e) {
                throw new SSHException("Failed to delete directory: " + path + '.', e);
            }
        }
    }


    private static boolean isExcludedFromDeletion(String firstName, SFTPPath... exclusions) {
        if (exclusions == null) {
            return false;
        }
        return Arrays.stream(exclusions).filter(p -> p.getNameCount() == 1)
            .anyMatch(p -> p.getFileName().toString().equals(firstName));
    }


    private static SFTPPath[] getSubDirectoryExclusions(String firstName, SFTPPath... exclusions) {
        if (exclusions == null) {
            return new SFTPPath[0];
        }
        return Arrays.stream(exclusions).filter(p -> p.getNameCount() > 1).filter(p -> p.startsWith(firstName))
            .map(p -> p.subpath(1, p.getNameCount())).toArray(SFTPPath[]::new);
    }


    /**
     * Upload local file to the remote file.
     *
     * @param localFile
     * @param remoteFile
     * @throws SSHException Command failed.
     */
    public void put(File localFile, SFTPPath remoteFile) throws SSHException {
        try {
            sftpChannel.cd(remoteFile.getParent().toString());
            sftpChannel.put(localFile.getAbsolutePath(), remoteFile.toString());
        } catch (SftpException e) {
            throw new SSHException(
                "Failed to upload the local file " + localFile + " to remote file " + remoteFile + '.', e);
        }
    }


    /**
     * Downloads the remote file to the local file. The local file must not exist yet.
     *
     * @param remoteFile
     * @param localFile
     * @throws SSHException Command failed.
     */
    public void download(SFTPPath remoteFile, Path localFile) throws SSHException {
        try (InputStream inputStream = sftpChannel.get(remoteFile.toString())) {
            Files.copy(inputStream, localFile);
        } catch (SftpException | IOException e) {
            throw new SSHException(
                "Failed to download the remote file " + remoteFile + " to local file " + localFile + '.', e);
        }
    }


    /**
     * Deletes the specified remote file.
     *
     * @param path
     * @throws SSHException
     */
    public void rm(SFTPPath path) throws SSHException {
        try {
            sftpChannel.cd(path.getParent().toString());
            sftpChannel.rm(path.toString());
        } catch (SftpException e) {
            throw new SSHException("Failed to remove path " + path + '.', e);
        }
    }


    /**
     * Rename/Move the remote file or directory to the new path.
     *
     * @param remoteSource
     * @param remoteTarget
     * @throws SSHException Command failed.
     */
    public void mv(SFTPPath remoteSource, SFTPPath remoteTarget) throws SSHException {
        try {
            sftpChannel.cd(remoteTarget.getParent().toString());
            sftpChannel.rename(remoteSource.toString(), remoteTarget.toString());
        } catch (SftpException e) {
            throw new SSHException("Failed to move the directory " + remoteSource + " to " + remoteTarget + '.', e);
        }
    }


    /**
     * @param path
     * @return true if the remote path exists.
     * @throws SSHException Command failed.
     */
    public boolean exists(SFTPPath path) throws SSHException {
        return stat(path) != null;
    }


    /**
     * Providing file details. This method follows symlinks.
     *
     * @param path
     * @return {@link SftpATTRS} or null if the path doesn't exist.
     * @throws SSHException Command failed.
     */
    public SftpATTRS stat(SFTPPath path) throws SSHException {
        try {
            return sftpChannel.stat(path.toString());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return null;
            }
            throw new SSHException("Failed to call SFTP stat for " + path + '.', e);
        }
    }


    /**
     * Providing file details. This method does not follow symlinks.
     *
     * @param path
     * @return {@link SftpATTRS} or null if the path doesn't exist.
     * @throws SSHException Command failed.
     */
    public SftpATTRS lstat(SFTPPath path) throws SSHException {
        try {
            return sftpChannel.lstat(path.toString());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return null;
            }
            throw new SSHException("Failed to call SFTP lstat for " + path + '.', e);
        }
    }


    /**
     * Calls SFTP MTIME for given path and millis.
     *
     * @param path
     * @param millisSinceUnixEpoch
     * @throws SSHException Command failed.
     */
    public void setTimeModified(SFTPPath path, long millisSinceUnixEpoch) throws SSHException {
        try {
            sftpChannel.setMtime(path.toString(), (int) (millisSinceUnixEpoch / 1000));
        } catch (SftpException e) {
            throw new SSHException("Failed to set time modification for path " + path + '.', e);
        }
    }


    /**
     * Calls SFTP CHMOD. Note that this command is not supported on Windows.
     *
     * @param path
     * @param permissions
     * @throws SSHException Command failed.
     * @see RemoteSystemCapabilities#isChmodSupported()
     */
    public void chmod(SFTPPath path, int permissions) throws SSHException {
        try {
            sftpChannel.chmod(permissions, path.toString());
        } catch (SftpException e) {
            throw new SSHException(
                "Failed to call chmod for remote path " + path + " and permissions " + permissions + ".", e);
        }
    }


    /**
     * Changes the current directory on the remote SFTP server.
     *
     * @param path
     * @throws SSHException Command failed.
     */
    public void cd(SFTPPath path) throws SSHException {
        try {
            sftpChannel.cd(path.toString());
        } catch (SftpException e) {
            throw new SSHException("Failed to change the remote directory to " + path + '.', e);
        }
    }


    /**
     * Lists file names the given remote directory. Excludes current directory and the parent
     * directory links (dot, double dot)
     *
     * @param path
     * @param filter additional filter, ie. to filter by file extension. Must not be null.
     * @return list of file names in the given directory
     * @throws SSHException Command failed.
     */
    public List<String> ls(SFTPPath path, Predicate<LsEntry> filter) throws SSHException {
        try {
            return sftpChannel.ls(path.toString()).stream().filter(filter.and(PREDICATE_NO_DOTS))
                .map(LsEntry::getFilename).collect(Collectors.toList());
        } catch (SftpException e) {
            throw new SSHException("Failed to list remote directory " + path + '.', e);
        }
    }


    /**
     * Lists entries in the given remote directory. Excludes current directory and the parent
     * directory links (dot, double dot)
     *
     * @param path
     * @param filter additional filter, ie. to filter by file extension.
     * @return list of file names in the given directory
     * @throws SSHException Command failed.
     */
    public List<LsEntry> lsDetails(SFTPPath path, Predicate<LsEntry> filter) throws SSHException {
        try {
            return sftpChannel.ls(path.toString()).stream().filter(filter.and(PREDICATE_NO_DOTS))
                .collect(Collectors.toList());
        } catch (SftpException e) {
            throw new SSHException("Failed to list remote directory " + path + '.', e);
        }
    }
}
