/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.glassfish.main.jul.formatter.ODLLogFormatter;
import org.glassfish.main.jul.handler.SimpleLogHandler;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Special logger for launchers used to start server in separate JVMs.
 * After that the launcher instance dies, but first it must close all ouputs, specially log files.
 *
 * @author bnevins
 */
public class GFLauncherLogger {

    // The resourceBundle name to be used for the module's log messages
    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "com.sun.enterprise.admin.launcher.LogMessages";

    @LoggerInfo(subsystem = "Launcher", description = "Launcher Logger", publish = true)
    public static final String LOGGER_NAME = "jakarta.enterprise.launcher";

    /**
     * Logger just for the launcher that only uses the standard output and doesn't send logs to
     * any other handler.
     */
    private final static Logger LOG;
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncherLogger.class);
    private static final Handler HANDLER_STDOUT;
    private static FileHandler logfileHandler;

    static {
        LOG = Logger.getLogger(LOGGER_NAME, SHARED_LOGMESSAGE_RESOURCE);
        LOG.setLevel(INFO);
        LOG.setUseParentHandlers(false);
        HANDLER_STDOUT = new SimpleLogHandler(System.out);
        HANDLER_STDOUT.setFormatter(new SimpleFormatter());
        LOG.addHandler(HANDLER_STDOUT);
    }

    @LogMessageInfo(message = "Single and double quote characters are not allowed in the CLASSPATH environmental variable.  "
            + "They were stripped out for you.\nBefore: {0}\nAfter: {1}", comment = "CLASSPATH is illegal.", cause = "see message", action = "see message", level = "SEVERE")
    public static final String NO_QUOTES_ALLOWED = "NCLS-GFLAUNCHER-00001";

    @LogMessageInfo(message = "Error Launching: {0}", comment = "Launcher Error", cause = "see message", action = "fix the CLASSPATH", level = "SEVERE")
    public static final String LAUNCH_FAILURE = "NCLS-GFLAUNCHER-00002";

    @LogMessageInfo(message = "Could not locate the flashlight agent here: {0}", comment = "catastrophic error", cause = "see message", action = "Find the agent file.", level = "SEVERE")
    public static final String NO_FLASHLIGHT_AGENT = "NCLS-GFLAUNCHER-00003";

    @LogMessageInfo(message = "JVM invocation command line:\n{0}", comment = "Routine Information", cause = "NA", action = "NA", level = "INFO")
    public static final String COMMAND_LINE = "NCLS-GFLAUNCHER-00005";

    private GFLauncherLogger() {
    }

    // use LocalStrings for < INFO level...

    public static void warning(String msg, Object... objs) {
        LOG.log(WARNING, msg, objs);
    }

    public static void info(String msg, Object... objs) {
        LOG.log(INFO, msg, objs);
    }

    public static void severe(String msg, Object... objs) {
        LOG.log(SEVERE, msg, objs);
    }

    public static void fine(String msg, Object... objs) {
        if (LOG.isLoggable(FINE)) {
            LOG.fine(strings.get(msg, objs));
        }
    }

    static void setConsoleLevel(Level level) {
        HANDLER_STDOUT.setLevel(level);
    }


    /**
     * IMPORTANT! The server's logfile is added to the *local* LOG.
     * The files are kept open by the handler, so don't forget to close the handler
     * calling {@link #removeLogFileHandler()}.
     *
     * @param logFile The logfile
     * @throws GFLauncherException if the info object has not been setup
     */
    static void addLogFileHandler(Path logFile) throws GFLauncherException {
        try {
            if (logFile == null || logfileHandler != null) {
                return;
            }
            Files.createDirectories(logFile.getParent());
            logfileHandler = new FileHandler(logFile.toString(), true);
            logfileHandler.setFormatter(new ODLLogFormatter());
            logfileHandler.setLevel(INFO);
            LOG.addHandler(logfileHandler);
        } catch (IOException e) {
            // should be seen in verbose and watchdog modes for debugging
            e.printStackTrace();
        }

    }

    static void removeLogFileHandler() {
        FileHandler handler = logfileHandler;
        logfileHandler = null;
        if (handler != null) {
            LOG.removeHandler(handler);
            handler.close();
        }
    }

}
