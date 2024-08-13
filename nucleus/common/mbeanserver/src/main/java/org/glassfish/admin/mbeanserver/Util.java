/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.mbeanserver;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
Can't use things in amx-api, so a few methods are place here.
 */
public final class Util
{
    @LoggerInfo(subsystem = "JMX", description="JMX System Logger")
    private static final String JMX_LOGGER_NAME = "jakarta.enterprise.system.jmx";

    @LogMessagesResourceBundle
    private static final String LOG_MESSAGES_RB = "org.glassfish.admin.mbeanserver.LogMessages";

    public static final Logger JMX_LOGGER = Logger.getLogger(JMX_LOGGER_NAME, LOG_MESSAGES_RB);

    public static final String LOG_PREFIX = "NCLS-JMX-";

    public static String localhost()
    {
        try
        {
            return java.net.InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (java.net.UnknownHostException e)
        {
        }
        return "localhost";
    }

    public static Logger getLogger()
    {
        return JMX_LOGGER;
    }

}




