/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation. All rights reserved.
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
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author David Matejcek
 */
public class LoggingRestITest {

    @BeforeAll
    public static void fillUpLog() {
        // The server log may become empty due to log rotation.
        // Restart domain to fill it up.
        AsadminResult result = getAsadmin().exec("restart-domain");
        assertThat(result, asadminOK());
    }

    @Test
    public void logFileNames() throws Exception {
        HttpURLConnection connection = GlassFishTestEnvironment
            .openConnection("/management/domain/view-log/details/lognames?instanceName=server");
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            connection.setDoOutput(true);
            assertEquals(200, connection.getResponseCode());
            JSONArray array = getJsonArrayFrom(connection, "InstanceLogFileNames");
            // Depends on the order of tests, there may be rolled file too.
            assertAll(
                () -> assertThat("InstanceLogFileNames", array.length(), greaterThanOrEqualTo(1)),
                () -> assertThat(array.get(0).toString(), startsWith("server.log"))
            );
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
            JSONArray array = getJsonArrayFrom(connection, "records");
            assertThat(array.length(), greaterThan(15));
        } finally {
            connection.disconnect();
        }
    }

    static String readIntoStringFrom(HttpURLConnection connection) throws Exception {
        StringWriter buffer = new StringWriter();
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            reader.transferTo(buffer);
        }
        return buffer.toString();
    }

    static JSONObject readJsonObjectFrom(HttpURLConnection connection) throws Exception {
        return new JSONObject(readIntoStringFrom(connection));
    }

    static JSONArray getJsonArrayFrom(HttpURLConnection connection, String name) throws Exception {
        return readJsonObjectFrom(connection).getJSONArray(name);
    }
}
