/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
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

import static com.sun.enterprise.admin.launcher.GFLauncherConstants.FLASHLIGHT_AGENT_NAME;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.LIBMON_NAME;
import static com.sun.enterprise.universal.collections.CollectionUtils.propertiesToStringMap;
import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.ok;
import static com.sun.enterprise.util.SystemPropertyConstants.DEBUG_MODE_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.DISABLE_ENV_VAR_EXPANSION_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.DROP_INTERRUPTED_COMMANDS;
import static com.sun.enterprise.util.SystemPropertyConstants.PREFER_ENV_VARS_OVER_PROPERTIES;
import static java.lang.Boolean.TRUE;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_ROOT;

/**
 * Prepares JVM configuration to use GlassFishMain to launch the domain or instance of the server
 * and launches it.
 *
 * @author bnevins
 */
class GlassFishMainLauncher extends GFLauncher {
    private static final Logger LOG = System.getLogger(GlassFishMainLauncher.class.getName());

    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.GlassFishMain";

    private final Long pidBeforeRestart;

    /**
     * System properties based on asenv file, but extended.
     */
    private Map<String, String> asenvProps;

    private Path javaExe;

    /**
     * The <code>java-config</code> attributes in domain.xml
     */
    private JavaConfig domainXMLjavaConfig;

    /**
     * the <code>debug-options</code> attribute from <code>java-config</code> in
     * domain.xml
     */
    private List<String> domainXMLjavaConfigDebugOptions;

    private File adminFileRealmKeyFile;

    private boolean secureAdminEnabled;

    private Path logFile;

    private boolean needsAutoUpgrade;
    private boolean needsManualUpgrade;

    /**
     * The <code>profiler<code> from <code>java-config</code> in domain.xml
     */
    private Profiler domainXMLJavaConfigProfiler;

    /**
     * The combined <code>jvm-options</code> from <code>java-config</code>,
     * <code>profiler</code> in domain.xml and extra ones added by this launcher
     */
    private JvmOptions jvmOptions;

    /**
     * The debug port (<code>address</code>) primarily extracted from
     * <code>domainXMLjavaConfigDebugOptions</code>
     */
    private Integer debugPort;

    /**
     * The debug suspend (<code>suspend</code>) primarily extracted from
     * <code>domainXMLjavaConfigDebugOptions</code>
     */
    private boolean debugSuspend;

    private boolean setup;

    private File modulepath;
    private File[] classpath;


    // sample profiler config
    //
    // <java-config classpath-suffix="" debug-enabled="false" debug-options="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009" env-classpath-ignored="true" java-home="${com.sun.aas.javaRoot}" javac-options="-g" rmic-options="-iiop -poa -alwaysgenerate -keepgenerated -g" system-classpath="">
    //   <profiler classpath="c:/dev/elf/dist/elf.jar" enabled="false" name="MyProfiler" native-library-path="c:/bin">
    //     <jvm-options>-Dprofiler3=foo3</jvm-options>
    //     <jvm-options>-Dprofiler2=foo2</jvm-options>
    //     <jvm-options>-Dprofiler1=foof</jvm-options>
    //   </profiler>

    GlassFishMainLauncher(GFLauncherInfo info) {
        super(info);
        pidBeforeRestart = resolvePidBeforeRestart();
    }

    @Override
    public Long getPidBeforeRestart() {
        return this.pidBeforeRestart;
    }

    @Override
    public File getAdminRealmKeyFile() {
        return adminFileRealmKeyFile;
    }

    @Override
    public boolean isSecureAdminEnabled() {
        return secureAdminEnabled;
    }

    @Override
    public Integer getDebugPort() {
        return debugPort;
    }

    @Override
    public boolean isSuspendEnabled() {
        return getDebugPort() != null && debugSuspend;
    }

    @Override
    public Path getLogFile() {
        return logFile;
    }

    @Override
    public final boolean needsAutoUpgrade() {
        return needsAutoUpgrade;
    }

    @Override
    public final boolean needsManualUpgrade() {
        return needsManualUpgrade;
    }


    @Override
    public void setup() throws GFLauncherException, MiniXmlParserException {
        if (setup) {
            throw new IllegalStateException("The setup() was already executed.");
        }
        setup = true;
        final GFLauncherInfo callerParameters = getParameters();
        callerParameters.setup();
        setupLogLevels(callerParameters.isVerbose());

        asenvProps = getAsEnvConfReader(callerParameters, isFakeLaunch()).getProps();

        final MiniXmlParser domainXML = new MiniXmlParser(callerParameters.getConfigFile(),
            callerParameters.getInstanceName());
        final String domainName = domainXML.getDomainName();
        if (ok(domainName)) {
            callerParameters.setDomainName(domainName);
        }

        // Can be empty! Then local commands will set what they have from user
        callerParameters.setXmlAdminAddresses(domainXML.getAdminAddresses());
        domainXMLjavaConfig = new JavaConfig(domainXML.getJavaConfig());

        domainXMLJavaConfigProfiler = createProfiler(domainXML);
        jvmOptions = createJvmOptions(callerParameters, domainXMLJavaConfigProfiler, domainXML.getJvmOptions());

        secureAdminEnabled = domainXML.getSecureAdminEnabled();

        if (domainXML.isMonitoringEnabled()) {
            setupMonitoring(callerParameters.getInstallDir());
        }
        final  Map<String, String> domainXMLSystemProperties = domainXML.getSystemProperties();
        asenvProps.put(INSTANCE_ROOT.getSystemPropertyName(), callerParameters.getInstanceRootDir().getPath());

        // Set the config java-home value as the Java home for the environment,
        // unless it is empty or it is already referring to a substitution of
        // the environment variable.
        String javaHome = domainXMLjavaConfig.getJavaHome();
        if (ok(javaHome) && !javaHome.trim().equals(JAVA_ROOT.toExpression())) {
            asenvProps.put(JAVA_ROOT.getPropertyName(), javaHome);
        }

        domainXMLjavaConfigDebugOptions = getDebugOptionsFromDomainXMLJavaConfig(callerParameters);
        setupDebugging(domainXMLjavaConfigDebugOptions);
        domainXML.setupConfigDir(callerParameters.getConfigDir(), callerParameters.getInstallDir());


        TokenResolver resolver = createTokenResolver(domainXMLSystemProperties);
        resolveAllTokens(resolver);

        adminFileRealmKeyFile = resolveAdminFileRealmKeyFile(domainXML, resolver);
        logFile = resolveLogFile(domainXML.getLogFilename(), callerParameters, resolver);
        GFLauncherLogger.addLogFileHandler(logFile);

        javaExe = resolveJavaExecutable();

        final Path installRoot = new File(asenvProps.get(INSTALL_ROOT.getPropertyName())).toPath();
        modulepath = installRoot.resolve(Path.of("lib", "bootstrap")).toAbsolutePath().normalize().toFile();
        classpath = createClasspath();
        setCommandLine(prepareCommandLine(callerParameters));

        // if no <network-config> element, we need to upgrade this domain
        needsAutoUpgrade = !domainXML.hasNetworkConfig();
        needsManualUpgrade = !domainXML.hasDefaultConfig();

        // Moving and removing files
        setupUpgradeSecurity(callerParameters);
        renameOsgiCache(callerParameters);
    }

    private CommandLine prepareCommandLine(final GFLauncherInfo callerParameters) throws GFLauncherException {
        final boolean useScript = !callerParameters.isVerboseOrWatchdog() && isSurviveWinUserSession();
        final CommandLine cmdLine = new CommandLine(useScript ? CommandFormat.Script : CommandFormat.ProcessBuilder);
        cmdLine.append(javaExe);
        cmdLine.appendModulePath(modulepath);
        if (classpath.length > 0) {
            cmdLine.appendClassPath(classpath);
        }
        addIgnoreNull(cmdLine, domainXMLjavaConfigDebugOptions);

        final String cliStartTime = System.getProperty("WALL_CLOCK_START");
        if (cliStartTime != null) {
            cmdLine.append("-DWALL_CLOCK_START=" + cliStartTime);
        }

        if (getDebugPort() != null) {
            cmdLine.appendSystemOption(DEBUG_MODE_PROPERTY, TRUE.toString());
        }

        jvmOptions.toList().forEach(cmdLine::appendJavaOption);

        GFLauncherNativeHelper nativeHelper = new GFLauncherNativeHelper(callerParameters, domainXMLjavaConfig,
            jvmOptions, domainXMLJavaConfigProfiler);
        cmdLine.appendNativeLibraryPath(nativeHelper.getNativePath());
        cmdLine.append(MAIN_CLASS);

        try {
            addIgnoreNull(cmdLine, callerParameters.getArgsAsList());
        } catch (GFLauncherException gfle) {
            throw gfle;
        } catch (Exception e) {
            throw new GFLauncherException(e);
        }
        return cmdLine;
    }


    private ASenvPropertyReader getAsEnvConfReader(final GFLauncherInfo callerParameters, final boolean fakeLaunch) {
        return fakeLaunch ? new ASenvPropertyReader(callerParameters.getInstallDir()) : new ASenvPropertyReader();
    }

    private Path resolveJavaExecutable() throws GFLauncherException {
        final Path javaFromDomainXml = getJavaExecutable(domainXMLjavaConfig.getJavaHome());
        if (javaFromDomainXml != null) {
            return javaFromDomainXml;
        }
        final Path javaFromAsenv = getJavaExecutable(asenvProps.get(JAVA_ROOT.getPropertyName()));
        if (javaFromAsenv != null) {
            return javaFromAsenv;
        }
        throw new GFLauncherException("nojvm");
    }

    private Path getJavaExecutable(String filename) {
        if (filename == null) {
            return null;
        }

        final File javaDir = new File(filename);
        if (!javaDir.isDirectory()) {
            return null;
        }

        final File javaFile;
        if (GFLauncherUtils.isWindows()) {
            javaFile = new File(javaDir, "bin/java.exe");
        } else {
            javaFile = new File(javaDir, "bin/java");
        }

        if (javaFile.exists()) {
            return javaFile.toPath().toAbsolutePath();
        }
        return null;
    }


    /**
     * Resolves jvm-options against:
     * <ol>
     * <li>itself
     * <li><system-property>'s from domain.xml
     * <li>system properties -- essential there is, e.g. "${path.separator}" in domain.xml
     * <li>asenvProps
     * <li>env variables (if the above contain a switch to prefer env variables, then they go first,
     *     before 1., i.e. add in reverse order to get the precedence right
     * </ol>
     *
     * @param domainXMLSystemProperties
     * @return {@link TokenResolver}
     */
    private TokenResolver createTokenResolver(final  Map<String, String> domainXMLSystemProperties) {
        Map<String, String> all = new HashMap<>();
        all.putAll(asenvProps);
        all.putAll(propertiesToStringMap(System.getProperties()));
        all.putAll(domainXMLSystemProperties);
        all.putAll(jvmOptions.getCombinedMap());
        all.putAll(domainXMLJavaConfigProfiler.getConfig());

        if (!isEnvVarExpansionDisabled(all)) {
            if (isPreferEnvOverProperties(all)) {
                all.putAll(System.getenv());
                replacePropertiesWithEnvVars(jvmOptions.xProps);
                replacePropertiesWithEnvVars(jvmOptions.xxProps);
                replacePropertiesWithEnvVars(jvmOptions.plainProps);
                replacePropertiesWithEnvVars(jvmOptions.longProps);
                replacePropertiesWithEnvVars(jvmOptions.sysProps);
            } else {
                System.getenv().forEach((name, value) -> all.putIfAbsent(name, value));
            }
        }
        return new TokenResolver(all);
    }


    private void resolveAllTokens(TokenResolver resolver) {
        resolver.resolve(jvmOptions.xProps);
        resolver.resolve(jvmOptions.xxProps);
        resolver.resolve(jvmOptions.plainProps);
        resolver.resolve(jvmOptions.longProps);
        resolver.resolve(jvmOptions.sysProps);
        resolver.resolve(domainXMLjavaConfig.getMap());
        resolver.resolve(domainXMLJavaConfigProfiler.getConfig());
        resolver.resolve(domainXMLjavaConfigDebugOptions);
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

    private Profiler createProfiler(MiniXmlParser domainXML) throws MiniXmlParserException {
        // Add JVM options from Profiler *last* so they override config's JVM options
        return new Profiler(domainXML.getProfilerConfig(), domainXML.getProfilerJvmOptions(),
            domainXML.getProfilerSystemProperties());
    }


    private JvmOptions createJvmOptions(GFLauncherInfo callerParameters, Profiler profiler,
        final List<String> domainXmlJvmOptions) throws GFLauncherException {
        final Map<String, String> props = new HashMap<>();
        props.put(INSTALL_ROOT.getSystemPropertyName(), callerParameters.getInstallDir().getAbsolutePath());
        props.put(INSTANCE_ROOT.getSystemPropertyName(), callerParameters.getInstanceRootDir().getAbsolutePath());
        List<String> rawJvmOptions = new ArrayList<>(domainXmlJvmOptions);
        rawJvmOptions.addAll(propsToJvmOptions(props));
        if (profiler.isEnabled()) {
            rawJvmOptions.addAll(profiler.getJvmOptions());
        }
        JvmOptions jvm = new JvmOptions(rawJvmOptions);
        if (callerParameters.isDropInterruptedCommands()) {
            jvm.sysProps.put(DROP_INTERRUPTED_COMMANDS, TRUE.toString());
        }

        return jvm;
    }

    private void setupUpgradeSecurity(GFLauncherInfo callerParameters) throws GFLauncherException {
        // If this is an upgrade and the security manager is on,
        // copy the current server.policy file to the domain
        // before the upgrade.
        if (callerParameters.isUpgrade() && jvmOptions.sysProps.containsKey("java.security.manager")) {

            LOG.log(INFO, "Will copy glassfish/lib/templates/server.policy file to domain before upgrading.");
            Path source = callerParameters.getInstallDir().toPath().resolve(Path.of("lib", "templates", "server.policy"));
            Path target = callerParameters.getConfigDir().toPath().resolve("server.policy");
            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                throw new GFLauncherException(
                    "Could not copy server.policy to domain. You may need to turn off the security manager before upgrading. "
                        + ioe.getMessage(),
                    ioe);
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
    private void renameOsgiCache(GFLauncherInfo callerParameters) throws GFLauncherException {
        if (callerParameters.isUpgrade()) {
            File osgiCacheDir = new File(callerParameters.getDomainRootDir(), "osgi-cache");
            File backupOsgiCacheDir = new File(callerParameters.getDomainRootDir(), "osgi-cache-" + System.currentTimeMillis());
            if (osgiCacheDir.exists() && !backupOsgiCacheDir.exists()) {
                if (FileUtils.renameFile(osgiCacheDir, backupOsgiCacheDir)) {
                    GFLauncherLogger.fine("rename_osgi_cache_succeeded", osgiCacheDir, backupOsgiCacheDir);
                } else {
                    throw new GFLauncherException("rename_osgi_cache_failed", osgiCacheDir, backupOsgiCacheDir);
                }
            }
        }
    }

    private void setupMonitoring(File installDirr) {
        // if the user has a hard-coded "-javaagent" jvm-option that uses OUR jar
        // then we do NOT want to add our own.
        Set<String> plainKeys = jvmOptions.plainProps.keySet();
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
            jvmOptions.plainProps.put(getMonitoringAgentJvmOptionString(installDirr), null);
        } catch (GFLauncherException gfe) {
            // This has been defined as a non-fatal error.
            // Silently ignore it -- but do NOT add it as an option
        }
    }

    private static Boolean isPreferEnvOverProperties(Map<String, String> properties) {
        return Boolean.parseBoolean(properties.get(PREFER_ENV_VARS_OVER_PROPERTIES));
    }

    private static boolean isEnvVarExpansionDisabled(Map<String, String> all) {
        return Boolean.parseBoolean(all.get(DISABLE_ENV_VAR_EXPANSION_PROPERTY));
    }

    private List<String> getDebugOptionsFromDomainXMLJavaConfig(GFLauncherInfo callerParameters) {
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
    private List<String> propsToJvmOptions(Map<String, String> map) {
        List<String> sysProps = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = map.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            String jvm = "-D" + name;

            if (value != null) {
                jvm += "=" + value;
            }

            sysProps.add(jvm);
        }

        return sysProps;
    }

    private void setupLogLevels(boolean verbose) {
        if (verbose) {
            GFLauncherLogger.setConsoleLevel(java.util.logging.Level.INFO);
        } else {
            GFLauncherLogger.setConsoleLevel(java.util.logging.Level.WARNING);
        }
    }


    /** create a list of all the classpath pieces in the right order */
    private File[] createClasspath() {
        List<File> all = new ArrayList<>();
        all.addAll(domainXMLjavaConfig.getPrefixClasspath());
        all.addAll(domainXMLJavaConfigProfiler.getClasspath());
        all.addAll(domainXMLjavaConfig.getSystemClasspath());
        all.addAll(domainXMLjavaConfig.getEnvClasspath());
        all.addAll(domainXMLjavaConfig.getSuffixClasspath());
        return all.toArray(File[]::new);
    }


    /**
     *
     * look for an option of this form:
     * <code>-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9009</code>
     * and extract the suspend and port values.
     *
     */
    private void setupDebugging(List<String> domainXMLjavaConfigDebugOptions) {
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
                        debugPort = null;
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

    private static String getMonitoringAgentJvmOptionString(File installDir) throws GFLauncherException {
        File libMonDir = new File(installDir, LIBMON_NAME);
        File flashlightJarFile = new File(libMonDir, FLASHLIGHT_AGENT_NAME);

        if (flashlightJarFile.isFile()) {
            return "javaagent:" + flashlightJarFile.toPath().toAbsolutePath().normalize();
        }
        GFLauncherLogger.warning(GFLauncherLogger.NO_FLASHLIGHT_AGENT, flashlightJarFile);
        throw new GFLauncherException("no_flashlight_agent", flashlightJarFile);
    }


    private static File resolveAdminFileRealmKeyFile(MiniXmlParser domainXML, TokenResolver resolver) {
        final Map<String, String> realmprops = domainXML.getAdminRealmProperties();
        if (realmprops == null) {
            return null;
        }
        final String classname = realmprops.get("classname");
        final String keyfile = realmprops.get("file");
        // Do not include class reference, we see it in compilation, but not in server's asadmin!
        if (keyfile == null || !"com.sun.enterprise.security.auth.realm.file.FileRealm".equals(classname)) {
            return null;
        }
        return new File(resolver.resolve(keyfile));
    }

    private static Path resolveLogFile(String domainXmlLogFile, GFLauncherInfo callerParameters, TokenResolver resolver)
        throws GFLauncherException {
        final String stringPath = ok(domainXmlLogFile) ? domainXmlLogFile : "logs/server.log";
        final Path logFilePath = Path.of(resolver.resolve(stringPath));
        // this is quite normal. Logging Service will by default return a relative path!
        if (logFilePath.isAbsolute()) {
            return logFilePath.normalize();
        }
        return callerParameters.getInstanceRootDir().toPath().resolve(logFilePath).normalize();
    }

    private static Long resolvePidBeforeRestart() {
        String pid = System.getProperty("AS_RESTART_PREVIOUS_PID");
        if (pid == null) {
            return null;
        }
        try {
            return Long.valueOf(pid);
        } catch (NumberFormatException e) {
            LOG.log(WARNING, "Cannot parse pid {0} required for waiting for the death of the parent process.", pid);
            return null;
        }
    }


    private static boolean isSurviveWinUserSession() {
        String surviveSessionValue = System.getenv("AS_SURVIVE_WIN_USER_SESSION");
        if (surviveSessionValue == null) {
            return isWindows() && isOverSSHSession();
        }
        return Boolean.parseBoolean(surviveSessionValue);
    }

    private static boolean isOverSSHSession() {
        return System.getenv("SSH_CLIENT") != null || System.getenv("SSH_CONNECTION") != null
                || System.getenv("SSH_TTY") != null;
    }

    private static void addIgnoreNull(CommandLine command, Collection<String> values) {
        if (values != null && !values.isEmpty()) {
            values.forEach(command::append);
        }
    }
}
