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

package org.glassfish.webservices.connector;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author Lukas Jungmann
 */
public final class LogUtils {

    private static final String LOGMSG_PREFIX = "AS-WSCONNECTOR";

    @LogMessagesResourceBundle
    public static final String LOG_MESSAGES = "org.glassfish.webservices.connector.LogMessages";

    @LoggerInfo(subsystem = "WEBSERVICES", description = "Web Services Connector Logger", publish = true)
    public static final String LOG_DOMAIN = "jakarta.enterprise.webservices.connector";

    private static final Logger LOGGER = Logger.getLogger(LOG_DOMAIN, LOG_MESSAGES);

    public static Logger getLogger() {
        return LOGGER;
    }

    @LogMessageInfo(
            message = "Invalid Deployment Descriptors element {0} value {1}.",
            comment = "{0} - prefix, {1} - localname",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String INVALID_DESC_MAPPING_FAILURE = LOGMSG_PREFIX + "-00046";

    @LogMessageInfo(
            message = "Following exception was thrown",
            level = "FINE")
    public static final String EXCEPTION_THROWN = LOGMSG_PREFIX + "-00050";

    @LogMessageInfo(
            message = "JAX-WS RI specific descriptor ({1}) is found in the archive {0} and \\n"
                    + "hence Enterprise Web Service (109) deployment is disabled for this archive"
                    + " to avoid duplication of services.",
            comment = "{0} - archive name, {1} - descriptor path",
            level = "INFO")
    public static final String DEPLOYMENT_DISABLED = LOGMSG_PREFIX + "-00057";

    @LogMessageInfo(
            message = "Handler class {0} specified in deployment descriptor not found.",
            comment = "{0} - class name",
            level = "WARNING")
    public static final String DDHANDLER_NOT_FOUND = LOGMSG_PREFIX + "-00201";

    @LogMessageInfo(
            message = "Handler class {0} specified in handler file {1} cannot be loaded.",
            comment = "{0} - class name, {1} - file name",
            level = "WARNING")
    public static final String HANDLER_FILE_HANDLER_NOT_FOUND = LOGMSG_PREFIX + "-00202";

    @LogMessageInfo(
            message = "Warning : Web service endpoint {0} is not tied to a component.",
            comment = "{0} - endpoint name",
            level = "INFO")
    public static final String WS_NOT_TIED_TO_COMPONENT = LOGMSG_PREFIX + "-00203";

    @LogMessageInfo(
            message = "Warning: Web service endpoint {0} component link {1} is not valid.",
            comment = "{0} - endpoint name, {1} - link name",
            level = "INFO")
    public static final String WS_COMP_LINK_NOT_VALID = LOGMSG_PREFIX + "-00204";

    @LogMessageInfo(
            message = "URL mapping for web service {0} already exists. Is port-component-name in webservices.xml correct?",
            comment = "{0} - endpoint name",
            level = "SEVERE",
            cause = "Invalid port-component-name value in webservices.xml.",
            action = "Fix port-component-name element in webservices.xml.")
    public static final String WS_URLMAPPING_EXISTS = LOGMSG_PREFIX + "-00205";
}
