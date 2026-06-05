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
import org.glassfish.grizzly.http.HttpServerFilter;

/*
 * Unit test for port unification
 */
public class PortUnificationTest extends BaseDevTest {
    private int port = Integer.valueOf(antProp("http.port"));
    private String puName = "pu-protocol-test";
    private String httpName = "pu-http-protocol";
    private String finderName = "http-finder-test";
    private String dummyName = "pu-dummy-protocol";
    private String clusterName = null;

    public static void main(String[] args) throws IOException {
        new PortUnificationTest(args[0]).run();
    }

    public PortUnificationTest(String clusterName) {
        this.clusterName = clusterName;
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
            report("create-pu-protocol", asadmin("create-protocol", "--target", clusterName,
                puName));
            createHttpElements();
            //createDummyProtocolElements();
            report("set-listener", asadmin("set",
                "configs.config." + clusterName + "-config.network-config.network-listeners.network-listener.http-listener-1.protocol="
                    + puName));
            //report("enable-listener", asadmin("set",
            //    "configs.config." + clusterName + "-config.network-config.network-listeners.network-listener.http-listener-1.enabled=true"));
            final String content = getContent(new URL("http://localhost:" + port).openConnection());
            report("http-read", content.contains("<h1>Your server is now running</h1>"));
            //report("dummy-read", "Dummy-Protocol-Response".equals(getDummyProtocolContent("localhost")));

            AsadminReturn aReturn = asadminWithOutput("list-protocol-filters", "--target", clusterName, httpName);
            report("list-protocol-filters", aReturn.out.contains("http-filter"));

            aReturn = asadminWithOutput("list-protocol-finders", "--target", clusterName, puName);
            report("list-protocol-finders", aReturn.out.contains(finderName));

            //report("disable-listener", asadmin("set",
            //    "configs.config." + clusterName + "-config.network-config.network-listeners.network-listener.http-listener-2.enabled=false"));
            report("reset-listener", asadmin("set",
                "configs.config." + clusterName + "-config.network-config.network-listeners.network-listener.http-listener-1.protocol=http-listener-1"));
            deletePUElements();
        } finally {
            stat.printSummary();
        }
    }

    private String getContent(URLConnection connection) {
        InputStreamReader reader = null;
        try {
            try {
                connection.setReadTimeout(30000);
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
        report("create-dummy-protocol", asadmin("create-protocol", "--target", clusterName,
            dummyName));
        report("create-protocol-finder-dummy", asadmin("create-protocol-finder",
            "--target", clusterName,
            "--protocol", puName,
            "--targetprotocol", dummyName,
            "--classname", DummyProtocolFinder.class.getName(),
            "dummy-finder"));
        report("create-protocol-filter-dummy", asadmin("create-protocol-filter",
            "--target", clusterName,
            "--protocol", dummyName,
            "--classname", DummyProtocolFilter.class.getName(),
            "dummy-filter"));
    }

    private void createHttpElements() {
        report("create-http-protocol", asadmin("create-protocol", "--target", clusterName,
            httpName));
        report("create-http", asadmin("create-http",
            "--target", clusterName,
            "--default-virtual-server", "server",
            httpName));
        report("create-protocol-finder-http", asadmin("create-protocol-finder",
            "--target", clusterName,
            "--protocol", puName,
            "--targetprotocol", httpName,
            "--classname", HttpProtocolFinder.class.getName(),
            finderName));
        report("create-protocol-filter-http", asadmin("create-protocol-filter",
            "--target", clusterName,
            "--protocol", httpName,
            "--classname", HttpServerFilter.class.getName(),
            "http-filter"));
    }

    private void deletePUElements() {
        report("delete-protocol-finder-http", asadmin("delete-protocol-finder",
            "--target", clusterName,
            "--protocol", puName,
            finderName));
//        report("delete-protocol-filter-http", asadmin("delete-protocol-filter",
//            "--target", clusterName,
//            "--protocol", httpName,
//            "http-filter"));
        report("delete-http-protocol", asadmin("delete-protocol", "--target", clusterName,
            httpName));
        /*report("delete-protocol-finder-dummy", asadmin("delete-protocol-finder",
            "--target", clusterName,
            "--protocol", puName,
            "dummy-finder"));
        report("delete-protocol-filter-dummy", asadmin("delete-protocol-filter",
            "--target", clusterName,
            "--protocol", dummyName,
            "dummy-filter"));
        report("delete-dummy-protocol", asadmin("delete-protocol",
            "--target", clusterName,
            dummyName));*/
        report("delete-pu-protocol", asadmin("delete-protocol",
            "--target", clusterName,
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
