/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.testing;

import org.codehaus.jettison.json.JSONObject;

import static org.glassfish.admin.rest.testing.Common.HEADER_LOCATION;
import static org.glassfish.admin.rest.testing.Common.HEADER_X_LOCATION;
import static org.glassfish.admin.rest.testing.Common.PROP_ITEM;

public class Response {
    private String method;
    private jakarta.ws.rs.core.Response jaxrsResponse;
    private String bodyAsString;

    public Response(String method, jakarta.ws.rs.core.Response jaxrsResponse) {
        this(method, jaxrsResponse, true);
    }

    public Response(String method, jakarta.ws.rs.core.Response jaxrsResponse, boolean readEntity) {
        this.method = method;
        this.jaxrsResponse = jaxrsResponse;
        if (readEntity) {
            // get the response body now in case the caller releases the connection before asking for the response body
            try {
                this.bodyAsString = this.jaxrsResponse.readEntity(String.class);
            } catch (Exception e) {
            }
        }
    }

    public jakarta.ws.rs.core.Response getJaxrsResponse() {
        return this.jaxrsResponse;
    }

    public String getMethod() {
        return this.method;
    }

    public int getStatus() {
        return getJaxrsResponse().getStatus();
    }

    public String getStringBody() {
        return this.bodyAsString;
    }

    public JSONObject getJsonBody() throws Exception {
        return new JSONObject(getStringBody());
    }

    public JSONObject getItem() throws Exception {
        return getJsonBody().getJSONObject(PROP_ITEM);
    }

    public String getLocationHeader() throws Exception {
        return getJaxrsResponse().getHeaderString(HEADER_LOCATION);
    }

    public String getXLocationHeader() throws Exception {
        return getJaxrsResponse().getHeaderString(HEADER_X_LOCATION);
    }
}
