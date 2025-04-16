/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.main.jdke.cl.GlassfishUrlClassLoader;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * A class loader that loads classes from all jar files in a specified directory.
 */
public class DirectoryClassLoader extends GlassfishUrlClassLoader {

    static {
        registerAsParallelCapable();
    }

    private static final LocalStringsImpl STRINGS = new LocalStringsImpl(DirectoryClassLoader.class);
    private static final int MAX_DEPTH = 5;
    private static final Comparator<Path> FILENAME_COMPARATOR = Comparator.comparing(Path::getFileName);
    private static final Function<Path, URL> MAPPER = p -> {
        try {
            return p.toUri().toURL();
        } catch (final Exception e) {
            throw new IllegalStateException(STRINGS.get("DirError", p));
        }
    };


    /**
     * Initializes a new instance by the jarsAndDirs, filtered and ordered by file names.
     *
     * @param jarsAndDirs
     * @param parent - parent has higher priority.
     */
    public DirectoryClassLoader(final Set<File> jarsAndDirs, final ClassLoader parent) {
        super("AdminCli", getJars(jarsAndDirs), parent);
    }


    /**
     * Create a DirectoryClassLoader to load from jar files in
     * the specified directory, with the specified parent class loader.
     *
     * @param dir the directory of jar files to load from
     * @param parent the parent class loader
     */
    public DirectoryClassLoader(final File dir, final ClassLoader parent) {
        super("AdminCli(" + dir.getName() + ")", getJars(dir), parent);
    }


    private static URL[] getJars(final Set<File> jarsAndDirs) {
        return getJars(jarsAndDirs.toArray(new File[jarsAndDirs.size()]));
    }


    private static URL[] getJars(final File... jarsAndDirs) {
        return Arrays.stream(jarsAndDirs).map(DirectoryClassLoader::getJarPaths).flatMap(Set::stream)
            .sorted(FILENAME_COMPARATOR).map(MAPPER).toArray(URL[]::new);
    }


    private static Set<Path> getJarPaths(final File jarOrDir) {
        if (jarOrDir.isFile()) {
            return Set.of(jarOrDir.toPath());
        }
        try (Stream<Path> stream = Files.walk(jarOrDir.toPath(), MAX_DEPTH)) {
            return stream.filter(path -> !Files.isDirectory(path))
                .filter(path -> path.getFileName().toString().endsWith(".jar")).collect(Collectors.toSet());
        } catch (final IOException e) {
            throw new IllegalStateException(STRINGS.get("DirError", jarOrDir), e);
        }
    }
}
