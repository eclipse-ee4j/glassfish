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

package org.glassfish.admin.restconnector;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 *
 * @author jdlee
 */
public class Logging {
    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.admin.restconnector.LogMessages";
    @LoggerInfo(subsystem = "RSTCN", description = "REST Connector Logger", publish = true)
    public static final String REST_CONNECTOR_LOGGER = "jakarta.enterprise.admin.rest.connector";
    public static final Logger logger = Logger.getLogger(REST_CONNECTOR_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    @LogMessageInfo(message = "The REST connector has been started", level = "INFO")
    public static final String REST_CONNECTOR_STARTED = "NCLS-RSTCN-00001";
}
