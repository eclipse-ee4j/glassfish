/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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

/**
 * @author tjquinn
 */
public class TestMain {
    private static final String JAR_CLIENT_NAME = "there/myClient.jar";
    private static final String USER_VALUE = "joe-the-user";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final AgentArguments agentArgs = AgentArguments.newInstance(
            "mode=acscript" + ",client=jar=" + JAR_CLIENT_NAME + ",arg=-textauth" + ",arg=-user,arg=" + USER_VALUE);
        CommandLaunchInfo.newInstance(agentArgs);
    }

}
