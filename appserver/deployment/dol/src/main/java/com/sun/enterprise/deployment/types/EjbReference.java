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
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.InjectionCapable;

/**
 * Protocol associated with defining an EJB Interface
 *
 * @author Jerome Dochez
 */
public interface EjbReference  extends NamedInformation, InjectionCapable {

    /**
     * Get the type of the EJB (Session, Entity or Message-Driven).
     * @return the type of the EJB.
     */
    String getType();

    /**
     * Set the type of the EJB. Allowed values are Session, Entity or
     * Message-driven.
     * @param the type of the EJB.
     */
    void setType(String type);

    /**
     * Gets the home classname of the referee EJB.
     * @return the class name of the EJB home.
     */
    String getEjbHomeInterface();

    /**
     * Sets the local or remote home classname of the referee EJB.
     * @param the class name of the EJB home.
     */
    void setEjbHomeInterface(String ejbHomeInterface);

    /**
     * Gets the local or remote interface classname of the referee EJB.
     * @return the classname of the EJB remote object.
     */
    String getEjbInterface();

    /**
     * Sets the local or remote bean interface classname of the referee EJB.
     * @param the classname of the EJB remote object.
     */
    void setEjbInterface(String ejbInterface);

    /**
     * Gets the link name of the reference. For use when linking to an EJB
     * within a J2EE application.
     * @return the link name.
     */
    String getLinkName();

    /**
     * Sets the link name of the reference. For use when linking to an EJB
     * within a J2EE application.
     * @param the link name.
     */
    void setLinkName(String linkName);

    /**
     * Tests if the reference to the referree EJB is through local or
     * remote interface
     * @return true if using the local interfaces
     */
    boolean isLocal();

    /**
     * Sets whether the reference uses the local or remote interfaces of
     * the referring EJB
     * @param true if using the local interface
     */
    void setLocal(boolean isLocal);

    /**
     * Set the referring bundle, i.e. the bundle within which this
     * EJB reference is declared.
     */
    void setReferringBundleDescriptor(BundleDescriptor referringBundle);

    /**
     * Get the referring bundle, i.e. the bundle within which this
     * EJB reference is declared.
     */
    BundleDescriptor getReferringBundleDescriptor();

    /**
     * Set the jndi name for this ejb reference
     */
    void setJndiName(String jndiName);

    /**
     * @return the jndi name for this ejb reference
     */
    String getJndiName();

    boolean hasJndiName();

    boolean hasLookupName();
    String getLookupName();

    EjbDescriptor getEjbDescriptor();
    void setEjbDescriptor(EjbDescriptor descriptor);

    /**
     * @return true if the EJB reference is a 30 client view
     */
    boolean isEJB30ClientView();

}

