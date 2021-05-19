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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.types.EntityManagerFactoryReference;
import com.sun.enterprise.deployment.util.DOLUtils;

/**
 * An object representing a component environment reference
 * to an EntityManagerFactory
 */
public class EntityManagerFactoryReferenceDescriptor extends
    EnvironmentProperty implements EntityManagerFactoryReference {

    private String unitName = null;
    private BundleDescriptor referringBundle;

    public EntityManagerFactoryReferenceDescriptor(String name, String unitName) {
        super(name, "", "");
        this.unitName = unitName;
    }

    public EntityManagerFactoryReferenceDescriptor() {}

    @Override
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public String getUnitName() {
        return unitName;
    }

    @Override
    public String getInjectResourceType() {
        return "jakarta.persistence.EntityManagerFactory";
    }

    @Override
    public void setInjectResourceType(String resourceType) {
    }

    @Override
    public void setReferringBundleDescriptor(BundleDescriptor referringBundle) {
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

    @Override
    public BundleDescriptor getReferringBundleDescriptor() {
        return referringBundle;
    }

    public boolean isConflict(EntityManagerFactoryReferenceDescriptor other) {
        return (getName().equals(other.getName())) &&
            (!DOLUtils.equals(getUnitName(), other.getUnitName())
            || isConflictResourceGroup(other));
    }
}

