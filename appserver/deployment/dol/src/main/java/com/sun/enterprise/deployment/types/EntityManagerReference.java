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

package com.sun.enterprise.deployment.types;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.InjectionCapable;

import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;
import java.util.Map;

/** 
 * Protocol associated with defining an EntityManager reference
 *
 */
public interface EntityManagerReference extends NamedInformation, 
    InjectionCapable {

    /**
     * Set the unit name of the entity manager factory to which this
     * reference is associated.
     */
    public void setUnitName(String unitName);


    /**
     * Get the unit name of the entity manager factory to which this
     * reference is associated.
     */
    public String getUnitName();

    
    public void setPersistenceContextType(PersistenceContextType type);
    public PersistenceContextType getPersistenceContextType();

    public SynchronizationType getSynchronizationType();
    public void setSynchronizationType(SynchronizationType type);

    public Map<String,String> getProperties();

    /**
     * Set the referring bundle, i.e. the bundle within which this
     * reference is declared. 
     */
    public void setReferringBundleDescriptor(BundleDescriptor referringBundle);

    /**
     * Get the referring bundle, i.e. the bundle within which this
     * reference is declared.  
     */
    public BundleDescriptor getReferringBundleDescriptor();

}

