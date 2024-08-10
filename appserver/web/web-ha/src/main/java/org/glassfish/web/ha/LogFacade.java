/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.ha;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
/**
 *
 * Provides the logging facilities.
 *
 * @author Shing Wai Chan
 */
public class LogFacade {
    /**
     * The logger to use for logging ALL web container related messages.
     */
    @LogMessagesResourceBundle
    private static final String SHARED_LOGMESSAGE_RESOURCE =
            "org.glassfish.web.ha.session.management.LogMessages";

    @LoggerInfo(subsystem="WEB", description="WEB HA Logger", publish=true)
    private static final String WEB_HA_LOGGER = "jakarta.enterprise.web.ha";

    private static final Logger LOGGER =
            Logger.getLogger(WEB_HA_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    private LogFacade() {}

    public static Logger getLogger() {
        return LOGGER;
    }

    private static final String prefix = "AS-WEB-HA-";

    @LogMessageInfo(
            message = "Exception during removing synchronized from backing store",
            level = "WARNING")
    public static final String EXCEPTION_REMOVING_SYNCHRONIZED = prefix + "00001";

    @LogMessageInfo(
            message = "Exception during removing expired session from backing store",
            level = "WARNING")
    public static final String EXCEPTION_REMOVING_EXPIRED_SESSION = prefix + "00002";

    @LogMessageInfo(
            message = "Error creating inputstream",
            level = "WARNING")
    public static final String ERROR_CREATING_INPUT_STREAM = prefix + "00003";

    @LogMessageInfo(
            message = "Exception during deserializing the session",
            level = "WARNING")
    public static final String EXCEPTION_DESERIALIZING_SESSION = prefix + "00004";

    @LogMessageInfo(
            message = "Exception occurred in getSession",
            level = "WARNING")
    public static final String EXCEPTION_GET_SESSION = prefix + "00005";

    @LogMessageInfo(
            message = "Failed to remove session from backing store",
            level = "WARNING")
    public static final String FAILED_TO_REMOVE_SESSION = prefix + "00006";

    @LogMessageInfo(
            message = "Required version NumberFormatException",
            level = "INFO")
    public static final String REQUIRED_VERSION_NFE = prefix + "00007";

    @LogMessageInfo(
            message = "Could not create backing store",
            level = "WARNING")
    public static final String COULD_NOT_CREATE_BACKING_STORE = prefix + "00008";

}
