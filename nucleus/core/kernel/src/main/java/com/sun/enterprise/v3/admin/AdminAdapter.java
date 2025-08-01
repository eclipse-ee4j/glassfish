/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.LogHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.uuid.UuidGenerator;
import com.sun.enterprise.util.uuid.UuidGeneratorImpl;
import com.sun.enterprise.v3.admin.adapter.AdminEndpointDecider;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.http.util.CookieSerializerUtils;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.AdminAccessController;
import org.glassfish.internal.api.Privacy;
import org.glassfish.internal.api.RemoteAdminAccessException;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.server.ServerEnvironmentImpl;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Listen to admin commands...
 * @author dochez
 */
public abstract class AdminAdapter extends StaticHttpHandler implements Adapter, PostConstruct, EventListener {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(AdminAdapter.class);
    private static final Logger LOG = KernelLoggerInfo.getLogger();

    private static final String BASIC = "Basic ";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private static final int MAX_AGE = 86400 ;
    private static final String ASADMIN_PATH="/__asadmin";
    private static final String QUERY_STRING_SEPARATOR = "&";

    @Inject
    ModulesRegistry modulesRegistry;

    @Inject
    CommandRunnerImpl commandRunner;

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    Events events;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    private AdminEndpointDecider epd;

    @Inject
    ServerContext sc;

    @Inject
    ServiceLocator habitat;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    volatile AdminService as;

    @Inject
    volatile Domain domain;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private volatile Server server;

    @Inject
    AdminAccessController authenticator;

    final Class<? extends Privacy> privacyClass;

    private boolean isRegistered;

    CountDownLatch latch = new CountDownLatch(1);

    protected AdminAdapter(Class<? extends Privacy> privacyClass) {
        super((Set<String>) null);
        this.privacyClass = privacyClass;
    }

    @Override
    public final HttpHandler getHttpService() {
        return this;
    }

    @Override
    public void postConstruct() {
        events.register(this);

        epd = new AdminEndpointDecider(config);
        addDocRoot(env.getInstanceRoot().getAbsolutePath() + "/asadmindocroot/");
    }

    /**
     * Call the service method, and notify all listeners
     *
     * @exception Exception if an error happens during handling of
     *   the request. Common errors are:
     *   <ul><li>IOException if an input/output error occurs and we are
     *   processing an included servlet (otherwise it is swallowed and
     *   handled by the top level error handler mechanism)
     *       <li>ServletException if a servlet throws an exception and
     *  we are processing an included servlet (otherwise it is swallowed
     *  and handled by the top level error handler mechanism)
     *  </ul>
     *  Tomcat should be able to handle and log any other exception ( including
     *  runtime exceptions )
     */
    @Override
    public void onMissingResource(Request req, Response res) {

        LOG.log(Level.FINER, "Received something on {0}", req.getRequestURI());
        LOG.log(Level.FINER, "QueryString = {0}", req.getQueryString());

        HttpStatus statusCode = HttpStatus.OK_200;

        String requestURI = req.getRequestURI();
        ActionReport report = getClientActionReport(requestURI, req);
        // remove the qualifier if necessary
        if (requestURI.indexOf('.')!=-1) {
            requestURI = requestURI.substring(0, requestURI.indexOf('.'));
        }

        Payload.Outbound outboundPayload = PayloadImpl.Outbound.newInstance();

        try {
            if (!latch.await(20L, TimeUnit.SECONDS)) {
                report = getClientActionReport(req.getRequestURI(), req);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage("V3 cannot process this command at this time, please wait");
            } else {

                final Subject s = (authenticator == null) ? null : authenticator.loginAsAdmin(req);
                if (s == null) {
                    reportAuthFailure(res, report, "adapter.auth.userpassword",
                        "Invalid user name or password",
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        "WWW-Authenticate", "BASIC");
                    return;
                }
                report = doCommand(requestURI, req, report, outboundPayload, s);
            }
        } catch (ProcessHttpCommandRequestException reqEx) {
            report = reqEx.getReport();
            statusCode = reqEx.getResponseStatus();
        } catch(InterruptedException e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("V3 cannot process this command at this time, please wait");
        } catch (Exception e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage("Exception while processing command: " + e);
        }

        try {
            res.setStatus(statusCode);
            /*
             * Format the command result report into the first part (part #0) of
             * the outbound payload and set the response's content type based
             * on the payload's.  If the report is the only part then the
             * stream will be written as content type text/something and
             * will contain only the report.  If the payload already has
             * content - such as files to be downloaded, for example - then the
             * content type of the payload reflects its multi-part nature and
             * an implementation-specific content type will be set in the response.
             */
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            report.writeReport(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final Properties reportProps = new Properties();
            reportProps.setProperty("data-request-type", "report");
            outboundPayload.addPart(0, report.getContentType(), "report", reportProps, bais);
            res.setContentType(outboundPayload.getContentType());
            String commandName = req.getRequestURI().substring(getContextRoot().length() + 1);
            //Check session routing for commands that have @ExecuteOn(RuntimeType.SINGLE_INSTANCE)
            if ( isSingleInstanceCommand(commandName)) {
                res.addHeader(SET_COOKIE_HEADER, getCookieHeader(req));
            }
            outboundPayload.writeTo(res.getOutputStream());
            res.getOutputStream().flush();
            res.finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method checks if the request has a Cookie header and
     * if the instance name serving the request is the same as the
     * jvmRoute information
     * @param req Request to examine the Cookie header
     * @return true if the Cookie header is set and the jvmRoute information is  the same as
     * the instance serving the request , false otherwise
     *
     */
    public boolean hasCookieHeader(Request req) {

        String[] nameValuePair = getJSESSIONIDHeaders(req);
        if (nameValuePair != null )  {
            String headerValue = nameValuePair[1];

            int index = headerValue.lastIndexOf('.');
            return  headerValue.substring(index+1)
                    .equals(server.getName())? true : false;

        }
        return false;
    }

    /**
     * This method will return the Cookie header with name JSESSIONID="..."
     * @param req  The request which may contain cookie headers
     * @return  cookie header
     */
    public String[] getJSESSIONIDHeaders(Request req) {
         for (String header : req.getHeaders("Cookie")){

            String cookieHeaders[] = header.trim().split(";");
            for (String cookieHeader:cookieHeaders) {
                String[] nameValuePair = cookieHeader.trim().split("=");
                if (nameValuePair[0].equals(SESSION_COOKIE_NAME)) {
                    return nameValuePair;
                }
            }

         }
        return null;

    }

    /**
     * This method checks if this command has @ExecuteOn annotation with
     * RuntimeType.SINGle_INSTANCE
     * @param commandName  the command which is executed
     * @return  true only if @ExecuteOn has RuntimeType.SINGLE_INSTANCE false for
     * other cases
     */
    public boolean isSingleInstanceCommand(String commandName) {

        CommandModel model = commandRunner.getModel(getScope(commandName), getCommandAfterScope(commandName));
        if (model != null ) {
            ExecuteOn executeOn = model.getClusteringAttributes();
            if ((executeOn != null) && (executeOn.value().length ==1) &&
                    executeOn.value()[0].equals(RuntimeType.SINGLE_INSTANCE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This will create a unique SessionId, Max-Age,Version,Path to be added to the Set-Cookie header
     * @return Set-Cookie2 header
     */
    public String getCookieHeader(Request req) {
        String sessionId = null;
        // If the request has a Cookie header and
        // there is no failover then send back the same
        // JSESSIONID in  Set-Cookie2 header
        if ( hasCookieHeader(req)) {
            sessionId = getJSESSIONIDHeaders(req)[1];
        }  else {
            //There is no Cookie header in request so generate a new JSESSIONID  or
            //failover has occured in which case you can generate a new JSESSIONID
            sessionId = createSessionId();
        }
        StringBuilder sb = new StringBuilder();
        final Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        cookie.setMaxAge(MAX_AGE);
        cookie.setPath(ASADMIN_PATH);
        cookie.setVersion(1);
        CookieSerializerUtils.serializeServerCookie(sb, true, false, false, cookie);
        return sb.toString();

    }

    /**
     * This will create a new sessionId and add the server name as a jvmroute information to it
     * @return String to be used for the JSESSIONID Set-Cookie2 header
     */

    public String createSessionId(){
        UuidGenerator uuidGenerator = new UuidGeneratorImpl();
        StringBuilder sessionBuf = new StringBuilder();
        String sessionId = uuidGenerator.generateUuid();
        sessionBuf.append(sessionId).append('.').append(server.getName());
        return sessionBuf.toString();
    }

    public AdminAccessController.Access authenticate(Request req) throws Exception {
        /*
         * At this point, this method should be obsolete.  But in case it
         * comes back to life it now conforms to the new API for loginAsAdmin.
         * That is, loginAsAdmin throws a RemoteAdminAccessException if the
         * request is remote but secure admin is disabled and it throws a
         * LoginException if the user is not a legitimate administrator.
         * Further, loginAsAdmin now does nothing regarding full vs. read-only
         * access; those decisions are made during authorization of particular
         * commands.
         */
        try {
            authenticator.loginAsAdmin(req);
            return (env.isDas() ? AdminAccessController.Access.FULL : AdminAccessController.Access.READONLY);
        } catch (RemoteAdminAccessException ex) {
            return AdminAccessController.Access.FORBIDDEN;
        } catch (LoginException ex) {
            return AdminAccessController.Access.NONE;
        }
    }


    /** A convenience method to extract user name from a request. It assumes the HTTP Basic Auth.
     *
     * @param req instance of Request
     * @return a two-element string array. If Auth header exists and can be correctly decoded, returns the user name
     *   and password as the two elements. If any error occurs or if the header does not exist, returns an array with
     *   two blank strings. Never returns a null.
     * @throws IOException in case of error with decoding the buffer (HTTP basic auth)
     */
    public static String[] getUserPassword(Request req) throws IOException {
        //implementation note: other adapters make use of this method
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            return new String[]{"", ""};
        }
        String enc = authHeader.substring(BASIC.length());
        String dec = new String(Base64.getDecoder().decode(enc), UTF_8);
        int i = dec.indexOf(':');
        if (i < 0) {
            return new String[] { "", "" };
        }
        return new String[] { dec.substring(0, i), dec.substring(i + 1) };
    }

    private void reportAuthFailure(final Response res,
            final ActionReport report,
            final String msgKey,
            final String msg,
            final int httpStatus,
            final String headerName,
            final String headerValue) throws IOException {
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        final String messageForResponse = I18N.getLocalString(msgKey, msg);
        report.setMessage(messageForResponse);
        report.setActionDescription("Authentication error");
        res.setStatus(httpStatus, messageForResponse);
        if (headerName != null) {
            res.setHeader(headerName, headerValue);
        }
        res.setContentType(report.getContentType());
        report.writeReport(res.getOutputStream());
        res.getOutputStream().flush();
        res.finish();
    }

    private ActionReport getClientActionReport(String requestURI, Request req) {


        ActionReport report=null;

        // first we look at the command extension (ie list-applications.[json | html | mf]
        if (requestURI.indexOf('.')!=-1) {
            String qualifier = requestURI.substring(requestURI.indexOf('.')+1);
            report = habitat.getService(ActionReport.class, qualifier);
        } else {
            String userAgent = req.getHeader("User-Agent");
            if (userAgent!=null) {
                report = habitat.getService(ActionReport.class, userAgent.substring(userAgent.indexOf('/')+1));
            }
            if (report==null) {
                String accept = req.getHeader("Accept");
                if (accept!=null) {
                    StringTokenizer st = new StringTokenizer(accept, ",");
                    while (report==null && st.hasMoreElements()) {
                        final String scheme=st.nextToken();
                        report = habitat.getService(ActionReport.class, scheme.substring(scheme.indexOf('/')+1));
                    }
                }
            }
        }
        if (report==null) {
            // get the default one.
            report = habitat.getService(ActionReport.class, "html");
        }
        return report;
    }

    protected abstract boolean validatePrivacy(AdminCommand command);

    private ActionReport doCommand(String requestURI, Request req, ActionReport report,
            Payload.Outbound outboundPayload, Subject subject) throws ProcessHttpCommandRequestException {

        if (!requestURI.startsWith(getContextRoot())) {
            String msg = I18N.getLocalString("adapter.panic",
                    "Wrong request landed in AdminAdapter {0}", requestURI);
            report.setMessage(msg);
            LogHelper.getDefaultLogger().info(msg);
            return report;
        }

        // wbn handle no command and no slash-suffix
        String command ="";
        if (requestURI.length() > getContextRoot().length() + 1) {
            command = requestURI.substring(getContextRoot().length() + 1);
        }

        String scope = getScope(command);
        command = getCommandAfterScope(command);

        String qs = req.getQueryString();
        final ParameterMap parameters = extractParameters(qs);
        String passwordOptions = req.getHeader("X-passwords");
        if (passwordOptions != null) {
            decodePasswords(parameters, passwordOptions);
        }

        try {
            Payload.Inbound inboundPayload = PayloadImpl.Inbound
                .newInstance(req.getContentType(), req.getInputStream());
            LOG.log(Level.FINE, "***** AdminAdapter {0}  *****", req.getMethod());
            AdminCommand adminCommand = commandRunner.getCommand(scope, command, report);
            if (adminCommand==null) {
                // maybe commandRunner already reported the failure?
                if (report.getActionExitCode() == ActionReport.ExitCode.FAILURE) {
                    return report;
                }
                String message = I18N.getLocalString("adapter.command.notfound", "Command {0} not found", command);
                // cound't find command, not a big deal
                LOG.log(Level.FINE, message);
                report.setMessage(message);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return report;
            }
            //Validate admin command eTag
            String modelETag = req.getHeader(RemoteRestAdminCommand.COMMAND_MODEL_MATCH_HEADER);
            if (modelETag != null && !commandRunner.validateCommandModelETag(adminCommand, modelETag)) {
                String message =
                    I18N.getLocalString("commandmodel.etag.invalid",
                        "Cached command model for command {0} is invalid.", command);
                LOG.log(Level.FINE, message);
                report.setMessage(message);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                throw new ProcessHttpCommandRequestException(report, HttpStatus.PRECONDITION_FAILED_412);
            }
            //Execute
            if (validatePrivacy(adminCommand)) {
                CommandInvocation<AdminCommandJob> inv = commandRunner.getCommandInvocation(scope, command, report,
                    subject, parameters.containsKey("notify"), parameters.containsKey("detach"));
                LOG.log(Level.INFO, "Executing command {0} with parameters {1}, outboundPayload: {2}",
                    new Object[] {command, parameters, outboundPayload});
                inv.parameters(parameters).inbound(inboundPayload).outbound(outboundPayload).execute();
                LOG.log(Level.INFO, "Command {0} executed, outboundPayload: {1}",
                    new Object[] {command, outboundPayload});
                try {
                    // note it has become extraordinarily difficult to change the reporter!
                    CommandRunnerExecutionContext inv2 = (CommandRunnerExecutionContext) inv;
                    report = inv2.report();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Exception while executing command " + command, e);
                }
            } else {
                report.failure( LOG,
                                I18N.getLocalString("adapter.wrongprivacy",
                                    "Command {0} does not have {1} visibility",
                                    command, privacyClass.getSimpleName().toLowerCase(Locale.ENGLISH)),
                                null);
                return report;

            }
        } catch (ProcessHttpCommandRequestException reqEx) {
            throw reqEx;
        } catch (Throwable t) {
            /*
             * Must put the error information into the report
             * for the client to see it.
             */
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(t);
            report.setMessage(t.getLocalizedMessage());
            report.setActionDescription("Last-chance AdminAdapter exception handler");
        }
        return report;
    }

    /**
     * Finish the response and recycle the request/response tokens. Base on
     * the connection header, the underlying socket transport will be closed
     */
    public void afterService(Request req, Response res) throws Exception {
    }

    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Adapter.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data) {
    }

    /**
     * decode the parameters that were passed in the X-Passwords header
     *
     * @params requestString value of the X-Passwords header
     * @returns a decoded requestString
     */
    void decodePasswords(ParameterMap pmap, final String requestString) {
        StringTokenizer stoken = new StringTokenizer(requestString == null ? "" : requestString, QUERY_STRING_SEPARATOR);
        while (stoken.hasMoreTokens()) {
            String token = stoken.nextToken();
            if (token.indexOf("=") == -1) {
                continue;
            }
            String paramName = token.substring(0, token.indexOf("="));
            String value = token.substring(token.indexOf("=") + 1);
            try {
                value = new String(Base64.getUrlDecoder().decode(value), UTF_8);
            } catch (IllegalArgumentException e) {
                LOG.log(Level.WARNING, KernelLoggerInfo.cantDecodeParameter,
                        new Object[] { paramName, value });
                continue;
            }
            pmap.add(paramName, value);
        }

    }

    /**
     *  extract parameters from URI and save it in ParameterMap obj
     *
     *  @params requestString string URI to extract
     *
     *  @returns ParameterMap
     */
    ParameterMap extractParameters(final String requestString) {
        // extract parameters...
        final ParameterMap parameters = new ParameterMap();
        StringTokenizer stoken = new StringTokenizer(requestString == null ? "" : requestString, QUERY_STRING_SEPARATOR);
        while (stoken.hasMoreTokens()) {
            String token = stoken.nextToken();
            if (token.indexOf("=") == -1) {
                continue;
            }
            String paramName = token.substring(0, token.indexOf("="));
            String value = token.substring(token.indexOf("=") + 1);
            try {
                value = URLDecoder.decode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.log(Level.WARNING, KernelLoggerInfo.cantDecodeParameter,
                        new Object[] {paramName, value});
            }

            parameters.add(paramName, value);
        }

        // Dump parameters...
        if (LOG.isLoggable(Level.FINER)) {
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                for (String v : entry.getValue()) {
                    LOG.log(Level.FINER, "Key {0} = {1}", new Object[]{entry.getKey(), v});
                }
            }
        }
        return parameters;
    }

    @Override
    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event<?> event) {
        if (event.is(EventTypes.SERVER_READY)) {
            latch.countDown();
            LOG.fine("Ready to receive administrative commands");
        }
        //the count-down does not start if any other event is received
    }


    @Override
    public int getListenPort() {
        return epd.getListenPort();
    }

    @Override
    public InetAddress getListenAddress() {
        return epd.getListenAddress();
    }

    @Override
    public List<String> getVirtualServers() {
        return epd.getAsadminHosts();
    }

    /**
     * Checks whether this adapter has been registered as a network endpoint.
     */
    @Override
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Marks this adapter as having been registered or unregistered as a
     * network endpoint
     */
    @Override
    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    /**
     * A command is defined in a particular scope by
     * using a prefix on the command service names, as in @Service(name="ascope/mycommand")
     * This method gets the scope for a command which is "ascope/"
     * for the above example
     * @param command  The command to be executed
     * @return the scope for a command
     */
    private String getScope(String command) {
        int ci = command.indexOf("/");
        return (ci != -1) ? command.substring(0, ci + 1) : null;
    }


    /**
     * This method gets the command after the scope string
     * as defined for a command like this @Service(name="ascope/mycommand")
     * @param command  The command to be executed
     * @return the shortened command after the scope ie "mycommand"
     * for the above example
     */
    private String getCommandAfterScope(String command) {
        int ci = command.indexOf("/");
        return (ci != -1) ? command = command.substring(ci + 1) : command;

    }
}
