/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.server.logging;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

public class LogFacade {

    @LoggerInfo(subsystem = "Logging", description="Main logger for core logging component.")
    public static final String LOGGING_LOGGER_NAME = "jakarta.enterprise.logging";

    @LoggerInfo(subsystem = "Logging", description="Stdout logger.", publish=false)
    private static final String STDOUT_LOGGER_NAME = "jakarta.enterprise.logging.stdout";

    @LoggerInfo(subsystem = "Logging", description="Stderr logger.", publish=false)
    private static final String STDERR_LOGGER_NAME = "jakarta.enterprise.logging.stderr";

    @LogMessagesResourceBundle()
    public static final String LOGGING_RB_NAME = "com.sun.enterprise.server.logging.LogMessages";

    public static final Logger LOGGING_LOGGER =
        Logger.getLogger(LOGGING_LOGGER_NAME, LOGGING_RB_NAME);

    public static final Logger STDOUT_LOGGER = Logger.getLogger(STDOUT_LOGGER_NAME);

    public static final Logger STDERR_LOGGER = Logger.getLogger(STDERR_LOGGER_NAME);

    @LogMessageInfo(message = "Cannot read logging configuration file.", level="SEVERE",
            cause="An exception has occurred while reading the logging configuration file.",
            action="Take appropriate action based on the exception message.")
    public static final String ERROR_READING_CONF_FILE = "NCLS-LOGGING-00001";

    @LogMessageInfo(message = "Could not apply the logging configuration changes.", level="SEVERE",
            cause="There was an exception thrown while applying the logging configuration changes.",
            action="Take appropriate action based on the exception message.")
    public static final String ERROR_APPLYING_CONF = "NCLS-LOGGING-00002";

    @LogMessageInfo(message = "Updated logger levels successfully.", level="INFO")
    public static final String UPDATED_LOG_LEVELS = "NCLS-LOGGING-00003";

    @LogMessageInfo(
        message = "The logging configuration file {0} has been deleted."
        + " The server will wait until the file reappears.",
        cause="The file was deleted.",
        action="Create the file again using the Admin Console or asadmin command.",
        level="SEVERE")
    public static final String CONF_FILE_DELETED = "NCLS-LOGGING-00004";

    @LogMessageInfo(message = "The logging configuration file {0} has reappeared.", level="INFO")
    public static final String CONF_FILE_REAPPEARED = "NCLS-LOGGING-00004-1";

    @LogMessageInfo(message = "Error executing query to fetch log records.", level="SEVERE",
            cause="There was an exception thrown while executing log query.",
            action="Take appropriate action based on the exception message.")
    public static final String ERROR_EXECUTING_LOG_QUERY = "NCLS-LOGGING-00005";

    @LogMessageInfo(message = "The syslog handler could not be initialized.", level="SEVERE",
            cause="There was an exception thrown while initializing the syslog handler.",
            action="Take appropriate action based on the exception message.")
    public static final String ERROR_INIT_SYSLOG = "NCLS-LOGGING-00006";

    @LogMessageInfo(message = "There was an error sending a log message to syslog.", level="SEVERE",
            cause="There was an exception thrown while sending a log message to the syslog.",
            action="Take appropriate action based on the exception message.")
    public static final String ERROR_SENDING_SYSLOG_MSG = "NCLS-LOGGING-00007";

    @LogMessageInfo(message = "The log file {0} for the instance does not exist.", level="WARNING")
    public static final String INSTANCE_LOG_FILE_NOT_FOUND = "NCLS-LOGGING-00008";

    @LogMessageInfo(message = "Running GlassFish Version: {0}", level="INFO")
    public static final String GF_VERSION_INFO = "NCLS-LOGGING-00009";

    @LogMessageInfo(message = "Server log file is using Formatter class: {0}", level="INFO")
    public static final String LOG_FORMATTER_INFO = "NCLS-LOGGING-00010";

    @LogMessageInfo(message = "Failed to parse the date: {0}", level="WARNING")
    public static final String DATE_PARSING_FAILED = "NCLS-LOGGING-00011";

    @LogMessageInfo(message = "An invalid value {0} has been specified for the {1} attribute in the logging configuration.", level="WARNING")
    public static final String INVALID_ATTRIBUTE_VALUE = "NCLS-LOGGING-00012";

    @LogMessageInfo(message = "The formatter class {0} could not be instantiated.", level="WARNING")
    public static final String INVALID_FORMATTER_CLASS_NAME = "NCLS-LOGGING-00013";

}
