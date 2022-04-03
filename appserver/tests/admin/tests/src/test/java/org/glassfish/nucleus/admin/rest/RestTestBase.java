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

package org.glassfish.nucleus.admin.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.glassfish.admin.rest.client.ClientWrapper;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.nucleus.test.tool.DomainLifecycleExtension;
import org.glassfish.nucleus.test.tool.NucleusTestUtils;
import org.glassfish.nucleus.test.webapp.HelloServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Document;

import static org.glassfish.nucleus.test.tool.NucleusTestUtils.ADMIN_PASSWORD;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.ADMIN_USER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DomainLifecycleExtension.class)
public class RestTestBase {

    private static final Logger LOG = Logger.getLogger(RestTestBase.class.getName());

    protected static final String RESPONSE_TYPE = MediaType.APPLICATION_JSON;

    protected static final String CONTEXT_ROOT_MANAGEMENT = "management";

    protected static final String URL_CLUSTER = "domain/clusters/cluster";
    protected static final String URL_APPLICATION_DEPLOY = "domain/applications/application";
    protected static final String URL_CREATE_INSTANCE = "domain/create-instance";
    protected static final String URL_CONFIGS = "domain/configs";
    protected static final String URL_JDBC_RESOURCE = "domain/resources/jdbc-resource";
    protected static final String URL_JDBC_CONNECTION_POOL = "domain/resources/jdbc-connection-pool";

    private static String adminHost;
    private static String adminPort;
    private static String instancePort;
    private static String baseAdminUrl;
    private static String baseInstanceUrl;

    private static String currentTestClass;
    private Client client;

    @BeforeAll
    public static void initialize() {
        adminPort = getParameter("admin.port", "4848");
        instancePort = getParameter("instance.port", "8080");
        adminHost = getParameter("instance.host", "localhost");
        baseAdminUrl = "http://" + adminHost + ':' + adminPort + '/';
        baseInstanceUrl = "http://" + adminHost + ':' + instancePort + '/';

        final RestTestBase rtb = new RestTestBase();
        rtb.get("domain/rotate-log");
    }

    @AfterAll
    public static void captureLog() {
        try {
            if (currentTestClass != null) {
                RestTestBase rtb = new RestTestBase();
                Client client = new ClientWrapper(new HashMap<String, String>(), ADMIN_USER, ADMIN_PASSWORD);
                Response response = client.target(rtb.getAddress("domain/view-log")).request().get(Response.class);
                File directory = NucleusTestUtils.BASEDIR.toPath().resolve(Path.of("target", "surefire-reports")).toFile();
                directory.mkdirs();
                File output = new File(directory, currentTestClass + "-server.log");
                try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
                    out.write(response.readEntity(String.class));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RestTestBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @BeforeEach
    public void setup() {
        currentTestClass = this.getClass().getName();
    }

    @AfterEach
    protected void resetClient() {
        if (client == null) {
            return;
        }
        client.close();
        client = null;
    }

    public void createAndVerifyConfig(String configName, MultivaluedMap<String, String> configData) {
        Response response = post(URL_CONFIGS + "/copy-config", configData);
        assertThat(response.getStatus(), equalTo(200));

        response = get(URL_CONFIGS + "/config/" + configName);
        assertEquals(200, response.getStatus());
    }

    public void deleteAndVerifyConfig(String configName) {
        Response response = post(URL_CONFIGS + "/config/" + configName + "/delete-config");
        assertThat(response.getStatus(), equalTo(200));

        response = get(URL_CONFIGS + "/config/" + configName);
        assertEquals(404, response.getStatus());
    }

    public String createCluster() {
        final String clusterName = "cluster_" + generateRandomString();
        createCluster(clusterName);
        return clusterName;
    }

    public void createCluster(final String clusterName) {
        Map<String, String> newCluster = Map.of("id", clusterName);
        Response response = post(URL_CLUSTER, newCluster);
        assertThat(response.getStatus(), equalTo(200));
    }

    public void startCluster(String clusterName) {
        Response response = post(URL_CLUSTER + "/" + clusterName + "/start-cluster");
        assertThat(response.getStatus(), equalTo(200));
    }

    public void stopCluster(String clusterName) {
        Response response = post(URL_CLUSTER + "/" + clusterName + "/stop-cluster");
        assertThat(response.getStatus(), equalTo(200));
    }

    public void createClusterInstance(final String clusterName, final String instanceName) {
        Response response = post("domain/create-instance",
            Map.of("cluster", clusterName, "id", instanceName, "node", "localhost-domain1"));
        assertThat(response.getStatus(), equalTo(200));
    }

    public void deleteCluster(String clusterName) {
        Response response = get(URL_CLUSTER + "/" + clusterName + "/list-instances");
        Map body = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map extraProperties = (Map) body.get("extraProperties");
        if (extraProperties != null) {
            List<Map<String, String>> instanceList = (List<Map<String, String>>) extraProperties.get("instanceList");
            if (instanceList != null && !instanceList.isEmpty()) {
                for (Map<String, String> instance : instanceList) {
                    String status = instance.get("status");
                    String instanceName = instance.get("name");
                    if (!"NOT_RUNNING".equalsIgnoreCase(status)) {
                        response = post("domain/servers/server/" + instanceName + "/stop-instance");
                        assertThat(response.getStatus(), equalTo(200));
                    }
                    response = delete("domain/servers/server/" + instanceName + "/delete-instance");
                    assertThat(response.getStatus(), equalTo(200));
                }
            }
        }
        response = delete(URL_CLUSTER + "/" + clusterName);
        assertEquals(200, response.getStatus());
        response = get(URL_CLUSTER + "/" + clusterName);
        assertEquals(404, response.getStatus());
    }

    public Map<String, String> deployApp(final File archive, final String contextRoot, final String name) {
        Map<String, Object> app = Map.of(
            "id", archive,
            "contextroot", contextRoot,
            "name", name
        );
        Response response = postWithUpload(URL_APPLICATION_DEPLOY, app);
        assertThat(response.getStatus(), equalTo(200));
        return getEntityValues(get(URL_APPLICATION_DEPLOY + "/" + app.get("name")));
    }

    public void addAppRef(final String applicationName, final String targetName){
        Response response = post("domain/servers/server/" + targetName + "/application-ref",
            Map.of("id", applicationName, "target", targetName));
        assertThat(response.getStatus(), equalTo(200));
    }

    public Response undeployApp(String appName) {
        Response response = delete(URL_APPLICATION_DEPLOY + "/" + appName);
        assertThat(response.getStatus(), equalTo(200));
        return response;
    }

    protected <T> T getTestClass(Class<T> clazz) {
        try {
            T test = clazz.getDeclaredConstructor().newInstance();
            ((RestTestBase) test).setup();
            return test;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected static String generateRandomString() {
        SecureRandom random = new SecureRandom();

        return new BigInteger(130, random).toString(16);
    }

    protected static int generateRandomNumber() {
        Random r = new Random();
        return Math.abs(r.nextInt()) + 1;
    }

    protected int generateRandomNumber(int max) {
        Random r = new Random();
        return Math.abs(r.nextInt(max - 1)) + 1;
    }

    protected String getResponseType() {
        return RESPONSE_TYPE;
    }


    protected static String getBaseAdminUrl() {
        return baseAdminUrl;
    }

    protected static String getBaseInstanceUrl() {
        return baseInstanceUrl;
    }


    protected String getContextRoot() {
        return CONTEXT_ROOT_MANAGEMENT;
    }

    protected String getAddress(String address) {
        if (address.startsWith("http://")) {
            return address;
        }

        return baseAdminUrl + getContextRoot() + '/' + address;
    }

    protected Client getClient() {
        if (client == null) {
            client = new ClientWrapper(new HashMap<String, String>(), ADMIN_USER, ADMIN_PASSWORD);
            client.register(LoggingFeature.builder().withLogger(Logger.getLogger("CLIENT")).level(Level.FINE).verbosity(Verbosity.PAYLOAD_TEXT).separator("\n").build());
        }
        return client;
    }

    protected Response get(String address) {
        return get(address, new HashMap<String, String>());
    }

    protected Response get(String address, Map<String, String> payload) {
        WebTarget target = getClient().target(getAddress(address));
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        return target.request(getResponseType())
                .get(Response.class);
    }

    protected Response options(String address) {
        return getClient().target(getAddress(address)).
                request(getResponseType()).
                options(Response.class);
    }

    protected Response post(String address, Map<String, String> payload) {
        return post(address, buildMultivaluedMap(payload));
    }

    protected Response post(String address, MultivaluedMap<String, String> payload) {
        return getClient().target(getAddress(address)).
            request(getResponseType()).
            post(Entity.entity(payload, MediaType.APPLICATION_FORM_URLENCODED), Response.class);
    }

    protected Response post(String address) {
        return getClient().target(getAddress(address)).
                request(getResponseType()).
                post(Entity.entity(null, MediaType.APPLICATION_FORM_URLENCODED), Response.class);
    }

    protected Response put(String address, Map<String, String> payload) {
        return getClient().target(getAddress(address)).
                request(getResponseType()).
                put(Entity.entity(buildMultivaluedMap(payload), MediaType.APPLICATION_FORM_URLENCODED), Response.class);
    }

    protected Response put(String address) {
        return getClient().target(getAddress(address)).
                request(getResponseType()).
                put(Entity.entity(null, MediaType.APPLICATION_FORM_URLENCODED), Response.class);
    }

    protected Response postWithUpload(String address, Map<String, Object> payload) {
        FormDataMultiPart form = new FormDataMultiPart();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (entry.getValue() instanceof File) {
                form.getBodyParts().
                        add((new FileDataBodyPart(entry.getKey(), (File) entry.getValue())));
            } else {
                form.field(entry.getKey(), entry.getValue(), MediaType.TEXT_PLAIN_TYPE);
            }
        }
        return getClient().target(getAddress(address)).
                request(getResponseType()).
                post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA), Response.class);
    }

    protected Response delete(String address) {
        return delete(address, new HashMap<String, String>());
    }

    protected Response delete(String address, Map<String, String> payload) {
        WebTarget target = getClient().target(getAddress(address));
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            target = target.queryParam(entry.getKey(), entry.getValue());
        }
        return target.request(getResponseType()).delete(Response.class);
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

    public Document getDocument(String input) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(input.getBytes()));
            return doc;
        } catch (Exception ex) {
            return null;
        }
    }

    public List<Map<String, String>> getProperties(Response response) {
        Map responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        Map extraProperties = (Map) responseMap.get("extraProperties");
        if (extraProperties != null) {
            return (List<Map<String, String>>) extraProperties.get("properties");
        }
        return new ArrayList<>();
    }

    private MultivaluedMap<String, String> buildMultivaluedMap(Map<String, String> payload) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        if (payload != null) {
            for (final Entry<String, String> entry : payload.entrySet()) {
                formData.add(entry.getKey(), entry.getValue());
            }
        }
        return formData;
    }

    protected String getErrorMessage(Response cr) {
        String message = null;
        Map map = MarshallingUtils.buildMapFromDocument(cr.readEntity(String.class));
        if (map != null) {
            message = (String) map.get("message");
        }

        return message;
    }

    protected static String getParameter(String paramName, String defaultValue) {
        String value = System.getenv(paramName);
        if (value == null) {
            value = System.getProperty(paramName);
        }
        if (value == null) {
            value = defaultValue;
        }

        return value;
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
