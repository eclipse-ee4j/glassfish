/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.ws.rs.core.Response;

import java.util.Map;

import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author jasonlee
 */
public class MetadataITest extends RestTestBase {
    private static final String URL_CONFIG = "/domain/configs/config.json";
    private static final String URL_UPTIMECOMMAND = "/domain/uptime.json";

    @Test
    public void configParameterTest() {
        Response response = managementClient.options(URL_CONFIG);
        assertEquals(200, response.getStatus());
        // Really dumb test.  Should be good enough for now

        Map extraProperties = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        assertNotNull(extraProperties);

        // Another dumb test to make sure that "name" shows up on the HTML page
        response = managementClient.get(URL_CONFIG);
        String data = response.readEntity(String.class);
        assertThat(data, stringContainsInOrder("extraProperties"));
    }

    @Test
    public void upTimeMetadaDataTest() {
        Response response = managementClient.options(URL_UPTIMECOMMAND);
        assertEquals(200, response.getStatus());

        Map<String, ?> extraProperties = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        assertNotNull(extraProperties);

        // Another dumb test to make sure that "extraProperties" shows up on the HTML page
        response = managementClient.get(URL_UPTIMECOMMAND);
        String resp = response.readEntity(String.class);
        assertThat(resp, stringContainsInOrder("extraProperties"));
        // test to see if we get the milliseconds parameter description which is an
        //optional param metadata for the uptime command
        assertThat(resp, stringContainsInOrder("milliseconds"));
        assertThat(resp, stringContainsInOrder("GET"));
    }
}
