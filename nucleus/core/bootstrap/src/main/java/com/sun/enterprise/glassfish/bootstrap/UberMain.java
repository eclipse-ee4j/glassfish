/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

/**
 * This is main class for the uber jars viz., glassfish-embedded-all.jar and
 * glassfish-embedded-web.jar, to be able to do:
 * <p/>
 * <p/>java -jar glassfish-embedded-all.jar
 * <p/>java -jar glassfish-embedded-web.jar
 *
 * @author bhavanishankar@dev.java.net
 */
public class UberMain {

    GlassFish gf;

    public static void main(String... args) throws Exception {
        new UberMain().run();
    }

    public void run() throws Exception {
        addShutdownHook(); // handle Ctrt-C.

        GlassFishProperties gfProps =new GlassFishProperties();
        gfProps.setProperty("org.glassfish.embeddable.autoDelete",
                System.getProperty("org.glassfish.embeddable.autoDelete", "true"));

        gf = GlassFishRuntime.bootstrap().newGlassFish(gfProps);

        gf.start();

        CommandRunner cr = gf.getCommandRunner();

        while (true) {
            System.out.print("\n\nGlassFish $ ");
            String str = null;
            try {
                str = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset())).readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (str != null && str.trim().length() != 0) {
                if ("exit".equalsIgnoreCase(str) || "quit".equalsIgnoreCase(str)) {
                    break;
                }
                String[] split = str.split(" ");
                String command = split[0].trim();
                String[] commandParams = null;
                if (split.length > 1) {
                    commandParams = new String[split.length - 1];
                    for (int i = 1; i < split.length; i++) {
                        commandParams[i - 1] = split[i].trim();
                    }
                }
                try {
                    CommandResult result = commandParams == null ?
                            cr.run(command) : cr.run(command, commandParams);
                    System.out.print('\n');
                    System.out.println(result.getOutput());
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }

        try {
            gf.stop();
            gf.dispose();
        } catch (Exception ex) {
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(
                "GlassFish Shutdown Hook") {
            @Override
            public void run() {
                try {
                    if (gf != null) {
                        gf.stop();
                        gf.dispose();
                    }
                } catch (Exception ex) {
                }
            }
        });
    }

}
