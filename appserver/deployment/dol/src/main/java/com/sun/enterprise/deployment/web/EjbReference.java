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

package com.sun.enterprise.deployment.web;

import com.sun.enterprise.deployment.BundleDescriptor;

/**
 * Specialization of ContextParameter that represents a link to an EJB.
 * @author Danny Coward
 */

public interface EjbReference extends ContextParameter {

    /**
     * Get the type of the EJB (Session, Entity or Message-Driven).
     * @return the type of the EJB.
     */
    public String getType();

    /**
     * Set the type of the EJB. Allowed values are Session, Entity or
     * Message-driven.
     * @param the type of the EJB.
     */
    public void setType(String type);

    /**
     * Gets the home classname of the referee EJB.
     * @return the class name of the EJB home.
     */
    public String getHomeClassName();

    /**
     * Sets the home classname of the referee EJB.
     * @param the class name of the EJB home.
     */
    public void setHomeClassName(String homeClassName);

    /**
     * Gets the bean instance interface classname of the referee EJB.
     * @return the classname of the EJB remote object.
     */
    public String getBeanClassName();

    /**
     * Sets the bean interface classname of the referee EJB.
     * @param the classname of the EJB remote object.
     */
    public void setBeanClassName(String beanClassName);

    /**
     * Gets the link name of the reference. For use when linking to an EJB
     * within a J2EE application.
     * @return the link name.
     */
    public String getLinkName();

    /**
     * Sets the link name of the reference. For use when linking to an EJB
     * within a J2EE application.
     * @param the link name.
     */
    public void setLinkName(String linkName);

    /**
     * Tests if the reference to the referree EJB is through local or
     * remote interface
     * @return true if using the local interfaces
     */
    public boolean isLocal();

    /**
     * Sets whether the reference uses the local or remote interfaces of
     * the referring EJB
     * @param true if using the local interface
     */
    public void setLocal(boolean isLocal);

    /**
     * Set the referring bundle, i.e. the bundle within which this
     * EJB reference is declared.
     */
    public void setReferringBundleDescriptor(BundleDescriptor referringBundle);

    /**
     * Get the referring bundle, i.e. the bundle within which this
     * EJB reference is declared.
     */
    public BundleDescriptor getReferringBundleDescriptor();
}
