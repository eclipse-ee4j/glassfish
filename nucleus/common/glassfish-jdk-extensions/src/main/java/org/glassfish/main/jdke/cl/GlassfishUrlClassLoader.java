/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.jdke.cl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Enumeration;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;


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

    private static final Logger LOG = System.getLogger(GlassfishUrlClassLoader.class.getName());

    static {
        registerAsParallelCapable();
    }


    /**
     * Initializes the internal classpath.
     *
     * @param name
     * @param urls
     */
    public GlassfishUrlClassLoader(String name, URL[] urls) {
        super(name, urls, ClassLoader.getSystemClassLoader());
    }


    /**
     * Initializes the internal classpath.
     *
     * @param name
     * @param urls
     * @param parent
     */
    public GlassfishUrlClassLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, urls, parent);
    }


    /**
     * Initializes the internal classpath.
     *
     * @param name
     * @param urls
     * @param parent
     * @param factory
     */
    public GlassfishUrlClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(name, urls, parent, factory);
    }

    // turn protected to public
    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }


    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        LOG.log(TRACE, "findClass(name={0})", name);
        return super.findClass(name);
    }


    @Override
    public URL findResource(final String name) {
        LOG.log(TRACE, "findResource(name={0})", name);
        return super.findResource(name);
    }


    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        LOG.log(TRACE, "findResources(name={0})", name);
        return super.findResources(name);
    }


    @Override
    public InputStream getResourceAsStream(String name) {
        LOG.log(TRACE, "getResourceAsStream(name={0})", name);
        return super.getResourceAsStream(name);
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        LOG.log(DEBUG, "loadClass(name={0}, resolve={1})", name, resolve);
        return super.loadClass(name, resolve);
    }


    @Override
    protected Package definePackage(String name, String specTitle, String specVersion, String specVendor,
        String implTitle, String implVersion, String implVendor, URL sealBase) {
        LOG.log(DEBUG,
            "definePackage(name={0}, specTitle={1}, specVersion={2}, specVendor={3}, implTitle={4}, implVersion={5}, implVendor={6}, sealBase={7})",
            name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
        return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
    }


    @Override
    public void close() throws IOException {
        LOG.log(DEBUG, "close()");
        super.close();
    }


    /**
     * Returns class name, hash code and list of managed urls.
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(1024);
        text.append(getClass().getName()).append('@').append(Integer.toHexString(hashCode()));
        text.append("[name=").append(getName()).append("], urls=[\n");
        Arrays.stream(getURLs()).forEach(u -> text.append(u).append('\n'));
        text.append(']');
        return text.toString();
    }
}
