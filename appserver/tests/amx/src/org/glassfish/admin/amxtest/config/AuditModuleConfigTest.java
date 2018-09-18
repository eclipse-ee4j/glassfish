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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/AuditModuleConfigTest.java,v 1.6 2007/05/05 05:23:54 tcfujii Exp $
* $Revision: 1.6 $
* $Date: 2007/05/05 05:23:54 $
*/
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.AuditModuleConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;

import java.util.Map;


/**
 */
public final class AuditModuleConfigTest
        extends ConfigMgrTestBase {
    static final String CLASSNAME = "com.sun.enterprise.security.Audit";

    public AuditModuleConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getConfigConfig().getSecurityServiceConfig());
        }
    }


    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("AuditModuleConfig");
    }


    public static AuditModuleConfig
    ensureDefaultInstance(final SecurityServiceConfig securityServiceConfig) {
        AuditModuleConfig result =
                securityServiceConfig.getAuditModuleConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(securityServiceConfig,
                                    getDefaultInstanceName(), CLASSNAME, false, null);
        }

        return result;
    }

    public static AuditModuleConfig
    createInstance(
            final SecurityServiceConfig securityServiceConfig,
            final String name,
            final String classname,
            final boolean enabled,
            final Map<String, String> optional) {
        return securityServiceConfig.createAuditModuleConfig(
                name, CLASSNAME, enabled, null);
    }


    protected Container
    getProgenyContainer() {
        return getConfigConfig().getSecurityServiceConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.AUDIT_MODULE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getConfigConfig().getSecurityServiceConfig().removeAuditModuleConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        return getConfigConfig().getSecurityServiceConfig().createAuditModuleConfig(name, CLASSNAME, false, options);
    }
}


