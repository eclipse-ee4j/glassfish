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

package org.glassfish.nucleus.admin.rest;

import javax.ws.rs.core.Response;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class ProvidersTest extends RestTestBase {
    private static final String URL_ACTION_REPORT_RESULT = "/domain/uptime";
    private static final String URL_COMMAND_RESOURCE_GET_RESULT = "/domain/stop";
    private static final String URL_GET_RESULT = "/domain";
    private static final String URL_GET_RESULT_LIST = "/domain/servers/server";
    private static final String URL_OPTIONS_RESULT = "/domain";
    private static final String URL_STRING_LIST_RESULT = "/domain/configs/config/server-config/java-config/jvm-options";
    private static String URL_TREE_NODE;

    public ProvidersTest() {
        URL_TREE_NODE = "http://localhost:" + getParameter("admin.port", "4848") + "/monitoring/domain";
    }

    @Test
    public void testActionReportResultHtmlProvider() {
        Response response = get(URL_ACTION_REPORT_RESULT + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testActionReportResultXmlProvider() {
        Response response = get(URL_ACTION_REPORT_RESULT + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testActionReportResultJsonProvider() {
        Response response = get(URL_ACTION_REPORT_RESULT + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testCommandResourceGetResultHtmlProvider() {
        Response response = get(URL_COMMAND_RESOURCE_GET_RESULT + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testCommandResourceGetResultXmlProvider() {
        Response response = get(URL_COMMAND_RESOURCE_GET_RESULT + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testCommandResourceGetResultJsonProvider() {
        Response response = get(URL_COMMAND_RESOURCE_GET_RESULT + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultHtmlProvider() {
        Response response = get(URL_GET_RESULT + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultXmlProvider() {
        Response response = get(URL_GET_RESULT + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultJsonProvider() {
        Response response = get(URL_GET_RESULT + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultListHtmlProvider() {
        Response response = get(URL_GET_RESULT_LIST + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultListXmlProvider() {
        Response response = get(URL_GET_RESULT_LIST + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testGetResultListJsonProvider() {
        Response response = get(URL_GET_RESULT_LIST + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testOptionsResultXmlProvider() {
        Response response = options(URL_OPTIONS_RESULT + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testOptionsResultJsonProvider() {
        Response response = options(URL_OPTIONS_RESULT + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testStringListResultHtmlProvider() {
        Response response = get(URL_STRING_LIST_RESULT + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testStringListResultXmlProvider() {
        Response response = get(URL_STRING_LIST_RESULT + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testStringListResultJsonProvider() {
        Response response = get(URL_STRING_LIST_RESULT + ".json");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testTreeNodeHtmlProvider() {
        Response response = get(URL_TREE_NODE + ".html");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testTreeNodeXmlProvider() {
        Response response = get(URL_TREE_NODE + ".xml");
        assertTrue(isSuccess(response));
    }

    @Test
    public void testTreeNodeJsonProvider() {
        Response response = get(URL_TREE_NODE + ".json");
        assertTrue(isSuccess(response));
    }
}
