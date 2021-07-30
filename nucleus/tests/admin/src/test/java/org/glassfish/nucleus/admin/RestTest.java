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

package org.glassfish.nucleus.admin;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

@Test
public class RestTest {

    public void testManagementEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/management/domain.xml");
            assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testMonitoringEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/monitoring/domain.xml");
            assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testAdminCommandEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/management/domain/version.xml");
            assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testChildConfigBeanEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/management/domain/applications.xml");
            assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testPostGetDelete() {
        deleteNode(); // This should almost always fail, so we don't check the status. Just need to clean up from any prior runs
        assertEquals(200, createNode());
        assertEquals(200, getNode());
        assertEquals(200, deleteNode());
    }

    protected HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("X-GlassFish-3", "true");
        connection.setRequestProperty("X-Requested-By", "dummy");
        return connection;
    }

    private int createNode() {
        HttpURLConnection connection = null;
        try {
            String parameters = "name=myConfigNode";
            connection = getConnection("http://localhost:4848/management/domain/nodes/create-node-config");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Language", "en-US");
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();
            return connection.getResponseCode();
        } catch (Exception ex) {
            fail(ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return -1;
    }

    private int getNode() {
        HttpURLConnection connection = null;
        try {
            connection = getConnection("http://localhost:4848/management/domain/nodes/node/myConfigNode");
            return connection.getResponseCode();
        } catch (Exception ex) {
            fail(ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return -1;
    }

    private int deleteNode() {
        HttpURLConnection connection = null;
        try {
            connection = getConnection("http://localhost:4848/management/domain/nodes/delete-node-config?name=myConfigNode");
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            return connection.getResponseCode();
        } catch (Exception ex) {
            fail(ex.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return -1;
    }
}
