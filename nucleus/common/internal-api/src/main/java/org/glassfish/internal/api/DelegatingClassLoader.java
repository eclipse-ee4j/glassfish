/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.api;

import com.sun.enterprise.module.common_impl.CompositeEnumeration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableList;

/**
 * This class loader has a list of class loaders called as delegates
 * that it uses to find classes.
 * <p>
 * All those delegates must have the same parent as this class loader in order to
 * have a consistent class space. By consistent class space, we mean a class space
 * where no two loaded class have same name. An inconsistent class space can lead
 * to {@link ClassCastException}.
 * <p>
 * This class loader does not define any class, classes are always loaded either by
 * its parent or by one of the delegates.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DelegatingClassLoader extends ClassLoader {

    static {
        registerAsParallelCapable();
    }

    /**
     * This interface is an optimization.
     * <p>
     * The {@code findClass} method of {@link ClassLoader} is usually a protected method.
     * Calling {@code loadClass} on a {@link ClassLoader} is expensive, as it searches
     * the delegation hierarchy before searching in its private space.
     */
    public interface ClassFinder {

        /**
         * Returns the parent class loader.
         * <p>
         * The parent class loader used to check delegation hierarchy.
         *
         * @return the parent classloader
         * @see ClassLoader#getParent()
         */
        ClassLoader getParent();

        /**
         * Finds the class with the specified binary name.
         *
         * @param name the binary name of the class
         * @return the resulting {@link Class} object
         * @throws ClassNotFoundException if class could not be found
         * @see ClassLoader#findClass(String)
         */
        Class<?> findClass(String name) throws ClassNotFoundException;

        /**
         * Returns the loaded class with the given binary name.
         *
         * @param name the binary name of the class
         * @return the {@link Class} object, or {@code null} if the class has not been loaded
         * @see ClassLoader#findLoadedClass(String)
         */
        Class<?> findExistingClass(String name);

        /**
         * Finds the resource with the given name.
         *
         * @param name the resource name
         * @return a URL object for reading the resource, or {@code null} if the resource
         * could not be found
         * @see ClassLoader#findResource(String)
         */
        URL findResource(String name);

        /**
         * Returns an enumeration of URL objects representing all resources with the given name.
         *
         * @param name the resource name
         * @return an enumeration of URL objects for the resources
         * @throws IOException if an I/O error occurs
         * @see ClassLoader#findResources(String)
         */
        Enumeration<URL> findResources(String name) throws IOException;
    }

    private final CopyOnWriteArrayList<ClassFinder> delegates = new CopyOnWriteArrayList<>();

    /**
     * Name of this class loader.
     * <p>
     * Used mostly for reporting purpose.
     * <p>
     * No guarantee about its uniqueness.
     */
    private volatile String name;

    /**
     * Creates new delegating class loader.
     *
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this class loader
     */
    public DelegatingClassLoader(ClassLoader parent, List<ClassFinder> delegates) {
        super(parent);
        for (ClassFinder classFinder : delegates) {
            checkDelegate(classFinder);
        }
        this.delegates.addAll(delegates);
    }

    public DelegatingClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Adds a class finder to list of delegates.
     * <p>
     * To have a consistent class space (by consistent class space, we mean a class space
     * where there does not exist two class with same name), this method does not allow
     * a delegate to be added that has a different parent.
     *
     * @param classFinder class finder to add to the list of delegates
     * @return {@code true} if the delegate is added, {@code false} otherwise
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this class loader
     */
    public boolean addDelegate(ClassFinder classFinder) {
        checkDelegate(classFinder);
        return delegates.addIfAbsent(classFinder);
    }

    /**
     * Checks delegation hierarchy.
     *
     * @param classFinder class finder to check
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this class loader
     */
    private void checkDelegate(ClassFinder classFinder) {
        final ClassLoader delegateParent = classFinder.getParent();
        final ClassLoader parent = getParent();
        if (!Objects.equals(delegateParent, parent)) {
            throw new IllegalArgumentException("Delegation hierarchy mismatch");
        }
    }

    /**
     * Removes a class finder from list of delegates.
     *
     * @param classFinder class finder to remove from the list of delegates
     * @return {@code true} if the delegate was removed, {@code false} otherwise
     */
    public boolean removeDelegate(ClassFinder classFinder) {
        return delegates.remove(classFinder);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassFinder classFinder : delegates) {
            try {
                Class<?> c;
                synchronized (classFinder) {
                    c = classFinder.findExistingClass(name);
                    if (c == null) {
                        c = classFinder.findClass(name);
                    }
                }
                return c;
            } catch (ClassNotFoundException e) {
                // Ignore, as we search next in list
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    protected URL findResource(String name) {
        for (ClassFinder classFinder : delegates) {
            URL resourceURL = classFinder.findResource(name);
            if (resourceURL != null) {
                return resourceURL;
            }
        }
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        List<Enumeration<URL>> enumerators = new ArrayList<>();
        for (ClassFinder classFinder : delegates) {
            Enumeration<URL> enumerator = classFinder.findResources(name);
            enumerators.add(enumerator);
        }
        return new CompositeEnumeration(enumerators);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClassFinder> getDelegates() {
        return unmodifiableList(delegates);
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return super.toString();
        }
    }
}
