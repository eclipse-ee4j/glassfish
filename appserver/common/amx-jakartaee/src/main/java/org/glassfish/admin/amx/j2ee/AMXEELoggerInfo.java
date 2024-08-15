/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.j2ee;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Logger information for the amx-jakartaee module.
 * @author Tom Mueller
 */
/* Module private */
public class AMXEELoggerInfo {
    public static final String LOGMSG_PREFIX = "AS-AMXEE";

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.admin.amx.j2ee.LogMessages";

    @LoggerInfo(subsystem = "AMX-JAKARTAEE", description = "AMX Services", publish = true)
    public static final String AMXEE_LOGGER = "jakarta.enterprise.system.tools.amxee";
    private static final Logger amxEELogger = Logger.getLogger(
                AMXEE_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    public static Logger getLogger() {
        return amxEELogger;
    }

    @LogMessageInfo(
            message = "Registering application {0} using AMX having exception {1}",
            level = "INFO")
    public static final String registeringApplicationException = LOGMSG_PREFIX + "-001";

    @LogMessageInfo(
            message = "Null from ApplicationInfo.getMetadata(Application.class) for application {0}",
            level = "WARNING")
    public static final String nullAppinfo = LOGMSG_PREFIX + "-002";

    @LogMessageInfo(
            message = "Unable to get Application config for application {0}",
            level = "WARNING")
    public static final String errorGetappconfig = LOGMSG_PREFIX + "-003";

    @LogMessageInfo(
            message = "Can't register JSR 77 MBean for resourceRef {0} having exception {1}",
            level = "INFO")
    public static final String cantRegisterMbean = LOGMSG_PREFIX + "-004";

    @LogMessageInfo(
            message = "Can't unregister MBean: {0}",
            level = "WARNING")
    public static final String cantUnregisterMbean = LOGMSG_PREFIX + "-005";

    @LogMessageInfo(
            message = "J2EEDomain registered at {0}",
            level = "INFO")
    public static final String domainRegistered = LOGMSG_PREFIX + "-006";



}
