/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.main.itest.tools.DomainAdminRestClient;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author jdlee
 */
public class JobsResourceITest extends RestTestBase {

    private static final String GF_JSON_TYPE = "application/vnd.oracle.glassfish+json";
    private static final String URL_JOBS = "/jobs";

    private DomainAdminRestClient client;
    private String jobId;

    @BeforeEach
    public void initInstanceClient() {
        client = new DomainAdminRestClient(getBaseAdminUrl() + "/management", GF_JSON_TYPE);
    }


    @AfterEach
    public void closeInstanceClient() {
        if (jobId != null) {
            GlassFishTestEnvironment.getAsadmin().exec("attach", jobId);
        }
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void testJobsListing() {
        assertEquals(200, client.get(URL_JOBS).getStatus());
    }

    @Test
    public void testGetJob() throws Exception {
        // make sure we have at least one detached job
        assertThat(getAsadmin().exec("--detach", "uptime"), asadminOK());

        // verify getting the collection
        Response response = client.get(URL_JOBS);
        assertEquals(200, response.getStatus());

        // verify the overall structure
        JSONObject json = response.readEntity(JSONObject.class);
        JSONArray resources = json.getJSONArray("resources");
        assertNotNull(resources);
        assertThat(resources.toString(), resources.length(), equalTo(1));

        JSONArray items = json.getJSONArray("items");
        assertNotNull(items);
        assertThat(items.toString(), items.length(), equalTo(1));

        // unlike most resources that also return a parent link,
        // the jobs resource only returns child links.
        // verify the first of them
        JSONObject resource = resources.getJSONObject(0);
        String uri = resource.getString("uri");
        assertNotNull(uri);
        assertEquals("job", resource.getString("rel"));

        jobId = resource.getString("title");
        assertNotNull(jobId);
        assertThat(uri, endsWith(URL_JOBS + "/id/" + jobId));

        // verify the job it refers to by following the link.
        // it should only have a parent link
        try (GenericClient genericClient = new GenericClient()) {
            response = genericClient.get(uri);
            assertThat(response.getStatus(), equalTo(200));
            json = response.readEntity(JSONObject.class);
        }
        JSONObject item = json.getJSONObject("item");
        System.out.println(item.toString());
        assertNotNull(item);
        assertEquals(jobId, item.getString("jobId"));

        resources = json.getJSONArray("resources");
        assertNotNull(resources);
        assertThat(resources.toString(), resources.length(), equalTo(1));

        resource = resources.getJSONObject(0);
        assertEquals("parent", resource.getString("rel"));
        assertThat(resource.getString("uri"), endsWith(URL_JOBS));

        // verify that the collection returned the item too
        item = null;
        for (int i = 0; item == null && i < items.length(); i++) {
            JSONObject thisItem = items.getJSONObject(i);
            if (jobId.equals(thisItem.getString("jobId"))) {
                item = thisItem;
            }
        }
        assertNotNull(item);
        assertEquals(jobId, item.getString("jobId"));
    }


    private static class GenericClient extends DomainAdminRestClient {
        public GenericClient() {
            super(GlassFishTestEnvironment.createClient(), "", GF_JSON_TYPE);
        }
    }
}
