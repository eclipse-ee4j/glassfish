/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.remote;

import com.sun.enterprise.admin.event.AdminCommandEventBrokerImpl;
import com.sun.enterprise.admin.remote.reader.CliActionReport;
import com.sun.enterprise.admin.remote.reader.ProprietaryReaderFactory;
import com.sun.enterprise.admin.remote.sse.GfSseInboundEvent;
import com.sun.enterprise.admin.remote.writer.ProprietaryWriterFactory;
import com.sun.enterprise.admin.util.AdminLoggerInfo;
import com.sun.enterprise.admin.util.AuthenticationInfo;
import com.sun.enterprise.admin.util.CachedCommandModel;
import com.sun.enterprise.admin.util.HttpConnectorAddress;
import com.sun.enterprise.admin.util.cache.AdminCacheUtils;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.net.ssl.SSLException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AuthenticationException;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandModel.ParamModel;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.InvalidCommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.common.util.admin.AuthTokenManager;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Utility class for executing remote admin commands. Each instance of RemoteAdminCommand represents a particular remote
 * command on a particular remote server accessed using particular credentials. The instance can be reused to execute
 * the same command multiple times with different arguments.
 * <p>
 * Arguments to the command are supplied using a ParameterMap passed to the executeCommand method. ParameterMap is a
 * MultiMap where each key can have multiple values, although this class only supports a single value for each option.
 * Operands for the command are stored as the option named "DEFAULT" and can have multiple values.
 * <p>
 * Before a command can be executed, the metadata for the command (in the form of a CommandModel) is required. The
 * getCommandModel method will fetch the metadata from the server, save it, and return it. If the CommandModel for a
 * command is known independently (e.g., stored in a local cache, or known a priori), it can be set using the
 * setCommandModel method. If the metadata isn't known when the exectureCommand method is called, it will fetch the
 * metadata from the server before executing the command.
 * <p>
 * Any files returned by the command will be stored in the current directory. The setFileOutputDirectory method can be
 * used to control where returned files are saved.
 *
 * <p>
 * <b>This implementation is now in retention period. All content was migrated to RemoteRestAdminCommand. This
 * implementation will be removed just after all necessary changes and tests will be done.</b>
 */
//Fork of RemoteAdminCommand
public class RemoteRestAdminCommand extends AdminCommandEventBrokerImpl<GfSseInboundEvent> {

    private static final LocalStringsImpl strings = new LocalStringsImpl(RemoteRestAdminCommand.class);

    private static final String ADMIN_URI_PATH = "/command/";
    private static final String COMMAND_NAME_REGEXP = "^[a-zA-Z_][-a-zA-Z0-9_]*$";
    private static final String READ_TIMEOUT = "AS_ADMIN_READTIMEOUT";
    public static final String COMMAND_MODEL_MATCH_HEADER = "X-If-Command-Model-Match";
    private static final String EOL = StringUtils.EOL;
    private static final int defaultReadTimeout; // read timeout for URL conns

    private String responseFormatType = "hk2-agent";
    // return output string rather than printing it
    protected String output;
    private Map<String, String> attrs;
    private boolean doUpload;
    private RestPayloadImpl.Outbound outboundPayload;
    private File fileOutputDir;
    private StringBuilder passwordOptions;
    private String manpage;
    private String cmduri;
    private ActionReport actionReport;

    // constructor parameters
    protected String name;
    protected final String host;
    private String canonicalHostCache;
    protected final int port;
    protected boolean secure;
    private final boolean detach;
    private final boolean notify;
    protected String user;
    protected char[] password;
    protected Logger logger;
    protected String scope;
    protected String authToken = null;
    protected boolean prohibitDirectoryUploads = false;

    // executeCommand parameters
    protected ParameterMap options;
    protected List<String> operands;

    private CommandModel commandModel;
    private boolean commandModelFromCache;
    private int readTimeout = defaultReadTimeout;
    private int connectTimeout = -1;
    private boolean interactive = true;

//    private final List<Header> requestHeaders = new ArrayList<>();
    private final AtomicBoolean closeSse = new AtomicBoolean();

    private boolean enableCommandModelCache = true;

    private final String commandCacheKey;

    /*
     * Set a default read timeout for URL connections.
     */
    static {
        String rt = System.getProperty(READ_TIMEOUT);
        if (rt == null) {
            rt = System.getenv(READ_TIMEOUT);
        }
        if (rt != null) {
            defaultReadTimeout = Integer.parseInt(rt);
        } else {
            defaultReadTimeout = 10 * 60 * 1000; // 10 minutes
        }
    }

    /**
     * content-type used for each file-transfer part of a payload to or from the server
     */
    private static final String FILE_PAYLOAD_MIME_TYPE = "application/octet-stream";

    public RemoteRestAdminCommand(String name, String host, int port, boolean secure, String user, char[] password,
        Logger logger, boolean notify, boolean detach) throws CommandException {
        this(name, host, port, secure, user, password, logger, null, null, false, notify, detach);
    }

    /**
     * Construct a new remote command object. The command and arguments are supplied later using the execute method in the
     * superclass.
     */
    public RemoteRestAdminCommand(String name, String host, int port, boolean secure, String user, char[] password,
        Logger logger, final String scope, final String authToken, final boolean prohibitDirectoryUploads,
        boolean notify, boolean detach) throws CommandException {
        this.name = name;
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.notify = notify;
        this.detach = detach;
        this.user = user;
        this.password = password;
        this.logger = logger;
        this.scope = scope;
        this.authToken = authToken;
        this.prohibitDirectoryUploads = prohibitDirectoryUploads;
        checkName();
        this.commandCacheKey = AdminCacheUtils.createCommandCacheKey(name, getCanonicalHost(), port);
    }

    /**
     * Make sure the command name is legitimate and won't allow any URL spoofing attacks.
     */
    private void checkName() throws CommandException {
        if (!name.matches(COMMAND_NAME_REGEXP)) {
            throw new CommandException("Illegal command name: " + name);
        }
    }

    public void closeSse(String message, ActionReport.ExitCode exitCode) {
        ActionReport report = new CliActionReport();
        report.setMessage(message);
        report.setActionExitCode(exitCode);
        setActionReport(report);
        this.closeSse.set(true);
    }

    /**
     * Set the response type used in requests to the server. The response type is sent in the User-Agent HTTP header and
     * tells the server what format of response to produce.
     */
    public void setResponseFormatType(String responseFormatType) {
        this.responseFormatType = responseFormatType;
    }

    /**
     * If set, the raw response from the command is written to the specified stream.
     */
    public void setUserOut(OutputStream userOut) {
    }

    /**
     * Set the CommandModel used by this command. Normally the CommandModel will be fetched from the server using the
     * getCommandModel method, which will also save the CommandModel for further use. If the CommandModel is known in
     * advance, it can be set with this method and avoid the call to the server.
     */
    public void setCommandModel(CommandModel commandModel) {
        this.commandModel = commandModel;
        this.commandModelFromCache = false;
    }

    /**
     * Set the read timeout for the URLConnection.
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public static int getReadTimeout() {
        return defaultReadTimeout;
    }

    public String findPropertyInReport(String key) {
        if (actionReport == null) {
            return null;
        }
        return actionReport.findProperty(key);
    }

    /**
     * Set the connect timeout for the URLConnection.
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Set the interactive mode for the command. By default, the command is interactive.
     */
    public void setInteractive(boolean state) {
        this.interactive = state;
    }

    public void setEnableCommandModelCache(boolean enableCommandModelCache) {
        this.enableCommandModelCache = enableCommandModelCache;
    }

    /**
     * Get the CommandModel for the command from the server. If the CommandModel hasn't been set, it's fetched from the
     * server.
     *
     * @return the model for the command
     * @throws CommandException if the server can't be contacted
     */
    public CommandModel getCommandModel() throws CommandException {
        if (commandModel == null && enableCommandModelCache) {
            long startNanos = System.nanoTime();
            try {
                commandModel = CommandModelHttpCommand.fromCache(commandCacheKey, detach, notify);
                if (commandModel != null) {
                    this.commandModelFromCache = true;
                    if (logger.isLoggable(FINEST)) {
                        logger.log(FINEST,
                                "Command model for command {0} was successfully loaded from the cache. [Duration: {1} nanos]",
                                new Object[] { name, System.nanoTime() - startNanos });
                    }
                } else {
                    if (logger.isLoggable(FINEST)) {
                        logger.log(FINEST, "Command model for command {0} is not in cache. It must be fatched from server.", name);
                    }
                }
            } catch (Exception ex) {
                logger.log(FINEST, "Can not get data from cache under key " + commandCacheKey, ex);
            }
        }
        if (commandModel == null) {
            fetchCommandModel();
        }
        return commandModel;
    }

    /**
     * If command model was load from local cache.
     */
    public boolean isCommandModelFromCache() {
        return commandModelFromCache;
    }

    /**
     * Set the directory in which any returned files will be stored. The default is the user's home directory.
     */
    public void setFileOutputDirectory(File dir) {
        fileOutputDir = dir;
    }
//
//    /**
//     * Return a modifiable list of headers to be added to the request.
//     */
//    public List<Header> headers() {
//        return requestHeaders;
//    }

    protected boolean useSse() throws CommandException {
        return getCommandModel().isManagedJob();
    }

    /**
     * Run the command using the specified arguments. Return the output of the command.
     */
    public String executeCommand(ParameterMap opts) throws CommandException {
        logger.log(FINER, "RemoteRestAdminCommand.executeCommand() - name: {0}", this.name);
        //Just to be sure. Cover get help
        if (opts != null && opts.size() == 1 && opts.containsKey("help")) {
            return getManPage();
        }
        ParameterMap params = processParams(opts);
        logger.log(FINEST, "Processed parameters: {0}", params);
        boolean retry;
        do { //Cache update cycle
            retry = false;
            try {
                executeRemoteCommand(params);
            } catch (CommandValidationException mve) {
                if (refetchInvalidModel() && isCommandModelFromCache()) {
                    fetchCommandModel();
                    retry = true;
                } else {
                    throw mve;
                }
            }
            return output;
        } while (retry);
    }

    private ParameterMap processParams(ParameterMap opts) throws CommandException {
        if (opts == null) {
            opts = new ParameterMap();
        }
        // first, make sure we have the command model
        getCommandModel();
        // XXX : This is to take care of camel case from ReST calls that
        // do not go through usual CLI path
        // XXX : This is not clean; this should be handled the same way
        // it is handled for incoming CLI commands
        options = new ParameterMap();
        for (Map.Entry<String, List<String>> o : opts.entrySet()) {
            String key = o.getKey();
            List<String> value = o.getValue();
            options.set(key.toLowerCase(Locale.ENGLISH), value);
        }
        operands = options.get("default"); // "DEFAULT".toLowerCase()

        try {
            initializeDoUpload();

            // if uploading, we need a payload
            if (doUpload) {
                outboundPayload = new RestPayloadImpl.Outbound(true);
            } else {
                outboundPayload = null;
            }

            ParameterMap result = new ParameterMap();
            ParamModel operandParam = null;
            for (ParamModel opt : commandModel.getParameters()) {
                if (opt.getParam().primary()) {
                    operandParam = opt;
                    continue;
                }
                String paramName = opt.getName();

                List<String> paramValues = new ArrayList<>(options.get(paramName.toLowerCase(Locale.ENGLISH)));
                if (!opt.getParam().alias().isEmpty() && !paramName.equalsIgnoreCase(opt.getParam().alias())) {
                    paramValues.addAll(options.get(opt.getParam().alias().toLowerCase(Locale.ENGLISH)));
                }
                if (!opt.getParam().multiple() && paramValues.size() > 1) {
                    throw new CommandException(strings.get("tooManyOptions", paramName));
                }
                if (paramValues.isEmpty()) {
                    // perhaps it's set in the environment?
                    String envValue = getFromEnvironment(paramName);
                    if (envValue != null) {
                        paramValues.add(envValue);
                    }
                }
                if (paramValues.isEmpty()) {
                    /*
                     * Option still not set.  Note that we ignore the default
                     * value and don't send it explicitly on the assumption
                     * that the server will supply the default value itself.
                     *
                     * If the missing option is required, that's an error,
                     * which should never happen here because validate()
                     * should check it first.
                     */
                    if (!opt.getParam().optional()) {
                        throw new CommandException("Option --" + opt.getName() + " is required but was not specified");
                    }
                    // optional param not set, skip it
                    continue;
                }
                for (String paramValue : paramValues) {
                    if (opt.getType() == File.class || opt.getType() == File[].class) {
                        addFileOption(result, paramName, paramValue);
                    } else {
                        result.add(paramName, paramValue);
                    }
                }
            }

            // add operands
            for (String operand : operands) {
                if (operandParam.getType() == File.class || operandParam.getType() == File[].class) {
                    addFileOption(result, "DEFAULT", operand);
                } else {
                    result.add("DEFAULT", operand);
                }
            }
            return result;
        } catch (IOException ioex) {
            // possibly an error caused while reading or writing a file?
            throw new CommandException("I/O Error", ioex);
        }
    }

    /**
     * If admin model is invalid, will be automatically refetched?
     */
    protected boolean refetchInvalidModel() {
        return true;
    }

    /**
     * After a successful command execution, the attributes returned by the command are saved. This method returns those
     * saved attributes.
     */
    public Map<String, String> getAttributes() {
        return attrs;
    }

    /**
     * Return true if we're successful in collecting new information (and thus the caller should try the request again).
     * Subclasses can override to (e.g.) collect updated authentication information by prompting the user. The
     * implementation in this class returns false, indicating that the authentication information was not updated.
     */
    protected boolean updateAuthentication() {
        return false;
    }

    /**
     * Subclasses can override to supply parameter values from environment. The implementation in this class returns null,
     * indicating that the name is not available in the environment.
     */
    protected String getFromEnvironment(String name) {
        return null;
    }

    /**
     * Called when a non-secure connection attempt fails and it appears that the server requires a secure connection.
     * Subclasses can override to indicate that the connection should The implementation in this class returns false,
     * indicating that the connection should not be retried.
     */
    protected boolean retryUsingSecureConnection(String host, int port) {
        return false;
    }

    /**
     * Return the error message to be used in the AuthenticationException. Subclasses can override to provide a more
     * detailed message, for example, indicating the source of the password that failed. The implementation in this class
     * returns a default error message.
     */
    protected String reportAuthenticationException() {
        return strings.get("InvalidCredentials", user);
    }

    /**
     * Get the URI for executing the command.
     */
    protected String getCommandURI() {
        if (cmduri == null) {
            StringBuilder rv = new StringBuilder(ADMIN_URI_PATH);
            if (scope != null) {
                rv.append(scope);
            }
            rv.append(name);
            cmduri = rv.toString();
        }
        return cmduri;
    }

    /**
     * Actually execute the remote command.
     */
    private void executeRemoteCommand(final ParameterMap params) throws CommandException {
        final ExecHttpCommand command = new ExecHttpCommand(params, detach, useSse(), closeSse, doUpload,
            outboundPayload, fileOutputDir, e -> fireEvent(e.getName(), e), this::downloadPayloadFromManaged);
        final ActionReport report = doHttpCommand(getCommandURI(), "POST", command);
        if (report == null) {
            // if closeSse was set by closeSse method, it is ok,
            // DetachListener set the job id and the report and we are done.
            if (!closeSse.get()) {
                this.output = null;
                throw new CommandException("Empty response from server.");
            }
        } else {
            setActionReport(report);
            if (report.getActionExitCode() == ExitCode.FAILURE) {
                throw new CommandException("Remote failure: " + this.output);
            }
        }
    }

    private void downloadPayloadFromManaged(String jobId) {
        if (jobId == null) {
            return;
        }
        try {
            RemoteRestAdminCommand command = new RemoteRestAdminCommand("_get-payload", this.host, this.port, this.secure, this.user,
                    this.password, this.logger, this.scope, this.authToken, this.prohibitDirectoryUploads, this.notify, false);
            ParameterMap params = new ParameterMap();
            params.add("DEFAULT", jobId);
            command.executeCommand(params);
        } catch (CommandException ex) {
            logger.log(WARNING, "Cannot retrieve payload.", ex);
        }
    }

    protected void setActionReport(ActionReport report) {
        logger.log(FINEST, "setActionReport(report={0})", report);
        this.actionReport = report;
        if (report == null) {
            this.output = null;
        } else {
            StringBuilder sb = new StringBuilder();
            if (report instanceof CliActionReport) {
                addCombinedMessages((CliActionReport) report, sb);
            } else if (report.getMessage() != null) {
                sb.append(report.getMessage());
            }
            addSubMessages("", report.getTopMessagePart(), sb);
            this.output = sb.toString();
            if (logger.isLoggable(FINER)) {
                logger.log(FINER, "------ ACTION REPORT ------");
                logger.log(FINER, String.valueOf(actionReport));
                logger.log(FINER, "---- END ACTION REPORT ----");
            }
        }
    }

    public ActionReport getActionReport() {
        return actionReport;
    }

    private static void addSubMessages(String indentPrefix, ActionReport.MessagePart mp, StringBuilder sb) {
        if (mp == null || sb == null) {
            return;
        }
        if (indentPrefix == null) {
            indentPrefix = "";
        }
        List<ActionReport.MessagePart> children = mp.getChildren();
        if (children != null) {
            for (ActionReport.MessagePart subPart : children) {
                if (sb.length() > 0) {
                    sb.append(EOL);
                }
                if (ok(subPart.getMessage())) {
                    sb.append(subPart.getMessage());
                }
                addSubMessages(indentPrefix + "    ", subPart, sb);
            }
        }
    }

    private static void addCombinedMessages(CliActionReport aReport, StringBuilder sb) {
        if (aReport == null || sb == null) {
            return;
        }
        String mainMsg = ""; //this is the message related to the topMessage
        String failMsg; //this is the message related to failure cause
        // Other code in the server may write something like report.setMessage(exception.getMessage())
        // and also set report.setFailureCause(exception). We need to avoid the duplicate message.
        if (aReport.getMessage() != null && aReport.getMessage().length() != 0) {
            if (sb.length() > 0) {
                sb.append(EOL);
            }
            sb.append(aReport.getMessage());
        }
        if (aReport.getFailureCause() != null && aReport.getFailureCause().getMessage() != null
                && aReport.getFailureCause().getMessage().length() != 0) {
            failMsg = aReport.getFailureCause().getMessage();
            if (!failMsg.equals(mainMsg)) {
                if (sb.length() > 0) {
                    sb.append(EOL);
                }
            }
            sb.append(failMsg);
        }
        for (CliActionReport sub : aReport.getSubActionsReport()) {
            addCombinedMessages(sub, sb);
        }
    }

    private <T> T doHttpCommand(String uriString, String httpMethod, HttpCommand<T> cmd) throws CommandException {
        return doHttpCommand(uriString, httpMethod, cmd, false /* isForMetadata */);
    }

    /**
     * Set up an HTTP connection, call cmd.prepareConnection so the consumer of the connection can further configure it,
     * then open the connection (following redirects if needed), then call cmd.useConnection so the consumer of the
     * connection can use it.
     * <P>
     * This method will try to execute the command repeatedly, for example, retrying with updated credentials (typically
     * from the interactive user), etc., until the command succeeds or there are no more ways to retry that might succeed.
     *
     * @param uriString the URI to connect to
     * @param httpMethod the HTTP method to use for the connection
     * @param cmd the HttpCommand object
     * @throws CommandException if anything goes wrong
     */
    private <T> T doHttpCommand(String uriString, String httpMethod, HttpCommand<T> cmd, boolean isForMetadata) throws CommandException {
        /*
         * There are various reasons we might retry the command - an authentication
         * challenges from the DAS, shifting from an insecure connection to
         * a secure one, etc.  So just keep trying as long as it makes sense.
         *
         * Any exception handling code inside the loop that changes something
         * about the connection or the request and wants to retry must set
         * shoudTryCommandAgain to true.
         */
        boolean shouldTryCommandAgain;

        /*
         * If the DAS challenges us for credentials and we've already sent
         * the caller-provided ones, we might ask the user for a new set
         * and use them.  But we want to ask only once.
         */
        boolean askedUserForCredentials = false;

        /*
         * On a subsequent retry we might need to use secure, even if the
         * caller did not request it.
         */
        boolean shouldUseSecure = secure;

        /*
         * Note: HttpConnectorAddress will set up SSL/TLS client cert
         * handling if the current configuration calls for it.
         */
        HttpConnectorAddress url = getHttpConnectorAddress(host, port, shouldUseSecure);
        url.setInteractive(interactive);
        T result = null;
        do {
            /*
             * Any code that wants to trigger a retry will say so explicitly.
             */
            shouldTryCommandAgain = false;
            try {
                final AuthenticationInfo authInfo = authenticationInfo();
                if (logger.isLoggable(FINER)) {
                    logger.log(FINER, "URI: {0}", uriString);
                    logger.log(FINER, "URL: {0}", url.toURL(uriString).toString());
                    logger.log(FINER, "Method: {0}", httpMethod);
                    logger.log(FINER, "Password options: {0}", passwordOptions);
                    logger.log(FINER, "Using auth info: {0}", authInfo);
                }
                if (authInfo != null) {
                    url.setAuthenticationInfo(authInfo);
                }
                final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(uriString);
                try {
                    urlConnection.setRequestProperty("User-Agent", responseFormatType);
                    urlConnection.addRequestProperty("Cache-Control", "no-cache");
                    urlConnection.addRequestProperty("Pragma", "no-cache");
                    if (passwordOptions != null) {
                        urlConnection.setRequestProperty("X-passwords", passwordOptions.toString());
                    }
                    if (authToken != null) {
                        /*
                         * If this request is for metadata then we expect to reuse
                         * the auth token.
                         */
                        urlConnection.setRequestProperty(SecureAdmin.ADMIN_ONE_TIME_AUTH_TOKEN_HEADER_NAME,
                                (isForMetadata ? AuthTokenManager.markTokenForReuse(authToken) : authToken));
                    }
                    if (commandModel != null && isCommandModelFromCache() && commandModel instanceof CachedCommandModel) {
                        urlConnection.setRequestProperty(COMMAND_MODEL_MATCH_HEADER, ((CachedCommandModel) commandModel).getETag());
                        if (logger.isLoggable(FINER)) {
                            logger.log(FINER, "CommandModel ETag: {0}", ((CachedCommandModel) commandModel).getETag());
                        }
                    }
                    urlConnection.setRequestMethod(httpMethod);
                    urlConnection.setReadTimeout(readTimeout);
                    if (connectTimeout >= 0) {
                        urlConnection.setConnectTimeout(connectTimeout);
                    }
                    addAdditionalHeaders(urlConnection);
                    urlConnection.addRequestProperty("X-Requested-By", "cli");
                    cmd.prepareConnection(urlConnection);
                    urlConnection.connect();
                    /*
                     * We must handle redirection from http to https explicitly
                     * because, even if the HttpURLConnection's followRedirect is
                     * set to true, the Java SE implementation does not do so if the
                     * procotols are different.
                     */
                    String redirection = checkConnect(urlConnection);
                    if (redirection != null) {
                        // Log at FINER; at FINE it would appear routinely when used from asadmin.
                        logger.log(FINER, () -> "Following redirection to " + redirection);
                        url = followRedirection(url, redirection);
                        shouldTryCommandAgain = true;
                        // During the retry of this request, we should use https.
                        shouldUseSecure = url.isSecure();
                        // If this is a metadata request, the real request should use https.
                        secure = true;
                        continue;
                    }

                    /*
                     * No redirection, so we have established the connection.
                     * Now delegate again to the command processing to use the
                     * now-created connection.
                     */
                    result = cmd.useConnection(urlConnection);
                    processHeaders(urlConnection);
                    logger.finer("doHttpCommand succeeds");
                } finally {
                    urlConnection.disconnect();
                }
            } catch (AuthenticationException authEx) {

                logger.log(FINER, "DAS has challenged for credentials");

                /*
                 * Try to update the credentials if we haven't already done so.
                 */
                if (askedUserForCredentials) {
                    /*
                     * We already updated the credentials once, and the updated
                     * ones did not work.  No recourse.
                     */
                    logger.log(FINER, "Already tried with updated credentials; cannot authenticate");
                    throw authEx;
                }

                /*
                 * Try to update the creds.
                 */
                logger.log(FINER, "Try to update credentials");
                if (!updateAuthentication()) {
                    /*
                     * No updated credentials are avaiable, so we
                     * have no more options.
                     */
                    logger.log(FINER, "Could not update credentials; cannot authenticate");
                    throw authEx;
                }
                /*
                 * We have another set of credentials we can try.
                 */
                logger.log(FINER, "Was able to update the credentials so will retry with the updated ones");
                askedUserForCredentials = true;
                shouldTryCommandAgain = true;
                continue;

            } catch (ConnectException ce) {
                logger.log(FINER, "doHttpCommand: connect exception", ce);
                // this really means nobody was listening on the remote server
                // note: ConnectException extends IOException and tells us more!
                String msg = strings.get("ConnectException", host, port + "");
                throw new CommandException(msg, ce);
            } catch (UnknownHostException he) {
                logger.log(FINER, "doHttpCommand: host exception", he);
                // bad host name
                String msg = strings.get("UnknownHostException", host);
                throw new CommandException(msg, he);
            } catch (SocketException se) {
                logger.log(FINER, "doHttpCommand: socket exception", se);
                throw new CommandException(se);
            } catch (SSLException se) {
                logger.log(FINER, "doHttpCommand: SSL exception", se);
                if (secure) {
                    logger.log(SEVERE, AdminLoggerInfo.mServerIsNotSecure, new Object[] { host, port });
                }
                throw new CommandException(se);
            } catch (SocketTimeoutException e) {
                logger.log(FINER, "doHttpCommand: read timeout", e);
                throw new CommandException(strings.get("ReadTimeout", (float) readTimeout / 1000), e);
            } catch (IOException e) {
                logger.log(FINER, "doHttpCommand: IO exception", e);
                throw new CommandException(strings.get("IOError", e.getMessage()), e);
            } catch (CommandException e) {
                throw e;
            } catch (Exception e) {
                logger.log(FINER, "Something went wrong: " + e.getMessage(), e);
                throw new CommandException(e);
            }
        } while (shouldTryCommandAgain);
        outboundPayload = null; // no longer needed
        return result;
    }

    /**
     * Creates a new HttpConnectorAddress corresponding to the location to which an earlier request was redirected.
     * <p>
     * If the new protocol is https then the HttpConnectorAddress secure setting is turned on.
     *
     * @param originalAddr the address which has been redirected elsewhere
     * @param redirection the location to which the attempted connection was redirected
     * @return connector address for the new location
     * @throws MalformedURLException
     */
    private HttpConnectorAddress followRedirection(final HttpConnectorAddress originalAddr, final String redirection)
            throws MalformedURLException {
        final URL url = new URL(redirection);
        final boolean useSecure = (url.getProtocol().equalsIgnoreCase("https"));
        HttpConnectorAddress hca = new HttpConnectorAddress(url.getHost(), url.getPort(), useSecure, originalAddr.getPath(),
                originalAddr.getSSLSocketFactory());
        hca.setInteractive(interactive);
        return hca;
    }

    /**
     * Provides an HttpConnectorAddress for use in connecting to the desired admin listener.
     * <p>
     * This implementation works for true admin clients and will not work correctly for commands submitted to instances from
     * inside the DAS. (That is done from the implementation in ServerRemoteAdminCommand which extends this class.)
     * <p>
     * This code constructs the HttpConnectorAddress in a way that uses either no SSLSocketFactory (if security is off) or
     * uses an SSLSocketFactory linked to the asadmin truststore.
     *
     * @param host the host name to which the connection should be made
     * @param port the admin port on that host
     * @param shouldUseSecure whether SSL should be used to connect or not
     * @return
     */
    protected HttpConnectorAddress getHttpConnectorAddress(final String host, final int port, final boolean shouldUseSecure) {
        HttpConnectorAddress hca = new HttpConnectorAddress(host, port, shouldUseSecure);
        hca.setInteractive(interactive);
        return hca;
    }

    /**
     * Adds any headers needed for the current environment to the admin request.
     *
     * @param urlConnection
     */
    protected void addAdditionalHeaders(final URLConnection urlConnection) {
        /*
         * No additional headers are needed for connections originating from
         * true admin clients.
         */
    }

    /**
     * Process any headers needed from the reply to the admin request. Subclasses can override this method to handle
     * processing headers in the command's reply.
     *
     * @param urlConnection
     */
    protected void processHeaders(final URLConnection urlConnection) {
        /*
         * No headers are processed by RemoteAdminCommand.
         */
    }

    /*
     * Returns the username/password authenticaiton information to use
     * in building the outbound HTTP connection.
     *
     * @return the username/password auth. information to send with the request
     */
    protected AuthenticationInfo authenticationInfo() {
        return ((user != null || password != null) ? new AuthenticationInfo(user, password) : null);
    }

    /**
     * Check that the connection was successful and handle any error responses, turning them into exceptions.
     */
    private String checkConnect(HttpURLConnection urlConnection) throws IOException, CommandException {
        int code = urlConnection.getResponseCode();
        if (logger.isLoggable(FINER)) {
            logger.log(FINER, "Response code: " + code);
        }
        if (code == -1) {
            URL url = urlConnection.getURL();
            throw new CommandException(strings.get("NotHttpResponse", url.getHost(), url.getPort()));
        }
        if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new AuthenticationException(reportAuthenticationException());
        }
        if (code == HttpURLConnection.HTTP_PRECON_FAILED) {
            throw new CommandValidationException("Code: " + HttpURLConnection.HTTP_PRECON_FAILED + ": Cached CommandModel is invalid.");
        }
        if (code == HttpURLConnection.HTTP_NOT_FOUND) {
            try (InputStream errorStream = urlConnection.getErrorStream()) {
                throw new InvalidCommandException(
                    ProprietaryReaderFactory.<String> getReader(String.class, urlConnection.getContentType())
                        .readFrom(errorStream, urlConnection.getContentType()));
            } catch (IOException ioex) {
                throw new InvalidCommandException(urlConnection.getResponseMessage());
            }
        }
        /*
         * The DAS might be redirecting to a secure port.  If so, follow
         * the redirection.
         */
        if (isStatusRedirection(code)) {
            return urlConnection.getHeaderField("Location");
        }
        if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_INTERNAL_ERROR) {
            throw new CommandException(strings.get("BadResponse", String.valueOf(code), urlConnection.getResponseMessage()));
        }
        /*
         * If the connection worked then return null, indicating no
         * redirection is needed.
         */
        return null;
    }

    private boolean isStatusRedirection(final int returnCode) {
        /*
         * Currently, Grizzly redirects using 302.  For admin requests the
         * other varieties of redirection do not apply.
         */
        return (returnCode == HttpURLConnection.HTTP_MOVED_TEMP);
    }

    /**
     * Get the usage text. If we got usage information from the server, use it.
     *
     * @return usage text
     */
    public String getUsage() {
        return commandModel instanceof CachedCommandModel ? ((CachedCommandModel) commandModel).getUsage() : null;
    }

    /**
     * Adds an option for a file argument, passing the name (for uploads) or the path (for no-upload) operations.
     *
     * @param params the URI string so far
     * @param optionName the option which takes a path or name
     * @param filename the name of the file
     * @return the URI string
     * @throws java.io.IOException
     */
    private void addFileOption(ParameterMap params, String optionName, String filename) throws IOException, CommandException {
        File f = SmartFile.sanitize(new File(filename));
        logger.finer("FILE PARAM: " + optionName + " = " + f);
        final boolean uploadThisFile = doUpload && !f.isDirectory();
        // attach the file to the payload - include the option name in the
        // relative URI to avoid possible conflicts with same-named files
        // in different directories
        if (uploadThisFile) {
            logger.finer("Uploading file");
            try {
                outboundPayload.attachFile(FILE_PAYLOAD_MIME_TYPE,
                        URI.create(optionName + "/" + f.getName() + (f.isDirectory() ? "/" : "")), optionName, null, f,
                        true /* isRecursive - in case it's a directory */);
            } catch (FileNotFoundException fnfe) {
                /*
                 * Probably due to an attempt to upload a non-existent file.
                 * Convert this to a CommandException so it's better handled
                 * by the rest of the command running infrastructure.
                 */
                throw new CommandException(strings.get("UploadedFileNotFound", f.getAbsolutePath()));
            }
        }
        if (f != null) {
            // if we are about to upload it -- give just the name
            // o/w give the full path
            String pathToPass = (uploadThisFile ? f.getName() : f.getPath());
            params.add(optionName, pathToPass);
        }
    }

    /**
     * Fetch the command metadata from the remote server.
     */
    public final void fetchCommandModel() throws CommandException {
        // For sure not be used during request header construction
        commandModel = null;
        CommandModelHttpCommand httpCommand = new CommandModelHttpCommand(name, commandCacheKey, detach, notify);
        commandModelFromCache = false;
        commandModel = doHttpCommand(getCommandURI(), "GET", httpCommand);
    }

    public String getManPage() throws CommandException {
        if (manpage == null) {
            manpage = doHttpCommand(getCommandURI() + "/manpage", "GET", new ManPageHttpCommand());
        }
        return manpage;
    }

    private String getCanonicalHost() {
        if (canonicalHostCache == null) {
            try {
                InetAddress address = InetAddress.getByName(host);
                canonicalHostCache = address.getCanonicalHostName();
            } catch (UnknownHostException ex) {
                canonicalHostCache = host;
                if (canonicalHostCache != null) {
                    canonicalHostCache = canonicalHostCache.trim().toLowerCase(Locale.ENGLISH);
                }
            }
        }
        return canonicalHostCache;
    }

    /**
     * Search all the parameters that were actually specified to see if any of them are FILE type parameters. If so, check
     * for the "--upload" option.
     */
    private void initializeDoUpload() throws CommandException {
        boolean sawFile = false;
        boolean sawDirectory = false;
        /*
         * We don't upload directories, even when asked to upload.
         */
        boolean sawUploadableFile = false;

        for (Map.Entry<String, List<String>> param : options.entrySet()) {
            String paramName = param.getKey();
            if (paramName.equals("DEFAULT")) { // operands handled below
                continue;
            }
            ParamModel opt = commandModel.getModelFor(paramName);
            if (opt != null && (opt.getType() == File.class || opt.getType() == File[].class)) {
                sawFile = true;
                for (String fname : options.get(opt.getName())) {
                    final File optionFile = new File(fname);
                    sawDirectory |= optionFile.isDirectory();
                    sawUploadableFile |= optionFile.isFile();
                }
            }
        }

        // now check the operands for files
        ParamModel operandParam = getOperandModel();
        if (operandParam != null && (operandParam.getType() == File.class || operandParam.getType() == File[].class)) {
            sawFile |= !operands.isEmpty();
            for (String operandValue : operands) {
                final File operandFile = new File(operandValue);
                sawDirectory |= operandFile.isDirectory();
                sawUploadableFile |= operandFile.isFile();
            }
        }

        if (sawFile) {
            logger.finer("Saw a file parameter");
            // found a FILE param, is doUpload set?
            String upString = getOption("upload");
            if (ok(upString)) {
                doUpload = Boolean.parseBoolean(upString);
            } else {
                doUpload = !isLocal(host) && sawUploadableFile;
            }
            if (prohibitDirectoryUploads && sawDirectory && doUpload) {
                // oops, can't upload directories
                logger.finer("--upload=" + upString + ", doUpload=" + doUpload);
                throw new CommandException(strings.get("CantUploadDirectory"));
            }
        }

        if (commandModel instanceof CachedCommandModel && ((CachedCommandModel) commandModel).isAddedUploadOption()) {
            logger.finer("removing --upload option");
            //options.remove("upload");    // remove it
            // XXX - no remove method, have to copy it
            ParameterMap noptions = new ParameterMap();
            for (Map.Entry<String, List<String>> e : options.entrySet()) {
                if (!e.getKey().equals("upload")) {
                    noptions.set(e.getKey(), e.getValue());
                }
            }
            options = noptions;
        }

        logger.finer("doUpload set to " + doUpload);
    }

    /**
     * Does the given hostname represent the local host?
     */
    private static boolean isLocal(String hostname) {
        if (hostname.equalsIgnoreCase("localhost")) { // the common case
            return true;
        }
        try {
            // let NetUtils do the hard work
            InetAddress ia = InetAddress.getByName(hostname);
            return NetUtils.isLocal(ia.getHostAddress());
        } catch (UnknownHostException ex) {
            /*
             * Sometimes people misconfigure their name service and they
             * can't even look up the name of their own machine.
             * Too bad.  We just give up and say it's not local.
             */
            return false;
        }
    }

    /**
     * Get the ParamModel that corresponds to the operand (primary parameter). Return null if none.
     */
    private ParamModel getOperandModel() {
        for (ParamModel pm : commandModel.getParameters()) {
            if (pm.getParam().primary()) {
                return pm;
            }
        }
        return null;
    }

    /**
     * Get an option value, that might come from the command line or from the environment. Return the default value for the
     * option if not otherwise specified.
     */
    private String getOption(String name) {
        String val = options.getOne(name);
        if (val == null) {
            val = getFromEnvironment(name);
        }
        if (val == null) {
            // no value, find the default
            ParamModel opt = commandModel.getModelFor(name);
            // if no value was specified and there's a default value, return it
            if (opt != null) {
                String def = opt.getParam().defaultValue();
                if (ok(def)) {
                    val = def;
                }
            }
        }
        return val;
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    /**
     * Can be called to start async preinitialisation. It can help a little bit in usage performance.
     */
    public static void preinit() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ProprietaryReaderFactory.getReader(Class.class, "not/defined");
                ProprietaryWriterFactory.getWriter(Class.class);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
