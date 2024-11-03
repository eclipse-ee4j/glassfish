/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.ant.embedded.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;


public class Util {

    private final static Map<String, GlassFish> gfMap =
            new HashMap<String, GlassFish>();

    private static GlassFishRuntime glassfishRuntime;

    public static synchronized GlassFish startGlassFish(String serverID, String installRoot,
                                                        String instanceRoot, String configFile,
                                                        boolean configFileReadOnly, int httpPort)
            throws GlassFishException {
        GlassFish glassfish = gfMap.get(serverID);
        if (glassfish != null) {
            return glassfish;
        }
        if (glassfishRuntime == null) {
            BootstrapProperties bootstrapProperties = new BootstrapProperties();
            if (installRoot != null) {
                bootstrapProperties.setInstallRoot(installRoot);
            }
            glassfishRuntime = GlassFishRuntime.bootstrap(bootstrapProperties);
        }

        GlassFishProperties glassfishProperties = new GlassFishProperties();
        if (instanceRoot != null) {
            glassfishProperties.setInstanceRoot(instanceRoot);
        }
        if (configFile != null) {
            glassfishProperties.setConfigFileURI(new File(configFile).toURI().toString());
            glassfishProperties.setConfigFileReadOnly(configFileReadOnly);
        }

        if (instanceRoot == null && configFile == null) {
            // only set port if embedded domain.xml is used
            if (httpPort != -1) {
                glassfishProperties.setPort("http-listener", httpPort);
            }
        }

        glassfish = glassfishRuntime.newGlassFish(glassfishProperties);
        glassfish.start();

        gfMap.put(serverID, glassfish);

        System.out.println("Started GlassFish [" + serverID + "]");

        return glassfish;
    }

    public static void deploy(String app, String serverId, List<String> deployParams)
            throws Exception {
        GlassFish glassfish = gfMap.get(serverId);
        if (glassfish == null) {
            throw new Exception("Embedded GlassFish [" + serverId + "] not running");
        }
        if (app == null) {
            throw new Exception("Application can not be null");
        }
        Deployer deployer = glassfish.getDeployer();
        final int len = deployParams.size();
        if (len > 0) {
            deployer.deploy(new File(app).toURI(), deployParams.toArray(String[]::new));
            System.out.println("Deployed [" + app + "] with parameters " + deployParams);
        } else {
            deployer.deploy(new File(app).toURI());
            System.out.println("Deployed [" + app + "]");
        }
    }

    public static void undeploy(String appName, String serverId) throws Exception {
        GlassFish glassfish = gfMap.get(serverId);
        if (glassfish == null) {
            throw new Exception("Embedded GlassFish [" + serverId + "] not running");
        }
        if (appName == null) {
            throw new Exception("Application name can not be null");
        }
        Deployer deployer = glassfish.getDeployer();
        deployer.undeploy(appName);
        System.out.println("Undeployed [" + appName + "]");
    }

    public static void runCommand(String commandLine, String serverId) throws Exception {
        GlassFish glassfish = gfMap.get(serverId);
        if (glassfish == null) {
            throw new Exception("Embedded GlassFish [" + serverId + "] not running");
        }
        if (commandLine == null) {
            throw new Exception("Command can not be null");
        }
        String[] split = commandLine.split(" ");
        String command = split[0].trim();
        String[] commandParams = null;
        if (split.length > 1) {
            commandParams = new String[split.length - 1];
            for (int i = 1; i < split.length; i++) {
                commandParams[i - 1] = split[i].trim();
            }
        }
        CommandRunner cr = glassfish.getCommandRunner();
        CommandResult result = commandParams == null ?
                cr.run(command) : cr.run(command, commandParams);
        System.out.println("Executed command [" + commandLine +
                "]. Output : \n" + result.getOutput());
    }

    public static synchronized void disposeGlassFish(String serverID)
            throws GlassFishException {
        GlassFish glassfish = gfMap.remove(serverID);
        if (glassfish != null) {
            glassfish.dispose();
            System.out.println("Stopped GlassFish [" + serverID + "]");
        }
    }

}
