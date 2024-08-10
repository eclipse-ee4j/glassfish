/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.util;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bnevins
 */
public final class PortConstants {

    private PortConstants() {
    }

    public static final int PORT_MAX_VAL = 65535;

    public static final int DEFAULT_HTTPSSL_PORT = 8181;
    public static final int DEFAULT_IIOPSSL_PORT = 3820;
    public static final int DEFAULT_IIOPMUTUALAUTH_PORT = 3920;
    public static final int DEFAULT_INSTANCE_PORT = 8080;
    public static final int DEFAULT_JMS_PORT = 7676;
    public static final int DEFAULT_IIOP_PORT = 3700;
    public static final int DEFAULT_JMX_PORT = 8686;
    public static final int DEFAULT_OSGI_SHELL_TELNET_PORT = 6666;
    public static final int DEFAULT_JAVA_DEBUGGER_PORT = 9009;

    public static final int PORTBASE_ADMINPORT_SUFFIX = 48;
    public static final int PORTBASE_HTTPSSL_SUFFIX = 81;
    public static final int PORTBASE_IIOPSSL_SUFFIX = 38;
    public static final int PORTBASE_IIOPMUTUALAUTH_SUFFIX = 39;
    public static final int PORTBASE_INSTANCE_SUFFIX = 80;
    public static final int PORTBASE_JMS_SUFFIX = 76;
    public static final int PORTBASE_IIOP_SUFFIX = 37;
    public static final int PORTBASE_JMX_SUFFIX = 86;
    public static final int PORTBASE_OSGI_SUFFIX = 66;
    public static final int PORTBASE_DEBUG_SUFFIX = 9;

    // these are the ports that we support handling conflicts for...
    public static final String ADMIN = "ASADMIN_LISTENER_PORT";
    public static final String HTTP = "HTTP_LISTENER_PORT";
    public static final String HTTPS = "HTTP_SSL_LISTENER_PORT";
    public static final String IIOP = "IIOP_LISTENER_PORT";
    public static final String IIOPM = "IIOP_SSL_MUTUALAUTH_PORT";
    public static final String IIOPS = "IIOP_SSL_LISTENER_PORT";
    public static final String JMS = "JMS_PROVIDER_PORT";
    public static final String JMX = "JMX_SYSTEM_CONNECTOR_PORT";
    public static final String OSGI = "OSGI_SHELL_TELNET_PORT";
    public static final String DEBUG = "JAVA_DEBUGGER_PORT";

    private static final String[] PORTS = new String[] { ADMIN, HTTP, HTTPS, IIOP, IIOPM, IIOPS, JMS, JMX, OSGI, DEBUG };

    public static final List<String> PORTSLIST = Arrays.asList(PORTS);
}
