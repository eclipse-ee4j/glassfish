/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.web.integration;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author Kokil Jain
 */

public class LogUtils {
    private static final String LOGMSG_PREFIX = "AS-SECURITY";

    @LogMessagesResourceBundle
    public static final String LOG_MESSAGES = "com.sun.enterprise.security.ee.web.integration.LogMessages";

    @LoggerInfo(subsystem = "SECURITY", description = "Core-ee Security Logger", publish = true)
    public static final String LOG_DOMAIN = "jakarta.enterprise.system.core.security.web";

    private static final Logger LOGGER = Logger.getLogger(LOG_DOMAIN, LOG_MESSAGES);

    public static Logger getLogger() {
        return LOGGER;
    }

    @LogMessageInfo(message = "Exception while getting the CodeSource", level = "SEVERE", cause = "unknown", action = "unknown")
    public static final String EJBSM_CODSOURCEERROR = LOGMSG_PREFIX + "-00001";

    @LogMessageInfo(message = "[Web-Security] WebSecurityManager - Exception while getting the PolicyFactory", level = "SEVERE", cause = "unknown", action = "unknown")
    public static final String JACCFACTORY_NOTFOUND = LOGMSG_PREFIX + "-00002";

    @LogMessageInfo(message = "[Web-Security] setPolicy SecurityPermission required to call PolicyContext.setContextID", level = "SEVERE", cause = "unknown", action = "unknown")
    public static final String SECURITY_PERMISSION_REQUIRED = LOGMSG_PREFIX + "-00003";

    @LogMessageInfo(message = "[Web-Security] Unexpected Exception while setting policy context", level = "SEVERE", cause = "unknown", action = "unknown")
    public static final String POLICY_CONTEXT_EXCEPTION = LOGMSG_PREFIX + "-00004";

    @LogMessageInfo(message = "JACC: For the URL pattern {0}, all but the following methods have been excluded: {1}", level = "INFO", cause = "unknown", action = "unknown")
    public static final String NOT_EXCLUDED_METHODS = LOGMSG_PREFIX + "-00005";

    @LogMessageInfo(message = "JACC: For the URL pattern {0}, the following methods have been excluded: {1}", level = "INFO", cause = "unknown", action = "unknown")
    public static final String EXCLUDED_METHODS = LOGMSG_PREFIX + "-00006";

    @LogMessageInfo(message = "JACC: For the URL pattern {0}, all but the following methods were uncovered: {1}", level = "WARNING", cause = "unknown", action = "unknown")
    public static final String NOT_UNCOVERED_METHODS = LOGMSG_PREFIX + "-00007";

    @LogMessageInfo(message = "JACC: For the URL pattern {0}, the following methods were uncovered: {1}", level = "WARNING", cause = "unknown", action = "unknown")
    public static final String UNCOVERED_METHODS = LOGMSG_PREFIX + "-00008";

}
