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

package org.glassfish.web.config.serverbeans;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;
import jakarta.validation.constraints.NotNull;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "managerProperties",
    "storeProperties"
}) */

@Configured
public interface SessionManager extends ConfigBeanProxy  {

    /**
     * Gets the value of the managerProperties property.
     *
     * @return possible object is
     *         {@link ManagerProperties }
     */
    @Element
    @NotNull
    public ManagerProperties getManagerProperties();

    /**
     * Sets the value of the managerProperties property.
     *
     * @param value allowed object is
     *              {@link ManagerProperties }
     */
    public void setManagerProperties(ManagerProperties value) throws PropertyVetoException;

    /**
     * Gets the value of the storeProperties property.
     *
     * @return possible object is
     *         {@link StoreProperties }
     */
    @Element
    @NotNull
    public StoreProperties getStoreProperties();

    /**
     * Sets the value of the storeProperties property.
     *
     * @param value allowed object is
     *              {@link StoreProperties }
     */
    public void setStoreProperties(StoreProperties value) throws PropertyVetoException;



}
