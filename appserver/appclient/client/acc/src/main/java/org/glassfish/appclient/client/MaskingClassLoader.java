/*
 * Copyright (c) 2008, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * {@link ClassLoader} that masks a specified set of classes from its parent class loader.
 *
 * <p>
 * This code is used to create an isolated environment.
 *
 * @author Jerome Dochez
 */
public class MaskingClassLoader extends ClassLoader {

    private final Set<String> punchins = new HashSet<String>();
    private final String[] multiples;

    private final boolean useExplicitCallsToFindSystemClass;

    public MaskingClassLoader(ClassLoader parent, Collection<String> punchins, Collection<String> multiples) {
        this(parent, punchins, multiples, true /* use explicit calls to findSystemClass */);
    }

    /**
     * Creates a new masking class loader letting a set of defined packages be loaded by the parent classloader. Multiples
     * packages can be specified so that only the parent package needs to be provided.
     *
     * @param parent    the parent classloader to delegate actual loading from when punchin is allowed
     * @param punchins  list of packages allowed to be visible from the parent
     * @param multiples list of parent packages allowed to be visible from the parent class loader
     */
    public MaskingClassLoader(ClassLoader parent, Collection<String> punchins, Collection<String> multiples,
            boolean useExplicitCallsToFindSystemClass) {
        super(parent);
        this.punchins.addAll(punchins);
        this.multiples = multiples.toArray(new String[multiples.size()]);
        this.useExplicitCallsToFindSystemClass = useExplicitCallsToFindSystemClass;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // I do not mask java packages, and I only mask javax. stuff for now.
        try {
            if (useExplicitCallsToFindSystemClass) {
                return findSystemClass(name);
            }
        } catch (ClassNotFoundException e) {

        }
        if (isDottedNameLoadableByParent(name)) {
            return super.loadClass(name, resolve);
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        if (isDottedNameLoadableByParent(resourceToDotted(name))) {
            return super.getResource(name);
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (isDottedNameLoadableByParent(resourceToDotted(name))) {
            return super.getResources(name);
        }

        return new Enumeration<URL>() {

            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public URL nextElement() {
                throw new NoSuchElementException();
            }
        };

    }

    private String resourceToDotted(String name) {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        return name.replace("/", ".");
    }

    protected boolean isDottedNameLoadableByParent(final String name) {
        if (!(name.startsWith("javax.") || name.startsWith("org."))) {
            return true;
        }

        String packageName = name.substring(0, name.lastIndexOf("."));
        if (punchins.contains(packageName)) {
            return true;
        }

        for (String multiple : multiples) {
            if (name.startsWith(multiple)) {
                return true;
            }
        }

        return false;
    }

}
