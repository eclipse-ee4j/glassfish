/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.web.loader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.stream.Collectors.toList;

/**
 * @author David Matejcek
 */
class RepositoryManager implements Closeable {

    private static final Logger LOG = System.getLogger(RepositoryManager.class.getName());

    /**
     * The list of local repositories, in the order they should be searched
     * for locally loaded classes or resources.
     */
    private final List<Repository> repositories = new ArrayList<>();


    synchronized void addRepository(String repository, File directory) {
        LOG.log(DEBUG, "addRepository(repository={0}, file={1})", repository, directory);
        Objects.requireNonNull(repository);
        Objects.requireNonNull(directory);
        if (!repository.endsWith("/")) {
            throw new IllegalArgumentException("The repository must end with the '/' character!");
        }
        repositories.add(new Repository(repository, directory));
    }


    synchronized List<RepositoryResource> getResources(String name) {
        return repositories.stream().map(r -> new RepositoryResource(r.name + name, new File(r.directory, name)))
            .collect(toList());
    }


    synchronized List<File> getDirectories() {
        return repositories.stream().map(r -> r.directory).distinct().collect(toList());
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(getClass().getSimpleName());
        sb.append('[');
        sb.append(repositories.stream().map(r -> r.toString()).collect(Collectors.joining("\n")));
        sb.append(']');
        return sb.toString();
    }


    @Override
    public void close() throws IOException {
        repositories.clear();
    }

    private static class Repository {

        public final String name;
        public final File directory;

        Repository(String name, File directory) {
            this.name = name;
            this.directory = directory;
        }


        @Override
        public String toString() {
            return name;
        }
    }

    static class RepositoryResource {

        public final String name;
        public final File file;

        RepositoryResource(String name, File file) {
            this.name = name;
            this.file = file;
        }


        @Override
        public String toString() {
            return name;
        }
    }
}
