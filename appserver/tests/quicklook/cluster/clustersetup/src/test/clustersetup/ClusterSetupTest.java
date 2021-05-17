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

package test.clustersetup;

import com.sun.appserv.test.AdminBaseDevTest;
import java.io.File;
import org.testng.annotations.Test;
import org.testng.Assert;

public class ClusterSetupTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "QL cluster test helloworld";
    }

        final String tn = "QLCluster";
        final String port1 = "18080";
        final String port2 = "28080";
        final String cname = "eec1";
        final String i1name = "eein1-with-a-very-very-very-long-name";
        final String i2name = "eein2";
        String i1url = "http://localhost:"+port1;
        String i2url = "http://localhost:"+port2;

        public boolean retStatus = false, ret1 = false,ret2 = false;

    @Test
    public void createClusterTest() throws Exception{
        // create a cluster and two instances
        retStatus = report(tn + "create-cluster", asadmin("create-cluster", cname));
        Assert.assertEquals(retStatus, true, "Create Cluster failed ...");
    }

    @Test(dependsOnMethods = { "createClusterTest" })
    public void createInstanceTest() throws Exception{
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:" +
                "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:" +
                "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));
        retStatus = report(tn + "create-local-instance2", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:" +
                "IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:" +
                "JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848", i2name));
        Assert.assertEquals(retStatus, true, "Create instance failed ...");
    }

    @Test(dependsOnMethods = { "createInstanceTest" })
    public void startInstanceTest() throws Exception{
        // start the instances
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i2name));
        System.out.println("Waiting for 5 sec...");
    Thread.currentThread().sleep(5000);
        // check that the instances are there
        report(tn + "list-instances", asadmin("list-instances"));
        report(tn + "getindex1", matchString("GlassFish Server", getURL(i1url)));
        retStatus = report(tn + "getindex2", matchString("GlassFish Server", getURL(i2url)));
        Assert.assertEquals(retStatus, true, "Start instance failed ...");
    }
 }
