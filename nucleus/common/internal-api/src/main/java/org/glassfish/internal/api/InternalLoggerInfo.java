/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.internal.api;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Logger information for the internal-api module.
 *
 * @author Tom Mueller
 */
/* Module private */
public class InternalLoggerInfo {

    public static final String LOGMSG_PREFIX = "NCLS-COM";

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.internal.api.LogMessages";

    @LoggerInfo(subsystem = "COMMON", description = "Internal API", publish = true)
    public static final String INT_LOGGER = "jakarta.enterprise.system.tools.util";

    private static final Logger intLogger = Logger.getLogger(INT_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    public static Logger getLogger() {
        return intLogger;
    }

    @LogMessageInfo(
            message = "Exception {0} resolving password alias {1} in property {2}.",
            level = "WARNING")
    public static final String exceptionResolvingAlias = LOGMSG_PREFIX + "-01001";

    @LogMessageInfo(
            message = "Unknown property {0} found unresolving {1}.",
            cause = "No value was found for a property. This indicates a software problem.",
            action = "Check the server logs.",
            level = "SEVERE")
    public static final String unknownProperty = LOGMSG_PREFIX + "-01002";

    @LogMessageInfo(
            message = "System property reference missing trailing \"'}'\" at {0} in domain.xml.",
            cause = "A system property reference in domain.xml is invalid.",
            action = "Check the domain.xml file for an invalid system property reference.",
            level = "SEVERE")
    public static final String referenceMissingTrailingDelim = LOGMSG_PREFIX + "-01003";

    @LogMessageInfo(
            message = "System property reference missing starting \"$'{'\" at {0} in domain.xml.",
            cause = "A system property reference in domain.xml is invalid.",
            action = "Check the domain.xml file for an invalid system property reference.",
            level = "SEVERE")
    public static final String referenceMissingStartingDelim = LOGMSG_PREFIX + "-01004";
}
