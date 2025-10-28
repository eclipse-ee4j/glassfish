/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.perf.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

public final class RestClientUtilities {

    public static WebTarget getWebTarget(URI restEndpoint, boolean verbose) {
        final ClientConfig clientCfg = new ClientConfig();
        clientCfg.register(new JacksonFeature());
        clientCfg.register(new ObjectMapper());
        if (verbose) {
            clientCfg.register(LoggingResponseFilter.class);
        }
        clientCfg.property(ClientProperties.FOLLOW_REDIRECTS, "true");
        final ClientBuilder builder = ClientBuilder.newBuilder().withConfig(clientCfg);
        return builder.build().target(restEndpoint);
    }
}
