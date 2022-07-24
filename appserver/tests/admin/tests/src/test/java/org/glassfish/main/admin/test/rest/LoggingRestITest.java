/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.core.MediaType;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.main.admin.test.tool.asadmin.GlassFishTestEnvironment;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author David Matejcek
 */
public class LoggingRestITest {

    @Test
    public void logFileNames() throws Exception {
        HttpURLConnection connection = GlassFishTestEnvironment
            .openConnection("/management/domain/view-log/details/lognames?instanceName=server");
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            connection.setDoOutput(true);
            assertEquals(200, connection.getResponseCode());
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                StringWriter buffer = new StringWriter();
                reader.transferTo(buffer);
                JSONObject json = new JSONObject(buffer.toString());
                JSONArray array = json.getJSONArray("InstanceLogFileNames");
                assertAll(
                    () -> assertThat("InstanceLogFileNames", array.length(), equalTo(1)),
                    () -> assertThat(array.get(0), equalTo("server.log"))
                );
            }
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void viewLogDetails() throws Exception {
        HttpURLConnection connection = GlassFishTestEnvironment.openConnection("/management/domain/view-log/details");
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            assertEquals(200, connection.getResponseCode());
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                StringWriter buffer = new StringWriter();
                reader.transferTo(buffer);
                JSONObject json = new JSONObject(buffer.toString());
                JSONArray array = json.getJSONArray("records");
                assertThat(array.length(), greaterThan(15));
            }
        } finally {
            connection.disconnect();
        }
    }
}
