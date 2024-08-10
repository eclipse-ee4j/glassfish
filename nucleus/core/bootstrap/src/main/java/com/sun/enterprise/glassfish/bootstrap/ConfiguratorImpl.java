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

package com.sun.enterprise.glassfish.bootstrap;

import java.util.Properties;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author bhavanishankar@dev.java.net
 */
class ConfiguratorImpl implements Configurator {

    ServiceLocator habitat;

    private static final String CONFIG_PROP_PREFIX = "embedded-glassfish-config.";

    public ConfiguratorImpl(ServiceLocator habitat) {
        this.habitat = habitat;
    }

    @Override
    public void configure(Properties props) throws GlassFishException {
        CommandRunner commandRunner = null;
        for (Object obj : props.keySet()) {
            String key = (String) obj;
            if (key.startsWith(CONFIG_PROP_PREFIX)) {
                if (commandRunner == null) {
                    // only create the CommandRunner if needed
                    commandRunner = habitat.getService(CommandRunner.class);
                }
                CommandResult result = commandRunner.run("set",
                        key.substring(CONFIG_PROP_PREFIX.length()) + "=" + props.getProperty(key));
                if (result.getExitStatus() != CommandResult.ExitStatus.SUCCESS) {
                    throw new GlassFishException(result.getOutput());
                }
            }
        }
    }
}
