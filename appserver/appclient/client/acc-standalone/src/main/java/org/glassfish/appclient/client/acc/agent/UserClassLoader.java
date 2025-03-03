/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.appclient.client.acc.agent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.glassfish.embeddable.client.ApplicationClientClassLoader;
import org.glassfish.embeddable.client.ApplicationClientContainer;

/**
 * Dependencies from -classpath argument, APPCPATH environment option, current directory
 */
public class UserClassLoader extends URLClassLoader implements ApplicationClientClassLoader {

    private static final Function<Path, URL> PATH_TO_URL = p -> {
        try {
            return p.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not convert path to url: " + p, e);
        }
    };


    UserClassLoader() {
        super("User", createClassPath(), new GFDependenciesClassLoader());
    }

    private volatile ApplicationClientContainer container;

    @Override
    public ApplicationClientContainer getApplicationClientContainer() {
        return container;
    }


    @Override
    public void setApplicationClientContainer(ApplicationClientContainer container) {
        this.container = container;
    }


    /**
     * Returns class name, hash code and list of managed urls and info about parent.
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(1024);
        text.append(getClass().getName()).append('@').append(Integer.toHexString(hashCode()));
        text.append("[name=").append(getName()).append(']');
        text.append("[container=").append(container).append(']');
        text.append(", urls=[\n");
        Arrays.stream(getURLs()).forEach(u -> text.append(u).append('\n'));
        text.append(']');
        text.append(", parent=").append(getParent());
        return text.toString();
    }


    private static URL[] createClassPath() {
        final Stream<Path> hardCodedPaths = convertClassPathToPaths(".");
        final Stream<Path> cpPaths = convertClassPathToPaths(System.getProperty("java.class.path"));
        final Stream<Path> envPaths = convertClassPathToPaths(System.getenv("APPCPATH"));
        final Predicate<Path> filterOutGfClient = f -> !f.endsWith(Path.of("gf-client.jar"));
        return Stream.of(hardCodedPaths, cpPaths, envPaths).reduce(Stream::concat).orElseGet(Stream::empty)
            .map(Path::toAbsolutePath).map(Path::normalize).distinct().filter(filterOutGfClient).map(PATH_TO_URL)
            .toArray(URL[]::new);
    }

    private static Stream<Path> convertClassPathToPaths(final String classPath) {
        if (classPath == null || classPath.isBlank()) {
            return Stream.empty();
        }
        try {
            return Stream.of(classPath.split(File.pathSeparator)).map(File::new).map(File::toPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse the classpath: " + classPath, e);
        }
    }
}
