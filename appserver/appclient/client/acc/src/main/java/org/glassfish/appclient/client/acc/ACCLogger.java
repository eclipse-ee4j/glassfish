/*
 * Copyright (c) 2021, 2024 Contributors to Eclipse Foundation.
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

package org.glassfish.appclient.client.acc;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.glassfish.appclient.client.acc.config.LogService;
import org.glassfish.main.jul.GlassFishLogger;

/**
 * Logger that conforms to the glassfish-acc.xml config file settings for logging while, in some cases, also adjusting
 * other loggers.
 *
 * <p>
 * Historically the logging level specified in the glassfish-acc.xml is used to set the level for all loggers. Beginning
 * with v3, which supports conventional log settings via logging.properties as well, we make sure that each logger's
 * level is at least as detailed as the setting in the config file.
 *
 * <p>
 * Also, in prior versions if the user specified a logging file in the glassfish-acc.xml config file then all
 * pre-existing handlers would be removed, essentially replaced with a single handler to send output to the
 * user-specified file. Beginning with v3 the ACC augments - rather than replaces - existing handlers if the settings in
 * glassfish-acc.xml specify a file.
 *
 * @author tjquinn
 */
public class ACCLogger extends GlassFishLogger {

    private static final String ACC_LOGGER_NAME = "GlassFish.ACC";

    private static final Level DEFAULT_ACC_LOG_LEVEL = Level.INFO;

    public ACCLogger(final LogService logService) throws IOException {
        super(ACC_LOGGER_NAME);
        init(logService);
    }

    private void init(final LogService logService) throws IOException {
        final Level level = chooseLevel(logService);
        final Handler configuredFileHandler = createHandler(logService, level);
        final ResourceBundle rb = ResourceBundle.getBundle(ACCLogger.class.getPackage().getName() + ".LogStrings");

        // Set existing loggers to at least the configured level.
        for (Enumeration<String> names = LogManager.getLogManager().getLoggerNames(); names.hasMoreElements();) {
            final String loggerName = names.nextElement();
            final Logger logger = LogManager.getLogManager().getLogger(loggerName);
            if (logger == null) {
                final String msg = MessageFormat.format(rb.getString("appclient.nullLogger"), loggerName);
                if (level.intValue() <= Level.CONFIG.intValue()) {
                    System.err.println(msg);
                }
            } else {
                reviseLogger(logger, level, configuredFileHandler);
            }
        }
    }

    /**
     * Returns the logging level to use, checking the configured log level and using the default if the configured value is
     * absent or invalid.
     *
     * @param configLevelText configured level name
     * @return log level to use for all logging in the ACC
     */
    private static Level chooseLevel(final LogService logService) {
        Level level = DEFAULT_ACC_LOG_LEVEL;
        if (logService != null) {
            String configLevelText = logService.getLevel();
            if (configLevelText != null && !configLevelText.isEmpty()) {
                try {
                    level = Level.parse(configLevelText);
                } catch (IllegalArgumentException e) {
                    // ignore - use the previously-assigned default - and log it !
                    Logger.getLogger(ACCLogger.class.getName()).warning("Logger.Level = " + configLevelText + "??");
                }
            }
        }
        return level;
    }

    /**
     * Creates a logging handler to send logging to the specified file, using the indicated level.
     *
     * @param filePath path to the log file to which to log
     * @param level level at which to log
     * @return logging Handler if filePath is specified and valid; null otherwise
     */
    private static Handler createHandler(final LogService logService, final Level level) throws IOException {
        Handler handler = null;
        final String filePath = (logService == null) ? null : logService.getFile();
        if (filePath == null || filePath.equals("")) {
            return null;
        }
        handler = new FileHandler(filePath, true /* append */);
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(level);
        File lockFile = new File(filePath + ".lck");
        lockFile.deleteOnExit();
        return handler;
    }

    private static void reviseLogger(final Logger logger, final Level level, final Handler handler) {
        if (!logger.isLoggable(level)) {
            logger.setLevel(level);
        }
        if (handler != null) {
            logger.addHandler(handler);
        }
    }
}
