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

package org.glassfish.admin.rest.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.codehaus.jettison.json.JSONException;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.sse.SseFeature;

/**
 * @author ludovic champenois ludo@dev.java.net Code moved from generated classes to here. Gen code inherits from this
 * template class that contains the logic for mapped commands RS Resources
 */
@Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5" })
public class TemplateCommandPostResource extends TemplateExecCommand {

    public TemplateCommandPostResource(String resourceName, String commandName, String commandMethod, String commandAction,
            String commandDisplayName, boolean isLinkedToParent) {
        super(resourceName, commandName, commandMethod, commandAction, commandDisplayName, isLinkedToParent);
    }

    // ---------------- POST

    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public Response processPostLegacyFormat(ParameterMap data) {
        if (data == null) {
            data = new ParameterMap();
        }
        if (data.containsKey("error")) {
            String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                    "Unable to parse the input entity. Please check the syntax.");
            throw new WebApplicationException(ResourceUtil.getResponse(400, /*parsing error*/ errorMessage, requestHeaders, uriInfo));
        }
        return super.executeCommandLegacyFormat(preprocessData(data));
    }

    @POST
    @Consumes(Constants.MEDIA_TYPE_JSON)
    @Produces(Constants.MEDIA_TYPE_JSON + ";qs=0.5")
    public CommandResult processPost(ParameterMap data) {
        if (data == null) {
            data = new ParameterMap();
        }
        if (data.containsKey("error")) {
            String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                    "Unable to parse the input entity. Please check the syntax.");
            throw new WebApplicationException(ResourceUtil.getResponse(400, /*parsing error*/ errorMessage, requestHeaders, uriInfo));
        }
        return super.executeCommand(preprocessData(data));
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(FormDataMultiPart formData) {
        /* data passed to the generic command running
         *
         * */
        return processPostLegacyFormat(createDataBasedOnForm(formData));

    }

    //Handle POST request without any entity(input).
    //Do not care what the Content-Type is.
    @POST
    public Response processPost() {
        try {
            return processPostLegacyFormat(new ParameterMap());
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // ---------------- SSE POST

    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    @Produces(SseFeature.SERVER_SENT_EVENTS + ";qs=0.5")
    public Response processSsePost(ParameterMap data) {
        if (data == null) {
            data = new ParameterMap();
        }
        if (data.containsKey("error")) {
            String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                    "Unable to parse the input entity. Please check the syntax.");
            throw new WebApplicationException(ResourceUtil.getResponse(400, /*parsing error*/ errorMessage, requestHeaders, uriInfo));
        }
        return super.executeCommandAsSse(preprocessData(data));
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(SseFeature.SERVER_SENT_EVENTS + ";qs=0.5")
    public Response ssePost(FormDataMultiPart formData) {
        return processSsePost(createDataBasedOnForm(formData));
    }

    @POST
    @Produces(SseFeature.SERVER_SENT_EVENTS + ";qs=0.5")
    public Response processSsePost() {
        try {
            return processSsePost(new ParameterMap());
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    // ---------------- GET

    @GET
    public ActionReportResult getLegacyFormat() {
        return optionsLegacyFormat();
    }

    @GET
    @Produces(Constants.MEDIA_TYPE_JSON + ";qs=0.5")
    public String get() {
        try {
            return options();
        } catch (JSONException ex) {
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private ParameterMap preprocessData(final ParameterMap data) {
        processCommandParams(data);
        adjustParameters(data);
        purgeEmptyEntries(data);
        return data;
    }

    private static ParameterMap createDataBasedOnForm(FormDataMultiPart formData) {
        ParameterMap data = new ParameterMap();
        if (formData == null) {
            formData = new FormDataMultiPart();
        }
        try {
            // data passed to the generic command running
            Map<String, List<FormDataBodyPart>> m1 = formData.getFields();

            Set<String> ss = m1.keySet();
            for (String fieldName : ss) {
                for (FormDataBodyPart bodyPart : formData.getFields(fieldName)) {

                    if (bodyPart.getContentDisposition().getFileName() == null) {
                        data.add(fieldName, bodyPart.getValue());
                    } else {
                        //save it and mark it as delete on exit.
                        InputStream fileStream = bodyPart.getValueAs(InputStream.class);

                        //Use just the filename without complete path. File creation
                        //in case of remote deployment failing because fo this.
                        String fileName = bodyPart.getContentDisposition().getFileName();
                        if (fileName.contains("/")) {
                            fileName = Util.getName(fileName, '/');
                        } else {
                            if (fileName.contains("\\")) {
                                fileName = Util.getName(fileName, '\\');
                            }
                        }

                        File f = Util.saveTemporaryFile(fileName, fileStream);
                        //put only the local path of the file in the same field.
                        data.add(fieldName, f.getAbsolutePath());
                    }
                }
            }
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        } finally {
            formData.cleanup();
        }
        return data;

    }
}
