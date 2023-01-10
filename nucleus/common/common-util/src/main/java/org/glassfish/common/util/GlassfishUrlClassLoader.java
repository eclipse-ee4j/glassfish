/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.common.util;

import java.io.Closeable;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;


/**
 * {@link URLClassLoader} with logs and overriden {@link #toString()} method so it prints list
 * of managed URLs.
 * <p>
 * This classloader is {@link Closeable}! As of JDK11+ {@link #close()} just closes any
 * unclosed resource streams which could survive the class loader.
 *
 * @author David Matejcek
 */
public class GlassfishUrlClassLoader extends URLClassLoader {

    /**
     * Initializes the internal classpath.
     *
     * @param urls
     */
    public GlassfishUrlClassLoader(URL[] urls) {
        super(urls);
    }


    /**
     * Initializes the internal classpath.
     *
     * @param urls
     * @param parent
     */
    public GlassfishUrlClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }


    /**
     * Initializes the internal classpath.
     *
     * @param urls
     * @param parent
     * @param factory
     */
    public GlassfishUrlClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }


    /**
     * Returns class name, hash code and list of managed urls.
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(1024);
        text.append(getClass().getName()).append('@').append(Integer.toHexString(hashCode())).append("[\n");
        Arrays.stream(getURLs()).forEach(u -> text.append(u).append('\n'));
        text.append(']');
        return text.toString();
    }
}
