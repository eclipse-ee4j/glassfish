/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests the changes to the handling of @Element("*") instances
 * @author jasonlee
 */
public class ElementStarTest extends RestTestBase {
    protected static final String URL_CREATE_INSTANCE = "/domain/create-instance";

    protected String instanceName1;
    protected String instanceName2;

    @BeforeMethod
    public void before() {
        instanceName1 = "instance_" + generateRandomString();
        instanceName2 = "instance_" + generateRandomString();

        Response response = post(URL_CREATE_INSTANCE, new HashMap<String, String>() {{ put("id", instanceName1); put("node", "localhost-domain1"); }});
        checkStatusForSuccess(response);
        response = post(URL_CREATE_INSTANCE, new HashMap<String, String>() {{ put("id", instanceName2); put("node", "localhost-domain1"); }});
        checkStatusForSuccess(response);
    }

    @AfterMethod
    public void after() {
        Response response = delete("/domain/servers/server/" + instanceName1 + "/delete-instance"); // TODO: This url should be fixed
        checkStatusForSuccess(response);
        response = delete("/domain/servers/server/" + instanceName2 + "/delete-instance");
        checkStatusForSuccess(response);
    }

    @Test(enabled=false)
    public void testApplications() throws URISyntaxException {
        ApplicationTest at = getTestClass(ApplicationTest.class);
        final String app1 = "app" + generateRandomString();
        final String app2 = "app" + generateRandomString();

        at.deployApp("test.war", app1, app1);
        at.deployApp("test.war", app2, app2);
        at.addAppRef(app1, instanceName1);
        at.addAppRef(app2, instanceName1);

        Response response = get("/domain/servers/server/"+instanceName1+"/application-ref");
        Map<String, String> children = this.getChildResources(response);
        assertEquals(2, children.size());
    }

    @Test
    public void testResources() {
        // The DAS should already have two resource-refs (jdbc/__TimerPool and jdbc/__default)
        Response response = get ("/domain/servers/server/server/resource-ref");
        Map<String, String> children = this.getChildResources(response);
        assertTrue(children.size() >= 2);
    }

    @Test(enabled=false)
    public void testLoadBalancerConfigs() {
        final String lbName = "lbconfig-" + generateRandomString();
        Response response = post ("/domain/lb-configs/lb-config/",
                new HashMap<String, String>() {{
                    put("id", lbName);
                    put("target", instanceName1);
                }});
        checkStatusForSuccess(response);

        response = post("/domain/lb-configs/lb-config/" + lbName + "/create-http-lb-ref",
                new HashMap<String,String>() {{
                    put ("id", instanceName2);
                }});
        checkStatusForSuccess(response);

        response = get ("/domain/lb-configs/lb-config/" + lbName + "/server-ref");
        Map<String, String> children = this.getChildResources(response);
        assertTrue(!children.isEmpty());
    }
}
