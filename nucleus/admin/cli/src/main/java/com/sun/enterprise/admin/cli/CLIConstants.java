/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli;

import java.lang.System.Logger.Level;
import java.time.Duration;

import org.glassfish.embeddable.GlassFishVariable;

import static org.glassfish.embeddable.GlassFishVariable.TIMEOUT_START_SERVER;
import static org.glassfish.embeddable.GlassFishVariable.TIMEOUT_STOP_SERVER;

/**
 * Constants for use in this package and "sub" packages
 *
 * @author bnevins
 */
public final class CLIConstants {
    public static final int DEFAULT_ADMIN_PORT = 4848;
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String EOL = System.lineSeparator();

    public static final Duration WAIT_FOR_DAS_TIME_MS = getEnv(TIMEOUT_START_SERVER, 60L);
    public static final int RESTART_NORMAL = 10;
    public static final int RESTART_DEBUG_ON = 11;
    public static final int RESTART_DEBUG_OFF = 12;
    public static final String WALL_CLOCK_START_PROP = "WALL_CLOCK_START";
    public static final String MASTER_PASSWORD = "AS_ADMIN_MASTERPASSWORD";
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;
    public static final int WARNING = 4;
    public static final Duration DEATH_TIMEOUT_MS = getEnv(TIMEOUT_STOP_SERVER, 60L);

    public static final String K_ADMIN_PORT = "agent.adminPort";
    public static final String K_ADMIN_HOST = "agent.adminHost";
    public static final String K_AGENT_PROTOCOL = "agent.protocol";
    public static final String K_CLIENT_HOST = "agent.client.host";
    public static final String K_DAS_HOST = "agent.das.host";
    public static final String K_DAS_PROTOCOL = "agent.das.protocol";
    public static final String K_DAS_PORT = "agent.das.port";
    public static final String K_DAS_IS_SECURE = "agent.das.isSecure";

    public static final String K_MASTER_PASSWORD = "agent.masterpassword";
    public static final String K_SAVE_MASTER_PASSWORD = "agent.saveMasterPassword";

    public static final String AGENT_LISTEN_ADDRESS_NAME = "listenaddress";
    public static final String REMOTE_CLIENT_ADDRESS_NAME = "remoteclientaddress";
    public static final String AGENT_JMX_PROTOCOL_NAME = "agentjmxprotocol";
    public static final String DAS_JMX_PROTOCOL_NAME = "dasjmxprotocol";
    public static final String AGENT_DAS_IS_SECURE = "isDASSecure";

    public static final String NODEAGENT_DEFAULT_DAS_IS_SECURE = "false";
    public static final String NODEAGENT_DEFAULT_DAS_PORT = String.valueOf(DEFAULT_ADMIN_PORT);
    public static final String NODEAGENT_DEFAULT_HOST_ADDRESS = "0.0.0.0";
    public static final String NODEAGENT_JMX_DEFAULT_PROTOCOL = "rmi_jrmp";
    public static final int RESTART_CHECK_INTERVAL_MSEC = 10;

    private CLIConstants() {
        // no instances allowed!
    }

    private static Duration getEnv(GlassFishVariable variable, long defaultValue) {
        String name = variable.getEnvName();
        String value = System.getenv(name);
        if (value == null) {
            return Duration.ofSeconds(defaultValue);
        }
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue > 0L) {
                return Duration.ofSeconds(parsedValue);
            }
            System.getLogger(CLIConstants.class.getName()).log(Level.WARNING,
                "The value of the environment property {0} must be positive.", name);
            return Duration.ofSeconds(defaultValue);
        } catch (NumberFormatException e) {
            System.getLogger(CLIConstants.class.getName()).log(Level.WARNING,
                "Environment property {0} is set to a value {1} which cannot be parsed to duration.", name, value);
            return Duration.ofSeconds(defaultValue);
        }
    }
}
