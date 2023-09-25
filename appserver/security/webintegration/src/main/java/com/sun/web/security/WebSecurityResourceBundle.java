/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.web.security;

import com.sun.logging.LogDomains;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

class WebSecurityResourceBundle {

    @LoggerInfo(subsystem = "websecurity", description = "logger for web security module", publish = true)
    static final String LOGGER_INFO = LogDomains.WEB_LOGGER + ".security";

    @LogMessagesResourceBundle
    static final String BUNDLE_NAME = "org.glassfish.main.web.security.LogMessages";

    /**
     * #no ID on realmBase.forbidden as it is sent to client
     */
    @LogMessageInfo(level = "WARNING", message = "Access to the requested resource has been denied")
    static final String MSG_FORBIDDEN = "realmBase.forbidden";

    @LogMessageInfo(
        level = "WARNING",
        message = "WEB9101: There are some problems with the request",
        cause = "Check the exception stack-trace for more details",
        action = "Check if the credential username/password/certificate was a valid one."
    )
    static final String MSG_INVALID_REQUEST = "realmAdapter.badRequestWithId";

    @LogMessageInfo(message = "Host header not found in request")
    static final String MSG_MISSING_HOST_HEADER = "missing_http_header.host";

    @LogMessageInfo(level = "WARNING", message = "WEB9100: No WebSecurityManager found for context {0}")
    static final String MSG_NO_WEB_SECURITY_MGR = "realmAdapter.noWebSecMgr";

}