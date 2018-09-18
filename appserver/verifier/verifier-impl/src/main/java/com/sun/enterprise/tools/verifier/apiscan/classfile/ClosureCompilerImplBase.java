/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the base class for classes that actually implement ClosureCompiler
 * interface, i.e. {@link BCELClosureCompilerImpl} and
 * {@link ASMClosureCompilerImpl}.
 * It contains common implementation for above classes.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class ClosureCompilerImplBase implements ClosureCompiler {

    protected ClassFileLoader loader;

    protected HashSet<String> excludedClasses = new HashSet<String>();

    protected HashSet<String> excludedPackages = new HashSet<String>();

    protected HashSet<String> excludedPatterns = new HashSet<String>();

    protected HashSet<String> visitedClasses = new HashSet<String>();

    private static String resourceBundleName = "com.sun.enterprise.tools.verifier.apiscan.LocalStrings";
    protected static final Logger logger = Logger.getLogger("apiscan.classfile", resourceBundleName); // NOI18N

    // used for logging
    private static final String myClassName = "ClosureCompilerImplBase"; // NOI18N

    /**
     * @param loader the ClassFileLoader that is used to load the referenced
     *               classes.
     */
    protected ClosureCompilerImplBase(ClassFileLoader loader) {
        this.loader = loader;
    }

    /**
     * @param className the class name to be excluded from closure
     *                     computation. It is in the external class name format
     *                     (i.e. java.util.Map$Entry instead of java.util.Map.Entry).
     *                     When the closure compiler sees a class matches this
     *                     name, it does not try to compute its closure any
     *                     more. It merely adds this name to the closure. So the
     *                     final closure will contain this class name, but not
     *                     its dependencies.
     */
    public void addExcludedClass(String className) {
        excludedClasses.add(className);
    }

    /**
     * @param pkgName the package name of classes to be excluded from
     *                     closure computation. It is in the external format
     *                     (i.e. java.lang (See no trailing '.'). When the
     *                     closure compiler sees a class whose package name
     *                     matches this name, it does not try to compute the
     *                     closure of that class any more. It merely adds that
     *                     class name to the closure. So the final closure will
     *                     contain that class name, but not its dependencies.
     */
    public void addExcludedPackage(String pkgName) {
        excludedPackages.add(pkgName);
    }

    /**
     * @param pattern the pattern for the names of classes to be excluded from
     *                closure computation. It is in the external format (i.e.
     *                org.apache.). When the closure compiler sees a class whose
     *                name begins with this pattern, it does not try to compute
     *                the closure of that class any more. It merely adds that
     *                class name to the closure. So the final closure will
     *                contain that class name, but not its dependencies. Among
     *                all the excluded list, it is given the lowest priority in
     *                search order.
     */
    public void addExcludedPattern(String pattern) {
        excludedPatterns.add(pattern);
    }

    /**
     * @param jar whose classes it will try to build closure of. This is a
     *            convenience method which iterates over all the entries in a
     *            jar file and computes their closure.
     */
    public boolean buildClosure(java.util.jar.JarFile jar) throws IOException {
        boolean result = true;
        for (java.util.Enumeration entries = jar.entries();
             entries.hasMoreElements();) {
            String clsName = ((java.util.jar.JarEntry) entries.nextElement()).getName();
            if (clsName.endsWith(".class")) { // NOI18N
                String externalClsName = clsName.substring(0,
                        clsName.lastIndexOf(".class")) // NOI18N
                        .replace('/', '.');
                boolean newresult = this.buildClosure(externalClsName);
                result = newresult && result;
            }
        }//for all jar entries
        return result;
    }

    public Collection<String> getNativeMethods() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param className name of class in external format.
     * @return
     */
    protected boolean needToBuildClosure(String className) {
        boolean result = true;
        if (visitedClasses.contains(className))
            result = false;
        else if (excludedClasses.contains(className)) {
            result = false;
        } else if (excludedPackages.contains(getPackageName(className))) {
            result = false;
        } else {
            for (Iterator i = excludedPatterns.iterator(); i.hasNext();) {
                String pattern = (String) i.next();
                if (className.startsWith(pattern)) {
                    result = false;
                    break;
                }
            }
        }
        logger.logp(Level.FINEST, myClassName, "needToBuildClosure", // NOI18N
                className + " " + result); // NOI18N
        return result;
    }

    /**
     * @param className name of class in external format.
     * @return package name in dotted format, e.g. java.lang for java.lang.void
     */
    protected static String getPackageName(String className) {
        int idx = className.lastIndexOf('.');
        if (idx != -1) {
            return className.substring(0, idx);
        } else
            return "";
    }

}
