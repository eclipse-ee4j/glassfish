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

package org.glassfish.admin.rest.resources;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.results.ActionReportResult;

import org.glassfish.jersey.media.sse.SseFeature;

import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.api.admin.ParameterMap;

/**
 *
 * @author ludovic champenois ludo@dev.java.net Code moved from generated classes to here. Gen code inherits from this
 * template class that contains the logic for mapped commands RS Resources
 *
 */
@Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5" })
public class TemplateCommandDeleteResource extends TemplateExecCommand {

    public TemplateCommandDeleteResource(String resourceName, String commandName, String commandMethod, String commandAction,
            String commandDisplayName, boolean b) {
        super(resourceName, commandName, commandMethod, commandAction, commandDisplayName, b);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public Response processDeleteLegacyFormat(ParameterMap data) {
        if (data == null) {
            data = new ParameterMap();
        }
        if (data.containsKey("error")) {
            String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                    "Unable to parse the input entity. Please check the syntax.");
            throw new WebApplicationException(ResourceUtil.getResponse(400, /*parsing error*/ errorMessage, requestHeaders, uriInfo));
        }
        return executeCommandLegacyFormat(preprocessData(data));
    }

    @DELETE
    @Produces(Constants.MEDIA_TYPE_JSON + ";qs=0.5")
    public CommandResult processDelete(ParameterMap data) {
        if (data == null) {
            data = new ParameterMap();
        }
        if (data.containsKey("error")) {
            String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                    "Unable to parse the input entity. Please check the syntax.");
            throw new WebApplicationException(ResourceUtil.getResponse(400, /*parsing error*/ errorMessage, requestHeaders, uriInfo));
        }
        return executeCommand(preprocessData(data));
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    @Produces(SseFeature.SERVER_SENT_EVENTS + ";qs=0.5")
    public Response processDeleteSse(ParameterMap data) {
        if (data == null) {
            data = new ParameterMap();
        }
        if (data.containsKey("error")) {
            String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                    "Unable to parse the input entity. Please check the syntax.");
            throw new WebApplicationException(ResourceUtil.getResponse(400, /*parsing error*/ errorMessage, requestHeaders, uriInfo));
        }
        return executeCommandAsSse(preprocessData(data));
    }

    //    //Handle POST request without any entity(input).
    //    //Do not care what the Content-Type is.
    //    @DELETE
    //    @Produces({
    //        "text/html",
    //        MediaType.APPLICATION_JSON+";qs=0.5",
    //        MediaType.APPLICATION_XML+";qs=0.5"})
    //    public ActionReportResult processDelete() {
    //        try {
    //            return processDelete(new ParameterMap());
    //        } catch (Exception e) {
    //            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    //        }
    //    }
    //hack-1 : support delete method for html
    //Currently, browsers do not support delete method. For html media,
    //delete operations can be supported through POST. Redirect html
    //client POST request for delete operation to DELETE method.

    //In case of delete command reosurce, we will also create post method
    //which simply forwards the request to delete method. Only in case of
    //html client delete request is routed through post. For other clients
    //delete request is directly handled by delete method.
    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public Response hack(ParameterMap data) {
        if (data != null && data.containsKey("operation")) {
            List<String> l = data.get("operation");
            if (l.contains("__deleteoperation")) {

                data.remove("operation");
            }
        }
        return processDeleteLegacyFormat(data);
    }

    @GET
    public ActionReportResult getLegacyFormat() {
        return optionsLegacyFormat();
    }

    @GET
    @Produces(Constants.MEDIA_TYPE_JSON + ";qs=0.5")
    public String get() throws JSONException {
        return options();
    }

    private ParameterMap preprocessData(final ParameterMap data) {
        processCommandParams(data);
        addQueryString(uriInfo.getQueryParameters(), data);
        adjustParameters(data);
        purgeEmptyEntries(data);
        return data;
    }
}
