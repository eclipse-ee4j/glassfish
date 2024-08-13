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

package org.glassfish.ejb;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

public final class LogFacade {

    @LoggerInfo(subsystem = "GlassFish-EJB", description = "GlassFish EJB Container Logger", publish = true)
    private static final String EJB_LOGGER_NAME = "jakarta.enterprise.ejb.container";

    @LogMessagesResourceBundle
    private static final String EJB_LOGGER_RB = "org.glassfish.ejb.LogMessages";

    private static final Logger LOGGER = Logger.getLogger(EJB_LOGGER_NAME, EJB_LOGGER_RB);

    private LogFacade() {}

    public static Logger getLogger() {
        return LOGGER;
    }

}
