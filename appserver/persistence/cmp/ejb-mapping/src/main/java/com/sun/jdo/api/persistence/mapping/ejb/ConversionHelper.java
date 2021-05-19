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
 * ConversionHelper.java
 *
 * Created on February 3, 2002, 12:34 PM
 */

package com.sun.jdo.api.persistence.mapping.ejb;

import java.util.List;

/** Helper interface for the {@link MappingFile}.
 * The mapping file classes use an object that
 * implements this interface to assist in the conversion
 * from the sun-cmp-mapping file, into a TP dot-mapping file.
 * The APIs to the deployment descriptors differ at deployment time
 * and at development time.  This interface provides a level of abstraction
 * for the needed information.
 *
 * @author vkraemer
 * @version 1.0
 */
public interface ConversionHelper {

    public static final String ONE = "One"; // NOI18N
    public static final String MANY = "Many"; // NOI18N

    /** Computes the name of the TP implementation class for a bean.
    * @param beanName The value of the ejb-name element for a bean.
    * @return The full name of the TP class that implements
    * the fields and relationships of an EJB.
    */
    public String getMappedClassName(String beanName);

    /**
    * @param beanName Name of bean to investigate for field.
    * @param fieldName Name of field sought in named bean.
    * @return True if the named bean has the named field
    */
    public boolean hasField(String beanName, String fieldName);

    /**
    */
    public Object[] getFields(String beanName);

    /** Compute the keyness of a field.
    * The value returned is the keyness of the field, if it is
    * computable. If it is not, the candidate value is returned.
    * @param beanName The value of the ejb-name element for a bean.
    * @param fieldName The name of a container managed field in the named bean.
    * @param candidate The value "proposed" by the content of the sun-cmp-mapping file.
    * @return The real value of the keyness of a field.
    * This may be different than the candidate value,
    * if the correct values of a fields keyness can
    * be computed from available data.
    */
    public boolean isKey(String beanName, String fieldName, boolean candidate);

    /** Return the name of the opposite roles ejb-name
    * @param ejbName The value of the ejb-name element for a bean.
    * @param fieldName The name of a container managed field in the named bean.
    * @return The ejb-name of the bean that is referenced by a
    * relationship field.  This is the ejb-name of the
    * "other" roles relationship-role-source.
    */
    public String getRelationshipFieldContent(String ejbName, String fieldName);

    /**
    * @param ejbName The ejb-name element for the bean
    * @param fieldName The name of a container managed field in the named bean.
    * @return The String values "One" or "Many".
    */
    public String getMultiplicity(String ejbName, String fieldName);

    /**
    * @param ejbName The value of the ejb-name element for a bean.
    * @param fieldName The name of a container managed field in the named bean.
    * @return The String values "One" or "Many".
    */
    public String getRelationshipFieldType(String ejbName, String fieldName);

    /**
    * @param ejbName The value of the ejb-name element for a bean.
    * @param fieldName The name of a container managed field in the named bean.
    * @return The String values "One" or "Many".
    */
    public String getInverseFieldName(String ejbName, String fieldName);

    /**
     * Returns flag whether the mapping conversion should apply the default
     * strategy for dealing with unknown primary key classes. This method will
     * only be called when {@link #generateFields} returns <code>true</code>.
     * @param ejbName The value of the ejb-name element for a bean.
     * @return <code>true</code> to apply the default unknown PK Class Strategy,
     * <code>false</code> otherwise
     */
    public boolean applyDefaultUnknownPKClassStrategy(String ejbName);

    /**
     * Returns the name used for generated primary key fields.
     * @return a string for key field name
     */
    public String getGeneratedPKFieldName();

    /**
     * Returns the prefix used for generated version fields.
     * @return a string for version field name prefix
     */
    public String getGeneratedVersionFieldNamePrefix();

    /**
    * @param ejbName The ejb-name element for the bean
    * @param fieldName The name of a container managed field in the named bean.
    * @return boolean flag indicating whether the objects in this collection field are to
    *   be deleted when this field' owning object is deleted.
    */
    public boolean relatedObjectsAreDeleted(String ejbName, String fieldName);

    /**
     * Returns the flag whether the mapping conversion should generate
     * relationship fields and primary key fields to support run-time.
     * The version field is always created even {@link #generateFields} is
     * <code>false</code> because it holds version column information.
     * @return <code>true</code> to generate fields in the dot-mapping file
     * (if they are not present).
     */
    public boolean generateFields();

    /**
     * Sets the flag whether the mapping conversion should generate relationship
     * fields, primary key fields, and version fields to support run-time.
     * @param generateFields a flag which indicates whether fields should be
     * generated
     */
    public void setGenerateFields(boolean generateFields);

    /** Returns the flag whether the mapping conversion should validate
     * all fields against schema columns.
     * @return <code>true</code> to validate all the fields in the dot-mapping
     * file.
     */
    public boolean ensureValidation();

    /**
     * Sets the flag whether the mapping conversion should validate all fields
     * against schema columns.
     * @param isValidating a boolean of indicating validating fields or not
     */
    public void setEnsureValidation(boolean isValidating);

    /**
     * Returns <code>true</code> if the field is generated. There are three
     * types of generated fields: generated relationships, unknown primary key
     * fields, and version consistency fields.
     * @param ejbName The ejb-name element for the bean
     * @param fieldName The name of a container managed field in the named bean
     * @return <code>true</code> if the field is generated; <code>false</code>
     * otherwise.
     */
    public boolean isGeneratedField(String ejbName, String fieldName);

    /** Flag whether the conversion helper generated the relationship field
    * @param ejbName The ejb-name element for the bean
    * @param fieldName The name of a container managed field in the named bean.
    * @return <code>true</code> if the field was created by the conversion
    * helper.
    */
    public boolean isGeneratedRelationship(String ejbName, String fieldName);

    /**
     * Returns a list of generated relationship field names.
     * @param ejbName The ejb-name element for the bean
     * @return a list of generated relationship field names
     */
    public List getGeneratedRelationships(String ejbName);
}

