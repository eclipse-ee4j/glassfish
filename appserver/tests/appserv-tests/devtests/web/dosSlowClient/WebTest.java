/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.appserv.test.util.results.SimpleReporterAdapter;
import java.util.concurrent.atomic.AtomicInteger;

public class WebTest extends Thread{
        private static String TEST_NAME = "dos-slow-client";

        public static final AtomicInteger count = new AtomicInteger();

        static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);

        public static void main(String args[]) throws InterruptedException {

                // The stat reporter writes out the test info and results
                // into the top-level quicklook directory during a run.

                stat.addDescription("Slow client bytes write");

                String host = args[0];
                int port = Integer.parseInt(args[1]);
                int num = Integer.parseInt(args[2]);

                for (int i=0; i < num; i++) {
                        new SlowClient(host, port, new WebTest());
                }
                Thread.sleep(30000);

                stat.addStatus(TEST_NAME, count.get() == num ? stat.PASS : stat.FAIL);
                stat.printSummary(TEST_NAME);
        }

}
