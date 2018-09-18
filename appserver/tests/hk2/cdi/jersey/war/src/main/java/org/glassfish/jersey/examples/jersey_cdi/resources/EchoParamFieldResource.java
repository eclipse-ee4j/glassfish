/*
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

package org.glassfish.jersey.examples.jersey_cdi.resources;

import java.util.List;
import java.util.Set;

import javax.annotation.ManagedBean;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Shows injection of path and query parameters into the fields of a managed bean.
 *
 * @author robc
 */
@ManagedBean
@Path("echofield/{b}")
public class EchoParamFieldResource {

    @GET
    @Produces("text/plain")
    public String get(@QueryParam("a") String a) {
        return "ECHO " + a + " " + b;
    }

    @PathParam("b") String b;
    @QueryParam("a") String a;
    @PathParam("b") Set<String> bs;
    @PathParam("b") List<String> bl;
    @PathParam("b") @Encoded String be;
    @QueryParam("a") Set<String> as;
    @QueryParam("a") @Encoded String ae;
    @QueryParam("a") @DefaultValue("boo") String ad;
    @QueryParam("c") @DefaultValue("boo") String c;
}
