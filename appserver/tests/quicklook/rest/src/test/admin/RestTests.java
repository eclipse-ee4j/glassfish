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

package test.admin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Supposed to have JDBC connection pool and resource tests.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
@Test(groups = {"rest"}, description = "REST API Tests")
public class RestTests {

    @Test
    public void testManagementEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/management/domain.xml");
            Assert.assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testMonitoringEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/monitoring/domain.xml");
            Assert.assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testEndpointWithEncodedSlash() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/management/domain/resources/jdbc-resource/jdbc%2F__TimerPool.xml");
            Assert.assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAdminCommandEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/management/domain/version.xml");
            Assert.assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testChildConfigBeanEndpoint() {
        try {
            HttpURLConnection connection = getConnection("http://localhost:4848/management/domain/applications.xml");
            Assert.assertEquals(200, connection.getResponseCode());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    protected HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("X-GlassFish-3", "true");
        return connection;
    }
}
