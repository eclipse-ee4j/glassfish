/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import java.util.logging.Level;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.adapter.LocatorBridge;
import org.glassfish.admin.rest.generator.ResourcesGenerator;
import org.glassfish.admin.rest.generator.TextResourcesGenerator;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
@Path("/generator/")
public class GeneratorResource {

    @Inject
    private ServiceLocator serviceLocator;

    @GET
    @Produces({ "text/plain" })
    public String get(@QueryParam("outputDir") String outputDir) {
        if (outputDir == null) {
            return "Please provide the outputDir query parameter.";
        }
        try {
            LocatorBridge locatorBridge = serviceLocator.getService(LocatorBridge.class);
            Dom dom = Dom.unwrap(locatorBridge.getRemoteLocator().<Domain>getService(Domain.class));
            DomDocument<?> document = dom.document;
            ConfigModel rootModel = dom.document.getRoot().model;

            ResourcesGenerator resourcesGenerator = new TextResourcesGenerator(outputDir, serviceLocator);
            resourcesGenerator.generateSingle(rootModel, document);
            resourcesGenerator.endGeneration();
            return "Code Generation done at: " + outputDir;
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
            return "Exception encountered during generation process: " + ex
                + "\nPlease look at server.log for more information.";
        }
    }
}
