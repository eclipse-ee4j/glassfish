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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=11979
 *  ("Filter.init() called twice")
 */
public class WebTest {

    private static final String TEST_NAME =
        "filter-multiple-init";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for IT 11979");
        final WebTest webTest = new WebTest(args);

        try {
            ExecutorService exService = Executors.newFixedThreadPool(3);
            List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
            tasks.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return webTest.doTest("index.jsp");
                }
            });
            tasks.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return webTest.doTest("index2.jsp");
                }
            });

            List<Future<Boolean>> futures = exService.invokeAll(tasks, 6, TimeUnit.SECONDS);
            boolean status = futures.get(0).get() && futures.get(1).get();

            stat.addStatus(TEST_NAME, ((status) ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public boolean doTest(String page) throws Exception {
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/" + page);

        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        System.out.println(page + ": " + responseCode);
        return (responseCode == 200);
    }
}
