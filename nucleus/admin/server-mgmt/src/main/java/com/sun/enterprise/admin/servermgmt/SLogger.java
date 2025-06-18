/*
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

package com.sun.enterprise.admin.servermgmt;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author Byron Nevins
 */
public class SLogger {

    public static Logger getLogger() {
        return logger;
    }

    private SLogger() {
    }

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "com.sun.enterprise.admin.servermgmt.LogMessages";
    @LoggerInfo(subsystem = "ServerManagement", description = "Server Management Logger", publish = true)
    public static final String LOGGER_NAME = "jakarta.enterprise.servermgmt";
    private final static Logger logger = Logger.getLogger(LOGGER_NAME, SHARED_LOGMESSAGE_RESOURCE);
    // these messages are historical.  We've transitioned to this latest Logging API
    @LogMessageInfo(message = "Caught an Exception: {0}", comment = "Unhandled Exception", cause = "see Exception message", action = "see Exception message", level = "SEVERE")
    public static final String UNHANDLED_EXCEPTION = "NCLS-SRVRMGMT-00000";
    @LogMessageInfo(message = "Renaming {0} to {1}", comment = "No error",
            //cause = "No error",
            //action = "delete the file manually",
            level = "INFO")
    public static final String RENAME_CERT_FILE = "NCLS-SRVRMGMT-00002";
    @LogMessageInfo(message = "Failed to rename {0} to {1}", comment = "File rename error", cause = "see message", action = "Check the file system", level = "SEVERE")
    public static final String BAD_RENAME_CERT_FILE = "NCLS-SRVRMGMT-00003";
    @LogMessageInfo(message = "Failure while upgrading jvm-options from V2 to V3", comment = "V2 to V3 Upgrade Failure", cause = "see message", action = "Check documentation.", level = "SEVERE")
    public static final String JVM_OPTION_UPGRADE_FAILURE = "NCLS-SRVRMGMT-00004";
    @LogMessageInfo(message = "JVM Monitoring", comment = "Just a title", level = "INFO")
    public static final String MONITOR_TITLE = "NCLS-SRVRMGMT-00005";
    @LogMessageInfo(message = "UpTime(ms)", comment = "Just a title", level = "INFO")
    public static final String MONITOR_UPTIME_TITLE = "NCLS-SRVRMGMT-00006";
    @LogMessageInfo(message = "Heap and NonHeap Memory(bytes)", comment = "Just a title", level = "INFO")
    public static final String MONITOR_MEMORY_TITLE = "NCLS-SRVRMGMT-00007";
    @LogMessageInfo(message = "Failure while upgrading log-service. Could not create logging.properties file. ", comment = "see message", cause = "see message", action = "Check documentation.", level = "SEVERE")
    public static final String FAIL_CREATE_LOG_PROPS = "NCLS-SRVRMGMT-00008";
    @LogMessageInfo(message = "Failure while upgrading log-service. Could not update logging.properties file. ", comment = "see message", cause = "see message", action = "Check documentation.", level = "SEVERE")
    public static final String FAIL_UPDATE_LOG_PROPS = "NCLS-SRVRMGMT-00009";
    @LogMessageInfo(message = "Failure while upgrading log-service ", comment = "see message", cause = "see message", action = "Check documentation.", level = "SEVERE")
    public static final String FAIL_UPGRADE_LOG_SERVICE = "NCLS-SRVRMGMT-00010";

    @LogMessageInfo(message = "Could not create directory {0}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "SEVERE")
    public static final String DIR_CREATION_ERROR = "NCLS-SRVRMGMT-00011";

    @LogMessageInfo(message = "Could not create domain info XML file {0}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String DOMAIN_INFO_CREATION_ERROR = "NCLS-SRVRMGMT-00012";

    @LogMessageInfo(message = "Missing file : {0}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String MISSING_FILE = "NCLS-SRVRMGMT-00013";

    @LogMessageInfo(message = "Default port {1} for {0} is in use. Using {2}", comment = "See message.", level = "INFO")
    public static final String DEFAULT_PORT_IN_USE = "NCLS-SRVRMGMT-00014";

    @LogMessageInfo(message = "Port for {0} is not specified. Using {1}", comment = "See message.", level = "INFO")
    public static final String PORT_NOT_SPECIFIED = "NCLS-SRVRMGMT-00015";

    @LogMessageInfo(message = "Invalid Port for {0}, should be between 1 and 65535. Using {1}", comment = "See message.", level = "INFO")
    public static final String INVALID_PORT_RANGE = "NCLS-SRVRMGMT-00016";

    @LogMessageInfo(message = "Port {1} for {0} is in use. Using {2}", comment = "See message.", level = "INFO")
    public static final String PORT_IN_USE = "NCLS-SRVRMGMT-00017";

    @LogMessageInfo(message = "Using default port {1} for {0}.", comment = "See message.", level = "INFO")
    public static final String USING_DEFAULT_PORT = "NCLS-SRVRMGMT-00018";

    @LogMessageInfo(message = "Using port {1} for {0}.", comment = "See message.", level = "INFO")
    public static final String USING_PORT = "NCLS-SRVRMGMT-00019";

    @LogMessageInfo(message = "On Unix platforms, port numbers below 1024 may require special privileges.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String PORT_PRIV_MSG = "NCLS-SRVRMGMT-00020";

    @LogMessageInfo(message = "Failed to update jar {0} with the substitutable files", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String ERR_ARCHIVE_SUBSTITUTION = "NCLS-SRVRMGMT-00021";

    @LogMessageInfo(message = "File {0} not present inside archive {1}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String INVALID_ARCHIVE_ENTRY = "NCLS-SRVRMGMT-00022";

    @LogMessageInfo(message = "Error occurred while closing the stream for file {0}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "SEVERE")
    public static final String ERR_CLOSING_STREAM = "NCLS-SRVRMGMT-00023";

    @LogMessageInfo(message = "Could not rename temporary jar {0} file to {1}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "SEVERE")
    public static final String ERR_RENAMING_JAR = "NCLS-SRVRMGMT-00024";

    @LogMessageInfo(message = "Could not locate file or resource {0}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String INVALID_FILE_LOCATION = "NCLS-SRVRMGMT-00025";

    @LogMessageInfo(message = "No processing defined for {0} mode", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String NO_PROCESSOR_DEFINED = "NCLS-SRVRMGMT-00026";

    @LogMessageInfo(message = "Component {0} is not present.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "INFO")
    public static final String MISSING_COMPONENT = "NCLS-SRVRMGMT-00027";

    @LogMessageInfo(message = "Group {0} is not present.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String MISSING_GROUP = "NCLS-SRVRMGMT-00028";

    @LogMessageInfo(message = "Change-Pair {0} referred by group {1} is not defined.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "INFO")
    public static final String MISSING_CHANGE_PAIR = "NCLS-SRVRMGMT-00029";

    @LogMessageInfo(message = "Invalid Mode Type {0}.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String INVALID_MODE_TYPE = "NCLS-SRVRMGMT-00030";

    @LogMessageInfo(message = "Found an empty <change-pair/>.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "INFO")
    public static final String EMPTY_CHANGE_PAIR = "NCLS-SRVRMGMT-00031";

    @LogMessageInfo(message = "IO Error occurred while retrieving substitutable entries from archive {0}.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "INFO")
    public static final String ERR_RETRIEVING_SUBS_ENTRIES = "NCLS-SRVRMGMT-00032";

    @LogMessageInfo(message = "In-memory string substitution file size is not defined.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "INFO")
    public static final String MISSING_MEMORY_FILE_SIZE = "NCLS-SRVRMGMT-00033";

    @LogMessageInfo(message = "Key already exist in tree, Current Value : {0} New Value : {1}.", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "INFO")
    public static final String CHANGE_IN_VALUE = "NCLS-SRVRMGMT-00034";

    @LogMessageInfo(message = "Parent node: {0} contains child node: {1} whose key starts with same character as the key of given node: {2}", comment = "See message.", cause = "See message.", action = "Check documentation.", level = "WARNING")
    public static final String CHILD_NODE_EXISTS = "NCLS-SRVRMGMT-00035";

}
