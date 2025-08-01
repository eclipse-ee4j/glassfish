/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.appserv.connectors.internal;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorDescriptorProxy;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;


/**
 * ResourceManager lifecycle listener that listens to resource-manager startup and shutdown
 * and does connector related work. eg: binding connector proxies.</br>
 * Also, does ping-connection-pool for application and module scoped resources (if ping=true)
 * @author Jagadish Ramu
 */
@Service
@Singleton
public class ConnectorResourceManagerLifecycleListener implements org.glassfish.resourcebase.resources.listener.ResourceManagerLifecycleListener, ConfigListener {

    private static final Logger LOG = LogDomains.getLogger(ConnectorResourceManagerLifecycleListener.class,
        LogDomains.RSR_LOGGER, false);

    @Inject
    private GlassfishNamingManager namingMgr;

    @Inject
    private Provider<ConnectorDescriptorProxy> connectorDescriptorProxyProvider;

    @Inject
    private Provider<CommandRunner> commandRunnerProvider;

    @Inject
    private Provider<ActionReport> actionReportProvider;

    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;

    @Inject
    private Domain domain;

    @Inject
    private Applications applications;

    @Inject
    private ServiceLocator connectorRuntimeHabitat;

    private ConnectorRuntime runtime;

    @Inject
    private ClassLoaderHierarchy clh;

    @Inject
    private ServerEnvironment serverEnvironment;

    @Inject
    private InternalSystemAdministrator internalSystemAdministrator;



    private void bindConnectorDescriptors() {
        for(String rarName : ConnectorConstants.systemRarNames){
            bindConnectorDescriptorProxies(rarName);
        }
    }

    private void bindConnectorDescriptorProxies(String rarName) {
        //these proxies are needed as appclient container may lookup descriptors
        SimpleJndiName jndiName = ConnectorsUtil.getReservePrefixedJNDINameForDescriptor(rarName);
        ConnectorDescriptorProxy proxy = connectorDescriptorProxyProvider.get();
        proxy.setJndiName(jndiName);
        proxy.setRarName(rarName);
        try {
            namingMgr.publishObject(jndiName, proxy, true);
        } catch (NamingException e) {
            LOG.log(WARNING,
                "Unable to bind connector descriptor for resource-adapter " + rarName + " to JNDI name " + jndiName, e);
        }
    }

    private ConnectorRuntime getConnectorRuntime() {
        if(runtime == null){
            runtime = connectorRuntimeProvider.get();
        }
        return runtime;
    }

    /**
     * Check whether connector-runtime is initialized.
     * @return boolean representing connector-runtime initialization status.
     */
    public boolean isConnectorRuntimeInitialized() {
        List<ServiceHandle<ConnectorRuntime>> serviceHandles =
                connectorRuntimeHabitat.getAllServiceHandles(ConnectorRuntime.class);
        for(ServiceHandle<ConnectorRuntime> inhabitant : serviceHandles){
            // there will be only one implementation of connector-runtime
            return inhabitant.isActive();
        }
        return true; // to be safe
    }

    @Override
    public void resourceManagerLifecycleEvent(EVENT event){
        if(EVENT.STARTUP.equals(event)){
            resourceManagerStarted();
        }else if(EVENT.SHUTDOWN.equals(event)){
            resourceManagerShutdown();
        }
    }

    public void resourceManagerStarted() {
        bindConnectorDescriptors();
    }

    public void resourceManagerShutdown() {
        if (isConnectorRuntimeInitialized()) {
            ConnectorRuntime cr = getConnectorRuntime();
            if (cr != null) {
                // clean up will take care of any system RA resources, pools
                // (including pools via datasource-definition)
                cr.cleanUpResourcesAndShutdownAllActiveRAs();
            }
        } else {
            LOG.log(FINEST, "ConnectorRuntime not initialized, hence skipping resource-adapters"
                + " shutdown, resources, pools cleanup");
        }
    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new ConfigChangeHandler(), LOG);
    }

    class ConfigChangeHandler implements Changed {

        private ConfigChangeHandler() {
        }

        /**
         * Notification of a change on a configuration object
         *
         * @param type            CHANGE means the changedInstance has mutated.
         * @param changedType     type of the configuration object
         * @param changedInstance changed instance.
         */
        @Override
        public <T extends ConfigBeanProxy> NotProcessed changed(Changed.TYPE type, Class<T> changedType,
                                                                T changedInstance) {
            NotProcessed np = null;
            if(!(changedInstance instanceof Application)){
                return np;
            }
            if(serverEnvironment.isDas()){
                ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
                try {
                    //use connector-class-loader so as to get access to classes from resource-adapters
                    ClassLoader ccl = clh.getConnectorClassLoader(null);
                    Thread.currentThread().setContextClassLoader(ccl);
                    switch (type) {
                        case ADD:
                            np = handleAddEvent(changedInstance);
                            break;
                        default:
                            break;
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(contextCL);
                }
            }
            return np;
        }
        private <T extends ConfigBeanProxy> NotProcessed handleAddEvent(T instance) {
            NotProcessed np = null;
            if(instance instanceof Application){
                Resources resources = ((Application)instance).getResources();
                pingConnectionPool(resources);

                Application app = (Application)instance;
                List<Module> modules = app.getModule();
                if(modules != null){
                    for(Module module : modules){
                        if(module.getResources() !=null && module.getResources().getResources() != null){
                            pingConnectionPool(module.getResources());
                        }
                    }
                }
            }
            return np;
        }

        private void pingConnectionPool(Resources resources) {
            if (resources == null || resources.getResources() == null) {
                return;
            }
            for (Resource resource : resources.getResources()) {
                if (!(resource instanceof ResourcePool)) {
                    continue;
                }
                ResourcePool pool = (ResourcePool)resource;
                if (Boolean.parseBoolean(pool.getPing())) {
                    PoolInfo poolInfo = ResourceUtil.getPoolInfo(pool);
                    CommandRunner commandRunner = commandRunnerProvider.get();
                    ActionReport report = actionReportProvider.get();
                    CommandInvocation invocation = commandRunner
                        .getCommandInvocation("ping-connection-pool", report, internalSystemAdministrator.getSubject());
                    ParameterMap params = new ParameterMap();
                    params.add("appname",poolInfo.getApplicationName());
                    params.add("modulename",poolInfo.getModuleName());
                    params.add("DEFAULT", poolInfo.getName().toString());
                    invocation.parameters(params).execute();
                    if (report.getActionExitCode() == ActionReport.ExitCode.SUCCESS) {
                        LOG.log(INFO, "The ping-connection-pool to {0} succeeded.", poolInfo);
                    } else {
                        LOG.log(WARNING, "The ping-connection-pool to " + poolInfo + " failed.",
                            report.getFailureCause());
                    }
                }
            }

        }
    }
}
