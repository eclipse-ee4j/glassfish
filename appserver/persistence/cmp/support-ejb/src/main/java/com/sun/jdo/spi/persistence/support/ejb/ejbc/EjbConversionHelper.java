/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import com.sun.jdo.api.persistence.mapping.ejb.AbstractNameMapper;
import com.sun.jdo.api.persistence.mapping.ejb.ConversionHelper;
import com.sun.jdo.spi.persistence.support.ejb.model.util.NameMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistenceDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistentFieldInfo;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;

/*
 * This class implements ConversionHelper interface by using data from
 * IASEjbBundleDescriptor.
 *
 * @author Shing Wai Chan 2002
 */
public class EjbConversionHelper implements ConversionHelper {

    private NameMapper nameMapper = null;
    private EjbBundleDescriptorImpl bundle = null;
    private HashMap<String, IASEjbCMPEntityDescriptor> ejbDescMap = new HashMap<>();
    private HashMap<String, HashMap<String, String>> ejbFieldMap = new HashMap<>();
    private HashMap<String, HashMap<String, String>> ejbKeyMap = new HashMap<>();
    private HashMap<String, PersistenceDescriptor> ejbPerDescMap = new HashMap<>();
    private HashMap<String, ArrayList<RelationshipDescriptor>> ejbRelMap = new HashMap<>();
    boolean generateFields = true;
    boolean ensureValidation = true;

    public EjbConversionHelper(NameMapper nameMapper) {
        this.nameMapper = nameMapper;
        this.bundle = nameMapper.getBundleDescriptor();

        Iterator<EjbDescriptor> iter = bundle.getEjbs().iterator();
        while (iter.hasNext()) {
            Object desc = iter.next();
            if (desc instanceof IASEjbCMPEntityDescriptor) {
                IASEjbCMPEntityDescriptor ejbDesc =
                        (IASEjbCMPEntityDescriptor)desc;

                String ejbName = ejbDesc.getName();
                //collect all ejbdesc
                ejbDescMap.put(ejbName, ejbDesc);

                //collect PersistenceDescriptor
                PersistenceDescriptor pers = ejbDesc.getPersistenceDescriptor();
                ejbPerDescMap.put(ejbName, pers);

                //collect pers fields
                Collection<PersistentFieldInfo> pFields = ejbDesc.getPersistentFields();
                HashMap<String, String> fieldMap = new HashMap<>();
                Iterator<PersistentFieldInfo> fIter = pFields.iterator();
                while (fIter.hasNext()) {
                    String fieldName = fIter.next().name;
                    fieldMap.put(fieldName, fieldName);
                }
                ejbFieldMap.put(ejbName, fieldMap);

                //collect pseudo cmr fields
                List<String> pseudoFields = nameMapper.getGeneratedRelationshipsForEjbName(ejbName);
                Iterator<String> pIter = pseudoFields.iterator();
                while (pIter.hasNext()) {
                    addField(ejbName, pIter.next());
                }

                //collect all keys
                Collection<PersistentFieldInfo> pKeys = ejbDesc.getPrimaryKeyFields();
                HashMap<String, String> pKeyMap = new HashMap<>();
                Iterator<PersistentFieldInfo> kIter = pKeys.iterator();
                while (kIter.hasNext()) {
                    String fieldName = kIter.next().name;
                    pKeyMap.put(fieldName, fieldName);
                }
                ejbKeyMap.put(ejbName, pKeyMap);
            }
        }

        //collect relationship
        Set<RelationshipDescriptor> rels = bundle.getRelationships();
        Iterator<RelationshipDescriptor> relIter = rels.iterator();
        while (relIter.hasNext()) {
            RelationshipDescriptor rel = relIter.next();
            RelationRoleDescriptor source = rel.getSource();
            RelationRoleDescriptor sink = rel.getSink();

            //collect source RelationshipDescriptor
            String sourceEjbName = source.getOwner().getName();
            ArrayList<RelationshipDescriptor> sourceRels = ejbRelMap.get(sourceEjbName);
            if (sourceRels == null) {
                sourceRels = new ArrayList<>();
                ejbRelMap.put(sourceEjbName, sourceRels);
            }
            sourceRels.add(rel);

            //collect source cmr field
            String sourceCMRField = source.getCMRField();
            if (sourceCMRField != null) {
                addField(sourceEjbName, sourceCMRField);
            }

            //collect sink RelationshipDescriptor
            String sinkEjbName = sink.getOwner().getName();
            ArrayList<RelationshipDescriptor> sinkRels = ejbRelMap.get(sinkEjbName);
            if (sinkRels == null) {
                sinkRels = new ArrayList<>();
                ejbRelMap.put(sinkEjbName, sinkRels);
            }
            sinkRels.add(rel);

            //collect sink cmr field
            String sinkCMRField = sink.getCMRField();
            if (sinkCMRField != null) {
                addField(sinkEjbName, sinkCMRField);
            }
        }
    }

    //---- implements interface ConversionHelper ----

    @Override
    public String getMappedClassName(String ejbName) {
        return nameMapper.getPersistenceClassForEjbName(ejbName);
    }

    /**
     * If {@link #generateFields} is <code>true</code>, then this method will
     * check if the field is one of the cmp + cmr + pseudo cmr fields, otherwise
     * the method will check if the field is one of the cmp + cmr fields.
     * @param ejbName The ejb-name element for the bean
     * @param fieldName The name of a container managed field in the named bean
     * @return <code>true</code> if the bean contains the field, otherwise
     * return <code>false</code>
     */
    @Override
    public boolean hasField(String ejbName, String fieldName) {
        if (generateFields || !isGeneratedRelationship(ejbName, fieldName)) {
            HashMap<String, String> fieldMap = ejbFieldMap.get(ejbName);
            return fieldMap == null ? false : fieldMap.get(fieldName) != null;
        }
        return false;
    }

    /**
     * If {@link #generateFields} is <code>true</code>, then this method will
     * return an array of cmp + cmr + pseudo cmr fields, otherwise
     * the method will return an array of cmp + cmr fields.
     * @param ejbName The ejb-name element for the bean
     * @return an array of fields in the ejb bean
     */
    @Override
    public Object[] getFields(String ejbName) {
        HashMap<String, String> fieldMap = ejbFieldMap.get(ejbName);
        if (fieldMap != null) {
            ArrayList<String> fields = new ArrayList<>(fieldMap.keySet());
            if (!generateFields) {
                fields.removeAll(getGeneratedRelationships(ejbName));
            }
            return fields.toArray();
        }
        return null;
    }

    /**
     * The boolean argument candidate is ignored in this case.
     */
    @Override
    public boolean isKey(String ejbName, String fieldName, boolean candidate) {
        HashMap<String, String> keyMap = ejbKeyMap.get(ejbName);
        return (keyMap != null) ? (keyMap.get(fieldName) != null) : false;
    }

    /**
     * This API will only be called from MappingFile when multiplicity is Many
     * on the other role.
     */
    @Override
    public String getRelationshipFieldType(String ejbName, String fieldName) {
        if (isGeneratedRelationship(ejbName, fieldName)) {
            return java.util.Collection.class.getName();
        } else {
            PersistenceDescriptor pers =
                ejbPerDescMap.get(ejbName);
            return pers.getCMRFieldReturnType(fieldName);
        }
    }

    /**
     * getMultiplicity of the other role on the relationship
     * Please note that multiplicity is JDO style
     */
    @Override
    public String getMultiplicity(String ejbName, String fieldName) {
        RelationRoleDescriptor oppRole = getRelationRoleDescriptor(ejbName,
                fieldName, false);
        return (oppRole.getIsMany()) ? MANY : ONE;
    }

    @Override
    public String getRelationshipFieldContent(String ejbName, String fieldName) {
        RelationRoleDescriptor oppRole = getRelationRoleDescriptor(ejbName,
                fieldName, false);
        return oppRole.getOwner().getName();
    }

    /**
     * This method return the fieldName of relation role on the other end.
     */
    @Override
    public String getInverseFieldName(String ejbName, String fieldName) {
        RelationRoleDescriptor oppRole = getRelationRoleDescriptor(ejbName,
                fieldName, false);
        String inverseName = oppRole.getCMRField();

        // if we are generating relationships, check for a generated inverse
        if ((generateFields) && (inverseName == null)) {
            inverseName = nameMapper.getGeneratedFieldForEjbField(
                ejbName, fieldName)[1];
        }

        return inverseName;
    }

    /**
     * Returns flag whether the mapping conversion should apply the default
     * strategy for dealing with unknown primary key classes. This method will
     * only be called when {@link #generateFields} returns <code>true</code>.
     * @param ejbName The value of the ejb-name element for a bean.
     * @return <code>true</code> to apply the default unknown PK Class Strategy,
     * <code>false</code> otherwise
     */
    @Override
    public boolean applyDefaultUnknownPKClassStrategy(String ejbName) {
        IASEjbCMPEntityDescriptor ejbDesc =
                ejbDescMap.get(ejbName);
        String keyClassName = ejbDesc.getPrimaryKeyClassName();
        return keyClassName != null &&
                keyClassName.equals(Object.class.getName());
    }

    /**
     * Returns the name used for generated primary key fields.
     * @return a string for key field name
     */
    @Override
    public String getGeneratedPKFieldName() {
        return AbstractNameMapper.GENERATED_KEY_FIELD_NAME;
    }

    /**
     * Returns the prefix used for generated version fields.
     * @return a string for version field name prefix
     */
    @Override
    public String getGeneratedVersionFieldNamePrefix() {
        return AbstractNameMapper.GENERATED_VERSION_FIELD_PREFIX;
    }

    @Override
    public boolean relatedObjectsAreDeleted(String beanName, String fieldName) {
        RelationRoleDescriptor oppRole = getRelationRoleDescriptor(beanName, fieldName, false);
        return oppRole.getCascadeDelete();
    }

    /**
     * Returns the flag whether the mapping conversion should generate
     * relationship fields and primary key fields to support run-time.
     * The version field is always created even {@link #generateFields} is
     * <code>false</code> because it holds version column information.
     * @return <code>true</code> to generate fields in the dot-mapping file
     * (if they are not present).
     */
    @Override
    public boolean generateFields() {
        return generateFields;
    }

    /**
     * Sets the flag whether the mapping conversion should generate relationship
     * fields, primary key fields, and version fields to support run-time.
     * @param generateFields a flag which indicates whether fields should be
     * generated
     */
    @Override
    public void setGenerateFields(boolean generateFields) {
        this.generateFields = generateFields;
    }

    /** Returns the flag whether the mapping conversion should validate
     * all fields against schema columns.
     * @return <code>true</code> to validate all the fields in the dot-mapping
     * file.
     */
    @Override
    public boolean ensureValidation() {
        return ensureValidation;
    }

    /**
     * Sets the flag whether the mapping conversion should validate all fields
     * against schema columns.
     * @param isValidating a boolean of indicating validating fields or not
     */
    @Override
    public void setEnsureValidation(boolean isValidating) {
        ensureValidation = isValidating;
    }

    /**
     * Returns <code>true</code> if the field is generated. There are three
     * types of generated fields: generated relationships, unknown primary key
     * fields, and version consistency fields.
     * @param ejbName The ejb-name element for the bean
     * @param fieldName The name of a container managed field in the named bean
     * @return <code>true</code> if the field is generated; <code>false</code>
     * otherwise.
     */

    @Override
    public boolean isGeneratedField(String ejbName, String fieldName) {
        return nameMapper.isGeneratedField(ejbName, fieldName);
    }

    @Override
    public boolean isGeneratedRelationship(String ejbName, String fieldName) {
        return nameMapper.isGeneratedEjbRelationship(ejbName, fieldName);
    }

    /**
     * Returns a list of generated relationship field names.
     * @param ejbName The ejb-name element for the bean
     * @return a list of generated relationship field names
     */
    @Override
    public List<String> getGeneratedRelationships(String ejbName) {
        return nameMapper.getGeneratedRelationshipsForEjbName(ejbName);

    }

    //-------------------------------------
    private RelationRoleDescriptor getRelationRoleDescriptor(String ejbName,
            String cmrFieldName, boolean self) {
        String myEjbName = ejbName;
        String myCMRFieldName = cmrFieldName;
        boolean myself = self;
        if (isGeneratedRelationship(ejbName, cmrFieldName)) {
            String[] nfPair = nameMapper.getEjbFieldForGeneratedField(
                    ejbName, cmrFieldName);
            myEjbName = nfPair[0];
            myCMRFieldName = nfPair[1];
            myself = !self;
        }
        return getRealRelationRoleDescriptor(myEjbName, myCMRFieldName, myself);
    }

    private RelationRoleDescriptor getRealRelationRoleDescriptor(
            String ejbName, String cmrFieldName, boolean self) {
        ArrayList<RelationshipDescriptor> rels = ejbRelMap.get(ejbName);
        for (int i = 0; i < rels.size(); i++) {
            RelationshipDescriptor rel = rels.get(i);
            RelationRoleDescriptor source = rel.getSource();
            RelationRoleDescriptor sink = rel.getSink();
            if (ejbName.equals(source.getOwner().getName()) &&
                    cmrFieldName.equals(source.getCMRField())) {
                return (self) ? source : sink;
            } else if (ejbName.equals(sink.getOwner().getName()) &&
                    cmrFieldName.equals(sink.getCMRField())) {
                return (self) ? sink : source;
            }
        }
        throw new IllegalArgumentException();
    }

    private void addField(String ejbName, String fieldName) {
        HashMap<String, String> fieldMap = ejbFieldMap.get(ejbName);
        if (fieldMap == null) {
            fieldMap = new HashMap<>();
            ejbFieldMap.put(ejbName, fieldMap);
        }
        fieldMap.put(fieldName, fieldName);
    }
}
