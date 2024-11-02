/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.glassfish.bootstrap.cp;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This classloader is located between OSGI classloader and system JDK extensions classloader,
 * so it can provide early initialized libraries (before OSGI startup, so even OSGI can use them).
 * <p>
 * List:
 * <ul>
 * <li>GlassFish Java Util Logging Extension
 * <li>Grizzly NPN API
 * </ul>
 *
 * @author David Matejcek
 */
public class GlassfishBootstrapClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    /**
     * Initializes the classloader.
     *
     * @param glassfishDir - the installation glassfish subdirectory
     * @param parent - parent classloader
     * @throws IOException - if required libraries (internally listed) could not be read
     */
    public GlassfishBootstrapClassLoader(final File glassfishDir, final ClassLoader parent) throws IOException {
        super(createUrls(glassfishDir), parent);
    }


    /**
     * Returns class name, hash code and list of managed urls.
     */
    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) + ": "
            + Arrays.stream(getURLs()).collect(Collectors.toList());
    }


    private static URL[] createUrls(final File glassfishDir) throws IOException {
        final List<URL> urls = new ArrayList<>();
        final File libDir = glassfishDir.toPath().resolve(Paths.get("lib", "bootstrap")).toFile();
        urls.add(getURL(libDir, "glassfish-jul-extension"));
        return urls.toArray(new URL[urls.size()]);
    }


    private static URL getURL(final File dir, final String jarFileName) throws IOException {
        final File file = new File(dir, jarFileName + ".jar");
        try {
            if (!file.canRead()) {
                throw new IOException("The jar file does not exist or cannot be read: " + file);
            }
            return new File(dir, jarFileName + ".jar").toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IOException("Could not convert file " + file + " to url.", e);
        }
    }
}
