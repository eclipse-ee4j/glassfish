/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.nucleus.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.glassfish.nucleus.test.tool.asadmin.Asadmin;
import org.glassfish.nucleus.test.tool.asadmin.GlassFishTestEnvironment;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.glassfish.nucleus.test.tool.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Tom Mueller
 */
@TestMethodOrder(OrderAnnotation.class)
public class ClusterITest {

    private static final String PORT1 = "55123";
    private static final String PORT2 = "55124";
    private static final String CLUSTER_NAME = "eec1";
    private static final String INSTANCE_NAME_1 = "eein1-with-a-very-very-very-long-name";
    private static final String INSTANCE_NAME_2 = "eein2";
    private static final String URL1 = "http://localhost:" + PORT1;
    private static final String URL2 = "http://localhost:" + PORT2;
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @Test
    @Order(1)
    public void createClusterTest() {
        assertThat(ASADMIN.exec("create-cluster", CLUSTER_NAME), asadminOK());
    }

    @Test
    @Order(2)
    public void createInstancesTest() {
        assertThat(
            ASADMIN.exec("create-local-instance", "--cluster", CLUSTER_NAME, "--systemproperties",
                   "HTTP_LISTENER_PORT=" + PORT1
                + ":HTTP_SSL_LISTENER_PORT=18181"
                + ":IIOP_SSL_LISTENER_PORT=13800"
                + ":IIOP_LISTENER_PORT=13700"
                + ":JMX_SYSTEM_CONNECTOR_PORT=17676"
                + ":IIOP_SSL_MUTUALAUTH_PORT=13801"
                + ":JMS_PROVIDER_PORT=18686"
                + ":ASADMIN_LISTENER_PORT=14848",
                INSTANCE_NAME_1), asadminOK());

        assertThat(
            ASADMIN.exec("create-local-instance", "--cluster", CLUSTER_NAME, "--systemproperties",
                   "HTTP_LISTENER_PORT=" + PORT2
                + ":HTTP_SSL_LISTENER_PORT=28181"
                + ":IIOP_SSL_LISTENER_PORT=23800"
                + ":IIOP_LISTENER_PORT=23700"
                + ":JMX_SYSTEM_CONNECTOR_PORT=27676"
                + ":IIOP_SSL_MUTUALAUTH_PORT=23801"
                + ":JMS_PROVIDER_PORT=28686"
                + ":ASADMIN_LISTENER_PORT=24848",
                INSTANCE_NAME_2), asadminOK());
    }


    @Test
    @Order(3)
    public void startInstancesTest() {
        assertThat(ASADMIN.exec(30_000, false, "start-local-instance", INSTANCE_NAME_1), asadminOK());
        assertThat(ASADMIN.exec(30_000, false, "start-local-instance", INSTANCE_NAME_2), asadminOK());
    }

    @Test
    @Order(4)
    public void checkClusterTest() {
        assertThat(ASADMIN.exec("list-instances"), asadminOK());
        assertThat(getURL(URL1), stringContainsInOrder("GlassFish Server"));
        assertThat(getURL(URL2), stringContainsInOrder("GlassFish Server"));
    }

    @Test
    @Order(5)
    public void stopInstancesTest() {
        assertThat(ASADMIN.exec("stop-local-instance", "--kill", INSTANCE_NAME_1), asadminOK());
        assertThat(ASADMIN.exec("stop-local-instance", "--kill", INSTANCE_NAME_2), asadminOK());
    }

    @Test
    @Order(6)
    public void deleteInstancesTest() {
        assertThat(ASADMIN.exec("delete-local-instance", INSTANCE_NAME_1), asadminOK());
        assertThat(ASADMIN.exec("delete-local-instance", INSTANCE_NAME_2), asadminOK());
    }

    @Test
    @Order(7)
    public void deleteClusterTest() {
        assertThat(ASADMIN.exec("delete-cluster", CLUSTER_NAME), asadminOK());
    }


    /**
     * This methods opens a connection to the given URL and
     * returns the string that is returned from that URL.  This
     * is useful for simple servlet retrieval
     *
     * @param urlstr The URL to connect to
     * @return The string returned from that URL, or empty
     * string if there was a problem contacting the URL
     */
    public static String getURL(String urlstr) {
        URLConnection urlc = openConnection(urlstr);
        try (
            BufferedReader ir = new BufferedReader(new InputStreamReader(urlc.getInputStream(), ISO_8859_1));
            StringWriter ow = new StringWriter();
        ) {
            String line;
            while ((line = ir.readLine()) != null) {
                ow.write(line);
                ow.write("\n");
            }
            return ow.getBuffer().toString();
        } catch (IOException ex) {
            return fail(ex);
        }
    }

    private static URLConnection openConnection(String url) {
        try {
            return new URL(url).openConnection();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
