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

package test.hellocluster;

import com.sun.appserv.test.AdminBaseDevTest;
import java.io.File;
import org.testng.annotations.Test;
import org.testng.Assert;

public class ClusterHelloworldTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "QL cluster test helloworld";
    }


        final String tn = "QLCluster";
        final String port1 = "18080";
        final String port2 = "28080";
        final String cname = "eec1";
        String i1url = "http://localhost:"+port1;
        String i2url = "http://localhost:"+port2;
        static String BASEDIR = System.getProperty("BASEDIR");
        
        public boolean retStatus = false, ret1 = false,ret2 = false;


    @Test
    public void clusterDeployTest() throws Exception{
        // deploy an web application to the cluster
        File webapp = new File(BASEDIR+"/dist/hellocluster", "helloworld.war");
        retStatus = report(tn + "deploy", asadmin("deploy", "--target", cname, webapp.getAbsolutePath()));
        Assert.assertEquals(retStatus, true, "Cluster deployment failed ...");
    }

    @Test(dependsOnMethods = { "clusterDeployTest" })
    public void clusterHelloWorldTest() throws Exception{
        System.out.println("Wait extra 5 sec for GF to generate helloworld app.");
	Thread.currentThread().sleep(5000);
        report(tn + "getapp1", matchString("Hello", getURL(i1url + "/helloworld/hi.jsp")));
        String s1 = getURL(i2url + "/helloworld/hi.jsp");
//        System.out.println("output from instance 2:" + s1);
        retStatus = report(tn + "getapp2", matchString("Hello", s1));
        Assert.assertEquals(retStatus, true, "Accessing helloworld page failed ...");
    }

    @Test(dependsOnMethods = { "clusterHelloWorldTest" })
    public void clusterUnDeployTest() throws Exception{
        retStatus = report(tn + "undeploy", asadmin("undeploy", "--target", cname, "helloworld"));
        Assert.assertEquals(retStatus, true, "Cluster undeployment failed ...");
    }
}
