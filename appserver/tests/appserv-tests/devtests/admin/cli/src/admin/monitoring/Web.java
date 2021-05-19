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
 * Tests web monitoring.
 * @author Carla Mott
 */
public class Web extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from Web Monitoring Tests!");
        basicTests();
        jspTests();
        testMyWeb();
    }

    void basicTests() {
        report(!wget(28080, "HelloWeb/"), "hit HelloWebURL on 28080 before deploy");
        report(!wget(28081, "HelloWeb/"), "hit HelloWebURL on 28081 before deploy");
        deploy(CLUSTER_NAME, hellowar);

        //next commands increment the session count
        report(wget(28080, "HelloWeb/"), "hit HelloWebURL on 28080 after deploy");
        report(wget(28081, "HelloWeb/"), "hit HelloWebURL on 28081 after deploy");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME1+".applications.HelloWeb.server.totalservletsloadedcount-count", "4" ), "totalservletsloadedcount test-1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2+".applications.HelloWeb.server.totalservletsloadedcount-count", "4" ), "totalservletsloadedcount test-1");

    }

    void jspTests() {
        final String uri = "HelloWeb";
        final String uriRepsonse = "HelloWeb/response.jsp";
        final String getmArg = ".applications.HelloWeb.server.totaljspcount-count";
        final String getmKey = "1";

        report(wget(28080, uri), "jspload on 28080-");  // commands increment the session count
        report(wget(28081, uri), "jspload on 28081-");

        report(verifyGetm(CLUSTERED_INSTANCE_NAME1+getmArg, getmKey), "jsploadtest-1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2+getmArg, getmKey), "jsploadtest-2");

        report(wget(28080, uriRepsonse), "jspload on 28080-");  // commands increment the session count
        report(wget(28081, uriRepsonse), "jspload on 28081-");

        report(verifyGetm(CLUSTERED_INSTANCE_NAME1+getmArg, "2"), "jsploadtest-1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2+getmArg, "2"), "jsploadtest-2");
    }

    private void testMyWeb() {
        final String uri = "HelloWeb/HelloWorld";
        final String uri2 = "HelloWeb/MyServlet";
        final String sessionCount =".applications.HelloWeb.server.sessionstotal-count";
        final String activatedCount =".web.session.activatedsessionstotal-count";


        // Count is 3 by now because of previous tests in this suite
            report(wget(28080, uri), "hit HelloWorld  on 28080-");
            report(wget(28081, uri), "hit HelloWorld on 28081-");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "4" ), "HelloWorld get session count-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "4" ), "HelloWorld get session count-test-2");


            report(wget(28080, uri), "hit HelloWorld - again");
            report(wget(28081, uri), "hit HelloWorld - again");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "5" ), "second HelloWorld get session count-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "5" ), "second HelloWorld get session count-test-test-2");

            report(wget(28080, uri2), "hit MyServlet on 28080-");
            report(wget(28081, uri2), "hit MyServlet on 28081-");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "6" ), "MyServlet get session-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "6" ), "MyServlet get session-test-2");

            report(wget(28080, uri2), "hit MyServlet on 28080- again");
            report(wget(28081, uri2), "hit MyServlet on 28081- again");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "7" ), "second MyServlet get session-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "7" ), "second MyServlet get session-test-2");
    }

    private boolean verifyGetm(String arg, String key) {
        AsadminReturn ret = asadminWithOutput("get", "-m", arg);
        return matchString(key, ret.outAndErr);
    }

    private static final File hellowar = new File(RESOURCES_DIR, "HelloWeb.war");
}
