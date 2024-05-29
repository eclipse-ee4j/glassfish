/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.OptionsCapable;
import org.glassfish.admin.rest.composite.CompositeUtil;
import org.glassfish.admin.rest.composite.metadata.RestResourceMetadata;
import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.jersey.media.sse.EventOutput;

/**
 * @author ludo
 */
public class TemplateExecCommand extends AbstractResource implements OptionsCapable {
    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(TemplateExecCommand.class);

    protected String resourceName;
    protected String commandName;
    protected String commandDisplayName;
    protected String commandMethod;
    protected String commandAction;
    protected boolean isLinkedToParent = false;

    public TemplateExecCommand(String resourceName, String commandName, String commandMethod, String commandAction,
            String commandDisplayName, boolean isLinkedToParent) {
        this.resourceName = resourceName;
        this.commandName = commandName;
        this.commandMethod = commandMethod;
        this.commandAction = commandAction;
        this.commandDisplayName = commandDisplayName;
        this.isLinkedToParent = isLinkedToParent;

    }

    @Override
    public UriInfo getUriInfo() {
        return uriInfo;
    }

    @Override
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @OPTIONS
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.APPLICATION_XML })
    public ActionReportResult optionsLegacyFormat() {
        RestActionReporter ar = new RestActionReporter();
        ar.setExtraProperties(new Properties());
        ar.setActionDescription(commandDisplayName);

        OptionsResult optionsResult = new OptionsResult(resourceName);
        Map<String, MethodMetaData> mmd = new HashMap<>();
        MethodMetaData methodMetaData = ResourceUtil.getMethodMetaData(commandName, getCommandParams(), locatorBridge.getRemoteLocator());

        optionsResult.putMethodMetaData(commandMethod, methodMetaData);
        mmd.put(commandMethod, methodMetaData);
        ResourceUtil.addMethodMetaData(ar, mmd);

        ActionReportResult ret = new ActionReportResult(ar, null, optionsResult);
        ret.setCommandDisplayName(commandDisplayName);
        return ret;
    }

    @OPTIONS
    @Produces(Constants.MEDIA_TYPE_JSON + ";qs=0.5")
    public String options() throws JSONException {
        try {
            return new RestResourceMetadata(this).toJson().toString(Util.getFormattingIndentLevel());
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Response executeCommandAsSse(ParameterMap data) {
        EventOutput ec = ResourceUtil.runCommandWithSse(commandName, data, null, null);
        return Response.status(HttpURLConnection.HTTP_OK).entity(ec).build();
    }

    protected Response executeCommandLegacyFormat(ParameterMap data) {
        RestActionReporter actionReport = ResourceUtil.runCommand(commandName, data, getSubject());
        final ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
        final int status = (exitCode == ActionReport.ExitCode.FAILURE) ? HttpURLConnection.HTTP_INTERNAL_ERROR : HttpURLConnection.HTTP_OK;
        ActionReportResult option = optionsLegacyFormat();
        ActionReportResult results = new ActionReportResult(commandName, actionReport, option.getMetaData());
        results.getActionReport().getExtraProperties().putAll(option.getActionReport().getExtraProperties());
        results.setCommandDisplayName(commandDisplayName);
        // GLASSFISH-21582: removed setting of error messasge because ActionReportResult object is used for constructing the response(with error message)
        // setting error message again is showing error two times on GUI.
        return Response.status(status).entity(results).build();
    }

    protected CommandResult executeCommand(ParameterMap data) {
        RestActionReporter actionReport = ResourceUtil.runCommand(commandName, data, getSubject());
        ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
        if (exitCode == ActionReport.ExitCode.FAILURE) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(actionReport.getMessage()).build());
        }

        CommandResult cr = CompositeUtil.instance().getModel(CommandResult.class);
        cr.setMessage(actionReport.getMessage());
        cr.setProperties(actionReport.getTopMessagePart().getProps());
        cr.setExtraProperties(getExtraProperties(actionReport));
        return cr;
    }

    private Map<String, Object> getExtraProperties(RestActionReporter actionReport) {
        Properties props = actionReport.getExtraProperties();
        Map<String, Object> map = new HashMap<>();

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }

        return map;
    }

    /*override it
     *
     *
     */

    protected HashMap<String, String> getCommandParams() {
        return null;
    }

    protected void processCommandParams(ParameterMap data) {
        HashMap<String, String> commandParams = getCommandParams();
        if (commandParams != null) {
            ResourceUtil.resolveParamValues(commandParams, uriInfo);
            for (Map.Entry<String, String> entry : commandParams.entrySet()) {
                data.add(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void addQueryString(MultivaluedMap<String, String> qs, ParameterMap data) {
        for (Map.Entry<String, List<String>> entry : qs.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                try {
                    data.add(key, URLDecoder.decode(value, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    protected void adjustParameters(ParameterMap data) {
        if (data != null) {
            if (!(data.containsKey("DEFAULT"))) {
                boolean isRenamed = renameParameter(data, "name", "DEFAULT");
                if (!isRenamed) {
                    renameParameter(data, "id", "DEFAULT");
                }
            }
            data.remove("jsoncallback"); //these 2 are for JSONP padding, not needed for CLI execs
            data.remove("_");
        }
    }

    protected boolean renameParameter(ParameterMap data, String parameterToRename, String newName) {

        if ((data.containsKey(parameterToRename))) {
            List<String> value = data.get(parameterToRename);
            data.remove(parameterToRename);
            data.set(newName, value);
            return true;
        }
        return false;
    }

    protected void purgeEmptyEntries(ParameterMap data) {

        HashSet<String> keyToRemove = new HashSet<>();
        Set<Entry<String, List<String>>> entries = data.entrySet();
        for (Entry<String, List<String>> entry : entries) {
            if ((entry.getValue() == null) || (entry.getValue().isEmpty())) {
                keyToRemove.add(entry.getKey());

            }
        }
        if ("true".equals(data.getOne("__remove_empty_entries__"))) {
            data.remove("__remove_empty_entries__");
            //now remove list of 1 element which is "" only
            Set<Entry<String, List<String>>> entries2 = data.entrySet();
            //temp list to avoid Concurrent Modification Exception
            for (Entry<String, List<String>> entry : entries2) {
                if (entry.getValue().size() == 1) {
                    if (entry.getValue().get(0).equals("")) {
                        keyToRemove.add(entry.getKey());
                    }
                }
            }
        }
        for (String k : keyToRemove) {
            data.remove(k);

        }
    }

    protected String getParent(UriInfo uriInfo) {
        List<PathSegment> segments = uriInfo.getPathSegments(true);
        String parent = segments.get(segments.size() - 2).getPath();

        return parent;
    }
}
