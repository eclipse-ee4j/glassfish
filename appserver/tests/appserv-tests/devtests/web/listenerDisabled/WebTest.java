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

import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6190900 ("httplistener created with --enabled=false doesn't
 * disable the listener").
 *
 * See also https://glassfish.dev.java.net/issues/show_bug.cgi?id=1301
 * ("Change semantics of <http-listener> "enabled" attribute to truely
 * reflect an HTTP listener's state").
 */
public class WebTest{

    private static final String TEST_NAME = "listener-disabled";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) throws Exception {

        String host = args[0];
        String port = "8079";
        String contextRoot = args[2];

        stat.addDescription("Ensure disabled HTTP listener gives ConnectException");

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/ServletTest");
        System.out.println("Invoking url: " + url.toString());

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.getResponseCode();
            System.err.println("Expected java.net.ConnectException: "
                               + "Connection refused");
            stat.addStatus(TEST_NAME, stat.FAIL);
        } catch (ConnectException e) {
            stat.addStatus(TEST_NAME, stat.PASS);
        }

        stat.printSummary();
    }
}
