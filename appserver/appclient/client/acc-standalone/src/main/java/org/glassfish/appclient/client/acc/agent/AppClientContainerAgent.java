/*
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

package org.glassfish.appclient.client.acc.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import org.glassfish.appclient.client.AppClientFacade;
import org.glassfish.appclient.client.acc.UserError;

import static java.util.logging.Level.FINE;
import static org.glassfish.appclient.client.CLIBootstrap.FILE_OPTIONS_INTRODUCER;

/**
 * Agent which prepares the ACC before the VM launches the selected main program.
 *
 * <p>
 * This agent gathers processes agent arguments, supplied either by the appclient script or the end-user (when entering
 * a java command directly), and processes those arguments. The primary purpose is to:
 * <ol>
 *   <li>identify the main class that the Java launcher has decided to start,
 *   <li>create and initialize a new app client container instance, asking the ACC to load and inject the indicated main
 *       class in the process <b>if and only if</b> the main class is not the AppClientCommand class.
 * </ol>
 *
 * Then the agent is done. The java launcher and the VM see to it that the main class's main method is invoked.
 *
 * @author tjquinn
 */
public class AppClientContainerAgent {

    private static Logger logger = Logger.getLogger(AppClientContainerAgent.class.getName());

    public static void premain(String agentArgsText, Instrumentation instrumentation) {
        try {
            long now = System.currentTimeMillis();

            /*
             * The agent prepares the ACC but does not launch the client.
             */
            AppClientFacade.prepareACC(optionsValue(agentArgsText), instrumentation);

            logger.fine("AppClientContainerAgent finished after " + (System.currentTimeMillis() - now) + " ms");

        } catch (UserError ue) {
            ue.displayAndExit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static String optionsValue(final String agentArgsText) throws FileNotFoundException, IOException {
        if (agentArgsText == null) {
            throw new IllegalArgumentException();
        }

        if (!agentArgsText.startsWith(FILE_OPTIONS_INTRODUCER)) {
            return agentArgsText;
        }

        File argsFile = new File(agentArgsText.substring(FILE_OPTIONS_INTRODUCER.length()));
        String result;

        try (LineNumberReader reader = new LineNumberReader(new FileReader(argsFile))) {
            result = reader.readLine();
        }

        if (Boolean.getBoolean("keep.argsfile")) {
            System.err.println("Agent arguments file retained: " + argsFile.getAbsolutePath());
        } else if (!argsFile.delete()) {
            logger.log(FINE, "Unable to delete temporary args file {0}; continuing", argsFile.getAbsolutePath());
        }

        return result;
    }
}
