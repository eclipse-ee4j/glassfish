/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;

import jakarta.inject.Inject;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.appclient.server.core.jws.JavaWebStartInfo;
import org.glassfish.common.util.GlassfishUrlClassLoader;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Represents an app client module, either stand-alone or nested inside
 * an EAR, loaded on the server.
 * <p>
 * The primary purpose of this class is to implement Java Web Start support for
 * launches of this app client.  Other than in that sense, app clients do not
 * run in the server.  To support a client for Java Web Start launches, this
 * class figures out what static content (JAR files) and dynamic content (JNLP
 * documents) are needed by the client.  It then generates the required
 * dynamic content templates and submits them and the static content to a
 * Grizzly adapter which actually serves the data in response to requests.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class AppClientServerApplication implements ApplicationContainer<ApplicationClientDescriptor> {

    @Inject
    private ServiceLocator habitat;

    @Inject
    private ProcessEnvironment processEnv;


    private DeploymentContext dc;

    private AppClientDeployerHelper helper;

    private ApplicationClientDescriptor acDesc;
    private Application appDesc;

    private String deployedAppName;

    private JavaWebStartInfo jwsInfo;

    public void init(final DeploymentContext dc, final AppClientDeployerHelper helper) {
        this.dc = dc;
        this.helper = helper;
        acDesc = helper.appClientDesc();
        appDesc = acDesc.getApplication();
        deployedAppName = dc.getCommandParameters(DeployCommandParameters.class).name();
    }

    public String deployedAppName() {
        return deployedAppName;
    }

    @Override
    public ApplicationClientDescriptor getDescriptor() {
        return acDesc;
    }

    public AppClientDeployerHelper helper() {
        return helper;
    }

    public boolean matches(final String appName, final String moduleName) {
        return (appName.equals(deployedAppName)
                && (moduleName != null &&
                    (moduleName.equals(acDesc.getModuleName())
                     || acDesc.getModuleName().equals(moduleName + ".jar"))));
    }

    @Override
    public boolean start(ApplicationContext startupContext) throws Exception {
        return start();
    }


    boolean start() {
        if (processEnv.getProcessType().isEmbedded()) {
            return true;
        }
        if (jwsInfo == null) {
            jwsInfo = newJavaWebStartInfo();
        }
        jwsInfo.start();

        return true;
    }

    private JavaWebStartInfo newJavaWebStartInfo() {
        final JavaWebStartInfo info = habitat.getService(JavaWebStartInfo.class);
        info.init(this);
        return info;
    }

    @Override
    public boolean stop(ApplicationContext stopContext) {
        return stop();
    }

    boolean stop() {
        if (jwsInfo != null) {
            jwsInfo.stop();
        }

        return true;
    }

    @Override
    public boolean suspend() {
        if (jwsInfo != null) {
            jwsInfo.suspend();
        }
        return true;
    }

    @Override
    public boolean resume() throws Exception {
        if (jwsInfo != null) {
            jwsInfo.resume();
        }
        return true;
    }

    @Override
    public ClassLoader getClassLoader() {
        // This cannot be null or it prevents the framework from invoking unload
        // on the deployer for this app.
        PrivilegedAction<URLClassLoader> action = () -> new GlassfishUrlClassLoader(
            "AppClientServer(" + deployedAppName + ")", new URL[0]);
        return AccessController.doPrivileged(action);
    }

    public DeploymentContext dc() {
        return dc;
    }

    public String registrationName() {
        return appDesc.getRegistrationName();
    }

    public String moduleExpression() {
        String moduleExpression;
        if (appDesc.isVirtual()) {
            moduleExpression = appDesc.getRegistrationName();
        } else {
            moduleExpression = appDesc.getRegistrationName() + "/" + acDesc.getModuleName();
        }
        return moduleExpression;
    }

}
