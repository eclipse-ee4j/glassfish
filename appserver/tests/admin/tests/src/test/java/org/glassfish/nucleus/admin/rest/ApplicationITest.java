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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.nucleus.test.webapp.HelloServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jasonlee
 */
public class ApplicationITest extends RestTestBase {

    @Test
    public void testApplicationDeployment() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();

        try {
            Map<String, String> deployedApp = deployApp(getWar("test"), appName, appName);
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

        Map<String, String> deployedApp = deployApp(getWar("test"), appName, appName);
        assertEquals(appName, deployedApp.get("name"));

        assertEquals("/" + appName, deployedApp.get("contextRoot"));

        try {
            String appUrl = getBaseInstanceUrl() + appName;
            Response response = get(appUrl);
            assertEquals ("Hello!", response.readEntity(String.class));

            response = post(URL_APPLICATION_DEPLOY + "/" + appName + "/disable");
            assertThat(response.getStatus(), equalTo(200));

            response = get(appUrl);
            assertEquals(404, response.getStatus());

            response = post(URL_APPLICATION_DEPLOY + "/" + appName + "/enable");
            assertThat(response.getStatus(), equalTo(200));

            response = get(appUrl);
            assertEquals ("Hello!", response.readEntity(String.class).trim());
        } finally {
            undeployApp(appName);
        }
    }

    @Test
    public void listSubComponents() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();

        try {
            deployApp(getEar("simple"), appName, appName);
            Response response = get(URL_APPLICATION_DEPLOY +"/" + appName + "/list-sub-components?id=" + appName);
            assertThat(response.getStatus(), equalTo(200));
            String subComponents = response.readEntity(String.class);
            assertThat(subComponents, stringContainsInOrder("simple"));

            response = get(URL_APPLICATION_DEPLOY +"/" + appName + "/list-sub-components?id=simple.war&appname=" + appName);
            assertThat(response.getStatus(), equalTo(200));
            subComponents = response.readEntity(String.class);
            assertThat(subComponents, stringContainsInOrder(HelloServlet.class.getName()));
        } finally {
            undeployApp(appName);
        }
    }

    @Test
    public void testCreatingAndDeletingApplicationRefs() throws URISyntaxException {
        final String instanceName = "instance_" + generateRandomString();
        final String appName = "testApp" + generateRandomString();
        final String appRefUrl = "domain/servers/server/" + instanceName + "/application-ref";

        Map<String, String> newInstance = Map.of("id", instanceName, "node", "localhost-domain1");
        Map<String, String> applicationRef = Map.of("id", appName, "target", instanceName);

        try {
            Response response = post(URL_CREATE_INSTANCE, newInstance);
            assertThat(response.getStatus(), equalTo(200));

            deployApp(getWar("test"), appName, appName);

            response = post (appRefUrl, applicationRef);
            assertThat(response.getStatus(), equalTo(200));

            response = get(appRefUrl + "/" + appName);
            assertThat(response.getStatus(), equalTo(200));

            response = delete(appRefUrl + "/" + appName, Map.of("target", instanceName));
            assertThat(response.getStatus(), equalTo(200));
        } finally {
            Response response = delete("domain/servers/server/" + instanceName + "/delete-instance");
            assertThat(response.getStatus(), equalTo(200));
            response = get("domain/servers/server/" + instanceName);
            assertEquals(404, response.getStatus());
            undeployApp(appName);
        }
    }

    @Test
    public void testGetContextRoot() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();
        try {
            Map<String, String> deployedApp = deployApp(getEar("simple"), appName, appName);
            assertEquals(appName, deployedApp.get("name"));
            Map<String, String> contextRootPayload = Map.of("appname", appName, "modulename", "simple");
            Response response = get("domain/applications/application/" + appName + "/get-context-root", contextRootPayload);
            assertThat(response.getStatus(), equalTo(200));
            assertThat(response.readEntity(String.class),
                stringContainsInOrder("command", "_get-context-root AdminCommand", "exit_code", "SUCCESS",
                    "--appname", appName, "--modulename", "simple", "method", "GET"));
        } finally {
            undeployApp(appName);
        }
    }

    @Test
    public void testUndeploySubActionWarnings() throws URISyntaxException {
        final String appName = "testApp" + generateRandomString();
        final String serverName = "in" + generateRandomNumber();
        try {
            Response response = post ("domain/create-instance", Map.of("id", serverName, "node", "localhost-domain1"));
            assertThat(response.getStatus(), equalTo(200));

            response = post("domain/servers/server/" + serverName + "/start-instance");
            assertThat(response.getStatus(), equalTo(200));

            deployApp(getWar("test"), appName, appName);
            addAppRef(appName, serverName);

            response = post("domain/servers/server/" + serverName + "/stop-instance");
            assertThat(response.getStatus(), equalTo(200));

            response = delete("domain/applications/application/" + appName, Map.of("target", "domain"));
            assertThat(response.readEntity(String.class),
                stringContainsInOrder("deleted successfully", "exit_code", "SUCCESS"));
        } finally {
            delete("domain/applications/application/" + appName, Map.of("target", "domain"));
        }
    }
}
