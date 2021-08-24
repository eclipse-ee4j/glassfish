/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.configapi.tests.cluster;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.configapi.tests.ConfigApiTest;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test a number of cluster related {@link org.jvnet.hk2.config.DuckTyped}
 * methods implementation
 *
 * @author Jerome Dochez
 */
public class DuckMethodsTest extends ConfigApiTest {
    private ServiceLocator locator;

    @Override
    public String getFileName() {
        return "ClusterDomain";
    }

    @BeforeEach
    public void setup() {
        locator = Utils.instance.getHabitat(this);
    }


    @Test
    public void getClusterFromServerTest() {
        Domain d = locator.getService(Domain.class);
        Server server = d.getServerNamed("server");
        assertNotNull(server);
        Cluster cluster = server.getCluster();
        assertEquals("clusterA", cluster.getName());
    }
}
