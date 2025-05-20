/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client;

import org.glassfish.embeddable.client.ApplicationClientClassLoader;
import org.glassfish.embeddable.client.UserError;

/**
 * @author tjquinn
 */
public class AppClientFacade {

    /**
     * Prepares the ACC (if not already done by the agent) and then transfers control to the ACC.
     * <p>
     * Eventually, the Java runtime will invoke this method as the main method of the application, whether or not the
     * command line specified the Java agent. If the agent has already run, then it will have prepared the ACC already. If
     * the agent has not already run, then this method prepares it.
     * <p>
     * If the user has run the generated app client JAR directly - not using the appclient script - then the Java runtime
     * will invoke this method directly and the command-line arguments should be intended for the client only; no agent or
     * ACC settings are possible. If the user has used the appclient script, then the script will have created a Java
     * command which specifies the agent, constructs an agent argument string, and passes as command line arguments only
     * those values which should be passed to the client. The net result is that, no matter how the app client was launched,
     * the args array contains only the arguments that are for the client's consumption, without any agent or ACC arguments.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ApplicationClientClassLoader loader = (ApplicationClientClassLoader) Thread.currentThread()
                .getContextClassLoader();
            loader.getApplicationClientContainer().launch(args);
        } catch (UserError ue) {
            ue.displayAndExit();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
