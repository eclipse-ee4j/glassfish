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

/*
 * ClassFile.java
 *
 * Created on August 17, 2004, 8:43 AM
 */

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.util.Collection;

/**
 * This represents the information available in a Java .class file. This
 * interface is used by {@link ClosureCompilerImpl} for closure computation. The
 * interface only represents the information as needed by api scanning feature
 * in verifier. I expect it to evolve over time to something very similar to
 * BCEL's ClassFile. Now a note about different ways a Java class can be named
 * and in different places different names are used for the same Java class. 1)
 * In the format "p.a.b". This is what we use when declaring variables etc in a
 * Java code. I do not use this because this is not an unambiguous
 * representation of a Java class. By looking at p.a.b it is not possible to say
 * if b is an inner class in class p.a or b is an outer class in package p.a. 2)
 * In the format "p.outer$inner". It is what is used when we invoke java command
 * or Class.forName(). It is same as what is returned by
 * java.lang.Class.getName() method. It is an unambiguous representation of a
 * class name, because by looking at it, we can tell "inner" is an inner class
 * of a class "outer" which belongs to package p. By default our {@link
 * #getName()} returns in this format. 3) In the format "p/outer$inner" This is
 * the internal name of a class. It is called internal name because in byte code
 * this is what is encoded. It is again an unambiguous representation as this a
 * path expression. It is fairly simple to convert from 2 to 3 by a simple call
 * to String.replace('.','/'). Similarly to convert from 3 to 2, call
 * String.replace('/','.'); Here is a test of what you understood. What does
 * this class name "a$b.c$d.Foo$Bar$Goo" mean? "Goo" is an inner class in a
 * class "Foo$Bar" which is defined in a package "a$b.c$d".
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface ClassFile {
    /**
     * @return names of the classes that are directly referenced by this class.
     *         The returned class names are in external form.
     */
    Collection<String> getAllReferencedClassNames();

    /**
     * @return names of the classes that are directly referenced by this class.
     *         The returned class names are in internal form. This is done with
     *         a purpose as it is easy to look up files with internal class
     *         name.
     */
    Collection getAllReferencedClassNamesInInternalForm();

    /**
     * @return the external name of this Java class. External name is of the
     *         form java.util.Map$Entry. It is what is used when we invoke java
     *         command or Class.forName(). It is same as what is returned by
     *         java.lang.Class.getName() method. Pl note that a Java Class name
     *         and package name can contain $, so when you see a$b, don't assume
     *         it is an inner class.
     * @see #getInternalName()
     */
    String getName();

    /**
     * @return the internal name of the Java class. Internal name is what is
     *         available in file system, e.g. java/util/Map$Entry Pl note that a
     *         Java Class name and package name can contain $, so when you see
     *         a$b, don't assume it is an inner class.
     */
    String getInternalName();

    /**
     * @return internal package name of the Java class. Unlike class name,
     *         package names do not have many forms. They are always specified
     *         using dor notation (i.e. java.lang). See getName() method in
     *         java.lang.Package class. Accordingly we have only one API for
     *         package name. Returns "" for default package.
     */
    String getPackageName();

    /**
     * @return all the methods that are present in this class. This includes
     *         methods that are added by compiler as well, e.g. clinit and init
     *         methods.
     */
    Collection<? extends Method> getMethods();

    /**
     * @param methodRef is the reference of the method that is being looked for
     * @return return the method object that matches the guven criteria. null,
     *         otherwise.
     */
    Method getMethod(MethodRef methodRef);

    /**
     * @return external name of super class
     */
    String getNameOfSuperClass();

    /**
     * @return internal name of super class. Every class other than
     *         java.lang.Object has a super class.
     */
    String getInternalNameOfSuperClass();

    /**
     * @return external names of any interfaces implemented by this class.
     */
    String[] getNamesOfInterfaces();

    /**
     * @return internal names of any interfaces implemented by this class.
     */
    String[] getInternalNamesOfInterfaces();

    /**
     * @return true if this is an interface, else false
     */
    boolean isInterface();

}
