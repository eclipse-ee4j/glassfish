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

package test.clusterteardown;

import com.sun.appserv.test.AdminBaseDevTest;
import org.testng.annotations.Test;
import org.testng.Assert;

public class ClusterTeardownTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "QL Cluster TEARDOWN Test";
    }
        final String tn = "QLCluster";
        final String cname = "eec1";
        final String i1name = "eein1-with-a-very-very-very-long-name";
        final String i2name = "eein2";
        
        public boolean retStatus;

        // Byron Nevins Nov 4,2010 -- Add plenty of output if there are problems.
        // previously deleteInstanceTest would never say boo no matter what happened...
    @Test
    public void deleteInstanceTest() throws Exception{
        AsadminReturn ar1 = asadminWithOutput("stop-local-instance", "--kill", i1name);
        AsadminReturn ar2 = asadminWithOutput("stop-local-instance", "--kill", i2name);
        AsadminReturn ar3 = asadminWithOutput("delete-local-instance", i1name);
        AsadminReturn ar4 = asadminWithOutput("delete-local-instance", i2name);

        report(tn + "stop-local-instance1", ar1.returnValue);
        report(tn + "stop-local-instance2", ar2.returnValue);
        report(tn + "delete-local-instance1", ar3.returnValue);
        report(tn + "delete-local-instance2", ar4.returnValue);

        Assert.assertTrue(ar1.returnValue, "Error stopping instance " + i1name + ": " + ar1.outAndErr);
        Assert.assertTrue(ar2.returnValue, "Error stopping instance " + i2name + ": " + ar2.outAndErr);
        Assert.assertTrue(ar3.returnValue, "Error deleting instance " + i1name + ": " + ar3.outAndErr);
        Assert.assertTrue(ar4.returnValue, "Error deleting instance " + i2name + ": " + ar4.outAndErr);
    }
        
    @Test(dependsOnMethods = { "deleteInstanceTest" })
    public void deleteClusterTest() throws Exception{
        AsadminReturn ar1 = asadminWithOutput("delete-cluster", cname);
        retStatus = report(tn + "delete-cluster", ar1.returnValue);

        Assert.assertTrue(retStatus, "Error deleting cluster " + cname + ": " + ar1.outAndErr);
    }
}
