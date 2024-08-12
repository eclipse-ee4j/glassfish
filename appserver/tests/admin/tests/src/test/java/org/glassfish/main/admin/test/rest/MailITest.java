/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.glassfish.main.itest.tools.RandomGenerator.generateRandomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class MailITest extends RestTestBase {

    private static final String URL_CREATE_MAIL_RESOURCE = "/domain/resources/create-mail-resource";

    private static final String URL_DELETE_MAIL_RESOURCE = "/domain/resources/delete-mail-resource";

    private static final String URL_LIST_MAIL_RESOURCES = "/domain/resources/list-mail-resources";

    @Test
    public void testCreateAndDeleteMailSession() {
        String sessionId = "MailSession" + generateRandomString();

        // Create mail session
        Map<String, String> newResource = Map.of(
            "id", sessionId,
            "host", "mail.example.org",
            "from", "test@example.org",
            "user", "testUser"
        );
        Response response = managementClient.post(URL_CREATE_MAIL_RESOURCE, newResource);
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.get(URL_LIST_MAIL_RESOURCES);
        assertThat(response.getStatus(), equalTo(200));

        List<String> mailSessions = getCommandResults(response);
        // Exactly one item
        assertThat(mailSessions, contains(sessionId));

        // Delete mail session
        response = managementClient.delete(URL_DELETE_MAIL_RESOURCE, Map.of("id", sessionId));
        assertThat(response.getStatus(), equalTo(200));
    }
}
