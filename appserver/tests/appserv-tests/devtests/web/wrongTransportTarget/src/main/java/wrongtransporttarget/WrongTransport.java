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

package wrongtransporttarget;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.appserv.test.BaseDevTest;
import org.glassfish.grizzly.config.portunif.HttpProtocolFinder;

public class WrongTransport extends BaseDevTest {
    private static final String TEST_NAME = "wrongTransportTarget";
    private String secureURL;
    private String clusterName;
    private String puName = "pu-protocol-test";
    private String finderName = "http-finder-test";


    public WrongTransport(final String clusterName, final String host, final String port) {
        this.clusterName = clusterName;
        createPUElements();
        try {
            secureURL = "https://" + host + ":" + port + "/";
            HttpURLConnection connection = (HttpURLConnection) new URL("http://" + host + ":" + port + "/").openConnection();
            connection.setInstanceFollowRedirects(true);
            checkStatus(connection);
            parseResponse(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            deletePUElements();
        }
        stat.printSummary();
    }

    @Override
    protected String getTestName() {
        return TEST_NAME;
    }

    @Override
    protected String getTestDescription() {
        return "Wrong Protocol SSL test";
    }

    public static void main(String args[]) throws Exception {
        new WrongTransport(args[0], args[1], args[2]);
    }

    private void createPUElements() {
        // http-redirect
        report("create-http-redirect-protocol", asadmin("create-protocol", "--target", clusterName,
            "http-redirect"));
        report("create-protocol-filter-redirect", asadmin("create-protocol-filter",
            "--target", clusterName,
            "--protocol", "http-redirect",
            "--classname", "org.glassfish.grizzly.config.portunif.HttpRedirectFilter",
            "redirect-filter"));

        //  pu-protocol
        report("create-pu-protocol", asadmin("create-protocol", "--target", clusterName,
            puName));
        report("create-protocol-finder-http-finder", asadmin("create-protocol-finder",
            "--target", clusterName,
            "--protocol", puName,
            "--targetprotocol", "http-listener-2",
            "--classname", HttpProtocolFinder.class.getName(),
            finderName));
        report("create-protocol-finder-http-redirect", asadmin("create-protocol-finder",
            "--target", clusterName,
            "--protocol", puName,
            "--targetprotocol", "http-redirect",
            "--classname", HttpProtocolFinder.class.getName(),
            "http-redirect"));
        // reset listener
        report("set-http-listener-protocol", asadmin("set",
            "configs.config." + clusterName + "-config.network-config.network-listeners.network-listener.http-listener-1.protocol="+puName));
    }

    private void deletePUElements() {
        // reset listener
        report("reset-http-listener-protocol", asadmin("set",
            "configs.config." + clusterName + "-config.network-config.network-listeners.network-listener.http-listener-1.protocol=http-listener-1"));
        report("delete-pu-protocol", asadmin("delete-protocol", "--target", clusterName,
            puName));
        report("delete-http-redirect", asadmin("delete-protocol", "--target", clusterName,
            "http-redirect"));
    }

    private void checkStatus(HttpURLConnection connection)
        throws Exception {
        int responseCode = connection.getResponseCode();
        String location = connection.getHeaderField("location");
        System.out.println("responseCode= "+responseCode+" location= "+location);
        report("response-code", responseCode == 302);
        report("returned-location", secureURL.equals(location));
    }

    private void parseResponse(HttpURLConnection connection) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            in.close();
        }
    }
}
