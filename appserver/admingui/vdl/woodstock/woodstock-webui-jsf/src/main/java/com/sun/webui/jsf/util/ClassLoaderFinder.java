/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.util;

/**
 * Strategies for finding the current ClassLoader. For example,
 * {@code Thread.currentThread().getContextClassLoader()}
 * does not work during design time, where the notion of the  class-path is more
 * constrained. Make sure you use this utility when you need to get hold of
 * the current loader.
 */
public final class ClassLoaderFinder {

    /**
     * Optional Special ClassLoader to use.
     */
    private static ClassLoader customClassLoader = null;

    /**
     * Cannot be instanciated.
     */
    private ClassLoaderFinder() {
    }

    /**
     * Set a preferred ClassLoader to use for loading resources such
     * as themes and messages (via {@link #getCurrentLoader}). If not set
     * (e.g. is null), the current thread's context class loader will be
     * used.
     * @param cLoader The new class loader to use
     */
    public static void setCustomClassLoader(final ClassLoader cLoader) {
        ClassLoaderFinder.customClassLoader = cLoader;
    }

    /**
     * Return the best class loader to use for loading resources.
     * This is normally the thread context class loader but can
     * overridden using {@link #setCustomClassLoader}.
     * @param fallbackClass If there is no context class loader, fall
     * back to using the class loader for the given object
     * @return A ClassLoader to use for loading resources
     */
    public static ClassLoader getCurrentLoader(final Object fallbackClass) {
        ClassLoader loader = customClassLoader;
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }
}
