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
    "sessionManager",
    "sessionProperties"
}) */

@Configured
public interface SessionConfig extends ConfigBeanProxy  {

    /**
     * Gets the value of the sessionManager property.
     *
     * @return possible object is
     *         {@link SessionManager }
     */
    @Element
    @NotNull
    public SessionManager getSessionManager();

    /**
     * Sets the value of the sessionManager property.
     *
     * @param value allowed object is
     *              {@link SessionManager }
     */
    public void setSessionManager(SessionManager value) throws PropertyVetoException;

    /**
     * Gets the value of the sessionProperties property.
     *
     * @return possible object is
     *         {@link SessionProperties }
     */
    @Element
    @NotNull
    public SessionProperties getSessionProperties();

    /**
     * Sets the value of the sessionProperties property.
     *
     * @param value allowed object is
     *              {@link SessionProperties }
     */
    public void setSessionProperties(SessionProperties value) throws PropertyVetoException;



}
