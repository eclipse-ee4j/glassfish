/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Arrays;

/**
 * Used as the system class loader during app client launch.
 * <p>
 * The main role of this class loader is to find a splash screen image that might be specified in
 * the manifest of the app client.
 * Once the ACC begins working it will create an ACCClassLoader for loading client classes and
 * locating client resources.
 * <p>
 * This class and it's dependencies must not use logging, which could cause recursion in class
 * loading. So don't extend GlassfishUrlClassLoader. Reproducer: TCK tests use this classloader.
 * <p>
 * The name of this class must not be changed - it is explicitly used in the TCK Platform Test
 * package.
 *
 * @author tjquinn
 * @author David Matejcek
 */
public class ACCAgentClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    /**
     * This constructor is used by the VM to create a system class loader (as specified by
     * -Djava.system.class.loader on the java command created from the appclient script).
     * <p>
     * This class loader ignores the parent and uses {@link GFDependenciesClassLoader}
     * with the {@link ClassLoader#getPlatformClassLoader()} as its parent instead.
     */
    public ACCAgentClassLoader(ClassLoader parent) {
        super("Agent", new URL[0], new UserClassLoader());
    }

    /**
     * A custom system class loader need to define this method in order to load the java agent.
     *
     * @param path
     * @throws MalformedURLException
     */
    public void appendToClassPathForInstrumentation(String path) throws MalformedURLException {
        addURL(new File(path).toURI().toURL());
    }

    /**
     * Returns class name, hash code and list of managed urls and info about parent.
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(1024);
        text.append(getClass().getName()).append('@').append(Integer.toHexString(hashCode()));
        text.append("[name=").append(getName()).append("], urls=[\n");
        Arrays.stream(getURLs()).forEach(u -> text.append(u).append('\n'));
        text.append(']');
        text.append(", parent=").append(getParent());
        return text.toString();
    }
}
