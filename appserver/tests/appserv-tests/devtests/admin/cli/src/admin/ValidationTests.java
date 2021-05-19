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
 * This will test The Config-006 Config validation using Bean validation feature
 * @author Bhakti Mehta
 */
public class ValidationTests extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Tests config validation";
    }

    public static void main(String[] args) {
        ValidationTests tests = new ValidationTests();
        tests.runTests();
    }

    private void runTests() {
        startDomain();
        testClusterValidation();
        testNodeInstanceValidation();
        stopDomain();
        stat.printSummary();
    }

    private void testClusterValidation() {
        final String cname = "^%^%";
        final String goodcl = "validcl";
        final String junksysprops = "$$$$=bar";

        report("create-cluster-" + cname , !asadmin("create-cluster", cname));

        report("create-cluster-" + goodcl + "-junksysprop",!asadmin("create-cluser", "--systemproperties", junksysprops, goodcl));

    }

    private void testNodeInstanceValidation() {
        final String iname = "@#^%^%";
        final String goodins = "validins";
        final String junksysprops = "$$$$=bar";
        final String goodconfig="goodconfig";
        final String goodnode = "goodnode";

        report("create-instance-" + iname , !asadmin("create-instance", iname));

        report("create-local-instance-junksysprops", !asadmin("create-local-instance",
                        "--systemproperties", junksysprops, goodins));

        report("copy-config",!asadmin("copy-config",  "--systemproperties", "!@*^*^=bar", "default-config", goodconfig));

        report ("create-node-ssh", !asadmin ("create-node-ssh", "--nodehost", "*%*", "--installdir","/tmp/bar" ,goodnode ));


        report("create-sysprops",!asadmin( "create-system-properties", "A%S%S=bar"));

        report("issue11200-create-message-sec-provider", !asadmin( "create-message-security-provider", "--classname", "com.sun.foo", "--layer" ,"SOAP" ,"<script>alert(\"x\")</script>"));

        report("create-message-sec-provider-invalid-classname", ! asadmin( "create-message-security-provider", "--layer", "SOAP",
                "--classname", "com/sun", "ggg"));

        report("create-audit-module-invalid-classname",! asadmin( "create-audit-module",
                "--classname", "*fffs344:33",  "foo1"));

    }
}
