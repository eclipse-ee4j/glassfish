/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * ClassPath utilities required by GlassFish clients.
 *
 * @author David Matejcek
 */
final class ClassPathUtils {

    private static final Function<Path, URL> TO_URL = p -> {
        try {
            return p.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not convert path to url: " + p, e);
        }
    };


    /**
     * @param clientJarFile
     * @return Main-Class attributer of the manifest file.
     */
    static String getMainClass(File clientJarFile) {
        try (JarFile jarFile = new JarFile(clientJarFile)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return null;
            }
            Attributes mainAttributes = manifest.getMainAttributes();
            return mainAttributes.getValue("Main-Class");
        } catch (IOException e) {
            throw new IllegalStateException("Could not detect the main class from the manifest of " + clientJarFile, e);
        }
    }


    static String getClassPathForGfClient(String clientJarPath) {
        URL[] classpath = getJavaClassPathForAppClient();
        if (classpath.length == 0) {
            return clientJarPath;
        }
        return clientJarPath + File.pathSeparator + Stream.of(classpath).map(ClassPathUtils::convertToString)
            .distinct().collect(Collectors.joining(File.pathSeparator));
    }


    private static URL[] getJavaClassPathForAppClient() {
        final List<Path> paths = convertClassPathToPaths(System.getProperty("java.class.path"));
        final List<URL> result = new ArrayList<>();
        for (Path path : paths) {
            result.add(TO_URL.apply(path));
        }
        result.addAll(convertClassPathToURLs(System.getenv("APPCPATH")));
        return result.toArray(URL[]::new);
    }


    private static List<URL> convertClassPathToURLs(final String classPath) {
        return convertClassPathToPaths(classPath).stream().map(TO_URL).collect(Collectors.toList());
    }


    private static List<Path> convertClassPathToPaths(final String classPath) {
        if (classPath == null || classPath.isBlank()) {
            return emptyList();
        }
        try {
            String[] paths = classPath.split(File.pathSeparator);
            final List<Path> result = new ArrayList<>(paths.length);
            for (String classPathElement : paths) {
                result.add(new File(classPathElement.trim()).toPath().normalize());
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse the classpath: " + classPath, e);
        }
    }


    private static String convertToString(URL url) {
        try {
            return new File(url.toURI()).toPath().toAbsolutePath().normalize().toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot convert to URI string: " + url, e);
        }
    }
}
