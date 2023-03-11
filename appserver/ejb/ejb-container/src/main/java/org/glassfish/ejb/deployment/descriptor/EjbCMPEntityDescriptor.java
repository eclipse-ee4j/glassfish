/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.BeanMethodCalculatorImpl;

/**
 * This class contains information about EJB1.1 and EJB2.0 CMP EntityBeans.
 * @author Sanjeev Krishnan
 */
public class EjbCMPEntityDescriptor extends EjbEntityDescriptor {

    private static final long serialVersionUID = 1L;
    private static final String FIELD_ACCESS_METHOD_PREFIX   = "get"; // NOI18N

    // Enum types to be returned from getVersionNumber().
    public static final int UNDEFINED = -1;
    public static final int CMP_1_1 = 1;
    public static final int CMP_2_x = 2;

    private int cmpVersion = UNDEFINED;
    private PersistenceDescriptor pers;
    private String abstractSchemaName;
    private FieldDescriptor primaryKeyFieldDesc;
    private String stateImplClassName;
    private String ejbImplementationImplClassName;

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EjbCMPEntityDescriptor.class);

    public EjbCMPEntityDescriptor() {
        this.setPersistenceType(CONTAINER_PERSISTENCE);
    }

    /**
     * The copy constructor.
     */
    public EjbCMPEntityDescriptor(EjbDescriptor other) {
        super(other);

        this.setPersistenceType(CONTAINER_PERSISTENCE);

        if (other instanceof EjbCMPEntityDescriptor) {
            EjbCMPEntityDescriptor entity = (EjbCMPEntityDescriptor) other;
            this.pers = entity.pers;
            this.cmpVersion = entity.cmpVersion;
            this.abstractSchemaName = entity.abstractSchemaName;
        }
    }

    /**
     * Sets the State class implementation classname.
     */
    public void setStateImplClassName(String name) {
        this.stateImplClassName = name;
    }

    /**
     * Returns the classname of the State class impl.
     */
    public String getStateImplClassName() {
        return this.stateImplClassName;
    }

    @Override
    public Vector<Field> getFields() {
        Vector<Field> fields = new Vector<>();
        if (isEJB20()) {
            // All cmp "fields" are abstract, so we can't construct
            // java.lang.reflect.Field elements from them.  Use
            // getFieldDescriptors() instead.
        } else {
            fields = super.getFields();
        }
        return fields;
    }

    @Override
    public Vector<FieldDescriptor> getFieldDescriptors() {
        Vector<FieldDescriptor> fieldDescriptors = new Vector<>();
        if (isEJB20()) {
            try {
                ClassLoader cl = getEjbBundleDescriptor().getClassLoader();
                BeanMethodCalculatorImpl bmc = new BeanMethodCalculatorImpl();
                fieldDescriptors = bmc.getPossibleCmpCmrFields(cl, this.getEjbClassName());
            } catch (Throwable t) {
                String errorMsg = localStrings.getLocalString("enterprise.deployment.errorloadingejbclass",
                    "error loading the ejb class {0} in getFields" + " on EjbDescriptor\n {1}",
                    new Object[] {this.getEjbClassName(), t.toString()});
                _logger.log(Level.FINE, errorMsg);
            }
        } else {
            fieldDescriptors = super.getFieldDescriptors();
        }
        return fieldDescriptors;
    }


    /**
     * @return CMP version as an enum type.
     */
    public int getCMPVersion() {
        if (cmpVersion == UNDEFINED) {
            if (getEjbBundleDescriptor() != null) {
                String bundleVersion = getEjbBundleDescriptor().getSpecVersion();
                if (bundleVersion.startsWith("1.")) {
                    cmpVersion = CMP_1_1;
                } else {
                    cmpVersion = CMP_2_x;
                }
            } else {
                // we cannot get the version from the bundle, set it to default...
                cmpVersion = CMP_2_x;
            }
        }
        return cmpVersion;
    }

    /**
     * Set the CMP version
     */
    public void setCMPVersion(int version) {
        if (version == CMP_1_1 || version == CMP_2_x) {
            cmpVersion = version;
        } else {
            throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.invalidcmpversion",
                "Invalid CMP version: {0}.", new Object[] {Integer.valueOf(version)})); // NOI18N
        }
    }


    /**
     * return true if this is an EJB2.0 CMP Entitybean
     * DEPRECATED
     */
    public boolean isEJB20() {
        return getCMPVersion() == CMP_2_x;
    }


    @Override
    public Vector<ContainerTransaction> getPossibleTransactionAttributes() {
        Vector<ContainerTransaction> txAttributes = null;
        if (isEJB20()) {
            txAttributes = new Vector<>();
            txAttributes.add(new ContainerTransaction(ContainerTransaction.REQUIRED, ""));
            txAttributes.add(new ContainerTransaction(ContainerTransaction.REQUIRES_NEW, ""));
            txAttributes.add(new ContainerTransaction(ContainerTransaction.MANDATORY, ""));
            if (isTimedObject()) {
                txAttributes.add(new ContainerTransaction(ContainerTransaction.NOT_SUPPORTED, ""));
            }
        } else {
            txAttributes = super.getPossibleTransactionAttributes();
        }
        return txAttributes;
    }


    public void setPersistenceDescriptor(PersistenceDescriptor pd) {
        this.pers = pd;
        pd.setParentDescriptor(this);
    }


    public PersistenceDescriptor getPersistenceDescriptor() {
        if (pers == null) {
            pers = new PersistenceDescriptor();
            this.setPersistenceDescriptor(pers);
        }
        return pers;
    }


    private void invalidatePersistenceInfo() {
        if (pers != null) {
            pers.invalidate();
        }
    }


    public void setPrimaryKeyFieldDesc(FieldDescriptor pkf) {
        this.primaryKeyFieldDesc = pkf;
        invalidatePersistenceInfo();
    }


    public FieldDescriptor getPrimaryKeyFieldDesc() {
        return primaryKeyFieldDesc;
    }


    public void setAbstractSchemaName(String abstractSchemaName) {
        this.abstractSchemaName = abstractSchemaName;
    }


    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    /**
     * set the generated implementation class for a CMP 2.0 Ejb object
     * @param className the generated implementation
     */
    public void setEjbImplementationImplClassName(String className) {
        ejbImplementationImplClassName = className;
    }

    /**
     * @return the generated implementation class
     */
    public String getEjbImplementationImplClassName() {
        return ejbImplementationImplClassName;
    }


    public static Vector<FieldDescriptor> getPossibleCmpCmrFields(ClassLoader cl, String className) throws Exception {
        Vector<FieldDescriptor> fieldDescriptors = new Vector<>();
        Class theClass = cl.loadClass(className);

        // Start with all *public* methods
        Method[] methods = theClass.getMethods();

        // Find all accessors that could be cmp fields. This list
        // will contain all cmr field accessors as well, since there
        // is no good way to distinguish between the two purely based
        // on method signature.
        for (Method next : methods) {
            String nextName = next.getName();
            int nextModifiers = next.getModifiers();
            if (Modifier.isAbstract(nextModifiers)) {
                if (nextName.startsWith(FIELD_ACCESS_METHOD_PREFIX) && nextName.length() > 3) {
                    String field = nextName.substring(3, 4).toLowerCase(Locale.US) + nextName.substring(4);
                    fieldDescriptors.add(new FieldDescriptor(field));
                }
            }
        }
        return fieldDescriptors;
    }

    /**
    * Return my formatted string representation.
    */
    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n cmpVersion ").append(cmpVersion).append("\n primKeyField ");

        if (getPrimaryKeyFieldDesc() != null) {
            ((Descriptor) getPrimaryKeyFieldDesc()).print(toStringBuffer);
        }

        if (pers != null) {
            ((Descriptor) pers).print(toStringBuffer);
        }
    }
}
