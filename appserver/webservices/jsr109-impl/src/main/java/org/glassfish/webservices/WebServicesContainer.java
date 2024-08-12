/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.xml.ws.api.server.LazyMOMProvider;

import jakarta.inject.Singleton;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.ObjectName;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.external.amx.MBeanListener;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.webservices.deployment.WebServicesDeploymentMBean;
import org.jvnet.hk2.annotations.Service;

/**
 * Web services container service
 *
 */
@Service(name="org.glassfish.webservices.WebServicesContainer")
@Singleton
public class WebServicesContainer extends MBeanListener.CallbackImpl implements Container, PostConstruct, PreDestroy {
    private final WebServicesDeploymentMBean deploymentBean = new WebServicesDeploymentMBean();
    private ManagedObjectManager mom;

    @Override
    public String getName() {
        return "webservices";
    }

    @Override
    public void postConstruct() {
        // Register listener for AMX DomainRoot loaded
        final AMXGlassfish amxg = AMXGlassfish.DEFAULT;
        amxg.listenForDomainRoot(ManagementFactory.getPlatformMBeanServer(), this);

        LazyMOMProvider.INSTANCE.initMOMForScope(LazyMOMProvider.Scope.GLASSFISH_NO_JMX);
    }

    @Override
    public void mbeanRegistered(javax.management.ObjectName objectName, org.glassfish.external.amx.MBeanListener listener) {
        ObjectName MONITORING_SERVER = AMXGlassfish.DEFAULT.serverMon(AMXGlassfish.DEFAULT.dasName());
        mom = ManagedObjectManagerFactory.createFederated(MONITORING_SERVER);
        if (mom != null) {
            mom.setJMXRegistrationDebug(false);
            mom.stripPackagePrefix();
            mom.createRoot(deploymentBean, "webservices-deployment");
        }

        LazyMOMProvider.INSTANCE.initMOMForScope(LazyMOMProvider.Scope.GLASSFISH_JMX);
    }

    public WebServicesDeploymentMBean getDeploymentBean() {
        return deploymentBean;
    }

    @Override
    public Class<? extends Deployer> getDeployer() {
        return WebServicesDeployer.class;
    }

    @Override
    public void preDestroy() {
        try {
            if (mom != null) {
                mom.close();
            }
        } catch(IOException ioe) {
            // ignored nothing much can be done
        }
    }
}

