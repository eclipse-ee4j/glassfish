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

package test.admincli;

import org.testng.annotations.Test;
import org.testng.Assert;
import test.admincli.util.*;

//@Test(sequential = true)
public class ExistStatusTests {
    private boolean execReturn = false;
    private String ASADMIN = System.getProperty("ASADMIN");
    private String cmd, cmd1;
    @Test
    public void createJDBCPool() throws Exception {
//        System.out.println(ASADMIN);
        cmd = ASADMIN + " create-jdbc-connection-pool " +
            "--datasourceclassname=org.apache.derby.jdbc.ClientDataSource --property " +
            "DatabaseName=sun-appserv-samples:PortNumber=1527:serverName=localhost:Password=APP:User=APP QLJdbcPool";

        execReturn = RtExec.execute(cmd);
        Assert.assertEquals(execReturn, true, "Create jdbc connection pool failed ...");
    }

    @Test(dependsOnMethods = { "createJDBCPool" })
    public void pingJDBCPool() throws Exception {
//      extra ping of DerbyPool to create sun-appserv-samples DB.
        cmd = ASADMIN + " ping-connection-pool DerbyPool";
        RtExec.execute(cmd);
        cmd1 = ASADMIN + " ping-connection-pool QLJdbcPool";
        execReturn = RtExec.execute(cmd1);
        Assert.assertEquals(execReturn, true, "Ping jdbc connection pool failed ...");
    }

    @Test(dependsOnMethods = { "pingJDBCPool" })
    public void deleteJDBCPool() throws Exception {
        cmd = ASADMIN + " delete-jdbc-connection-pool QLJdbcPool";
        execReturn = RtExec.execute(cmd);
        Assert.assertEquals(execReturn, true, "Delete jdbc connection pool failed ...");
    }
}
