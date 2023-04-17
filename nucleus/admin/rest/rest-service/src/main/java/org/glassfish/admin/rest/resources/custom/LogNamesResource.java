/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.rest.resources.custom;

import com.sun.enterprise.server.logging.logviewer.backend.LogFilter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.LogManager;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static javax.xml.stream.XMLOutputFactory.newDefaultFactory;

/**
 * REST resource to get Log Names simple wrapper around internal LogFilter class
 *
 * @author ludovic Champenois
 */
public class LogNamesResource {

    protected ServiceLocator habitat = Globals.getDefaultBaseServiceLocator();

    @GET
    @Produces("text/plain; qs=0.5")
    public Response getLogNamesText(@QueryParam("instanceName") String instanceName) throws Exception {
        return getLogNames(instanceName, TEXT_PLAIN);
    }

    @GET
    @Produces("application/json; qs=1")
    public Response getLogNamesJson(@QueryParam("instanceName") String instanceName) throws Exception {
        return getLogNames(instanceName, APPLICATION_JSON);
    }

    @GET
    @Produces("application/xml; qs=0.75")
    public Response getLogNamesXml(@QueryParam("instanceName") String instanceName) throws Exception {
        return getLogNames(instanceName, APPLICATION_XML);
    }

    private Response getLogNames(String instanceName, String type) throws Exception {
        if (habitat.getService(LogManager.class) == null) {
            // the logger service is not install, so we cannot rely on it.
            // return an error
            throw new IOException("The GlassFish LogManager Service is not available. Not installed?");
        }

        LogFilter logFilter = habitat.getService(LogFilter.class);

        return convertQueryResult(logFilter.getInstanceLogFileNames(instanceName), type);
    }

    private Response convertQueryResult(List<String> files, String type) throws Exception {
        Object entity;

        switch (type) {
            case APPLICATION_JSON:
                entity = new JSONObject().put("InstanceLogFileNames", files);
                break;
            case APPLICATION_XML:
                Writer xml = new StringWriter();

                XMLStreamWriter writer = newDefaultFactory().createXMLStreamWriter(xml);
                try {
                    writer.writeStartElement("InstanceLogFileNames");
                    for (String file : files) {
                        writer.writeEmptyElement(file);
                    }
                    writer.writeEndElement();
                } finally {
                    writer.close();
                }

                entity = xml;
                break;
            case TEXT_PLAIN:
                StringBuilder sb = new StringBuilder();

                String separator = "";
                // extract every record
                for (String file : files) {
                    sb.append(separator);
                    sb.append(file);
                    separator = ",";
                }

                entity = sb;
                break;
            default:
                // should not reach here
                return Response.status(UNSUPPORTED_MEDIA_TYPE).build();
        }

        return Response.ok(entity.toString(), type).build();
    }
}
