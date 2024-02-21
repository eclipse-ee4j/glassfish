/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.util.io.FileUtils;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.main.admin.test.webapp.HelloServlet;
import org.glassfish.main.itest.tools.DomainAdminRestClient;
import org.glassfish.main.itest.tools.RandomGenerator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import static com.sun.enterprise.util.io.FileUtils.ensureWritableDir;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getTargetDirectory;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestTestBase {

    private static final Logger LOG = Logger.getLogger(RestTestBase.class.getName());

    protected static final String CONTEXT_ROOT_MANAGEMENT = "/management";

    protected static final String URL_DOMAIN = "/domain";
    protected static final String URL_CLUSTER = "/domain/clusters/cluster";
    protected static final String URL_APPLICATION_DEPLOY = "/domain/applications/application";
    protected static final String URL_CREATE_INSTANCE = "/domain/create-instance";
    protected static final String URL_CONFIGS = "/domain/configs";
    protected static final String URL_JDBC_RESOURCE = "/domain/resources/jdbc-resource";
    protected static final String URL_JDBC_CONNECTION_POOL = "/domain/resources/jdbc-connection-pool";

    private static String baseAdminUrl;
    private static String baseInstanceUrl;
    protected static DomainAdminRestClient managementClient;

    private Client client;

    @BeforeAll
    public static void initialize() {
        baseAdminUrl = "http://localhost:4848";
        baseInstanceUrl = "http://localhost:8080";
        managementClient = new DomainAdminRestClient(baseAdminUrl + CONTEXT_ROOT_MANAGEMENT);
        Response response = managementClient.post("/domain/rotate-log");
        assertThat(response.getStatus(), equalTo(200));
    }

    @AfterAll
    public static void captureLogAndCloseClient(final TestInfo testInfo) throws Exception {
        if (managementClient != null) {
            managementClient.close();
        }
        try (DomainAdminRestClient client = new DomainAdminRestClient(baseAdminUrl + CONTEXT_ROOT_MANAGEMENT, TEXT_PLAIN)) {
            Response response = client.get("/domain/view-log");
            assertThat(response.getStatus(), equalTo(200));

            File reportDir = new File(getTargetDirectory(), "surefire-reports");
            ensureWritableDir(reportDir);

            File reportFile = new File(reportDir, testInfo.getTestClass().orElseThrow().getName() + "-server.log");
            try (InputStream readEntity = response.readEntity(InputStream.class)) {
                FileUtils.copy(readEntity, reportFile);
            }
        }
    }

    @AfterEach
    protected void closeClient() throws Exception {
        if (client == null) {
            return;
        }
        client.close();
        client = null;
    }

    public void createAndVerifyConfig(String configName, MultivaluedMap<String, String> configData) {
        Response response = managementClient.post(URL_CONFIGS + "/copy-config", configData);
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.get(URL_CONFIGS + "/config/" + configName);
        assertThat(response.getStatus(), equalTo(200));
    }

    public void deleteAndVerifyConfig(String configName) {
        Response response = managementClient.post(URL_CONFIGS + "/config/" + configName + "/delete-config");
        assertThat(response.getStatus(), equalTo(200));

        response = managementClient.get(URL_CONFIGS + "/config/" + configName);
        assertThat(response.getStatus(), equalTo(404));
    }

    public String createCluster() {
        final String clusterName = "cluster_" + RandomGenerator.generateRandomString();
        createCluster(clusterName);
        return clusterName;
    }

    public void createCluster(final String clusterName) {
        Map<String, String> newCluster = Map.of("id", clusterName);
        Response response = managementClient.post(URL_CLUSTER, newCluster);
        assertThat(response.getStatus(), equalTo(200));
    }

    public void startCluster(String clusterName) {
        Response response = managementClient.post(URL_CLUSTER + "/" + clusterName + "/start-cluster");
        assertThat(response.getStatus(), equalTo(200));
    }

    public void stopCluster(String clusterName) {
        Response response = managementClient.post(URL_CLUSTER + "/" + clusterName + "/stop-cluster");
        assertThat(response.getStatus(), equalTo(200));
    }

    public void createClusterInstance(final String clusterName, final String instanceName) {
        Response response = managementClient.post("/domain/create-instance",
            Map.of("cluster", clusterName, "id", instanceName, "node", "localhost-domain1"));
        assertThat(response.getStatus(), equalTo(200));
    }

    public void deleteCluster(String clusterName) {
        Response response = managementClient.get(URL_CLUSTER + "/" + clusterName + "/list-instances");
        Map<String, ?> body = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map<String, ?> extraProperties = (Map<String, ?>) body.get("extraProperties");
        if (extraProperties != null) {
            List<Map<String, String>> instanceList = (List<Map<String, String>>) extraProperties.get("instanceList");
            LOG.log(Level.INFO, "Found instances: {0}", instanceList);
            if (instanceList != null && !instanceList.isEmpty()) {
                for (Map<String, String> instance : instanceList) {
                    String instanceName = instance.get("name");
                    response = managementClient.post("/domain/servers/server/" + instanceName + "/stop-instance");
                    assertThat(instanceName, response.getStatus(), equalTo(200));
                    response = managementClient.delete("/domain/servers/server/" + instanceName + "/delete-instance");
                    assertThat(instanceName, response.getStatus(), equalTo(200));
                }
            }
        }
        response = managementClient.delete(URL_CLUSTER + "/" + clusterName);
        assertEquals(200, response.getStatus());
        response = managementClient.get(URL_CLUSTER + "/" + clusterName);
        assertEquals(404, response.getStatus());
    }

    /**
     * Arguments contextRoot and name can be null, then they are not set in the deploy command
     */
    public Map<String, String> deployApp(final File archive, final String contextRoot, final String name) {
        Map<String, Object> app = new HashMap<>();
        app.put("id", archive);
        putIfNotNull("contextroot", contextRoot, app);
        putIfNotNull("name", name, app);

        Response response = managementClient.postWithUpload(URL_APPLICATION_DEPLOY, app);
        assertThat(response.getStatus(), equalTo(200));
        return getEntityValues(managementClient.get(URL_APPLICATION_DEPLOY + "/" + app.get("name")));
    }

    private void putIfNotNull(final String key, Object value, Map<String, Object> app) {
        if (value != null) {
            app.put(key, value);
        }
    }

    public void addAppRef(final String applicationName, final String targetName){
        Response response = managementClient.post("/domain/servers/server/" + targetName + "/application-ref",
            Map.of("id", applicationName, "target", targetName));
        assertThat(response.getStatus(), equalTo(200));
    }

    public Response undeployApp(String appName) {
        Response response = managementClient.delete(URL_APPLICATION_DEPLOY + "/" + appName);
        assertThat(response.getStatus(), equalTo(200));
        return response;
    }

    protected static String getBaseAdminUrl() {
        return baseAdminUrl;
    }

    protected static String getBaseInstanceUrl() {
        return baseInstanceUrl;
    }


    /**
     * This method will parse the provided XML document and return a map of the attributes and values on the root
     * element
     *
     * @param response
     * @return
     */
    protected Map<String, String> getEntityValues(Response response) {
        String xml = response.readEntity(String.class);
        Map<String, Object> responseMap = MarshallingUtils.buildMapFromDocument(xml);
        if (responseMap == null) {
            return null;
        }
        Map<String, Object> obj = (Map<String, Object>) responseMap.get("extraProperties");
        if (obj == null) {
            return null;
        }
        return (Map<String, String>) obj.get("entity");
    }

    protected List<String> getCommandResults(Response response) {
        String document = response.readEntity(String.class);
        List<String> results = new ArrayList<>();
        Map map = MarshallingUtils.buildMapFromDocument(document);
        String message = (String) map.get("message");
        if (message != null && !"".equals(message)) {
            results.add(message);
        }
        Object children = map.get("children");
        if (children instanceof List) {
            for (Object child : (List) children) {
                Map childMap = (Map) child;
                message = (String) childMap.get("message");
                if (message != null) {
                    results.add(message);
                }
            }
        }
        return results;
    }

    protected Map<String, String> getChildResources(Response response) {
        Map responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        LOG.log(Level.INFO, "responseMap: \n{0}", responseMap);
        Map<String, Map> extraProperties = (Map<String, Map>) responseMap.get("extraProperties");
        if (extraProperties != null) {
            return extraProperties.get("childResources");
        }

        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getExtraProperties(Response response) {
        Map<String, Object> responseEntity = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        if (responseEntity == null) {
            return null;
        }
        return (Map<String, Object>) responseEntity.get("extraProperties");
    }

    protected List<Map<String, String>> getProperties(Response response) {
        Map responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map extraProperties = (Map) responseMap.get("extraProperties");
        if (extraProperties != null) {
            return (List<Map<String, String>>) extraProperties.get("properties");
        }
        return new ArrayList<>();
    }


    protected static File getEar(final String appName) {
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class).addAsModule(getWar(appName), "simple.war");
        LOG.info(ear.toString(true));
        try {
            File tempFile = File.createTempFile(appName, ".ear");
            ear.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Deployment failed - cannot load the input archive!", e);
        }
    }

    protected static File getWar(final String appName) {
        final WebArchive war = ShrinkWrap.create(WebArchive.class).addPackage(HelloServlet.class.getPackage());
        LOG.info(war.toString(true));
        try {
            File tempFile = File.createTempFile(appName, ".war");
            war.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Deployment failed - cannot load the input archive!", e);
        }
    }
}
