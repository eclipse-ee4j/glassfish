/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.commands;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * @author sanshriv
 *
 */
final class TargetInfo {

    private boolean isDas = false;

    private String configName = null;

    public TargetInfo(Domain domain, String target) {

        Server dasServer = domain.getServerNamed(
                SystemPropertyConstants.DAS_SERVER_NAME);

        if (target != null && target.equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
            isDas = true;
        } else if (dasServer != null && dasServer.getConfigRef().equals(target)) {
            isDas = true;
        } else {
            Config config = domain.getConfigNamed(target);
            if (config != null) {
                configName = target;
            } else {
                Server targetServer = domain.getServerNamed(target);
                if (targetServer != null) {
                    if (!targetServer.isDas()) {
                        configName = targetServer.getConfigRef();
                        Cluster clusterForInstance = targetServer.getCluster();
                        if (clusterForInstance != null) {
                            configName = clusterForInstance.getConfigRef();
                        }
                    }
                } else {
                    com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
                    if (cluster != null) {
                        configName = cluster.getConfigRef();
                    }
                }
            }
        }
    }

    /**
     * @return the isDas
     */
    boolean isDas() {
        return isDas;
    }

    /**
     * @return the configName
     */
    String getConfigName() {
        return configName;
    }

}
