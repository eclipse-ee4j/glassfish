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

package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
import org.jvnet.hk2.config.DuckTyped;

import java.util.Properties;

/**
 * Records information about a tenant provisioned for a given application.
 *
 * @author Tim Quinn
 */
@Configured
public interface AppTenant extends ConfigBeanProxy {

    @Attribute
    String getTenant();

    void setTenant(String value) throws PropertyVetoException;

    @Attribute
    String getContextRoot();

    void setContextRoot(String value) throws PropertyVetoException;

    @DuckTyped
    Properties getDeployProperties();

    class Duck {
        public static Properties getDeployProperties(AppTenant instance) {
            Properties deploymentProps = new Properties();
            if (instance.getContextRoot() != null) {
                deploymentProps.setProperty(ServerTags.CONTEXT_ROOT, instance.getContextRoot());
            }
            return deploymentProps;
        }
    }
}
