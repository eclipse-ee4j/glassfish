/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.connectors.ActiveResourceAdapter;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.module.ConnectorApplication;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import javax.naming.NamingException;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils.getReservePrefixedJNDINameForDescriptor;


/**
 * This is resource adapter admin service. It creates, deletes Resource adapter
 * and also the resource adapter configuration updation.
 *
 * @author Binod P.G, Srikanth P, Aditya Gore, Jagadish Ramu
 */
public class ResourceAdapterAdminServiceImpl extends ConnectorService {

    private final ExecutorService execService =
    Executors.newCachedThreadPool(new ThreadFactory() {
           @Override
        public Thread newThread(Runnable r) {
             Thread th = new Thread(r);
             th.setDaemon(true);
             return th;
           }
    });

    /**
     * Default constructor
     */
    public ResourceAdapterAdminServiceImpl() {
    }

    /**
     * Destroys/deletes the Active resource adapter object from the connector
     * container. Active resource adapter abstracts the rar deployed.
     *
     * @param moduleName Name of the rarModule to destroy/delete
     * @throws ConnectorRuntimeException if the deletion fails
     */
    private void destroyActiveResourceAdapter(String moduleName) throws ConnectorRuntimeException {

        ResourcesUtil resutil = ResourcesUtil.createInstance();
        if (resutil == null) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Failed to get ResourcesUtil object");
            _logger.log(Level.SEVERE, "rardeployment.resourcesutil_get_failure", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }

        if (!stopAndRemoveActiveResourceAdapter(moduleName)) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException("Failed to remove Active Resource Adapter");
            _logger.log(Level.SEVERE, "rardeployment.ra_removal_registry_failure", moduleName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }

        unbindConnectorDescriptor(moduleName);
    }

    private void unbindConnectorDescriptor(String moduleName) throws ConnectorRuntimeException {
        if(ConnectorRuntime.getRuntime().isServer()){
            try {

                SimpleJndiName descriptorJNDIName = getReservePrefixedJNDINameForDescriptor(moduleName);
                _runtime.getNamingManager().getInitialContext().unbind(descriptorJNDIName.toString());

                if(_logger.isLoggable(Level.FINEST)){
                    _logger.finest("ResourceAdapterAdminServiceImpl :: destroyActiveRA "
                        + moduleName + " removed descriptor " + descriptorJNDIName);
                }

            } catch (NamingException ne) {
                if(_logger.isLoggable(Level.FINEST)){
                    _logger.log(Level.FINEST, "rardeployment.connector_descriptor_jndi_removal_failure", moduleName);
                }
            }
        }
    }

    /**
     * Creates Active resource Adapter which abstracts the rar module. During
     * the creation of ActiveResourceAdapter, default pools and resources also
     * are created.
     *
     * @param connectorDescriptor object which abstracts the connector deployment descriptor
     *                            i.e rar.xml and sun-ra.xml.
     * @param moduleName          Name of the module
     * @param moduleDir           Directory where rar module is exploded.
     * @param loader              Classloader to use
     * @throws ConnectorRuntimeException if creation fails.
     */

    public void createActiveResourceAdapter(ConnectorDescriptor connectorDescriptor,
                                            String moduleName, String moduleDir, ClassLoader loader)
            throws ConnectorRuntimeException {

        synchronized (_registry.getLockObject(moduleName)) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA "
                        + moduleName + " at " + moduleDir);
            }

            ActiveResourceAdapter activeResourceAdapter = _registry.getActiveResourceAdapter(moduleName);
            if (activeResourceAdapter != null) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "rardeployment.resourceadapter.already.started", moduleName);
                }
                return;
            }


            //TODO V3 works fine ?
            if (loader == null) {
                try {
                    loader = connectorDescriptor.getClassLoader();
                } catch (Exception ex) {
                    if(_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, "No classloader available with connector descriptor");
                    }
                    loader = null;
                }
            }
            ConnectorRuntime connectorRuntime = ConnectorRuntime.getRuntime();
            ModuleDescriptor moduleDescriptor = null;
            Application application = null;
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA "
                    + moduleName + " at " + moduleDir + " loader :: " + loader);
            }
            //class-loader can not be null for standalone rar as deployer should have provided one.
            //class-laoder can (may) be null for system-rars as they are not actually deployed.
            //TODO V3 don't check for system-ra if the resource-adapters are not loaded before recovery
            // (standalone + embedded)
            if (loader == null && ConnectorsUtil.belongsToSystemRA(moduleName)) {
                if (connectorRuntime.isServer()) {
                    loader = connectorRuntime.getSystemRARClassLoader(moduleName);
                }
            } else {
                connectorDescriptor.setClassLoader(null);
                moduleDescriptor = connectorDescriptor.getModuleDescriptor();
                application = connectorDescriptor.getApplication();
                connectorDescriptor.setModuleDescriptor(null);
                connectorDescriptor.setApplication(null);
            }
            try {

                activeResourceAdapter =
                        connectorRuntime.getActiveRAFactory().
                                createActiveResourceAdapter(connectorDescriptor, moduleName, loader);
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA " +
                        moduleName + " at " + moduleDir +
                        " adding to registry " + activeResourceAdapter);
                }
                _registry.addActiveResourceAdapter(moduleName, activeResourceAdapter);
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("ResourceAdapterAdminServiceImpl:: createActiveRA " +
                        moduleName + " at " + moduleDir
                        + " env =server ? " + (connectorRuntime.isServer()));
                }

                if (connectorRuntime.isServer()) {
                    //Update RAConfig in Connector Descriptor and bind in JNDI
                    //so that ACC clients could use RAConfig
                    updateRAConfigInDescriptor(connectorDescriptor, moduleName);
                    SimpleJndiName descriptorJNDIName = getReservePrefixedJNDINameForDescriptor(moduleName);
                    if(_logger.isLoggable(Level.FINE)) {
                        _logger.fine("ResourceAdapterAdminServiceImpl :: createActiveRA "
                            + moduleName + " at " + moduleDir
                            + " publishing descriptor " + descriptorJNDIName);
                    }
                    _runtime.getNamingManager().publishObject(descriptorJNDIName, connectorDescriptor, true);

                    activeResourceAdapter.setup();

                    String securityWarningMessage=
                        connectorRuntime.getSecurityPermissionSpec(moduleName);
                    // To i18N.
                    if (securityWarningMessage != null) {
                        _logger.log(Level.WARNING, securityWarningMessage);
                    }
                }

            } catch (NullPointerException npEx) {
                ConnectorRuntimeException cre =
                        new ConnectorRuntimeException("Error in creating active RAR");
                cre.initCause(npEx);
                _logger.log( Level.SEVERE, "rardeployment.nullPointerException", moduleName);
                _logger.log(Level.SEVERE, "", cre);
                throw cre;
            } catch (NamingException ne) {
                ConnectorRuntimeException cre =
                        new ConnectorRuntimeException("Error in creating active RAR");
                cre.initCause(ne);
                _logger.log(Level.SEVERE, "rardeployment.jndi_publish_failure");
                _logger.log(Level.SEVERE, "", cre);
                throw cre;
            } finally {
                if (moduleDescriptor != null) {
                    connectorDescriptor.setModuleDescriptor(moduleDescriptor);
                    connectorDescriptor.setApplication(application);
                    connectorDescriptor.setClassLoader(loader);
                }
            }
        }
    }

    /**
     * Updates the connector descriptor of the connector module, with the
     * contents of a resource adapter config if specified.
     *
     * This modified ConnectorDescriptor is then bound to JNDI so that ACC
     * clients while configuring a non-system RAR could get the correct merged
     * configuration. Any updates to resource-adapter config while an ACC client
     * is in use is not transmitted to the client dynamically. All such changes
     * would be visible on ACC client restart.
     */

    private void updateRAConfigInDescriptor(ConnectorDescriptor connectorDescriptor,
                                            String moduleName) {

        ResourceAdapterConfig raConfig =
                ConnectorRegistry.getInstance().getResourceAdapterConfig(moduleName);

        List<Property> raConfigProps = null;
        if (raConfig != null) {
            raConfigProps = raConfig.getProperty();
        }

        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("current RAConfig In Descriptor " + connectorDescriptor.getConfigProperties());
        }

        if (raConfigProps != null) {
            Set mergedProps = ConnectorDDTransformUtils.mergeProps(
                    raConfigProps, connectorDescriptor.getConfigProperties());
            Set actualProps = connectorDescriptor.getConfigProperties();
            actualProps.clear();
            actualProps.addAll(mergedProps);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("updated RAConfig In Descriptor " + connectorDescriptor.getConfigProperties());
            }
        }

    }


    /**
     * Creates Active resource Adapter which abstracts the rar module. During
     * the creation of ActiveResourceAdapter, default pools and resources also
     * are created.
     *
     * @param moduleDir  Directory where rar module is exploded.
     * @param moduleName Name of the module
     * @throws ConnectorRuntimeException if creation fails.
     */
    public void createActiveResourceAdapter(String moduleDir, String moduleName, ClassLoader loader)
            throws ConnectorRuntimeException {

        synchronized (_registry.getLockObject(moduleName)){
            ActiveResourceAdapter activeResourceAdapter =
                    _registry.getActiveResourceAdapter(moduleName);
            if (activeResourceAdapter != null) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "rardeployment.resourceadapter.already.started", moduleName);
                }
                return;
            }

            if (ConnectorsUtil.belongsToSystemRA(moduleName)) {
                moduleDir = ConnectorsUtil.getSystemModuleLocation(moduleName);
            }

            ConnectorDescriptor connectorDescriptor = ConnectorDDTransformUtils.getConnectorDescriptor(moduleDir, moduleName);

            if (connectorDescriptor == null) {
                ConnectorRuntimeException cre = new ConnectorRuntimeException("Failed to obtain the connectorDescriptor");
                _logger.log(Level.SEVERE, "rardeployment.connector_descriptor_notfound", moduleName);
                _logger.log(Level.SEVERE, "", cre);
                throw cre;
            }

            createActiveResourceAdapter(connectorDescriptor, moduleName, moduleDir, loader);
        }
    }


    /**
     * associates the given instance of ResourceAdapterAssociation with
     * the ResourceAdapter java-bean of the specified RAR
     * @param rarName resource-adapter-name
     * @param raa Object that is an instance of ResourceAdapterAssociation
     * @throws ResourceException when unable to associate the RA Bean with RAA instance.
     */
    public void associateResourceAdapter(String rarName, ResourceAdapterAssociation raa)
            throws ResourceException {
        ResourceAdapter ra = ConnectorRegistry.getInstance().
                getActiveResourceAdapter(rarName).getResourceAdapter();
        if(ra != null){
            raa.setResourceAdapter(ra);
        }else{
            throw new ResourceException("RA Bean [ "+rarName+" ] not available");
        }
    }


    /**
     * Stops the resourceAdapter and removes it from connector container/
     * registry.
     *
     * @param moduleName Rarmodule name.
     * @return true it is successful stop and removal of ActiveResourceAdapter
     *         false it stop and removal fails.
     */
    private boolean stopAndRemoveActiveResourceAdapter(String moduleName) {

        ActiveResourceAdapter acr = null;
        if (moduleName != null) {
            acr = _registry.getActiveResourceAdapter(moduleName);
        }
        if (acr != null) {
            sendStopToResourceAdapter(acr);
            _registry.removeLockObject(moduleName);
            return _registry.removeActiveResourceAdapter(moduleName);
        }
        return true;
    }

    /**
     * Checks if the rar module is already reployed.
     *
     * @param moduleName Rarmodule name
     * @return true if it is already deployed. false if it is not deployed.
     */
    public boolean isRarDeployed(String moduleName) {

        ActiveResourceAdapter activeResourceAdapter =
                _registry.getActiveResourceAdapter(moduleName);
        return activeResourceAdapter != null;
    }

    /**
     * Calls the stop method for all J2EE Connector 1.5/1.0 spec compliant RARs
     */
    public void stopAllActiveResourceAdapters() {
        ActiveResourceAdapter[] resourceAdapters =
                ConnectorRegistry.getInstance().getAllActiveResourceAdapters();

        //stop system-rars after stopping all other rars.
        Set<ActiveResourceAdapter> systemRAs = new HashSet<>();
        List<Future> rarExitStatusList = new ArrayList<>();

        for (ActiveResourceAdapter resourceAdapter : resourceAdapters) {
            if(!ConnectorsUtil.belongsToSystemRA(resourceAdapter.getModuleName())){
                RAShutdownHandler handler = new RAShutdownHandler(resourceAdapter.getModuleName());
                rarExitStatusList.add(execService.submit(handler));
            }else{
                systemRAs.add(resourceAdapter);
            }
        }

        for(Future future: rarExitStatusList){
            try {
                future.get();
            } catch (InterruptedException e) {
                //ignore as the child task will log any failures
            } catch (ExecutionException e) {
                //ignore as the child task will log any failures
            }
        }
        rarExitStatusList.clear();

        for(ActiveResourceAdapter resourceAdapter : systemRAs){
            RAShutdownHandler handler = new RAShutdownHandler(resourceAdapter.getModuleName());
            rarExitStatusList.add(execService.submit(handler));
        }

        for(Future future: rarExitStatusList){
            try {
                future.get();
            } catch (InterruptedException e) {
                //ignore as the child task will log any failures
            } catch (ExecutionException e) {
                //ignore as the child task will log any failures
            }
        }
    }

    /**
     * stop the active resource adapter (runtime)
     * @param raName resource-adapter name
     */
    public void stopActiveResourceAdapter(String raName) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Stopping RA : ", raName);
        }
        try {
            destroyActiveResourceAdapter(raName);
        } catch (ConnectorRuntimeException cre) {
            Object params[] = new Object[]{raName, cre.getMessage()};
            _logger.log(Level.WARNING, "unable.to.stop.ra", params);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "unable to stop resource adapter [ " + raName + " ]", cre);
            }
        }
    }

    /**
     * add the resource-adapter-config
     * @param rarName resource-adapter name
     * @param raConfig resource-adapter-config
     * @throws ConnectorRuntimeException
     */
    public void addResourceAdapterConfig(String rarName, ResourceAdapterConfig raConfig)
        throws ConnectorRuntimeException {
        if (rarName != null && raConfig != null) {
            _registry.addResourceAdapterConfig(rarName, raConfig);
            reCreateActiveResourceAdapter(rarName);
        }
    }

    /**
     * Delete the resource adapter configuration to the connector registry
     * @param rarName resource-adapter-name
     * @throws ConnectorRuntimeException when unable to remove RA Config.
     */
    public void deleteResourceAdapterConfig(String rarName) throws ConnectorRuntimeException {
        if (rarName != null) {
            _registry.removeResourceAdapterConfig(rarName);
            reCreateActiveResourceAdapter(rarName);
        }
    }

    /**
     * The ActiveResourceAdapter object which abstract the rar module is
     * recreated in the connector container/registry. All the pools and
     * resources are killed. But the infrastructure to create the pools and and
     * resources is untouched. Only the actual pool is killed.
     *
     * @param moduleName
     *                     rar module Name.
     * @throws ConnectorRuntimeException
     *                      if recreation fails.
     */

    public void reCreateActiveResourceAdapter(String moduleName)
            throws ConnectorRuntimeException {

        ConnectorRuntime runtime = ConnectorRuntime.getRuntime();

        if (isRarDeployed(moduleName)) {
            if(!ConnectorsUtil.belongsToSystemRA(moduleName)){
                ConnectorApplication app = _registry.getConnectorApplication(moduleName);
                app.undeployResources();
                stopAndRemoveActiveResourceAdapter(moduleName);
                String moduleDir = ConnectorsUtil.getLocation(moduleName);
                createActiveResourceAdapter(moduleDir, moduleName, app.getClassLoader());
                _registry.getConnectorApplication(moduleName).deployResources();
            }else{
             Collection<Resource> resources =
                     getResourcesUtil().filterConnectorResources(getResourcesUtil().getGlobalResources(), moduleName, true);
                runtime.getGlobalResourceManager().undeployResources(resources);
                stopAndRemoveActiveResourceAdapter(moduleName);
                String moduleDir = ConnectorsUtil.getLocation(moduleName);
                createActiveResourceAdapter(moduleDir, moduleName,
                        runtime.getSystemRARClassLoader(moduleName));
                runtime.getGlobalResourceManager().deployResources(resources);
            }
        }
    }

    /**
     * Calls the stop method for all RARs
     *
     * @param resourceAdapterToStop ra to stop
     */
    private void sendStopToResourceAdapter(ActiveResourceAdapter resourceAdapterToStop) {
        Runnable rast = new RAShutdownTask(resourceAdapterToStop);
        String raName =  resourceAdapterToStop.getModuleName();

        Long timeout = ConnectorRuntime.getRuntime().getShutdownTimeout();

        Future future = null;
        boolean stopSuccessful = false;
        try {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "scheduling stop for RA [ " + raName +" ] ");
            }
            future = execService.submit(rast);
            future.get(timeout, TimeUnit.MILLISECONDS);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "stop() Complete for active 1.5 compliant RAR " +
                    "[ "+ raName  +" ]");
            }
            stopSuccessful = true;
        } catch (TimeoutException e) {
            Object params[] = new Object[]{raName, e};
            _logger.log(Level.WARNING, "ra.stop.timeout", params);
            cancelTask(future, true, raName);
        } catch(Exception e){
            Object params[] = new Object[]{raName, e};
            _logger.log(Level.WARNING, "ra.stop.failed", params);
            cancelTask(future, true, raName);
        }

        if (stopSuccessful) {
            _logger.log(Level.INFO, "ra.stop-successful", raName);
        } else {
            _logger.log(Level.WARNING, "ra.stop-unsuccessful", raName);
        }
    }

    private void cancelTask(Future future, boolean interruptIfRunning, String raName){
        if(future != null){
            if(!(future.isCancelled()) && !(future.isDone())){
                boolean cancelled = future.cancel(interruptIfRunning);
                _logger.log(Level.INFO, "cancelling the shutdown of RA [ " + raName +" ] status : " + cancelled);
            } else {
                _logger.log(Level.INFO, "shutdown of RA [ " + raName +" ] is either already complete or already cancelled");
            }
        }
    }

    private static class RAShutdownTask implements Runnable {
        private final ActiveResourceAdapter ra;

        public RAShutdownTask(ActiveResourceAdapter ratoBeShutDown) {
            this.ra = ratoBeShutDown;
        }

        @Override
        public void run() {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Calling RA [ " + ra.getModuleName() + " ] shutdown ");
            }
            this.ra.destroy();
        }
    }

    private class RAShutdownHandler implements Runnable {
        private final String moduleName;

        public RAShutdownHandler(String moduleName){
            this.moduleName = moduleName;
        }
        @Override
        public void run(){
            stopActiveResourceAdapter(moduleName);
        }
    }
}
