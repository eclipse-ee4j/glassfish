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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.config.support.Create;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.TransactionFailure;

import static org.glassfish.admin.rest.utils.ResourceUtil.getActionReportResult;
import static org.glassfish.admin.rest.utils.Util.decode;
import static org.glassfish.admin.rest.utils.Util.getName;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
public abstract class TemplateListOfResource extends AbstractResource {
    private static final Logger LOG = Logger.getLogger(TemplateListOfResource.class.getName());
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(TemplateListOfResource.class);

    @Context
    protected ServiceLocator injector;

    protected List<Dom> entity;
    protected Dom parent;
    protected String tagName;

    @GET
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5" })
    public Response get(@QueryParam("expandLevel") @DefaultValue("1") int expandLevel) {
        return Response.ok().entity(buildActionReportResult()).build();
    }

    @POST
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5" })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public Response createResource(HashMap<String, String> data) {
        if (data == null) {
            data = new HashMap<>();
        }
        try {
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                ActionReportResult arr = getActionReportResult(ActionReport.ExitCode.FAILURE, errorMessage, requestHeaders,
                        uriInfo);
                return Response.status(400).entity(arr).build();
            }

            ResourceUtil.purgeEmptyEntries(data);

            //Command to execute
            String commandName = getPostCommand();
            String resourceToCreate = uriInfo.getAbsolutePath() + "/";

            if (commandName == null) {
                ActionReportResult arr = getActionReportResult(ActionReport.ExitCode.FAILURE,
                    "No CRUD Create possible.", requestHeaders, uriInfo);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(arr).build();
            }
            ResourceUtil.adjustParameters(data);
            if (data.containsKey("name")) {
                resourceToCreate += data.get("name");
            } else {
                resourceToCreate += data.get("DEFAULT");
            }
            RestActionReporter actionReport = ResourceUtil.runCommand(commandName, data, getSubject());

            ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
            if (exitCode == ActionReport.ExitCode.FAILURE) {
                String errorMessage = actionReport.getMessage();
                LOG.log(Level.SEVERE, errorMessage, actionReport.getFailureCause());
                ActionReportResult result = getActionReportResult(actionReport, errorMessage, requestHeaders, uriInfo);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
            }
            String successMessage = localStrings.getLocalString("rest.resource.create.message",
                "\"{0}\" created successfully.", resourceToCreate);
            ActionReportResult arr = getActionReportResult(actionReport, successMessage, requestHeaders, uriInfo);
            return Response.ok(arr).build();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(FormDataMultiPart formData) {
        // data passed to the generic command running
        HashMap<String, String> data = TemplateRestResource.createDataBasedOnForm(formData);
        return createResource(data, data.get("name")); //execute the deploy command with a copy of the file locally

    }

    @OPTIONS
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.APPLICATION_XML })
    public Response options() {
        return Response.ok().entity(buildActionReportResult()).build();
    }

    public void setEntity(List<Dom> p) {
        entity = p;
    }

    public List<Dom> getEntity() {
        return entity;
    }

    public void setParentAndTagName(Dom parent, String tagName) {
        this.parent = parent;
        this.tagName = tagName;
        if (parent != null) {
            entity = parent.nodeElements(tagName);
        }

    }

    /**
     * allows for remote files to be put in a tmp area and we pass the local location of this file to the corresponding
     * command instead of the content of the file * Yu need to add enctype="multipart/form-data" in the form for ex:
     * <form action="http://localhost:4848/management/domain/applications/application" method="post" enctype=
     * "multipart/form-data"> then any param of type="file" will be uploaded, stored locally and the param will use the
     * local location on the server side (ie. just the path)
     */
    public String getPostCommand() {
        ConfigModel.Property p = parent.model.getElement(tagName);

        if (p == null) { //"*"
            ConfigModel.Property childElement = parent.model.getElement("*");
            if (childElement != null) {
                ConfigModel.Node node = (ConfigModel.Node) childElement;
                ConfigModel childModel = node.getModel();
                List<ConfigModel> subChildConfigModels = ResourceUtil.getRealChildConfigModels(childModel, parent.document);
                for (ConfigModel subChildConfigModel : subChildConfigModels) {
                    if (subChildConfigModel.getTagName().equals(tagName)) {
                        return ResourceUtil.getCommand(RestRedirect.OpType.POST, subChildConfigModel);
                    }
                }

            }
        } else {
            ConfigModel.Node n = (ConfigModel.Node) p;
            String command = ResourceUtil.getCommand(RestRedirect.OpType.POST, n.getModel());
            if (command != null) {
                return command;
            }
            //last  possible case...the @Create annotation on a parent method
            Class<? extends ConfigBeanProxy> cbp = null;
            try {
                cbp = (Class<? extends ConfigBeanProxy>) parent.model.classLoaderHolder.loadClass(parent.model.targetTypeName);
            } catch (MultiException e) {
                return null;
            }
            Create create = null;
            for (Method m : cbp.getMethods()) {
                ConfigModel.Property pp = parent.model.toProperty(m);
                if ((pp != null) && (pp.xmlName.equals(tagName)) && (m.isAnnotationPresent(Create.class))) {
                    create = m.getAnnotation(Create.class);
                    break;
                }
            }
            if (create != null) {
                return create.value();
            }
        }

        return null;
    }

    public String[][] getCommandResourcesPaths() {
        return new String[][] {};
    }

    public static Class<? extends ConfigBeanProxy> getElementTypeByName(Dom parentDom, String elementName) throws ClassNotFoundException {

        DomDocument document = parentDom.document;
        ConfigModel.Property a = parentDom.model.getElement(elementName);
        if (a != null) {
            if (a.isLeaf()) {
                //  : I am not too sure, but that should be a String @Element
                return null;
            } else {
                ConfigModel childModel = ((ConfigModel.Node) a).getModel();
                return (Class<? extends ConfigBeanProxy>) childModel.classLoaderHolder.loadClass(childModel.targetTypeName);
            }
        }
        // global lookup
        ConfigModel model = document.getModelByElementName(elementName);
        if (model != null) {
            return (Class<? extends ConfigBeanProxy>) model.classLoaderHolder.loadClass(model.targetTypeName);
        }

        return null;
    }

    protected ActionReportResult buildActionReportResult() {
        if (entity == null) {//wrong resource
            String errorMessage = localStrings.getLocalString("rest.resource.erromessage.noentity", "Resource not found.");
            return getActionReportResult(ActionReport.ExitCode.FAILURE, errorMessage, requestHeaders, uriInfo);
        }
        RestActionReporter ar = new RestActionReporter();
        final String typeKey = (decode(getName(uriInfo.getPath(), '/')));
        ar.setActionDescription(typeKey);

        OptionsResult optionsResult = new OptionsResult(Util.getResourceName(uriInfo));
        Map<String, MethodMetaData> mmd = getMethodMetaData();
        optionsResult.putMethodMetaData("GET", mmd.get("GET"));
        optionsResult.putMethodMetaData("POST", mmd.get("POST"));

        ResourceUtil.addMethodMetaData(ar, mmd);
        ar.getExtraProperties().put("childResources", ResourceUtil.getResourceLinks(getEntity(), uriInfo));
        ar.getExtraProperties().put("commands", ResourceUtil.getCommandLinks(getCommandResourcesPaths()));

        // FIXME:  I'd rather not keep using OptionsResult, but I don't have the time at this point to do it "right."  This is
        // an internal impl detail, so it can wait
        return new ActionReportResult(ar, optionsResult);
    }

    //called in case of POST on application resource (deployment).
    //resourceToCreate is the name attribute if provided.
    private Response createResource(HashMap<String, String> data, String resourceToCreate) {
        try {
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                return Response.status(400)
                        .entity(getActionReportResult(ActionReport.ExitCode.FAILURE, errorMessage, requestHeaders, uriInfo))
                        .build();
            }

            ResourceUtil.purgeEmptyEntries(data);

            //Command to execute
            String commandName = getPostCommand();
            ResourceUtil.defineDefaultParameters(data);

            if ((resourceToCreate == null) || (resourceToCreate.equals(""))) {
                String newResourceName = data.get("DEFAULT");
                if (newResourceName != null) {
                    if (newResourceName.contains("/")) {
                        newResourceName = Util.getName(newResourceName, '/');
                    } else {
                        if (newResourceName.contains("\\")) {
                            newResourceName = Util.getName(newResourceName, '\\');
                        }
                    }
                    resourceToCreate = uriInfo.getAbsolutePath() + "/" + newResourceName;
                }
            } else {
                resourceToCreate = uriInfo.getAbsolutePath() + "/" + resourceToCreate;
            }

            if (null != commandName) {
                RestActionReporter actionReport = ResourceUtil.runCommand(commandName, data, getSubject());

                ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
                if (exitCode != ActionReport.ExitCode.FAILURE) {
                    String successMessage = localStrings.getLocalString("rest.resource.create.message", "\"{0}\" created successfully.",
                            new Object[] { resourceToCreate });
                    return Response.ok().entity(getActionReportResult(actionReport, successMessage, requestHeaders, uriInfo))
                            .build();
                }

                String errorMessage = actionReport.getMessage();
                return Response.status(400)
                    .entity(getActionReportResult(actionReport, errorMessage, requestHeaders, uriInfo)).build();
            }
            String message = localStrings.getLocalString("rest.resource.post.forbidden",
                "POST on \"{0}\" is forbidden.", new Object[] {resourceToCreate});
            return Response.status(403)
                .entity(getActionReportResult(ActionReport.ExitCode.FAILURE, message, requestHeaders, uriInfo)).build();

        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private Map<String, MethodMetaData> getMethodMetaData() {
        Map<String, MethodMetaData> map = new TreeMap<>();
        //GET meta data
        map.put("GET", new MethodMetaData());

        //POST meta data
        String command = getPostCommand();
        if (command != null) {
            MethodMetaData postMethodMetaData = ResourceUtil.getMethodMetaData(command, locatorBridge.getRemoteLocator());
            if (Util.getResourceName(uriInfo).equals("Application")) {
                postMethodMetaData.setIsFileUploadOperation(true);
            }
            map.put("POST", postMethodMetaData);
        }

        return map;
    }

    private WebApplicationException handleException(Exception e) {
        // note: WeApplicationExceptions are not logged otherwise.
        LOG.log(Level.SEVERE, "Create resource call failed", e);
        if (e instanceof TransactionFailure) {
            TransactionFailure failure = (TransactionFailure) e;
            if (failure.getCause() instanceof ConstraintViolationException) {
                return new WebApplicationException(failure, Response.Status.BAD_REQUEST);
            }
        }
        return new WebApplicationException(e);
    }
}
