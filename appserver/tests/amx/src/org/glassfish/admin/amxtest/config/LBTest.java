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

import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.ClusterSupportRequired;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 Unit test class to create-delete lb-config and load-balancer elements
 */
public final class LBTest
        extends AMXTestBase
        implements ClusterSupportRequired {

    final boolean runDels = true;
    final boolean runCreates = true;
    final boolean testGetLoadBalancerConfigMap = false;

    public LBTest() {}

    public void testCreateLBConfig() {
        if (checkNotOffline("testDeleteLBConfig")) {
            if (!runCreates) {
                return;
            }
            String name = "test-lb-config";
            boolean monitoringEnabled = true;
            boolean routeCookieEnabled = false;
            boolean httpsRouting = false;
            String responseTimeout = "130";
            String reloadInterval = "380";

            Map<String, String> params = new HashMap<String, String>();
            //params.put("name", name);
            params.put("route-cookie-enabled", "" + routeCookieEnabled);
            params.put("monitoring-enabled", "" + monitoringEnabled);
            params.put("https-routing", "" + httpsRouting);
            params.put("response-timeout-in-seconds", responseTimeout);
            params.put("reload-poll-interval-in-seconds", reloadInterval);

            try {
                getDomainConfig().getLBConfigsConfig().createLBConfig(name, params);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void testCreateLoadBalancerConfig() {
        if (checkNotOffline("testDeleteLBConfig")) {
            if (!runCreates) {
                return;
            }
            String name = "test-load-balancer";
            String lbConfigName = "test-lb-config";
            boolean autoApplyEnabled = true;
            Map<String, String> optional = null;
            try {
                getDomainConfig().getLoadBalancersConfig().createLoadBalancerConfig(name, lbConfigName, autoApplyEnabled, optional);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void testGetLBConfigMap() {
        try {
            Map map = getDomainConfig().getLBConfigsConfig().getLBConfigMap();
            //System.out.println("Here is a list of Load Balancer Config MBeans in DAS: ");
            //System.out.println(map);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void testGetLoadBalancerConfigMap() {
        if (testGetLoadBalancerConfigMap == false) {
            return;
        }
        try {
            Map map = getDomainConfig().getLoadBalancersConfig().getLoadBalancerConfigMap();
            //System.out.println("Here is a list of Load Balancer Config MBeans in DAS: ");
            //System.out.println(map);
            ObjectName objName = new ObjectName("amx:j2eeType=X-LoadBalancerConfig,name=test-load-balancer");
            MBeanInfo minfo = getConnection().getMBeanInfo(objName);
            MBeanAttributeInfo[] mattrsinfo = minfo.getAttributes();
            /*
            for (MBeanAttributeInfo mattrinfo : mattrsinfo)
            {
                System.out.println("Attribute Name is : " + mattrinfo.getName());
            }
            */

            String[] attrsNames = (String[]) getConnection().getAttribute(objName, "AttributeNames");
            /*for (String attrName : attrsNames)
                System.out.println("Actual Attribute Name is : " + attrName);
            System.out.println("AttributeNames are : " + attrsNames);
            System.out.println("MBeanInfo is \n"+minfo);*/

            String attrName = (String) getConnection().getAttribute(objName, "LbConfigName");
            System.out.println("attrName is = " + attrName);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void testDeleteLoadBalancerConfig() {
        if (checkNotOffline("testDeleteLBConfig")) {

            if (!runDels) {
                return;
            }
            String name = "test-load-balancer";
            try {
                getDomainConfig().getLoadBalancersConfig().removeLoadBalancerConfig(name);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void testDeleteLBConfig() {
        if (checkNotOffline("testDeleteLBConfig")) {

            if (!runDels) {
                return;
            }
            String name = "test-lb-config";
            try {
                getDomainConfig().getLBConfigsConfig().removeLBConfig(name);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
