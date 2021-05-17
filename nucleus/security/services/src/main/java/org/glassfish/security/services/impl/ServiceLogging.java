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

package org.glassfish.security.services.impl;

import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * The security service logging class.
 */
public class ServiceLogging {
    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.security.services.LogMessages";

    @LoggerInfo(subsystem="SECSVCS", description="Security Services Logger", publish=true)
    public static final String SEC_SVCS_LOGGER = "jakarta.enterprise.security.services";

    @LoggerInfo(subsystem="SECPROV", description="Security Provider Logger", publish=true)
    public static final String SEC_PROV_LOGGER = "jakarta.enterprise.security.services.provider";

    @LoggerInfo(subsystem="SECCMDS", description="Security Services Command Logger", publish=true)
    public static final String SEC_COMMANDS_LOGGER = "jakarta.enterprise.security.services.commands";
}
