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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.glassfish.api.Param;

import java.beans.PropertyVetoException;

/**
 * A cluster defines a homogeneous set of server instances that share the same
 * applications, resources, and configuration.
 */
@Configured
public interface SshAuth  extends ConfigBeanProxy {
 
    
    /**
     * points to a named host. 
     *
     * @return a named host name
     */
    @Attribute(defaultValue="${user.name}")
    String getUserName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="sshuser", optional=true,defaultValue="${user.name}")
    void setUserName(String value) throws PropertyVetoException;

    /**
     * points to a named host.
     *
     * @return a named host name
     */

    @Attribute
    String getKeyfile();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="sshkeyfile", optional=true)
    void setKeyfile(String value) throws PropertyVetoException;

    /**
     * SSH Password
     *
     * @return SSH Password which may be a password alias of the form
     *         ${ALIAS=aliasname}
     */

    @Attribute
    String getPassword();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="sshpassword", optional=true)
    void setPassword(String value) throws PropertyVetoException;

    /**
     * SSH Keyfile passphrase
     *
     * @return SSH keyfile encryption passphrase which may be a password alias
     * of the form ${ALIAS=aliasname}
     */

    @Attribute
    String getKeyPassphrase();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name="sshkeypassphrase", optional=true)
    void setKeyPassphrase(String value) throws PropertyVetoException;

}
