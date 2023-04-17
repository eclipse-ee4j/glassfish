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

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.logviewer.LogRecord;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.LogManager;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static javax.xml.stream.XMLOutputFactory.newDefaultFactory;

/**
 * REST resource to get Log records simple wrapper around internal LogFilter query class
 *
 * @author ludovic Champenois
 */
public class StructuredLogViewerResource {

    protected ServiceLocator habitat = Globals.getDefaultBaseServiceLocator();

    @Context
    protected ServiceLocator injector;

    @Path("lognames/")
    public LogNamesResource getLogNamesResource() {
        return injector.createAndInitialize(LogNamesResource.class);
    }

    @GET
    @Produces("text/plain; qs=0.5")
    public Response getViewLogDetailsText(@BeanParam Params params) throws Exception {
        return getViewLogDetails(params, TEXT_PLAIN);
    }

    @GET
    @Produces("application/json; qs=1")
    public Response getViewLogDetailsJson(@BeanParam Params params) throws Exception {
        return getViewLogDetails(params, APPLICATION_JSON);
    }

    @GET
    @Produces("application/xml; qs=0.75")
    public Response getViewLogDetailsXml(@BeanParam Params params) throws Exception {
        return getViewLogDetails(params, APPLICATION_XML);
    }

    private Response getViewLogDetails(Params params, String type) throws Exception {
        if (habitat.getService(LogManager.class) == null) {
            // the logger service is not install, so we cannot rely on it.
            throw new IOException("The GlassFish LogManager Service is not available. Not installed?");
        }

        List<String> modules = new ArrayList<>();
        if (params.getListOfModules() != null && !params.getListOfModules().isEmpty()) {
            modules.addAll(Arrays.asList(params.getListOfModules().split(",")));
        }

        Properties nameValueMap = new Properties();

        LogFilter logFilter = habitat.getService(LogFilter.class);

        boolean sortAscending = params.isSearchForward();
        AttributeList result;
        if (params.getInstanceName().isEmpty()) {
            result = logFilter.getLogRecordsUsingQuery(params.getLogFileName(), params.getStartIndex(),
                    params.isSearchForward(), sortAscending, params.getMaximumNumberOfResults(),
                    params.getFromTime() == -1 ? null : Instant.ofEpochMilli(params.getFromTime()),
                    params.getToTime() == -1 ? null : Instant.ofEpochMilli(params.getToTime()),
                    params.getLogLevel(), params.isOnlyLevel(), modules, nameValueMap, params.getAnySearch());
        } else {
            result = logFilter.getLogRecordsUsingQuery(params.getLogFileName(), params.getStartIndex(),
                    params.isSearchForward(), sortAscending, params.getMaximumNumberOfResults(),
                    params.getFromTime() == -1 ? null : Instant.ofEpochMilli(params.getFromTime()),
                    params.getToTime() == -1 ? null : Instant.ofEpochMilli(params.getToTime()),
                    params.getLogLevel(), params.isOnlyLevel(), modules, nameValueMap, params.getAnySearch(), params.getInstanceName());
        }
        return convertQueryResult(result, type);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> asList(final Object list) {
        return (List<T>) list;
    }

    private Response convertQueryResult(final AttributeList queryResult, String type) throws Exception {
        Object entity;

        List<List<Serializable>> logRecords = asList(((Attribute) queryResult.get(1)).getValue());

        switch (type) {
            case APPLICATION_JSON:
                JSONArray logDetails = new JSONArray();

                for (List<Serializable> logRecord : logRecords) {
                    logDetails.put(new LogRecord(logRecord).toJSONObject());
                }

                entity = new JSONObject().put("records", logDetails);
                break;
            case APPLICATION_XML:
                Writer xml = new StringWriter();

                XMLStreamWriter writer = newDefaultFactory().createXMLStreamWriter(xml);
                try {
                    writer.writeStartElement("records");
                    for (List<Serializable> logRecord : logRecords) {
                        new LogRecord(logRecord).writeXml(writer);
                    }
                    writer.writeEndElement();
                } finally {
                    writer.close();
                }

                entity = xml;
                break;
            case TEXT_PLAIN:
                StringBuilder sb = new StringBuilder();

                String lineSeparator = "";
                for (List<Serializable> logRecord : logRecords) {
                    sb.append(lineSeparator);
                    new LogRecord(logRecord).writeCsv(sb);
                    lineSeparator = "\r\n";
                }

                entity = sb;
                break;
            default:
                // should not reach here
                return Response.status(UNSUPPORTED_MEDIA_TYPE).build();
        }

        return Response.ok(entity.toString(), type).build();
    }

    private static final class Params {

        @QueryParam("logFileName")
        @DefaultValue("${com.sun.aas.instanceRoot}/logs/server.log")
        private String logFileName;

        @QueryParam("startIndex")
        @DefaultValue("-1")
        private long startIndex;

        @QueryParam("searchForward")
        @DefaultValue("false")
        private boolean searchForward;

        @QueryParam("maximumNumberOfResults")
        @DefaultValue("40")
        private int maximumNumberOfResults;

        @QueryParam("onlyLevel")
        @DefaultValue("false")
        private boolean onlyLevel;

        @QueryParam("fromTime")
        @DefaultValue("-1")
        private long fromTime;

        @QueryParam("toTime")
        @DefaultValue("-1")
        private long toTime;

        @QueryParam("logLevel")
        @DefaultValue("INFO")
        private String logLevel;

        @QueryParam("anySearch")
        @DefaultValue("")
        private String anySearch;

        @QueryParam("listOfModules")
        private String listOfModules; //default value is empty for List

        @QueryParam("instanceName")
        @DefaultValue("")
        private String instanceName;

        public String getLogFileName() {
            return logFileName;
        }

        public long getStartIndex() {
            return startIndex;
        }

        public boolean isSearchForward() {
            return searchForward;
        }

        public int getMaximumNumberOfResults() {
            return maximumNumberOfResults;
        }

        public boolean isOnlyLevel() {
            return onlyLevel;
        }

        public long getFromTime() {
            return fromTime;
        }

        public long getToTime() {
            return toTime;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public String getAnySearch() {
            return anySearch;
        }

        public String getListOfModules() {
            return listOfModules;
        }

        public String getInstanceName() {
            return instanceName;
        }
    }
}
