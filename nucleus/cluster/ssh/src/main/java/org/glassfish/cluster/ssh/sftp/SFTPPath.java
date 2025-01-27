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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * This class "parasites" on {@link Path} perfect features, however you have to respect that
 * paths which it represents are remote parts, despite this path is backed by local file system.
 * <p>
 * It is possible that later we will change the internal implementation, however for out purpose
 * it seems it works well.
 */
public final class SFTPPath implements Path {

    private static final SFTPFileSystem FS = new SFTPFileSystem(new SFTPFileSystemProvider());
    private static final String ROOT = FS.getSeparator();
    private static final SFTPPath PATH_ROOT = new SFTPPath(true);

    private boolean absolute;
    private List<String> path;

    private SFTPPath(boolean absolute, String... path) {
        this.absolute = absolute;
        this.path = List.of(path);
    }

    private SFTPPath(boolean absolute, List<String> path) {
        this.absolute = absolute;
        this.path = path;
    }

    @Override
    public FileSystem getFileSystem() {
        return FS;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public SFTPPath getRoot() {
        return PATH_ROOT;
    }

    @Override
    public SFTPPath getFileName() {
        int length = getNameCount();
        if (length == 0) {
            return null;
        }
        return new SFTPPath(false, path.get(length - 1));
    }

    @Override
    public SFTPPath getParent() {
        int length = getNameCount();
        if (length == 0) {
            return null;
        }
        if (length == 1) {
            return getRoot();
        }
        return new SFTPPath(absolute, path.subList(0, length - 1));
    }

    @Override
    public int getNameCount() {
        return path.size();
    }

    @Override
    public SFTPPath getName(int index) {
        return new SFTPPath(false, path.get(index));
    }

    @Override
    public SFTPPath subpath(int beginIndex, int endIndex) {
        int length = getNameCount();
        if (beginIndex == 0 && endIndex == length) {
            return this;
        }
        if (beginIndex < 0) {
            throw new IllegalArgumentException("The beginIndex is lower than zero");
        }
        if (beginIndex >= length) {
            throw new IllegalArgumentException("The beginIndex is higher than length");
        }
        if (endIndex > length) {
            throw new IllegalArgumentException("The endIndex is higher than length");
        }
        if (beginIndex >= endIndex) {
            throw new IllegalArgumentException("The beginIndex is not lower than endIndex");
        }
        return new SFTPPath(absolute && beginIndex == 0, path.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(String other) {
        SFTPPath theother = of(other);
        return startsWith(theother);
    }

    @Override
    public boolean startsWith(Path other) {
        if (other.getNameCount() > getNameCount()) {
            return false;
        }
        if ((isAbsolute() && !other.isAbsolute()) || (!isAbsolute() && other.isAbsolute())) {
            return false;
        }
        Iterator<Path> iOther = other.iterator();
        Iterator<Path> iMy = iterator();
        while(iMy.hasNext() && iOther.hasNext()) {
            if (!iMy.next().toString().equals(iOther.next().toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(String other) {
        SFTPPath theother = of(other);
        return endsWith(theother);
    }

    @Override
    public boolean endsWith(Path other) {
        final int nameCount = getNameCount();
        final int otherNameCount = other.getNameCount();
        if (otherNameCount > nameCount) {
            return false;
        }
        int diff = nameCount - otherNameCount;
        for (int i = other.getNameCount() - 1; i >= 0; i--) {
            if (!getName(i + diff).toString().equals(other.getName(i).toString())) {
                return false;
            }
        }
        return !other.isAbsolute() || isAbsolute();
    }

    /**
     * @param name
     * @return true if the path contains an element of the given name
     */
    public boolean contains(String name) {
        for (String element : path) {
            if (element.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SFTPPath normalize() {
        boolean[] filter = new boolean[getNameCount()];
        for (int i = 0; i < path.size(); i++) {
            String name = path.get(i);
            if (".".equals(name)) {
                filter[i] = true;
            }
            if ("..".equals(name)) {
                boolean found = false;
                for (int j = i - 1; j >= 0; j--) {
                    if (!filter[j]) {
                        filter[j] = true;
                        found = true;
                        break;
                    }
                }
                if (found) {
                    filter[i] = true;
                }
            }
        }
        List<String> filtered = new ArrayList<>(filter.length);
        for (int i = 0; i < filter.length; i++) {
            if (!filter[i]) {
                filtered.add(path.get(i));
            }
        }
        return new SFTPPath(absolute, List.copyOf(filtered));
    }

    @Override
    public SFTPPath resolve(String other) {
        return resolve(of(other));
    }

    @Override
    public SFTPPath resolve(Path other) {
        if (other.isAbsolute()) {
            return of(other);
        }
        if (other.toString().isEmpty()) {
            return this;
        }
        return of(toString() + FS.getSeparator() + other.toString());
    }

    @Override
    public SFTPPath relativize(Path other) {
        throw new UnsupportedOperationException("Unsupported method: relativize");
    }

    @Override
    public SFTPPath resolveSibling(String other) {
        return resolveSibling(of(other));
    }

    @Override
    public SFTPPath resolveSibling(Path other) {
        throw new UnsupportedOperationException("Unsupported method: resolveSibling");
    }

    @Override
    public URI toUri() {
        return URI.create(FS.provider().getScheme() + toString());
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException("Unsupported toFile - we cannot resolve remote files in this class");
    }

    @Override
    public SFTPPath toAbsolutePath() {
        // all SFTP paths we use are absolute.
        throw new UnsupportedOperationException(
            "Unsupported toAbsolutePath - we cannot resolve remote paths in this class");
    }

    @Override
    public SFTPPath toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException(
            "Unsupported toRealPath - we cannot resolve remote paths in this class");
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException(
            "Unsupported resgister watcher - we cannot watch remote paths in this class");
    }

    @Override
    public Iterator<Path> iterator() {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < getNameCount();
            }

            @Override
            public SFTPPath next() {
                if (i < getNameCount()) {
                    SFTPPath result = getName(i);
                    i++;
                    return result;
                }
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public int compareTo(Path other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SFTPPath) {
            return toString().equals(other.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(64);
        boolean isFirst = true;
        if (isAbsolute()) {
            string.append(ROOT);
        }
        for (String name : path) {
            if (isFirst) {
                isFirst = false;
            } else {
                string.append(FS.getSeparator());
            }
            string.append(name);
        }
        return string.toString();
    }

    /**
     * WARNING: The local path might use different file separators, multiple roots, and use
     * different complicated rules which might cause issues when using this method.
     *
     * Converts the file's path to a SFTP path.
     * If the file is absolute, the path will be absolute.
     * The root prefix is removed (ie. <code>C:\</code>).
     * The file separator in SFTP is always </code>/</code>.
     * The result is normalized.
     *
     * @param file
     * @return {@link SFTPPath}
     */
    public static SFTPPath of(File file) {
        return of(file.toPath().normalize());
    }

    /**
     * WARNING: The local path might use different file separators, multiple roots, and use
     * different complicated rules which might cause issues when using this method.
     *
     * Converts the file's path to a SFTP path.
     * If the file is absolute, the path will be absolute.
     * The root prefix is removed (ie. <code>C:\</code>).
     * The file separator in SFTP is always </code>/</code>.
     * The result is normalized.
     *
     * @param path
     * @return {@link SFTPPath}
     */
    public static SFTPPath of(Path path) {
        if (path instanceof SFTPPath) {
            return (SFTPPath) path;
        }
        final Path relativeToRoot;
        if (path.isAbsolute()) {
            relativeToRoot = path.getRoot().relativize(path);
        } else {
            relativeToRoot = path;
        }
        final String fixedSeparators = relativeToRoot.toString().replaceAll("\\\\", "/");
        if (path.isAbsolute()) {
            return PATH_ROOT.resolve(fixedSeparators);
        }
        return of(fixedSeparators).normalize();
    }


    /**
     * Converts the SFTP path as string to the {@link SFTPPath} instance.
     * Do not use this method for generic paths as they may use different roots and different file
     * separators.
     * <p>
     * The file separator in SFTP is always <code>/</code><br>
     * The root in SFTP is always <code>/</code>
     *
     * @param path
     * @return {@link SFTPPath}
     */
    public static SFTPPath of(String path) {
        String[] elements = Arrays.stream(path.split(FS.getSeparator())).filter(Predicate.not(String::isEmpty))
            .toArray(String[]::new);
        return new SFTPPath(path.startsWith(ROOT), elements);
    }

    /**
     * Creates an absolute {@link SFTPPath} using given element names in order.
     *
     * @param path
     * @return {@link SFTPPath}
     */
    public static SFTPPath ofAbsolutePath(String... path) {
        return new SFTPPath(true, path);
    }

    /**
     * Creates a relative {@link SFTPPath} using given element names in order.
     * Relative paths then can be resolved by another relative or absolute path.
     *
     * @param path
     * @return {@link SFTPPath}
     */
    public static SFTPPath ofRelativePath(String... path) {
        return new SFTPPath(false, path);
    }

    /**
     * Creates a relative {@link SFTPPath} with just a single element.
     *
     * @param fileName
     * @return {@link SFTPPath}
     */
    public static SFTPPath ofFileName(String fileName) {
        return new SFTPPath(false, fileName);
    }
}
