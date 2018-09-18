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

package org.glassfish.nucleus.admin.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class ClusterTest extends RestTestBase {
    public static final String URL_CLUSTER = "/domain/clusters/cluster";

    @Test
    public void testClusterCreationAndDeletion() {
        final String clusterName = "cluster_" + generateRandomString();
        createCluster(clusterName);

        Map<String, String> entity = getEntityValues(get(URL_CLUSTER + "/" + clusterName));
        assertEquals(clusterName + "-config", entity.get("configRef"));

        deleteCluster(clusterName);
    }

    @Test
    public void testListLifecycleModules() {
        final String clusterName = "cluster_" + generateRandomString();
        Map<String, String> newCluster = new HashMap<String, String>() {
            {
                put("id", clusterName);
            }
        };

        Response response = post(URL_CLUSTER, newCluster);
        checkStatusForSuccess(response);

        response = get(URL_CLUSTER + "/" + clusterName + "/list-lifecycle-modules");
        checkStatusForSuccess(response);

        response = delete(URL_CLUSTER + "/" + clusterName); // + "/delete-cluster");
        checkStatusForSuccess(response);

        response = get(URL_CLUSTER + "/" + clusterName);
        checkStatusForFailure(response);

    }

    public String createCluster() {
        final String clusterName = "cluster_" + generateRandomString();
        createCluster(clusterName);

        return clusterName;
    }

    public void createCluster(final String clusterName) {
        Map<String, String> newCluster = new HashMap<String, String>() {
            {
                put("id", clusterName);
            }
        };

        Response response = post(URL_CLUSTER, newCluster);
        checkStatusForSuccess(response);
    }

    public void startCluster(String clusterName) {
        Response response = post(URL_CLUSTER + "/" + clusterName + "/start-cluster");
        checkStatusForSuccess(response);
    }

    public void stopCluster(String clusterName) {
        Response response = post(URL_CLUSTER + "/" + clusterName + "/stop-cluster");
        checkStatusForSuccess(response);
    }

    public void createClusterInstance(final String clusterName, final String instanceName) {
        Response response = post("/domain/create-instance", new HashMap<String, String>() {
            {
                put("cluster", clusterName);
                put("id", instanceName);
                put("node", "localhost-domain1");
            }
        });
        checkStatusForSuccess(response);
    }

    public void deleteCluster(String clusterName) {
        Response response = get(URL_CLUSTER + "/" + clusterName + "/list-instances");
        Map body = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map extraProperties = (Map) body.get("extraProperties");
        if (extraProperties != null) {
            List<Map<String, String>> instanceList = (List<Map<String, String>>) extraProperties.get("instanceList");
            if ((instanceList != null) && (!instanceList.isEmpty())) {
                for (Map<String, String> instance : instanceList) {
                    String status = instance.get("status");
                    String instanceName = instance.get("name");
                    if (!"NOT_RUNNING".equalsIgnoreCase(status)) {
                        response = post("/domain/servers/server/" + instanceName + "/stop-instance");
                        checkStatusForSuccess(response);
                    }
                    response = delete("/domain/servers/server/" + instanceName + "/delete-instance");
                    checkStatusForSuccess(response);
                }
            }
        }


        response = delete(URL_CLUSTER + "/" + clusterName);// + "/delete-cluster");
        checkStatusForSuccess(response);

//        response = get(URL_CLUSTER + "/" + clusterName);
//        checkStatusForFailure(response);
    }
}
