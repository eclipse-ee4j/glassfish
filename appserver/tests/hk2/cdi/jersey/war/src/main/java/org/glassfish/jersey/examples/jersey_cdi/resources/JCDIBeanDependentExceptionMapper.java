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

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.ManagedBean;
import jakarta.annotation.PostConstruct;

import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Provider
@ManagedBean
public class JCDIBeanDependentExceptionMapper implements ExceptionMapper<JDCIBeanDependentException> {

    // TODO: this should be using proxiable injection support
    private @Context jakarta.inject.Provider<UriInfo> uiFieldInjectProvider;

    // TODO: this should be using proxiable injection support
    private @Context jakarta.inject.Provider<ResourceContext> rcProvider;

    private UriInfo uiMethodInject;

    @Context
    public void set(UriInfo ui) {
        this.uiMethodInject = ui;
    }

    @PostConstruct
    public void postConstruct() {
        Logger.getLogger(JCDIBeanDependentExceptionMapper.class.getName()).log(Level.INFO,
                "In post construct " + this);

        if (uiFieldInjectProvider.get() == null || uiMethodInject == null || rcProvider.get() == null) {
            throw new IllegalStateException();
        }
    }


    @Override
    public Response toResponse(JDCIBeanDependentException exception) {

        if (uiFieldInjectProvider.get() == null || uiMethodInject == null || rcProvider.get() == null) {
            throw new IllegalStateException();
        }

        return Response.serverError().entity("JDCIBeanDependentException").build();
    }
}
