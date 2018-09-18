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
public class CreateJmsDestTest extends AdminBaseDevTest {
    private static String DEST1 = "jmsdest1";
    private static String DEST2 = "jmsdest2";
    private static String DEST3 = "jmsdest3";
    private static String DEST4 = "jmsdest4";
    private static String QUEUE = "queue";
    private static String TOPIC = "topic";

    public static void main(String[] args) {
        new CreateJmsDestTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for creating jms physical destination";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("delete-jmsdest", "--desttype", "queue", DEST1);
            asadmin("delete-jmsdest", "--desttype", "topic", DEST2);
            asadmin("delete-jmsdest", "--desttype", "queue", DEST3);
            asadmin("delete-jmsdest", "--desttype", "topic", DEST4);
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
        report("createJmsHostWithoutForce-0", asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, DEST1));
        checkResource("checkJmsHostWithoutForce-0", QUEUE, DEST1);
        report("createJmsHostWithoutForce-1", asadmin("create-jmsdest", "--target", "server", "--desttype", TOPIC, DEST2));
        checkResource("checkJmsHostWithoutForce-1", TOPIC, DEST2);
    }

    private void createJmsHostWithForce() {
        report("createJmsHostWithForce-0", asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, "--force", DEST1));
        checkResource("checkJmsHostWithForce-0", QUEUE, DEST1);
        report("createJmsHostWithForce-1", asadmin("create-jmsdest", "--target", "server", "--desttype", TOPIC, "--force", DEST2));
        checkResource("checkJmsHostWithForce-1", TOPIC, DEST2);
        report("createJmsHostWithForce-2", !asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, "--force=xyz", DEST3));
        report("createJmsHostWithForce-3", asadmin("create-jmsdest", "--target", "server", "--desttype", QUEUE, "--force", DEST3));
        checkResource("checkJmsHostWithForce-3", QUEUE, DEST3);
        report("createJmsHostWithForce-4", asadmin("create-jmsdest", "--target", "server", "--desttype", TOPIC, "--force", DEST4));
        checkResource("checkJmsHostWithForce-4", TOPIC, DEST4);
    }

    private void checkResource(String testName, String destType, String expected) {
        AsadminReturn result = asadminWithOutput("list-jmsdest", "--destType", destType);
        report(testName, result.out.contains(expected));
    }
}
