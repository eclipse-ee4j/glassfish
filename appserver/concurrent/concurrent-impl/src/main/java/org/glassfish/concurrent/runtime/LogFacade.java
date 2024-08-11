/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

public class LogFacade {

    @LoggerInfo(subsystem = "GlassFish-Concurrency", description = "GlassFish Concurrency Logger", publish = true)
    private static final String LOGGER_NAME = "jakarta.enterprise.concurrent";

    @LogMessagesResourceBundle
    private static final String LOGGER_RB = "org.glassfish.concurrent.runtime.LogMessages";

    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME, LOGGER_RB);

    private LogFacade() {}

    public static Logger getLogger() {
        return LOGGER;
    }

    private static final String prefix = "AS-CONCURRENT-";

    @LogMessageInfo(
            message = "Task [{0}] has been running on thread [{1}] for {2} seconds, which is more than the configured " +
                    "hung task threshold of {3} seconds in [{4}].",
            comment = "A task has been running for longer time than the configured hung task threshold setting.",
            level = "WARNING",
            cause = "A task has been running for longer time than the configured hung task threshold setting.",
            action = "Monitor the task to find out why it is running for a long time. " +
                     "If this is normal, consider setting a higher hung task threshold or setting the " +
                     "\"Long-Running Tasks\" configuration attribute to true. "
    )
    public static final String UNRESPONSIVE_TASK = prefix + "00001";

    @LogMessageInfo(
            message = "Unable to setup or reset runtime context for a task because an invalid context handle is being passed.",
            comment = "When trying to setup and runtime context for a task, an invalid context handle is being passed",
            level = "SEVERE",
            cause = "An invalid context handle is being passed.",
            action = "Contact Glassfish support. "
    )
    public static final String UNKNOWN_CONTEXT_HANDLE = prefix + "00002";

    @LogMessageInfo(
            message = "Unable to bind {0} to JNDI location [{1}].",
            comment = "An unexpected exception occurred when trying to bind a managed object to JNDI namespace.",
            level = "SEVERE",
            cause = "An unexpected exception occurred when trying to bind a managed object to JNDI namespace",
            action = "Review the exception message to determine the cause of the failure and take appropriate action. "
    )
    public static final String UNABLE_TO_BIND_OBJECT = prefix + "00003";

    @LogMessageInfo(
            message = "Unable to deploy {0}.",
            comment = "Unable to deploy a managed object because the configuration information is missing",
            level = "WARNING",
            cause = "No configuration information is provided when trying to deploy a managed object.",
            action = "Contact Glassfish support. "
    )
    public static final String DEPLOY_ERROR_NULL_CONFIG = prefix + "00004";

}
