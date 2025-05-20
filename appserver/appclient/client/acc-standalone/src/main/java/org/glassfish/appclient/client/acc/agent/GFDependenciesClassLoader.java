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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This class loader was created to minimize GlassFish dependencies on the command line.
 * It can be used as a system class loader too.
 * It should not depend on other classes except JDK.
 */
public final class GFDependenciesClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    private static final Function<File, URL> TO_URL = file -> {
        try {
            return file.toURI().toURL();
        } catch (IOException e) {
            throw new Error("Could not resolve url: " + file, e);
        }
    };

    private static final boolean AS_TRACE = Boolean.parseBoolean(System.getenv("AS_TRACE"));

    /**
     * This constructor is used when you instantiate this class loader in your code.
     */
    GFDependenciesClassLoader() {
        super("GlassFish", findGlassFishJars(), ClassLoader.getPlatformClassLoader());
    }


    /**
     * Returns class name, hash code and list of managed urls and info about parent.
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(1024);
        text.append(getClass().getName()).append('@').append(Integer.toHexString(hashCode()));
        text.append("[name=").append(getName()).append(']');
        if (AS_TRACE) {
            text.append(", urls=[\n");
            Arrays.stream(getURLs()).forEach(u -> text.append(u).append('\n'));
            text.append(']');
            text.append(", parent=").append(getParent());
        }
        return text.toString();
    }

    private static URL[] findGlassFishJars() {
        Path gfHome = getGlassFishHome();
        Path gfModules = gfHome.resolve("modules");
        Set<String> ignoredFiles = Set.of(
            // TODO: Which should be here?
        );
        Predicate<Path> moduleFilter = path -> {
            String fileName = path.getFileName().toString();
            if (!fileName.endsWith(".jar") || ignoredFiles.contains(fileName)) {
                return false;
            }
            String relativePath = gfModules.relativize(path).toString();
            return !relativePath.contains("autostart/");
        };
        Path appInstall = gfHome.resolve(Path.of("lib", "install", "applications"));
        Predicate<Path> libFilter = filePath -> {
            String path = appInstall.relativize(filePath).toString();
            return path.endsWith(".jar") && (path.contains("jmsra/") || path.contains("_jdbc_ra/"));
        };
        Path derbyLibDir = gfHome.resolve(Path.of("..", "javadb", "lib")).normalize();
        Predicate<Path> derbyFilter = filePath -> {
            Set<String> files = Set.of("derby.jar", "derbyclient.jar", "derbynet.jar");
            return files.contains(filePath.getFileName().toString());
        };
        Stream<File> modules = findFiles(gfModules, moduleFilter);
        Stream<File> libs = findFiles(appInstall, libFilter);
        Stream<File> derbyLibs = findFiles(derbyLibDir, derbyFilter);
        return Stream.of(modules, libs, derbyLibs).reduce(Stream::concat).orElseGet(Stream::empty).map(TO_URL)
            .toArray(URL[]::new);
    }


    private static Stream<File> findFiles(Path root, Predicate<Path> filter) {
        try {
            return Files.walk(root, 5).filter(filter).map(Path::toFile);
        } catch (IOException e) {
            throw new Error(e);
        }
    }


    private static Path getGlassFishHome() {
        return new File(getMyJar()).getParentFile().getParentFile().toPath();
    }


    private static URI getMyJar() {
        try {
            CodeSource codeSource = GFDependenciesClassLoader.class.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                throw new Error("Unable to detect the current jar file location,"
                    + " because the getCodeSource() or getLocation() method returned null."
                    + " That can happen ie. when you use the boot classloader"
                    + " or a classloader which doesn't use locations.");
            }
            return codeSource.getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new Error("Could not detect the GlassFish lib directory.", e);
        }
    }
}
