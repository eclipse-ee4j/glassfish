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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for large cookie error.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "large-cookie-error";

    private static final String EXPECTED_RESPONSE = "SUCCESS";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6456553");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        try {
            invokeWithLargeCookie();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        return;
    }

    private void invokeWithLargeCookie() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/largecookie.html");
        System.out.println(url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            buf.append('a');
        }
        String largeValue = buf.toString();
        StringBuilder cookieBuf = new StringBuilder();
        for (int j = 0; j < 9; j++) {
            if (j > 0) {
                cookieBuf.append(";");
            }
            cookieBuf.append("key" + j + "=" + largeValue);
        }

        int oneMinute = 60 * 1000;
        Date now = new Date();
        Date later = new Date(now.getTime() + 60 * oneMinute);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        TimeZone timeZone = dateFormat.getTimeZone();
        int min = timeZone.getOffset(later.getTime()) / oneMinute;
        int hour = min / 60;
        timeZone = TimeZone.getTimeZone("GMT" + (hour >= 0 ? "+" : "") + hour + ":" + min);
        dateFormat.setTimeZone(timeZone);

        cookieBuf.append("; expires " + dateFormat.format(later));
        conn.setRequestProperty("Cookie", cookieBuf.toString());
        conn.connect();
        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);
        if (responseCode == 400) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
