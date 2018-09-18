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

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Path("/jcdibean/dependent/singleton")
@ApplicationScoped
@ManagedBean
public class JCDIBeanDependentSingletonResource {

    private @Resource(name="injectedResource") int injectedResource = 0;

    // TODO: this should be using proxiable injection support
    private @Context javax.inject.Provider<UriInfo> uiFieldInjectProvider;

    // TODO: this should be using proxiable injection support
    private @Context javax.inject.Provider<ResourceContext> rcProvider;

    private UriInfo uiMethodInject;

    @Context
    public void set(UriInfo ui) {
        this.uiMethodInject = ui;
    }

    @PostConstruct
    public void postConstruct() {
        Logger.getLogger(JCDIBeanDependentSingletonResource.class.getName()).log(Level.INFO,
                "In post construct " + this);

        if (uiFieldInjectProvider.get() == null || uiMethodInject == null || rcProvider.get() == null) {
            throw new IllegalStateException();
        }
    }

    @GET
    @Produces("text/plain")
    public String getMessage() {
        Logger.getLogger(JCDIBeanDependentSingletonResource.class.getName()).log(Level.INFO,
                "In getMessage " + this +
                "; uiFieldInject: " + uiFieldInjectProvider.get() + "; uiMethodInject: " + uiMethodInject);

        if (uiFieldInjectProvider.get() == null || uiMethodInject == null || rcProvider.get() == null) {
            throw new IllegalStateException();
        }

        return Integer.toString(injectedResource++);
    }

    @Path("exception")
    public String getException() {
        throw new JDCIBeanDependentException();
    }

    @PreDestroy
    public void preDestroy() {
        Logger.getLogger(JCDIBeanDependentSingletonResource.class.getName()).log(Level.INFO, "In pre destroy " + this);
    }
}
