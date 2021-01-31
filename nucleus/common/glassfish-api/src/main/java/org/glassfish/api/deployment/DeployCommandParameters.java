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

package org.glassfish.api.deployment;

import java.io.File;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;

/**
 * Parameters passed by the user to a deployment request.
 */
public class DeployCommandParameters extends OpsParams {

    @Param(optional = true)
    public String name = null;

    @Param(name = ParameterNames.CONTEXT_ROOT, optional = true)
    public String contextroot = null;

    public String getContextRoot() {
        return contextroot;
    }

    public void setContextRoot(String val) {
        contextroot = val;
    }

    @Param(name = ParameterNames.VIRTUAL_SERVERS, optional = true)
    @I18n("virtualservers")
    public String virtualservers = null;

    public String getVirtualServers() {
        return virtualservers;
    }

    @Param(name = ParameterNames.LIBRARIES, optional = true)
    public String libraries = null;

    @Param(optional = true, defaultValue = "false")
    public Boolean force = false;

    public Boolean isForce() {
        return force;
    }

    @Param(name = ParameterNames.PRECOMPILE_JSP, optional = true, defaultValue = "false")
    public Boolean precompilejsp = false;

    public Boolean isPrecompileJsp() {
        return precompilejsp;
    }

    @Param(optional = true, defaultValue = "false")
    public Boolean verify = false;

    public Boolean isVerify() {
        return verify;
    }

    @Param(optional = true)
    public String retrieve = null;

    public String getRetrieve() {
        return retrieve;
    }

    @Param(optional = true)
    public String dbvendorname = null;

    public String getDBVendorName() {
        return dbvendorname;
    }

    // mutually exclusive with dropandcreatetables
    @Param(optional = true)
    public Boolean createtables;

    // mutually exclusive with createtables
    @Param(optional = true)
    public Boolean dropandcreatetables;

    @Param(optional = true)
    public Boolean uniquetablenames;

    @Param(name = ParameterNames.DEPLOYMENT_PLAN, optional = true)
    public File deploymentplan = null;

    public File getDeploymentPlan() {
        return deploymentplan;
    }

    @Param(name = ParameterNames.ALT_DD, optional = true)
    public File altdd = null;

    public File getAltdd() {
        return altdd;
    }

    @Param(name = ParameterNames.RUNTIME_ALT_DD, optional = true)
    public File runtimealtdd = null;

    public File getRuntimeAltdd() {
        return runtimealtdd;
    }

    @Param(name = ParameterNames.ENABLED, optional = true)
    public Boolean enabled = null;

    public Boolean isEnabled() {
        return enabled;
    }

    @Param(optional = true, defaultValue = "false")
    public Boolean generatermistubs = false;

    public Boolean isGenerateRMIStubs() {
        return generatermistubs;
    }

    @Param(optional = true, defaultValue = "false")
    public Boolean availabilityenabled = false;

    public Boolean isAvailabilityEnabled() {
        return availabilityenabled;
    }

    @Param(optional = true, defaultValue = "true")
    public Boolean asyncreplication = true;

    public Boolean isAsyncReplication() {
        return asyncreplication;
    }

    @Param(optional = true)
    public String target;

    @Param(optional = true, defaultValue = "false")
    public Boolean keepreposdir = false;

    public Boolean isKeepReposDir() {
        return keepreposdir;
    }

    @Param(optional = true, defaultValue = "false")
    public Boolean keepfailedstubs = false;

    public Boolean isKeepFailedStubs() {
        return keepfailedstubs;
    }

    @Param(optional = true, defaultValue = "false")
    public Boolean isredeploy = false;

    public Boolean isRedeploy() {
        return isredeploy;
    }

    @Param(optional = true, defaultValue = "true")
    public Boolean logReportedErrors = false;

    public Boolean isLogReportedErrors() {
        return logReportedErrors;
    }

    @Param(primary = true)
    public File path;

    public File getPath() {
        return path;
    }

    @Param(optional = true)
    public String description;

    @Param(optional = true, name = "properties", separator = ':')
    public Properties properties;

    @Param(optional = true, name = "property", separator = ':')
    public Properties property;

    @Param(optional = true)
    public String type = null;

    public String getType() {
        return type;
    }

    @Param(optional = true)
    public Boolean keepstate;

    @Param(optional = true, acceptableValues = "true,false")
    public String lbenabled;

    @Param(name = ParameterNames.DEPLOYMENT_ORDER, optional = true)
    public Integer deploymentorder = 100;

    // todo : why is this not a param ?
    public Boolean clientJarRequested = true;

    public Boolean isClientJarRequested() {
        return clientJarRequested;
    }

    public String previousContextRoot = null;

    public String getPreviousContextRoot() {
        return previousContextRoot;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String libraries() {
        return libraries;
    }

    public DeployCommandParameters() {
    }

    public DeployCommandParameters(File path) {
        this.path = path;
        if (path.getName().lastIndexOf('.') != -1) {
            name = path.getName().substring(0, path.getName().lastIndexOf('.'));
        } else {
            name = path.getName();
        }
    }

    public static class ParameterNames {

        public static final String COMPONENT = "component";
        public static final String VIRTUAL_SERVERS = "virtualservers";
        public static final String CONTEXT_ROOT = "contextroot";
        public static final String LIBRARIES = "libraries";
        public static final String DIRECTORY_DEPLOYED = "directorydeployed";
        public static final String LOCATION = "location";
        public static final String ENABLED = "enabled";
        public static final String PRECOMPILE_JSP = "precompilejsp";
        public static final String DEPLOYMENT_PLAN = "deploymentplan";
        public static final String DEPLOYMENT_ORDER = "deploymentorder";
        public static final String ALT_DD = "altdd";
        public static final String RUNTIME_ALT_DD = "runtimealtdd";
    }

}
