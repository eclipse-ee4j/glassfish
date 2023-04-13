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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.glassfish.api.Param;

/**
 * A cluster defines a homogeneous set of server instances that share
 * the same applications, resources, and configuration.
 */
@Configured
public interface SshAuth extends ConfigBeanProxy {

    /**
     * Points to a username.
     *
     * @return a username name
     */
    @Attribute(defaultValue = "${user.name}")
    String getUserName();

    /**
     * Sets the value of the {@code userName} property.
     *
     * @param userName allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "sshuser", optional = true, defaultValue = "${user.name}")
    void setUserName(String userName) throws PropertyVetoException;

    /**
     * Points to a key file.
     *
     * @return a key file name
     */
    @Attribute
    String getKeyfile();

    /**
     * Sets the value of the {@code keyfile} property.
     *
     * @param keyfile allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "sshkeyfile", optional = true)
    void setKeyfile(String keyfile) throws PropertyVetoException;

    /**
     * SSH {@code password}.
     *
     * @return SSH Password which may be a password alias of the form ${ALIAS=aliasname}
     */
    @Attribute
    String getPassword();

    /**
     * Sets the value of the {@code password} property.
     *
     * @param password allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "sshpassword", optional = true)
    void setPassword(String password) throws PropertyVetoException;

    /**
     * SSH Keyfile {@code passphrase}.
     *
     * @return SSH keyfile encryption {@code passphrase} which may be a password alias of the form ${ALIAS=aliasname}
     */
    @Attribute
    String getKeyPassphrase();

    /**
     * Sets the value of the {@code passphrase} property.
     *
     * @param passphrase allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "sshkeypassphrase", optional = true)
    void setKeyPassphrase(String passphrase) throws PropertyVetoException;
}
