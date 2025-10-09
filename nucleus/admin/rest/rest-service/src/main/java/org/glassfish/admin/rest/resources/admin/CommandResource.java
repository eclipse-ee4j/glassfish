/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.rest.resources.admin;

import com.sun.enterprise.admin.remote.ParamsWithPayload;
import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.admin.remote.RestPayloadImpl;
import com.sun.enterprise.admin.util.CachedCommandModel;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.universal.collections.ManifestUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.uuid.UuidGenerator;
import com.sun.enterprise.util.uuid.UuidGeneratorImpl;
import com.sun.enterprise.v3.admin.AdminCommandJob;
import com.sun.enterprise.v3.admin.AsyncAdminCommandInvoker;
import com.sun.enterprise.v3.common.ActionReporter;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.enterprise.v3.common.PropsFileActionReporter;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;

import javax.security.auth.Subject;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.utils.DetachedSseAdminCommandInvoker;
import org.glassfish.admin.rest.utils.SseAdminCommandInvoker;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.media.sse.SseFeature;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 *
 * @author mmares
 */
@Path("/")
public class CommandResource {
    private final static LocalStringManagerImpl strings = new LocalStringManagerImpl(CommandResource.class);

    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private static final int MAX_AGE = 86400;

    private static UuidGenerator uuidGenerator = new UuidGeneratorImpl();
    private static volatile String serverName;

    private CommandRunner<AdminCommandJob> commandRunner;

    @Inject
    private Ref<Subject> subjectRef;

    // -------- GET+OPTION: Get CommandModel

    @GET
    @Path("/{command:.*}/")
    @Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
    public Response getCommandModel(@PathParam("command") String command) throws WebApplicationException {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "getCommandModel({0})", commandName);
        CommandModel model = getCommandModel(commandName);
        String eTag = CachedCommandModel.computeETag(model);
        return Response.ok(model).tag(new EntityTag(eTag, true)).build();
    }

    @OPTIONS
    @Path("/{command:.*}/")
    @Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
    public Response optionsCommandModel(@PathParam("command") String commandName) throws WebApplicationException {
        return getCommandModel(commandName);
    }

    // -------- GET: Manpage

    @GET
    @Path("/{command:.*}/manpage")
    @Produces({ MediaType.TEXT_HTML })
    public String getManPageHtml(@PathParam("command") String command) throws IOException, WebApplicationException {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "getManPageHtml({0})", commandName);
        BufferedReader help = getManPageReader(commandName);
        if (help == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        result.append("<html><body>");
        String line;
        while ((line = help.readLine()) != null) {
            result.append(leadingSpacesToNbsp(StringUtils.escapeForHtml(line))).append("<br/>\n");
        }
        result.append("</body></html>");
        return result.toString();
    }

    @GET
    @Path("/{command:.*}/manpage")
    @Produces({ MediaType.TEXT_PLAIN })
    public String getManPageTxt(@PathParam("command") String command, @QueryParam("eol") String eol)
            throws IOException, WebApplicationException {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "getManPageTxt({0}, {1})", new Object[] { commandName, eol });
        BufferedReader help = getManPageReader(commandName);
        if (help == null) {
            return null;
        }
        if (!StringUtils.ok(eol)) {
            eol = ManifestUtils.EOL;
        }
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = help.readLine()) != null) {
            result.append(line).append(eol);
        }
        return result.toString();
    }

    // -------- POST: Execute command [just ACTION-REPORT]

    @POST
    @Path("/{command:.*}/")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
    @Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
    public Response execCommandSimpInSimpOut(@PathParam("command") String command, @HeaderParam("X-Indent") String indent,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId, ParameterMap data) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandSimpInSimpOut({0})", commandName);
        return executeCommand(commandName, null, data, false, indent, modelETag, jSessionId);
    }

    @POST
    @Path("/{command:.*}/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
    public Response execCommandMultInSimpOut(@PathParam("command") String command, @HeaderParam("X-Indent") String indent,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId, ParamsWithPayload pwp) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandMultInSimpOut({0})", commandName);
        ParameterMap data = null;
        Payload.Inbound inbound = null;
        if (pwp != null) {
            data = pwp.getParameters();
            inbound = pwp.getPayloadInbound();
        }
        return executeCommand(commandName, inbound, data, false, indent, modelETag, jSessionId);
    }

    @POST
    @Path("/{command:.*}/")
    @Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
    public Response execCommandEmptyInSimpOut(@PathParam("command") String command, @HeaderParam("X-Indent") String indent,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandEmptyInSimpOut({0})", commandName);
        ParameterMap data = new ParameterMap();
        return executeCommand(commandName, null, data, false, indent, modelETag, jSessionId);
    }

    // -------- POST: Execute command [MULTIPART result]

    @POST
    @Path("/{command:.*}/")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
    @Produces("multipart/mixed")
    public Response execCommandSimpInMultOut(@PathParam("command") String command, @HeaderParam("X-Indent") String indent,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId, ParameterMap data) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandSimpInMultOut({0})", commandName);
        return executeCommand(commandName, null, data, true, indent, modelETag, jSessionId);
    }

    @POST
    @Path("/{command:.*}/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("multipart/mixed")
    public Response execCommandMultInMultOut(@PathParam("command") String command, @HeaderParam("X-Indent") String indent,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId, ParamsWithPayload pwp) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandMultInMultOut({0})", commandName);
        ParameterMap data = null;
        Payload.Inbound inbound = null;
        if (pwp != null) {
            data = pwp.getParameters();
            inbound = pwp.getPayloadInbound();
        }
        return executeCommand(commandName, inbound, data, true, indent, modelETag, jSessionId);
    }

    @POST
    @Path("/{command:.*}/")
    @Produces("multipart/mixed")
    public Response execCommandEmptyInMultOut(@PathParam("command") String command, @HeaderParam("X-Indent") String indent,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandEmptyInMultOut({0})", commandName);
        ParameterMap data = new ParameterMap();
        return executeCommand(commandName, null, data, true, indent, modelETag, jSessionId);
    }

    // -------- POST: Execute command [SSE]

    @POST
    @Path("/{command:.*}/")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Response execCommandSimpInSseOut(@PathParam("command") String command,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId, ParameterMap data) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandSimpInSseOut({0})", commandName);
        return executeSseCommand(commandName, null, data, modelETag, jSessionId);
    }

    @POST
    @Path("/{command:.*}/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Response execCommandMultInSseOut(@PathParam("command") String command,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId, ParamsWithPayload pwp) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandMultInSseOut({0})", commandName);
        ParameterMap data = null;
        if (pwp != null) {
            data = pwp.getParameters();
        }
        return executeSseCommand(commandName, null, data, modelETag, jSessionId);
    }

    @POST
    @Path("/{command:.*}/")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Response execCommandEmptyInSseOut(@PathParam("command") String command,
            @HeaderParam(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER) String modelETag,
            @CookieParam(SESSION_COOKIE_NAME) Cookie jSessionId) {
        CommandName commandName = new CommandName(normalizeCommandName(command));
        RestLogging.restLogger.log(Level.FINEST, "execCommandEmptyInSseOut({0})", commandName);
        ParameterMap data = new ParameterMap();
        return executeSseCommand(commandName, null, data, modelETag, jSessionId);
    }

    // -------- private implementation

    private String normalizeCommandName(String str) {
        if (str == null) {
            return null;
        }
        if (str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    }

    private void checkCommandModelETag(CommandModel model, String modelETag) throws WebApplicationException {
        CommandRunner cr = getCommandRunner();
        if (StringUtils.ok(modelETag) && !cr.validateCommandModelETag(model, modelETag)) {
            String message = strings.getLocalString("commandmodel.etag.invalid", "Cached command model for command {0} is invalid.",
                    model.getCommandName());
            throw new WebApplicationException(
                    Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.TEXT_PLAIN).entity(message).build());
        }
    }

    private Response executeSseCommand(CommandName commandName, Payload.Inbound inbound, ParameterMap params,
        String modelETag, Cookie jSessionId) throws WebApplicationException {
        RestLogging.restLogger.log(Level.FINEST, "executeSseCommand({0})", commandName);
        final CommandModel model = getCommandModel(commandName);
        checkCommandModelETag(model, modelETag);
        final boolean notify = params == null ? false : params.containsKey("notify");
        final boolean detach = params == null ? false : params.containsKey("detach");
        final CommandInvocation<AdminCommandJob> invocation = getCommandRunner().getCommandInvocation(
            commandName.getScope(), commandName.getName(), new PropsFileActionReporter(), getSubject(), notify, detach);
        if (inbound != null) {
            invocation.inbound(inbound);
        }
        invocation.outbound(new RestPayloadImpl.Outbound(false)).parameters(params);
        ResponseBuilder builder = Response.status(HTTP_OK);
        if (isSingleInstanceCommand(model)) {
            builder.cookie(getJSessionCookie(jSessionId));
        }
        final AsyncAdminCommandInvoker<Response> invoker = detach
            ? new DetachedSseAdminCommandInvoker(invocation, builder)
            : new SseAdminCommandInvoker(invocation, builder);
        return invoker.start();
    }

    private Response executeCommand(CommandName commandName, Payload.Inbound inbound, ParameterMap params, boolean supportsMultiparResult,
            String xIndentHeader, String modelETag, Cookie jSessionId) throws WebApplicationException {
        RestLogging.restLogger.log(Level.FINEST, "executeCommand({0})", commandName);
        CommandModel model = getCommandModel(commandName);
        checkCommandModelETag(model, modelETag);
        final boolean notify = params == null ? false : params.containsKey("notify");
        final boolean detach = params == null ? false : params.containsKey("detach");
        final RestPayloadImpl.Outbound outbound = new RestPayloadImpl.Outbound(false);
        final ActionReporter actionReporter = new PropsFileActionReporter();
        // new RestActionReporter()
        // - must use PropsFileActionReporter because some commands react differently on it :-(
        final CommandInvocation<?> commandInvocation = getCommandRunner().getCommandInvocation(commandName.getScope(),
                commandName.getName(), actionReporter, getSubject(), notify, detach);
        if (inbound != null) {
            commandInvocation.inbound(inbound);
        }
        commandInvocation.outbound(outbound).parameters(params).execute();
        fixActionReporterSpecialCases(actionReporter);
        ActionReport.ExitCode exitCode = actionReporter.getActionExitCode();
        int status = HTTP_OK;
        if (exitCode == ActionReport.ExitCode.FAILURE) {
            status = HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        ResponseBuilder rb = Response.status(status);
        if (xIndentHeader != null) {
            rb.header("X-Indent", xIndentHeader);
        }
        if (supportsMultiparResult && outbound.size() > 0) {
            ParamsWithPayload pwp = new ParamsWithPayload(outbound, actionReporter);
            rb.entity(pwp);
        } else {
            rb.type(MediaType.APPLICATION_JSON_TYPE);
            rb.entity(actionReporter);
        }
        if (isSingleInstanceCommand(model)) {
            rb.cookie(getJSessionCookie(jSessionId));
        }
        return rb.build();
    }

    /**
     * Some ActionReporters has special logic which must be reflected here
     */
    private void fixActionReporterSpecialCases(ActionReporter ar) {
        if (ar == null) {
            return;
        }
        if (ar instanceof PlainTextActionReporter) {
            PlainTextActionReporter par = (PlainTextActionReporter) ar;
            StringBuilder finalOutput = new StringBuilder();
            par.getCombinedMessages(par, finalOutput);
            String outs = finalOutput.toString();
            if (!StringUtils.ok(outs)) {
                par.getTopMessagePart().setMessage(strings.getLocalString("get.mon.no.data", "No monitoring data to report.") + "\n");
            }
        }
    }

    /**
     * This will create a unique SessionId, Max-Age,Version,Path to be added to the Set-Cookie header
     */
    public NewCookie getJSessionCookie(Cookie jSessionId) {
        String value;
        // If the request has a Cookie header and
        // there is no failover then send back the same
        // JSESSIONID
        if (jSessionId != null && isJSessionCookieOk(jSessionId.getValue())) {
            value = jSessionId.getValue();
        } else {
            value = uuidGenerator.generateUuid() + '.' + getServerName();
        }
        NewCookie result = new NewCookie(SESSION_COOKIE_NAME, value, "/command", null, null, MAX_AGE, false);
        return result;
    }

    private boolean isJSessionCookieOk(String value) {
        if (!StringUtils.ok(value)) {
            return false;
        }
        return value.endsWith("." + getServerName());
    }

    private static boolean isSingleInstanceCommand(CommandModel model) {
        if (model != null) {
            ExecuteOn executeOn = model.getClusteringAttributes();
            if ((executeOn != null) && (executeOn.value().length == 1)
                    && executeOn.value()[0].equals(org.glassfish.api.admin.RuntimeType.SINGLE_INSTANCE)) {
                return true;
            }
        }
        return false;
    }

    private static String leadingSpacesToNbsp(String str) {
        if (str == null) {
            return null;
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                StringBuilder sb = new StringBuilder((i * 6) + (str.length() - i));
                for (int j = 0; j < i; j++) {
                    sb.append("&nbsp;");
                }
                sb.append(str.substring(i));
                return sb.toString();
            }
        }
        return str;
    }

    private CommandModel getCommandModel(CommandName commandName) throws WebApplicationException {
        CommandRunner cr = getCommandRunner();
        CommandModel model = cr.getModel(commandName.getScope(), commandName.getName());
        if (model == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
                    .entity(strings.getLocalString("adapter.command.notfound",
                            "Command {0} not found. \nCheck the entry of command name. This command may be provided by a package that is not installed.",
                            commandName.getName()))
                    .build());
        }
        return model;
    }

    private BufferedReader getManPageReader(CommandName commandName) throws WebApplicationException {
        CommandModel model = getCommandModel(commandName);
        return getCommandRunner().getHelp(model);
    }

    private CommandRunner<AdminCommandJob> getCommandRunner() {
        if (this.commandRunner == null) {
            commandRunner = getHabitat().getService(CommandRunner.class);
        }
        return this.commandRunner;
    }

    private ServiceLocator getHabitat() {
        return Globals.getDefaultHabitat();
    }

    private String getServerName() {
        if (serverName == null) {
            Server server = getHabitat().getService(Server.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
            if (server != null) {
                serverName = server.getName();
            }
        }
        return serverName;
    }

    private Subject getSubject() {
        return subjectRef.get();
    }

    private static class CommandName {
        private String scope;
        private String name;

        public CommandName(String fullName) {
            if (fullName == null) {
                return;
            }
            int ind = fullName.indexOf('/');
            if (ind > 0) {
                this.scope = fullName.substring(0, ind + 1);
                this.name = fullName.substring(ind + 1);
            } else {
                this.name = fullName;
            }
        }

        public String getName() {
            return name;
        }

        public String getScope() {
            return scope;
        }

        @Override
        public String toString() {
            if (this.scope == null) {
                return "CommandName[" + name + "]";
            }
            return "CommandName[" + scope + name + "]";
        }
    }
}
