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

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.util.CommandModelData.ParamModelData;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandModel.ParamModel;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.common.util.admin.AsadminInput;
import org.glassfish.common.util.admin.AuthTokenManager;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Representation of the options known to the asadmin program. These options control the overall behavior of asadmin,
 * e.g., the server to contact, and aren't specific to any of the commands supported by asadmin.
 * <p>
 * In GlassFish v3, asadmin program options are normally specified before the asadmin command name, with command options
 * after the command name (although intermixed program and command options are still supported for compatibility).
 */
public class ProgramOptions {

    public enum PasswordLocation {
        DEFAULT, USER, PASSWORD_FILE, LOGIN_FILE, LOCAL_PASSWORD
    }

    private static final Set<ParamModel> programOptions;
    private static final Set<ParamModel> helpOption; //--help can be after command name also if all others are before

    // the known program option names
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String USER = "user";
    public static final String PASSWORDFILE = "passwordfile";
    public static final String TERSE = "terse";
    public static final String ECHO = "echo";
    public static final String INTERACTIVE = "interactive";
    public static final String SECURE = "secure";
    public static final String HELP = "help";
    public static final String DETACH = "detach";
    public static final String NOTIFY = "notify";
    public static final String AUTHTOKEN = AuthTokenManager.AUTH_TOKEN_OPTION_NAME;
    public static final String AUXINPUT = AsadminInput.CLI_INPUT_OPTION_NAME;

    private static final Logger logger = Logger.getLogger(ProgramOptions.class.getPackage().getName());

    private static final LocalStringsImpl strings = new LocalStringsImpl(ProgramOptions.class);

    private ParameterMap options;
    private final Environment env;
    private boolean optionsSet;
    private char[] password;
    private PasswordLocation location;
    private String commandName;


    private String modulePath;
    /**
     * Information passed in from AsadminMain and used by start-domain.
     */
    private String classPath;
    private String className;

    /**
     * Define the meta-options known by the asadmin command.
     */
    static {
        Set<ParamModel> opts = new HashSet<>();
        Set<ParamModel> hopts = new HashSet<>();
        addMetaOption(opts, HOST, 'H', String.class, false, CLIConstants.DEFAULT_HOSTNAME);
        addMetaOption(opts, PORT, 'p', String.class, false, Integer.toString(CLIConstants.DEFAULT_ADMIN_PORT));
        addMetaOption(opts, USER, 'u', String.class, false, null);
        addMetaOption(opts, PASSWORDFILE, 'W', File.class, false, null);
        addMetaOption(opts, SECURE, 's', Boolean.class, false, "false");
        addMetaOption(opts, TERSE, 't', Boolean.class, false, "false");
        addMetaOption(opts, ECHO, 'e', Boolean.class, false, "false");
        addMetaOption(opts, INTERACTIVE, 'I', Boolean.class, false, "false");
        addMetaOption(opts, HELP, '?', Boolean.class, false, "false");
        addMetaOption(opts, AUXINPUT, '\0', String.class, false, null);
        addMetaOption(opts, AUTHTOKEN, '\0', String.class, false, null);
        addMetaOption(opts, DETACH, '\0', Boolean.class, false, "false");
        addMetaOption(opts, NOTIFY, '\0', Boolean.class, false, "false");
        programOptions = Collections.unmodifiableSet(opts);
        addMetaOption(hopts, HELP, '?', Boolean.class, false, "false");
        helpOption = Collections.unmodifiableSet(hopts);
    }

    /**
     * Helper method to define a meta-option.
     *
     * @param name long option name
     * @param sname short option name
     * @param type option type (String.class, Boolean.class, etc.)
     * @param req is option required?
     * @param def default value for option
     */
    private static void addMetaOption(Set<ParamModel> opts, String name, char sname, Class type, boolean req, String def) {
        ParamModel opt = new ParamModelData(name, type, !req, def, Character.toString(sname));
        opts.add(opt);
    }

    /**
     * Initialize program options based only on environment defaults, with no options from the command line.
     */
    public ProgramOptions(Environment env) throws CommandException {
        this(new ParameterMap(), env);
        optionsSet = false;
    }

    /**
     * Initialize the programoptions based on parameters parsed from the command line, with defaults supplied by the
     * environment.
     */
    public ProgramOptions(ParameterMap options, Environment env) throws CommandException {
        this.env = env;
        updateOptions(options);
    }

    /**
     * Copy constructor. Create a new ProgramOptions with the same options as the specified ProgramOptions.
     */
    public ProgramOptions(ProgramOptions other) {
        this.options = new ParameterMap(other.options);
        this.env = other.env;
        this.password = other.password;
        this.modulePath = other.modulePath;
        this.classPath = other.classPath;
        this.className = other.className;
    }

    /**
     * Update the program options based on the specified options from the command line.
     */
    public final void updateOptions(ParameterMap newOptions) throws CommandException {
        if (options == null) {
            options = newOptions;
        } else {
            // merge in the new options
            for (Map.Entry<String, List<String>> e : newOptions.entrySet()) {
                options.set(e.getKey(), e.getValue());
            }
        }
        optionsSet = true;

        // additional validations
        String host = options.getOne(HOST);
        if (ok(host)) {
            final AtomicBoolean resolvable = new AtomicBoolean();
            Runnable hostResolution = () -> {
                try {
                    InetAddress.getByName(host);
                    resolvable.set(true);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Host could not be resolved: " + host, e);
                }
            };
            Thread thread = new Thread(hostResolution, "host-resolution");
            thread.setDaemon(true);
            thread.start();
            long timeout = 5000L;
            try {
                thread.join(timeout);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Host " + host + " could not be resolved in " + timeout + " ms", e);
            }
            if (!resolvable.get()) {
                throw new CommandException("Provided hostname could not be resolved to an IP address: " + host);
            }
        }

        String sport = options.getOne(PORT);
        if (ok(sport)) {
            String badPortMsg = strings.get("InvalidPortNumber", sport);
            try {
                int port = Integer.parseInt(sport);
                if (port < 1 || port > 65535) {
                    throw new CommandException(badPortMsg);
                }
            } catch (NumberFormatException e) {
                throw new CommandException(badPortMsg);
            }
        }
    }

    private static boolean ok(String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * Return a set of all the valid program options.
     *
     * @return the valid program options
     */
    public static Collection<ParamModel> getValidOptions() {
        return programOptions;
    }

    /**
     * Return a set of all the valid program options.
     *
     * @return the valid program options
     */
    public static Collection<ParamModel> getHelpOption() {
        return helpOption;
    }

    /**
     * Copy the program options that were specified on the command line into the corresponding environment variables.
     */
    public void toEnvironment(Environment env) {
        // copy all the parameters into corresponding environment variables
        putEnv(env, ECHO);
        putEnv(env, TERSE);
        putEnv(env, INTERACTIVE);
        putEnv(env, HOST);
        putEnv(env, PORT);
        putEnv(env, SECURE);
        putEnv(env, USER);
        putEnv(env, PASSWORDFILE);
        putEnv(env, AUTHTOKEN);
        putEnv(env, AUXINPUT);
        putEnv(env, DETACH);
        // XXX - HELP?
    }

    private void putEnv(Environment env, String name) {
        String value = options.getOne(name);
        if (value != null) {
            env.putOption(name, value);
        }
    }

    /**
     * @return the host
     */
    public String getHost() {
        String host = options.getOne(HOST);
        if (!ok(host)) {
            host = env.getStringOption(HOST);
        }
        if (!ok(host)) {
            host = CLIConstants.DEFAULT_HOSTNAME;
        }
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        options.set(HOST, host);
    }

    /**
     * @return the port
     */
    public int getPort() {
        int port;
        String sport = options.getOne(PORT);
        if (!ok(sport)) {
            sport = env.getStringOption(PORT);
        }
        if (ok(sport)) {
            try {
                port = Integer.parseInt(sport);
                if (port < 1 || port > 65535) {
                    port = -1; // should've been verified in constructor
                }
            } catch (NumberFormatException e) {
                port = -1; // should've been verified in constructor
            }
        }
        else {
            port = CLIConstants.DEFAULT_ADMIN_PORT; // the default port
        }
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        options.set(PORT, Integer.toString(port));
    }

    /**
     * Convenience method to set the host and port (and secure) attributes from a HostAndPort object.
     *
     * @param address the HostAndPort object from which to set the attributes
     */
    public void setHostAndPort(HostAndPort address) {
        setHost(address.getHost());
        setPort(address.getPort());
        setSecure(address.isSecure());
    }

    /**
     * @return the user
     */
    public String getUser() {
        String user = options.getOne(USER);
        if (!ok(user)) {
            user = env.getStringOption(USER);
        }
        if (!ok(user))
         {
            user = null; // distinguish between specify the default explicitly
        }
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Setting user to: " + user);
        }
        options.set(USER, user);
    }

    /**
     * @return the password
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * @return the password location
     */
    public PasswordLocation getPasswordLocation() {
        return location;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(char[] password, PasswordLocation location) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Setting password to: " + ((password != null && password.length > 0) ? "<non-null>" : "<null>"));
        }
        this.password = password;
        this.location = location;
    }

    /**
     * @return the passwordFile
     */
    public String getPasswordFile() {
        String passwordFile = options.getOne(PASSWORDFILE);

        if (!ok(passwordFile)) {
            passwordFile = env.getStringOption(PASSWORDFILE);
        }

        if (!ok(passwordFile))
         {
            return null; // no default
        }

        // weird, huh?  This means use standard input
        if (!passwordFile.equals("-")) {
            passwordFile = SmartFile.sanitize(passwordFile);
        }

        return passwordFile;
    }

    /**
     * @param passwordFile the passwordFile to set
     */
    public void setPasswordFile(String passwordFile) {
        options.set(PASSWORDFILE, passwordFile);
    }

    /**
     * @return the secure
     */
    public boolean isSecure() {
        boolean secure;
        if (options.containsKey(SECURE)) {
            String value = options.getOne(SECURE);
            if (ok(value)) {
                secure = Boolean.parseBoolean(value);
            } else {
                secure = true;
            }
        } else {
            secure = env.getBooleanOption(SECURE);
        }
        return secure;
    }

    /**
     * @param secure the secure to set
     */
    public void setSecure(boolean secure) {
        options.set(SECURE, Boolean.toString(secure));
    }

    public void setAuthToken(final String token) {
        options.set(AUTHTOKEN, token);
    }

    public String getAuthToken() {
        return getString(AUTHTOKEN);
    }

    public void setAuxInput(final String authInput) {
        options.set(AUXINPUT, authInput);
    }

    public String getAuxInput() {
        return getString(AUXINPUT);
    }

    private String getString(final String optionName) {
        String result;
        result = options.getOne(optionName);
        if (!ok(result)) {
            result = env.getStringOption(optionName);
            if (!ok(result)) {
                result = null;
            }
        }
        return result;
    }

    /**
     * @return the terse
     */
    public boolean isTerse() {
        boolean terse;
        if (options.containsKey(TERSE)) {
            String value = options.getOne(TERSE);
            if (ok(value)) {
                terse = Boolean.parseBoolean(value);
            } else {
                terse = true;
            }
        } else {
            terse = env.getBooleanOption(TERSE);
        }
        return terse;
    }

    /**
     * @return detach option
     */
    public boolean isDetachedCommand() {
        if (options.containsKey(DETACH)) {
            String value = options.getOne(DETACH);
            return (ok(value)) ? Boolean.parseBoolean(value) : true;
        }
        return false;
    }

    /**
     * @return notify option
     */
    public boolean isNotifyCommand() {
        if (options.containsKey(NOTIFY)) {
            String value = options.getOne(NOTIFY);
            return (ok(value)) ? Boolean.parseBoolean(value) : true;
        }
        return false;
    }

    public void removeDetach() {
        if (options.containsKey(DETACH)) {
            options.remove(DETACH);
        }
    }

    /**
     * @param terse the terse to set
     */
    public void setTerse(boolean terse) {
        options.set(TERSE, Boolean.toString(terse));
    }

    /**
     * @return the echo
     */
    public boolean isEcho() {
        boolean echo;
        if (options.containsKey(ECHO)) {
            String value = options.getOne(ECHO);
            if (ok(value)) {
                echo = Boolean.parseBoolean(value);
            } else {
                echo = true;
            }
        } else {
            echo = env.getBooleanOption(ECHO);
        }
        return echo;
    }

    /**
     * @param echo the echo to set
     */
    public void setEcho(boolean echo) {
        options.set(ECHO, Boolean.toString(echo));
    }

    /**
     * @return the interactive
     */
    public boolean isInteractive() {
        boolean interactive;
        if (options.containsKey(INTERACTIVE)) {
            String value = options.getOne(INTERACTIVE);
            if (ok(value)) {
                interactive = Boolean.parseBoolean(value);
            } else {
                interactive = true;
            }
        } else if (env.hasOption(INTERACTIVE)) {
            interactive = env.getBooleanOption(INTERACTIVE);
        } else {
            interactive = System.console() != null;
        }
        return interactive;
    }

    /**
     * @param interactive the interactive to set
     */
    public void setInteractive(boolean interactive) {
        options.set(INTERACTIVE, Boolean.toString(interactive));
    }

    /**
     * @return the help
     */
    public boolean isHelp() {
        if (options.containsKey(HELP)) {
            String value = options.getOne(HELP);
            // --help=true or --help
            return ok(value) ? Boolean.parseBoolean(value) : true;
        }
        return env.getBooleanOption(HELP);
    }

    /**
     * @param help the help to set
     */
    public void setHelp(boolean help) {
        options.set(HELP, Boolean.toString(help));
    }

    /**
     * @return were options set on the command line?
     */
    public boolean isOptionsSet() {
        return optionsSet;
    }

    /**
     * Set whether the program options have already been set.
     */
    public void setOptionsSet(boolean optionsSet) {
        this.optionsSet = optionsSet;
    }

    /**
     * Return an array of asadmin command line options that specify all the options of this ProgramOptions instance.
     */
    public String[] getProgramArguments() {
        List<String> args = new ArrayList<>(15);
        if (ok(getHost())) {
            args.add("--host");
            args.add(getHost());
        }
        if (getPort() > 0) {
            args.add("--port");
            args.add(String.valueOf(getPort()));
        }
        if (ok(getUser())) {
            args.add("--user");
            args.add(getUser());
        }
        if (ok(getPasswordFile())) {
            args.add("--passwordfile");
            args.add(getPasswordFile());
        }
        if (ok(getAuxInput())) {
            args.add("--" + AUXINPUT);
            args.add(getAuxInput());
        }
        args.add("--secure=" + String.valueOf(isSecure()));
        args.add("--terse=" + String.valueOf(isTerse()));
        args.add("--echo=" + String.valueOf(isEcho()));
        args.add("--interactive=" + String.valueOf(isInteractive()));
        String[] a = new String[args.size()];
        args.toArray(a);
        return a;
    }

    /**
     * Option by name just as was parsed. No added value.
     */
    public String getPlainOption(String name) {
        return options.getOne(name);
    }

    /**
     * @return the modulePath
     */
    public String getModulePath() {
        return modulePath;
    }

    /**
     * @param modulePath the modulePath to set
     */
    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    /**
     * @return the classPath
     */
    public String getClassPath() {
        return classPath;
    }

    /**
     * @param classPath the classPath to set
     */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the name of the command (not the subcommand)
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * @param commandName the name of the command (not the subcommand)
     */
    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    /**
     * String representation of the asadmin program options. Included in the --echo output.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ok(getHost())) {
            sb.append("--host ").append(getHost()).append(' ');
        }
        if (getPort() > 0) {
            sb.append("--port ").append(getPort()).append(' ');
        }
        if (ok(getUser())) {
            sb.append("--user ").append(getUser()).append(' ');
        }
        if (ok(getPasswordFile())) {
            sb.append("--passwordfile ").append(getPasswordFile()).append(' ');
        }
        if (isSecure()) {
            sb.append("--secure ");
        }
        sb.append("--interactive=").append(Boolean.toString(isInteractive())).append(' ');
        sb.append("--echo=").append(Boolean.toString(isEcho())).append(' ');
        sb.append("--terse=").append(Boolean.toString(isTerse())).append(' ');
        sb.setLength(sb.length() - 1); // strip trailing space
        return sb.toString();
    }
}
