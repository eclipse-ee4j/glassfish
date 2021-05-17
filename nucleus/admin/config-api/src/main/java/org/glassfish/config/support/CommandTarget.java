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

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.*;

import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * CommandTarget is an enumeration of valid configuration target for a command execution
 *
 */
public enum CommandTarget implements TargetValidator {

    /**
     * a domain wide configuration change
     */
    DOMAIN {
        @Override
        public boolean isValid(ServiceLocator habitat, String target) {
            return target.equals("domain");
        }

        @Override
        public String getDescription() {
            return "Domain";
        }
    },
    /**
     * configuration change to default server
     */
    DAS {
        @Override
        public boolean isValid(ServiceLocator habitat, String target) {
            return target.equals("server");
        }

        @Override
        public String getDescription() {
            return "Default server";
        }
    },
    /**
     * a clustered instance configuration change
     */
    CLUSTERED_INSTANCE {
        @Override
        public boolean isValid(ServiceLocator habitat, String target) {
            Domain domain = habitat.getService(Domain.class);
            return (domain.getClusterForInstance(target) != null);
        }

        @Override
        public String getDescription() {
            return "Clustered Instance";
        }
    },
    /**
     * a standalone instance configuration change
     */
    STANDALONE_INSTANCE {
        @Override
        public boolean isValid(ServiceLocator habitat, String target) {
            Domain domain = habitat.getService(Domain.class);
            return (domain.getServerNamed(target) != null);
        }

        @Override
        public String getDescription() {
            return "Stand alone instance";
        }
    },
    /**
     * a config configuration change
     */
    CONFIG {
        @Override
        public boolean isValid(ServiceLocator habitat, String target) {
            Domain domain = habitat.getService(Domain.class);
            return domain.getConfigNamed(target) != null;
        }

        @Override
        public String getDescription() {
            return "Config";
        }
    },
    /**
     * a cluster configuration change
     */
    CLUSTER {
        @Override
        public boolean isValid(ServiceLocator habitat, String target) {
            Domain domain = habitat.getService(Domain.class);
            return domain.getClusterNamed(target) != null;
        }

        @Override
        public String getDescription() {
            return "Cluster";
        }
    },
    /**
     * a node configuration change
     */
    NODE {
        @Override
        public boolean isValid(ServiceLocator habitat, String target) {
            Domain domain = habitat.getService(Domain.class);
            return domain.getNodeNamed(target) != null;
        }

        @Override
        public String getDescription() {
            return "Node";
        }
    };

    @Override
    public boolean isValid(ServiceLocator habitat, String target) {
        return false;
    }

    @Override
    public String getDescription() {
        return this.name();
    }
}
