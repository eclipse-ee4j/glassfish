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
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jasonlee
 */
public class JvmOptionsTest extends RestTestBase {
    protected static final String URL_SERVER_JVM_OPTIONS = "/domain/configs/config/server-config/java-config/jvm-options";
    protected static final String URL_DEFAULT_JVM_OPTIONS = "/domain/configs/config/default-config/java-config/jvm-options";

    protected static final String URL_SERVER_CONFIG_CREATE_PROFILER = "/domain/configs/config/server-config/java-config/create-profiler";
    protected static final String URL_SERVER_CONFIG_DELETE_PROFILER = "/domain/configs/config/server-config/java-config/profiler/delete-profiler";
    protected static final String URL_SERVER_CONFIG_PROFILER_JVM_OPTIONS = "/domain/configs/config/server-config/java-config/profiler/jvm-options";

    protected static final String URL_DEFAULT_CONFIG_CREATE_PROFILER = "/domain/configs/config/default-config/java-config/create-profiler";
    protected static final String URL_DEFAULT_CONFIG_DELETE_PROFILER = "/domain/configs/config/default-config/java-config/profiler/delete-profiler";
    protected static final String URL_DEFAULT_CONFIG_PROFILER_JVM_OPTIONS = "/domain/configs/config/default-config/java-config/profiler/jvm-options";

    private ConfigTest configTest;
    private String testConfigName;
    private String URL_TEST_CONFIG;
    private String URL_TEST_CONFIG_JVM_OPTIONS;

    @BeforeMethod
    public void createConfig() {
        if (configTest == null) {
            configTest = getTestClass(ConfigTest.class);
        }

        testConfigName = "config-" + generateRandomString();
        MultivaluedMap formData = new MultivaluedHashMap() {{
            add("id", "default-config");
            add("id", testConfigName);
        }};
        configTest.createAndVerifyConfig(testConfigName, formData);

        URL_TEST_CONFIG = "/domain/configs/config/" + testConfigName;
        URL_TEST_CONFIG_JVM_OPTIONS = URL_TEST_CONFIG + "/java-config/jvm-options";
    }

    @AfterMethod
    public void deleteConfig() {
        configTest.deleteAndVerifyConfig(testConfigName);
    }


    @Test
    public void getJvmOptions() {
        Response response = get(URL_SERVER_JVM_OPTIONS);
        assertTrue(isSuccess(response));
        Map<String, Object> responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        List<String> jvmOptions = (List<String>)((Map)responseMap.get("extraProperties")).get("leafList");
        assertTrue(jvmOptions.size() > 0);
    }

    @Test
    public void createAndDeleteOptions() {
        final String option1Name = "-Doption" + generateRandomString();
        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(option1Name, "someValue");
        }};

        Response response = post(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(option1Name+"=someValue"));

        response = delete(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(option1Name+"=someValue"));
    }

    // http://java.net/jira/browse/GLASSFISH-19069
    @Test
    public void createAndDeleteOptionsWithBackslashes() {
        final String optionName = "-Dfile" + generateRandomString();
        final String optionValue = "C:\\ABC\\DEF\\";
        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(optionName, escape(optionValue));
        }};

        Response response = post(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(optionName+"="+optionValue));

        response = delete(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(optionName+"="+optionValue));
    }

    @Test
    public void createAndDeleteOptionsWithoutValues() {
        final String option1Name = "-Doption" + generateRandomString();
        final String option2Name = "-Doption" + generateRandomString();
        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(option1Name, "");
            put(option2Name, "");
        }};

        Response response = post(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(option1Name));
        assertFalse(jvmOptions.contains(option1Name+"="));
        assertTrue(jvmOptions.contains(option2Name));
        assertFalse(jvmOptions.contains(option2Name+"="));

        response = delete(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(option1Name));
        assertFalse(jvmOptions.contains(option2Name));
    }

    @Test
    public void testIsolatedOptionsCreationOnNewConfig() {
        final String optionName = "-Doption" + generateRandomString();

        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(optionName, "");
            put("target", testConfigName);
        }};

        // Test new config to make sure option is there
        Response response = post(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(optionName));

        // Test server-config to make sure the options are NOT there
        response = get(URL_SERVER_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(optionName));
    }

    @Test
    public void testProfilerJvmOptions() {
        final String profilerName = "profiler" + generateRandomString();
        final String optionName = "-Doption" + generateRandomString();
        Map<String, String> attrs = new HashMap<String, String>() {{
            put("name", profilerName);
            put("target", testConfigName);
        }};
        Map<String, String> newOptions = new HashMap<String, String>() {{
//            put("target", testConfigName);
//            put("profiler", "true");
            put(optionName, "");
        }};

        deleteProfiler(URL_TEST_CONFIG + "/java-config/profiler/delete-profiler", testConfigName, false);

        Response response = post(URL_TEST_CONFIG + "/java-config/create-profiler", attrs);
        assertTrue(isSuccess(response));

        response = post(URL_TEST_CONFIG + "/java-config/profiler/jvm-options", newOptions);
        assertTrue(isSuccess(response));

        response = get(URL_TEST_CONFIG + "/java-config/profiler/jvm-options");
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(optionName));

        deleteProfiler(URL_TEST_CONFIG + "/java-config/profiler/delete-profiler", testConfigName, true);
    }

    @Test
    public void testJvmOptionWithColon() {
        final String optionName = "-XX:MaxPermSize";
        final String optionValue = "152m";
        Map<String, String> newOptions = new HashMap<String, String>() {{
            put(escape(optionName), optionValue);
        }};

        Response response = post(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
//        assertTrue(isSuccess(response));
        List<String> jvmOptions = getJvmOptions(response);
        assertTrue(jvmOptions.contains(optionName+"="+optionValue));

        response = delete(URL_TEST_CONFIG_JVM_OPTIONS, newOptions);
        assertTrue(isSuccess(response));
        response = get(URL_TEST_CONFIG_JVM_OPTIONS);
        jvmOptions = getJvmOptions(response);
        assertFalse(jvmOptions.contains(optionName+"="+optionValue));
    }

    protected void deleteProfiler(final String url, final String target, final boolean failOnError) {
        Response response = delete (url, new HashMap() {{ put ("target", target); }});
        if (failOnError) {
            assertTrue(isSuccess(response));
        }
    }

    protected List<String> getJvmOptions(Response response) {
        Map<String, Object> responseMap = MarshallingUtils.buildMapFromDocument(response.readEntity(String.class));
        List<String> jvmOptions = (List<String>)((Map)responseMap.get("extraProperties")).get("leafList");

        return jvmOptions;
    }

    protected String escape(String part) {
        String changed = part
                .replace("\\", "\\\\")
                .replace(":", "\\:");
        return changed;
    }
}
