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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/ext/wsmgmt/WebServiceConfigCRTest.java,v 1.4 2007/05/05 05:24:04 tcfujii Exp $
* $Revision: 1.4 $
* $Date: 2007/05/05 05:24:04 $
*/
package org.glassfish.admin.amxtest.ext.wsmgmt;

import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.TransformationRuleConfig;
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 */
public final class WebServiceConfigCRTest
        extends AMXTestBase {

    public WebServiceConfigCRTest()
            throws IOException {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }

    public void testConfigCR() {
        assert (getDomainRoot().getWebServiceMgr() != null);

        final Set<J2EEApplicationConfig> s =
                getDomainRoot().getQueryMgr().queryJ2EETypeSet(XTypes.J2EE_APPLICATION_CONFIG);

        final Iterator iter = s.iterator();
        while (iter.hasNext()) {

            J2EEApplicationConfig ac = (J2EEApplicationConfig) iter.next();
            System.out.println("config name is " + ac.getName());

            Map m = ac.getWebServiceEndpointConfigMap();
            int init = m.size();
            System.out.println("WebServiceEndpoints found " + init);
            Iterator itr = m.values().iterator();
            while (itr.hasNext()) {
                WebServiceEndpointConfig wsCfg = (WebServiceEndpointConfig)
                        itr.next();
                System.out.println("WebServiceEndpoint's name " +
                        wsCfg.getName());
            }

            /*
             if ( !( ac.getName().equals("jaxrpc-simple") ) ){
                continue;
             }

            ac.createWebServiceEndpointConfig("remove#me", null);

            m = ac.getWebServiceEndpointConfigMap();
            int afterCreate  = m.size();
            System.out.println("WebServiceEndpoints found " + afterCreate);

            assert ( init +1== afterCreate);
            */
        }
        assert (true);
    }

    public void testWSConfigCR() {
        assert (getDomainRoot().getWebServiceMgr() != null);

        final Set<WebServiceEndpointConfig> s =
                getDomainRoot().getQueryMgr().queryJ2EETypeSet(
                        XTypes.WEB_SERVICE_ENDPOINT_CONFIG);

        for (final WebServiceEndpointConfig wsc : s) {
            Map m = wsc.getTransformationRuleConfigMap();
            int init = m.size();
            System.out.println("Transformation rules found " + init);
            Iterator itr = m.values().iterator();
            while (itr.hasNext()) {
                TransformationRuleConfig trCfg = (TransformationRuleConfig)
                        itr.next();
                System.out.println("Transformation Rule's name " +
                        trCfg.getName());
            }

            /*

            /* Finish later, creating a transformation rule requires the
             * transformation file to be uploaded to DAS.

            wsc.createTransformationRuleConfig("xrule22",
                "/tmp/req.xsl", false, "request", null);

            m = wsc.getTransformationRuleConfigMap();
            int afterCreate  = m.size();
            System.out.println("Transformation rules found " + afterCreate);

            assert ( init +1== afterCreate);

            wsc.removeTransformationRuleConfig("xrule22");
            m = wsc.getTransformationRuleConfigMap();
            int afterDel  = m.size();
            System.out.println("Transformation rules found " + afterDel);

            assert ( init == afterDel);
            */
        }
        assert (true);
    }

}


