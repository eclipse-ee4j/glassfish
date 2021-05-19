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

package org.glassfish.devtests.web.httpcompression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.sun.appserv.test.BaseDevTest;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
* Unit test for http compression
*/
public class HttpCompressionTest extends BaseDevTest {
    @Override
    protected String getTestName() {
        return "http-compression";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for setting http compression levels";
    }

    public void run() {
        try {
            final int port = Integer.valueOf(antProp("http.port"));
                                final String path = "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=";
            final String[] schemes = {"gzip", "lzma"};
            for (String scheme : schemes) {
                String header = scheme + "-";
                get("localhost", port, false, "compressed-output-off", scheme);

                report(header + "set-compression-on", asadmin("set", path + "on"));
                get("localhost", port, true, "compressed-output-on", scheme);

                report(header + "set-compression-force", asadmin("set", path + "force"));
                get("localhost", port, true, "compressed-output-force", scheme);

                report(header + "set-compression-false", !asadmin("set", path + "false"));

                report(header + "set-compression-true", !asadmin("set", path + "true"));

                report(header + "set-compression-1024", asadmin("set", path + "1024"));
                get("localhost", port, true, "compressed-output-1024", scheme);

                report(header + "set-compression-off", asadmin("set", path + "off"));
                get("localhost", port, false, "compressed-output-off-2", scheme);
            }
        } catch (Exception e) {
                          report(e.getMessage(), false);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            stat.printSummary();
        }
    }

    private void get(String host, int port, boolean zipped, final String test, final String compScheme)
        throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        send(os, "GET /index.html HTTP/1.1");
        send(os, "Host: localhost:8080");
        if (zipped) {
            send(os, "Accept-Encoding: " + compScheme);
        }
        send(os, "\n");
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean found = false;
        boolean chunked = false;
        boolean contentLength = false;
        try {
            while ((line = bis.readLine()) != null && !"".equals(line.trim())) {
                found |= line.toLowerCase().contains("content-encoding: " + compScheme);
                if (zipped) {
                   chunked |= line.toLowerCase().contains("transfer-encoding: chunked");
                   contentLength |= !line.toLowerCase().contains("content-length");
                }
            }
        } finally {
            s.close();
        }
        if (zipped) {
            stat.addStatus(compScheme + "-" + test, (found && chunked && contentLength) ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
        } else {
            stat.addStatus(compScheme + "-" + test, found ? SimpleReporterAdapter.FAIL : SimpleReporterAdapter.PASS);
        }
    }

    private void send(final OutputStream os, final String text) throws IOException {
        os.write((text + "\n").getBytes());
    }

    public static void main(String[] args) {
        try {
            new HttpCompressionTest().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
