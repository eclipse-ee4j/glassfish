/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SFTPFileSystemProvider extends FileSystemProvider {
    private static final String SCHEME = "sftp";

    @Override
    public String getScheme() {
        return SCHEME;
    }

    /**
     * Create a new filesystem from URI like: sftp://username:password@host:port/path
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        throw new FileSystemNotFoundException("This SFTP filesystem cannot be created, it is just a mock.");
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        throw new FileSystemNotFoundException("SFTP filesystem needs to be created explicitly");
    }

    @Override
    public Path getPath(URI uri) {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public SeekableByteChannel newByteChannel(
            Path path,
            Set<? extends OpenOption> options,
            FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(
            Path path,
            DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public boolean isSameFile(Path path1, Path path2) throws IOException {
        return path1.equals(path2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        // SFTP doesn't have a native "hidden" attribute
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }


    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
        throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("This SFTP filesystem is just a mock.");
    }
}

