/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class covers the resource loading rules of GlassFish.
 * <ul>
 * <li>Some resources can be overriden by the application (ie. services)
 * <li>Some applications may use the <code>delegate</code> attribute to prioritize their classloader
 * over server's own resources.
 * </ul>
 *
 * @author David Matejcek
 */
public class ResourceLocator {

    private static final Logger LOG = Logger.getLogger(ResourceLocator.class.getName());
    private final URLClassLoader classLoader;
    private final ClassLoader delegate;
    private final boolean prioritizeDelegate;

    /**
     * @param classLoader - current classloader used
     * @param delegate - delegate classloader, usually parent or system classloader
     * @param prioritizeDelegate - true if the delegate should be used first. This rule is not
     *            applied for explicitly named resources which not only have higher priority than
     *            delegate's but they do override them. If they are present in current classloader,
     *            the delegate is then not used.
     */
    public ResourceLocator(URLClassLoader classLoader, ClassLoader delegate, boolean prioritizeDelegate) {
        this.classLoader = classLoader;
        this.delegate = delegate;
        this.prioritizeDelegate = prioritizeDelegate;
    }


    /**
     * @param name
     * @return true if the resource present in the current classloader can shadow parent's resource.
     */
    public boolean isOverridableResource(String name) {
        if (name.startsWith("META-INF/services/jakarta.json.spi.JsonProvider")) {
            return true;
        }
        return false;
    }


    /**
     * Finds resources with the given name and returns enumeration providing their locations.
     *
     * @param name
     * @return {@link Enumeration} of {@link URL}s
     * @throws IOException
     */
    public Enumeration<URL> getResources(String name) throws IOException {
        LOG.log(Level.FINEST, "getResources({0})", name);

        @SuppressWarnings("unchecked")
        final Enumeration<URL>[] enums = new Enumeration[2];
        final Enumeration<URL> localResources = classLoader.findResources(name);
        if (localResources.hasMoreElements() && isOverridableResource(name)) {
            return localResources;
        }
        final Enumeration<URL> delegateResources = delegate.getResources(name);
        if (prioritizeDelegate) {
            enums[0] = delegateResources;
            enums[1] = localResources;
        } else {
            enums[1] = delegateResources;
            enums[0] = localResources;
        }

        return new Enumeration<>() {

            int index = 0;

            private boolean next() {
                while (index < enums.length) {
                    if (enums[index] != null && enums[index].hasMoreElements()) {
                        return true;
                    }
                    index++;
                }
                return false;
            }

            @Override
            public boolean hasMoreElements() {
                return next();
            }

            @Override
            public URL nextElement() {
                if (!next()) {
                    throw new NoSuchElementException();
                }
                return enums[index].nextElement();
            }
        };
    }
}
