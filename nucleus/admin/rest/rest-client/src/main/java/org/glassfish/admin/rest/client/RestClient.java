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

package org.glassfish.admin.rest.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.filter.CsrfProtectionFilter;

/**
 *
 * @author jasonlee
 */
public class RestClient {
    protected String host;
    protected int port;
    protected Client client;
    protected boolean useSsl = false;

    public RestClient() {
        this("localhost", 4848, false, null, null);
    }

    public RestClient(String host, int port, boolean useSsl) {
        this(host, port, useSsl, null, null);
    }

    public RestClient(String host, int port, boolean useSsl, String user, String password) {
        this.host = host;
        this.port = port;
        this.useSsl = useSsl;
        client = ClientBuilder.newClient();

        client.register(new CsrfProtectionFilter());
        if (user != null) {
            client.register(HttpAuthenticationFeature.basic(user, password));
        }
    }

    public String getRestUrl() {
        return (useSsl ? "https" : "http") + "://" + host + ":" + port + "/management";
    }
}
