/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.glassfish.admin.rest.logviewer.LogRecord;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.LogManager;

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
        LogNamesResource resource = injector.createAndInitialize(LogNamesResource.class);
        return resource;
    }

    @GET
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public String getJson(@QueryParam("logFileName") @DefaultValue("${com.sun.aas.instanceRoot}/logs/server.log") String logFileName,
            @QueryParam("startIndex") @DefaultValue("-1") long startIndex,
            @QueryParam("searchForward") @DefaultValue("false") boolean searchForward,
            @QueryParam("maximumNumberOfResults") @DefaultValue("40") int maximumNumberOfResults,
            @QueryParam("onlyLevel") @DefaultValue("false") boolean onlyLevel, @QueryParam("fromTime") @DefaultValue("-1") long fromTime,
            @QueryParam("toTime") @DefaultValue("-1") long toTime, @QueryParam("logLevel") @DefaultValue("INFO") String logLevel,
            @QueryParam("anySearch") @DefaultValue("") String anySearch, @QueryParam("listOfModules") String listOfModules, //default value is empty for List
            @QueryParam("instanceName") @DefaultValue("") String instanceName) throws IOException {

        return getWithType(logFileName, startIndex, searchForward, maximumNumberOfResults, fromTime, toTime, logLevel, onlyLevel, anySearch,
                listOfModules, instanceName, "json");

    }

    @GET
    @Produces({ MediaType.APPLICATION_XML })
    public String getXML(@QueryParam("logFileName") @DefaultValue("${com.sun.aas.instanceRoot}/logs/server.log") String logFileName,
            @QueryParam("startIndex") @DefaultValue("-1") long startIndex,
            @QueryParam("searchForward") @DefaultValue("false") boolean searchForward,
            @QueryParam("maximumNumberOfResults") @DefaultValue("40") int maximumNumberOfResults,
            @QueryParam("onlyLevel") @DefaultValue("true") boolean onlyLevel, @QueryParam("fromTime") @DefaultValue("-1") long fromTime,
            @QueryParam("toTime") @DefaultValue("-1") long toTime, @QueryParam("logLevel") @DefaultValue("INFO") String logLevel,
            @QueryParam("anySearch") @DefaultValue("") String anySearch, @QueryParam("listOfModules") String listOfModules, //default value is empty for List,
            @QueryParam("instanceName") @DefaultValue("") String instanceName) throws IOException {

        return getWithType(logFileName, startIndex, searchForward, maximumNumberOfResults, fromTime, toTime, logLevel, onlyLevel, anySearch,
                listOfModules, instanceName, "xml");

    }

    private String getWithType(String logFileName, long startIndex, boolean searchForward, int maximumNumberOfResults, long fromTime,
            long toTime, String logLevel, boolean onlyLevel, String anySearch, String listOfModules, String instanceName, String type)
            throws IOException {
        if (habitat.getService(LogManager.class) == null) {
            //the logger service is not install, so we cannot rely on it.
            //return an error
            throw new IOException("The GlassFish LogManager Service is not available. Not installed?");
        }

        List<String> modules = new ArrayList<>();
        if ((listOfModules != null) && !listOfModules.isEmpty()) {
            modules.addAll(Arrays.asList(listOfModules.split(",")));

        }

        Properties nameValueMap = new Properties();

        boolean sortAscending = true;
        if (!searchForward) {
            sortAscending = false;
        }
        LogFilter logFilter = habitat.getService(LogFilter.class);
        if (instanceName.isEmpty()) {
            final AttributeList result = logFilter.getLogRecordsUsingQuery(logFileName, startIndex, searchForward,
                sortAscending, maximumNumberOfResults, fromTime == -1 ? null : Instant.ofEpochMilli(fromTime),
                toTime == -1 ? null : Instant.ofEpochMilli(toTime), logLevel, onlyLevel, modules, nameValueMap,
                anySearch);
            return convertQueryResult(result, type);
        } else {
            final AttributeList result = logFilter.getLogRecordsUsingQuery(logFileName, startIndex, searchForward,
                sortAscending, maximumNumberOfResults, fromTime == -1 ? null : Instant.ofEpochMilli(fromTime),
                toTime == -1 ? null : Instant.ofEpochMilli(toTime), logLevel, onlyLevel, modules, nameValueMap,
                anySearch, instanceName);
            return convertQueryResult(result, type);
        }

    }

    private <T> List<T> asList(final Object list) {
        return List.class.cast(list);
    }

    /*    private String quoted(String s) {
        return "\"" + s + "\"";
    }*/

    private String convertQueryResult(final AttributeList queryResult, String type) {
        // extract field descriptions into a String[]
        StringBuilder sb = new StringBuilder();
        String sep = "";
        if (type.equals("json")) {
            sb.append("{\"records\": [");
        } else {
            sb.append("<records>\n");
        }

        if (queryResult.size() > 0) {
            final AttributeList fieldAttrs = (AttributeList) ((Attribute) queryResult.get(0)).getValue();
            String[] fieldHeaders = new String[fieldAttrs.size()];
            for (int i = 0; i < fieldHeaders.length; ++i) {
                final Attribute attr = (Attribute) fieldAttrs.get(i);
                fieldHeaders[i] = (String) attr.getValue();
            }

            List<List<Serializable>> srcRecords = asList(((Attribute) queryResult.get(1)).getValue());

            // extract every record
            for (List<Serializable> record : srcRecords) {
                assert (record.size() == fieldHeaders.length);
                //Serializable[] fieldValues = new Serializable[fieldHeaders.length];

                LogRecord rec = new LogRecord();
                int fieldIdx = 0;
                rec.setRecordNumber(((Long) record.get(fieldIdx++)).longValue());
                rec.setLoggedDateTime((Date) record.get(fieldIdx++));
                rec.setLoggedLevel((String) record.get(fieldIdx++));
                rec.setProductName((String) record.get(fieldIdx++));
                rec.setLoggerName((String) record.get(fieldIdx++));
                rec.setNameValuePairs((String) record.get(fieldIdx++));
                rec.setMessageID((String) record.get(fieldIdx++));
                rec.setMessage((String) record.get(fieldIdx++));
                if (type.equals("json")) {
                    sb.append(sep);
                    sb.append(rec.toJSON());
                    sep = ",";
                } else {
                    sb.append(rec.toXML());

                }
            }
        }

        if (type.equals("json")) {
            sb.append("]}\n");
        } else {
            sb.append("\n</records>\n");

        }

        return sb.toString();
    }
}
