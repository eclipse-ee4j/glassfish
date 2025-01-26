/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessStreamDrainer;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.sun.enterprise.admin.launcher.GFLauncher.LaunchType.fake;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.DEFAULT_LOGFILE;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.FLASHLIGHT_AGENT_NAME;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.LIBMON_NAME;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.NEWLINE;
import static com.sun.enterprise.admin.launcher.GFLauncherLogger.COMMAND_LINE;
import static com.sun.enterprise.universal.collections.CollectionUtils.propertiesToStringMap;
import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.ok;
import static com.sun.enterprise.universal.io.SmartFile.sanitize;
import static com.sun.enterprise.universal.process.ProcessStreamDrainer.dispose;
import static com.sun.enterprise.universal.process.ProcessStreamDrainer.redirect;
import static com.sun.enterprise.universal.process.ProcessStreamDrainer.save;
import static com.sun.enterprise.util.OS.isDarwin;
import static com.sun.enterprise.util.SystemPropertyConstants.DEBUG_MODE_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.DROP_INTERRUPTED_COMMANDS;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JAVA_ROOT_PROPERTY;
import static java.lang.Boolean.TRUE;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * This is the main Launcher class designed for external and internal usage.
 *
 * <p>
 * Each of the 3 kinds of server, domain, node-agent and instance, need to subclass this class.
 *
 * @author bnevins
 */
public abstract class GFLauncher {

    private static final LocalStringsImpl I18N = new LocalStringsImpl(GFLauncher.class);
    private static final Logger LOG = System.getLogger(GFLauncher.class.getName(), I18N.getBundle());
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncher.class);

    /**
     * Parameters provided by the caller of a launcher, either programmatically (for GF embedded) or as commandline
     * parameters (GF DAS or Instance).
     *
     */
    private final GFLauncherInfo callerParameters;

    /**
     * Properties from asenv.conf, such as <code>AS_DEF_DOMAINS_PATH="../domains"</code>
     */
    private Map<String, String> asenvProps;

    /**
     * The <code>java-config</code> attributes in domain.xml
     */
    private JavaConfig domainXMLjavaConfig;

    /**
     * the <code>debug-options</code> attribute from <code>java-config</code> in domain.xml
     */
    private List<String> domainXMLjavaConfigDebugOptions;

    /**
     * The debug port (<code>address</code>) primarily extracted from <code>domainXMLjavaConfigDebugOptions</code>
     */
    private int debugPort = -1;

    /**
     * The debug suspend (<code>suspend</code>) primarily extracted from <code>domainXMLjavaConfigDebugOptions</code>
     */
    private boolean debugSuspend;

    /**
     * The combined <code>jvm-options</code> from <code>java-config</code>, <code>profiler</code> in domain.xml and extra
     * ones added by this launcher
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

    private String javaExe;
    private String classpath;
    private String adminFileRealmKeyFile;
    private boolean secureAdminEnabled;

    /**
     * The file name to log to using a <code>java.util.logging.FileHandler.FileHandler</code>
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
     * The full commandline string used to start GlassFish in process <code<>glassFishProcess</code>
     */
    private final List<String> commandLine = new ArrayList<>();

    /**
     * Time when GlassFish was launched
     */
    private long startTime;

    /**
     * The process which is running GlassFish
     */
    private Process glassFishProcess;
    private ProcessWhacker processWhacker;
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
     * Launches the server. Any fatal error results in a GFLauncherException No unchecked Throwables of any kind will be
     * thrown.
     *
     * @throws com.sun.enterprise.admin.launcher.GFLauncherException
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
            // Typically invokes launchInstance()
            internalLaunch();
        } catch (GFLauncherException gfe) {
            throw gfe;
        } catch (Throwable t) {
            // hk2 might throw a java.lang.Error
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

    public final void launchJVM(List<String> cmdsIn) throws GFLauncherException {
        try {
            setup(); // we only use one thing -- the java executable
            List<String> commands = new LinkedList<>();
            commands.add(javaExe);

            for (String cmd : cmdsIn) {
                commands.add(cmd);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            ProcessStreamDrainer.drain("launchJVM", process); // just to be safe
        } catch (GFLauncherException gfe) {
            throw gfe;
        } catch (Throwable t) {
            // hk2 might throw a java.lang.Error
            throw new GFLauncherException(strings.get("unknownError", t.getMessage()), t);
        } finally {
            GFLauncherLogger.removeLogFileHandler();
        }
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
        setCommandLine();
        setJvmOptions();
        logCommandLine();

        // if no <network-config> element, we need to upgrade this domain
        needsAutoUpgrade = !domainXML.hasNetworkConfig();
        needsManualUpgrade = !domainXML.hasDefaultConfig();
        setupCalledByClients = true;
    }

    /**
     *
     * @return The callerParameters object that contains startup callerParameters
     */
    public final GFLauncherInfo getInfo() {
        return callerParameters;
    }

    /**
     * Returns the admin realm key file for the server, if the admin realm is a FileRealm. Otherwise return null. This value
     * can be used to create a FileRealm for the server.
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
     * Returns the exit value of the glassFishProcess. This only makes sense when we ran in verbose mode and waited for the
     * glassFishProcess to exit in the wait() method. Caveat Emptor!
     *
     * @return the glassFishProcess' exit value if it completed and we waited. Otherwise it returns -1
     */
    public final int getExitValue() {
        return exitValue;
    }

    /**
     * You don't want to call this before calling launch because it would not make sense.
     *
     * @return The Process object of the launched Server glassFishProcess. you will either get a valid Process object or an
     * Exceptio will be thrown. You are guaranteed not to get a null.
     * @throws GFLauncherException if the Process has not been created yet - call launch() before calling this method.
     */
    public final Process getProcess() throws GFLauncherException {
        if (glassFishProcess == null) {
            throw new GFLauncherException("invalid_process");
        }

        return glassFishProcess;
    }

    /**
     * A ProcessStreamDrainer is always attached to every Process created here. It is handy for getting the stdin and stdout
     * as a nice String.
     *
     * @return A valid ProcessStreamDrainer. You are guaranteed to never get a null.
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
     * Return the port number of the debug port, or -1 if debugging is not enabled.
     *
     * @return the debug port, or -1 if not debugging
     */
    public final int getDebugPort() {
        return debugPort;
    }

    /**
     * Return true if suspend=y AND debugging is on. otherwise return false.
     *
     * @return true if suspending, or false if either not suspending or not debugging
     */
    public final boolean isDebugSuspend() {
        return debugPort >= 0 && debugSuspend;
    }

    /**
     * Does this domain need to be automatically upgraded before it can be started?
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

        List<String> cmds = null;

        // Use launchctl bsexec on MacOS versions before 10.10
        // otherwise use regular startup.
        // (No longer using StartupItemContext).
        // See GLASSFISH-21343
        if (isDarwin() && useLaunchCtl(System.getProperty("os.version")) && !getInfo().isVerboseOrWatchdog()) {

            // On MacOS we need to start long running glassFishProcess with
            // StartupItemContext. See IT 12942
            cmds = new ArrayList<>();

            // cmds.add("/usr/libexec/StartupItemContext");
            // In MacOS 10.10 they removed StartupItemContext
            // so call launchctl directly doing what StartupItemContext did
            // See GLASSFISH-21113
            cmds.add("launchctl");
            cmds.add("bsexec");
            cmds.add("/");
            cmds.addAll(getCommandLine());
        } else {
            cmds = getCommandLine();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(cmds);

        // Change the directory if there is one specified, o/w stick with the
        // default.
        try {
            processBuilder.directory(getInfo().getConfigDir());
        } catch (Exception e) {
        }

        // Run the glassFishProcess and attach Stream Drainers
        try {
            closeStandardStreamsMaybe();

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

            writeSecurityTokens(glassFishProcess);
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

    public final List<String> getCommandLine() {
        return commandLine;
    }

    // unit tests will want 'fake' so that the glassFishProcess is not really started.
    enum LaunchType {
        normal,
        debug,
        trace,
        /** Useful just for unit tests so the server will not be started. */
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

    /**
     * Checks whether to use launchctl for start up by checking if mac os version < 10.10
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
     * look for an option of this form: <code>-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9009</code> and
     * extract the suspend and port values.
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

    private void setLogFilename(MiniXmlParser domainXML) throws GFLauncherException {
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
        // 5. env variables
        // i.e. add in reverse order to get the precedence right

        Map<String, String> all = new HashMap<>();

        all.putAll(System.getenv());
        all.putAll(asenvProps);
        all.putAll(propertiesToStringMap(System.getProperties()));
        all.putAll(domainXMLSystemProperty);
        all.putAll(domainXMLjvmOptions.getCombinedMap());
        all.putAll(domainXMLJavaConfigProfiler.getConfig());

        TokenResolver resolver = new TokenResolver(all);
        resolver.resolve(domainXMLjvmOptions.xProps);
        resolver.resolve(domainXMLjvmOptions.xxProps);
        resolver.resolve(domainXMLjvmOptions.plainProps);
        resolver.resolve(domainXMLjvmOptions.sysProps);
        resolver.resolve(domainXMLjavaConfig.getMap());
        resolver.resolve(domainXMLJavaConfigProfiler.getConfig());
        resolver.resolve(domainXMLjavaConfigDebugOptions);

        logFilename = resolver.resolve(logFilename);
        adminFileRealmKeyFile = resolver.resolve(adminFileRealmKeyFile);
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
        logFile = sanitize(logFile);

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
            javaExe = sanitize(javaFile).getPath();
            return true;
        }

        return false;
    }

    void setClasspath() throws GFLauncherException {
        List<File> mainCP = getMainClasspath(); // subclass provides this
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
        setClasspath(GFLauncherUtils.fileListToPathString(all));
    }

    void setCommandLine() throws GFLauncherException {
        List<String> cmdLine = getCommandLine();
        cmdLine.clear();
        addIgnoreNull(cmdLine, javaExe);
        addIgnoreNull(cmdLine, "-cp");
        addIgnoreNull(cmdLine, getClasspath());
        addIgnoreNull(cmdLine, domainXMLjavaConfigDebugOptions);

        String CLIStartTime = System.getProperty("WALL_CLOCK_START");
        if (CLIStartTime != null && CLIStartTime.length() > 0) {
            cmdLine.add("-DWALL_CLOCK_START=" + CLIStartTime);
        }

        if (debugPort >= 0) {
            cmdLine.add("-D" + DEBUG_MODE_PROPERTY + "=" + TRUE);
        }

        if (domainXMLjvmOptions != null) {
            addIgnoreNull(cmdLine, domainXMLjvmOptions.toList());
        }

        GFLauncherNativeHelper nativeHelper = new GFLauncherNativeHelper(callerParameters, domainXMLjavaConfig, domainXMLjvmOptions,
                domainXMLJavaConfigProfiler);
        addIgnoreNull(cmdLine, nativeHelper.getCommands());
        addIgnoreNull(cmdLine, getMainClass());

        try {
            addIgnoreNull(cmdLine, getInfo().getArgsAsList());
        } catch (GFLauncherException gfle) {
            throw gfle;
        } catch (Exception e) {
            // harmless
        }
    }

    void setJvmOptions() throws GFLauncherException {
        domainXMLJvmOptionsAsList.clear();

        if (domainXMLjvmOptions != null) {
            addIgnoreNull(domainXMLJvmOptionsAsList, domainXMLjvmOptions.toList());
        }

    }

    void logCommandLine() {
        StringBuilder sb = new StringBuilder();

        if (!isFakeLaunch()) {
            Iterable<String> cmdLine = getCommandLine();

            for (String s : cmdLine) {
                sb.append(NEWLINE);
                sb.append(s);
            }
            GFLauncherLogger.info(COMMAND_LINE, sb.toString());
        }
    }

    public final List<String> getJvmOptions() {
        return domainXMLJvmOptionsAsList;
    }

    private void addIgnoreNull(List<String> list, String s) {
        if (ok(s)) {
            list.add(s);
        }
    }

    private void addIgnoreNull(List<String> list, Collection<String> ss) {
        if (ss != null && !ss.isEmpty()) {
            list.addAll(ss);
        }
    }

    private void wait(final Process p) throws GFLauncherException {
        try {
            setShutdownHook(p);
            p.waitFor();
            exitValue = p.exitValue();
        } catch (InterruptedException ex) {
            throw new GFLauncherException("verboseInterruption", ex, ex);
        }
    }

    private void setShutdownHook(final Process p) {
        // ON UNIX a ^C on the console will also kill DAS
        // On Windows a ^C on the console will not kill DAS
        // We want UNIX behavior on Windows
        // note that the hook thread will run in both cases:
        // 1. the server died on its own, e.g. with a stop-domain
        // 2. a ^C (or equivalent signal) was received by the console
        // note that exitValue is still set to -1

        // if we are restarting we may get many many processes.
        // Each time this method is called we reset the Process reference inside
        // the processWhacker

        if (processWhacker == null) {
            Runtime runtime = Runtime.getRuntime();
            final String msg = strings.get("serverStopped", callerParameters.getType());
            processWhacker = new ProcessWhacker(p, msg);

            runtime.addShutdownHook(new Thread(processWhacker, "GlassFish Process Whacker Shutdown Hook"));
        } else {
            processWhacker.setProcess(p);
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

    private void writeSecurityTokens(Process sp) throws GFLauncherException, IOException {
        handleDeadProcess();
        OutputStream os = sp.getOutputStream();
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            osw = new OutputStreamWriter(os, Charset.defaultCharset());
            bw = new BufferedWriter(osw);
            for (String token : callerParameters.securityTokens) {
                bw.write(token);
                bw.newLine();
                bw.flush(); // flushing once is ok too
            }
        } catch (IOException e) {
            handleDeadProcess();
            throw e; // glassFishProcess is not dead, but got some other exception, rethrow it
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
                handleDeadProcess();
            }
        }
    }

    private void handleDeadProcess() throws GFLauncherException {
        String trace = getDeadProcessTrace(glassFishProcess);
        if (trace != null) {
            throw new GFLauncherException(trace);
        }
    }

    private String getDeadProcessTrace(Process sp) throws GFLauncherException {
        // returns null in case the glassFishProcess is NOT dead
        try {
            int ev = sp.exitValue();
            ProcessStreamDrainer psd1 = getProcessStreamDrainer();
            String output = psd1.getOutErrString();
            String trace = strings.get("server_process_died", ev, output);
            return trace;
        } catch (IllegalThreadStateException e) {
            // the glassFishProcess is still running and we are ok
            return null;
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
     * Because of some issues in GlassFish OSGi launcher, a server updated from version 3.0.x to 3.1 won't start if a OSGi
     * cache directory populated with 3.0.x modules is used. So, as a work around, we rename the cache directory when
     * upgrade path is used. See GLASSFISH-15772 for more details.
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

    private void setupMonitoring(MiniXmlParser parser) throws GFLauncherException {
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
            return "javaagent:" + getCleanPath(flashlightJarFile);
        } else {
            String msg = strings.get("no_flashlight_agent", flashlightJarFile);
            GFLauncherLogger.warning(GFLauncherLogger.NO_FLASHLIGHT_AGENT, flashlightJarFile);
            throw new GFLauncherException(msg);
        }
    }

    private static String getCleanPath(File f) {
        return sanitize(f).getPath().replace('\\', '/');
    }

    private List<String> getSpecialSystemProperties() throws GFLauncherException {
        Map<String, String> props = new HashMap<>();
        props.put(INSTALL_ROOT_PROPERTY, getInfo().getInstallDir().getAbsolutePath());
        props.put(INSTANCE_ROOT_PROPERTY, getInfo().getInstanceRootDir().getAbsolutePath());

        return propsToJvmOptions(props);
    }

    String getClasspath() {
        return classpath;
    }

    void setClasspath(String s) {
        classpath = s;
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

    private void closeStandardStreamsMaybe() {
        // see issue 12832
        // Windows bug/feature -->
        // Say glassFishProcess A (ssh) creates Process B (asadmin start-instance )
        // which then fires up Process C (the instance).
        // Process B exits but Process A does NOT. Process A is waiting for
        // Process C to exit.
        // The solution is to close down the standard streams BEFORE creating
        // Process C. Then Process A becomes convinced that the glassFishProcess it created
        // has finished.
        // If there is a console that means the user is sitting at the terminal
        // directly and we don't have to worry about it.
        // Note that the issue is inside SSH -- not inside GF code per se. I.e.
        // Process B absolutely positively does exit whether or not this code runs...
        // don't run this unless we have to because our "..." messages disappear.

        if (System.console() == null && OS.isWindows() && !callerParameters.isVerboseOrWatchdog()) {
            String sname;

            if (callerParameters.isDomain()) {
                sname = callerParameters.getDomainName();
            } else {
                sname = callerParameters.getInstanceName();
            }

            System.out.println(strings.get("ssh", sname));
            try {
                System.in.close();
            } catch (Exception e) { // ignore
            }
            try {
                System.err.close();
            } catch (Exception e) { // ignore
            }
            try {
                System.out.close();
            } catch (Exception e) { // ignore
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    private static class ProcessWhacker implements Runnable {

        private final String message;
        private Process process;

        ProcessWhacker(Process p, String msg) {
            message = msg;
            process = p;
        }

        void setProcess(Process p) {
            process = p;
        }

        @Override
        public void run() {
            // we are in a shutdown hook -- most of the JVM is gone.
            // logger won't work anymore...
            System.out.println(message);
            process.destroy();
        }

    }
}
