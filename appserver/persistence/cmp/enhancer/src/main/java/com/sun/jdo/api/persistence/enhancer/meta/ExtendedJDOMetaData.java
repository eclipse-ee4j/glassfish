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

//ExtendedJDOMetaData - Java Source


//***************** package ***********************************************

package com.sun.jdo.api.persistence.enhancer.meta;


//***************** import ************************************************


//#########################################################################
/**
 * Provides extended JDO meta information for byte-code enhancement.
 */
//#########################################################################

public interface ExtendedJDOMetaData
       extends   JDOMetaData
{
    /**
     * The JDO field flags.
     */
    int CHECK_READ    = 0x01;
    int MEDIATE_READ  = 0x02;
    int CHECK_WRITE   = 0x04;
    int MEDIATE_WRITE = 0x08;
    int SERIALIZABLE  = 0x10;

    /**********************************************************************
     *  Gets all known classnames.
     *
     *  @return  All known classnames.
     *********************************************************************/

    String[] getKnownClasses() throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     *  Gets all known fieldnames of a class.
     *
     *  @param  classname  The classname.
     *
     *  @return  All known fieldnames.
     *********************************************************************/

    String[] getKnownFields(String classname) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     *  Gets the type of a field.
     *
     *  @param  classname  The classname.
     *  @param  fieldname  The fieldname.
     *
     *  @return  The type of the field.
     *********************************************************************/

    String getFieldType(String classname, String fieldname) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     *  Gets the modifiers of a class. The return value is a constant of the
     *  <code>java.lang.reflect.Modifier</code> class.
     *
     *  @param  classname  The classname.
     *
     *  @return  The modifiers.
     *
     *  @see  java.lang.reflect.Modifier
     *********************************************************************/

    int getClassModifiers(String classname) throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**********************************************************************
     *  Gets the modifiers of a field. The return value is a constant of the
     *  <code>java.lang.reflect.Modifier</code> class.
     *
     *  @param  classname  The classname.
     *  @param  fieldname  The fieldname.
     *
     *  @return  The modifiers.
     *
     *  @see  java.lang.reflect.Modifier
     *********************************************************************/

    int getFieldModifiers(String classname, String fieldname) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns the name of the key class of a class.
     * <P>
     * The following holds:
     *   (String s = getKeyClass(classPath)) != null
     *       ==> !isPersistenceCapableClass(s)
     *           && isPersistenceCapableClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return the name of the key class or null if there is none
     * @see #isPersistenceCapableClass(String)
     *********************************************************************/

    String getKeyClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns whether a field of a class is known to be non-managed.
     * <P>
     * This method differs from isManagedField() in that a field may or
     * may not be managed if its not known as non-managed.
     * The following holds (not vice versa!):
     *   isKnownNonManagedField(classPath, fieldName)
     *       ==> !isManagedField(classPath, fieldName)
     * <P>
     * This method doesn't require the field having been declared by
     * declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @param fieldSig the non-null type signature of the field
     * @return true if this field is known to be non-managed; otherwise false
     * @see #isManagedField(String, String)
     *
     *********************************************************************/

    boolean isKnownNonManagedField(String classPath, String fieldName, String fieldSig)
        throws JDOMetaDataUserException, JDOMetaDataFatalError;


   /**********************************************************************
     * Returns whether a field of a class is transient transactional
     * or persistent.
     * <P>
     * A managed field must not be known as non-managed and must be either
     * transient transactional or persistent.  The following holds:
     *   isManagedField(classPath, fieldName)
     *       ==> !isKnownNonManagedField(classPath, fieldName)
     *           && (isPersistentField(classPath, fieldName)
     *               ^ isTransactionalField(classPath, fieldName))
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return true if this field is managed; otherwise false
     * @see #isKnownNonManagedField(String, String, String)
     * @see #isPersistentField(String, String)
     * @see #isPersistenceCapableClass(String)
     *********************************************************************/

   boolean isManagedField(String classPath, String fieldName) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns whether a field of a class is key.
     * <P>
     * A key field must be persistent.
     * The following holds:
     *   isKeyField(classPath, fieldName)
     *       ==> isPersistentField(classPath, fieldName)
     *           && !isDefaultFetchGroupField(classPath, fieldName)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return true if this field is key; otherwise false
     * @see #isPersistentField(String, String)
     *
     *********************************************************************/

    boolean isKeyField(String classPath, String fieldName) throws JDOMetaDataUserException, JDOMetaDataFatalError;

    /**********************************************************************
     * Returns the field flags for a declared field of a class.
     * <P>
     * The following holds for the field flags:
     *   int f = getFieldFlags(classPath, fieldName);
     *
     *   !isManagedField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE == 0) && (f & MEDIATE_WRITE == 0)
     *
     *   isTransientField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE != 0) && (f & MEDIATE_WRITE == 0)
     *
     *   isKeyField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE == 0) && (f & MEDIATE_WRITE != 0)
     *
     *   isDefaultFetchGroupField(classPath, fieldName)
     *       ==> (f & CHECK_READ != 0) && (f & MEDIATE_READ != 0) &&
     *           (f & CHECK_WRITE == 0) && (f & MEDIATE_WRITE == 0)
     *
     *   isPersistentField(classPath, fieldName)
     *   && isKeyField(classPath, fieldName)
     *   && isDefaultFetchGroupField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE != 0) && (f & MEDIATE_WRITE != 0)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return the field flags for this field
     *
     *********************************************************************/

    int getFieldFlags(String classPath, String fieldName) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns the field flags for some declared, managed fields of a class.
     * <P>
     * This method requires all fields having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldNames the non-null array of names of the declared fields
     * @return the field flags for the fields
     *
     *********************************************************************/

    int[] getFieldFlags(String classPath, String[] fieldNames) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     *  Gets the type of some fields.
     *
     *  @param  classname  The classname.
     *  @param  fieldnames  The fieldnames.
     *  @return  The type of the fields.
     *********************************************************************/

    String[] getFieldType(String classname, String[] fieldnames) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns the unique field index of some declared, managed fields of a
     * class.
     * <P>
     * This method requires all fields having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldNames the non-null array of names of the declared fields
     * @return the non-negative, unique field indices
     *
     *********************************************************************/

    int[] getFieldNo(String classPath, String[] fieldNames) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns an array of field names of all key fields of a class.
     * <P>
     * This method requires all fields having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @return an array of all declared key fields of a class
     *
     *********************************************************************/

    String[] getKeyFields(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns the name of the persistence-capable superclass of a class.
     * <P>
     * The following holds:
     *   (String s = getPersistenceCapableSuperClass(classPath)) != null
     *       ==> isPersistenceCapableClass(classPath)
     *           && !isPersistenceCapableRootClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return the name of the PC superclass or null if there is none
     * @see #isPersistenceCapableClass(String)
     * @see #getPersistenceCapableRootClass(String)
     *********************************************************************/

    String getPersistenceCapableSuperClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError;


    /**********************************************************************
     * Returns the name of the key class of the next persistence-capable
     * superclass that defines one.
     * <P>
     * The following holds:
     *   (String s = getSuperKeyClass(classPath)) != null
     *       ==> !isPersistenceCapableClass(s)
     *           && isPersistenceCapableClass(classPath)
     *           && !isPersistenceCapableRootClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return the name of the key class or null if there is none
     * @see #getKeyClass(String)
     * @see #getPersistenceCapableSuperClass(String)
     *********************************************************************/

    String getSuperKeyClass(String classPath) throws JDOMetaDataUserException, JDOMetaDataFatalError;

}  //ExtendedJDOMetaData


//ExtendedJDOMetaData - Java Source End
