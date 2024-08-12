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

package com.sun.jdo.api.persistence.model;

import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author nico
 * @version %I%
 */
class EnhancerModel extends Model {
    private static final boolean DEBUG = false;

    /**
     * Creates a new <code>EnhancerModel</code>. This constructor should not be
     * called directly; instead, the static instance accessible from the
     * <code>Model</code> class should be used.
     *
     * @see Model#ENHANCER
     */
    EnhancerModel () {
        super();
    }

    /**
     * Determines if the specified className represents an interface type.
     * @param className the fully qualified name of the class to be checked
     * @return <code>true</code> if this class name represents an interface;
     * <code>false</code> otherwise.
     */
    public boolean isInterface (String className) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the input stream with the supplied resource name found with
     * the supplied class name.
     *
     * @param className the fully qualified name of the class which will
     * be used as a base to find the resource
     * @param classLoader the class loader used to find the resource
     * @param resourceName the name of the resource to be found
     *
     * @return the input stream for the specified resource, <code>null</code>
     * if an error occurs or none exists
     */
    protected BufferedInputStream getInputStreamForResource (String className,
        ClassLoader classLoader, String resourceName)
    {
        debug("getInputStreamForResource(" + className +     // NOI18N
            "," + resourceName + ")");                        // NOI18N

        InputStream is = (classLoader != null)
            ? classLoader.getResourceAsStream(resourceName)
            : ClassLoader.getSystemResourceAsStream(resourceName);

        BufferedInputStream rc = null;
        if (is != null && !(is instanceof BufferedInputStream)) {
            rc = new BufferedInputStream(is);
        } else {
            rc = (BufferedInputStream)is;
        }
        return rc;
    }

    /**
     * Returns the name of the second to top (top excluding java.lang.Object)
     * superclass for the given class name.
     * @param className the fully qualified name of the class to be checked
     * @return the top non-Object superclass for className,
     * <code>className</code> if an error occurs or none exists
     */
    protected String findPenultimateSuperclass (String className) {
        debug("findPenultimateSuperclass(" + className + ")");    // NOI18N
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the name of the superclass for the given class name.
     * @param className the fully qualified name of the class to be checked
     * @return the superclass for className, <code>null</code> if an error
     * occurs or none exists
     */
    protected String getSuperclass (String className) {
        debug("getSuperclass (" + className + ")");    // NOI18N
        return null;    // "java.lang.Object";        // NOI18N
    }

    /**
     * Creates a file with the given base file name and extension
     * parallel to the supplied class (if it does not yet exist).
     *
     * @param className the fully qualified name of the class
     * @param baseFileName the name of the base file
     * @param extension the file extension
     *
     * @return the output stream for the specified resource, <code>null</code>
     * if an error occurs or none exists
     *
     * @exception IOException if there is some error creating the file
     */
    protected BufferedOutputStream createFile (String className, String baseFileName,
        String extension) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes the file with the given file name which is parallel
     * to the supplied class.
     * @param className the fully qualified name of the class
     * @param fileName the name of the file
     * @exception IOException if there is some error deleting the file
     */
    protected void deleteFile (String className, String fileName)
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    /** Returns the class element with the specified className.
     * @param className the fully qualified name of the class to be checked
     * @param classLoader the class loader used to check the class
     * @return the class element for the specified className
     */
    public Object getClass (String className, ClassLoader classLoader)
    {
        throw new UnsupportedOperationException();
    }

    /** Determines if the specified class implements the specified interface.
     * Note, class element is a model specific class representation as returned
     * by a getClass call executed on the same model instance.
     * @param classElement the class element to be checked
     * @param interfaceName the fully qualified name of the interface to
     * be checked
     * @return <code>true</code> if the class implements the interface;
     * <code>false</code> otherwise.
     * @see #getClass
     */
    public boolean implementsInterface (Object classElement,
        String interfaceName)
    {
        throw new UnsupportedOperationException();
    }

    /** Determines if the class with the specified name declares a constructor.
     * @param className the name of the class to be checked
     * @return <code>true</code> if the class declares a constructor;
     * <code>false</code> otherwise.
     * @see #getClass
     */
    public boolean hasConstructor (String className)
    {
        throw new UnsupportedOperationException();
    }

    /** Returns the constructor element for the specified argument types
     * in the class with the specified name. Types are specified as type
     * names for primitive type such as int, float or as fully qualified
     * class names.
     * @param className the name of the class which contains the constructor
     * to be checked
     * @param argTypeNames the fully qualified names of the argument types
     * @return the constructor element
     * @see #getClass
     */
    public Object getConstructor (String className, String[] argTypeNames)
    {
        throw new UnsupportedOperationException();
    }

    /** Returns the method element for the specified method name and argument
     * types in the class with the specified name. Types are specified as
     * type names for primitive type such as int, float or as fully qualified
     * class names.
     * @param className the name of the class which contains the method
     * to be checked
     * @param methodName the name of the method to be checked
     * @param argTypeNames the fully qualified names of the argument types
     * @return the method element
     * @see #getClass
     */
    public Object getMethod (String className, String methodName,
       String[] argTypeNames)
    {
        throw new UnsupportedOperationException();
    }

    /** Returns the string representation of type of the specified element.
     * If element denotes a field, it returns the type of the field.
     * If element denotes a method, it returns the return type of the method.
     * Note, element is either a field element as returned by getField, or a
     * method element as returned by getMethod executed on the same model
     * instance.
     * @param element the element to be checked
     * @return the string representation of the type of the element
     * @see #getField
     * @see #getMethod
     */
    public String getType (Object element)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a list of names of all the declared field elements in the
     * class with the specified name.
     * @param className the fully qualified name of the class to be checked
     * @return the names of the field elements for the specified class
     */
    public List getFields (String className) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the field element for the specified fieldName in the class
     * with the specified className.
     * @param className the fully qualified name of the class which contains
     * the field to be checked
     * @param fieldName the name of the field to be checked
     * @return the field element for the specified fieldName
     */
    public Object getField (String className, String fieldName) {
        throw new UnsupportedOperationException();
    }

    /** Determines if the specified field element has a serializable type.
     * A type is serializable if it is a primitive type, a class that implements
     * java.io.Serializable or an interface that inherits from
     * java.io.Serializable.
     * Note, the field element is a model specific field representation as
     * returned by a getField call executed on the same model instance.
     * @param fieldElement the field element to be checked
     * @return <code>true</code> if the field element has a serializable type;
     * <code>false</code> otherwise.
     * @see #getField
     */
    public boolean isSerializable (Object fieldElement)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines if a field with the specified fieldName in the class
     * with the specified className is an array.
     * @param className the fully qualified name of the class which contains
     * the field to be checked
     * @param fieldName the name of the field to be checked
     * @return <code>true</code> if this field name represents a java array
     * field; <code>false</code> otherwise.
     * @see #getFieldType
     */
    public boolean isArray (String className, String fieldName) {
        throw new UnsupportedOperationException();
    }

    /** Returns the string representation of declaring class of
     * the specified member element.  Note, the member element is
     * either a class element as returned by getClass, a field element
     * as returned by getField, a constructor element as returned by
     * getConstructor, or a method element as returned by getMethod
     * executed on the same model instance.
     * @param memberElement the member element to be checked
     * @return the string representation of the declaring class of
     * the specified memberElement
     * @see #getClass
     * @see #getField
     * @see #getConstructor
     * @see #getMethod
     */
    public String getDeclaringClass (Object memberElement)
    {
        throw new UnsupportedOperationException();
    }

    /** Returns the modifier mask for the specified member element.
     * Note, the member element is either a class element as returned by
     * getClass, a field element as returned by getField, a constructor element
     * as returned by getConstructor, or a method element as returned by
     * getMethod executed on the same model instance.
     * @param memberElement the member element to be checked
     * @return the modifier mask for the specified memberElement
     * @see java.lang.reflect.Modifier
     * @see #getClass
     * @see #getField
     * @see #getConstructor
     * @see #getMethod
     */
    public int getModifiers (Object memberElement)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the PersistenceFieldElement with the supplied fieldName found
     * in the supplied className.
     * @param className the fully qualified name of the class which contains
     * the field to be checked
     * @param fieldName the name of the field to be checked
     * @return the PersistenceFieldElement for the specified field,
     * <code>null</code> if an error occurs or none exists
     */
    public PersistenceFieldElement getPersistenceField(String className,
        String fieldName)
    {
        return getPersistenceFieldInternal(className, fieldName);
    }

    /**
     * Determines if the specified className and fieldName pair represent a
     * key field.
     * @param className the fully qualified name of the class which contains
     * the field to be checked
     * @param fieldName the name of the field to be checked
     * @return <code>true</code> if this field name represents a key field;
     * <code>false</code> otherwise.
     */
    public boolean isKey (String className, String fieldName)
    {
        PersistenceFieldElement field =
            getPersistenceField(className, fieldName);

        return field != null ? field.isKey() : false;
    }

    private void debug (Object o) {
        if (DEBUG)
            System.out.println("EnhancerModel::" + o);    // NOI18N
    }
}
