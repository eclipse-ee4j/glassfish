/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.util;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Logger information for the admin-util module.
 *
 * @author Tom Mueller
 */
/* Module private */
public class AdminLoggerInfo {

    public static final String LOGMSG_PREFIX = "NCLS-ADMIN";

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "com.sun.enterprise.admin.util.LogMessages";

    @LoggerInfo(subsystem = "ADMIN", description = "Administration Services", publish = true)
    public static final String ADMIN_LOGGER = "jakarta.enterprise.system.tools.admin";

    private static final Logger adminLogger = Logger.getLogger(ADMIN_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    public static Logger getLogger() {
        return adminLogger;
    }

    @LogMessageInfo(
            message = "Could not find state of instance registered in the state service",
            cause = "Unknown",
            action = "Unknown",
            level = "SEVERE")
    static final String stateNotFound = LOGMSG_PREFIX + "-00001";

    @LogMessageInfo(
            message = "Error during command replication: {0}",
            cause = "Unknown",
            action = "Unknown",
            level = "SEVERE")
    static final String replicationError = LOGMSG_PREFIX + "-00002";

    @LogMessageInfo(
            message = "Unable to read instance state file {0}, recreating",
            level = "FINE")
    final static String mISScannotread = LOGMSG_PREFIX + "-00003";

    @LogMessageInfo(
            message = "Unable to create instance state file: {0}, exception: {1}",
            cause = "The instance state file is missing and the system is trying to recreated it but and exception was raised.",
            action = "Check the server logs.",
            level = "SEVERE")
    final static String mISScannotcreate = LOGMSG_PREFIX + "-00004";

    @LogMessageInfo(
            message = "Error while adding new server state to instance state: {0}",
            cause = "An attempt to add a new server to the instance state file failed.",
            action = "Check the server logs.",
            level = "SEVERE")
    final static String mISSaddstateerror = LOGMSG_PREFIX + "-00005";

    @LogMessageInfo(
            message = "Error while adding failed command to instance state: {0}",
            cause = "An attempt to add a failed command to the instance state file failed.",
            action = "Check the server logs.",
            level = "SEVERE")
    final static String mISSaddcmderror = LOGMSG_PREFIX + "-00006";

    @LogMessageInfo(
            message = "Error while removing failed commands from instance state: {0}",
            cause = "An attempt to remove a failed command from the instance state file failed.",
            action = "Check the server logs.",
            level = "SEVERE")
    final static String mISSremcmderror = LOGMSG_PREFIX + "-00007";

    @LogMessageInfo(
            message = "Error while setting instance state: {0}",
            cause = "An attempt to set the state of a server in the instance state file failed.",
            action = "Check the server logs.",
            level = "SEVERE")
    final static String mISSsetstateerror = LOGMSG_PREFIX + "-00008";

    @LogMessageInfo(
            message = "Error while removing instance: {0}",
            cause = "An attempt to remove a server from the instance state file failed.",
            action = "Check the server logs.",
            level = "SEVERE")
    final static String mISSremstateerror = LOGMSG_PREFIX + "-00009";

    @LogMessageInfo(
            message = "It appears that server [{0}:{1}] does not accept secure connections. Retry with --secure=false.",
            cause = "An attempt to invoke a command on another server failed.",
            action = "Check that the server is configured to accept secure connections.",
            level = "SEVERE")
    public final static String mServerIsNotSecure = LOGMSG_PREFIX + "-00010";

    @LogMessageInfo(
            message = "An unexpected exception occurred.",
            cause = "An unexpected exception occurred.",
            action = "Check the server logs.",
            level = "SEVERE")
    public final static String mUnexpectedException = LOGMSG_PREFIX + "-00011";

    @LogMessageInfo(
            message = "The server requires a valid admin password to be set before it can start. "
                    + "Please set a password using the change-admin-password command.",
            cause = "For security reason, the server requires a valid admin password before it can start.",
            action = "Set a password using the change-admin-password command.",
            level = "SEVERE")
    public final static String mSecureAdminEmptyPassword = LOGMSG_PREFIX + "-00012";

    @LogMessageInfo(
            message = "Can not put data to cache under key {0}",
            cause = "While invoking a command on another server, this server is unable "
                    + "to cache the meta data related to the command.",
            action = "Check the server logs.",
            level = "WARNING")
    public final static String mCantPutToCache = LOGMSG_PREFIX + "-00013";

    @LogMessageInfo(
            message = "An admin request arrived from {0} with the domain identifier {1} "
                    + "which does not match the domain identifier {2} configured for this "
                    + "server's domain; rejecting the request",
            cause = "There is a error in the cluster or network configuration.",
            action = "Check the server logs.",
            level = "WARNING")
    public final static String mForeignDomainID = LOGMSG_PREFIX + "-00014";

    @LogMessageInfo(
            message = "Error searching for a default admin user",
            cause = "An unexpected exception occurred while searching for the default admin user.",
            action = "Check the server logs.",
            level = "WARNING")
    public final static String mAdminUserSearchError = LOGMSG_PREFIX + "-00015";
}
