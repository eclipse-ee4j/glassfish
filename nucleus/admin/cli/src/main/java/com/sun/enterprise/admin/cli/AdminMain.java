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

import com.sun.enterprise.admin.remote.reader.ProprietaryReaderFactory;
import com.sun.enterprise.admin.remote.writer.ProprietaryWriterFactory;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.PrintStream;
import java.lang.Runtime.Version;
import java.net.ConnectException;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.InvalidCommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.common.util.admin.AsadminInput;
import org.glassfish.embeddable.GlassFishVariable;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.main.jul.GlassFishLogManager;
import org.glassfish.main.jul.GlassFishLogManagerInitializer;
import org.glassfish.main.jul.cfg.GlassFishLogManagerConfiguration;
import org.glassfish.main.jul.cfg.LoggingProperties;
import org.glassfish.main.jul.formatter.GlassFishLogFormatter;
import org.glassfish.main.jul.handler.BlockingExternallyManagedLogHandler;

import static com.sun.enterprise.admin.cli.CLIConstants.WALL_CLOCK_START_PROP;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

/**
 * The admin main program (nadmin).
 */
public class AdminMain {
    private static final Environment env = new Environment();
    private static final int SUCCESS = 0;
    private static final int ERROR = 1;
    private static final int CONNECTION_ERROR = 2;
    private static final int INVALID_COMMAND_ERROR = 3;
    private static final int WARNING = 4;
    private static final String ADMIN_CLI_LOGGER = "com.sun.enterprise.admin.cli";

    private static final String[] SYS_PROPERTIES_TO_SET_FROM_ASENV = {
        GlassFishVariable.INSTALL_ROOT.getSystemPropertyName(),
        SystemPropertyConstants.CONFIG_ROOT_PROPERTY,
        SystemPropertyConstants.PRODUCT_ROOT_PROPERTY,
    };
    private static final LocalStringsImpl strings = new LocalStringsImpl(AdminMain.class);

    static {
        GlassFishLogManagerInitializer.tryToSetAsDefault();
        Map<String, String> systemProps = new ASenvPropertyReader().getProps();
        for (String prop : SYS_PROPERTIES_TO_SET_FROM_ASENV) {
            String val = systemProps.get(prop);
            if (isNotEmpty(val)) {
                System.setProperty(prop, val);
            }
        }
    }

    private final Path installRoot;
    private String modulePath;
    private String classPath;
    private String className;
    private String command;
    private ProgramOptions po;
    private CLIContainer cliContainer;
    private Logger logger;


    public AdminMain() {
        this.installRoot = GFLauncherUtils.getInstallDir().toPath();
    }

    protected Path getInstallRoot() {
        return this.installRoot;
    }

    /**
     * Get the class loader that is used to load local commands.
     *
     * @return a class loader used to load local commands
     */
    private ClassLoader getExtensionClassLoader(final Set<File> extensions) {
        final ClassLoader ecl = getClass().getClassLoader();
        if (extensions == null || extensions.isEmpty()) {
            return ecl;
        }
        final PrivilegedAction<ClassLoader> action = () -> {
            try {
                return new DirectoryClassLoader(extensions, ecl);
            } catch (final RuntimeException ex) {
                throw new Error(strings.get("ExtDirFailed", extensions), ex);
            }
        };
        return AccessController.doPrivileged(action);
    }

    /**
     * Get set of JAR files that is used to locate local commands (CLICommand).
     * Results can contain JAR files or directories where all JAR files are
     * used. It must return all JARs or directories
     * with acceptable CLICommands excluding admin-cli.jar.
     * Default implementation returns INSTALL_ROOT_PROPERTY/lib/asadmin
     *
     * @return set of JAR files or directories with JAR files
     */
    protected Set<File> getExtensions() {
        final Set<File> locations = new HashSet<>();
        final File ext = installRoot.resolve(Path.of("lib", "asadmin")).toFile();
        if (ext.isDirectory()) {
            locations.add(ext);
        } else {
            throw new Error(strings.get("ExtDirMissing", ext));
        }
        final String envClasspath = System.getenv("ASADMIN_CLASSPATH");
        if (envClasspath == null) {
            System.err.println(
                "The ASADMIN_CLASSPATH environment variable is not set. Adding whole modules directory as a default.");
            final File modules = getInstallRoot().resolve("modules").toFile();
            if (modules.isDirectory()) {
                locations.add(modules);
            }
        } else {
            for (String path : envClasspath.split(File.pathSeparator)) {
                File file = new File(path);
                // nucleus doesn't contain some files, ie. backup.jar
                if (file.exists()) {
                    locations.add(file);
                }
            }
        }
        return locations;
    }


    protected String getCommandName() {
        return "nadmin";
    }

    public static void main(String[] args) {
        AdminMain adminMain = new AdminMain();
        int code = adminMain.doMain(args);
        System.exit(code);
    }

    protected final int doMain(String[] args) {
        Version version = Runtime.version();
        if (version.feature() < 11) {
            System.err.println(strings.get("OldJdk", 11, version));
            return ERROR;
        }

        System.setProperty(WALL_CLOCK_START_PROP, Instant.now().toString());
        final LoggingProperties logging = new LoggingProperties();
        logging.setProperty("handlers", BlockingExternallyManagedLogHandler.class.getName());
        if (isClassAndMethodDetectionRequired()) {
            logging.setProperty("org.glassfish.main.jul.classAndMethodDetection.enabled", "true");
        }
        GlassFishLogManager.getLogManager().reconfigure(new GlassFishLogManagerConfiguration(logging),
            this::configureLogging, null);

        // Set the thread's context class loader so that everyone can load from our extension directory.
        Set<File> extensions = getExtensions();
        ClassLoader ecl = getExtensionClassLoader(extensions);
        Thread.currentThread().setContextClassLoader(ecl);

         // It helps a little with CLI performance
        Thread thread = new Thread(() -> {
            ProprietaryReaderFactory.getReader(Class.class, "not/defined");
            ProprietaryWriterFactory.getWriter(Class.class);
        });
        thread.setDaemon(true);
        thread.start();

        cliContainer = new CLIContainer(ecl, extensions, logger);

        classPath = SmartFile.sanitizePaths(System.getProperty("java.class.path"));
        modulePath = SmartFile.sanitize(System.getProperty("jdk.module.path"));
        className = AdminMain.class.getName();

        if (logger.isLoggable(FINEST)) {
            logger.log(FINEST, getClass() + "\nModulePath: {0}\nClasspath: {1}\nExtensions: {2}\nArguments: {3}",
                new Object[] {modulePath, classPath, ecl, Arrays.toString(args)});
        }

        if (args.length == 0) {
            // Special case: no arguments is the same as "multimode".
            args = new String[] {"multimode"};
        } else if (args[0].equals("-V")) {
            // Special case: -V argument is the same as "version".
            args = new String[] {"version"};
        }

        command = args[0];
        int exitCode = executeCommand(args);

        switch (exitCode) {
            case SUCCESS:
                if (!po.isTerse() && logger.isLoggable(FINE)) {
                    String key = po.isDetachedCommand() ? "CommandSuccessfulStarted" : "CommandSuccessful";
                    logger.fine(strings.get(key, command));
                }
                break;

            case WARNING:
                if (logger.isLoggable(FINE)) {
                    logger.fine(strings.get("CommandSuccessfulWithWarnings", command));
                }
                exitCode = SUCCESS;
                break;

            case ERROR:
            case INVALID_COMMAND_ERROR:
            case CONNECTION_ERROR:
            default:
                if (logger.isLoggable(FINE)) {
                    logger.fine(strings.get("CommandUnSuccessful", command));
                }
                break;
        }
        CLIUtil.writeCommandToDebugLog(getCommandName(), env, args, exitCode);
        return exitCode;
    }


    private boolean isClassAndMethodDetectionRequired() {
        Formatter formatter = env.getLogFormatter();
        if (formatter instanceof GlassFishLogFormatter) {
            GlassFishLogFormatter gfFormatter = (GlassFishLogFormatter) formatter;
            return gfFormatter.isPrintSource();
        }
        return false;
    }

    public int executeCommand(String[] argv) {
        CLICommand cmd = null;
        try {
            // if the first argument is an option, we're using the new form
            if (argv.length > 0 && argv[0].startsWith("-")) {
                // Parse all the admin options, stopping at the first
                // non-option, which is the command name.
                Parser rcp = new Parser(argv, 0, ProgramOptions.getValidOptions(), false);
                ParameterMap params = rcp.getOptions();
                po = new ProgramOptions(params, env);
                readAndMergeOptionsFromAuxInput(po);
                List<String> operands = rcp.getOperands();
                argv = operands.toArray(String[]::new);
            } else {
                po = new ProgramOptions(env);
            }
            po.toEnvironment(env);
            po.setModulePath(modulePath);
            po.setClassPath(classPath);
            po.setClassName(className);
            po.setCommandName(getCommandName());
            if (argv.length == 0) {
                if (po.isHelp()) {
                    argv = new String[]{"help"};
                } else {
                    argv = new String[]{"multimode"};
                }
            }
            command = argv[0];

            cliContainer.setEnvironment(env);
            cliContainer.setProgramOptions(po);
            cmd = CLICommand.getCommand(cliContainer, command);
            return cmd.execute(argv);
        } catch (CommandValidationException cve) {
            logError(cve);
            if (cmd == null) {
                // error parsing program options
                printUsage();
            } else {
                logger.severe(cmd.getUsage());

            }
            return ERROR;
        } catch (InvalidCommandException ice) {
            logError(ice);
            // find closest match with local or remote commands
            try {
                po.setEcho(false);
                CLIUtil.displayClosestMatch(command,
                        CLIUtil.getAllCommands(cliContainer, po, env),
                        strings.get("ClosestMatchedLocalAndRemoteCommands"), logger);
            } catch (InvalidCommandException e) {
                // not a big deal if we cannot help
            }
            return ERROR;
        } catch (CommandException ce) {
            logError(ce);
            if (ce.getCause() instanceof ConnectException) {
                // find closest match with local commands
                try {
                    CLIUtil.displayClosestMatch(command,
                        CLIUtil.getLocalCommands(cliContainer),
                        strings.get("ClosestMatchedLocalCommands"), logger);
                } catch (InvalidCommandException e) {
                    logger.info(strings.get("InvalidRemoteCommand", command));
                }
            }
            return ERROR;
        }
    }

    private void configureLogging() {
        boolean trace = env.trace();
        boolean debug = env.debug();

        // Use a logger associated with the top-most package that we expect all
        // admin commands to share. Only this logger and its children obey the
        // conventions that map terse=false to the INFO level and terse=true to
        // the FINE level.
        final Logger cliLogger = Logger.getLogger(ADMIN_CLI_LOGGER);
        if (trace) {
            cliLogger.setLevel(FINEST);
        } else if (debug) {
            cliLogger.setLevel(FINER);
        } else {
            cliLogger.setLevel(FINE);
        }
        cliLogger.setUseParentHandlers(false);
        Formatter formatter = env.getLogFormatter();
        Handler cliHandler = new CLILoggerHandler(formatter == null ? new CLILoggerFormatter() : formatter);
        cliHandler.setLevel(cliLogger.getLevel());
        cliLogger.addHandler(cliHandler);

        // make sure the root logger uses our handler as well
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setUseParentHandlers(false);
        if (trace) {
            rootLogger.setLevel(cliLogger.getLevel());
        }
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
            handler.close();
        }
        rootLogger.addHandler(cliHandler);
        this.logger = cliLogger;
    }

    private void logError(Exception e) {
        if (env.trace() || env.debug()) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } else {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Print usage message for the admin command. XXX - should be derived from
     * ProgramOptions.
     */
    private void printUsage() {
        logger.severe(strings.get("Usage.full", getCommandName()));
    }

    private static void readAndMergeOptionsFromAuxInput(final ProgramOptions progOpts) {
        final String auxInput = progOpts.getAuxInput();
        if (auxInput == null || auxInput.isEmpty()) {
            return;
        }
        // We will place the options passed via the aux. input on the command
        // line and we do not want to repeat the read from stdin again, so
        // remove the aux input setting.
        progOpts.setAuxInput(null);
        final ParameterMap newParamMap = new ParameterMap();
        try {
            final AsadminInput.InputReader reader = AsadminInput.reader(auxInput);
            final Properties newOptions = reader.settings().get("option");
            for (String propName : newOptions.stringPropertyNames()) {
                newParamMap.add(propName, newOptions.getProperty(propName));
            }
            progOpts.updateOptions(newParamMap);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }


    /**
     * A ConsoleHandler that prints all non-SEVERE messages to System.out and
     * all SEVERE messages to System.err.
     */
    private static class CLILoggerHandler extends ConsoleHandler {

        private CLILoggerHandler(final Formatter formatter) {
            setFormatter(formatter);
        }

        @Override
        public void publish(LogRecord logRecord) {
            if (!isLoggable(logRecord)) {
                return;
            }
            final PrintStream ps = logRecord.getLevel() == SEVERE ? System.err : System.out;
            ps.print(getFormatter().format(logRecord));
            ps.flush();
        }
    }

    private static class CLILoggerFormatter extends SimpleFormatter {
        private static final boolean TRACE = env.trace();

        @Override
        public synchronized String format(LogRecord record) {
            // this formatter adds blank lines between records
            if (record.getThrown() == null) {
                return formatMessage(record) + System.lineSeparator();
            }
            // Some messages use exception as a parameter.
            // If we don't print stacktraces, the cause would be lost.
            final Object[] parameters;
            if (record.getParameters() == null) {
                parameters = new Object[] {record.getThrown()};
            } else {
                parameters = new Object[record.getParameters().length + 1];
                System.arraycopy(record.getParameters(), 0, parameters, 0, parameters.length - 1);
                parameters[parameters.length - 1] = record.getThrown();
            }
            record.setParameters(parameters);
            if (TRACE) {
                return super.format(record) + System.lineSeparator();
            }
            return formatMessage(record) + " " + record.getThrown().getLocalizedMessage() + System.lineSeparator();
        }
    }
}
