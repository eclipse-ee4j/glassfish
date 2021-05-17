/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.types.EntityManagerReference;
import com.sun.enterprise.deployment.util.DOLUtils;

import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.SynchronizationType;
import java.util.HashMap;
import java.util.Map;

/**
 * An object representing an component environment reference
 * to an EntityManager
 *
*/
public class EntityManagerReferenceDescriptor extends
    EnvironmentProperty implements EntityManagerReference {

    private String unitName = null;
    private PersistenceContextType contextType = PersistenceContextType.TRANSACTION;
    private SynchronizationType synchronizationType = SynchronizationType.SYNCHRONIZED;
    private BundleDescriptor referringBundle;

    private Map<String, String> properties = new HashMap<String,String>();

    public EntityManagerReferenceDescriptor(String name,
                                            String unitName,
                                            PersistenceContextType type) {
        super(name, "", "");

        this.unitName = unitName;
        this.contextType = type;
    }

    public EntityManagerReferenceDescriptor() {}

    public void setUnitName(String unitName) {

        this.unitName = unitName;
    }

    public String getUnitName() {

        return unitName;

    }

    public String getInjectResourceType() {
        return "jakarta.persistence.EntityManager";
    }

    public void setInjectResourceType(String resourceType) {
    }

    public void setPersistenceContextType(PersistenceContextType type) {

        contextType = type;

    }

    public PersistenceContextType getPersistenceContextType() {

        return contextType;

    }


    public SynchronizationType getSynchronizationType() {
        return synchronizationType;
    }


    public void setSynchronizationType(SynchronizationType synchronizationType) {
        this.synchronizationType = synchronizationType;
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public Map<String,String> getProperties() {
        return new HashMap<String,String>(properties);
    }

    public void setReferringBundleDescriptor(BundleDescriptor referringBundle)
    {
    this.referringBundle = referringBundle;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    public BundleDescriptor getReferringBundleDescriptor()
    {
    return referringBundle;
    }

    public boolean isConflict(EntityManagerReferenceDescriptor other) {
        return getName().equals(other.getName()) &&
            (!(
                DOLUtils.equals(getUnitName(), other.getUnitName()) &&
                DOLUtils.equals(getPersistenceContextType(), other.getPersistenceContextType()) &&
                properties.equals(other.properties)
                ) ||
            isConflictResourceGroup(other));
    }
}

