/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.TypeUtil;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.Descriptor;

/**
 * This class contains information about the persistent state
 * (abstract persistence schema)
 * for EJB2.0 CMP EntityBeans and Join Objects.
 *
 * @author Sanjeev Krishnan
 */
public final class PersistenceDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(PersistenceDescriptor.class);

    private Set<FieldDescriptor> cmpFields = new HashSet<>();

    // there can be 0 or more pkey fields
    // This set contains FieldDescriptors for fields from bean or pkey class
    private Set<FieldDescriptor> pkeyFields = new HashSet<>();

    // true if primkey-field is set or for container-generated pk field
    private boolean pkeyIsOneField;

    // false for beans with no primkey-field and pk class = Object
    private boolean pkeyFieldSpecified = true;

    private String primaryKeyClassName;
    private boolean pkeyStuffInitialized;

    // true for beans whose primary fields are all primitive fields
    private boolean pkeyFieldsAllPrimitive;


    private EjbCMPEntityDescriptor parentDesc; //the bean whose persistence I describe

    private transient Class<?> persistentClass; // the bean class
    private transient Class<?> stateClass; // the class which holds persistent fields
    private transient Class<?> primaryKeyClass;

    private PersistentFieldInfo[] persFieldInfo;
    private PersistentFieldInfo[] persNoPkeyFieldInfo;
    private PersistentFieldInfo[] pkeyFieldInfo;
    private boolean fieldInfoInitialized;
    private PersistentFieldInfo[] fkeyFields;

    private CMRFieldInfo[] cmrFieldInfo;

    private transient Field[] pkeyClassPkeyFields; // fields in primary key class

    private final Hashtable<MethodDescriptor, QueryDescriptor> queries = new Hashtable<>();

    public PersistenceDescriptor() {
    }

    /**
     * The copy constructor.
     */
    public PersistenceDescriptor(PersistenceDescriptor pers) {
        super(pers);
        this.getCMPFields().addAll(pers.getCMPFields());
        // this.primaryKeyFieldDesc = pers.primaryKeyFieldDesc;
    }


    public String getCMRFieldReturnType(String field) {
        if (field.isBlank()) {
            return Collection.class.getName();
        }
        try {
            Class<?> persClass = getPersistentClass();
            String methodName = "get" + field.substring(0, 1).toUpperCase(Locale.US) + field.substring(1);
            Method method = TypeUtil.getMethod(persClass, persClass.getClassLoader(), methodName, new String[] {});
            return method.getReturnType().getName();
        } catch (Throwable t) {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().log(Level.FINE, t.toString(), t);
            }
            return Collection.class.getName();
        }
    }


    public void setParentDescriptor(EjbCMPEntityDescriptor parentDesc) {
        this.parentDesc = parentDesc;
    }


    public Descriptor getParentDescriptor() {
        return parentDesc;
    }


    public EjbBundleDescriptorImpl getEjbBundleDescriptor() {
        return parentDesc.getEjbBundleDescriptor();
    }

    /**
     * Get all CMR fields of this bean.  All relationships
     * are stored in EjbBundleDescriptor to avoid the complexity of
     * keeping the two sets consistent.  NOTE : To add or remove
     * a relationship use EjbBundleDescriptor.
     */
    private Set<RelationshipDescriptor> getRelationships() {
        Set<RelationshipDescriptor> allRelationships = getEjbBundleDescriptor().getRelationships();
        Set<RelationshipDescriptor> myRelationships  = new HashSet<>();
        for (RelationshipDescriptor next : allRelationships) {
            if( next.hasParticipant(parentDesc) ) {
                myRelationships.add(next);
            }
        }
        return myRelationships;
    }

    /**
     * Return array of CMRFieldInfo objects for all CMR fields.
     */
    public CMRFieldInfo[] getCMRFieldInfo() {
        if (cmrFieldInfo == null) {
            try {
                initCMRFieldStuff();
            } catch (Exception ex) {
                DOLUtils.getDefaultLogger().log(Level.SEVERE,
                    "enterprise.deployment.backend.invalidDescriptorMappingFailure", new Object[] {ex.toString()});
                throw new DeploymentException(ex);
            }
        }
        return cmrFieldInfo;
    }


    /**
     * @return the CMRFieldInfo object for the given CMR field
     */
    public CMRFieldInfo getCMRFieldInfoByName(String fieldName) {
        CMRFieldInfo[] cmrf = this.getCMRFieldInfo();
        for (CMRFieldInfo element : cmrf) {
            if (element.name.equals(fieldName)) {
                return element;
            }
        }
        throw new DeploymentException("CMRFieldInfo not found for field " + fieldName);
    }

    /**
     * Ensures that persistence descriptor will regenerate its
     * derived information after changes have been made to
     * persistent characteristics.
     */
    public void invalidate() {
        cmrFieldInfo = null;
        persFieldInfo = null;
        fieldInfoInitialized = false;
        pkeyStuffInitialized = false;
    }

    private void initCMRFieldStuff() throws Exception {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }

        Set<RelationshipDescriptor> relationships = getRelationships();
        // set to the biggest possible size when all relationships
        // are self-referencing
        CMRFieldInfo[] cmrFieldInfo2 = new CMRFieldInfo[relationships.size() * 2];
        int count = 0;
        for (RelationshipDescriptor rd : relationships) {
            RelationRoleDescriptor source = rd.getSource();
            RelationRoleDescriptor sink = rd.getSink();
            RelationRoleDescriptor[] myroles;
            // if this is a self-referencing relationship, initialize
            // both source and sink cmr fields
            if (source.getPersistenceDescriptor() == sink.getPersistenceDescriptor()) {
                myroles = new RelationRoleDescriptor[2];
                myroles[0] = source;
                myroles[1] = sink;
            } else {
                myroles = new RelationRoleDescriptor[1];
                if (source.getPersistenceDescriptor() == this) {
                    myroles[0] = source;
                } else {
                    myroles[0] = sink;
                }
            }

            // for all relation role elements in myroles, initialize
            // their cmr fields and put them in cmrFieldInfo2
            for (RelationRoleDescriptor myrole : myroles) {
                CMRFieldInfo finfo = new CMRFieldInfo();
                cmrFieldInfo2[count++] = finfo;

                PersistenceDescriptor partnerPers = myrole.getPartner().getPersistenceDescriptor();

                // Check if partner has a local interface
                EjbCMPEntityDescriptor partner = myrole.getPartner().getOwner();
                if (!partner.isLocalInterfacesSupported() && myrole.getCMRField() != null) {
                    throw new RuntimeException("No local interface for target bean of CMR field");
                }

                String type;
                if (myrole.getPartner().getIsMany() == false) {
                    // 1-1 and many-1
                    if (partner.isLocalInterfacesSupported()) {
                        type = partner.getLocalClassName();
                    } else {
                        // a unidirectional relation, partner is
                        // remote-only bean
                        type = partner.getPrimaryKeyClassName();
                    }
                } else {
                    // 1-many and many-many
                    type = myrole.getCMRFieldType();
                    if (type == null) {
                        // A unidirectional relationship from partner
                        // to this obj
                        type = "java.util.Collection";
                    }
                }
                finfo.type = getClass(type);

                finfo.name = myrole.getCMRField();
                if (finfo.name == null) {
                    // A unidirectional relationship from partner to this obj.
                    // We need to maintain a pointer to partner anyway.
                    finfo.name = myrole.composeReverseCmrFieldName();
                }
                finfo.role = myrole;
                myrole.setCMRFieldInfo(finfo);

                if (rd.isOneOne() && fkeyFields != null) {
                    // set foreign key fields corresponding to this CMR field
                    PersistentFieldInfo[] cmrFkeyFields;
                    PersistentFieldInfo[] partnerPkeyFields = partnerPers.getPkeyFieldInfo();
                    cmrFkeyFields = new PersistentFieldInfo[partnerPkeyFields.length];
                    for (int i = 0; i < partnerPkeyFields.length; i++) {
                        String fkeyName = "_" + finfo.name + "_" + partnerPkeyFields[i].name;
                        for (PersistentFieldInfo fkeyField : fkeyFields) {
                            if (fkeyField.name.equals(fkeyName)) {
                                cmrFkeyFields[i] = fkeyField;
                            }
                        }
                    }
                    finfo.fkeyFields = cmrFkeyFields;
                }
            }
        }

        // initialize the cmrFieldInfo array with the actual size
        // and copy the non-null values of cmrFieldInfo2 to it
        cmrFieldInfo = new CMRFieldInfo[count];
        System.arraycopy(cmrFieldInfo2, 0, cmrFieldInfo, 0, count);

        // Sort cmrFieldInfo in alphabetical order of CMR field name
        for (int i = cmrFieldInfo.length - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (cmrFieldInfo[j].name.compareTo(cmrFieldInfo[j + 1].name) > 0) {
                    CMRFieldInfo tmp = cmrFieldInfo[j];
                    cmrFieldInfo[j] = cmrFieldInfo[j+1];
                    cmrFieldInfo[j+1] = tmp;
                }
            }
        }
    }

    public void clearCMPFields() {
        this.cmpFields.clear();
        setCMPFields(this.cmpFields);
    }

    public void addCMPField(String field) {
        addCMPField(new FieldDescriptor(field));
    }

    public void addCMPField(FieldDescriptor fieldDesc) {
        this.cmpFields.add(fieldDesc);
        setCMPFields(this.cmpFields);
    }

    public void removeCMPField(String field) {
        removeCMPField(new FieldDescriptor(field));
    }

    public void removeCMPField(FieldDescriptor fieldDesc) {
        this.cmpFields.remove(fieldDesc);
        setCMPFields(this.cmpFields);
    }

    /**
     * Set the FieldDescriptor objects that the EJB container will persist
     * for this bean.
     */
    public void setCMPFields(Set<FieldDescriptor> cmpFields) {
        this.cmpFields = cmpFields;
        persFieldInfo = null;
        fieldInfoInitialized = false;
    }

    /**
     * Has the supplied field object been deemed persistent.
     */
    public boolean isCMPField(String field) {
        return this.getCMPFields().contains(new FieldDescriptor(field));
    }


    /**
     * Return the Set of fields deemed persistent.
     * The elements of this Set are FieldDescriptor objects.
     * This Set should be modified by calling addCMPField, removeCMPField
     */
    public Set<FieldDescriptor> getCMPFields() {
        return this.cmpFields;
    }

    /**
    public void clearPkeyFields() {
        this.pkeyFields.clear();
        setPkeyFields(this.pkeyFields);
    }

    public void addPkeyField(String field) {
        addPkeyField(new FieldDescriptor(field));
    }

    public void addPkeyField(FieldDescriptor fieldDesc) {
        this.pkeyFields.add(fieldDesc);
        setPkeyFields(this.pkeyFields);
    }

    public void removePkeyField(String field) {
        removePkeyField(new FieldDescriptor(field));
    }

    public void removePkeyField(FieldDescriptor fieldDesc) {
        this.pkeyFields.remove(fieldDesc);
        setPkeyFields(this.pkeyFields);
    }
    **/

    /**
     * Set the FieldDescriptor objects for primary key fields
     * for this bean.
     */
    public void setPkeyFields(Set<FieldDescriptor> pkeyFields) {
        this.pkeyFields = pkeyFields;
        fieldInfoInitialized = false;
        persFieldInfo = null;
        pkeyStuffInitialized = false;
    }

    /**
     * Return the Set of primary key fields.
     * The elements of this Set are FieldDescriptor objects.
     * This Set can be modified by calling addPkeyField, removePkeyField
     */
    public Set<FieldDescriptor> getPkeyFields() {
        if (!pkeyStuffInitialized) {
            initPkeyInfo();
        }
        return pkeyFields;
    }

    /**
     * Is the supplied field object one of the pkey fields.
     */
    public boolean isPkeyField(String field) {
        return isPkeyField(new FieldDescriptor(field));
    }

    public boolean isPkeyField(FieldDescriptor fieldDesc) {
        return this.getPkeyFields().contains(fieldDesc);
    }

    /**
     * Initialize pkeyFields, pkeyIsOneField, primaryKeyClassName
     * Must be called after this PersistenceDescriptor has been attached
     * to the Ejb/JoinDescriptor which has been
     * attached to the EjbBundleDescriptor.
     */
    private void initPkeyInfo() {
        try {
            pkeyIsOneField = false;
            pkeyFieldSpecified = true;
            primaryKeyClassName = parentDesc.getPrimaryKeyClassName();

            FieldDescriptor fd = parentDesc.getPrimaryKeyFieldDesc();
            if (pkeyFields == null || pkeyFields.isEmpty()) {
                pkeyFields = new HashSet<>();

                if (fd != null) {
                    // primkey-field was set
                    pkeyFields.add(fd);
                } else if (!primaryKeyClassName.equals("java.lang.Object")) {
                    // get fields of primaryKeyClass
                    primaryKeyClass = getClass(primaryKeyClassName);
                    Field[] fields = primaryKeyClass.getFields();
                    pkeyFieldsAllPrimitive = true;
                    for (Field field : fields) {
                        // ignore static or final fields
                        int m = field.getModifiers();
                        if ( Modifier.isStatic(m) || Modifier.isFinal(m) ) {
                            continue;
                        }
                        if (!field.getType().isPrimitive()) {
                            pkeyFieldsAllPrimitive = false;
                        }
                        pkeyFields.add(new FieldDescriptor(field.getName()));
                    }
                } else {
                    // Will use PM-generated primary key
                    primaryKeyClass = getClass(primaryKeyClassName);
                    pkeyIsOneField = true;
                    pkeyFieldSpecified = false;
                }
            }
            if (fd != null) {
                pkeyIsOneField = true;
            }

            pkeyStuffInitialized = true;
        } catch ( Exception ex ) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE,
                "enterprise.deployment.backend.invalidDescriptorMappingFailure", new Object[] {ex.toString()});
            throw new DeploymentException(ex);
        }
    }

    /**
     * @return true if the primary key of this object is one field
     * in its class and the type of the field is not a primitive type.
     * True for EJBs if the primkey-field deployment descriptor element
     * is specified, or if a container-inserted pk field is used.
     */
    public boolean primaryKeyIsOneField() {
        if (!pkeyStuffInitialized) {
            initPkeyInfo();
        }
        return pkeyIsOneField;
    }


    /**
     * @return false if the primkey-field is not specified and pk class = Object
     */
    public boolean primaryKeyIsSpecified() {
        if (!pkeyStuffInitialized) {
            initPkeyInfo();
        }
        return pkeyFieldSpecified;
    }


    /**
     * @return true if the primkey-field is not specified all fields of
     * pkey class are Java primitive types.
     */
    public boolean primaryKeyFieldsAllPrimitive() {
        if (!pkeyStuffInitialized) {
            initPkeyInfo();
        }
        return pkeyFieldsAllPrimitive;
    }


    /**
     * Get this bean's primary key class.
     * For EJBs, this is EjbEntityDescriptor.getPrimaryKeyClassName(),
     */
    public Class<?> getPrimaryKeyClass() {
        if (!pkeyStuffInitialized) {
            initPkeyInfo();
        }
        return primaryKeyClass;
    }


    // May return null for Join objects when called at/before deployment
    public Class<?> getPersistentClass() {
        if (persistentClass == null) {
            persistentClass = getClass(parentDesc.getEjbClassName());
        }
        return persistentClass;
    }


    public Class<?> getStateClass() {
        if (stateClass == null) {
            stateClass = getPersistentClass();
            if (parentDesc.isEJB20()) {
                if (!Modifier.isAbstract(stateClass.getModifiers())) {
                    throw new DeploymentException("2.x CMP bean class "
                        + stateClass.getName() + " must be decleared abstract "
                        + "or cmp-version for the corresponding bean must be set to 1.x.");
                }
                String stateClassName = parentDesc.getStateImplClassName();
                stateClass = getClass(stateClassName);
            }
        }
        return stateClass;
    }


    private Class<?> getClass(String className) {
        try {
            return getEjbBundleDescriptor().getClassLoader().loadClass(className);
        } catch (Exception ex) {
            throw new DeploymentException(ex);
        }
    }


    /**
     * Set the array of PersistentFieldInfo objects representing the
     * foreign key fields of this bean.
     */
    public void setFkeyFields(PersistentFieldInfo[] fkeyFields) {
        this.fkeyFields = fkeyFields;
        fieldInfoInitialized = false;
        persFieldInfo = null;
    }


    /**
     * Return the array of PersistentFieldInfo objects for the
     * foreign key fields of this bean.
     */
    public PersistentFieldInfo[] getFkeyFields() {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }
        return this.fkeyFields;
    }


    /**
     * Return the array of PersistentFieldInfo objects for the
     * CMP + foreign key fields
     */
    public PersistentFieldInfo[] getPersistentFieldInfo() {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }
        return persFieldInfo;
    }


    /**
     * Return the PersistentFieldInfo object for the given CMP/fkey field
     */
    public PersistentFieldInfo getPersistentFieldInfoByName(String fieldName) {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }
        for (PersistentFieldInfo element : persFieldInfo) {
            if (element.name.equals(fieldName)) {
                return element;
            }
        }
        throw new DeploymentException("PersistentFieldInfo not found for field " + fieldName);
    }


    /**
     * Return the array of PersistentFieldInfo objects for the CMP fields
     * which are not primary keys + foreign key fields.
     */
    public PersistentFieldInfo[] getNonPkeyPersFieldInfo() {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }
        return persNoPkeyFieldInfo;
    }


    /**
     * Return the array of PersistentFieldInfo objects for the pkey fields.
     */
    public PersistentFieldInfo[] getPkeyFieldInfo() {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }
        return pkeyFieldInfo;
    }


    /**
     * Return PersistentFieldInfo object for the given pkey field.
     */
    public PersistentFieldInfo getPkeyFieldInfoByName(String fieldName) {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }
        for (PersistentFieldInfo element : pkeyFieldInfo) {
            if (element.name.equals(fieldName)) {
                return element;
            }
        }
        throw new DeploymentException("PersistentFieldInfo not found for pkey field " + fieldName);
    }



    /**
     * @return an array of all Field objects in the primary key class.
     *         Returns null if primaryKeyIsOneField() == true.
     */
    public Field[] getPkeyClassFields() {
        if (!fieldInfoInitialized) {
            initializeFieldInfo();
        }
        return pkeyClassPkeyFields;
    }


    // Initialize persFieldInfo, pkeyFieldInfo, persNoPkeyFieldInfo,
    // pkeyClassPkeyFields
    private void initializeFieldInfo() {
        if (!pkeyStuffInitialized) {
            initPkeyInfo();
        }

        int cmpFieldCount = cmpFields.size();
        if (cmpFieldCount == 0) {
            throw new DeploymentException("No cmp field defined for CMP EJB " + parentDesc.getName());
        }

        int fkeyCount = 0;
        if (fkeyFields != null) {
            fkeyCount = fkeyFields.length;
        }

        persFieldInfo = new PersistentFieldInfo[cmpFieldCount + fkeyCount];
        // Add CMP fields
        int fcount = 0;
        for (FieldDescriptor cmpField : cmpFields) {
            persFieldInfo[fcount] = new PersistentFieldInfo();
            persFieldInfo[fcount].name = cmpField.getName();
            fcount++;
        }
        // Add foreign key fields
        if (fkeyFields != null) {
            for (PersistentFieldInfo fkeyField : fkeyFields) {
                persFieldInfo[fcount] = fkeyField;
                fcount++;
            }
        }

        // sort persFieldInfo in alphabetical order
        for (int i = persFieldInfo.length - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (persFieldInfo[j].name.compareTo(persFieldInfo[j + 1].name) > 0) {
                    PersistentFieldInfo tmp = persFieldInfo[j];
                    persFieldInfo[j] = persFieldInfo[j + 1];
                    persFieldInfo[j + 1] = tmp;
                }
            }
        }

        // Initialize pkeyFieldInfo[] and persNoPkeyFieldInfo[]
        // They contain the same PersistentFieldInfo objects as persFieldInfo.
        pkeyFieldInfo = new PersistentFieldInfo[pkeyFields.size()];
        if (pkeyFieldSpecified) {

            // check if PK class has public non-persistent fields
            StringBuilder nonPersFieldsInPK = new StringBuilder();
            for (FieldDescriptor fd : pkeyFields) {
                boolean isPersistent = false;
                for (PersistentFieldInfo element : persFieldInfo) {
                    if (fd.getName().equals(element.name)) {
                        isPersistent = true;
                        break;
                    }
                }
                if (!isPersistent) {
                    // if not the first non-persistent field
                    if (nonPersFieldsInPK.length() != 0) {
                        nonPersFieldsInPK.append(", ");
                    }
                    nonPersFieldsInPK.append(fd.getName());
                }
            }
            if (nonPersFieldsInPK.length() != 0) {
                throw new DeploymentException(localStrings.getLocalString(
                    "enterprise.deployment.pkhasnopersistentfields",
                    "CMP bean [{0}], primary key class [{1}] has " + "public non-persistent field(s) [{2}].",
                    new Object[] {getParentDescriptor().getName(), primaryKeyClassName, nonPersFieldsInPK.toString()}));
            }

            persNoPkeyFieldInfo = new PersistentFieldInfo[persFieldInfo.length - pkeyFieldInfo.length];
            int pkeyCount = 0;
            int noPkeyCount = 0;
            for (PersistentFieldInfo element : persFieldInfo) {
                boolean isPkey = false;
                for (FieldDescriptor fd : pkeyFields) {
                    if (fd.getName().equals(element.name)) {
                        isPkey = true;
                        break;
                    }
                }
                if (isPkey) {
                    pkeyFieldInfo[pkeyCount++] = element;
                } else {
                    persNoPkeyFieldInfo[noPkeyCount++] = element;
                }
            }
        }

        if (pkeyIsOneField && pkeyFieldSpecified) {
            // Initialize pkey field type. This is needed for
            // beans with no pkey field (i.e. PM-generated pkey)
            // because they wont have get/set for the pkey in the bean class
            // so the getCMPFieldType() below will bomb if type is not set.
            // Note: pkeyFieldInfo and persFieldInfo arrays share the same
            // PersistentFieldInfo objects.
            pkeyFieldInfo[0].type = getPrimaryKeyClass();
        }

        // Initialize Java types in persFieldInfo
        for (int i = 0; i < persFieldInfo.length; i++) {
            // fill type for CMP fields if not there
            // (fkey types will already be filled)
            if (persFieldInfo[i].type == null) {
                persFieldInfo[i].type = getCMPFieldType(persFieldInfo[i].name);
            }
        }

        // When called from Deploytool, the bean class is abstract,
        // and doesnt have the CMP fields: they will be in the generated
        // code after deployment. So all the java.lang.reflect.Field
        // values in PersistentFieldInfo can only be initialized at runtime.
        try {
            if (persistentClass != null && !Modifier.isAbstract(persistentClass.getModifiers())) {
                for (int i = 0; i < persFieldInfo.length; i++) {
                    persFieldInfo[i].field = getField(getStateClass(), persFieldInfo[i].name);
                }
            }

            // Initialize pkeyClassPkeyFields
            if (!pkeyIsOneField && primaryKeyClass != null && !Modifier.isAbstract(primaryKeyClass.getModifiers())) {
                pkeyClassPkeyFields = new Field[pkeyFieldInfo.length];
                for (int i = 0; i < pkeyFieldInfo.length; i++) {
                    pkeyClassPkeyFields[i] = primaryKeyClass.getField(pkeyFieldInfo[i].name);
                }
            }
        } catch (NoSuchFieldException ex) {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().log(Level.FINE, ex.toString(), ex);
            }
            throw new DeploymentException(ex);
        }

        fieldInfoInitialized = true;
    }


    private Field getField(final Class<?> c, final String name) throws NoSuchFieldException {
        Field field;
        try {
            // This is needed for EJB2.0 CMP beans whose
            // generated fields are private.
            field = c.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        }

        if (field == null) {
            field = c.getField(name);
        }

        return field;
    }

    /**
     * @return the Class object corresponding to the type of the given
     *         CMP field.
     */
    public Class<?> getTypeFor(String field) {
        return getCMPFieldType(field);
    }

    private Class<?> getCMPFieldType(String field) {
        Class<?> pclass = getPersistentClass();
        if (Modifier.isAbstract(pclass.getModifiers())) {
            // An EJB2.0 CMP bean : field is a virtual field
            String javaBeanName = capitalize(field);
            String getter = "get"+javaBeanName;
            try {
                Method method = pclass.getMethod(getter, (Class[]) null);
                return method.getReturnType();
            } catch ( Exception ex ) {
                throw new RuntimeException("Cannot find accessor " + getter + " for CMP field "+field);
            }
        }
        // field is a Java field in state class
        try {
            Field f = getField(getStateClass(), field);
            return f.getType();
        } catch ( NoSuchFieldException ex ) {
            throw new RuntimeException("Cant find CMP field "+field+" in class "+getStateClass().getName());
        }
    }


    // called from CMPClassGenerator too
    public static String capitalize(String name) {
        // EJB2.0 proposed final draft says that CMP/CMR field names
        // must begin with a lower case letter.
        if (Character.isUpperCase(name.charAt(0))) {
            throw new DeploymentException("CMP/CMR field " + name + " must start with a lower case character.");
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }


    public void setQueryFor(MethodDescriptor method, QueryDescriptor query) {
        queries.put(method, query);
    }


    public QueryDescriptor getQueryFor(MethodDescriptor method) {
        return queries.get(method);
    }


    public void removeQueryFor(MethodDescriptor method) {
        queries.remove(method);
    }


    /**
     * Use our own method equality check. This prevents problems
     * when a different classloader was used to load the input
     * method. Also note that two methods with the same name and
     * signature on *different* interfaces are considered EQUAL.
     * This matches the spec requirement that the same finder
     * method defined on the LocalHome and RemoteHome has only
     * ONE query-method declaration in the deployment descriptor.
     *
     * @param method
     * @param query
     */
    public void setQueryFor(Method method, QueryDescriptor query) {
        MethodDescriptor md = new MethodDescriptor(method, "");
        setQueryFor(md, query);
    }


    public QueryDescriptor getQueryFor(Method method) {
        // Use our own method equality check. See setQueryFor comment
        // for more details.
        MethodDescriptor md = new MethodDescriptor(method,"");
        return queries.get(md);
    }

    /**
     * Get all methods for which setQueryFor was done
     * @return a Set of Methods
     */
    public Set<MethodDescriptor> getQueriedMethods() {
        return queries.keySet();
    }


    /**
     * Return my formatted string representation.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n Entity descriptor");
        toStringBuffer.append("\n cmpFields ").append(cmpFields);
    }
}
