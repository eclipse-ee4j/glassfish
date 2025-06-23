/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import java.util.Arrays;
import java.util.Map;

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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
        AsadminResult result = getAsadmin().exec(60_000, "restart-domain");
        assertThat(result, asadminOK());
    }

    @Test
    public void viewLog() {
        try (ViewLogClient client = new ViewLogClient()) {
            // Read entire log
            Response response = client.get(URL_VIEW_LOG);
            assertThat(response.getStatus(), equalTo(200));

            String content = response.readEntity(String.class).trim();
            // Should not be empty
            assertThat(content, not(emptyString()));

            // Get the entire URL to return the changes since the last call
            String nextAppend = response.getHeaderString("X-Text-Append-Next");
            assertThat(nextAppend, not(emptyOrNullString()));

            // Because log unchanged, response should be empty.
            // In rare cases it may contain a few records.
            response = client.get(nextAppend);
            assertThat(response.getStatus(), equalTo(200));

            String newNextAppend = response.getHeaderString("X-Text-Append-Next");
            assertThat(newNextAppend, not(emptyOrNullString()));

            String newContent = response.readEntity(String.class).trim();
            if (sameURIs(nextAppend, newNextAppend)) {
                assertThat(newContent, emptyString());
            } else {
                assertThat(content, not(containsString(newContent.lines().findFirst().orElseThrow())));
            }
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
        assertThat(array.length(), greaterThan(14));
    }

    static JSONObject readJsonObjectFrom(Response response) {
        return response.readEntity(JSONObject.class);
    }

    static JSONArray getJsonArrayFrom(Response response, String name) throws Exception {
        return readJsonObjectFrom(response).getJSONArray(name);
    }

    private static boolean sameURIs(String uri1, String uri2) {
        // Fast path
        if (uri1.equals(uri2)) {
            return true;
        }

        // Compare only query params, because they may have
        // a different order for each call (theoretically) and
        // different value of the 'start' param.
        // The rest parts of the URI is unchanged.
        String[] queryParams1 = uri1.substring(uri1.indexOf('?') + 1).split("&");
        String[] queryParams2 = uri2.substring(uri2.indexOf('?') + 1).split("&");

        Arrays.sort(queryParams1);
        Arrays.sort(queryParams2);

        return Arrays.equals(queryParams1, queryParams2);
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
