/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * @author David Zhao
 */
public class CreateJmsHostTest extends AdminBaseDevTest {
    private static String HOST1 = "jms-host1";
    private static String HOST2 = "jms-host2";

    public static void main(String[] args) {
        new CreateJmsHostTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for creating jms host";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("delete-jms-host", HOST1);
            asadmin("delete-jms-host", HOST2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
        startDomain();
        createJmsHostWithoutForce();
        createJmsHostWithForce();
        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void createJmsHostWithoutForce() {
        report("createJmsHostWithoutForce-0", asadmin("create-jms-host", "--target", "server", "--mqhost", "localhost", "--mqport", "7676", "--mquser", "guest", "--mqpassword", "guest", HOST1));
        checkHost("checkJmsHostWithoutForce-0", HOST1);
    }

    private void createJmsHostWithForce() {
        report("createJmsHostWithForce-0", asadmin("create-jms-host", "--target", "server", "--force", "--mqhost", "localhost", "--mqport", "7676", "--mquser", "guest", "--mqpassword", "guest", HOST1));
        checkHost("checkJmsHostWithForce-0", HOST1);
        report("createJmsHostWithForce-1", !asadmin("create-jms-host", "--target", "server", "--force=xyz", "--mqhost", "localhost", "--mqport", "7676", "--mquser", "guest", "--mqpassword", "guest", HOST2));
        report("createJmsHostWithForce-2", asadmin("create-jms-host", "--target", "server", "--force", "--mqhost", "localhost", "--mqport", "7676", "--mquser", "guest", "--mqpassword", "guest", HOST2));
        checkHost("checkJmsHostWithForce-2", HOST2);
    }

    private void checkHost(String testName, String expected) {
        AsadminReturn result = asadminWithOutput("list-jms-hosts", "--target", "server");
        report(testName, result.out.contains(expected));
    }
}
