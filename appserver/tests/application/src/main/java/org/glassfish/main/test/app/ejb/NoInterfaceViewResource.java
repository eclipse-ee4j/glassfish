/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.ejb;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/")
@Produces(TEXT_PLAIN)
public class NoInterfaceViewResource {

    @EJB
    private NoInterfaceViewEJB bean;

    @GET
    @Path("object")
    public Object getObject() {
        return bean.getObject();
    }

    @GET
    @Path("boolean")
    public boolean getBoolean() {
        return bean.getBoolean();
    }

    @GET
    @Path("byte")
    public byte getByte() {
        return bean.getByte();
    }

    @GET
    @Path("short")
    public short getShort() {
        return bean.getShort();
    }

    @GET
    @Path("int")
    public int getInt() {
        return bean.getInt();
    }

    @GET
    @Path("long")
    public long getLong() {
        return bean.getLong();
    }

    @GET
    @Path("float")
    public float getFloat() {
        return bean.getFloat();
    }

    @GET
    @Path("double")
    public double getDouble() {
        return bean.getDouble();
    }

    @GET
    @Path("void")
    public Response callVoidMethod() {
        bean.callVoidMethod();
        return Response.noContent().build();
    }

    @GET
    @Path("package-private")
    public Response callPackagePrivateMethod() {
        try {
            bean.callPackagePrivateMethod();
        } catch (EJBException e) {
            return Response.ok(e.getMessage()).build();
        }
        return Response.noContent().build();
    }

    @GET
    @Path("protected")
    public Response callProtectedMethod() {
        try {
            bean.callProtectedMethod();
        } catch (EJBException e) {
            return Response.ok(e.getMessage()).build();
        }
        return Response.noContent().build();
    }

    @GET
    @Path("static")
    public Response callNonPublicStaticMethod() {
        bean.callNonPublicStaticMethod();
        return Response.noContent().build();
    }
}
