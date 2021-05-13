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

package com.sun.jdo.api.persistence.enhancer.util;

import java.io.File;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/**
 * ClassPath provides class file lookup according to a classpath
 * specification.
 */
public class ClassPath {
    /* The entire class path specification */
    private String theClassPathSpec;

    /* Linked list of class path elements */
    private ClassPathElement theClassPath;

    /**
     * Construct a class path from the input String argument.
     * The path is expected to be in the form appropriate for the current
     * execution environment.
     */
    public ClassPath(String path) {
        theClassPathSpec = path;
        parsePath();
    }

    /**
     * locate a class file given a fully qualified class name
     */
    public ClassFileSource findClass(String className) {
        return findClass(className, theClassPath);
    }

    /**
     * locate a class file given a fully qualified class name
     * starting at the specified class path element
     */
    static ClassFileSource findClass(String className, ClassPathElement path) {
        for (ClassPathElement e = path; e != null; e = e.next()) {
            ClassFileSource source = e.sourceOf(className);
            if (source != null) {
                source.setSourceElement(e);
                return source;
            }
        }

        return null;
    }

    /**
     * Return a file name which might reasonably identify a file containing
     * the specified class.  The name is a "./" relative path.
     */
    public static String fileNameOf(String className, char separator) {
        StringBuffer path = new StringBuffer();
        StringTokenizer parser = new StringTokenizer(className, "./", false);//NOI18N
        for (boolean first = true; parser.hasMoreElements(); first = false) {
            if (!first)
                path.append(separator);
            path.append(parser.nextToken());
        }
        path.append(".class");//NOI18N
        return path.toString();
    }


    /**
     * Return a file name which might reasonably identify a file containing
     * the specified class.  The name is a "./" relative path.
     */
    public static String fileNameOf(String className) {
        return fileNameOf(className, File.separatorChar);
    }


    /**
     * Return a file name which might reasonably identify a file containing
     * the specified class in a zip file.
     */
    public static String zipFileNameOf(String className) {
        return fileNameOf(className, '/');
    }


    /**
     * Return the vm class name which corresponds to the input file name.
     * The file name is expected to be a "./" relative path.
     * Returns null if the file name doesn't end in ".class"
     */
    public static String classNameOf(String fileName) {
        int fnlen = fileName.length();
        if (fnlen > 6 && fileName.regionMatches(true, fnlen - 6, ".class", 0, 6)) {//NOI18N
            /* the file name ends with .class */
            fileName = fileName.substring(0, fileName.length()-6);
            StringBuffer className = new StringBuffer();
            StringTokenizer parser = new StringTokenizer(fileName, "\\/", false);//NOI18N
            for (boolean first = true; parser.hasMoreElements(); first = false) {
                if (!first)
                    className.append('/');
                className.append(parser.nextToken());
            }
            return className.toString();
        }
        return null;
    }

    /**
     * Remove any class path elements which match directory
     */
    public boolean remove(File directory) {
        boolean matched = false;
        ClassPathElement firstElement = theClassPath;
        ClassPathElement prevElement = null;
        for (ClassPathElement cpe = firstElement; cpe != null; cpe = cpe.next()) {
            if (cpe.matches(directory)) {
                matched = true;
                if (prevElement == null)
                    firstElement = cpe.next();
                else
                    prevElement.setNext(cpe.next());
            } else {
                prevElement = cpe;
            }
        }
        theClassPath = firstElement;
        return matched;
    }

    /**
     * Append a directory to the classpath.
     */
    public void append(File directory) {
        append(ClassPathElement.create(directory.getPath()));
    }

    /**
     * Append a class path element to the classpath.
     */
    public void append(ClassPathElement anElement) {
        if (theClassPath == null)
            theClassPath = anElement;
        else
            theClassPath.append(anElement);
    }

    /**
     * Return an enumeration of all of the class files in the specified
     * package in this class path.
     * @param packageName specifies the VM format package name
     *    to which class files must belong.
     * @return an Enumeration of the VM format class names which
     *    can be found.  Note that the Enumeration value is of type String
     *    and duplicate entries may be returned as the result of finding
     *    a class through more than one class path element.  Note also
     *    that the class name returned might not correspond the the
     *    name of the class in the file.
     */
    public Enumeration classesInPackage(String packageName) {
        return new ClassPackageEnumeration(this, packageName);
    }

    /* package local accessors */
    ClassPathElement getPathElements() {
        return theClassPath;
    }

    /* private accessors */

    private void parsePath() {
        StringTokenizer parser =
            new StringTokenizer(theClassPathSpec,
                java.io.File.pathSeparator,
                false /* dont return delimiters */
                );

        ClassPathElement lastElement = null;
        while (parser.hasMoreElements()) {
            ClassPathElement anElement = ClassPathElement.create(parser.nextToken());

            if (lastElement == null)
                theClassPath = anElement;
            else
                lastElement.append(anElement);

            lastElement = anElement;
        }
    }

}

/**
 * An enumeration class which returns the names of the classes which
 * can be found in a class path
 */

class ClassPackageEnumeration implements Enumeration {
    /* The next class path element to look for matches in once
     the current enumeration is complete */
    private ClassPathElement nextClassPathElement;

    /* The package name */
    private String thePackageName;

    /* The enumeration of matching class names in the current class path
     element */
    private Enumeration currentElementEnumeration;

    /**
     * Construct a ClassPackageEnumeration.
     * @param classPath The class path in which to search for classes.
     * @param packageName The VM name of the package in which to search.
     */
    ClassPackageEnumeration(ClassPath classPath, String packageName) {
        nextClassPathElement = classPath.getPathElements();
        thePackageName = packageName;
    }

    public boolean hasMoreElements() {
        while ((currentElementEnumeration == null ||
            !currentElementEnumeration.hasMoreElements()) &&
            nextClassPathElement != null) {
            currentElementEnumeration =
                nextClassPathElement.classesInPackage(thePackageName);
            nextClassPathElement = nextClassPathElement.next();
        }

        return (currentElementEnumeration != null &&
            currentElementEnumeration.hasMoreElements());
    }

    public Object nextElement() {
        if (hasMoreElements())
            return currentElementEnumeration.nextElement();

        throw new NoSuchElementException();
    }
}

