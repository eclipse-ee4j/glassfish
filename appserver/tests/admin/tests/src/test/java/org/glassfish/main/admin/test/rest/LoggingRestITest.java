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

import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.main.itest.tools.DomainAdminRestClient;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author David Matejcek
 */
public class LoggingRestITest extends RestTestBase {

    private static final String URL_VIEW_LOG = "/domain/view-log";
    private static final String URL_VIEW_LOG_DETAILS = "/domain/view-log/details";
    private static final String URL_LOGNAMES = "/domain/view-log/details/lognames";

    @BeforeAll
    public static void fillUpLog() {
        // The server log may become empty due to log rotation.
        // Restart domain to fill it up.
        AsadminResult result = getAsadmin().exec("restart-domain");
        assertThat(result, asadminOK());
    }

    @Test
    public void viewLog() {
        try (ViewLogClient client = new ViewLogClient()) {
            // Read entire log
            Response response = client.get(URL_VIEW_LOG);
            assertThat(response.getStatus(), equalTo(200));

            int logSize = response.readEntity(String.class).length();
            // Should not be empty
            assertThat(logSize, greaterThan(0));

            // Get the entire URL to return the changes since the last call
            String nextUrl = response.getHeaderString("X-Text-Append-Next");
            assertThat(nextUrl, not(emptyOrNullString()));

            // Because log unchanged, response should be empty.
            // Actually they may contain several records due to deferred writing.
            response = client.get(nextUrl);
            assertThat(response.getStatus(), equalTo(200));
            assertThat(response.readEntity(String.class).length(), lessThan(logSize));
        }
    }

    @Test
    public void logFileNames() throws Exception {
        Response response = managementClient.get(URL_LOGNAMES, Map.of("instanceName", "server"));
        assertThat(response.getStatus(), equalTo(200));

        JSONArray logFileNames = getJsonArrayFrom(response, "InstanceLogFileNames");
        // Depends on the order of tests, there may be rolled file too.
        assertAll(
            () -> assertThat("InstanceLogFileNames", logFileNames.length(), greaterThanOrEqualTo(1)),
            () -> assertThat(logFileNames.get(0).toString(), startsWith("server.log"))
        );
    }

    @Test
    public void viewLogDetails() throws Exception {
        Response response = managementClient.get(URL_VIEW_LOG_DETAILS);
        assertThat(response.getStatus(), equalTo(200));

        JSONArray array = getJsonArrayFrom(response, "records");
        assertThat(array.length(), greaterThan(15));
    }

    static JSONObject readJsonObjectFrom(Response response) {
        return response.readEntity(JSONObject.class);
    }

    static JSONArray getJsonArrayFrom(Response response, String name) throws Exception {
        return readJsonObjectFrom(response).getJSONArray(name);
    }

    private static final class ViewLogClient extends DomainAdminRestClient {

        public ViewLogClient() {
            super("", TEXT_PLAIN);
        }

        @Override
        public Response get(String url) {
            if (url.startsWith("/")) {
                url = getBaseAdminUrl() + CONTEXT_ROOT_MANAGEMENT + url;
            }
            return super.get(url);
        }
    }
}
