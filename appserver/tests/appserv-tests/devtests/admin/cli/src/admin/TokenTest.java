/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin;

/**
 *
 * @author Jennifer Chou
 */
public class TokenTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Tests token support.";
    }

    public static void main(String[] args) {
        new TokenTest().runTests();
    }

    private void runTests() {
        startDomain();
        testDAS();
        //testCluster();
        stopDomain();
        stat.printSummary();
    }

    private void testDAS() {
        report("create-system-properties-domain", asadmin("create-system-properties", "--target", "domain", "jenport=1010"));
        report("create-network-listener", asadmin("create-network-listener", "--listenerport", "${jenport}", "--protocol", "http-listener-1", "jenlistener"));
        report("create-virtual-server", asadmin("create-virtual-server", "--hosts", "localhost", "--networklisteners", "jenlistener", "jenvs"));
        AsadminReturn ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        boolean success = ret.outAndErr.indexOf("1010") >= 0;
        report("port-set-create-domain-sysprop", success);

        // Commented out until 12318, 12330 is fixed
        //report("create-system-properties-config-ISSUE-12330", asadmin("create-system-properties", "--target", "server-config", "jenport=2020"));
        //ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        //success = ret.outAndErr.indexOf("2020") >= 0;
        //report("port-change-create-config-sysprop-ISSUE-12318", success);

        report("create-system-properties-server", asadmin("create-system-properties", "jenport=3030"));
        ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        success = ret.outAndErr.indexOf("3030") >= 0;
        report("port-change-create-server-sysprop", success);

        report("delete-system-property-server", asadmin("delete-system-property", "jenport"));
        ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        success = ret.outAndErr.indexOf("1010") >= 0; //Change back to 2020 when 12330 is fixed
        report("port-change-delete-server-sysprop", success);

        // Commented out until 12318, 12330 is fixed
        //report("delete-system-property-config-ISSUE-12330", asadmin("delete-system-property","--target", "server-config", "jenport"));
        //ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "jenvs");
        //success = ret.outAndErr.indexOf("1010") >= 0;
        //report("port-change-delete-config-sysprop-ISSUE-12318", success);

        report("delete-virtual-server", asadmin("delete-virtual-server", "jenvs"));
        report("delete-network-listener", asadmin("delete-network-listener", "jenlistener"));
        report("delete-system-property-domain", asadmin("delete-system-property","--target", "domain", "jenport"));
    }

    private void testCluster() {
        //uncomment when issue 12312 is fixed
        //report("create-system-properties-domain", asadmin("create-system-properties", "--target", "domain", "HTTP_LISTENER_PORT=1010"));
        report("create-cluster-with-syspropport", asadmin("create-cluster", "--systemproperties", "HTTP_LISTENER_PORT=3030", "cluster1"));
        report("create-instance-with-syspropport", asadmin("create-local-instance", "--cluster", "cluster1", "--systemproperties", "HTTP_LISTENER_PORT=4040", "instance1"));

        report("start-local-instance-syspropport", asadmin("start-local-instance", "instance1"));
        String str = getURL("http://localhost:4040");
        boolean success = !str.isEmpty();
        report("check-url-at-server-port", success);

        report("stop-local-instance-syspropport", asadmin("stop-local-instance", "instance1"));
        report("delete-local-instance-syspropport", asadmin("delete-local-instance", "instance1"));
        report("delete-cluster-syspropport", asadmin("delete-cluster", "cluster1"));
    }

}
