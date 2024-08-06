/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.applib.webapp;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.glassfish.main.test.app.applib.lib.LibraryResource;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/")
public class LibraryEndpoint {

    @GET
    @Path("version")
    @Produces(TEXT_PLAIN)
    public Response getVersion() {
        try {
            LibraryResource resource = new LibraryResource();
            String version = resource.getVersion();
            return Response.ok(version).build();
        } catch (Throwable t) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("uuid")
    @Produces(TEXT_PLAIN)
    public Response getUUID() {
        try {
            LibraryResource resource = new LibraryResource();
            String uuid = resource.getUUID();
            return Response.ok(uuid).build();
        } catch (Throwable t) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("resource")
    @Produces(TEXT_PLAIN)
    public Response findResource() throws IOException {
        URL resource = getClass().getResource("/org/glassfish/main/test/app/applib/Version.properties");
        if (resource == null) {
            return Response.serverError().build();
        }

        Properties version = new Properties();
        try (InputStream inputStream = resource.openStream()) {
            version.load(inputStream);
        }
        return Response.ok(version.toString()).build();
    }
}
