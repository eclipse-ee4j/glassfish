/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.admin.launcher.CommandLine.CommandFormat;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static com.sun.enterprise.admin.launcher.GFLauncher.LaunchType.fake;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.DEFAULT_LOGFILE;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.FLASHLIGHT_AGENT_NAME;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.LIBMON_NAME;
import static com.sun.enterprise.admin.launcher.GFLauncherLogger.COMMAND_LINE;
import static com.sun.enterprise.universal.collections.CollectionUtils.propertiesToStringMap;
import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.ok;
import static com.sun.enterprise.universal.process.ProcessStreamDrainer.dispose;
import static com.sun.enterprise.universal.process.ProcessStreamDrainer.redirect;
import static com.sun.enterprise.universal.process.ProcessStreamDrainer.save;
import static com.sun.enterprise.util.OS.isDarwin;
import static com.sun.enterprise.util.SystemPropertyConstants.DEBUG_MODE_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.DROP_INTERRUPTED_COMMANDS;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JAVA_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.PREFER_ENV_VARS_OVER_PROPERTIES;
import static java.lang.Boolean.TRUE;
import static java.lang.System.Logger.Level.INFO;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * This is the main Launcher class designed for external and internal usage.
 *
 * <p>
 * Each of the 3 kinds of server, domain, node-agent and instance, need to
 * subclass this class.
 *
 * @author bnevins
 */
public abstract class GFLauncher {

    private static final LocalStringsImpl I18N = new LocalStringsImpl(GFLauncher.class);
    private static final Logger LOG = System.getLogger(GFLauncher.class.getName(), I18N.getBundle());
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncher.class);

    /**
     * Parameters provided by the caller of a launcher, either programmatically
     * (for GF embedded) or as commandline parameters (GF DAS or Instance).
     *
     */
    private final GFLauncherInfo callerParameters;

    /**
     * Properties from asenv.conf, such as
     * <code>AS_DEF_DOMAINS_PATH="../domains"</code>
     */
    private Map<String, String> asenvProps;

    /**
     * The <code>java-config</code> attributes in domain.xml
     */
    private JavaConfig domainXMLjavaConfig;

    /**
     * the <code>debug-options</code> attribute from <code>java-config</code> in
     * domain.xml
     */
    private List<String> domainXMLjavaConfigDebugOptions;

    /**
     * The debug port (<code>address</code>) primarily extracted from
     * <code>domainXMLjavaConfigDebugOptions</code>
     */
    private int debugPort = -1;

    /**
     * The debug suspend (<code>suspend</code>) primarily extracted from
     * <code>domainXMLjavaConfigDebugOptions</code>
     */
    private boolean debugSuspend;

    /**
     * The combined <code>jvm-options</code> from <code>java-config</code>,
     * <code>profiler</code> in domain.xml and extra ones added by this launcher
     */
    private JvmOptions domainXMLjvmOptions;

    /**
     * Same data as domainXMLjvmOptions, but as list
     */
    private final List<String> domainXMLJvmOptionsAsList = new ArrayList<>();

    /**
     * The <code>profiler<code> from <code>java-config</code> in domain.xml
     */
    private Profiler domainXMLJavaConfigProfiler;

    /**
     * The (combined) <code>system-property</code> elements in domain.xml
     */
    private Map<String, String> domainXMLSystemProperty;

    private Path javaExe;
    private File[] classpath;
    private String adminFileRealmKeyFile;
    private boolean secureAdminEnabled;

    /**
     * The file name to log to using a
     * <code>java.util.logging.FileHandler.FileHandler</code>
     */
    private String logFilename; // defaults to "logs/server.log"

    /**
     * Tracks whether the fixLogFileName() method was called.
     */
    private boolean logFilenameWasFixed;

    /**
     * Tracks whether the setup() had been called and/or should be called again.
     */
    private boolean setupCalledByClients; // handle with care

    // Tracks upgrading from V2 to V3. Since we're at V6 atm of wring, is this really needed?
    private boolean needsAutoUpgrade;
    private boolean needsManualUpgrade;

    private LaunchType mode = LaunchType.normal;

    /**
     * The full commandline string used to start GlassFish in process
     * <code<>glassFishProcess</code>
     */
    private CommandLine commandLine;

    /**
     * Time when GlassFish was launched
     */
    private long startTime;

    /**
     * The process which is running GlassFish
     */
    private Process glassFishProcess;
    private ProcessStreamDrainer processStreamDrainer;

    /**
     * Exit value of the glassFishProcess, IFF we waited on the process
     */
    private int exitValue = -1;

    GFLauncher(GFLauncherInfo info) {
        this.callerParameters = info;
    }

    ///////////////////////////////////////////////////////////////////////////
    ////// PUBLIC api area starts here ////////////////////////
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Launches the server.
     *
     * @throws GFLauncherException if launch failed.
     */
    public final void launch() throws GFLauncherException {
        if (isDebugSuspend()) {
            LOG.log(INFO, "ServerStart.DebuggerSuspendedMessage", debugPort);
        } else if (debugPort >= 0) {
            LOG.log(INFO, "ServerStart.DebuggerMessage", debugPort);
        }

        try {
            startTime = System.currentTimeMillis();
            if (!setupCalledByClients) {
                setup();
            }
            internalLaunch();
        } catch (GFLauncherException gfe) {
            throw gfe;
        } catch (Exception t) {
            throw new GFLauncherException(strings.get("unknownError", t.getMessage()), t);
        } finally {
            GFLauncherLogger.removeLogFileHandler();
        }
    }

    /**
     * Launches the server - but forces the setup() to go through again.
     *
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException
     */
    public final void relaunch() throws GFLauncherException {
        setupCalledByClients = false;
        launch();
    }

    public void setup() throws GFLauncherException, MiniXmlParserException {
        asenvProps = getAsEnvConfReader().getProps();
        callerParameters.setup();
        setupLogLevels();

        MiniXmlParser domainXML = new MiniXmlParser(getInfo().getConfigFile(), getInfo().getInstanceName());

        String domainName = domainXML.getDomainName();
        if (ok(domainName)) {
            callerParameters.setDomainName(domainName);
        }

        callerParameters.setAdminAddresses(domainXML.getAdminAddresses());
        domainXMLjavaConfig = new JavaConfig(domainXML.getJavaConfig());
        setupProfilerAndJvmOptions(domainXML);
        setupUpgradeSecurity();

        Map<String, String> realmprops = domainXML.getAdminRealmProperties();
        if (realmprops != null) {
            String classname = realmprops.get("classname");
            String keyfile = realmprops.get("file");
            if ("com.sun.enterprise.security.auth.realm.file.FileRealm".equals(classname) && keyfile != null) {
                adminFileRealmKeyFile = keyfile;
            }
        }

        secureAdminEnabled = domainXML.getSecureAdminEnabled();

        renameOsgiCache();
        setupMonitoring(domainXML);
        domainXMLSystemProperty = domainXML.getSystemProperties();
        asenvProps.put(INSTANCE_ROOT_PROPERTY, getInfo().getInstanceRootDir().getPath());

        // Set the config java-home value as the Java home for the environment,
        // unless it is empty or it is already refering to a substitution of
        // the environment variable.
        String javaHome = domainXMLjavaConfig.getJavaHome();
        if (ok(javaHome) && !javaHome.trim().equals("${" + JAVA_ROOT_PROPERTY + "}")) {
            asenvProps.put(JAVA_ROOT_PROPERTY, javaHome);
        }

        domainXMLjavaConfigDebugOptions = getDebugOptionsFromDomainXMLJavaConfig();
        parseJavaConfigDebugOptions();
        domainXML.setupConfigDir(getInfo().getConfigDir(), getInfo().getInstallDir());

        setLogFilename(domainXML);
        resolveAllTokens();
        fixLogFilename();
        GFLauncherLogger.addLogFileHandler(logFilename);

        setJavaExecutable();
        setClasspath();
        initCommandLine();
        setJvmOptions();
        logCommandLine();

        // if no <network-config> element, we need to upgrade this domain
        needsAutoUpgrade = !domainXML.hasNetworkConfig();
        needsManualUpgrade = !domainXML.hasDefaultConfig();
        setupCalledByClients = true;
    }

    /**
     *
     * @return The callerParameters object that contains startup
     * callerParameters
     */
    public final GFLauncherInfo getInfo() {
        return callerParameters;
    }

    /**
     * Returns the admin realm key file for the server, if the admin realm is a
     * FileRealm. Otherwise return null. This value can be used to create a
     * FileRealm for the server.
     */
    public String getAdminRealmKeyFile() {
        return adminFileRealmKeyFile;
    }

    /**
     * Returns true if secure admin is enabled
     */
    public boolean isSecureAdminEnabled() {
        return secureAdminEnabled;
    }

    /**
     * Returns the exit value of the glassFishProcess. This only makes sense
     * when we ran in verbose mode and waited for the glassFishProcess to exit
     * in the wait() method. Caveat Emptor!
     *
     * @return the glassFishProcess' exit value if it completed and we waited.
     * Otherwise it returns -1
     */
    public final int getExitValue() {
        return exitValue;
    }

    /**
     * You don't want to call this before calling launch because it would not
     * make sense.
     *
     * @return The Process object of the launched Server glassFishProcess. you
     * will either get a valid Process object or an Exceptio will be thrown. You
     * are guaranteed not to get a null.
     * @throws GFLauncherException if the Process has not been created yet -
     * call launch() before calling this method.
     */
    public final Process getProcess() throws GFLauncherException {
        if (glassFishProcess == null) {
            throw new GFLauncherException("invalid_process");
        }

        return glassFishProcess;
    }

    /**
     * A ProcessStreamDrainer is always attached to every Process created here.
     * It is handy for getting the stdin and stdout as a nice String.
     *
     * @return A valid ProcessStreamDrainer. You are guaranteed to never get a
     * null.
     * @throws GFLauncherException if the glassFishProcess has not launched yet
     * @see com.sun.enterprise.universal.process.ProcessStreamDrainer
     */
    public final ProcessStreamDrainer getProcessStreamDrainer() throws GFLauncherException {
        if (processStreamDrainer == null) {
            throw new GFLauncherException("invalid_psd");
        }

        return processStreamDrainer;
    }

    /**
     * Get the location of the server logfile
     *
     * @return The full path of the logfile
     * @throws GFLauncherException if you call this method too early
     */
    public String getLogFilename() throws GFLauncherException {
        if (!logFilenameWasFixed) {
            throw new GFLauncherException(strings.get("internalError") + " call to getLogFilename() before it has been initialized");
        }

        return logFilename;
    }

    /**
     * Return the port number of the debug port, or -1 if debugging is not
     * enabled.
     *
     * @return the debug port, or -1 if not debugging
     */
    public final int getDebugPort() {
        return debugPort;
    }

    /**
     * Return true if suspend=y AND debugging is on. otherwise return false.
     *
     * @return true if suspending, or false if either not suspending or not
     * debugging
     */
    public final boolean isDebugSuspend() {
        return debugPort >= 0 && debugSuspend;
    }

    /**
     * Does this domain need to be automatically upgraded before it can be
     * started?
     *
     * @return true if the domain needs to be upgraded first
     */
    public final boolean needsAutoUpgrade() {
        return needsAutoUpgrade;
    }

    /**
     * Does this domain need to be manually upgraded before it can be started?
     *
     * @return true if the domain needs to be upgraded first
     */
    public final boolean needsManualUpgrade() {
        return needsManualUpgrade;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ////// ALL private and package-private below ////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    abstract void internalLaunch() throws GFLauncherException;

    void launchInstance() throws GFLauncherException {
        if (isFakeLaunch()) {
            return;
        }

        final boolean securityTokensAvailable = !callerParameters.securityTokens.isEmpty();
        final List<String> cmds;

        // Use launchctl bsexec on MacOS versions before 10.10
        // otherwise use regular startup.
        if (isDarwin() && useLaunchCtl(System.getProperty("os.version")) && !getInfo().isVerboseOrWatchdog()) {
            cmds = new ArrayList<>();
            cmds.add("launchctl");
            cmds.add("bsexec");
            cmds.add("/");
            cmds.addAll(commandLine.toList());
        } else if (commandLine.getFormat() == CommandFormat.Script) {
            cmds = prepareWindowsEnvironment(commandLine, getInfo().getConfigDir().toPath(), securityTokensAvailable);
        } else if (getInfo().isVerboseOrWatchdog()) {
            cmds = new ArrayList<>();
            cmds.addAll(commandLine.toList());
        } else {
            cmds = new ArrayList<>();
            if (!isWindows()) {
                cmds.add("nohup");
            }
            cmds.addAll(commandLine.toList());
        }

        // When calling cluster nodes, this will be visible in the server.log too.
        System.err.println("Executing: " + cmds.stream().collect(Collectors.joining(" ")));
        System.err.println("Please look at the server log for more details...");
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        if (getInfo().isVerboseOrWatchdog()) {
            processBuilder.redirectOutput(Redirect.INHERIT);
            processBuilder.redirectError(Redirect.INHERIT);
        }

        // Change the directory if there is one specified, o/w stick with the default.
        try {
            processBuilder.directory(getInfo().getConfigDir());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Run the glassFishProcess and attach Stream Drainers
        try {
            // We have to abandon server.log file to avoid file locking issues on Windows.
            // From now on the server.log file is owned by the server, not by launcher.
            GFLauncherLogger.removeLogFileHandler();

            // Startup GlassFish
            glassFishProcess = processBuilder.start();

            String name = getInfo().getDomainName();

            // verbose trumps watchdog.
            if (getInfo().isVerbose()) {
                processStreamDrainer = redirect(name, glassFishProcess);
            } else if (getInfo().isWatchdog()) {
                processStreamDrainer = dispose(name, glassFishProcess);
            } else {
                processStreamDrainer = save(name, glassFishProcess);
            }
            handleDeadProcess(glassFishProcess, processStreamDrainer);
            if (securityTokensAvailable) {
                writeSecurityTokens(glassFishProcess, processStreamDrainer, callerParameters.securityTokens);
            }
        } catch (Exception e) {
            throw new GFLauncherException("jvmfailure", e, e);
        }

        // If verbose, hang around until the domain stops
        if (getInfo().isVerboseOrWatchdog()) {
            wait(glassFishProcess);
        }
    }

    boolean isFakeLaunch() {
        return mode == fake;
    }

    public final CommandLine getCommandLine() {
        return commandLine;
    }

    protected final void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    // unit tests will want 'fake' so that the glassFishProcess is not really started.
    enum LaunchType {
        normal,
        debug,
        trace,
        /**
         * Useful just for unit tests so the server will not be started.
         */
        fake
    }

    void setMode(LaunchType mode) {
        this.mode = mode;
    }

    LaunchType getMode() {
        return mode;
    }

    final long getStartTime() {
        return startTime;
    }

    abstract List<File> getMainClasspath() throws GFLauncherException;

    abstract String getMainClass() throws GFLauncherException;

    final Map<String, String> getEnvProps() {
        return asenvProps;
    }


    private ASenvPropertyReader getAsEnvConfReader() {
        if (isFakeLaunch()) {
            return new ASenvPropertyReader(callerParameters.getInstallDir());
        }

        return new ASenvPropertyReader();
    }

    private List<String> getDebugOptionsFromDomainXMLJavaConfig() {
        if (callerParameters.isDebug() || callerParameters.isSuspend() || domainXMLjavaConfig.isDebugEnabled()) {
            // Suspend setting from domain.xml can be overridden by caller
            if (!callerParameters.isSuspend()) {
                return domainXMLjavaConfig.getDebugOptions();
            }

            return domainXMLjavaConfig.getDebugOptions().stream().filter(e -> e.startsWith("-agentlib:jdwp"))
                    .map(e -> e.replace("suspend=n", "suspend=y")).collect(toList());
        }

        return emptyList();
    }

    /**
     *
     * look for an option of this form:
     * <code>-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9009</code>
     * and extract the suspend and port values.
     *
     */
    private void parseJavaConfigDebugOptions() {
        for (String option : domainXMLjavaConfigDebugOptions) {
            if (!option.startsWith("-agentlib:jdwp")) {
                continue;
            }

            String[] attributes = option.substring(10).split(",");
            for (String attribute : attributes) {
                if (attribute.startsWith("address=")) {
                    try {
                        debugPort = Integer.parseInt(attribute.substring(10));
                    } catch (NumberFormatException ex) {
                        debugPort = -1;
                    }
                }

                if (attribute.startsWith("suspend=")) {
                    try {
                        debugSuspend = attribute.substring(8).toLowerCase(Locale.getDefault()).equals("y");
                    } catch (Exception ex) {
                        debugSuspend = false;
                    }
                }
            }
        }
    }

    private void setLogFilename(MiniXmlParser domainXML) {
        logFilename = domainXML.getLogFilename();

        if (logFilename == null) {
            logFilename = DEFAULT_LOGFILE;
        }
    }

    private void resolveAllTokens() {
        // resolve jvm-options against:
        // 1. itself
        // 2. <system-property>'s from domain.xml
        // 3. system properties -- essential there is, e.g. "${path.separator}" in domain.xml
        // 4. asenvProps
        // 5. env variables (if the above contain a switch to prefer env variables, then they go first, before 1.
        // i.e. add in reverse order to get the precedence right

        Map<String, String> all = new HashMap<>();

        all.putAll(asenvProps);
        all.putAll(propertiesToStringMap(System.getProperties()));
        all.putAll(domainXMLSystemProperty);
        all.putAll(domainXMLjvmOptions.getCombinedMap());
        all.putAll(domainXMLJavaConfigProfiler.getConfig());

        if (isPreferEnvOverProperties(all)) {
            all.putAll(System.getenv());
            replacePropertiesWithEnvVars(domainXMLjvmOptions.xProps);
            replacePropertiesWithEnvVars(domainXMLjvmOptions.xxProps);
            replacePropertiesWithEnvVars(domainXMLjvmOptions.plainProps);
            replacePropertiesWithEnvVars(domainXMLjvmOptions.longProps);
            replacePropertiesWithEnvVars(domainXMLjvmOptions.sysProps);
        } else {
            System.getenv().forEach((name, value) -> all.putIfAbsent(name, value));
        }

        TokenResolver resolver = new TokenResolver(all);
        resolver.resolve(domainXMLjvmOptions.xProps);
        resolver.resolve(domainXMLjvmOptions.xxProps);
        resolver.resolve(domainXMLjvmOptions.plainProps);
        resolver.resolve(domainXMLjvmOptions.longProps);
        resolver.resolve(domainXMLjvmOptions.sysProps);
        resolver.resolve(domainXMLjavaConfig.getMap());
        resolver.resolve(domainXMLJavaConfigProfiler.getConfig());
        resolver.resolve(domainXMLjavaConfigDebugOptions);

        logFilename = resolver.resolve(logFilename);
        adminFileRealmKeyFile = resolver.resolve(adminFileRealmKeyFile);
    }

    private void replacePropertiesWithEnvVars(Map<String, String> properties) {
        Pattern invalidEnvVarCharsPattern = Pattern.compile("[^_0-9a-zA-Z]");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String envValue = System.getenv(entry.getKey());
            if (envValue != null) {
                entry.setValue(envValue);
            } else {
                final String sanitizedKey = invalidEnvVarCharsPattern
                        .matcher(entry.getKey())
                        .replaceAll("_");
                envValue = System.getenv(sanitizedKey);
                if (envValue != null) {
                    entry.setValue(envValue);
                } else {
                    envValue = System.getenv(sanitizedKey.toUpperCase());
                    if (envValue != null) {
                        entry.setValue(envValue);
                    }
                }
            }

        }
    }

    private static Boolean isPreferEnvOverProperties(Map<String, String> properties) {
        return Boolean.parseBoolean(properties.get(PREFER_ENV_VARS_OVER_PROPERTIES));
    }

    private void fixLogFilename() throws GFLauncherException {
        if (!ok(logFilename)) {
            logFilename = DEFAULT_LOGFILE;
        }

        File logFile = new File(logFilename);
        if (!logFile.isAbsolute()) {
            // this is quite normal. Logging Service will by default return a relative path!
            logFile = new File(callerParameters.getInstanceRootDir(), logFilename);
        }

        // Get rid of garbage like "c:/gf/./././../gf"
        logFile = logFile.toPath().toAbsolutePath().normalize().toFile();

        // if the file doesn't exist -- make sure the parent dir exists
        // this is common in unit tests AND the first time the instance is
        // started....
        if (!logFile.exists()) {
            File parent = logFile.getParentFile();
            if (!parent.isDirectory()) {
                boolean wasCreated = parent.mkdirs();
                if (!wasCreated) {
                    logFile = null; // give up!!
                }
            }
        }

        if (logFile == null) {
            logFilename = null;
        } else {
            logFilename = logFile.getPath();
        }

        logFilenameWasFixed = true;
    }

    private void setJavaExecutable() throws GFLauncherException {
        // first choice is from domain.xml
        if (setJavaExecutableIfValid(domainXMLjavaConfig.getJavaHome())) {
            return;
        }

        // second choice is from asenv
        if (!setJavaExecutableIfValid(asenvProps.get(JAVA_ROOT_PROPERTY))) {
            throw new GFLauncherException("nojvm");
        }

    }

    boolean setJavaExecutableIfValid(String filename) {
        if (!ok(filename)) {
            return false;
        }

        File javaFile = new File(filename);

        if (!javaFile.isDirectory()) {
            return false;
        }

        if (GFLauncherUtils.isWindows()) {
            javaFile = new File(javaFile, "bin/java.exe");
        } else {
            javaFile = new File(javaFile, "bin/java");
        }

        if (javaFile.exists()) {
            javaExe = javaFile.toPath().toAbsolutePath();
            return true;
        }

        return false;
    }

    void setClasspath() throws GFLauncherException {
        List<File> mainCP = getMainClasspath();
        List<File> envCP = domainXMLjavaConfig.getEnvClasspath();
        List<File> sysCP = domainXMLjavaConfig.getSystemClasspath();
        List<File> prefixCP = domainXMLjavaConfig.getPrefixClasspath();
        List<File> suffixCP = domainXMLjavaConfig.getSuffixClasspath();
        List<File> profilerCP = domainXMLJavaConfigProfiler.getClasspath();

        // create a list of all the classpath pieces in the right order
        List<File> all = new ArrayList<>();
        all.addAll(prefixCP);
        all.addAll(profilerCP);
        all.addAll(mainCP);
        all.addAll(sysCP);
        all.addAll(envCP);
        all.addAll(suffixCP);
        setClasspath(all.toArray(File[]::new));
    }

    void initCommandLine() throws GFLauncherException {
        final boolean useScript = !getInfo().isVerboseOrWatchdog() && isSurviveWinUserSession();
        final CommandLine cmdLine = new CommandLine(useScript ? CommandFormat.Script : CommandFormat.ProcessBuilder);
        cmdLine.append(javaExe);
        if (classpath.length > 0) {
            cmdLine.appendClassPath(getClasspath());
        }
        addIgnoreNull(cmdLine, domainXMLjavaConfigDebugOptions);

        String CLIStartTime = System.getProperty("WALL_CLOCK_START");
        if (CLIStartTime != null) {
            cmdLine.append("-DWALL_CLOCK_START=" + CLIStartTime);
        }

        if (debugPort >= 0) {
            cmdLine.appendSystemOption(DEBUG_MODE_PROPERTY, TRUE.toString());
        }

        if (domainXMLjvmOptions != null) {
            domainXMLjvmOptions.toList().forEach(cmdLine::appendJavaOption);
        }

        GFLauncherNativeHelper nativeHelper = new GFLauncherNativeHelper(callerParameters, domainXMLjavaConfig,
                domainXMLjvmOptions, domainXMLJavaConfigProfiler);
        cmdLine.appendNativeLibraryPath(nativeHelper.getNativePath());
        addIgnoreNull(cmdLine, getMainClass());

        try {
            addIgnoreNull(cmdLine, getInfo().getArgsAsList());
        } catch (GFLauncherException gfle) {
            throw gfle;
        } catch (Exception e) {
            throw new GFLauncherException(e);
        }

        setCommandLine(cmdLine);
    }

    void setJvmOptions() {
        domainXMLJvmOptionsAsList.clear();
        if (domainXMLjvmOptions != null) {
            domainXMLjvmOptions.toList().forEach(domainXMLJvmOptionsAsList::add);
        }
    }

    void logCommandLine() {
        if (!isFakeLaunch()) {
            GFLauncherLogger.info(COMMAND_LINE, commandLine.toString("\n"));
        }
    }

    public final List<String> getJvmOptions() {
        return domainXMLJvmOptionsAsList;
    }

    private void addIgnoreNull(CommandLine command, String s) {
        if (ok(s)) {
            command.append(s);
        }
    }

    private void addIgnoreNull(CommandLine command, Collection<String> ss) {
        if (ss != null && !ss.isEmpty()) {
            ss.forEach(command::append);
        }
    }

    private void wait(final Process p) throws GFLauncherException {
        try {
            p.waitFor();
            exitValue = p.exitValue();
        } catch (InterruptedException ex) {
            throw new GFLauncherException("verboseInterruption", ex, ex);
        }
    }

    private void setupProfilerAndJvmOptions(MiniXmlParser domainXML) throws MiniXmlParserException, GFLauncherException {
        // Add JVM options from Profiler *last* so they override config's JVM options
        domainXMLJavaConfigProfiler = new Profiler(domainXML.getProfilerConfig(), domainXML.getProfilerJvmOptions(),
                domainXML.getProfilerSystemProperties());

        List<String> rawJvmOptions = domainXML.getJvmOptions();
        rawJvmOptions.addAll(getSpecialSystemProperties());
        if (domainXMLJavaConfigProfiler.isEnabled()) {
            rawJvmOptions.addAll(domainXMLJavaConfigProfiler.getJvmOptions());
        }

        domainXMLjvmOptions = new JvmOptions(rawJvmOptions);
        if (callerParameters.isDropInterruptedCommands()) {
            domainXMLjvmOptions.sysProps.put(DROP_INTERRUPTED_COMMANDS, TRUE.toString());
        }
    }

    private void setupUpgradeSecurity() throws GFLauncherException {
        // If this is an upgrade and the security manager is on,
        // copy the current server.policy file to the domain
        // before the upgrade.
        if (callerParameters.isUpgrade() && domainXMLjvmOptions.sysProps.containsKey("java.security.manager")) {

            GFLauncherLogger.info(GFLauncherLogger.copy_server_policy);
            Path source = callerParameters.installDir.toPath().resolve(Path.of("lib", "templates", "server.policy"));
            Path target = callerParameters.getConfigDir().toPath().resolve("server.policy");
            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                // the actual error is wrapped differently depending on
                // whether the problem was with the source or target
                Throwable cause = ioe.getCause() == null ? ioe : ioe.getCause();
                throw new GFLauncherException(strings.get("copy_server_policy_error", cause.getMessage()), ioe);
            }
        }
    }

    /**
     * Because of some issues in GlassFish OSGi launcher, a server updated from
     * version 3.0.x to 3.1 won't start if a OSGi cache directory populated with
     * 3.0.x modules is used. So, as a work around, we rename the cache
     * directory when upgrade path is used. See GLASSFISH-15772 for more
     * details.
     *
     * @throws GFLauncherException if it fails to rename the cache directory
     */
    private void renameOsgiCache() throws GFLauncherException {
        if (callerParameters.isUpgrade()) {
            File osgiCacheDir = new File(callerParameters.getDomainRootDir(), "osgi-cache");
            File backupOsgiCacheDir = new File(callerParameters.getDomainRootDir(), "osgi-cache-" + System.currentTimeMillis());
            if (osgiCacheDir.exists() && !backupOsgiCacheDir.exists()) {
                if (FileUtils.renameFile(osgiCacheDir, backupOsgiCacheDir)) {
                    GFLauncherLogger.fine("rename_osgi_cache_succeeded", osgiCacheDir, backupOsgiCacheDir);
                } else {
                    throw new GFLauncherException(strings.get("rename_osgi_cache_failed", osgiCacheDir, backupOsgiCacheDir));
                }
            }
        }
    }

    private void setupMonitoring(MiniXmlParser parser) {
        // As usual we have to be very careful.

        // If it is NOT enabled -- we are out of here!!!
        if (parser.isMonitoringEnabled() == false) {
            return;
        }

        // if the user has a hard-coded "-javaagent" jvm-option that uses OUR jar
        // then we do NOT want to add our own.
        Set<String> plainKeys = domainXMLjvmOptions.plainProps.keySet();
        for (String key : plainKeys) {
            if (key.startsWith("javaagent:")) {
                // complications -- of course!! They may have mix&match forward and back slashes
                key = key.replace('\\', '/');
                if (key.indexOf(FLASHLIGHT_AGENT_NAME) >= 0) {
                    return; // Done!!!!
                }
            }
        }

        // It is not already specified AND monitoring is enabled.
        try {
            domainXMLjvmOptions.plainProps.put(getMonitoringAgentJvmOptionString(), null);
        } catch (GFLauncherException gfe) {
            // This has been defined as a non-fatal error.
            // Silently ignore it -- but do NOT add it as an option
        }
    }

    private String getMonitoringAgentJvmOptionString() throws GFLauncherException {
        File libMonDir = new File(getInfo().getInstallDir(), LIBMON_NAME);
        File flashlightJarFile = new File(libMonDir, FLASHLIGHT_AGENT_NAME);

        if (flashlightJarFile.isFile()) {
            return "javaagent:" + flashlightJarFile.toPath().toAbsolutePath().normalize();
        }
        String msg = strings.get("no_flashlight_agent", flashlightJarFile);
        GFLauncherLogger.warning(GFLauncherLogger.NO_FLASHLIGHT_AGENT, flashlightJarFile);
        throw new GFLauncherException(msg);
    }


    private List<String> getSpecialSystemProperties() throws GFLauncherException {
        Map<String, String> props = new HashMap<>();
        props.put(INSTALL_ROOT_PROPERTY, getInfo().getInstallDir().getAbsolutePath());
        props.put(INSTANCE_ROOT_PROPERTY, getInfo().getInstanceRootDir().getAbsolutePath());

        return propsToJvmOptions(props);
    }

    final File[] getClasspath() {
        return classpath;
    }

    final void setClasspath(File... classpath) {
        this.classpath = classpath;
    }

    private List<String> propsToJvmOptions(Map<String, String> map) {
        List<String> ss = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = map.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            String jvm = "-D" + name;

            if (value != null) {
                jvm += "=" + value;
            }

            ss.add(jvm);
        }

        return ss;
    }

    private void setupLogLevels() {
        if (callerParameters.isVerbose()) {
            GFLauncherLogger.setConsoleLevel(java.util.logging.Level.INFO);
        } else {
            GFLauncherLogger.setConsoleLevel(java.util.logging.Level.WARNING);
        }
    }

    /**
     * Checks whether to use launchctl for start up by checking if mac os
     * version < 10.10
     *
     * @return True if osversion < 10.10
     */
    private static boolean useLaunchCtl(String osversion) {

        int major = 0;
        int minor = 0;

        if (osversion == null || osversion.isEmpty()) {
            return false;
        }

        String[] split = osversion.split("[\\._\\-]+");

        try {
            if (split.length > 0 && split[0].length() > 0) {
                major = Integer.parseInt(split[0]);
            }
            if (split.length > 1 && split[1].length() > 0) {
                minor = Integer.parseInt(split[1]);
            }

            return major <= 9 || major <= 10 && minor < 10;
        } catch (NumberFormatException e) {
            // Assume version is 10.10 or later.
            return false;
        }
    }

    private static boolean isSurviveWinUserSession() {
        String surviveSessionValue = System.getenv("AS_SURVIVE_WIN_USER_SESSION");
        if (surviveSessionValue == null) {
            return isWindows() && isOverSSHSession();
        }
        return Boolean.parseBoolean(surviveSessionValue);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    }

    private static boolean isOverSSHSession() {
        return System.getenv("SSH_CLIENT") != null || System.getenv("SSH_CONNECTION") != null
                || System.getenv("SSH_TTY") != null;
    }

    private static List<String> prepareWindowsEnvironment(final CommandLine command, final Path configDir,
            final boolean stdinPreloaded) throws GFLauncherException {
        try {
            final Path startPsFile = createStartPsScript(command, configDir, stdinPreloaded);
            final Path schedulerPsFile = createSchedulerPsScript(startPsFile, configDir, stdinPreloaded);
            final List<String> cmds = new ArrayList<>();
            cmds.add("powershell.exe");
            if (stdinPreloaded) {
                cmds.add("-noninteractive");
            }
            cmds.add("-File");
            cmds.add("\"" + schedulerPsFile + "\"");
            return cmds;
        } catch (IOException e) {
            throw new GFLauncherException(e);
        }
    }


    private static Path createSchedulerPsScript(final Path startPsFile, final Path configDir,
        final boolean stdinPreloaded) throws IOException {
        final StringBuilder schedulerFileContent = new StringBuilder(8192);
        schedulerFileContent.append("$ErrorActionPreference = \"Stop\"\n\n");

        schedulerFileContent.append("$pidFile = \"").append(new File(configDir.toFile(), "pid").getAbsolutePath()).append("\"\n");
        schedulerFileContent.append("if (Test-Path $pidFile) {\n");
        schedulerFileContent.append("    Remove-Item $pidFile -Force\n");
        schedulerFileContent.append("}\n");
        if (stdinPreloaded) {
            schedulerFileContent.append("$stdin = [System.IO.StreamReader]::new([Console]::OpenStandardInput()).ReadToEnd()\n");
            schedulerFileContent.append("$tempFile = [System.IO.Path]::GetTempFileName()\n");
            schedulerFileContent.append("[System.IO.File]::WriteAllText($tempFile, $stdin)\n");
        }
        schedulerFileContent.append("$action = New-ScheduledTaskAction -Execute \"powershell.exe\" -Argument \"-File `\"").append(startPsFile).append("`\" `\"$tempFile`\"\"\n");
        schedulerFileContent.append("$taskName = \"GlassFishInstance_\" + [System.Guid]::NewGuid().ToString()\n");
        schedulerFileContent.append("$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date).AddSeconds(1)\n");
        schedulerFileContent.append("$principal = New-ScheduledTaskPrincipal -UserId $env:USERNAME -LogonType S4U\n");
        schedulerFileContent.append("$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -ExecutionTimeLimit (New-TimeSpan -Hours 0)\n");

        schedulerFileContent.append("Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Principal $principal -Settings $settings\n");
        schedulerFileContent.append("Start-ScheduledTask -TaskName $taskName\n");
        schedulerFileContent.append("Unregister-ScheduledTask -TaskName $taskName -Confirm:$false\n");
        schedulerFileContent.append("$start = Get-Date\n");
        schedulerFileContent.append("$timeout = 60\n");
        schedulerFileContent.append("while (-not (Test-Path $pidFile)) {\n");
        schedulerFileContent.append("    if ((New-TimeSpan -Start $start -End (Get-Date)).TotalSeconds -gt $timeout) {\n");
        if (stdinPreloaded) {
            schedulerFileContent.append("        Remove-Item $tempFile -Force\n");
        }
        schedulerFileContent.append("        Write-Error \"Timeout waiting for GlassFish to start (pid file not created within $timeout seconds)\"\n");
        schedulerFileContent.append("        exit 1\n");
        schedulerFileContent.append("    }\n");
        schedulerFileContent.append("    Start-Sleep -Seconds 1\n");
        schedulerFileContent.append("}\n");
        if (stdinPreloaded) {
            schedulerFileContent.append("Remove-Item $tempFile -Force\n");
        }
        final Path schedulerPsFile = configDir.resolve("scheduler.ps1");
        Files.writeString(schedulerPsFile, schedulerFileContent, UTF_8, TRUNCATE_EXISTING, CREATE);
        return schedulerPsFile;
    }

    private static Path createStartPsScript(final CommandLine command, final Path configDir, final boolean stdinPreloaded)
        throws IOException {
        final List<String> commandArgs = command.toList();
        final StringBuilder scriptBlock = new StringBuilder();
        scriptBlock.append("$javaExe = ").append(commandArgs.get(0)).append('\n');
        if (stdinPreloaded) {
            scriptBlock.append("$tempFile = $args[0]\n");
        }
        scriptBlock.append("$javaArgs = @(").append(toPowerShellArgumentList(commandArgs)).append(")\n");

        scriptBlock.append("Start-Process -NoNewWindow -PassThru -FilePath ");
        scriptBlock.append(commandArgs.get(0));
        if (stdinPreloaded) {
            scriptBlock.append(" -RedirectStandardInput");
            scriptBlock.append(" \"$tempFile\"");
        }
        scriptBlock.append(" -ArgumentList $javaArgs\n");
        final Path startPsFile = configDir.resolve("start.ps1");
        Files.writeString(startPsFile, scriptBlock, UTF_8, TRUNCATE_EXISTING, CREATE);
        return startPsFile;
    }

    private static StringBuilder toPowerShellArgumentList(List<String> command) {
        StringBuilder psContent = new StringBuilder();
        for (int i = 1; i < command.size(); i++) {
            psContent.append('\n');
            psContent.append('"');
            psContent.append(command.get(i).replace("\"", "`\""));
            psContent.append('"');
        }
        return psContent;
    }


    private static void writeSecurityTokens(Process glassfishProcess, ProcessStreamDrainer drainer,
        List<String> securityTokens) throws GFLauncherException, IOException {
        OutputStream os = glassfishProcess.getOutputStream();
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            osw = new OutputStreamWriter(os, Charset.defaultCharset());
            bw = new BufferedWriter(osw);
            for (String token : securityTokens) {
                bw.write(token);
                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            handleDeadProcess(glassfishProcess, drainer);
            // glassFishProcess is not dead, but got some other exception, rethrow it
            throw e;
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (osw != null) {
                osw.close();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                    // nothing to do
                }
            }
            if (bw != null) {
                handleDeadProcess(glassfishProcess, drainer);
            }
        }
    }


    private static void handleDeadProcess(Process glassfishProcess, ProcessStreamDrainer drainer)
        throws GFLauncherException {
        String trace = getDeadProcessTrace(glassfishProcess, drainer);
        if (trace != null) {
            throw new GFLauncherException(trace);
        }
    }

    /**
     * @returns null in case the process is NOT dead or succeeded
     */
    private static String getDeadProcessTrace(Process process, ProcessStreamDrainer drainer) {
        if (process.isAlive()) {
            return null;
        }
        int ev = process.exitValue();
        if (ev == 0) {
            return null;
        }
        String output = drainer.getOutErrString();
        return strings.get("server_process_died", ev, output);
    }

}
