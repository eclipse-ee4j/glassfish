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

package com.sun.enterprise.glassfish.bootstrap.launch;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.glassfish.main.jdke.cl.GlassfishUrlClassLoader;


/**
 * This classloader is located between OSGI classloader and system JDK extensions classloader,
 * so it can provide early initialized libraries (before OSGI startup, so even OSGI can use them).
 * <p>
 * List:
 * <ul>
 * <li>GlassFish OSGI Bootstrap
 * </ul>
 *
 * @author David Matejcek
 */
public class GlassfishOsgiBootstrapClassLoader extends GlassfishUrlClassLoader {

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
    public GlassfishOsgiBootstrapClassLoader(final File glassfishDir, final ClassLoader parent) throws IOException {
        super("GlassFishOsgiBootstrap", createUrls(glassfishDir), parent);
    }


    public void launchGlassFishServer(final Properties properties) {
        try {
            @SuppressWarnings("unchecked")
            final Class<Launcher> launcherClass = (Class<Launcher>) loadClass(
                "org.glassfish.main.boot.osgi.GlassFishOsgiLauncher");
            final Launcher launcher = launcherClass.getDeclaredConstructor(ClassLoader.class).newInstance(this);
            launcher.launch(properties);
        } catch (Exception e) {
            throw new Error("Failed to launch GlassFish Server!", e);
        }
    }


    private static URL[] createUrls(final File glassfishDir) throws IOException {
        final List<URL> urls = new ArrayList<>();
        final File libDir = glassfishDir.toPath().resolve(Path.of("lib", "bootstrap")).toFile();
        urls.add(getURL(libDir, "glassfish-osgi-bootstrap"));
        return urls.toArray(URL[]::new);
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
