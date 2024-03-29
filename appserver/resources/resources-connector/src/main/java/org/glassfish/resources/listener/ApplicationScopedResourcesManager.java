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

package org.glassfish.resources.listener;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.resourcebase.resources.ResourceTypeOrderProcessor;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.api.ResourcesBinder;
import org.glassfish.resourcebase.resources.util.ResourceManagerFactory;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Resource manager to bind various application or module scoped resources during
 * startup, create/update/delete of resource/pool
 * @author Jagadish Ramu
 */
@RunLevel(value=PostStartupRunLevel.VAL, mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
@Service(name="ApplicationScopedResourcesManager")
public class ApplicationScopedResourcesManager implements PostConstruct, PreDestroy, ConfigListener {

    private static final Logger _logger = LogDomains.getLogger(ApplicationScopedResourcesManager.class,LogDomains.RESOURCE_BUNDLE);

    @Inject
    private ResourcesBinder resourcesBinder;

    @Inject
    private Provider<ResourceManagerFactory> resourceManagerFactoryProvider;

    @Inject
    private ClassLoaderHierarchy clh;

    @Inject
    private Applications applications;

    @Inject
    private ResourceTypeOrderProcessor resourceTypeOrderProcessor;

    @Override
    public void postConstruct() {
        Collection<Application> apps = applications.getApplications();
        if(apps != null){
            for(Application app : apps){
                Resources resources = app.getResources();
                if(resources != null){
                    deployResources(resourceTypeOrderProcessor.getOrderedResources(resources.getResources()));
                }
                List<Module> modules = app.getModule();
                if(modules != null){
                    for(Module module : modules){
                        Resources moduleResources = module.getResources();
                        if(moduleResources != null){
                            deployResources(new ResourceTypeOrderProcessor().getOrderedResources(moduleResources.getResources()));
                        }
                    }
                }
            }
        }
    }

    public Resources getResources(String applicationName){
        Application app = applications.getApplication(applicationName);
        if(app != null){
            return app.getResources();
        }
        return null;
    }

    public void deployResources(String applicationName){
        Application app = applications.getApplication(applicationName);
        if(app != null){
            Resources appScopedResources = app.getResources();
            if(appScopedResources != null){
                deployResources(appScopedResources.getResources());
            }

            List<Module> modules = app.getModule();
            if(modules != null){
                for(Module module : modules){
                    Resources moduleScopedResources = module.getResources();
                    if(moduleScopedResources != null){
                        deployResources(moduleScopedResources.getResources());
                    }
                }
            }
        }

    }

    /**
     * deploy resources
     * @param resources list
     */
    public void deployResources(Collection<Resource> resources) {
        if (resources != null) {
            String applicationName = null;
            String moduleName = null;

            for (Resource resource : resources) {
                Object parentObject = resource.getParent().getParent();
                if(parentObject instanceof Application){
                    applicationName = ((Application)parentObject).getName();
                }else if(parentObject instanceof Module){
                    moduleName = ((Module)parentObject).getName();
                    applicationName = ((Application)((Module)parentObject).getParent()).getName();
                }

                if (resource instanceof BindableResource) {
                    BindableResource bindableResource = (BindableResource) resource;
                    SimpleJndiName jndiName = SimpleJndiName.of(bindableResource.getJndiName());
                    ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);
                    resourcesBinder.deployResource(resourceInfo, resource);
                } else if (resource instanceof ResourcePool) {
                    // ignore, as they are loaded lazily
                } else {
                    // only other resources left are RAC, CWSM
                    try {
                        getResourceDeployer(resource).deployResource(resource, applicationName, moduleName);
                    } catch (Exception e) {
                        Object[] params = {ResourceUtil.getGenericResourceInfo(resource), e};
                        _logger.log(Level.WARNING, "resources.resource-manager.deploy-resource-failed", params);
                    }
                }
            }
            addListenerToResources(resources);
        }
    }

    /**
     * Do cleanup of system-resource-adapter, resources, pools
     */
    @Override
    public void preDestroy() {
    }

    public void undeployResources(String applicationName){
        Application app = applications.getApplication(applicationName);
        if(app != null){
            List<Module> modules = app.getModule();
            if(modules != null){
                for(Module module : modules){
                    Resources moduleScopedResources = module.getResources();
                    if(moduleScopedResources != null){
                        undeployResources(moduleScopedResources);
                    }
                }
            }
            Resources appScopedResources = app.getResources();
            if(appScopedResources != null){
                undeployResources(appScopedResources);
            }
        }
    }

    /**
     * undeploy the given set of resources<br>
     * resources (bindable) are removed first and then the pools
     * @param resources list of resources
     */
    public void undeployResources(Resources resources){

        for(Resource resource : resources.getResources()){
            //destroy all resources first and then pools
            if(resource instanceof BindableResource){
            // no need to undeploy resource-adapter-config as it (config) will be removed by the end of undeploy
            // operation of the application.
                undeployResource(resource);
            }
        }
        Collection<ResourcePool> pools = resources.getResources(ResourcePool.class);
        for(ResourcePool pool : pools){
            undeployResource(pool);
        }
    }

    private void undeployResource(Resource resource){
        try{
            getResourceDeployer(resource).undeployResource(resource);
        }catch(Exception e){
            Object[] params = {ResourceUtil.getGenericResourceInfo(resource), e};
            _logger.log(Level.WARNING, "resources.resource-manager.undeploy-resource-failed", params);
        }finally{
            removeListenerFromResource(resource);
        }
    }

    private void addListenerToResources(Collection<Resource> resources){
        if(resources != null){
            for(Resource resource : resources){
                addListenerToResource(resource);
            }
        }
    }

    private void addListenerToResource(Resource instance) {
        ObservableBean bean = null;
        if(_logger.isLoggable(Level.FINEST)){
            debug("adding listener : " + instance);
        }
        bean = (ObservableBean) ConfigSupport.getImpl(instance);
        bean.addListener(this);
    }

    /**
     * Remove listener from all resources - JDBC Connection Pools/JDBC Resources/
     * Connector Connection Pools/ Connector Resources.
     * Invoked from preDestroy()
     */
    private void removeListenerFromResources(Resources resources) {
        if(resources != null){
            for (Resource configuredResource : resources.getResources()) {
                removeListenerFromResource(configuredResource);
            }
        }
    }

    /**
     * Remove listener from a generic resource (JDBC Connection Pool/Connector
     * Connection Pool/JDBC resource/Connector resource)
     * Used in the case of delete asadmin command
     * @param instance remove the resource from listening to resource events
     */
    private void removeListenerFromResource(Resource instance) {
        ObservableBean bean = null;
        debug("removing listener : " + instance);
        bean = (ObservableBean) ConfigSupport.getImpl(instance);
        bean.removeListener(this);
    }

    /**
     * Given a <i>resource</i> instance, appropriate deployer will be provided
     *
     * @param resource resource instance
     * @return ResourceDeployer
     */
    private ResourceDeployer getResourceDeployer(Object resource){
        return resourceManagerFactoryProvider.get().getResourceDeployer(resource);
    }

    private void  debug(String message){
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("[ASR] [ApplicationScopedResourcesManager] " + message);
        }
     }

    /**
     * Notification that @Configured objects that were injected have changed
     *
     * @param events list of changes
     */
    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new ConfigChangeHandler(), _logger);
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
            NotProcessed np ;
            ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
            try {
                //use connector-class-loader so as to get access to classes from resource-adapters
                ClassLoader ccl = clh.getConnectorClassLoader(null);
                Thread.currentThread().setContextClassLoader(ccl);
                switch (type) {
                    case ADD:
                        np = handleAddEvent(changedInstance);
                        break;
                    case CHANGE:
                        np = handleChangeEvent(changedInstance);
                        break;
                    case REMOVE:
                        np = handleRemoveEvent(changedInstance);
                        break;

                    default:
                        np = new NotProcessed("Unrecognized type of change: " + type);
                        break;
                }
                return np;
            } finally {
                Thread.currentThread().setContextClassLoader(contextCL);
            }
        }
        private <T extends ConfigBeanProxy> NotProcessed handleAddEvent(T instance) {
            NotProcessed np = null;
            if(instance instanceof Application){
                Resources resources = ((Application)instance).getResources();
                if(resources != null){
                    addListenerToResources(resources.getResources());
                }

                Application app = (Application)instance;
                List<Module> modules = app.getModule();
                if(modules != null){
                    for(Module module : modules){
                        if(module.getResources() !=null && module.getResources().getResources() != null){
                            addListenerToResources(module.getResources().getResources());
                        }
                    }
                }
            }
            return np;
        }

        private <T extends ConfigBeanProxy> NotProcessed handleRemoveEvent(T instance) {
            NotProcessed np = null;
            if(instance instanceof Application){
                removeListenerFromResources(((Application)instance).getResources());

                Application app = (Application)instance;
                List<Module> modules = app.getModule();
                if(modules != null){
                    for(Module module : modules){
                        if(module.getResources() !=null && module.getResources().getResources() != null){
                            removeListenerFromResources(module.getResources());
                        }
                    }
                }
            }
            return np;
        }


        private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T instance) {
            NotProcessed np = null;
            //TODO V3 handle enabled / disabled /redeploy ?
            debug("handling change event");
            try {
                if (ResourceUtil.isValidEventType(instance)) {
                    if(_logger.isLoggable(Level.FINEST)){
                        debug("redeploying resource : " + instance);
                    }
                    getResourceDeployer(instance).redeployResource(instance);
                } else if (ResourceUtil.isValidEventType(instance.getParent())) {
                    if(_logger.isLoggable(Level.FINEST)){
                        debug("redeploying resource due to property change : " + instance.getParent());
                    }
                    //Added in case of a property change
                    //check for validity of the property's parent and redeploy
                    getResourceDeployer(instance.getParent()).redeployResource(instance.getParent());
                }
            } catch (Exception ex) {
                final String msg = ApplicationScopedResourcesManager.class.getName() + " : " +
                        "Error while handling change Event";
                _logger.severe(msg);
                np = new NotProcessed(msg);
            }
            return np;
        }
    }
}
