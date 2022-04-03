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

package org.glassfish.nucleus.admin.rest;

import jakarta.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadminWithOutput;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author jdlee
 */
public class JobsResourceITest extends RestTestBase {
    public static final String URL_JOBS = "jobs";

    @Test
    public void testJobsListing() {
        assertEquals(200, get(URL_JOBS).getStatus());
    }

    @Test
    public void testGetJob() throws Exception {
        // make sure we have at least one detached job
        nadminWithOutput("--detach", "uptime");

        // verify getting the collection
        Response response = get(URL_JOBS);
        assertEquals(200, response.getStatus());

        // verify the overall structure
        JSONObject json = response.readEntity(JSONObject.class);
        JSONArray resources = json.getJSONArray("resources");
        assertNotNull(resources);
        assertThat(resources.length(), equalTo(1));
        JSONArray items = json.getJSONArray("items");
        assertNotNull(items);
        assertThat(items.length(), equalTo(1));

        // unlike most resources that also return a parent link,
        // the jobs resource only returns child links.
        // verify the first of them
        JSONObject resource = resources.getJSONObject(0);
        String uri = resource.getString("uri");
        assertNotNull(uri);
        assertEquals("job", resource.getString("rel"));
        String jobId = resource.getString("title");
        assertNotNull(jobId);
        assertThat(uri, endsWith(URL_JOBS + "/id/" + jobId));

        // verify the job it refers to by following the link.
        // it should only have a parent link
        response = get(uri);
        assertEquals(200, response.getStatus());
        json = response.readEntity(JSONObject.class);
        JSONObject item = json.getJSONObject("item");
        System.out.println(item.toString());
        verifyItem(jobId, item);
        resources = json.getJSONArray("resources");
        assertNotNull(resources);
        assertThat(resources.length(), equalTo(1));
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
        verifyItem(jobId, item);
    }

    private void verifyItem(String expectedJobId, JSONObject jobJsonObject) throws JSONException {
        assertNotNull(jobJsonObject);
        assertEquals(expectedJobId, jobJsonObject.getString("jobId"));
    }

    @Override
    protected String getResponseType() {
        return "application/vnd.oracle.glassfish+json";
    }
}
