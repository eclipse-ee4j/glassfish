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

package org.glassfish.admin.rest.composite;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

import org.codehaus.jettison.json.JSONException;
import org.glassfish.admin.rest.OptionsCapable;
import org.glassfish.admin.rest.composite.metadata.DefaultsGenerator;
import org.glassfish.admin.rest.composite.metadata.RestResourceMetadata;
import org.glassfish.admin.rest.model.ResponseBody;
import org.glassfish.admin.rest.model.RestModelResponseBody;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.api.admin.ParameterMap;

/**
 * This is the base class for all legacy composite resources. It provides all of the basic configuration and utilities
 * needed by composites. For top-level resources, the <code>@Path</code> and <code>@Service</code> annotations are still
 * required, though, in order for the resource to be located and configured properly.
 *
 * @author jdlee
 */
public abstract class LegacyCompositeResource extends CompositeResource implements DefaultsGenerator, OptionsCapable {

    @Override
    public UriInfo getUriInfo() {
        return uriInfo;
    }

    @Override
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    /**
     * This method will handle any OPTIONS requests for composite resources.
     *
     * @return
     * @throws JSONException
     */
    @OPTIONS
    public String options() throws JSONException {
        RestResourceMetadata rrmd = new RestResourceMetadata(this);
        return rrmd.toJson().toString(Util.getFormattingIndentLevel());
    }

    @Override
    public Object getDefaultValue(String propertyName) {
        return null;
    }

    protected Response legacyCreated(String name, String message, RestModel model) {
        RestModelResponseBody rb = legacyResponseBody(RestModel.class);
        rb.setEntity(model);
        rb.addSuccess(message);
        return legacyCreated(getChildItemUri(name), rb);
    }

    protected Response legacyCreated(URI location, RestModelResponseBody responseBody) {
        return Response.status(Status.CREATED).header("Location", location).entity(responseBody).build();
    }

    protected Response legacyUpdated(String message, RestModel model) {
        RestModelResponseBody<RestModel> rb = legacyResponseBody(RestModel.class);
        rb.setEntity(model);
        rb.addSuccess(message);
        return legacyUpdated(rb);
    }

    protected Response legacyUpdated(ResponseBody responseBody) {
        return Response.ok().entity(responseBody).build();
    }

    protected Response legacyDeleted(String message) {
        return deleted(responseBody().addSuccess(message));
    }

    protected Response legacyDeleted(ResponseBody responseBody) {
        return Response.ok().entity(responseBody).build();
    }

    protected Response legacyAccepted(String command, ParameterMap parameters) {
        return legacyAccepted(command, parameters, null);
    }

    protected Response legacyAccepted(String command, ParameterMap parameters, URI childUri) {
        URI jobUri = launchDetachedCommand(command, parameters);
        ResponseBuilder rb = Response.status(Response.Status.ACCEPTED).header("Location", jobUri);
        if (childUri != null) {
            rb.header("X-Location", childUri);
        }
        return rb.build();
    }

    protected <T extends RestModel> RestModelResponseBody<T> legacyResponseBody(Class<T> modelIface) {
        return restModelResponseBody(modelIface);
    }
}
