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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/ext/wsmgmt/WebServiceConfigTest.java,v 1.7 2007/05/05 05:24:04 tcfujii Exp $
* $Revision: 1.7 $
* $Date: 2007/05/05 05:24:04 $
*/
package org.glassfish.admin.amxtest.ext.wsmgmt;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.TransformationRuleConfig;
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public final class WebServiceConfigTest
        extends AMXTestBase {

    public WebServiceConfigTest()
            throws IOException {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }

    public void testConfigMBeans() {
        assert (getDomainRoot().getWebServiceMgr() != null);

        final Set<WebServiceEndpointConfig> s =
                getDomainRoot().getQueryMgr().queryJ2EETypeSet(
                        XTypes.WEB_SERVICE_ENDPOINT_CONFIG);

        for (final WebServiceEndpointConfig wsc : s) {
            String oldSize = wsc.getMaxHistorySize();
            System.out.println("Old Max History size is " + oldSize);
            System.out.println("Setting Max History size to 1 ");
            wsc.setMaxHistorySize("1");
            System.out.println("New Max History size is  "
                    + wsc.getMaxHistorySize());
            assert ("1".equals(wsc.getMaxHistorySize()));
            System.out.println("Resetting Max History size to " + oldSize);
            wsc.setMaxHistorySize(oldSize);
            System.out.println("Config value is " + wsc.getMonitoringLevel());

            Map m = wsc.getTransformationRuleConfigMap();

            System.out.println("Transformation rules found " + m.size());

            Iterator itr = m.values().iterator();
            while (itr.hasNext()) {
                TransformationRuleConfig tc = (TransformationRuleConfig)
                        itr.next();
                System.out.println("rule name is " + tc.getName());
            }
            System.out.println("Getting tranformation rules in order ");
            List l = wsc.getTransformationRuleConfigList();

            System.out.println("Transformation rules found " + l.size());

            Iterator litr = l.iterator();
            while (litr.hasNext()) {
                TransformationRuleConfig tc = (TransformationRuleConfig)
                        litr.next();
                System.out.println("rule name is " + tc.getName());
            }
        }
        assert (true);
    }
}


