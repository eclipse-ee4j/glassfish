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

package listjndi;

import admin.AdminBaseDevTest;

/*
 * Dev test for list-jndi-entries
 * @author Cheng Fang
 */
public class ListJndiEntriesTest extends AdminBaseDevTest {

    public static final String[] EXPECTED_TOKENS =
    {"UserTransaction:", "java:global:", "ejb:", "com.sun.enterprise.naming.impl.TransientContext"};

    public static final String INSTANCE_RESOURCE_NAME = "INSTANCE_RESOURCE_NAME";
    public static final String CLUSTER_RESOURCE_NAME = "CLUSTER_RESOURCE_NAME";
    public static final String CLUSTER_NAME = "cluster1";
    public static final String INSTANCE1_NAME = "instance1";
    public static final String INSTANCE2_NAME = "instance2";
    public static final String STANDALONE_INSTANCE_NAME = "instance3";

    public static void main(String[] args) {
        new ListJndiEntriesTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for list-jndi-entries";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("stop-local-instance", STANDALONE_INSTANCE_NAME);
            asadmin("delete-local-instance", STANDALONE_INSTANCE_NAME);
            asadmin("stop-local-instance", INSTANCE1_NAME);
            asadmin("stop-local-instance", INSTANCE2_NAME);
            asadmin("stop-cluster", CLUSTER_NAME);
            asadmin("delete-local-instance", INSTANCE1_NAME);
            asadmin("delete-local-instance", INSTANCE2_NAME);
            asadmin("delete-cluster", CLUSTER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
     //   asadmin("start-domain");
        asadmin("create-cluster", CLUSTER_NAME);

        asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);

        asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);

        asadmin("create-local-instance", STANDALONE_INSTANCE_NAME);

        asadmin("start-cluster", CLUSTER_NAME);
        asadmin("start-local-instance", STANDALONE_INSTANCE_NAME);
        //TODO create a resource in STANDALONE_INSTANCE_NAME only
        //TODO create a resource in CLUSTER_NAME only

        testListJndiEntries();
        testListJndiEntriesTargetServer();
        testListJndiEntriesTargetDomain();
        testListJndiEntriesTargetCluster();
        testListJndiEntriesTargetInstance1();
        testListJndiEntriesTargetInstance2();
        testListJndiEntriesTargetStandaloneInstance();

        cleanup();
      //  asadmin("stop-domain");
        stat.printSummary();
    }

    public void testListJndiEntries() {
        String testName = "testListJndiEntries";
        AsadminReturn result = asadminWithOutput("list-jndi-entries");
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }

    public void testListJndiEntriesTargetServer() {
        String testName = "testListJndiEntriesTargetServer";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", "server");
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }

    public void testListJndiEntriesTargetDomain() {
        String testName = "testListJndiEntriesTargetDomain";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", "domain");
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
    }

    public void testListJndiEntriesTargetCluster() {
        String testName = "testListJndiEntriesTargetCluster";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    public void testListJndiEntriesTargetInstance1() {
        String testName = "testListJndiEntriesTargetInstance1";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", INSTANCE1_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, INSTANCE1_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE2_NAME);
    }

    public void testListJndiEntriesTargetInstance2() {
        String testName = "testListJndiEntriesTargetInstance2";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", INSTANCE2_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE1_NAME);
    }

    public void testListJndiEntriesTargetStandaloneInstance() {
        String testName = "testListJndiEntriesTargetStandaloneInstance";
        AsadminReturn result = asadminWithOutput("list-jndi-entries", STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
        reportUnexpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME, CLUSTER_NAME);
    }

    private void reportResultStatus(String testName, AsadminReturn result) {
        report(testName + "-returnValue", result.returnValue);
        report(testName + "-isEmpty", result.err.isEmpty());
    }

    private void reportExpectedResult(String testName, AsadminReturn result, String... expected) {
        if (expected.length == 0) {
            expected = EXPECTED_TOKENS;
        }
        for (String token : expected) {
            report(testName + "-expected", result.out.contains(token));
        }
    }

    private void reportUnexpectedResult(String testName, AsadminReturn result, String... unexpected) {
        for (String token : unexpected) {
            report(testName + "-unexpected", !result.out.contains(token));
        }
    }
}
