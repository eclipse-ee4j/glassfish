/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli.remote;

import com.sun.appserv.management.client.prefs.LoginInfo;
import com.sun.appserv.management.client.prefs.LoginInfoStore;
import com.sun.appserv.management.client.prefs.LoginInfoStoreFactory;
import com.sun.appserv.management.client.prefs.StoreException;
import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.admin.cli.DirectoryClassLoader;
import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.cli.ProgramOptions.PasswordLocation;
import com.sun.enterprise.admin.remote.RemoteAdminCommand;
import com.sun.enterprise.admin.util.CachedCommandModel;
import com.sun.enterprise.admin.util.CommandModelData;
import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.security.store.AsadminSecurityUtil;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.common.util.admin.ManPageFinder;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * A remote command handled by the asadmin CLI.
 */
public class RemoteCommand extends CLICommand {

    private static final LocalStringsImpl strings = new LocalStringsImpl(RemoteCommand.class);

    // return output string rather than printing it
    private boolean returnOutput;
    private String output;
    private boolean returnAttributes;
    private Map<String, String> attrs;
    private String usage;

    private String responseFormatType;
    private OutputStream userOut;
    private File outputDir;

    private CLIRemoteAdminCommand rac;

    /**
     * A special RemoteAdminCommand that overrides methods so that we can handle the interactive requirements of a CLI
     * command.
     */
    private class CLIRemoteAdminCommand extends RemoteAdminCommand {

        private static final String JSESSIONID = "JSESSIONID";
        private static final String COOKIE_HEADER = "Cookie";
        private CookieManager cookieManager;
        private final File sessionCache;

        /**
         * Construct a new remote command object. The command and arguments are supplied later using the execute method in the
         * superclass.
         */
        private CLIRemoteAdminCommand(String name, String host, int port, boolean secure, String user, char[] password,
            Logger logger, String authToken) throws CommandException {
            super(name, host, port, secure, user, password, logger, getCommandScope(), authToken, true /* prohibitDirectoryUploads */);
            sessionCache = AsadminSecurityUtil.getGfClientSessionFile(host, port);
        }

        /**
         * If we're interactive, prompt for a new username and password. Return true if we're successful in collecting new
         * information (and thus the caller should try the request again).
         */
        @Override
        protected boolean updateAuthentication() {
            Console cons;
            if (programOpts.isInteractive() && (cons = System.console()) != null) {
                // if appropriate, tell the user why authentication failed
                PasswordLocation pwloc = programOpts.getPasswordLocation();
                if (pwloc == PasswordLocation.PASSWORD_FILE) {
                    logger.fine(strings.get("BadPasswordFromFile", programOpts.getPasswordFile()));
                } else if (pwloc == PasswordLocation.LOGIN_FILE) {
                    try {
                        LoginInfoStore store = LoginInfoStoreFactory.getDefaultStore();
                        logger.fine(strings.get("BadPasswordFromLogin", store.getName()));
                    } catch (StoreException ex) {
                        // ignore it
                    }
                }

                String user = null;
                // only prompt for a user name if the user name is set to
                // the default.  otherwise, assume the user specified the
                // correct username to begin with and all we need is the
                // password.
                if (programOpts.getUser() == null) {
                    cons.printf("%s ", strings.get("AdminUserPrompt"));
                    user = cons.readLine();
                    if (user == null) {
                        return false;
                    }
                }
                char[] password;
                String puser = ok(user) ? user : programOpts.getUser();
                if (ok(puser)) {
                    password = readPassword(strings.get("AdminUserPasswordPrompt", puser));
                } else {
                    password = readPassword(strings.get("AdminPasswordPrompt"));
                }
                if (password == null) {
                    return false;
                }
                if (ok(user)) { // if none entered, don't change
                    programOpts.setUser(user);
                    this.user = user;
                }
                programOpts.setPassword(password, PasswordLocation.USER);
                this.password = password;
                return true;
            }
            return false;
        }

        /**
         * Get from environment.
         */
        @Override
        protected String getFromEnvironment(String name) {
            return env.getStringOption(name);
        }

        /**
         * Called when a non-secure connection attempt fails and it appears that the server requires a secure connection. Tell
         * the user that we're retrying.
         */
        @Override
        protected boolean retryUsingSecureConnection(String host, int port) {
            String msg = strings.get("ServerMaybeSecure", host, port + "");
            logger.info(msg);
            return true;
        }

        /**
         * Return the error message to be used in the AuthenticationException. Subclasses can override to provide a more
         * detailed message, for example, indicating the source of the password that failed.
         */
        @Override
        protected String reportAuthenticationException() {
            String msg = null;
            PasswordLocation pwloc = programOpts.getPasswordLocation();
            if (pwloc == PasswordLocation.PASSWORD_FILE) {
                msg = strings.get("InvalidCredentialsFromFile", programOpts.getUser(), programOpts.getPasswordFile());
            } else if (pwloc == PasswordLocation.LOGIN_FILE) {
                try {
                    LoginInfoStore store = LoginInfoStoreFactory.getDefaultStore();
                    msg = strings.get("InvalidCredentialsFromLogin", programOpts.getUser(), store.getName());
                } catch (StoreException ex) {
                    // ignore it
                }
            }

            if (msg == null) {
                msg = strings.get("InvalidCredentials", programOpts.getUser());
            }
            return msg;
        }

        /**
         * Adds cookies to the header to support session based client routing.
         *
         * @param urlConnection
         */
        @Override
        protected synchronized void addAdditionalHeaders(final URLConnection urlConnection) {
            addCookieHeaders(urlConnection);
        }

        /*
         * Adds any cookies maintained in the clients session cookie cache.
         */
        private void addCookieHeaders(final URLConnection urlConnection) {

            // Get the last modified time of the session cache file.
            long modifiedTime = sessionCache.lastModified();
            if (modifiedTime == 0) {
                // No session file so no cookies to add.
                return;
            }

            // Remote any Set-Cookie's in the system cookie manager otherwise
            // they appear as cookies in the outgoing request.
            ((CookieManager) CookieHandler.getDefault()).getCookieStore().removeAll();

            cookieManager = new CookieManager(new ClientCookieStore(new CookieManager().getCookieStore(), sessionCache),
                    CookiePolicy.ACCEPT_ALL);

            // XXX: If this is an interactive command we don't want to
            // keep reloading the cookie store.
            try {
                ((ClientCookieStore) cookieManager.getCookieStore()).load();
            } catch (IOException e) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Unable to load cookies: " + e);
                }
                return;
            }

            if (isSessionCookieExpired(cookieManager, modifiedTime)) {
                logger.finer("Cookie session file has expired.");
                if (!sessionCache.delete()) {
                    logger.finer("Unable to delete session file.");
                }
                return;
            }

            StringBuilder sb = new StringBuilder("$Version=1");
            boolean hasCookies = false;
            for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
                hasCookies = true;
                sb.append("; ").append(cookie.getName()).append("=").append(cookie.getValue());
            }
            if (hasCookies) {
                urlConnection.setRequestProperty(COOKIE_HEADER, sb.toString());
            }
        }

        /*
         * Looks for the SESSIONID cookie in the cookie store and
         * determines if the cookie has expired (based on the
         * Max-Age and the time in which the file was last modified.)
         * The assumption, based on how we write cookies, is that
         * the cookie session file will only be changed when the
         * JSESSIONID cookie changes.   Therefor the last mod time of
         * the file is a reasonable proxy for when the cookie was
         * "created".
         * If we can't find the JSESSIONID cookie then we return true.
         */
        private boolean isSessionCookieExpired(CookieManager manager, long creationTime) {
            for (URI uri : manager.getCookieStore().getURIs()) {
                for (HttpCookie cookie : manager.getCookieStore().get(uri)) {
                    if (cookie.getName().equals(JSESSIONID)) {
                        if ((creationTime / 1000 + cookie.getMaxAge()) < System.currentTimeMillis() / 1000) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        /**
         * Processes the headers to support session based client routing.
         *
         * @param urlConnection
         */
        @Override
        protected synchronized void processHeaders(final URLConnection urlConnection) {
            processCookieHeaders(urlConnection);
        }

        private void processCookieHeaders(final URLConnection urlConnection) {

            CookieManager systemCookieManager = (CookieManager) CookieHandler.getDefault();

            if (systemCookieManager == null) {
                logger.finer("Assertion failed: null system CookieManager");
                return;
            }

            // Using the system CookieHandler, retrieve any cookies.
            CookieStore systemCookieJar = systemCookieManager.getCookieStore();
            List<HttpCookie> newCookies = systemCookieJar.getCookies();

            if (newCookies.isEmpty()) {
                // If there are no cookies to set in the request we
                // have nothing to do.
                return;
            }

            /*
            Console console = System.console();
            for (HttpCookie cookie: newCookies) {
                console.printf("Cookie: %s%n", cookie.toString());
                console.printf("   MaxAge: %d%n", cookie.getMaxAge());
                console.printf("   Domain: %s%n", cookie.getDomain());
                console.printf("   Path: %s%n", cookie.getPath());
            }
             *
             */

            // Get the last modified time of the session cache file.
            if (sessionCache.lastModified() == 0) {
                // No file, if we have cookies we need to save them.
                if (cookieManager == null) {
                    cookieManager = new CookieManager(new ClientCookieStore(new CookieManager().getCookieStore(), sessionCache),
                            CookiePolicy.ACCEPT_ALL);
                }
                try {
                    cookieManager.put(((ClientCookieStore) cookieManager.getCookieStore()).getStaticURI(), urlConnection.getHeaderFields());
                } catch (IOException e) {
                    // Thrown by cookieManger.put()
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Unable to save cookies: " + e.toString());
                    }
                    return;
                }

                try {
                    ((ClientCookieStore) cookieManager.getCookieStore()).store();
                } catch (IOException e) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Unable to store cookies: " + e.toString());
                    }
                }
                return;
            }

            if (cookieManager == null) {
                cookieManager = new CookieManager(new ClientCookieStore(new CookieManager().getCookieStore(), sessionCache),
                        CookiePolicy.ACCEPT_ALL);
                try {
                    ((ClientCookieStore) cookieManager.getCookieStore()).load();
                } catch (IOException e) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Unable to load cookies: " + e.toString());
                    }
                    return;
                }
            }

            boolean newCookieFound = false;

            // Check to see if any of the set cookies in the reply are
            // different from what is already in the persistent store.
            for (HttpCookie cookie : systemCookieJar.getCookies()) {
                // Check to see if any of the set cookies in the reply are
                // different from what is already in the persistent store.
                int cookieIndex = cookieManager.getCookieStore().getCookies().indexOf(cookie);
                if (cookieIndex == -1) {
                    newCookieFound = true;
                    break;
                } else {
                    HttpCookie c1 = cookieManager.getCookieStore().getCookies().get(cookieIndex);

                    if (!c1.getValue().equals(cookie.getValue())) {
                        newCookieFound = true;
                        break;
                    }
                }
            }

            // Note: This has the potential to overwrite the existing file
            // which may contain changes that were introduced by another
            // command's execution.   Those changes will be lost.
            // Since the cookie store is only used for optimized session
            // routing we are only interested in preserving the last
            // set of session/routing cookies received from the server/LB
            // as we believe those to be the most current and accurate in
            // regards to future request routing.
            if (newCookieFound) {
                try {
                    try {
                        cookieManager.put(((ClientCookieStore) cookieManager.getCookieStore()).getStaticURI(),
                                urlConnection.getHeaderFields());
                    } catch (IOException e) {
                        // Thrown by cookieManger.put()
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("Unable to save cookies: " + e.toString());
                        }
                        return;
                    }
                    ((ClientCookieStore) cookieManager.getCookieStore()).store();
                } catch (IOException e) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Unable to store cookies: " + e.toString());
                    }
                }
            } else {
                // No cookies changed.  Update the modification time on the store.
                ((ClientCookieStore) cookieManager.getCookieStore()).touchStore();
            }
        }
    }

    /**
     * A class loader for the "modules" directory.
     */
    private static ClassLoader moduleClassLoader;

    /**
     * A habitat just for finding man pages.
     */
    private static ServiceLocator manServiceLocator;

    /**
     * Construct a new remote command object. The command and arguments are supplied later using the execute method in the
     * superclass.
     */
    public RemoteCommand() throws CommandException {
        super();
    }

    /**
     * Construct a new remote command object. The command and arguments are supplied later using the execute method in the
     * superclass.
     */
    public RemoteCommand(String name, ProgramOptions po, Environment env) throws CommandException {
        super(name, po, env);
    }

    /**
     * Construct a new remote command object. The command and arguments are supplied later using the execute method in the
     * superclass. This variant is used by the RemoteDeploymentFacility class to control and capture the output.
     */
    public RemoteCommand(String name, ProgramOptions po, Environment env, String responseFormatType, OutputStream userOut)
            throws CommandException {
        this(name, po, env);
        this.responseFormatType = responseFormatType;
        this.userOut = userOut;
    }

    /**
     * Helper for situation, where {@code CommandModel} is from cache and something shows, that server side signature of
     * command was changed
     */
    private void reExecuteAfterMetadataUpdate() throws ReExecuted, CommandException {
        //Check CommandModel
        if (rac == null) {
            return;
        }
        if (rac.getCommandModel() == null) {
            return;
        }
        if (!rac.isCommandModelFromCache()) {
            return;
        }
        //Refetch command model
        String eTag = CachedCommandModel.computeETag(rac.getCommandModel());
        rac = null;
        initializeRemoteAdminCommand();
        rac.fetchCommandModel();
        String newETag = CachedCommandModel.computeETag(rac.getCommandModel());
        if (eTag != null && eTag.equals(newETag)) {
            return; //Nothing change in command model
        }
        logger.log(Level.WARNING, "Command signature of {0} command was changed. Reexecuting with new metadata.", name);
        //clean state of this instance
        this.options = null;
        this.operands = null;
        //Reexecute it
        int result = execute(argv);
        throw new ReExecuted(result);
    }

    @Override
    public int execute(String... argv) throws CommandException {
        try {
            return super.execute(argv);
        } catch (ReExecuted reex) {
            return reex.getExecutionResult();
        }
    }

    /**
     * Set the directory in which any returned files will be stored. The default is the user's home directory.
     */
    public void setFileOutputDirectory(File dir) {
        outputDir = dir;
    }

    @Override
    protected void prepare() throws CommandException, CommandValidationException {
        try {
            processProgramOptions();

            initializeAuth();

            /*
             * Now we have all the information we need to create
             * the remote admin command object.
             */
            initializeRemoteAdminCommand();

            if (responseFormatType != null) {
                rac.setResponseFormatType(responseFormatType);
            }
            if (userOut != null) {
                rac.setUserOut(userOut);
            }

            /*
             * Initialize a CookieManager so that we can retreive
             * any cookies included in the reply.   These cookies
             * (e.g. JSESSIONID, JROUTE) are used for CLI session
             * based routing.
             */
            initializeCookieManager();

            /*
             * If this is a help request, we don't need the command
             * metadata and we throw away all the other options and
             * fake everything else.
             */
            if (programOpts.isHelp()) {
                commandModel = helpModel();
                rac.setCommandModel(commandModel);
                return;
            }

            /*
             * Find the metadata for the command.
             */
            commandModel = rac.getCommandModel();

            if (programOpts.isNotifyCommand()) {
                commandModel.add(new ParamModelData("notify", boolean.class, true, "false"));
            }
            if (programOpts.isDetachedCommand()) {
                commandModel.add(new ParamModelData("detach", boolean.class, true, "false"));
            }
        } catch (CommandException e) {
            logger.log(Level.SEVERE, "RemoteCommand.prepare throws exception.", e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "RemoteCommand.prepare throws exception.", e);
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    protected void prevalidate() throws CommandException {
        try {
            super.prevalidate();
        } catch (CommandException ex) {
            reExecuteAfterMetadataUpdate();
            throw ex;
        }
    }

    /**
     * If it's a help request, don't prompt for any missing options.
     */
    @Override
    protected void validate() throws CommandException, CommandValidationException {
        if (programOpts.isHelp()) {
            return;
        }
        try {
            super.validate();
        } catch (CommandValidationException ex) {
            reExecuteAfterMetadataUpdate();
            throw ex;
        }
    }

    @Override
    protected void inject() throws CommandException {
        try {
            super.prevalidate();
        } catch (CommandValidationException ex) {
            reExecuteAfterMetadataUpdate();
            throw ex;
        }
    }

    /**
     * We do all our help processing in executeCommand.
     */
    @Override
    protected boolean checkHelp() throws CommandException, CommandValidationException {
        return false;
    }

    /**
     * Runs the command using the specified arguments.
     */
    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        try {
            options.set("DEFAULT", operands);
            output = rac.executeCommand(options);
            if (returnAttributes) {
                attrs = rac.getAttributes();
            } else if (!returnOutput) {
                if (output.length() > 0) {
                    logger.info(output);
                }
            }
        } catch (CommandValidationException cve) {
            reExecuteAfterMetadataUpdate();
            throw cve;
        } catch (CommandException ex) {
            // if a --help request failed, try to emulate it locally
            if (programOpts.isHelp()) {
                Reader r = getLocalManPage();
                BufferedReader br = null;
                try {
                    if (r != null) {
                        br = new BufferedReader(r);
                        PrintWriter pw = new PrintWriter(System.out, false, Charset.defaultCharset());
                        char[] buf = new char[8192];
                        int cnt;
                        while ((cnt = br.read(buf)) > 0) {
                            pw.write(buf, 0, cnt);
                        }
                        pw.flush();
                        return SUCCESS;
                    }
                } catch (IOException ioex2) {
                    // ignore it and throw original exception
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException ioex3) {
                        // ignore it
                    }
                }
            }
            throw ex;
        }
        final Map<String, String> racAttrs = rac.getAttributes();
        String returnVal = racAttrs != null ? racAttrs.get("exit-code") : null;
        if (returnVal != null && "WARNING".equals(returnVal)) {
            return WARNING;
        }
        return SUCCESS;
    }

    /**
     * Execute the command and return the output as a string instead of writing it out.
     */
    public String executeAndReturnOutput(String... args) throws CommandException, CommandValidationException {
        /*
         * Tell the low level output processing to just save the output
         * string instead of writing it out.  Yes, this is pretty gross.
         */
        returnOutput = true;
        execute(args);
        returnOutput = false;
        return output;
    }

    /**
     * Execute the command and return the main attributes from the manifest instead of writing out the output.
     */
    public Map<String, String> executeAndReturnAttributes(String... args) throws CommandException, CommandValidationException {
        /*
         * Tell the low level output processing to just save the attributes
         * instead of writing out the output.  Yes, this is pretty gross.
         */
        returnAttributes = true;
        execute(args);
        returnAttributes = false;
        return attrs;
    }

    /**
     * Get the usage text. If we got usage information from the server, use it.
     *
     * @return usage text
     */
    @Override
    public String getUsage() {
        if (usage == null) {
            if (rac == null) {
                /*
                 * We weren't able to initialize the RemoteAdminCommand
                 * object, probably because we failed to parse the program
                 * options.  With no ability to contact the remote server,
                 * we can't provide any command-specific usage information.
                 * Sigh.
                 */
                return getCommandUsage();
            }
            usage = rac.getUsage();
        }
        if (usage == null) {
            return super.getUsage();
        }

        StringBuilder usageText = new StringBuilder();
        usageText.append(strings.get("Usage", getBriefCommandUsage()));
        usageText.append(" ");
        usageText.append(usage);
        return usageText.toString();
    }

    /**
     * Get the man page from the server. If the man page isn't available, e.g., because the server is down, try to find it
     * locally by looking in the modules directory.
     */
    @Override
    public BufferedReader getManPage() {
        try {
            initializeRemoteAdminCommand();
            rac.setCommandModel(helpModel());
            ParameterMap params = new ParameterMap();
            params.set("help", "true");
            String manpage = rac.executeCommand(params);
            return new BufferedReader(new StringReader(manpage));
        } catch (CommandException cex) {
            // ignore
        }

        /*
         * Can't find the man page remotely, try to find it locally.
         * XXX - maybe should only do this on connection failure
         */
        BufferedReader r = getLocalManPage();
        return r != null ? r : super.getManPage();
    }

    /**
     * Return a CommandModel that only includes the --help option.
     */
    private CommandModel helpModel() {
        CommandModelData cm = new CommandModelData(name);
        cm.add(new ParamModelData("help", boolean.class, true, "false", "?"));
        return cm;
    }

    /**
     * Try to find a local version of the man page for this command.
     */
    private BufferedReader getLocalManPage() {
        logger.fine(strings.get("NoRemoteManPage"));
        String cmdClass = getCommandClass(getName());
        ClassLoader mcl = getModuleClassLoader();
        if (cmdClass != null && mcl != null) {
            return ManPageFinder.getCommandManPage(getName(), cmdClass, Locale.getDefault(), mcl, logger);
        }
        return null;
    }

    private void initializeRemoteAdminCommand() throws CommandException {
        if (rac == null) {
            rac = new CLIRemoteAdminCommand(name, programOpts.getHost(), programOpts.getPort(), programOpts.isSecure(),
                    programOpts.getUser(), programOpts.getPassword(), logger, programOpts.getAuthToken());
            rac.setFileOutputDirectory(outputDir);
            rac.setInteractive(programOpts.isInteractive());
        }
    }

    private void initializeAuth() throws CommandException {
        LoginInfo li = null;

        try {
            LoginInfoStore store = LoginInfoStoreFactory.getDefaultStore();
            li = store.read(programOpts.getHost(), programOpts.getPort());
            if (li == null) {
                return;
            }
        } catch (StoreException se) {
            logger.finer("Login info could not be read from ~/.asadminpass file");
            return;
        }

        /*
         * If we don't have a user name, initialize it from .asadminpass.
         * In that case, also initialize the password unless it was
         * already specified (overriding what's in .asadminpass).
         *
         * If we already have a user name, and it's the same as what's
         * in .asadminpass, and we don't have a password, use the password
         * from .asadminpass.
         */
        if (programOpts.getUser() == null) {
            // not on command line and in .asadminpass
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Getting user name from ~/.asadminpass: " + li.getUser());
            }
            programOpts.setUser(li.getUser());
            if (programOpts.getPassword() == null) {
                // not in passwordfile and in .asadminpass
                logger.finer("Getting password from ~/.asadminpass");
                programOpts.setPassword(li.getPassword(), ProgramOptions.PasswordLocation.LOGIN_FILE);
            }
        } else if (programOpts.getUser().equals(li.getUser())) {
            if (programOpts.getPassword() == null) {
                // not in passwordfile and in .asadminpass
                logger.finer("Getting password from ~/.asadminpass");
                programOpts.setPassword(li.getPassword(), ProgramOptions.PasswordLocation.LOGIN_FILE);
            }
        }
    }

    /*
     * Initialize a CookieManager so that we can retreive
     * any cookies included in the reply.   These cookies
     * (e.g. JSESSIONID, JROUTE) are used for CLI session
     * based routing.
    */
    private void initializeCookieManager() {
        CookieStore defaultCookieStore = new CookieManager().getCookieStore();
        CookieManager manager = new CookieManager(defaultCookieStore, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
    }

    /**
     * Given a command name, return the name of the class that implements that command in the server.
     */
    private static String getCommandClass(String cmdName) {
        ServiceLocator h = getManHabitat();
        String cname = "org.glassfish.api.admin.AdminCommand";
        for (ActiveDescriptor<?> ad : h.getDescriptors(BuilderHelper.createContractFilter(cname))) {
            if (ad.getName() != null && ad.getName().equals(cmdName)) {
                return ad.getImplementation();
            }
        }

        return null;
    }

    /**
     * Return a ServiceLocator used just for reading man pages from the modules in the modules directory.
     */
    private static synchronized ServiceLocator getManHabitat() {
        if (manServiceLocator != null) {
            return manServiceLocator;
        }

        ModulesRegistry registry = new StaticModulesRegistry(getModuleClassLoader());
        ServiceLocator serviceLocator = registry.createServiceLocator("default");
        manServiceLocator = serviceLocator;
        return manServiceLocator;
    }

    /**
     * Return a ClassLoader that loads classes from all the modules (jar files) in the <INSTALL_ROOT>/modules directory.
     */
    private static synchronized ClassLoader getModuleClassLoader() {
        if (moduleClassLoader != null) {
            return moduleClassLoader;
        }
        File installDir = new File(System.getProperty(INSTALL_ROOT.getSystemPropertyName()));
        File modulesDir = new File(installDir, "modules");
        moduleClassLoader = new DirectoryClassLoader(modulesDir, CLICommand.class.getClassLoader());
        return moduleClassLoader;
    }
}
