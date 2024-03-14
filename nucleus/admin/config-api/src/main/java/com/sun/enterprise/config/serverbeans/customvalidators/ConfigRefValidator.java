/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.logging.Logger;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.api.admin.config.Named;

public class ConfigRefValidator implements ConstraintValidator<ConfigRefConstraint, Named>, Payload {

    static final Logger logger = ConfigApiLoggerInfo.getLogger();
    static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ConfigRefValidator.class);

    public void initialize(final ConfigRefConstraint constraint) {
    }

    @Override
    public boolean isValid(final Named bean, final ConstraintValidatorContext constraintValidatorContext) {
        if (bean == null)
            return true;

        Server server = null;
        Cluster mycluster = null;
        String configRef = null;
        String serverName = null;
        if (bean instanceof Server) {
            server = (Server) bean;
            configRef = server.getConfigRef();
            serverName = server.getName();
        } else if (bean instanceof Cluster) {
            mycluster = (Cluster) bean;
            configRef = mycluster.getConfigRef();
            serverName = mycluster.getName();
        }

        if (configRef == null)
            return true; // skip validation @NotNull is already on getConfigRef

        // cannot use default-config
        if (configRef.equals(SystemPropertyConstants.TEMPLATE_CONFIG_NAME)) {
            logger.warning(ConfigApiLoggerInfo.configRefDefaultconfig);
            return false;
        }
        // cannot change config-ref of DAS
        if (server != null) {
            if (server.isDas() && !configRef.equals(SystemPropertyConstants.DAS_SERVER_CONFIG)) {
                logger.warning(ConfigApiLoggerInfo.configRefDASconfig);
                return false;
            }
            // cannot use server-config if not DAS
            if (!server.isDas() && configRef.equals(SystemPropertyConstants.DAS_SERVER_CONFIG)) {
                logger.warning(ConfigApiLoggerInfo.configRefServerconfig);
                return false;
            }

            final Servers servers = server.getParent(Servers.class);
            final Domain domain = servers.getParent(Domain.class);
            final Configs configs = domain.getConfigs();

            if (servers.getServer(serverName) != null) { // validate for set, not _register-instance
                // cannot change config ref of a clustered instance
                Cluster cluster = domain.getClusterForInstance(serverName);
                if (cluster != null) { // cluster is not null during create-local-instance --cluster c1 i1
                    if (!cluster.getConfigRef().equals(configRef)) {
                        // During set when trying to change config-ref of a clustered instance,
                        // the value of desired config-ref will be different than the current config-ref.
                        // During _register-instance, (create-local-instance --cluster c1 i1)
                        // cluster.getConfigRef().equals(configRef) will be true and not come here.
                        logger.warning(ConfigApiLoggerInfo.configRefClusteredInstance);
                        return false;
                    }
                }
                // cannot use a non-existent config  (Only used by set.  _register-instance will fail earlier)
                if (configs == null || configs.getConfigByName(configRef) == null) {
                    logger.warning(ConfigApiLoggerInfo.configRefNonexistent);
                    return false;
                }
            }
        }
        return true;
    }

}
