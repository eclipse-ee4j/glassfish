/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import org.glassfish.api.Param;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * A cluster defines a homogeneous set of server instances that share the same
 * applications, resources, and configuration.
 */
@Configured
public interface SshConnector extends ConfigBeanProxy {

    /**
     * Gets the value of the {@code sshPort} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "22")
    String getSshPort();

    /**
     * Sets the value of the {@code sshPort} property.
     *
     * @param sshPort allowed object is {@link String}
     */
    @Param(name = "sshport", optional = true, defaultValue = "22")
    void setSshPort(String sshPort) throws PropertyVetoException;

    /**
     * Gets the value of the {@code sshHost} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getSshHost();

    /**
     * Sets the value of the {@code sshHost} property.
     *
     * @param sshHost allowed object is {@link String}
     */
    @Param(name = "sshnodehost", optional = true)
    void setSshHost(String sshHost) throws PropertyVetoException;

    @Element
    SshAuth getSshAuth();

    void setSshAuth(SshAuth sshAuth);
}
