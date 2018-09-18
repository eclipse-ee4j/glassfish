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

package org.glassfish.admingui.devtests;

import org.glassfish.admingui.devtests.util.SeleniumHelper;
import org.glassfish.admingui.common.util.RestUtil;
import java.util.HashMap;
import org.glassfish.admingui.common.util.RestResponse;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 *
 * @author jasonlee
 */
/*public class RestResponseTest {
    private static final SeleniumHelper helper = SeleniumHelper.getInstance();
    public static final String BASE_URL = helper.getBaseUrl() + "/management/domain";
    static final String URL_UPTIME = BASE_URL + "/uptime";
    static final String URL_GENERATE_JVM_REPORT = BASE_URL + "/servers/server/server/generate-jvm-report";

    @Test
    public void testPostCommand() {
        RestResponse response = RestUtil.get(URL_GENERATE_JVM_REPORT, new HashMap<String, Object>(){{
            put ("type", "summary");
        }});
        final String responseBody = response.getResponseBody();
        System.err.println(responseBody);
        assertTrue(responseBody.contains("Operating System Information"));
    }

    @Test
    public void testGetCommand() {
        RestResponse response = RestUtil.get(URL_UPTIME);
        assertTrue(response.getResponseBody().contains("\"uptime AdminCommand\""));
    }

    @Test
    public void testEndpointExists() {
        RestResponse response = RestUtil.get(URL_UPTIME);
        assertTrue(response.isSuccess());

        response = RestUtil.get(URL_UPTIME + "/forceFailure");
        assertFalse(response.isSuccess());
    }
}*/
