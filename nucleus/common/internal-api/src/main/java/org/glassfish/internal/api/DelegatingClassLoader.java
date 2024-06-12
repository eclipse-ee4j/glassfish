/*
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

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;
import java.net.URL;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This classloader has a list of classloaders called as delegates
 * that it uses to find classes. All those delegates must have the
 * same parent as this classloader in order to have a consistent class space.
 * By consistent class space, I mean a class space where no two loaded class
 * have same name. An inconsistent class space can lead to ClassCastException.
 * This classloader does not define any class, classes are always loaded
 * either by its parent or by one of the delegates.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DelegatingClassLoader extends ClassLoader {
    /*
     * TODO(Sahoo):
     * 1. I18N
     * 2. Move to a more common package, as it has no dependency on kernel.
     */

    /**
     * findClass method of ClassLoader is usually a protected method.
     * Calling loadClass on a ClassLoader is expenssive, as it searches
     * the delegation hierarchy before searching in its private space.
     * Hence we add this interface as an optimization.
     */
    public interface ClassFinder {

        /**
         * @see ClassLoader#getParent()
         */
        ClassLoader getParent();

        /**
         * @see ClassLoader#findClass(String)
         */
        Class<?> findClass(String name) throws ClassNotFoundException;

        /**
         * @see ClassLoader#findLoadedClass(String)
         */
        Class<?> findExistingClass(String name);

        /**
         * @see ClassLoader#findResource(String)
         */
        URL findResource(String name);

        /**
         * @see ClassLoader#findResources(String)
         */
        Enumeration<URL> findResources(String name) throws IOException;
    }

    private final CopyOnWriteArrayList<ClassFinder> delegates = new CopyOnWriteArrayList<>();

    /**
     * Name of this class loader. Used mostly for reporting purpose.
     * No guarantee about its uniqueness.
     */
    private volatile String name;

    /**
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this classloader.
     */
    public DelegatingClassLoader(ClassLoader parent, List<ClassFinder> delegates)
            throws IllegalArgumentException{
        super(parent);
        for (ClassFinder d : delegates) {
            checkDelegate(d);
        }
        this.delegates.addAll(delegates);
    }

    public DelegatingClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Adds a ClassFinder to list of delegates. To have a consistent
     * class space (by consistent class space, I mean a classpace where there
     * does not exist two class with same name), this method does not allow
     * a delegate to be added that has a different parent.
     * @param d ClassFinder to add to the list of delegates
     * @return true if the delegate is added, false otherwise.
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this classloader.
     */
    public boolean addDelegate(ClassFinder d) throws IllegalArgumentException {
        checkDelegate(d);
        return delegates.addIfAbsent(d);
    }

    /**
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this classloader.
     */
    private void checkDelegate(ClassFinder d) throws IllegalArgumentException {
        final ClassLoader dp = d.getParent();
        final ClassLoader p = getParent();
        if (dp != p) { // check for equals
            if ((dp != null && !dp.equals(p)) || !p.equals(dp)) {
                throw new IllegalArgumentException("Delegation hierarchy mismatch");
            }
        }
    }

    /**
     * Removes a ClassFinder from list of delegates.
     *
     * @param d ClassFinder to remove from the list of delegates
     * @return true if the delegate was removed, false otherwise.
     */
    public boolean removeDelegate(ClassFinder d) {
        return delegates.remove(d);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassFinder d : delegates) {
            try {
                Class c = null;
                synchronized(d){
                    c = d.findExistingClass(name);
                    if(c == null){
                        c = d.findClass(name);
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
        for (ClassFinder d : delegates) {
            URL u = d.findResource(name);
            if (u!=null) return u;
        }
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
        for (ClassFinder delegate : delegates) {
            Enumeration<URL> enumerator = delegate.findResources(name);
            enumerators.add(enumerator);
        }
        return new CompositeEnumeration(enumerators);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClassFinder> getDelegates() {
        return Collections.unmodifiableList(delegates);
    }

    @Override
    public String toString() {
        if (name!=null) {
            return name;
        } else {
            return super.toString();
        }
    }
}
