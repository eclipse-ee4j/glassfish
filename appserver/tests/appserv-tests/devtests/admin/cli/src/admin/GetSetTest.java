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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This will test miscellaneous commands that don't have a dedicated test file
 * @author Tom Mueller
 */
public class GetSetTest extends AdminBaseDevTest {
    final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    @Override
    protected String getTestDescription() {
        return "Get/Set Commands Test";
    }

    public static void main(String[] args) {
        new GetSetTest().runTests();
    }

    private String encodeStar(String s) {
        return isWindows ? "\"" + s + "\"" : s;
    }

    private void runTests() {
        setup();
        testGetSetAll();
        testGetSetAlias();
        cleanup();
        stat.printSummary();
    }

    private void setup() {
        startDomain();
        report("create-cluster", asadmin("create-cluster", "gs1"));
    }

    @Override
    public void cleanup() {
        report("delete-cluster", asadmin("delete-cluster", "gs1"));
        stopDomain();
    }

    /*
     * Use "get *" to get all settable values, and then use set to set the attribute
     * to the current value.
     */
    private void testGetSetAll() {
        final String t = "getsetall-";

        AsadminReturn rv = asadminWithOutput("get", encodeStar("*"));
        report(t + "get", rv.returnValue);
        String[] lines = rv.out.split("[\r\n]");
        if (!rv.returnValue) return;

        filterJunk(lines);
        List<String> setlines = new ArrayList(lines.length);
        for (String line : lines) {
            line = filterSet(line);
            if (line == null) continue;
            setlines.add(line);
        }
        testSets(t, setlines);
    }

    /*
     * Use "get *" to get all settable alias values, and then use set to set the attribute
     * to the current value using the alias for the value.
     */
    private void testGetSetAlias() {
        final String t = "getsetalias-";
        final String[] aliases = {
            "configs.config",
            "servers.server",
            "clusters.cluster",
        };
        List<String> setlines = new ArrayList();


        for (String a : aliases) {
            AsadminReturn rv = asadminWithOutput("get", encodeStar(a + ".*"));
            report(t + "get", rv.returnValue);
            String[] lines = rv.out.split("[\r\n]");
            if (!rv.returnValue) continue;

            filterJunk(lines);

            for (String line : lines) {
                line = filterSet(line);
                if (line == null) continue;
                line = line.replace(a + ".", "");

                if (line.contains("server.name") ||
                    line.contains("gs1.name")) continue;
                setlines.add(line);
            }
        }
        testSets(t, setlines);
    }

    /*
     * Filter out set requests that are known not work.
     * Encode the request if necessary.
     */
    private String filterSet(String line) {
        // set doesn't work on log levels or the log service
        if (line.contains("module-log-levels")) return null;
        if (line.contains("log-service")) return null;
        // cannot set the primary key
        if (line.contains("jndi-name")) return null;
        if (line.contains("jdbc-connection-pool") && line.contains("name")) return null;
        if (line.contains("connector-connection-pool") && line.contains("name")) return null;
        // cannot set the jvm options
        if (line.contains("jvm-options")) return null;
        // bug ????? - there currently is no set method for this, remove once the bug is fixed
        if (line.contains("ssl.ssl-inactivity-timeout")) return null;
        // cannot set a list
        if (line.contains("secure-admin-principal")) return null;
        // cannot set applications
        if (line.contains("applications.application")) return null;
        // new security config has keys - cannot set them
        if (line.contains("security-configurations")) return null;

        // escape the "." in some property names
        final String[] dottedprops = {
            "encryption.key.alias",
            "signature.key.alias",
            "dynamic.username.password",
            "security.config",
            "administrative.domain.name",
        };

        for (String dp : dottedprops) {
            if (line.contains(dp)) {
                 line = line.replace(dp, dp.replace(".", "\\."));
                 break;
            }
        }
        return line;

}
    /*
     * Test a list of set requests by running asadmin set repeatedly.
     */
    private void testSets(String tname, List<String> reqs) {
        String nvs[] = new String[20];
        int nvi = 1;
        int cnt = 0;
        nvs[0] = "set";
        for (String line : reqs) {
            if (line.trim().length() == 0)
                continue;
            nvs[nvi++ % nvs.length] = line;
            if (nvi % nvs.length == 0) {
                boolean ret = asadmin(nvs);
                if (ret) {
                    report(tname + "set-" + cnt, ret);
                }
                else {
                    // set each one individually to see which one failed.
                    for (int i = 1; i < nvs.length; i++) {
                        report(tname + "set-" + cnt + "-" + i, asadmin("set", nvs[i]));
                    }
                }
                nvi = 1;
                ++cnt;
            }
        }
        // run the last few by themselves
        for (int i = 1; i < nvi; i++) {
            report(tname + "set-end" + "-" + i, asadmin("set", nvs[i]));
        }
    }
    private void filterJunk(String[] lines) {
        // set junk to empty strings
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("configs"))
                return;

            lines[i] = "";
        }
    }
}
