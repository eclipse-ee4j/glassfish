/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.modularity.tests;

import java.beans.PropertyVetoException;

import org.glassfish.api.admin.config.ConfigExtension;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;

/**
 * @author Masoud Kalali
 */
@Configured
@com.sun.enterprise.config.modularity.annotation.CustomConfiguration(baseConfigurationFileName = "conf-extension-two.xml")
@com.sun.enterprise.config.modularity.annotation.HasCustomizationTokens
public interface ConfigExtensionTwo extends ConfigExtension {

    @Attribute(defaultValue = "extension.type.two.attr")
    String getAttributeTwo();

    void setAttributeTwo(String value) throws PropertyVetoException;
}
