/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.nucleus.admin.rest;

import jakarta.ws.rs.core.Response;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jasonlee
 */
@Disabled("Missing test application")
public class ApplicationTest extends RestTestBase {

    @Test
    public void testApplicationDeployment() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();

        try {
            Map<String, String> deployedApp = deployApp(getFile("test.war"), appName, appName);
            assertEquals(appName, deployedApp.get("name"));

            assertEquals("/" + appName, deployedApp.get("contextRoot"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            undeployApp(appName);
        }
    }

    @Test
    public void testApplicationDisableEnable() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();

        Map<String, String> deployedApp = deployApp(getFile("test.war"), appName, appName);
        assertEquals(appName, deployedApp.get("name"));

        assertEquals("/" + appName, deployedApp.get("contextRoot"));

        try {
            String appUrl = getBaseAdminUrl() + appName;
            Response response = get(appUrl);
            assertEquals ("Test", response.readEntity(String.class).trim());

            response = post(URL_APPLICATION_DEPLOY + "/" + appName + "/disable");
            checkStatus(response);

            response = get(appUrl);
            assertEquals(404, response.getStatus());

            response = post(URL_APPLICATION_DEPLOY + "/" + appName + "/enable");
            checkStatus(response);

            response = get(appUrl);
            assertEquals ("Test", response.readEntity(String.class).trim());
        } finally {
            undeployApp(appName);
        }
    }

    @Test
    public void listSubComponents() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();

        try {
            deployApp(getFile("stateless-simple.ear"), appName, appName);
            Response response = get(URL_APPLICATION_DEPLOY +"/" + appName + "/list-sub-components?id=" + appName);
            checkStatus(response);
            String subComponents = response.readEntity(String.class);
            assertTrue(subComponents.contains("stateless-simple.war"));

            response = get(URL_APPLICATION_DEPLOY +"/" + appName + "/list-sub-components?id=stateless-simple.war&appname=" + appName);
            checkStatus(response);
            subComponents = response.readEntity(String.class);
            assertTrue(subComponents.contains("GreeterServlet"));
        } finally {
            undeployApp(appName);
        }
    }

    @Test
    public void testCreatingAndDeletingApplicationRefs() throws URISyntaxException {
        final String instanceName = "instance_" + generateRandomString();
        final String appName = "testApp" + generateRandomString();
        final String appRefUrl = "domain/servers/server/" + instanceName + "/application-ref";

        Map<String, String> newInstance = new HashMap<>() {{
            put("id", instanceName);
            put("node", "localhost-domain1");
        }};
        Map<String, String> applicationRef = new HashMap<>() {{
            put("id", appName);
            put("target", instanceName);
        }};

        try {
            Response response = post(URL_CREATE_INSTANCE, newInstance);
            checkStatus(response);

            deployApp(getFile("test.war"), appName, appName);

            response = post (appRefUrl, applicationRef);
            checkStatus(response);

            response = get(appRefUrl + "/" + appName);
            checkStatus(response);

            response = delete(appRefUrl + "/" + appName, new HashMap<String, String>() {{ put("target", instanceName); }});
            checkStatus(response);
        } finally {
            Response response = delete("domain/servers/server/" + instanceName + "/delete-instance");
            checkStatus(response);
            response = get("domain/servers/server/" + instanceName);
            assertEquals(404, response.getStatus());
            undeployApp(appName);
        }
    }

    @Test
    public void testGetContextRoot() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();

        try {
            Map<String, String> deployedApp = deployApp(getFile("stateless-simple.ear"), appName, appName);
            assertEquals(appName, deployedApp.get("name"));
            Map<String, String> contextRootPayload = new HashMap<>() {{
                put("appname", appName);
                put("modulename", "stateless-simple.war");
            }};

            Response response = get("domain/applications/application/" +appName + "/get-context-root", contextRootPayload);
            checkStatus(response);
            assertTrue(response.readEntity(String.class).contains("helloworld"));
        } finally {
            undeployApp(appName);
        }
    }

    @Test
    public void testUndeploySubActionWarnings() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();
        final String serverName = "in" + generateRandomNumber();
        try {
            Response response = post ("domain/create-instance", new HashMap<String, String>() {{
                put("id", serverName);
                put("node", "localhost-domain1");
            }});
            checkStatus(response);

            response = post("domain/servers/server/" + serverName + "/start-instance");
            checkStatus(response);

            deployApp(getFile("test.war"), appName, appName);
            addAppRef(appName, serverName);

            response = post("domain/servers/server/" + serverName + "/stop-instance");
            checkStatus(response);

            response = delete ("domain/applications/application/"+appName, new HashMap<String, String>() {{
                put("target", "domain");
            }});
            assertTrue(response.readEntity(String.class).contains("WARNING: Instance " + serverName + " seems to be offline"));
        } finally {
            delete ("domain/applications/application/" + appName, new HashMap<String, String>() {{
                put("target", "domain");
            }});
        }
    }

    private File getFile(String fileName) throws URISyntaxException {
        final URL resource = getClass().getResource("/" + fileName);
        return new File(resource.toURI());
    }
}
