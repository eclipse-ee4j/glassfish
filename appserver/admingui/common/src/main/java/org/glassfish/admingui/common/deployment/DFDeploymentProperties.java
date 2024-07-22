/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.common.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Convenience class for managing deployment properties - settings or options to
 * be conveyed to the back-end during deployment-related operations.
 * <p>
 * Heavily inspired by the original from common-utils but copied here to
 * minimize dependencies.
 *
 * @author tjquinn
 */
public class DFDeploymentProperties extends Properties {

    public String getWsdlTargetHint() throws IllegalArgumentException {
        return getProperty(WSDL_TARGET_HINT, null);
    }

    public void setWsdlTargetHint(String target) {
        if (target != null) {
            setProperty(WSDL_TARGET_HINT, target);
        }
    }

    public String getTarget() throws IllegalArgumentException {
        return getProperty(TARGET, null);
    }

    public void setTarget(String target) {
        if (target != null) {
            setProperty(TARGET, target);
        }
    }

    public boolean getRedeploy() {
        return Boolean.parseBoolean(getProperty(REDEPLOY, DEFAULT_REDEPLOY));
    }

    public void setRedeploy(boolean redeploy) {
        setProperty(REDEPLOY, Boolean.toString(redeploy));
    }

    public boolean getForce() {
        return Boolean.parseBoolean(getProperty(FORCE, DEFAULT_FORCE));
    }

    public void setForce(boolean force) {
        setProperty(FORCE, Boolean.toString(force));
    }

    public boolean getReload() {
        return Boolean.parseBoolean(getProperty(RELOAD, DEFAULT_RELOAD));
    }

    public void setReload(boolean reload) {
        setProperty(RELOAD, Boolean.toString(reload));
    }

    public boolean getCascade() {
        return Boolean.parseBoolean(getProperty(CASCADE, DEFAULT_CASCADE));
    }

    public void setCascade(boolean cascade) {
        setProperty(CASCADE, Boolean.toString(cascade));
    }

    public boolean getPrecompileJSP() {
        return Boolean.parseBoolean(getProperty(PRECOMPILE_JSP, DEFAULT_PRECOMPILE_JSP));
    }

    public void setPrecompileJSP(boolean precompileJSP) {
        setProperty(PRECOMPILE_JSP, Boolean.toString(precompileJSP));
    }

    public boolean getVerify() {
        return Boolean.parseBoolean(getProperty(VERIFY, DEFAULT_VERIFY));
    }

    public void setVerify(boolean verify) {
        setProperty(VERIFY, Boolean.toString(verify));
    }

    public String getVirtualServers() {
        return getProperty(VIRTUAL_SERVERS, DEFAULT_VIRTUAL_SERVERS);
    }

    public void setVirtualServers(String virtualServers) {
        if (virtualServers != null) {
            setProperty(VIRTUAL_SERVERS, virtualServers);
        }
    }

    public boolean getEnabled() {
        return Boolean.parseBoolean(getProperty(ENABLED, DEFAULT_ENABLED));
    }

    public void setEnabled(boolean enabled) {
        setProperty(ENABLED, Boolean.toString(enabled));
    }

    public String getContextRoot() {
        return getProperty(CONTEXT_ROOT, null);
    }

    public void setContextRoot(String contextRoot) {
        if (contextRoot != null) {
            setProperty(CONTEXT_ROOT, contextRoot);
        }
    }

    public String getName() {
        return getProperty(NAME);
    }

    public void setName(String name) {
        if (name != null) {
            setProperty(NAME, name);
        }
    }

    public String getDescription() {
        return getProperty(DESCRIPTION, "");
    }

    public void setDescription(String description) {
        if (description != null) {
            setProperty(DESCRIPTION, description);
        }
    }

    public boolean getGenerateRMIStubs() {
        return Boolean.parseBoolean(getProperty(GENERATE_RMI_STUBS,
                DEFAULT_GENERATE_RMI_STUBS));
    }

    public void setGenerateRMIStubs(boolean generateRMIStubs) {
        setProperty(GENERATE_RMI_STUBS,
                Boolean.toString(generateRMIStubs));
    }

    public boolean getAvailabilityEnabled() {
        return Boolean.parseBoolean(getProperty(AVAILABILITY_ENABLED,
                DEFAULT_AVAILABILITY_ENABLED));
    }

    public void setAvailabilityEnabled(boolean availabilityEnabled) {
        setProperty(AVAILABILITY_ENABLED,
                Boolean.toString(availabilityEnabled));
    }

    public boolean getJavaWebStartEnabled() {
        return Boolean.parseBoolean(getProperty(DEPLOY_OPTION_JAVA_WEB_START_ENABLED,
                DEFAULT_JAVA_WEB_START_ENABLED));
    }

    public void setJavaWebStartEnabled(boolean javaWebStartEnabled) {
        setProperty(DEPLOY_OPTION_JAVA_WEB_START_ENABLED,
                Boolean.toString(javaWebStartEnabled));
    }

    public String getLibraries() {
        return getProperty(DEPLOY_OPTION_LIBRARIES, null);
    }

    public void setLibraries(String libraries) {
        if (libraries != null) {
            setProperty(DEPLOY_OPTION_LIBRARIES, libraries);
        }
    }

    public String getResourceAction() {
        return getProperty(RESOURCE_ACTION, null);
    }

    public void setResourceAction(String resourceAction) {
        if (resourceAction != null) {
            setProperty(RESOURCE_ACTION, resourceAction);
        }
    }

    public String getResourceTargetList() {
        return getProperty(RESOURCE_TARGET_LIST, null);
    }

    public void setResourceTargetList(String resTargetList) {
        if (resTargetList != null) {
            setProperty(RESOURCE_TARGET_LIST, resTargetList);
        }
    }

    public void setUpload(boolean uploadEnabled) {
        setProperty(UPLOAD, Boolean.toString(uploadEnabled));
    }

    public boolean getUpload() {
        return Boolean.parseBoolean(getProperty(UPLOAD, DEFAULT_UPLOAD));
    }

    public void setExternallyManaged(boolean isExternallyManaged) {
        setProperty(EXTERNALLY_MANAGED, Boolean.toString(isExternallyManaged));
    }

    public void setPath(String path) {
        setProperty(PATH, path);
    }

    public String getPath() {
        return getProperty(PATH);
    }

    public boolean getExternallyManaged() {
        return Boolean.parseBoolean(getProperty(EXTERNALLY_MANAGED,
                DEFAULT_EXTERNALLY_MANAGED));
    }

    public void setProperties(Properties props) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> prop : props.entrySet()) {
            if (sb.length() > 0) {
                sb.append(PROPERTY_SEPARATOR);
            }
            sb.append(prop.getKey()).append("=").append(prop.getValue());
        }
        setProperty(PROPERTY, sb.toString());
    }

    public Properties getProperties() {
        Properties result = new Properties();
        String[] settings = getProperty(PROPERTY).split(PROPERTY_SEPARATOR);
        for (String setting : settings) {
            int equals = setting.indexOf('=');
            if (equals != -1) {
                result.setProperty(
                        setting.substring(0, equals),
                        setting.substring(equals + 1)
                );
            }
        }
        return result;
    }

    public static final String WSDL_TARGET_HINT = "wsdlTargetHint";
    public static final String TARGET = "target";
    public static final String REDEPLOY = "redeploy";
    public static final String DEFAULT_REDEPLOY = "false";
    public static final String FORCE = "force";
    public static final String DEFAULT_FORCE = "true";
    public static final String RELOAD = "reload";
    public static final String DEFAULT_RELOAD = "false";
    public static final String CASCADE = "cascade";
    public static final String DEFAULT_CASCADE = "false";
    public static final String VERIFY = "verify";
    public static final String DEFAULT_VERIFY = "false";
    public static final String VIRTUAL_SERVERS = "virtualservers";
    public static final String DEFAULT_VIRTUAL_SERVERS = null;
    public static final String PRECOMPILE_JSP = "precompilejsp";
    public static final String DEFAULT_PRECOMPILE_JSP = "false";
    public static final String GENERATE_RMI_STUBS = "generatermistubs";
    public static final String DEFAULT_GENERATE_RMI_STUBS = "false";
    public static final String AVAILABILITY_ENABLED = "availabilityenabled";
    public static final String DEFAULT_AVAILABILITY_ENABLED = "false";
    public static final String ENABLED = "enabled";
    public static final String DEFAULT_ENABLED = "true";
    public static final String CONTEXT_ROOT = "contextroot";
    public static final String ARCHIVE_NAME = "archiveName";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String DESCRIPTION = "description";
    public static final String CLIENTJARREQUESTED = "clientJarRequested";
    public static final String UPLOAD = "upload";
    public static final String EXTERNALLY_MANAGED = "externallyManaged";
    public static final String PATH = "path";
    public static final String DEFAULT_JAVA_WEB_START_ENABLED = "true";
    public static final String DEPLOYMENT_PLAN = "deploymentplan";

    public static final String PROPERTY = "property";
    private static final String PROPERTY_SEPARATOR = ":";

    public static final String DEFAULT_UPLOAD = "true";
    public static final String DEFAULT_EXTERNALLY_MANAGED = "false";
    // resource constants
    public static final String RESOURCE_ACTION = "resourceAction";
    public static final String RESOURCE_TARGET_LIST = "resourceTargetList";

    // possible values for resource action
    public static final String RES_DEPLOYMENT = "resDeployment";
    public static final String RES_CREATE_REF = "resCreateRef";
    public static final String RES_DELETE_REF = "resDeleteRef";
    public static final String RES_UNDEPLOYMENT = "resUndeployment";
    public static final String RES_REDEPLOYMENT = "resRedeployment";
    public static final String RES_NO_OP = "resNoOp";
    public static final String DEPLOY_OPTION_JAVA_WEB_START_ENABLED = "java-web-start-enabled";
    public static final String DEPLOY_OPTION_LIBRARIES = "libraries";

    // possible values for module state
    public static final String ALL = "all";
    public static final String RUNNING = "running";
    public static final String NON_RUNNING = "non-running";

    // lifecycle module constants
    public static final String LIFECYCLE_MODULE = "lifecycle-module";
    public static final String CLASS_NAME = "class-name";
    public static final String CLASSPATH = "classpath";
    public static final String LOAD_ORDER = "load-order";
    public static final String IS_FAILURE_FATAL = "is-failure-fatal";
    public static final String IS_LIFECYCLE = "isLifecycle";
    public static final String IS_COMPOSITE = "isComposite";

    public Map<String, String> asMap() {
        return new HashMap<>();
    }

}
