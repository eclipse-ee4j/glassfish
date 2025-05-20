/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.glassfish.bootstrap.log;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

public class LogFacade {

    @LoggerInfo(subsystem = "BOOTSTRAP", description="Main bootstrap logger.")
    public static final String BOOTSTRAP_LOGGER_NAME = "jakarta.enterprise.bootstrap";

    @LogMessagesResourceBundle
    public static final String RB_NAME = "com.sun.enterprise.glassfish.bootstrap.log.LogMessages";

    public static final Logger BOOTSTRAP_LOGGER = Logger.getLogger(BOOTSTRAP_LOGGER_NAME, RB_NAME);

    @LogMessageInfo(
            message = "GlassFish requires JDK {0}, you are using JDK version {1}.",
            level = "SEVERE",
            cause="Incorrect JDK version is used.",
            action="Please use correct JDK version.")
    public static final String BOOTSTRAP_INCORRECT_JDKVERSION = "NCLS-BOOTSTRAP-00001";

    @LogMessageInfo(
            message = "Using {0} as the framework configuration file.",
            level = "INFO")
    public static final String BOOTSTRAP_FMWCONF = "NCLS-BOOTSTRAP-00002";

}
