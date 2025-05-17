/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static org.glassfish.appclient.client.acc.agent.CLIBootstrap.FILE_OPTIONS_INTRODUCER;

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

    private static final Logger LOG = Logger.getLogger(AppClientContainerAgent.class.getName());

    public static void premain(String agentArgsText, Instrumentation instrumentation) {
        ClassLoader loader = ClassLoader.getSystemClassLoader().getParent();
        try {
            long now = System.currentTimeMillis();

            // The agent prepares the ACC but does not launch the client.
            // The thread class loader is used in init method.
            Thread.currentThread().setContextClassLoader(loader);
            Class<?> containerInitClass = loader.loadClass("org.glassfish.appclient.client.AppClientContainerHolder");
            Method initContainer = containerInitClass.getMethod("init", String.class, Instrumentation.class);
            try {
                initContainer.invoke(null, optionsValue(agentArgsText), instrumentation);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }

            LOG.fine("AppClientContainerAgent finished after " + (System.currentTimeMillis() - now) + " ms");
        } catch (Throwable e) {
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

        final Path argsFile = new File(agentArgsText.substring(FILE_OPTIONS_INTRODUCER.length())).toPath();
        final String result = Files.readString(argsFile).trim();

        if (Boolean.getBoolean("keep.argsfile")) {
            System.err.println("Agent arguments file retained: " + argsFile);
        } else if (!Files.deleteIfExists(argsFile)) {
            LOG.log(FINE, "Unable to delete temporary args file {0}; continuing", argsFile);
        }
        return result;
    }
}
