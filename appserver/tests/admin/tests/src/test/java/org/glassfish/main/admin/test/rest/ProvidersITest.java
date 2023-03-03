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

package org.glassfish.main.admin.test.rest;

import jakarta.ws.rs.core.Response;

import org.glassfish.main.itest.tools.DomainAdminRestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jasonlee
 */
public class ProvidersITest extends RestTestBase {
    private static final String URL_ACTION_REPORT_RESULT = "/domain/uptime";
    private static final String URL_COMMAND_RESOURCE_GET_RESULT = "/domain/stop";
    private static final String URL_GET_RESULT = "/domain";
    private static final String URL_GET_RESULT_LIST = "/domain/servers/server";
    private static final String URL_OPTIONS_RESULT = "/domain";
    private static final String URL_STRING_LIST_RESULT = "/domain/configs/config/server-config/java-config/jvm-options";

    private static DomainAdminRestClient monitoringClient;

    @BeforeAll
    public static void init() {
        monitoringClient = new DomainAdminRestClient(getBaseAdminUrl() + "/monitoring/domain");
    }


    @AfterAll
    public static void closeResources() {
        if (monitoringClient != null) {
            monitoringClient.close();
        }
    }

    @Test
    public void testActionReportResultHtmlProvider() {
        Response response = managementClient.get(URL_ACTION_REPORT_RESULT + ".html");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testActionReportResultXmlProvider() {
        Response response = managementClient.get(URL_ACTION_REPORT_RESULT + ".xml");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testActionReportResultJsonProvider() {
        Response response = managementClient.get(URL_ACTION_REPORT_RESULT + ".json");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCommandResourceGetResultHtmlProvider() {
        Response response = managementClient.get(URL_COMMAND_RESOURCE_GET_RESULT + ".html");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCommandResourceGetResultXmlProvider() {
        Response response = managementClient.get(URL_COMMAND_RESOURCE_GET_RESULT + ".xml");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCommandResourceGetResultJsonProvider() {
        Response response = managementClient.get(URL_COMMAND_RESOURCE_GET_RESULT + ".json");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetResultHtmlProvider() {
        Response response = managementClient.get(URL_GET_RESULT + ".html");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetResultXmlProvider() {
        Response response = managementClient.get(URL_GET_RESULT + ".xml");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetResultJsonProvider() {
        Response response = managementClient.get(URL_GET_RESULT + ".json");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetResultListHtmlProvider() {
        Response response = managementClient.get(URL_GET_RESULT_LIST + ".html");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetResultListXmlProvider() {
        Response response = managementClient.get(URL_GET_RESULT_LIST + ".xml");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetResultListJsonProvider() {
        Response response = managementClient.get(URL_GET_RESULT_LIST + ".json");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testOptionsResultXmlProvider() {
        Response response = managementClient.options(URL_OPTIONS_RESULT + ".xml");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testOptionsResultJsonProvider() {
        Response response = managementClient.options(URL_OPTIONS_RESULT + ".json");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStringListResultHtmlProvider() {
        Response response = managementClient.get(URL_STRING_LIST_RESULT + ".html");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStringListResultXmlProvider() {
        Response response = managementClient.get(URL_STRING_LIST_RESULT + ".xml");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testStringListResultJsonProvider() {
        Response response = managementClient.get(URL_STRING_LIST_RESULT + ".json");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testTreeNodeHtmlProvider() {
        Response response = monitoringClient.get(".html");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testTreeNodeXmlProvider() {
        Response response = monitoringClient.get(".xml");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testTreeNodeJsonProvider() {
        Response response = monitoringClient.get(".json");
        assertEquals(200, response.getStatus());
    }
}
