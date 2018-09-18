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

import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Path("/jcdibean/per-request")
@RequestScoped
public class JCDIBeanPerRequestResource {

    private @Resource(name="injectedResource") int injectedResource = 0;

    private @Context UriInfo uiFieldInject;

    private @Context ResourceContext rc;

    private @QueryParam("x") String x;

    private UriInfo uiMethodInject;

    @Context
    public void set(UriInfo ui) {
        this.uiMethodInject = ui;
    }

    @PostConstruct
    public void postConstruct() {
        Logger.getLogger(JCDIBeanPerRequestResource.class.getName()).log(Level.INFO,
                "In post construct " + this +
                "; uiFieldInject: " + uiFieldInject + "; uiMethodInject: " + uiMethodInject);

        if (uiFieldInject == null || uiMethodInject == null || rc == null)
            throw new IllegalStateException();
    }

    @GET
    @Produces("text/plain")
    public String getMessage() {
        Logger.getLogger(JCDIBeanPerRequestResource.class.getName()).log(Level.INFO,
                "In getMessage " + this +
                "; uiFieldInject: " + uiFieldInject + "; uiMethodInject: " + uiMethodInject);

        if (uiFieldInject == null || uiMethodInject == null || rc == null)
            throw new IllegalStateException();

        return x + injectedResource++;
    }

    @PreDestroy
    public void preDestroy() {
        Logger.getLogger(JCDIBeanPerRequestResource.class.getName()).log(Level.INFO, "In pre destroy");
    }
}
