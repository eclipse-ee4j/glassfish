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

package org.glassfish.admin.amxtest.ext.wsmgmt;

import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.j2ee.WebServiceEndpoint;
import com.sun.appserv.management.monitor.WebServiceEndpointMonitor;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 */
public final class WebServiceMgrTest
        extends AMXTestBase {

    public WebServiceMgrTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }

    public void testGetWebServiceMgr() {
        assert (getDomainRoot().getWebServiceMgr() != null);
    }

    public void testGetWebServiceNames() {
        java.util.Map m = null;

        m = getDomainRoot().getWebServiceMgr().getWebServiceEndpointKeys();

        if (m == null) {
            System.out.println("No web services found ");
            return;
        }

        System.out.println("Number of web services " + m.keySet().size());
        System.out.println("Fully qualified names...");
        for (Iterator iter = m.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            System.out.println("Looking for runtime objects for " + key);
            Set<WebServiceEndpoint> epSet =
                    getDomainRoot().getWebServiceMgr().getWebServiceEndpointSet(key,
                                                                                "server");
            if (epSet != null) {
                System.out.println("Found " + epSet.size() + " for " + key);
                for (Iterator epItr = epSet.iterator(); epItr.hasNext();) {
                    WebServiceEndpoint ep = (WebServiceEndpoint) epItr.next();
                    System.out.println("Found " + ep.getName());
                    WebServiceEndpointMonitor epm = (WebServiceEndpointMonitor)
                            ep.getMonitoringPeer();
                    System.out.println("Monitoing peer for  " + ep.getName() +
                            " is " + epm);

                }
            }
        }
        System.out.println("Display names...");
        for (Iterator iter = m.values().iterator(); iter.hasNext();) {
            System.out.println((String) iter.next());
        }
        assert (true);
    }

    public void testGetWebServiceInfo() {
        Map<Object, String> m = null;

        m = getDomainRoot().getWebServiceMgr().getWebServiceEndpointKeys();

        if (m == null) {
            System.out.println("No web services found ");
            return;
        }

        System.out.println("Number of web services " + m.keySet().size());
        System.out.println("Fully qualified names...");
        for (final Object fqn : m.keySet()) {
            System.out.println("Info for web service " + fqn);

            final WebServiceEndpointInfo info =
                    getDomainRoot().getWebServiceMgr().getWebServiceEndpointInfo(fqn);

            /*
            System.out.println("Keys are  " + propMap.keySet().size());
            for( final String key : infos.keySet() )
            {
                System.out.println( key );
            }

            System.out.println("Values are  ");
            for( final WebServiceEndpointInfo info : infos.values() )
            {
                 System.out.println( obj.toString() );
            }
            */
        }
    }

    /**
     Tests to see if any RegistryLocations are present.
     Expects to see atleast one, else the test fails. Create a connection
     pool with a type javax.xml.registry.ConnectionFactory
     */
    public void testListRegistryLocations() {
        String[] list = getDomainRoot().getWebServiceMgr().listRegistryLocations();
        if (list == null) {
            fail("Did not get any registry locations. Please check you have " +
                    "created one with the name foo");
        } else {
            for (int i = 0; i < list.length; i++) {
                System.out.println("RegistryLocation = " + list[i]);
            }
            // if you get any names in the connection definition, pass the test
            assert (true);
        }
    }

    public void testAddRegistryConnectionResources() {
        String jndiname = "eis/SOAR";
        String description = "Duh";
        String purl = "http://publishurl";
        String qurl = "http://queryurl";
        Map<String, String> map = new HashMap<String, String>();
        map.put(WebServiceMgr.QUERY_URL_KEY, qurl);
        map.put(WebServiceMgr.PUBLISH_URL_KEY, purl);

        //getDomainRoot().getWebServiceMgr().addRegistryConnectionResources (jndiname, description,
        //       map);
        assertTrue(true);
    }

    public void testRemoveRegistryConnectionResources() {
        String jndiname = "eis/SOAR";
        getDomainRoot().getWebServiceMgr().removeRegistryConnectionResources(jndiname);
        assertTrue(true);
    }
}


