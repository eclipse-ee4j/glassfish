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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.glassfish.admin.rest.composite.LegacyCompositeResource;
import org.glassfish.admin.rest.model.BaseModel;

/**
 *
 * @author jdlee
 */
public class DummyResource extends LegacyCompositeResource {
    @GET
    public BaseModel getDummyData(@QueryParam("foo") String foo) {
        return compositeUtil.getModel(BaseModel.class);
    }

    @DELETE
    public Response deleteDummy(@PathParam("name") String name) {
        return Response.ok().build();
    }

    @Override
    public UriInfo getUriInfo() {
        return new DummyUriInfo();
    }
}
