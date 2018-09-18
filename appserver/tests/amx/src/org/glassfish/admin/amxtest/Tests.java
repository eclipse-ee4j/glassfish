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

package org.glassfish.admin.amxtest;


import com.sun.appserv.management.util.misc.TypeCast;
import org.glassfish.admin.amxtest.base.*;
import org.glassfish.admin.amxtest.client.MiscTest;
import org.glassfish.admin.amxtest.client.PerformanceTest;
import org.glassfish.admin.amxtest.client.ProxyFactoryTest;
import org.glassfish.admin.amxtest.client.ProxyTest;
import org.glassfish.admin.amxtest.config.*;
import org.glassfish.admin.amxtest.ext.logging.LoggingHelperTest;
import org.glassfish.admin.amxtest.ext.logging.LoggingTest;
import org.glassfish.admin.amxtest.ext.logging.StatefulLoggingHelperTest;
import org.glassfish.admin.amxtest.helper.RefHelperTest;
import org.glassfish.admin.amxtest.j2ee.J2EETest;
import org.glassfish.admin.amxtest.j2ee.ServletTest;
import org.glassfish.admin.amxtest.monitor.CallFlowMonitorTest;
import org.glassfish.admin.amxtest.monitor.JMXMonitorMgrTest;
import org.glassfish.admin.amxtest.monitor.MonitorTest;
import org.glassfish.admin.amxtest.support.CoverageInfoTest;

import java.util.ArrayList;
import java.util.List;


/**
 <b>The place</b> to put list any new test; the official list
 of tests.  The file amxtest.classes is also used, but since
 it may be inadvertantly modified, this is the official list
 of tests.
 */
public class Tests {
    private Tests() {}

    private static final Class<junit.framework.TestCase>[] TestClasses =
            TypeCast.asArray(new Class[]
            {
                TestTemplateTest.class, // ensure that the template one works OK, too!

                // these tests are standalone and do not require a
                // server connection
                CoverageInfoTest.class,

                //  Tests that follow require a server connection
                //AppserverConnectionSourceTest.class,
                RunMeFirstTest.class,

                ProxyTest.class,
                ProxyFactoryTest.class,
                AMXTest.class,
                GetSetAttributeTest.class,
                ContainerTest.class,
                GenericTest.class,
                PropertiesAccessTest.class,
                SystemPropertiesAccessTest.class,

                LoggingTest.class,
                LoggingHelperTest.class,
                StatefulLoggingHelperTest.class,

                DomainRootTest.class,
                UploadDownloadMgrTest.class,
                BulkAccessTest.class,
                QueryMgrTest.class,
                NotificationEmitterServiceTest.class,
                NotificationServiceMgrTest.class,
                NotificationServiceTest.class,
                MiscTest.class,

                MonitorTest.class,
                JMXMonitorMgrTest.class,

                J2EETest.class,
                ServletTest.class,

                DanglingRefsTest.class,
                ConfigRunMeFirstTest.class,
                DescriptionTest.class,
                EnabledTest.class,
                LibrariesTest.class,
                RefHelperTest.class,
                ListenerTest.class,
                DomainConfigTest.class,
                ConfigConfigTest.class,
                SecurityServiceConfigTest.class,
                MessageSecurityConfigTest.class,
                StandaloneServerConfigTest.class,
                ClusteredServerConfigTest.class,
                NodeAgentConfigTest.class,
                CustomMBeanConfigTest.class,
                ReferencesTest.class,
                HTTPServiceConfigTest.class,
                HTTPListenerConfigTest.class,
                ClusterConfigTest.class,
                SSLConfigTest.class,
                JMXConnectorConfigTest.class,
                IIOPListenerConfigTest.class,
                HTTPListenerConfigTest.class,
                AuditModuleConfigTest.class,
                AuthRealmConfigTest.class,
                JavaConfigTest.class,
                ProfilerConfigTest.class,
                VirtualServerConfigTest.class,
                JACCProviderConfigTest.class,
                AdminObjectResourceConfigTest.class,
                JDBCResourceConfigTest.class,
                MailResourceConfigTest.class,
                ConnectorConnectionPoolConfigTest.class,
                JDBCConnectionPoolConfigTest.class,
                PersistenceManagerFactoryResourceConfigTest.class,
                JNDIResourceConfigTest.class,
                ThreadPoolConfigTest.class,
                LBTest.class,
                SecurityMapConfigTest.class,
                ConnectorConnectionPoolConfigTest.class,
                ResourceAdapterConfigTest.class,
                CustomResourceConfigTest.class,
                ConnectorServiceConfigTest.class,
                DiagnosticServiceConfigTest.class,

                PerformanceTest.class,
                CallFlowMonitorTest.class,
                RunMeLastTest.class,
            });

    public static List<Class<junit.framework.TestCase>>
    getTestClasses() {
        final List<Class<junit.framework.TestCase>> classes =
                new ArrayList<Class<junit.framework.TestCase>>();

        for (int i = 0; i < TestClasses.length; ++i) {
            final Class<junit.framework.TestCase> testClass = TestClasses[i];

            classes.add(testClass);
        }

        return (classes);
    }

};

