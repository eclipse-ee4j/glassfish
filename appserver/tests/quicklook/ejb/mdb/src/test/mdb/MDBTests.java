/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.mdb;

import java.io.File;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import test.admincli.util.*;

@Test(sequential = true)
public class MDBTests {
    private boolean execReturn = false;
    private String APPCLIENT = System.getProperty("APPCLIENT");
    private String ASADMIN = System.getProperty("ASADMIN");
    private String cwd = System.getProperty("BASEDIR") == null ? System.getProperty("user.dir") : System.getProperty("BASEDIR");
    private String cmd;
    private String mdbApp= "ejb-ejb30-hello-mdbApp";

    @Parameters({ "BATCH_FILE1" })
    @Test
    public void createJMSRscTest(String batchFile1) throws Exception {
        cmd = ASADMIN + " multimode --file " + cwd + File.separator + batchFile1;
        execReturn = RtExec.execute(cmd);
        Assert.assertEquals(execReturn, true, "Create JMS resource failed ...");
    }

    @Parameters({ "MDB_APP_DIR" })
    @Test(dependsOnMethods = { "createJMSRscTest" })
    public void deployJMSAppTest(String mdbAppDir) throws Exception {
        cmd = ASADMIN + " deploy  --retrieve=" + cwd + File.separator + mdbAppDir
                + " " + cwd + File.separator + mdbAppDir + mdbApp + ".ear ";
        execReturn = RtExec.execute(cmd);
        Assert.assertEquals(execReturn, true, "Deploy the mdb app failed ... ");
    }

    @Parameters({ "MDB_APP_DIR" })
    @Test(dependsOnMethods = { "deployJMSAppTest" })
    public void runJMSAppTest(String mdbAppDir) throws Exception {
        cmd = APPCLIENT+" -client "+ cwd + File.separator +mdbAppDir+mdbApp+"Client.jar ";
//           + "-name ejb-ejb30-hello-mdbClient " ;
        execReturn = RtExec.execute(cmd);
        Assert.assertEquals(execReturn, true, "Run appclient against JMS APP failed ...");
    }

    @Test (dependsOnMethods = { "runJMSAppTest" })
    public void undeployJMSAppTest() throws Exception {
        cmd = ASADMIN + " undeploy " + mdbApp;
        execReturn = RtExec.execute(cmd);
        Assert.assertEquals(execReturn, true, "UnDeploy the mdb app failed ... ");
    }

    @Parameters({ "BATCH_FILE2" })
    @Test (dependsOnMethods = { "undeployJMSAppTest" })
    public void deleteJMSRscTest(String batchFile2) throws Exception {
        cmd = ASADMIN + " multimode --file " + cwd + File.separator + batchFile2;
        execReturn = RtExec.execute(cmd);
        Assert.assertEquals(execReturn, true, "Delete JMD Resource failed ...");
    }

}
