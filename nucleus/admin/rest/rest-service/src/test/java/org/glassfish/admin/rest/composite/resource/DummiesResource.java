/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.composite.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.glassfish.admin.rest.composite.LegacyCompositeResource;
import org.glassfish.admin.rest.composite.RestCollection;
import org.glassfish.admin.rest.composite.metadata.HelpText;
import org.glassfish.admin.rest.model.BaseModel;

/**
 *
 * @author jdlee
 */
@Path("dummies")
public class DummiesResource extends LegacyCompositeResource {

    @Override
    public UriInfo getUriInfo() {
        return new DummyUriInfo();
    }

    @GET
    public RestCollection<BaseModel> getDummyDataCollection(
            @QueryParam("type") @HelpText(bundle="org.glassfish.admin.rest.composite.HelpText", key="dummy.type") String type
            ) {
        RestCollection<BaseModel> rc = new RestCollection<BaseModel>();

        return rc;
    }

    @Path("id/{name}")
    public DummyResource getDummyData(@QueryParam("foo") String foo) {
        return getSubResource(DummyResource.class);
    }

    @POST
    public Response createDummy(BaseModel model) {
        return Response.ok().build();
    }
}
