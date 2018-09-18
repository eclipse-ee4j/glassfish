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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.config.AdminServiceConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;
import org.glassfish.admin.amxtest.AMXTestBase;


/**
 This test should normally be run before the generic tests
 so that it can set up default items for many of the config elements
 so that the generic tests will actually test them. Otherwise,
 when the generic tests are run, they won't see any instances
 of many of the AMXConfig MBeans.
 <p/>
 If there are errors doing this, disable this test in amxtest.classes,
 fix the error in the specific place it's occurring, then re-enabled
 this test.
 */
public final class ConfigRunMeFirstTest
        extends AMXTestBase {
    public ConfigRunMeFirstTest() {
    }

    private void
    setupConfig() {
        final DomainConfig dc = getDomainConfig();
        final ConfigConfig cc = getConfigConfig();
        final SecurityServiceConfig ss = cc.getSecurityServiceConfig();
        final AdminServiceConfig as = cc.getAdminServiceConfig();

        AuditModuleConfigTest.ensureDefaultInstance(ss);

        AuthRealmConfigTest.ensureDefaultInstance(ss);

        ConnectorConnectionPoolConfigTest.ensureDefaultInstance(dc);

        JMXConnectorConfigTest.ensureDefaultInstance(as);

        ResourceAdapterConfigTest.ensureDefaultInstance(dc);

        AdminObjectResourceConfigTest.ensureDefaultInstance(dc);

        JDBCConnectionPoolConfigTest.ensureDefaultInstance(dc);

        JDBCResourceConfigTest.ensureDefaultInstance(dc);

        JNDIResourceConfigTest.ensureDefaultInstance(dc);

        ConnectorResourceConfigTest.ensureDefaultInstance(dc);

        CustomMBeanConfigTest.ensureDefaultInstance(dc);

        JACCProviderConfigTest.ensureDefaultInstance(ss);

        MailResourceConfigTest.ensureDefaultInstance(dc);

        ThreadPoolConfigTest.ensureDefaultInstance(cc);

        PersistenceManagerFactoryResourceConfigTest.ensureDefaultInstance(dc);

        CustomResourceConfigTest.ensureDefaultInstance(dc);

        ProfilerConfigTest.ensureDefaultInstance(cc.getJavaConfig());
    }

    public void
    testSetup() {
        if (checkNotOffline("testIllegalCreate")) {
            setupConfig();
        }
    }
}














