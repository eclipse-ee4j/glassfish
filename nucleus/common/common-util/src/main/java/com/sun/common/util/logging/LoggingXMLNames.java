/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.common.util.logging;

import com.sun.logging.LogDomains;

import java.util.HashMap;
import java.util.Map;

import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_ROOT_HANDLERS;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ENABLED;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.FORMATTER;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.OUTPUT_FILE;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.REDIRECT_STANDARD_STREAMS;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_LIMIT_SIZE;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.ROTATION_LIMIT_TIME;

public class LoggingXMLNames {

    public static final String file = "file";

    public static final String logRotationLimitInBytes = "log-rotation-limit-in-bytes";

    public static final String logRotationTimelimitInMinutes = "log-rotation-timelimit-in-minutes";

    public static final String logFormatter = "log-formatter";

    public static final String logHandler = "log-handler";

    public static final String useSystemLogging = "use-system-logging";

    public static final String logFilter = "log-filter";

    public static final String logToConsole = "log-to-console";
    public static final String logToFile = "log-to-file";
    public static final String logStandardStreams = "log-standard-streams";

    public static final String alarms = "alarms";

    public static final String retainErrorStatisticsForHours = "retain-error-statistics-for-hours";
    // logger names from DTD
    public static final String root = "root";
    public static final String server = "server";
    public static final String ejbcontainer = "ejb-container";
    public static final String cmpcontainer = "cmp-container";
    public static final String mdbcontainer = "mdb-container";
    public static final String webcontainer = "web-container";
    public static final String classloader = "classloader";
    public static final String configuration = "configuration";
    public static final String naming = "naming";
    public static final String security = "security";
    public static final String jts = "jts";
    public static final String jta = "jta";
    public static final String admin = "admin";
    public static final String deployment = "deployment";
    public static final String verifier = "verifier";
    public static final String jaxr = "jaxr";
    public static final String jaxrpc = "jaxrpc";
    public static final String saaj = "saaj";
    public static final String corba = "corba";
    public static final String javamail = "javamail";
    public static final String jms = "jms";
    public static final String connector = "connector";
    public static final String jdo = "jdo";
    public static final String cmp = "cmp";
    public static final String util = "util";
    public static final String resourceadapter = "resource-adapter";
    public static final String synchronization = "synchronization";
    public static final String nodeAgent = "node-agent";
    public static final String selfmanagement = "self-management";
    public static final String groupmanagementservice = "group-management-service";
    public static final String managementevent = "management-event";

    private static final String LEVEL = ".level";

//mapping of the names used in domain.xml to the names used in logging.properties
public static final Map<String, String> xmltoPropsMap = new HashMap<>() {
        {
            put(logHandler, KEY_ROOT_HANDLERS.getPropertyName());

            put(file, OUTPUT_FILE.getPropertyFullName());
            put(logToFile, ENABLED.getPropertyFullName());
            put(logStandardStreams, REDIRECT_STANDARD_STREAMS.getPropertyFullName());
            put(logFormatter, FORMATTER.getPropertyFullName());
            put(logRotationLimitInBytes, ROTATION_LIMIT_SIZE.getPropertyFullName());
            put(logRotationTimelimitInMinutes, ROTATION_LIMIT_TIME.getPropertyFullName());

            put(root, LogDomains.DOMAIN_ROOT + LEVEL);
            put(server, LogDomains.SERVER_LOGGER + LEVEL);
            put(ejbcontainer, LogDomains.EJB_LOGGER + LEVEL);
            put(cmpcontainer, LogDomains.CMP_LOGGER + LEVEL);
            put(mdbcontainer, LogDomains.MDB_LOGGER + LEVEL);
            put(webcontainer, LogDomains.WEB_LOGGER + LEVEL);
            put(classloader, LogDomains.LOADER_LOGGER + LEVEL);
            put(configuration, LogDomains.CONFIG_LOGGER + LEVEL);
            put(naming, LogDomains.JNDI_LOGGER + LEVEL);
            put(security, LogDomains.SECURITY_LOGGER + LEVEL);
            put(jts, LogDomains.TRANSACTION_LOGGER + LEVEL);
            put(jta, LogDomains.JTA_LOGGER + LEVEL);
            put(admin, LogDomains.ADMIN_LOGGER + LEVEL);
            put(deployment, LogDomains.DPL_LOGGER + LEVEL);
            put(jaxr, LogDomains.JAXR_LOGGER + LEVEL);
            put(jaxrpc, LogDomains.JAXRPC_LOGGER + LEVEL);
            put(saaj, LogDomains.SAAJ_LOGGER + LEVEL);
            put(corba, LogDomains.CORBA_LOGGER + LEVEL);
            put(javamail, LogDomains.JAVAMAIL_LOGGER + LEVEL);
            put(jms, LogDomains.JMS_LOGGER + LEVEL);
            put(jdo, LogDomains.JDO_LOGGER + LEVEL);
            put(cmp, LogDomains.CMP_LOGGER + LEVEL);
            put(util, LogDomains.UTIL_LOGGER + LEVEL);
            put(resourceadapter, LogDomains.RSR_LOGGER + LEVEL);
            put(selfmanagement, LogDomains.SELF_MANAGEMENT_LOGGER + LEVEL);

            // following values will be removed, because they would not be used.
            put(logToConsole, null);
            put(alarms, null);
            put(retainErrorStatisticsForHours, null);
        }
    };
}

