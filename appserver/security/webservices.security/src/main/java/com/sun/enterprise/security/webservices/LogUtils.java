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

package com.sun.enterprise.security.webservices;

import java.util.logging.Logger;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author lukas
 */
public class LogUtils {
private static final String LOGMSG_PREFIX = "AS-WSSECURITY";

    @LogMessagesResourceBundle
    public static final String LOG_MESSAGES = "com.sun.enterprise.security.ee.webservices.LogMessages";

    @LoggerInfo(subsystem = "WEBSERVICES", description = "Web Services Security Logger", publish = true)
    public static final String LOG_DOMAIN = "jakarta.enterprise.webservices.security";

    private static final Logger LOGGER = Logger.getLogger(LOG_DOMAIN, LOG_MESSAGES);

    public static Logger getLogger() {
        return LOGGER;
    }

    @LogMessageInfo(
            message = "Request processing failed.",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String NEXT_PIPE = LOGMSG_PREFIX + "-00001";

    @LogMessageInfo(
            message = "SEC2002: Container-auth: wss: Error validating request.",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String ERROR_REQUEST_VALIDATION = LOGMSG_PREFIX + "-00002";

    @LogMessageInfo(
            message = "SEC2003: Container-auth: wss: Error securing response.",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String ERROR_RESPONSE_SECURING = LOGMSG_PREFIX + "-00003";

    @LogMessageInfo(
            message = "SEC2004: Container-auth: wss: Error securing request.",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String ERROR_REQUEST_SECURING = LOGMSG_PREFIX + "-00004";

    @LogMessageInfo(
            message = "SEC2005: Container-auth: wss: Error validating response.",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String ERROR_RESPONSE_VALIDATION = LOGMSG_PREFIX + "-00005";

    @LogMessageInfo(
            message = "SEC2006: Container-auth: wss: Not a SOAP message context.",
            level = "WARNING",
            cause = "unknown",
            action = "unknown")
    public static final String NOT_SOAP = LOGMSG_PREFIX + "-00006";

    @LogMessageInfo(
            message = "EJB Webservice security configuration Failure.",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String EJB_SEC_CONFIG_FAILURE = LOGMSG_PREFIX + "-00007";

    @LogMessageInfo(
            message = "Servlet Webservice security configuration Failure",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String SERVLET_SEC_CONFIG_FAILURE = LOGMSG_PREFIX + "-00008";

    @LogMessageInfo(
            message = "BASIC AUTH username/password http header parsing error for {0}",
            comment = "{0} - endpont",
            level = "WARNING",
            cause = "unknown",
            action = "unknown")
    public static final String BASIC_AUTH_ERROR = LOGMSG_PREFIX + "-00009";

    @LogMessageInfo(
            message = "Servlet Webservice security configuration Failure",
            comment = "{0} - endpont",
            level = "WARNING",
            cause = "unknown",
            action = "unknown")
    public static final String CLIENT_CERT_ERROR = LOGMSG_PREFIX + "-00010";

    @LogMessageInfo(
            message = "Following exception was thrown:",
            level = "SEVERE",
            cause = "unknown",
            action = "unknown")
    public static final String EXCEPTION_THROWN = LOGMSG_PREFIX + "-00011";

}
