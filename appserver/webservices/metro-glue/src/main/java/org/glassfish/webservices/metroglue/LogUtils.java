/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices.metroglue;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author Lukas Jungmann
 */
public final class LogUtils {

    private static final String LOGMSG_PREFIX = "AS-WSMETROGLUE";

    @LogMessagesResourceBundle
    public static final String LOG_MESSAGES = "org.glassfish.webservices.metroglue.LogMessages";

    @LoggerInfo(subsystem = "WEBSERVICES", description = "Metro Glue Main Logger", publish = true)
    public static final String LOG_DOMAIN = "jakarta.enterprise.webservices.metroglue";

    private static final Logger LOGGER = Logger.getLogger(LOG_DOMAIN, LOG_MESSAGES);

    public static Logger getLogger() {
        return LOGGER;
    }

    @LogMessageInfo(
            message = "Web service endpoint deployment events listener registered successfully.",
            level = "INFO")
    public static final String ENDPOINT_EVENT_LISTENER_REGISTERED = LOGMSG_PREFIX + "-10010";

    @LogMessageInfo(
            message = "High availability environment configuration injected into Metro high availability provider.",
            level = "INFO")
    public static final String METRO_HA_ENVIRONEMT_INITIALIZED = LOGMSG_PREFIX + "-10020";

    @LogMessageInfo(
            message = "Endpoint deployment even received.",
            level = "FINEST")
    public static final String ENDPOINT_EVENT_DEPLOYED = LOGMSG_PREFIX + "-10011";

    @LogMessageInfo(
            message = "Endpoint undeployment even received.",
            level = "FINEST")
    public static final String ENDPOINT_EVENT_UNDEPLOYED = LOGMSG_PREFIX + "-10012";

    @LogMessageInfo(
            message = "Loading WS-TX Services. Please wait.",
            level = "INFO")
    public static final String WSTX_SERVICE_LOADING = LOGMSG_PREFIX + "-10001";

    @LogMessageInfo(
            message = "WS-TX Services successfully started.",
            level = "INFO")
    public static final String WSTX_SERVICE_STARTED = LOGMSG_PREFIX + "-10002";

    @LogMessageInfo(
            message = "WS-TX Services application was deployed explicitly.",
            level = "WARNING")
    public static final String WSTX_SERVICE_DEPLOYED_EXPLICITLY = LOGMSG_PREFIX + "-10003";

    @LogMessageInfo(
            message = "Cannot deploy or load WS-TX Services: {0}",
            comment = "{0} - cause",
            level = "WARNING")
    public static final String WSTX_SERVICE_CANNOT_DEPLOY = LOGMSG_PREFIX + "-10004";

    @LogMessageInfo(
            message = "Caught unexpected exception.",
            level = "WARNING")
    public static final String WSTX_SERVICE_UNEXPECTED_EXCEPTION = LOGMSG_PREFIX + "-19999";

    @LogMessageInfo(
            message = "Exception occurred retrieving port configuration for WSTX service.",
            level = "FINEST")
    public static final String WSTX_SERVICE_PORT_CONFIGURATION_EXCEPTION = LOGMSG_PREFIX + "-19998";
}
