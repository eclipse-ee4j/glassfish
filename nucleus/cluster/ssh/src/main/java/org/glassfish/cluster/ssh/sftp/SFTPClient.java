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

    public SFTPClient(ChannelSftp channel) throws JSchException {
        this.sftpChannel = channel;
        this.sftpChannel.connect();
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

    public ChannelSftp getSftpChannel() {
        return sftpChannel;
    }


    public Path getHome() throws SftpException {
        return Path.of(sftpChannel.getHome());
    }


    /**
     * Makes sure that the directory exists, by creating it if necessary.
     */
    public void mkdirs(Path path) throws SftpException {
        if (existsDirectory(path)) {
            return;
        }
        Path current = Path.of("/");
        for (Path part : path.normalize()) {
            current = current.resolve(part);
            if (existsDirectory(current)) {
                continue;
            }
            sftpChannel.mkdir(current.toString());
        }
    }

    public boolean existsDirectory(Path path) throws SftpException {
        SftpATTRS attrs = stat(path);
        return attrs != null && attrs.isDir();
    }


    public boolean isEmptyDirectory(Path path) throws SftpException {
        SftpATTRS attrs = stat(path);
        return attrs != null && attrs.isDir() && ls(path, e -> true).isEmpty();
    }


    /**
     * Recursively deletes the specified directory.
     *
     * @param path
     * @param onlyContent
     * @param exclude
     * @throws SftpException
     */
    public void rmDir(Path path, boolean onlyContent, Path... exclude) throws SftpException {
        if (!exists(path)) {
            return;
        }
        // We use recursion while the channel is stateful
        sftpChannel.cd(path.getParent().toString());
        List<LsEntry> content = lsDetails(path, p -> true);
        for (LsEntry entry : content) {
            final String filename = entry.getFilename();
            final Path entryPath = path.resolve(filename);
            if (matches(filename, exclude)) {
                LOG.log(TRACE, "Skipping excluded {0}", entryPath);
                continue;
            }
            if (entry.getAttrs().isDir()) {
                rmDir(entryPath, false, getSubFilter(filename, exclude));
            } else {
                LOG.log(TRACE, "Deleting file {0}", entryPath);
                sftpChannel.rm(entryPath.toString());
            }
        }
        if (!onlyContent) {
            sftpChannel.cd(path.getParent().toString());
            LOG.log(TRACE, "Deleting directory {0}", path);
            sftpChannel.rmdir(path.toString());
        }
    }


    private static boolean matches(String firstName, Path... exclusions) {
        if (exclusions == null) {
            return false;
        }
        return Arrays.stream(exclusions).filter(p -> p.getNameCount() == 1)
            .anyMatch(p -> p.getFileName().toString().equals(firstName));
    }


    private static Path[] getSubFilter(String firstName, Path... exclusions) {
        if (exclusions == null) {
            return new Path[0];
        }
        return Arrays.stream(exclusions).filter(p -> p.getNameCount() > 1).filter(p -> p.startsWith(firstName))
            .map(p -> p.subpath(1, p.getNameCount())).toArray(Path[]::new);
    }


    public void put(File localFile, Path remoteFile) throws SftpException {
        sftpChannel.cd(remoteFile.getParent().toString());
        sftpChannel.put(localFile.getAbsolutePath(), remoteFile.toString());
    }

    /**
     * Deletes the specified remote file.
     *
     * @param path
     * @throws SftpException
     */
    public void rm(Path path) throws SftpException {
        sftpChannel.cd(path.getParent().toString());
        sftpChannel.rm(path.toString());
    }


    /**
     * @return true if the given path exists.
     */
    public boolean exists(Path path) throws SftpException {
        return stat(path) != null;
    }


    /**
     * Graceful stat that returns null if the path doesn't exist.
     * This method follows symlinks.
     */
    public SftpATTRS stat(Path path) throws SftpException {
        try {
            return sftpChannel.stat(path.toString());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Graceful lstat that returns null if the path doesn't exist.
     * This method does not follow symlinks.
     */
    public SftpATTRS lstat(Path path) throws SftpException {
        try {
            return sftpChannel.lstat(path.toString());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return null;
            }
            throw e;
        }
    }

    public void setTimeModified(Path path, long millisSinceUnixEpoch) throws SftpException {
        sftpChannel.setMtime(path.toString(), (int) (millisSinceUnixEpoch / 1000));
    }

    public void chmod(Path path, int permissions) throws SftpException {
        sftpChannel.chmod(permissions, path.toString());
    }

    public void cd(Path path) throws SftpException {
        sftpChannel.cd(path.toString());
    }


    public void download(Path remoteFile, Path localFile) throws IOException, SftpException {
        try (InputStream inputStream = sftpChannel.get(remoteFile.toString())) {
            Files.copy(inputStream, localFile);
        }
    }

    public List<String> ls(Path path, Predicate<LsEntry> filter) throws SftpException {
        return sftpChannel.ls(path.toString()).stream().filter(filter.and(PREDICATE_NO_DOTS)).map(LsEntry::getFilename)
            .collect(Collectors.toList());
    }


    public List<LsEntry> lsDetails(Path path, Predicate<LsEntry> filter) throws SftpException {
        return sftpChannel.ls(path.toString()).stream().filter(filter.and(PREDICATE_NO_DOTS))
            .collect(Collectors.toList());
    }
}
