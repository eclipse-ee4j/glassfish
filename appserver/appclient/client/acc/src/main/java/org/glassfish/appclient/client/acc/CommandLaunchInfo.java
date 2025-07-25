/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.appclient.client.acc;

import java.util.List;

/**
 * Encapsulates all the details of handling the ACC agent arguments.
 * <p>
 * The agent accepts agent arguments, mostly to find out:
 * <ul>
 * <li>what type of client
 * the user specified (JAR, directory, class, or class file) and the path or
 * name for the file, and
 * <li>the appclient command arguments from the appclient command.
 * </ul>
 * Each word that is an appclient script argument appears in the agent
 * arguments as "arg=..." which distinguishes them from arguments intended
 * directly for the agent.  Note that the appclient script does not pass
 * through the user's -client xxx option and value.  Instead it passes
 * client=... intended for the agent (note the lack of the - sign).
 *
 * @author tjquinn
 */
public class CommandLaunchInfo {

    /* agent argument names */
    private static final String CLIENT_AGENT_ARG_NAME = "client";
    private static final String APPCPATH = "appcpath";

    /* records the type of launch the user requested: jar, directory, class, or class file*/
    private ClientLaunchType clientLaunchType;

    /* records the client JAR file path, directory path, class name, or class file path */
    private String clientName;

    private String appcPath;


    public static CommandLaunchInfo newInstance(final AgentArguments agentArgs) {
        final CommandLaunchInfo result = new CommandLaunchInfo(agentArgs);
        return result;
    }


    private CommandLaunchInfo(final AgentArguments agentArgs) {
        clientLaunchType = saveArgInfo(agentArgs);
    }

    /**
     * Returns the name part of the client selection expression.  This can be
     * the file path to a JAR, a file path to a directory, the fully-qualified
     * class name for the main class, or the file path to a .class file.
     *
     * @return the name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Returns which type of launch the user has triggered given the combination
     * of options he or she specified.
     * @return
     */
    public ClientLaunchType getClientLaunchType() {
        return clientLaunchType;
    }

    public String getAppcPath() {
        return appcPath;
    }

    private ClientLaunchType saveArgInfo(final AgentArguments agentArgs) {
        if (agentArgs == null){
            return ClientLaunchType.UNKNOWN;
        }
        ClientLaunchType result = ClientLaunchType.UNKNOWN;

        String s;
        if ((s = lastFromList(agentArgs.namedValues(CLIENT_AGENT_ARG_NAME))) != null) {
            result = processClientArg(s);
        }
        if ((s = lastFromList(agentArgs.namedValues(APPCPATH)))  != null) {
            processAppcPath(s);
        }

        return result;
    }

    private String lastFromList(final List<String> list) {
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    private ClientLaunchType processClientArg(final String clientSpec) {
        /*
         * We are in the process of handling the agent argument
         * "client=(type)=(value).  clientSpec contains (type)=(value).
         */
        final int equalsSign = clientSpec.indexOf('=');

        final String clientType = clientSpec.substring(0, equalsSign);
        clientName = clientSpec.substring(equalsSign + 1);
        if (clientName.startsWith("\"") && clientName.endsWith("\"")) {
            clientName = clientName.substring(1, clientName.length() - 1);
        }
        return ClientLaunchType.byType(clientType);
    }

    private void processAppcPath(final String appcPath) {
        this.appcPath = appcPath;
    }

    /**
     * Represents the types of client launches.
     */
    public enum ClientLaunchType {
        JAR,
        DIR(true),
        CLASSFILE(true),
        CLASS,
        URL,
        UNKNOWN;

        private final boolean usesAppClientCommandForMainProgram;

        ClientLaunchType() {
            this(false);
        }

        ClientLaunchType(final boolean usesAppClientCommandForMainProgram) {
            this.usesAppClientCommandForMainProgram = usesAppClientCommandForMainProgram;
        }

        boolean usesAppClientCommandForMainProgram() {
            return usesAppClientCommandForMainProgram;
        }

        /**
         * Returns the ClientLaunchType for the specified launch type name.
         * @param lowerCaseType launch type name (in lower-case)
         * @return relevant ClientLaunchType for the type; null if no match
         */
        static ClientLaunchType byType(final String lowerCaseType) {
            for (ClientLaunchType t : values()) {
                if (t.name().equalsIgnoreCase(lowerCaseType)) {
                    return t;
                }
            }
            return UNKNOWN;
        }
    }
}
