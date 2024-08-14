/*
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

package com.sun.enterprise.connectors.work;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

public class LogFacade {

    @LoggerInfo(subsystem = "connector", description = "logger for connector work manager module", publish = true)
    public static final String CONNECTOR_WORK_MANAGER_LOGGER = "jakarta.enterprise.connector.work";

    @LogMessagesResourceBundle
    public static final String SHARING_MESSAGE_RB = "com.sun.enterprise.connectors.work.LogMessages";

    private static final Logger _logger = Logger.getLogger(CONNECTOR_WORK_MANAGER_LOGGER, SHARING_MESSAGE_RB);

    private LogFacade() {}

    public static Logger getLogger() {
        return _logger;
    }

}

