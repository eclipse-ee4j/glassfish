/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.main.admin.test.rest;

import jakarta.ws.rs.core.Response;

import java.util.Map;

import org.glassfish.main.itest.tools.RandomGenerator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jasonlee
 */
public class ClusterITest extends RestTestBase {

    @Test
    public void testClusterCreationAndDeletion() {
        final String clusterName = "cluster_" + RandomGenerator.generateRandomString();
        createCluster(clusterName);

        Map<String, String> entity = getEntityValues(managementClient.get(URL_CLUSTER + "/" + clusterName));
        assertEquals(clusterName + "-config", entity.get("configRef"));

        deleteCluster(clusterName);
    }

    @Test
    public void testListLifecycleModules() {
        final String clusterName = "cluster_" + RandomGenerator.generateRandomString();
        Response response = managementClient.post(URL_CLUSTER, Map.of("id", clusterName));
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.get(URL_CLUSTER + "/" + clusterName + "/list-lifecycle-modules");
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.delete(URL_CLUSTER + "/" + clusterName);
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.get(URL_CLUSTER + "/" + clusterName);
        assertEquals(404, response.getStatus());
    }
}
