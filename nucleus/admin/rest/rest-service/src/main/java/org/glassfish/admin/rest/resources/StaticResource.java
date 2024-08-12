/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import java.io.InputStream;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Path("/static/")
public class StaticResource {

    private final String PATH_INSIDE_JAR = "org/glassfish/admin/rest/static/";
    private final String mimes[] = { ".bmp", "image/bmp", ".bz", "application/x-bzip", ".bz2", "application/x-bzip2", ".css", "text/css",
            ".gz", "application/x-gzip", ".gzip", "application/x-gzip", ".htm", "text/html", ".html", "text/html", ".htmls", "text/html",
            ".htx", "text/html", ".ico", "image/x-icon", ".jpe", "image/jpeg", ".jpe", "image/pjpeg", ".jpeg", "image/jpeg", ".jpg",
            "image/jpeg", ".js", "application/x-javascript", ".javascript", "application/x-javascript", ".json", "application/json", ".png",
            "image/png", ".text", "text/plain", ".tif", "image/tiff", ".tiff", "image/tiff", ".xml", "text/xml", ".zip",
            "application/zip" };

    @GET
    @Path("{resource: .+}")
    public Response getPath(@PathParam("resource") String resource) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(PATH_INSIDE_JAR + resource);
        Response r = null;
        String m = getMime(resource);
        ResponseBuilder rp = Response.ok(is, m);
        rp.header("resource3-header", m);
        r = rp.build();
        return r;

    }

    private String getMime(String extension) {
        for (int i = 0; i < mimes.length; i = i + 2) {
            if (extension.endsWith(mimes[i])) {
                return mimes[i + 1];
            }
        }
        return "text/plain";
    }
}
