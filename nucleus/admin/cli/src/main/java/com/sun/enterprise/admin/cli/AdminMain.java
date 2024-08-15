/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.io.PrintStream;
import java.lang.Runtime.Version;
import java.net.ConnectException;
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
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.InvalidCommandException;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.common.util.admin.AsadminInput;
import org.glassfish.main.jul.GlassFishLogManager;
import org.glassfish.main.jul.GlassFishLogManagerInitializer;
import org.glassfish.main.jul.cfg.GlassFishLogManagerConfiguration;
import org.glassfish.main.jul.cfg.GlassFishLoggingConstants;
import org.glassfish.main.jul.cfg.LoggingProperties;
import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

import static com.sun.enterprise.admin.cli.CLIConstants.WALL_CLOCK_START_PROP;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

/**
 * The admin main program (nadmin).
 */
public class AdminMain {
    private static final LoggingProperties LOGGING_CFG;
    static {
        GlassFishLoggingTracer.trace(AdminMain.class, "Preconfiguring logging for asadmin.");
        // The logging is explicitly configured in doMain method
        LOGGING_CFG = new LoggingProperties();
        LOGGING_CFG.setProperty("handlers", GlassFishLoggingConstants.CLASS_HANDLER_BLOCKING);
        if (!GlassFishLogManagerInitializer.tryToSetAsDefault(LOGGING_CFG)) {
            throw new IllegalStateException("GlassFishLogManager is not set as the default LogManager!");
        }
    }
    private static final Environment env = new Environment();
    private static final int SUCCESS = 0;
    private static final int ERROR = 1;
    private static final int CONNECTION_ERROR = 2;
    private static final int INVALID_COMMAND_ERROR = 3;
    private static final int WARNING = 4;
    private static final String ADMIN_CLI_LOGGER = "com.sun.enterprise.admin.cli";

    private static final String[] SYS_PROPERTIES_TO_SET_FROM_ASENV = {
        SystemPropertyConstants.INSTALL_ROOT_PROPERTY,
        SystemPropertyConstants.CONFIG_ROOT_PROPERTY,
        SystemPropertyConstants.PRODUCT_ROOT_PROPERTY
    };
    private static final LocalStringsImpl strings = new LocalStringsImpl(AdminMain.class);

    static {
        Map<String, String> systemProps = new ASenvPropertyReader().getProps();
        for (String prop : SYS_PROPERTIES_TO_SET_FROM_ASENV) {
            String val = systemProps.get(prop);
            if (isNotEmpty(val)) {
                System.setProperty(prop, val);
            }
        }
    }

    private String classPath;
    private String className;
    private String command;
    private ProgramOptions po;
    private CLIContainer cliContainer;
    private Logger logger;

    /**
     * Get the class loader that is used to load local commands.
     *
     * @return a class loader used to load local commands
     */
    private ClassLoader getExtensionClassLoader(final Set<File> extensions) {
        final ClassLoader ecl = AdminMain.class.getClassLoader();
        if (extensions == null || extensions.isEmpty()) {
            return ecl;
        }
        final PrivilegedAction<ClassLoader> action = () -> {
            try {
                return new DirectoryClassLoader(extensions, ecl);
            } catch (final RuntimeException ex) {
                // any failure here is fatal
                logger.info(strings.get("ExtDirFailed", ex));
            }
            return ecl;
        };
        return AccessController.doPrivileged(action);
    }

    /** Get set of JAR files that is used to locate local commands (CLICommand).
     * Results can contain JAR files or directories where all JAR files are
     * used. It must return all JARs or directories
     * with acceptable CLICommands excluding admin-cli.jar.
     * Default implementation returns INSTALL_ROOT_PROPERTY/lib/asadmin
     *
     * @return set of JAR files or directories with JAR files
     */
    protected Set<File> getExtensions() {
        final Set<File> result = new HashSet<>();
        final File inst = new File(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
        final File ext = new File(new File(inst, "lib"), "asadmin");
        if (ext.exists() && ext.isDirectory()) {
            result.add(ext);
        } else {
            if (logger.isLoggable(FINER)) {
                logger.finer(strings.get("ExtDirMissing", ext));
            }
        }
        result.add(new File(new File(inst, "modules"), "admin-cli.jar"));
        return result;
    }


    protected String getCommandName() {
        return "nadmin";
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
            @SuppressWarnings("resource")
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

    public static void main(String[] args) {
        AdminMain adminMain = new AdminMain();
        int code = adminMain.doMain(args);
        System.exit(code);
    }

    protected int doMain(String[] args) {
        Version version = Runtime.version();
        if (version.feature() < 21) {
            System.err.println(strings.get("OldJdk", 21, version));
            return ERROR;
        }

        System.setProperty(WALL_CLOCK_START_PROP, Instant.now().toString());
        GlassFishLogManager.getLogManager().reconfigure(new GlassFishLogManagerConfiguration(LOGGING_CFG),
            this::reconfigureLogging, null);

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
        className = AdminMain.class.getName();

        if (logger.isLoggable(FINER)) {
            logger.log(FINER, "Classpath: {0}\nArguments: {1}", new Object[] {classPath, Arrays.toString(args)});
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
                argv = operands.toArray(new String[operands.size()]);
            } else {
                po = new ProgramOptions(env);
            }
            po.toEnvironment(env);
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
            logger.severe(cve.getMessage());
            if (cmd == null) // error parsing program options
            {
                printUsage();
            } else {
                logger.severe(cmd.getUsage());
            }
            return ERROR;
        } catch (InvalidCommandException ice) {
            // find closest match with local or remote commands
            logger.severe(ice.getMessage());
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
            if (ce.getCause() instanceof ConnectException) {
                // find closest match with local commands
                logger.severe(ce.getMessage());
                try {
                    CLIUtil.displayClosestMatch(command,
                        CLIUtil.getLocalCommands(cliContainer),
                        strings.get("ClosestMatchedLocalCommands"), logger);
                } catch (InvalidCommandException e) {
                    logger.info(strings.get("InvalidRemoteCommand", command));
                }
            } else {
                logger.severe(ce.getMessage());
            }
            return ERROR;
        }
    }

    private void reconfigureLogging() {
        GlassFishLoggingTracer.trace(AdminMain.class, "Configuring logging for asadmin.");
        boolean trace = env.trace();
        boolean debug = env.debug();

        // Use a logger associated with the top-most package that we expect all
        // admin commands to share. Only this logger and its children obey the
        // conventions that map terse=false to the INFO level and terse=true to
        // the FINE level.
        logger = Logger.getLogger(ADMIN_CLI_LOGGER);
        if (trace) {
            logger.setLevel(FINEST);
        } else if (debug) {
            logger.setLevel(FINER);
        } else {
            logger.setLevel(FINE);
        }
        logger.setUseParentHandlers(false);
        Formatter formatter = env.getLogFormatter();
        Handler cliHandler = new CLILoggerHandler(formatter == null ? new CLILoggerFormatter() : formatter);
        cliHandler.setLevel(logger.getLevel());
        logger.addHandler(cliHandler);

        // make sure the root logger uses our handler as well
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setUseParentHandlers(false);
        if (trace) {
            rootLogger.setLevel(logger.getLevel());
        }
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
            handler.close();
        }
        rootLogger.addHandler(cliHandler);
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

    /**
     * Print usage message for the admin command. XXX - should be derived from
     * ProgramOptions.
     */
    private void printUsage() {
        logger.severe(strings.get("Usage.full", getCommandName()));
    }

    private static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
