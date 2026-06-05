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

package org.glassfish.devtests.web.portunif;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import com.sun.appserv.test.BaseDevTest;
import org.glassfish.grizzly.config.portunif.HttpProtocolFinder;

/*
 * Unit test for port unification
 */
public class PortUnificationTest extends BaseDevTest {
    private int port = Integer.valueOf(antProp("https.port"));
    private String puName = "pu-protocol";
    private String httpName = "pu-http-protocol";
    private String dummyName = "pu-dummy-protocol";

    public static void main(String[] args) throws IOException {
        new PortUnificationTest().run();
    }

    @Override
    protected String getTestName() {
        return "port-unification";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for managing port unification";
    }

    public void run() throws IOException {
        try {
            report("create-pu-protocol", asadmin("create-protocol",
                puName));
            createHttpElements();
            createDummyProtocolElements();
            report("set-listener", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol="
                    + puName));
            final String content = getContent(new URL("http://localhost:" + port).openConnection());
            report("http-read", content.contains("<h1>Your server is now running</h1>"));
            report("dummy-read", "Dummy-Protocol-Response".equals(getDummyProtocolContent("localhost")));

            AsadminReturn aReturn = asadminWithOutput("list-protocol-filters", "pu-dummy-protocol");
            report("list-protocol-filters", aReturn.out.contains("dummy-filter"));

            aReturn = asadminWithOutput("list-protocol-finders", "pu-protocol");
            report("list-protocol-finders", aReturn.out.contains("http-finder") && aReturn.out.contains("dummy-finder"));

        } finally {
            try {
                report("reset-listener", asadmin("set",
                    "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=http-listener-2"));
                deletePUElements();
            } finally {
                stat.printSummary();
            }
        }
    }

    private String getContent(URLConnection connection) {
        InputStreamReader reader = null;
        try {
            try {
                connection.setConnectTimeout(30000);
                reader = new InputStreamReader(connection.getInputStream());
                StringBuilder builder = new StringBuilder();
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    builder.append(buffer, 0, read);
                }
                return builder.toString();
            } finally {
                if(reader != null) {
                    reader.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getDummyProtocolContent(String host) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        Socket s = new Socket(host, port);
        try {
            os = s.getOutputStream();
            os.write("dummy-protocol".getBytes());
            os.flush();
            is = s.getInputStream();
            baos = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
        } finally {
            close(os);
            close(is);
            close(baos);
            s.close();
        }
        return new String(baos.toByteArray());
    }

    private void createDummyProtocolElements() {
        report("create-dummy-protocol", asadmin("create-protocol",
            dummyName));
        report("create-protocol-finder-dummy", asadmin("create-protocol-finder",
            "--protocol", puName,
            "--targetprotocol", dummyName,
            "--classname", DummyProtocolFinder.class.getName(),
            "dummy-finder"));
        report("create-protocol-filter-dummy", asadmin("create-protocol-filter",
            "--protocol", dummyName,
            "--classname", DummyProtocolFilter.class.getName(),
            "dummy-filter"));
    }

    private void createHttpElements() {
        report("create-http-protocol", asadmin("create-protocol",
            httpName));
        report("create-http", asadmin("create-http",
            "--default-virtual-server", "server",
            httpName));
        report("create-protocol-finder-http", asadmin("create-protocol-finder",
            "--protocol", puName,
            "--targetprotocol", httpName,
            "--classname", HttpProtocolFinder.class.getName(),
            "http-finder"));
    }

    private void deletePUElements() {
        report("delete-http-protocol", asadmin("delete-protocol",
            httpName));
        report("delete-protocol-finder-http", asadmin("delete-protocol-finder",
            "--protocol", puName,
            "http-finder"));
        report("delete-protocol-finder-dummy", asadmin("delete-protocol-finder",
            "--protocol", puName,
            "dummy-finder"));
        report("delete-protocol-filter-dummy", asadmin("delete-protocol-filter",
            "--protocol", dummyName,
            "dummy-filter"));
        report("delete-dummy-protocol", asadmin("delete-protocol",
            dummyName));
        report("delete-pu-protocol", asadmin("delete-protocol",
            puName));
    }

    private void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }
}
