/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.composite;

import com.sun.enterprise.v3.admin.AdminCommandJob;
import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.security.auth.Subject;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.RestResource;
import org.glassfish.admin.rest.model.ResponseBody;
import org.glassfish.admin.rest.model.RestCollectionResponseBody;
import org.glassfish.admin.rest.model.RestModelResponseBody;
import org.glassfish.admin.rest.resources.AbstractResource;
import org.glassfish.admin.rest.utils.JobIdAsyncAdminCommandInvoker;
import org.glassfish.admin.rest.utils.JsonFilter;
import org.glassfish.admin.rest.utils.JsonUtil;
import org.glassfish.admin.rest.utils.Util;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.internal.api.Globals;
import org.glassfish.jersey.internal.util.collection.Ref;

/**
 * This is the base class for all composite resources. It provides all of the basic configuration and utilities needed
 * by composites. For top-level resources, the <code>@Path</code> and <code>@Service</code> annotations are still
 * required, though, in order for the resource to be located and configured properly.
 *
 * @author jdlee
 */
@Produces(Constants.MEDIA_TYPE_JSON)
public abstract class CompositeResource extends AbstractResource implements RestResource {
    // All methods that expect a request body should include the annotation:
    // @Consumes(CONSUMES_TYPE)
    public static final String CONSUMES_TYPE = Constants.MEDIA_TYPE_JSON;

    protected static final String DETACHED = "__detached";
    protected static final String DETACHED_DEFAULT = "false";

    protected static final String INCLUDE = "__includeFields";
    protected static final String EXCLUDE = "__excludeFields";

    // TODO: These should be configurable
    protected static final int THREAD_POOL_CORE = 5;
    protected static final int THREAD_POOL_MAX = 10;

    protected CompositeUtil compositeUtil = CompositeUtil.instance();

    public void setSubjectRef(Ref<Subject> subjectRef) {
        this.subjectRef = subjectRef;
    }

    public CompositeUtil getCompositeUtil() {
        return compositeUtil;
    }

    /**
     * This method creates a sub-resource of the specified type. Since the JAX-RS does not allow for injection into
     * sub-resources (as it doesn't know or control the lifecycle of the object), this method performs a manual "injection"
     * of the various system objects the resource might need. If the requested Class can not be instantiated (e.g., it does
     * not have a no-arg public constructor), the system will throw a <code>WebApplicationException</code> with an HTTP
     * status code of 500 (internal server error).
     *
     * @param clazz The Class of the desired sub-resource
     * @return
     */
    public <T> T getSubResource(Class<T> clazz) {
        logger.log(Level.FINEST, () -> "Creating sub resource of " + clazz);
        try {
            T resource = clazz.getDeclaredConstructor().newInstance();
            CompositeResource cr = (CompositeResource) resource;
            cr.locatorBridge = locatorBridge;
            cr.subjectRef = subjectRef;
            cr.uriInfo = uriInfo;
            cr.requestHeaders = requestHeaders;
            cr.serviceLocator = serviceLocator;

            return resource;
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // Convenience methods for creating models
    protected <T> T newModel(Class<T> modelIface) {
        return getCompositeUtil().getModel(modelIface);
    }

    protected <T extends RestModel> T newTemplate(Class<T> modelIface) {
        // We don't want any model trimming to happen on templates since the caller is supposed to
        // get the template, modify it, then POST it back and since POST should be getting full entities.
        T template = newModel(modelIface);
        template.allFieldsSet();
        return template;
    }

    protected <T extends RestModel> T getTypedModel(Class<T> modelIface, JSONObject jsonModel) throws Exception {
        if (jsonModel == null) {
            return null;
        }
        return CompositeUtil.instance().unmarshallClass(getLocale(), modelIface, jsonModel);
    }

    protected JSONObject getJsonModel(RestModel typedModel) throws Exception {
        return (JSONObject) JsonUtil.getJsonObject(typedModel, false); // include confidential properties
    }

    // Convenience methods for constructing URIs
    /**
     * Every resource that returns a collection will need to return the URI for each item in the colleciton. This method
     * handles the creation of that URI, ensuring a correct and consistent URI pattern.
     *
     * @param name
     * @return
     */
    protected URI getChildItemUri(String name) {
        return getSubUri("id/" + name);
    }

    protected URI getUri(String path) {
        return this.uriInfo.getBaseUriBuilder().path(path).build();
    }

    protected URI getSubUri(String name) {
        return this.uriInfo.getAbsolutePathBuilder().path(name).build();
    }

    // Convenience methods for adding links
    protected void addResourceLink(ResponseBody rb, String rel) throws Exception {
        rb.addResourceLink(rel, getSubUri(rel));
    }

    protected void addActionResourceLink(ResponseBody rb, String action) throws Exception {
        rb.addActionResourceLink(action, getSubUri(action));
    }

    protected boolean includeResourceLinks() {
        final String hdr = requestHeaders.getRequestHeaders().getFirst("X-Skip-Metadata"); // X-Skip-Resource-Links
        boolean skip = "true".equalsIgnoreCase(hdr);
        return !skip;
    }

    // Convenience methods for computing a resource's parent uri
    protected URI getParentUri() throws Exception {
        return getParentUri(false);
    }

    protected URI getCollectionChildParentUri() throws Exception {
        return getParentUri(true);
    }

    private URI getParentUri(boolean isCollectionChild) throws Exception {
        List<PathSegment> pathSegments = this.uriInfo.getPathSegments();
        int count = pathSegments.size() - 1; // go up a level to get to the parent
        if (isCollectionChild) {
            count--; // collection children have the url pattern .../foos/id/myfoo. need to go up another level
        }
        // [0] = 'javaservice', which is a resource
        if (count <= 0) {
            return null; // top level resource
        }
        UriBuilder bldr = this.uriInfo.getBaseUriBuilder();
        for (int i = 0; i < count; i++) {
            bldr.path(pathSegments.get(i).getPath());
        }
        return bldr.build();
    }

    /**
     * Execute a read-only <code>AdminCommand</code> with no parameters.
     *
     * @param command
     * @param parameters
     * @return
     */
    protected ActionReporter executeReadCommand(String command, ParameterMap parameters) {
        return getCompositeUtil().executeCommand(getSubject(), command, parameters, Status.NOT_FOUND, true, true);
    }

    /**
     * TBD - Jason Lee wants to move this into the defaults generators.
     *
     * Finds an unused name given the list of currently used names and a name prefix.
     *
     * @param namePrefix
     * @param usedNames
     * @return a String containing an unused dname, or an empty string if all candidate names are currently in use.
     */
    protected String generateDefaultName(String namePrefix, Collection<String> usedNames) {
        for (int i = 1; i <= 100; i++) {
            String name = namePrefix + "-" + i;
            if (!usedNames.contains(name)) {
                return name;
            }
        }
        // All the candidate names are in use.  Return an empty name.
        return "";
    }

    // Convenience methods for 'create' method responses
    protected Response created(String name, String message) throws Exception {
        return created(responseBody(), name, message);
    }

    protected Response created(ResponseBody rb, String name, String message) throws Exception {
        rb.addSuccess(message);
        return created(rb, name);
    }

    protected Response created(ResponseBody rb, String name) throws Exception {
        return created(rb, getChildItemUri(name));
    }

    protected Response created(ResponseBody rb, URI uri) throws Exception {
        return Response.created(uri).entity(rb).build();
    }

    // Convenience methods for 'update' method responses
    protected Response updated(String message) {
        return updated(responseBody(), message);
    }

    protected Response updated(ResponseBody rb, String message) {
        rb.addSuccess(message);
        return updated(rb);
    }

    protected Response updated(ResponseBody rb) {
        return ok(rb);
    }

    // Convenience methods for 'delete' method responses
    protected Response deleted(String message) {
        return deleted(responseBody(), message);
    }

    protected Response deleted(ResponseBody rb, String message) {
        rb.addSuccess(message);
        return deleted(rb);
    }

    protected Response deleted(ResponseBody rb) {
        return ok(rb);
    }

    // Convenience methods for 'action' method responses
    protected Response acted(String message) {
        return acted(responseBody(), message);
    }

    protected Response acted(ResponseBody rb, String message) {
        rb.addSuccess(message);
        return acted(rb);
    }

    protected Response acted(ResponseBody rb) {
        return ok(rb);
    }

    // Convenience methods for detached method responses
    protected Response accepted(String message, URI jobUri, URI newItemUri) {
        return accepted(responseBody(), message, jobUri, newItemUri);
    }

    protected Response accepted(ResponseBody rb, String message, URI jobUri, URI newItemUri) {
        rb.addSuccess(message);
        return accepted(rb, jobUri, newItemUri);
    }

    protected Response accepted(ResponseBody rb, URI jobUri, URI newItemUri) {
        ResponseBuilder bldr = Response.status(Status.ACCEPTED).entity(rb);
        if (jobUri != null) {
            bldr.header("Location", jobUri);
        }
        if (newItemUri != null) {
            bldr.header("X-Location", newItemUri);
        }
        return bldr.build();
    }

    protected Response accepted(String command, ParameterMap parameters, URI childUri) {
        return accepted(responseBody(), launchDetachedCommand(command, parameters), childUri);
    }

    protected URI launchDetachedCommand(String command, ParameterMap parameters) {
        CommandRunner<AdminCommandJob> cr = Globals.getDefaultHabitat().getService(CommandRunner.class);
        final RestActionReporter ar = new RestActionReporter();
        final CommandInvocation<AdminCommandJob> commandInvocation = cr.getCommandInvocation(command, ar, getSubject())
            .parameters(parameters);
        final String jobId = new JobIdAsyncAdminCommandInvoker(commandInvocation).start();
        return getUri("jobs/id/" + jobId);
    }

    protected Response ok(ResponseBody rb) {
        return Response.ok(rb).build();
    }

    // Convenience methods for throwing common webapp exceptions
    protected Response badRequest(ResponseBody rb, String message) {
        rb.addFailure(message);
        return badRequest(rb);
    }

    protected Response badRequest(ResponseBody rb) {
        return Response.status(Status.BAD_REQUEST).entity(rb).build();
    }

    protected WebApplicationException badRequest(Throwable cause) {
        return new WebApplicationException(cause, Status.BAD_REQUEST);
    }

    protected WebApplicationException badRequest(String message) {
        return new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(message).build());
    }

    protected WebApplicationException notFound(String message) {
        return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(message).build());
    }

    /*
    protected void internalServerError(Exception e) {
        ExceptionUtils.log(e);
    }
    */

    // Convenience methods for creating response bodies
    protected <T extends RestModel> RestCollectionResponseBody<T> restCollectionResponseBody(Class<T> modelIface, String collectionName,
            URI parentUri) {
        RestCollectionResponseBody<T> rb = restCollectionResponseBody(modelIface, collectionName);
        rb.addParentResourceLink(parentUri);
        return rb;
    }

    protected <T extends RestModel> RestCollectionResponseBody<T> restCollectionResponseBody(Class<T> modelIface, String collectionName) {
        return new RestCollectionResponseBody<>(includeResourceLinks(), this.uriInfo, collectionName);
    }

    protected <T extends RestModel> RestModelResponseBody<T> restModelResponseBody(Class<T> modelIface, URI parentUri, T entity) {
        RestModelResponseBody<T> rb = restModelResponseBody(modelIface, parentUri);
        rb.setEntity(entity);
        return rb;
    }

    protected <T extends RestModel> RestModelResponseBody<T> restModelResponseBody(Class<T> modelIface, URI parentUri) {
        RestModelResponseBody<T> rb = restModelResponseBody(modelIface);
        rb.addParentResourceLink(parentUri);
        return rb;
    }

    protected <T extends RestModel> RestModelResponseBody<T> restModelResponseBody(Class<T> modelIface) {
        return new RestModelResponseBody<>(includeResourceLinks());
    }

    protected ResponseBody responseBody() {
        return new ResponseBody(includeResourceLinks());
    }

    // Convenience methods for creating responses from response bodies
    protected Response getResponse(ResponseBody responseBody) {
        return getResponse(Status.OK, responseBody);
    }

    protected Response getResponse(Status status, ResponseBody responseBody) {
        return Response.status(status).entity(responseBody).build();
    }

    // Convenience methods to help filter returned data
    protected JsonFilter getFilter(String include, String exclude) throws Exception {
        return new JsonFilter(getLocale(), include, exclude);
    }

    protected JsonFilter getFilter(String include, String exclude, String identityAttr) throws Exception {
        return new JsonFilter(getLocale(), include, exclude, identityAttr);
    }

    protected <T extends RestModel> T filterModel(Class<T> modelIface, T unfilteredModel, String include, String exclude) throws Exception {
        return filterModel(modelIface, unfilteredModel, getFilter(include, exclude));
    }

    protected <T extends RestModel> T filterModel(Class<T> modelIface, T unfilteredModel, String include, String exclude,
            String identityAttr) throws Exception {
        return filterModel(modelIface, unfilteredModel, getFilter(include, exclude, identityAttr));
    }

    protected <T extends RestModel> T filterModel(Class<T> modelIface, T unfilteredModel, JsonFilter filter) throws Exception {
        JSONObject unfilteredJson = (JSONObject) JsonUtil.getJsonObject(unfilteredModel, false); // don't hide confidential properties
        JSONObject filteredJson = filter.trim(unfilteredJson);
        T filteredModel = getTypedModel(modelIface, filteredJson);
        filteredModel.trimmed(); // TBD - remove once the conversion to the new REST style guide is completed
        return filteredModel;
    }

    protected Locale getLocale() {
        return CompositeUtil.instance().getLocale(requestHeaders);
    }

    /**
     * Convenience method for getting a path parameter. Equivalent to uriInfo.getPathParameters().getFirst(name)
     *
     * @param name
     * @return
     */
    protected String getPathParam(String name) {
        return this.uriInfo.getPathParameters().getFirst(name);
    }

    protected ParameterMap parameterMap() {
        return Util.parameterMap();
    }

    protected synchronized ExecutorService getExecutorService() {
        return ExecutorServiceHolder.INSTANCE;
    }


    private class CommandInvoker {

        public CommandInvoker() {
        }

        public CommandInvoker(String command, ParameterMap params, String successMessage) {
            setCommand(command);
            setParams(params);
            setSuccessMessage(successMessage);
        }

        private String command;

        public void setCommand(String val) {
            this.command = val;
        }

        public String getCommand() {
            return this.command;
        }

        private ParameterMap params;

        public void setParams(ParameterMap val) {
            this.params = val;
        }

        public ParameterMap getParams() {
            return this.params;
        }

        private String successMsg;

        public void setSuccessMessage(String val) {
            this.successMsg = val;
        }

        public String getSuccessMessage() {
            return this.successMsg;
        }

        public void setResult(Properties extraProperties) {
        }
    }

    public class CreateCommandInvoker extends CommandInvoker {

        public CreateCommandInvoker() {
            super();
        }

        public CreateCommandInvoker(String command, ParameterMap params, String successMessage, String newItemName) {
            super(command, params, successMessage);
            setNewItemName(newItemName);
        }

        private String newItemName;

        public void setNewItemName(String val) {
            this.newItemName = val;
        }

        public String getNewItemName() {
            return this.newItemName;
        }
    }

    private static class ExecutorServiceHolder {
        private static final ExecutorService INSTANCE = new ThreadPoolExecutor(THREAD_POOL_CORE, // core thread pool size
                THREAD_POOL_MAX, // maximum thread pool size
                1, // time to wait before resizing pool
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(THREAD_POOL_MAX, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
