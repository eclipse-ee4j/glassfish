/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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


package com.sun.jaspic.config.helper;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 */
public class JASPICLogManager {

    @LoggerInfo(subsystem = "jaspic", description = "logger for jaspic security config module", publish = true)
    public static final String LOGGER = "com.sun.logging.enterprise.system.jaspic.config";

    @LogMessagesResourceBundle
    public static final String BUNDLE = "com.sun.jaspic.config.helper.LogMessages";

    @LogMessageInfo(level = "WARNING", message = "SEC1203: GFAuthConfigFactory unable to load Provider: ")
    public static final String MSG_UNABLE_LOAD_PROVIDER = "jmac.factory_unable_to_load_provider";

    @LogMessageInfo(level = "WARNING", message = "SEC1204: GFAuthConfigFactory loader failure")
    public static final String MSG_LOADER_FAILURE = "jmac.factory_auth_config_loader_failure";

    @LogMessageInfo(
        level = "WARNING",
        message = "SEC1205: Cannot write to file {0}. Updated provider list will not be persisted.")
    public static final String MSG_CANNOT_WRITE_PROVIDERS_TO_FILE = "jmac.factory_cannot_write_file";

    @LogMessageInfo(
        level = "WARNING",
        message = "SEC1206: Could not persist updated provider list. Will use default providers when reloaded.")
    public static final String MSG_CANNOT_PERSIST_PROVIDERS = "jmac.factory_could_not_persist";

    @LogMessageInfo(
        level = "WARNING",
        message = "SEC1207: Could not read auth configuration file. Will use default providers.")
    public static final String MSG_COULD_NOT_READ_AUTH_CFG = "jmac.factory_could_not_read";

    @LogMessageInfo(
        level = "FINE",
        message = "SEC1208: Configuration file does not exist at {0}. Will use default providers.")
    public static final String MSG_FILE_NOT_EXIST = "jmac.factory_file_not_found";

    @LogMessageInfo(level = "INFO", message = "SEC1210: Creating JMAC Configuration file {0}.")
    public static final String MSG_CREATING_JMAC_FILE = "jmac.factory_creating_conf_file";
}
