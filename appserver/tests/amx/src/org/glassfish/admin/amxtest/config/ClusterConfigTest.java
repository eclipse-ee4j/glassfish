/*
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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.DomainConfig;
import org.glassfish.admin.amxtest.ClusterSupportRequired;

import java.util.Map;

/**
 Test the creation/removal of ClusterConfig
 */
public final class ClusterConfigTest
        extends ConfigMgrTestBase
        implements ClusterSupportRequired {
    public ClusterConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("test-cluster");
    }

    public static ClusterConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        ClusterConfig result =
                domainConfig.getClustersConfig().getClusterConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = domainConfig.getClustersConfig().createClusterConfig(
                    getDefaultInstanceName(), null, null);
        }

        return result;
    }

    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.CLUSTER_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getDomainConfig().getClustersConfig().removeClusterConfig(name);
    }


    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        assert (name != null && name.length() >= 1);
        return getDomainConfig().getClustersConfig().createClusterConfig(name, null, options);
    }

    public void
    testCreateWithIllegalConfig() {
        try {
            getDomainConfig().getClustersConfig().createClusterConfig("dummy-cluster-1", "no such config", null);
            failure("expecting failure creating cluster with illegal config name");

            getDomainConfig().getClustersConfig().createClusterConfig("dummy-cluster-2", "server-config", null);
            failure("expecting failure creating cluster with name 'server-config'");

            getDomainConfig().getClustersConfig().createClusterConfig("dummy-cluster-2", "default-config", null);
            failure("expecting failure creating name 'default-config'");
        }
        catch (Exception e) {
            // good
        }
    }
}








