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

package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;

import org.glassfish.grizzly.config.dom.Ssl;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * Specifies the SSL configuration when the Application Server is making outbound IIOP/SSL connections.
 */

/* @XmlType(name = "", propOrder = {
    "ssl"
}) */

@Configured
public interface SslClientConfig extends ConfigBeanProxy {

    /**
     * Gets the value of the ssl property.
     *
     * @return possible object is {@link Ssl }
     */
    @Element(required = true)
    Ssl getSsl();

    /**
     * Sets the value of the ssl property.
     *
     * @param value allowed object is {@link Ssl }
     */
    void setSsl(Ssl value) throws PropertyVetoException;

}
