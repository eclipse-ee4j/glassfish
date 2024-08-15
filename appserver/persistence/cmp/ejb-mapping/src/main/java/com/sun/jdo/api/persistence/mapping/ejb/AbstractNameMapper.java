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
 * AbstractNameMapper.java
 *
 * Created on October 28, 2004, 2:51 PM
 */

package com.sun.jdo.api.persistence.mapping.ejb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** This is a class which helps translate between the various names of the
 * CMP (ejb name, abstract schema, abstract bean, concrete bean, local
 * interface, remote interface) and the persistence-capable class name.  It
 * also has methods for translation of field names.  The basic entry point
 * is ejb name or persistence-capable class name.
 *
 * @author Rochelle Raccah
 */
abstract public class AbstractNameMapper {
    public static final int USER_DEFINED_KEY_CLASS = 1;
    public static final int PRIMARY_KEY_FIELD = 2;
    public static final int UNKNOWN_KEY_CLASS = 3;

    /** Defines key field name for unknown primary key */
    public static final String GENERATED_KEY_FIELD_NAME = "generatedPKField"; // NOI18N
    /** Defines version field name prefix for version consistency */
    public static final String GENERATED_VERSION_FIELD_PREFIX =
        "thisVersionFieldWasGeneratedByTheNameMapper"; // NOI18N
    protected static final String GENERATED_CMR_FIELD_PREFIX =
        "thisRelationshipFieldWasGeneratedByTheNameMapper";        // NOI18N

    abstract protected Map getGeneratedFieldsMap();

    abstract protected Map getInverseFieldsMap();

    /** Determines if the specified name represents an ejb.
     * @param name the fully qualified name to be checked
     * @return <code>true</code> if this name represents an ejb;
     * <code>false</code> otherwise.
     */
    abstract public boolean isEjbName(String name);

    /** Gets the name of the abstract bean class which corresponds to the
     * specified ejb name.
     * @param name the name of the ejb
     * @return the name of the abstract bean for the specified ejb
     */
    abstract public String getAbstractBeanClassForEjbName(String name);

    /** Gets the name of the key class which corresponds to the specified
     * ejb name.
     * @param name the name of the ejb
     * @return the name of the key class for the ejb
     */
    abstract public String getKeyClassForEjbName(String name);

    /** Gets the name of the key class which corresponds to the specified
     * persistence-capable key class name.  Returns <code>null</code> if the
     * supplied className is not a persistence-capable key class name.
     * @param className the name of the persistence-capable key class
     * @return the name of the key class for the ejb
     */
    public String getKeyClassForPersistenceKeyClass(String className) {
        String ejbName = getEjbNameForPersistenceKeyClass(className);

        return ((ejbName != null) ? getKeyClassForEjbName(ejbName) : null);
    }

    /** Gets the name of the ejb name which corresponds to the
     * specified persistence-capable key class name.  Returns
     * <code>null</code> if the supplied className is not a
     * persistence-capable key class name.
     * @param className the name of the persistence-capable key class
     * @return the name of the ejb for the specified persistence-capable
     * key class
     */
    public String getEjbNameForPersistenceKeyClass(String className) {
        if (className.toUpperCase().endsWith("OID")) {    // NOI18N
            return getEjbNameForPersistenceClass(
                className.substring(0, className.length() - 4));
        }

        return null;
    }

    /** Get the type of key class of this ejb.
     * @return the key class type, one of {@link #USER_DEFINED_KEY_CLASS},
     * {@link #PRIMARY_KEY_FIELD}, or {@link #UNKNOWN_KEY_CLASS}
     */
    abstract public int getKeyClassTypeForEjbName (String name);

    /** Gets the name of the abstract schema which corresponds to the
     * specified ejb.
     * @param name the name of the ejb
     * @return the name of the abstract schema for the specified ejb
     */
    abstract public String getAbstractSchemaForEjbName(String name);

    /** Gets the name of the ejb name which corresponds to the
     * specified persistence-capable class name.
     * @param className the name of the persistence-capable
     * @return the name of the ejb for the specified persistence-capable
     */
    abstract public String getEjbNameForPersistenceClass(String className);

    /** Gets the name of the persistence-capable class which corresponds to
     * the specified ejb name.
     * @param name the name of the ejb
     * @return the name of the persistence-capable for the specified ejb
     */
    abstract public String getPersistenceClassForEjbName(String name);

    /** Determines if the specified name represents a local interface.
     * @param name the fully qualified name to be checked
     * @return <code>true</code> if this name represents a local interface;
     * <code>false</code> otherwise.
     */
    abstract public boolean isLocalInterface(String name);

    /** Gets the name of the persistence-capable class which corresponds to
     * the specified local interface name.
     * @param className the name of the persistence-capable class which
     * contains fieldName from which to find relationship and therefore the
     * local interface
     * @param fieldName the name of the field in the persistence-capable class
     * @param interfaceName the name of the local interface
     * @return the name of the persistence-capable for the specified
     * local interface which is related to the specified class name, field name
     * pair
     */
    public String getPersistenceClassForLocalInterface(String className,
            String fieldName, String interfaceName) {
        if (isLocalInterface(interfaceName)) {
            String ejbName = getEjbNameForPersistenceClass(className);
            String ejbField =
                getEjbFieldForPersistenceField(className, fieldName);

            return getPersistenceClassForEjbName(
                getEjbNameForLocalInterface(ejbName, ejbField, interfaceName));
        }

        return null;
    }

    /** Gets the name of the ejb which corresponds to the specified
     * local interface name.
     * @param ejbName the name of the ejb which contains fieldName
     * from which to find relationship and therefore the local interface
     * @param fieldName the name of the field in the ejb
     * @param interfaceName the name of the local interface
     * @return the name of the ejb for the specified local interface
     */
    abstract public String getEjbNameForLocalInterface(String ejbName,
        String fieldName, String interfaceName);

    /** Gets the name of the local interface which corresponds to the
     * specified ejb name.
     * @param name the name of the ejb
     * @return the name of the local interface for the specified ejb
     */
    abstract public String getLocalInterfaceForEjbName(String name);

    /** Determines if the specified name represents a remote interface.
     * @param name the fully qualified name to be checked
     * @return <code>true</code> if this name represents a remote interface;
     * <code>false</code> otherwise.
     */
    abstract public boolean isRemoteInterface(String name);

    /** Gets the name of the persistence-capable class which corresponds to
     * the specified remote interface name.
     * @param className the name of the persistence-capable class which
     * contains fieldName from which to find relationship and therefore the
     * remote interface
     * @param fieldName the name of the field in the persistence-capable class
     * @param interfaceName the name of the remote interface
     * @return the name of the persistence-capable for the specified
     * remote interface which is related to the specified class name, field name
     * pair
     */
    public String getPersistenceClassForRemoteInterface(
            String className, String fieldName, String interfaceName) {
        if (isRemoteInterface(interfaceName)) {
            String ejbName = getEjbNameForPersistenceClass(className);
            String ejbField =
                getEjbFieldForPersistenceField(className, fieldName);

            return getPersistenceClassForEjbName(
                getEjbNameForRemoteInterface(ejbName, ejbField, interfaceName));
        }

        return null;
    }

    /** Gets the name of the ejb which corresponds to the specified
     * remote interface name.
     * @param ejbName the name of the ejb which contains fieldName
     * from which to find relationship and therefore the remote interface
     * @param fieldName the name of the field in the ejb
     * @param interfaceName the name of the remote interface
     * @return the name of the ejb for the specified remote interface
     */
    abstract public String getEjbNameForRemoteInterface(String ejbName,
        String fieldName, String interfaceName);

    /** Gets the name of the remote interface which corresponds to the
     * specified ejb name.
     * @param name the name of the ejb
     * @return the name of the remote interface for the specified ejb
     */
    abstract public String getRemoteInterfaceForEjbName(String name);

    /** Gets the name of the field in the ejb which corresponds to the
     * specified persistence-capable class name and field name pair.
     * @param className the name of the persistence-capable
     * @param fieldName the name of the field in the persistence-capable
     * @return the name of the field in the ejb for the specified
     * persistence-capable field
     */
    abstract public String getEjbFieldForPersistenceField(String className,
        String fieldName);

    /** Gets the name of the field in the persistence-capable class which
     * corresponds to the specified ejb name and field name pair.
     * @param name the name of the ejb
     * @param fieldName the name of the field in the ejb
     * @return the name of the field in the persistence-capable for the
     * specified ejb field
     */
    abstract public String getPersistenceFieldForEjbField(String name,
        String fieldName);

    /** Returns <code>true</code> if the field is a generated field.
     * That includes: relationships generated for 2 way managed relationships,
     * key fields generated for use with {@link #UNKNOWN_KEY_CLASS}, or
     * version fields generated to hold a version consistency column.
     * @param name the name of the ejb
     * @param fieldName the name of the field in the ejb
     * @return <code>true</code> if the field is generated;<code>false</code>
     * otherwise
     */
    public boolean isGeneratedField(String name, String fieldName) {
        return isGeneratedEjbRelationship(name, fieldName) ||
            fieldName.equals(GENERATED_KEY_FIELD_NAME) ||
            fieldName.startsWith(GENERATED_VERSION_FIELD_PREFIX);
    }

    /** Returns <code>true</code> if the field in the persistence-capable
     * class which corresponds to the specified ejb name and field name pair
     * is one which was generated automatically for 2 way managed
     * relationships in the case that the ejb specifies one way
     * relationships.
     * @param name the name of the ejb
     * @param fieldName the name of the field in the ejb
     * @return <code>true</code> if the field is generated;<code>false</code>
     * otherwise
     */
    public boolean isGeneratedEjbRelationship(String name, String fieldName) {
        return getGeneratedFieldsMap().keySet().contains(
            Arrays.asList(new String[]{name, fieldName}));
    }

    /** The list contains generated relationship field names.
     * @param name the name of the ejb
     * @return a List of generated relationship names
     */
    public List getGeneratedRelationshipsForEjbName(String name) {
        Map generatedFieldsMap = getGeneratedFieldsMap();
        Iterator iterator = generatedFieldsMap.keySet().iterator();
        List returnList = new ArrayList();

        while (iterator.hasNext()) {
            List nextField = (List)iterator.next();

            if (nextField.get(0).equals(name))
                returnList.add(nextField.get(1));
        }

        return returnList;
    }

    /** Gets the name of the generated field in the ejb which corresponds to
     * the specified ejb name and field name pair.
     * @param name the name of the ejb
     * @param fieldName the name of the field in the ejb
     * @return a String array of the form {<ejb name>, <field name>} which
     * represents the generated field for the ejb field
     */
    public String[] getGeneratedFieldForEjbField(String name,
            String fieldName) {
        List field = (List)getInverseFieldsMap().get(
            Arrays.asList(new String[]{name, fieldName}));

        return ((field != null) ?
            (String[])field.toArray(new String[2]) : null);
    }

    /** Gets the name of the ejb field which corresponds to the specified
     * generated ejb name and field name pair.
     * @param name the name of the ejb
     * @param fieldName the name of the field in the ejb
     * @return a String array of the form {<ejb name>, <field name>} which
     * represents the inverse field for the generated field
     */
    public String[] getEjbFieldForGeneratedField(String name, String fieldName)
    {
        List field = (List)getGeneratedFieldsMap().get(
            Arrays.asList(new String[]{name, fieldName}));

        return ((field != null) ?
            (String[])field.toArray(new String[2]) : null);
    }
}
