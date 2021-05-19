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

import java.io.IOException;
import java.net.URL;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public class WebTest {
        private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "thread-pool-idle-timeout");

        public static void main(String args[]) {
                stat.addDescription("HTTP thread timeout");

                String host = args[0];
                String portS = args[1];
                String contextRoot = args[2];
                boolean noTimeout = args.length == 4;

                int port = new Integer(portS);

                goGet(host, port, contextRoot + "/ServletTest", noTimeout );

                stat.printSummary("web-thread-timeout");
        }

        private static void goGet(String host, int port, String contextPath, boolean noTimeout) {
                try {
                        URL url = new URL("http://" + host + ":" + port + contextPath);
                        url.getContent();
                        stat.addStatus("web-thread-timeout" + (noTimeout ? "-debug" : ""), noTimeout ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
                } catch( IOException ex){
                        stat.addStatus("web-thread-timeout", noTimeout ? SimpleReporterAdapter.FAIL : SimpleReporterAdapter.PASS);
                } catch( Exception ex){
                        stat.addStatus("web-thread-timeout", SimpleReporterAdapter.FAIL);
                        ex.printStackTrace();
                }
        }
}
