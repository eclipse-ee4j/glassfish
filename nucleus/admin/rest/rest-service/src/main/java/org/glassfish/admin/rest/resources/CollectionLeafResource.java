/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.admin.rest.utils.Util.decode;
import static org.glassfish.admin.rest.utils.Util.upperCaseFirstLetter;

/**
 * @author Rajeshwar Patil
 */
@Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5",
        MediaType.APPLICATION_FORM_URLENCODED + ";qs=0.5" })
public abstract class CollectionLeafResource extends AbstractResource {
    protected List<String> entity;
    protected Dom parent;
    protected String tagName;
    protected String target;
    protected String profiler = "false";

    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CollectionLeafResource.class);

    /** Creates a new instance of xxxResource */
    public CollectionLeafResource() {
    }

    public void setEntity(List<String> p) {
        entity = p;
    }

    public List<String> getEntity() {
        return entity;
    }

    public void setParentAndTagName(Dom parent, String tagName) {
        this.parent = parent;
        this.tagName = tagName;
        if (parent != null) {
            entity = parent.leafElements(tagName);

            if (parent.getImplementationClass().equals(JavaConfig.class)) {
                target = parent.parent().attribute("name");
            } else {
                target = parent.parent().parent().attribute("name");
                profiler = "true";
            }
        }
    }

    @GET
    public Response get(@QueryParam("expandLevel") @DefaultValue("1") int expandLevel) {
        if (getEntity() == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return Response.ok(buildActionReportResult()).build();
    }

    @POST //create
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public Response create(HashMap<String, String> data) throws TransactionFailure {
        //hack-1 : support delete method for html
        //Currently, browsers do not support delete method. For html media,
        //delete operations can be supported through POST. Redirect html
        //client POST request for delete operation to DELETE method.
        if ((data.containsKey("operation")) && (data.get("operation").equals("__deleteoperation"))) {
            data.remove("operation");
            return delete(data);
        }

        String postCommand = getPostCommand();
        Map<String, String> payload = null;
        Map<String, String> existing = null;

        if (isJvmOptions(postCommand)) {
            existing = deleteExistingOptions();
            payload = processData(data);
        } else {
            payload = data;
        }

        // Create all JVM options.
        Response response = runCommand(postCommand, payload, "rest.resource.create.message", "\"{0}\" created successfully.",
                "rest.resource.post.forbidden", "POST on \"{0}\" is forbidden.");
        if (response.getStatus() != 200) {
            // If creating JVM options is error, restore JVM options with exsiting.
            payload = processData(existing);
            runCommand(postCommand, payload, "rest.resource.create.message", "\"{0}\" created successfully.",
                    "rest.resource.post.forbidden", "POST on \"{0}\" is forbidden.");
        }
        return response;
    }

    @PUT //create
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    @Deprecated
    public Response add(HashMap<String, String> data) throws TransactionFailure {
        String postCommand = getPostCommand();
        Map<String, String> payload = null;

        if (isJvmOptions(postCommand)) {
            payload = processData(data);
        } else {
            payload = data;
        }

        return runCommand(postCommand, payload, "rest.resource.create.message", "\"{0}\" created successfully.",
                "rest.resource.post.forbidden", "POST on \"{0}\" is forbidden.");
    }

    @DELETE //delete
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public Response delete(HashMap<String, String> data) {
        if (data == null) {
            data = new HashMap<>();
        }
        ResourceUtil.addQueryString(uriInfo.getQueryParameters(), data);
        String deleteCommand = getDeleteCommand();

        if (isJvmOptions(deleteCommand)) {
            if (data.isEmpty()) {
                deleteExistingOptions();
                return Response.ok().build();
            } else {
                return runCommand(deleteCommand, processData(data), "rest.resource.delete.message", "\"{0}\" deleted successfully.",
                        "rest.resource.delete.forbidden", "DELETE on \"{0}\" is forbidden.");
            }
        } else {
            return runCommand(deleteCommand, data, "rest.resource.delete.message", "\"{0}\" deleted successfully.",
                    "rest.resource.delete.forbidden", "DELETE on \"{0}\" is forbidden.");
        }

    }

    @OPTIONS
    @Produces({ MediaType.APPLICATION_JSON + ";qs=0.5", "text/html", MediaType.APPLICATION_XML + ";qs=0.5" })
    public Response options() {
        return Response.ok(buildActionReportResult()).build();
    }

    protected ActionReportResult buildActionReportResult() {
        RestActionReporter ar = new RestActionReporter();
        final String typeKey = upperCaseFirstLetter((decode(getName())));
        ar.setActionDescription(typeKey);
        ar.getExtraProperties().put("leafList", getEntity());

        OptionsResult optionsResult = new OptionsResult(Util.getResourceName(uriInfo));
        Map<String, MethodMetaData> mmd = getMethodMetaData();
        optionsResult.putMethodMetaData("GET", mmd.get("GET"));
        optionsResult.putMethodMetaData("POST", mmd.get("POST"));

        ResourceUtil.addMethodMetaData(ar, mmd);
        return new ActionReportResult(ar, optionsResult);
    }

    protected Map<String, MethodMetaData> getMethodMetaData() {
        Map<String, MethodMetaData> mmd = new TreeMap<>();
        //GET meta data
        mmd.put("GET", new MethodMetaData());

        //POST meta data
        String postCommand = getPostCommand();
        if (postCommand != null) {
            MethodMetaData postMethodMetaData = ResourceUtil.getMethodMetaData(postCommand, locatorBridge.getRemoteLocator());
            mmd.put("POST", postMethodMetaData);
        }

        //DELETE meta data
        String deleteCommand = getDeleteCommand();
        if (deleteCommand != null) {
            MethodMetaData deleteMethodMetaData = ResourceUtil.getMethodMetaData(deleteCommand, locatorBridge.getRemoteLocator());
            mmd.put("DELETE", deleteMethodMetaData);
        }

        return mmd;
    }

    protected void addDefaultParameter(Map<String, String> data) {
        int index = uriInfo.getAbsolutePath().getPath().lastIndexOf('/');
        String defaultParameterValue = uriInfo.getAbsolutePath().getPath().substring(index + 1);
        data.put("DEFAULT", defaultParameterValue);
    }

    protected String getPostCommand() {
        return null;
    }

    protected String getDeleteCommand() {
        return null;
    }

    protected String getName() {
        return Util.getResourceName(uriInfo);
    }

    private Response runCommand(String commandName, Map<String, String> data, String successMsgKey, String successMsg,
            String operationForbiddenMsgKey, String operationForbiddenMsg) {
        try {
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                return Response.status(400)
                        .entity(ResourceUtil.getActionReportResult(ActionReport.ExitCode.FAILURE, errorMessage, requestHeaders, uriInfo))
                        .build();
            }

            ResourceUtil.purgeEmptyEntries(data);
            ResourceUtil.adjustParameters(data);

            String attributeName = data.get("DEFAULT");

            if (null != commandName) {
                RestActionReporter actionReport = ResourceUtil.runCommand(commandName, data, getSubject());

                ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
                if (exitCode != ActionReport.ExitCode.FAILURE) {
                    String successMessage = localStrings.getLocalString(successMsgKey, successMsg, new Object[] { attributeName });
                    return Response.ok(ResourceUtil.getActionReportResult(actionReport, successMessage, requestHeaders, uriInfo)).build();
                }

                String errorMessage = getErrorMessage(data, actionReport);
                return Response.status(400).entity(ResourceUtil.getActionReportResult(actionReport, errorMessage, requestHeaders, uriInfo))
                        .build();
            }
            String message = localStrings.getLocalString(operationForbiddenMsgKey, operationForbiddenMsg,
                    new Object[] { uriInfo.getAbsolutePath() });
            return Response.status(403)
                    .entity(ResourceUtil.getActionReportResult(ActionReport.ExitCode.FAILURE, message, requestHeaders, uriInfo)).build();

        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String getErrorMessage(Map<String, String> data, ActionReport ar) {
        String message = ar.getMessage();

        /*if (data.isEmpty()) {
            try {
                //usage info
                message = ar.getTopMessagePart().getChildren().get(0).getMessage();
            } catch (Exception e) {
                message = ar.getMessage();
            }
        }*/
        return message;
    }

    // Ugly, temporary hack
    private Map<String, String> processData(Map<String, String> data) {
        Map<String, String> results = new HashMap<>();
        StringBuilder options = new StringBuilder();
        String sep = "";
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            if ("target".equals(key) || "profiler".equals(key)) {
                results.put(key, entry.getValue());
            } else {
                //                options.append(sep).append(escapeOptionPart(entry.getKey()));
                options.append(sep).append(entry.getKey());

                String value = entry.getValue();
                if ((value != null) && (!value.isEmpty())) {
                    //                    options.append("=").append(escapeOptionPart(entry.getValue()));
                    options.append("=").append(entry.getValue());
                }
                sep = ":";
            }
        }

        results.put("id", options.toString());
        if (results.get("target") == null) {
            results.put("target", target);
        }
        if (results.get("profiler") == null) {
            results.put("profiler", profiler);
        }

        return results;
    }

    /**
     * Escapes special chars (e.g., colons) in a JVM Option part
     *
     * @param part
     * @return
     */
    protected String escapeOptionPart(String part) {
        String changed = part.replace("\\", "\\\\").replace(":", "\\:");
        return changed;
    }

    // TODO: JvmOptions needs to have its own class, but the generator doesn't seem to support
    // overriding resourcePath mappings.  We need to address this post-3.1
    private boolean isJvmOptions(String command) {
        return (command != null) && (command.contains("jvm-options"));
    }

    protected Map<String, String> deleteExistingOptions() {
        Map<String, String> existing = new HashMap<>();
        existing.put("target", target);
        for (String option : getEntity()) {
            int index = option.indexOf("=");
            if (index > -1) {
                existing.put(escapeOptionPart(option.substring(0, index)), escapeOptionPart(option.substring(index + 1)));
            } else {
                existing.put(escapeOptionPart(option), "");
            }
        }

        runCommand(getDeleteCommand(), processData(existing), "rest.resource.delete.message", "\"{0}\" deleted successfully.",
                "rest.resource.delete.forbidden", "DELETE on \"{0}\" is forbidden.");
        return existing;
    }

}
