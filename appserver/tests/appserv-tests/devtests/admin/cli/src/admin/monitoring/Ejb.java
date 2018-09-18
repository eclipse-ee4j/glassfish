/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin.monitoring;

import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import java.io.*;
import static admin.monitoring.Constants.*;

/**
 * Tests ejb monitoring.  Note that this requires a running JavaDB database.
 * @author Byron Nevins
 */
public class Ejb extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from EJB Monitoring Tests!");
        someTests();
        moreTests();
        testMyEjb();
    }

    void someTests() {
        report(!wget(28080, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL");
        report(!wget(28081, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL");
        deploy(CLUSTER_NAME, blackBoxRar);
        createConnectionPool();
        createConnectionResource();
        deploy(CLUSTER_NAME, conApp1);

        report(wget(28080, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL on 28080-");
        report(wget(28081, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL on 28081-");

        verifyList(CLUSTERED_INSTANCE_NAME1 + ".resources.MConnectorPool.numconnused-name", "NumConnUsed");
        verifyList(CLUSTERED_INSTANCE_NAME2 + ".resources.MConnectorPool.numconnused-name", "NumConnUsed");
    }

    void moreTests() {
        final String uri = "ejbsfapp1/SFApp1Servlet1?sleepTime=12&attribute=cachemisses";
        final String getmArg = ".applications.ejbsfapp1.ejbsfapp1ejbmod1\\.jar.SFApp1EJB1.bean-cache.*";
        final String getmKey = ".applications.ejbsfapp1.ejbsfapp1ejbmod1\\.jar.SFApp1EJB1.bean-cache.numpassivations-count";

        report(!wget(28080, uri), "hit ejbsfapp1URL on 28080-");
        report(!wget(28081, uri), "hit ejbsfapp1URL on 28081-");
        deploy(CLUSTER_NAME, ejbsfapp1);

        report(wget(28080, uri), "hit ejbsfapp1URL on 28080-");
        report(wget(28081, uri), "hit ejbsfapp1URL on 28081-");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME1 + getmArg, getmKey), "ejbbeantest-in1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2 + getmArg, getmKey), "ejbbeantest-in2");
        //report(verifyGetm(getmKey), "ejbbeantest-getm-ok");
    }

    private void testMyEjb() {
        final String uri = "MyEjb-war/MyEjbServlet";
        final String arg = ".applications.MyEjb.MyEjb-ejb\\.jar.MySessionBean.bean-methods.getMessage.methodstatistic-count";
        final String getmArgInstance1 = CLUSTERED_INSTANCE_NAME1 + arg;
        final String getmArgInstance2 = CLUSTERED_INSTANCE_NAME2 + arg;

        deploy(CLUSTER_NAME, myejbear);

        // We looking for get -m to return something like this:
        // clustered-i1.applications.MyEjb.MyEjb-ejb\.jar.MySessionBean.bean-methods.getMessage.methodstatistic-count = 8
        for (int i = 0; i < 10; i++) {
            verifyGetm(getmArgInstance1, getmArgInstance1 + " = " + i);
            verifyGetm(getmArgInstance2, getmArgInstance2 + " = " + i);
            report(wget(28080, uri), "hit MyEjbServlet on 28080-");
            report(wget(28081, uri), "hit MyEjbServlet on 28081-");
        }
    }

    private boolean verifyGetm(String arg, String key) {
        AsadminReturn ret = asadminWithOutput("get", "-m", arg);
        return matchString(key, ret.outAndErr);
    }

    private void createConnectionPool() {
        report(asadmin("create-connector-connection-pool",
                "--raname", "blackbox-tx",
                "--connectiondefinition", "javax.sql.DataSource",
                "--property", "DatabaseName=sun-appserv-samples:PortNumber=1527:serverName=localhost:connectionAttributes=;create\\=true:password=APP:user=APP",
                "MConnectorPool"),
                "createMConnectorPool");
    }

    private void createConnectionResource() {
        report(asadmin("create-connector-resource", "--poolname", "MConnectorPool", "--target", CLUSTER_NAME, "eis/ConnectorMonitoring"),
                "createConnectorResource");
    }

    private void verifyList(String name, String desiredValue) {
        AsadminReturn ret = asadminWithOutput("list", "-m", name);
        report(matchString(desiredValue, ret.outAndErr), "verify-list");
    }
    private static final File blackBoxRar = new File(RESOURCES_DIR, "blackbox-tx.rar");
    private static final File conApp1 = new File(RESOURCES_DIR, "conapp1.ear");
    private static final File ejbsfapp1 = new File(RESOURCES_DIR, "ejbsfapp1.ear");
    private static final File myejbear = new File(BUILT_RESOURCES_DIR, "MyEjb/dist/MyEjb.ear");
}
/**
 * NOTES
 *
 * asadmin get -m server.applications.MyEjb.MyEjb-ejb\.jar.MySessionBean.bean-methods.getMessage.methodstatistic-count
 */
