/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resourcebase.resources.listener;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerResource;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.InitRunLevel;
import org.glassfish.internal.api.LogManager;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.resourcebase.resources.ResourceLoggingConstansts;
import org.glassfish.resourcebase.resources.ResourceTypeOrderProcessor;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceDeployerValidator;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.api.ResourcesBinder;
import org.glassfish.resourcebase.resources.util.ResourceManagerFactory;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;


/**
 * Resource manager to bind various resources during start-up, create/update/delete of resource/pool
 *
 * @author Jagadish Ramu
 */
@RunLevel( value= InitRunLevel.VAL + 1, mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
@Service(name="ResourceManager") // this name is used in ApplicationLoaderService
public class ResourceManager implements PostConstruct, PreDestroy, ConfigListener {

    @LogMessagesResourceBundle
    public static final String LOGMESSAGE_RESOURCE = "org.glassfish.resourcebase.resources.LogMessages";

    @LoggerInfo(subsystem="RESOURCE", description="Nucleus Resource", publish=true)

    public static final String LOGGER = "jakarta.enterprise.resources.listener";
    private static final Logger logger = Logger.getLogger(LOGGER, LOGMESSAGE_RESOURCE);

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ResourceManager.class);

    @SuppressWarnings("unused")
    @Inject @Optional
    private LogManager dependency0;  // The LogManager must come up prior to this service

    @Inject
    private ServiceLocator locator;

    @Inject
    private ResourcesBinder resourcesBinder;

    @Inject
    private org.glassfish.resourcebase.resources.util.BindableResourcesHelper bindableResourcesHelper;

    @Inject
    private IterableProvider<ResourceManagerLifecycleListener> resourceManagerLifecycleListenerProviders;

    @Inject
    private Provider<ResourceManagerFactory> resourceManagerFactoryProvider;

    @Inject
    private ServerEnvironment environment;

    @Inject
    private Domain domain;

    @Inject
    private ClassLoaderHierarchy clh;

    @Inject
    private ResourceTypeOrderProcessor resourceTypeOrderProcessor;

    @Override
    public void postConstruct() {
        notifyListeners(ResourceManagerLifecycleListener.EVENT.STARTUP);
        deployResources(resourceTypeOrderProcessor.getOrderedResources(domain.getResources().getResources()));
        addListenerToResources();
        addListenerToResourceRefs();
        addListenerToServer();
    }

    private void notifyListeners(ResourceManagerLifecycleListener.EVENT event) {
        for (ResourceManagerLifecycleListener listener : resourceManagerLifecycleListenerProviders) {
            listener.resourceManagerLifecycleEvent(event);
        }
    }

    private void addListenerToResources() {
        Resources resources = domain.getResources();
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(resources);
        bean.addListener(this);
    }

    private void addListenerToServer() {
        Server server = domain.getServerNamed(environment.getInstanceName());
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(server);
        bean.addListener(this);
        Config config = server.getConfig();
        if (config != null) {
            ((ObservableBean)ConfigSupport.getImpl(config)).addListener(this);
        }
    }

    private Server getServerBean() {
        return domain.getServerNamed(environment.getInstanceName());
    }

    /**
     * deploy resources
     *
     * @param resources list
     */
    public void deployResources(Collection<Resource> resources) {
        for (Resource resource : resources) {
            if (resource instanceof BindableResource) {
                BindableResource bindableResource = (BindableResource) resource;
                if (bindableResourcesHelper.isBindableResourceEnabled(bindableResource)) {
                    SimpleJndiName jndiName = SimpleJndiName.of(bindableResource.getJndiName());
                    ResourceInfo resourceInfo = new ResourceInfo(jndiName);
                    resourcesBinder.deployResource(resourceInfo, resource);
                }
            } else if (resource instanceof ServerResource) {
                deployServerResource(resource);
            } else if (resource instanceof ResourcePool) {
                // ignore, as they are loaded lazily
            } else {
                // only other resource types left are RAC, CWSM
                try {
                    ResourceDeployer<Resource> deployer = getResourceDeployer(resource);
                    if (deployer != null) {
                        deployer.deployResource(resource);
                    }
                } catch (Exception e) {
                    Object[] params = {ResourceUtil.getGenericResourceInfo(resource), e};
                    logger.log(Level.WARNING, ResourceLoggingConstansts.UNABLE_TO_DEPLOY, params);
                }
            }
        }
        /* TODO V3
            will there be a chance of double listener registration for a resource ?
            eg: allresources added during startup, resources of a particular
            connector during connector startup / redeploy ?
        */
        addListenerToResources(resources);
    }

    public Resources getAllResources() {
        return domain.getResources();
    }


    /**
     * Do cleanup of system-resource-adapter, resources, pools
     */
    @Override
    public void preDestroy() {
        removeListenerForAllResources();
        removeListenerForResources();
        removeListenerForResourceRefs();
        removeListenerForServer();
        notifyListeners(ResourceManagerLifecycleListener.EVENT.SHUTDOWN);
    }

    private void removeListenerForServer() {
        Server server = domain.getServerNamed(environment.getInstanceName());
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(server);
        bean.removeListener(this);
        Config config = server.getConfig();
        if (config != null) {
            ((ObservableBean)ConfigSupport.getImpl(config)).removeListener(this);
        }
    }

    /**
     * undeploy the given set of resources<br>
     * <b>care has to be taken for the case of dependent resources<br>
     * eg : all resources need to be undeployed <br>
     * before undeploying the pool that they refer to</b>
     *
     * @param resources list of resources
     */
    public void undeployResources(Collection<Resource> resources) {
        for (Resource resource : resources) {
            try {
                ResourceDeployer<Resource> deployer = getResourceDeployer(resource);
                if (deployer != null) {
                    deployer.undeployResource(resource);
                } else {
                    Object[] params = {resource.getIdentity()};
                    logger.log(Level.WARNING,
                            ResourceLoggingConstansts.UNABLE_TO_UNDEPLOY,
                            params);
                }
            } catch (Exception e) {
                Object[] params = {org.glassfish.resourcebase.resources.util.ResourceUtil.getGenericResourceInfo(resource), e};
                logger.log(Level.WARNING, ResourceLoggingConstansts.UNABLE_TO_UNDEPLOY_EXCEPTION, params);
            } finally {
                removeListenerForResource(resource);
            }
        }
    }

    /**
     * Notification that @Configured objects that were injected have changed
     *
     * @param events list of changes
     */
    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new PropertyChangeHandler(events), logger);
    }

    class PropertyChangeHandler implements Changed {

        PropertyChangeEvent[] events;

        private PropertyChangeHandler(PropertyChangeEvent[] events) {
            this.events = events;
        }

        /**
         * Notification of a change on a configuration object
         *
         * @param type            type of change : ADD mean the changedInstance was added to the parent
         *                        REMOVE means the changedInstance was removed from the parent, CHANGE means the
         *                        changedInstance has mutated.
         * @param changedType     type of the configuration object
         * @param changedInstance changed instance.
         */
        @Override
        public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
            NotProcessed np = null;
            ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
            try {
                ClassLoader ccl = clh.getConnectorClassLoader(null);
                Thread.currentThread().setContextClassLoader(ccl);
                switch (type) {
                    case ADD:
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("A new " + changedType.getName() + " was added : " + changedInstance);
                        }
                        np = handleAddEvent(changedInstance);
                        break;

                    case CHANGE:
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("A " + changedType.getName() + " was changed : " + changedInstance);
                        }
                        np = handleChangeEvent(changedInstance);
                        break;

                    case REMOVE:
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("A " + changedType.getName() + " was removed : " + changedInstance);
                        }
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

        private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T instance) {
            NotProcessed np = null;
            //TODO V3 handle enabled / disabled / resource-ref / redeploy ?
            try {
                if (ResourceUtil.isValidEventType(instance)) {
                    ResourceDeployer<ConfigBeanProxy> deployer = getResourceDeployer(instance);
                    boolean enabledAttributeChange = false;
                    if (instance instanceof BindableResource ||
                        instance instanceof ServerResource) {
                        Resource resource = (Resource) instance;
                        for (PropertyChangeEvent event : events) {
                            String propertyName = event.getPropertyName();
                            //Depending on the type of event (disable/enable, invoke the
                            //method on deployer.
                            if ("enabled".equalsIgnoreCase(propertyName)) {
                                enabledAttributeChange = true;
                                if (deployer != null) {
                                    boolean newValue = Boolean.valueOf(event.getNewValue().toString());
                                    boolean oldValue = Boolean.valueOf(event.getOldValue().toString());
                                    if (!(newValue && oldValue)) {
                                        if (resource instanceof ServerResource) {
                                            if (!isServerResourceEnabled(resource)) {
                                                // if pvs value was enabled AND new config is disabled, disable
                                                deployer.disableResource(resource);
                                            } else {
                                                // if pvs value was disabled, new config is Enabled
                                                deployer.enableResource(resource);
                                            }
                                        } else if (resource instanceof BindableResource){
                                            if (newValue) {
                                                deployer.enableResource(resource);
                                            } else {
                                                deployer.disableResource(resource);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!enabledAttributeChange && deployer != null) {
                        if (instance instanceof BindableResource) {
                            BindableResource bindableResource = (BindableResource) instance;
                            if (getEnabledResourceRefforResource(bindableResource)
                                && Boolean.parseBoolean(bindableResource.getEnabled())) {
                                deployer.redeployResource(bindableResource);
                            }
                        } else if (instance instanceof ServerResource){
                            redeployResource((ServerResource) instance);
                        } else {
                            deployer.redeployResource(instance);
                        }
                    }
                } else if (ResourceUtil.isValidEventType(instance.getParent())) {
                    //Added in case of a property change
                    //check for validity of the property's parent and redeploy
                    if (instance.getParent() instanceof BindableResource) {
                        BindableResource bindableResource = (BindableResource) instance.getParent();
                        if (getEnabledResourceRefforResource(bindableResource)
                            && Boolean.parseBoolean(bindableResource.getEnabled())) {
                            ResourceDeployer<ConfigBeanProxy> parentDeployer = getResourceDeployer(
                                instance.getParent());
                            if (parentDeployer != null) {
                                parentDeployer.redeployResource(instance.getParent());
                            }
                        }
                    } else if (instance instanceof ServerResource){
                        redeployResource((Resource) instance);
                    } else {
                        ResourceDeployer<ConfigBeanProxy> parentDeployer = getResourceDeployer(instance.getParent());
                        if (parentDeployer != null) {
                            parentDeployer.redeployResource(instance.getParent());
                        }
                    }
                } else if (instance instanceof ResourceRef) {
                    ResourceRef ref = (ResourceRef) instance;
                    String refName = ref.getRef();
                    Resource resource = ResourceUtil.getResourceByName(Resource.class, domain.getResources(), refName);
                    ResourceDeployer<Resource> deployer = getResourceDeployer(resource);
                    if (deployer != null) {
                        for (PropertyChangeEvent event : events) {
                            String propertyName = event.getPropertyName();
                            //Depending on the type of event (disable/enable, invoke the
                            //method on deployer.
                            if ("enabled".equalsIgnoreCase(propertyName)) {
                                if (resource instanceof ServerResource) {
                                    if (!isServerResourceEnabled(resource)) {
                                        // should be no op if already disabled
                                        deployer.disableResource(resource);
                                    } else {
                                        // should be no op if already enabled
                                        deployer.enableResource(resource);
                                    }
                                } else {
                                    //both cannot be true or false
                                    boolean newValue = Boolean.parseBoolean(event.getNewValue().toString());
                                    boolean oldValue = Boolean.parseBoolean(event.getOldValue().toString());
                                    if (!(newValue && oldValue)) {
                                        if (newValue) {
                                            deployer.enableResource(resource);
                                        } else {
                                            deployer.disableResource(resource);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, ResourceLoggingConstansts.ERROR_HANDLE_CHANGE_EVENT, ex);
                np = new NotProcessed(
                        localStrings.getLocalString(
                                "resources.resource-manager.change-event-failed",
                                "Change event failed"));
            }
            return np;
        }

        private <T extends ConfigBeanProxy> NotProcessed handleAddEvent(T instance) {
            NotProcessed np = null;
            //Add listener to the changed instance object
            ResourceManager.this.addListenerToResource(instance);

            if (instance instanceof BindableResource) {
                //ignore - as bindable-resources are handled during resource-ref creation
            } else if (instance instanceof ServerResource) {
                //ignore - as bindable-resources are handled during resource-ref creation
            } else if (instance instanceof ResourcePool) {
                //ignore - as pools are handled lazily
            } else if (instance instanceof Resource) {
                //only resource type left are RAC and CWSM
                try {
                    ResourceDeployer deployer = getResourceDeployer(instance);
                    if (deployer != null) {
                        deployer.deployResource(instance);
                    }
                } catch (Exception e) {
                    Object params[] = {ResourceUtil.getGenericResourceInfo((Resource) instance), e};
                    logger.log(Level.WARNING, ResourceLoggingConstansts.UNABLE_TO_DEPLOY, params);
                }
            } else if (instance instanceof Property) {
                //Property is not handled here. It is handled as part of the
                //Change event of the resource.
            } else if (instance instanceof ResourceRef) {
                //create-resource-ref
                ResourceRef ref = (ResourceRef) instance;
                Resource resource = ResourceUtil.getResourceByIdentity(domain.getResources(), ref.getRef());
                if (resource instanceof BindableResource) {
                    BindableResource br = (BindableResource) resource;
                    if (Boolean.parseBoolean(ref.getEnabled()) && Boolean.parseBoolean(br.getEnabled())) {
                        SimpleJndiName jndiName = SimpleJndiName.of(br.getJndiName());
                        ResourceInfo resourceInfo = new ResourceInfo(jndiName);
                        resourcesBinder.deployResource(resourceInfo, br);
                    }
                } else if (resource instanceof ServerResource) {
                    try {
                        deployServerResource(resource);
                    } catch (Exception e) {
                        Object params[] = {
                                ResourceUtil.getGenericResourceInfo(
                                        (Resource) instance), e };
                        logger.log(
                                Level.WARNING,
                                ResourceLoggingConstansts.UNABLE_TO_DEPLOY,
                                params);
                    }
                }
            }
            return np;
        }

        private <T extends ConfigBeanProxy> NotProcessed handleRemoveEvent(final T instance) {
            NotProcessed np = null;
            try {
                //this check ensures that a valid resource is handled
                if (instance instanceof BindableResource ||
                    instance instanceof ServerResource) {
                    //ignore as bindable-resources will have resource-ref.
                    ResourceManager.this.removeListenerForResource(instance);
                } else if (instance instanceof Resource) {
                    //Remove listener from the removed instance
                    ResourceManager.this.removeListenerForResource(instance);
                    //get appropriate deployer and undeploy resource
                    ResourceDeployer<T> resourceDeployer = getResourceDeployer(instance);
                    if (resourceDeployer != null) {
                        resourceDeployer.undeployResource(instance);
                    }
                } else if (ResourceUtil.isValidEventType(instance.getParent())) {
                    //Added in case of a property remove
                    //check for validity of the property's parent and redeploy
                    if (instance.getParent() instanceof BindableResource) {
                        BindableResource bindableResource = (BindableResource) instance.getParent();
                        if (getEnabledResourceRefforResource(bindableResource) && Boolean.valueOf(bindableResource.getEnabled())) {
                            ResourceDeployer<ConfigBeanProxy> parentDeployer = getResourceDeployer(instance.getParent());
                            if (parentDeployer != null) {
                                parentDeployer.redeployResource(instance.getParent());
                            }
                        }
                    } else {
                        redeployResource((Resource) instance.getParent());
                    }

                } else if (instance instanceof ResourceRef) {
                    //delete-resource-ref
                    ResourceRef ref = (ResourceRef) instance;
                    Resource resource = ResourceUtil.getResourceByName(
                            Resource.class, domain.getResources(), ref.getRef());
                    //get appropriate deployer and undeploy resource
                    if (resource instanceof BindableResource) {
                        boolean resourceEnabled = isResourceEnabled(resource);
                        if (resourceEnabled && Boolean.valueOf(ref.getEnabled())) {
                            ResourceDeployer<Resource> resourceDeployer = getResourceDeployer(resource);
                            if (resourceDeployer != null) {
                                resourceDeployer.undeployResource(resource);
                            }
                        }
                    } else if (resource instanceof ServerResource) {
                        undeployServerResource(resource);
                    }
                    //Remove listener from the removed instance
                    ResourceManager.this.removeListenerForResource(instance);
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING,ResourceLoggingConstansts.ERROR_HANDLE_REMOVE_EVENT,ex);
                np = new NotProcessed(
                        localStrings.getLocalString(
                                "resources.resource-manager.remove-event-failed",
                                "Remove event failed"));
            }
            return np;
        }

        private boolean isResourceEnabled(Resource resource) {
            boolean resourceEnabled = resource instanceof BindableResource
                ? Boolean.parseBoolean(((BindableResource) resource).getEnabled())
                : isServerResourceEnabled(resource);
            return resourceEnabled;
        }
    }

    /**
     * Add listener to all resources
     * Invoked from postConstruct()
     *
     * @param resources list of resources for which listeners will be registered.
     */
    private void addListenerToResources(Collection<Resource> resources) {
        for (Resource configuredResource : resources) {
            addListenerToResource(configuredResource);
        }
    }

    private boolean getEnabledResourceRefforResource(BindableResource bindableResource) {
        for (ResourceRef ref : getResourceRefs()) {
            if (ref.getRef().equals(bindableResource.getJndiName())) {
                return Boolean.valueOf(ref.getEnabled());
            }
        }
        return false;
    }

    private void addListenerToResourceRefs() {
        for (ResourceRef ref : getResourceRefs()) {
            addListenerToResource(ref);
        }
    }

    private List<ResourceRef> getResourceRefs() {
        //Instead of injecting ResourceRef[] config array (which will inject all resource-refs in domain.xml
        //including the ones in other server instances), get appropriate instance's resource-refs alone.
        Server server = getServerBean();
        List<ResourceRef> resourceRefs = new ArrayList<>(server.getResourceRef());
        // MAC 10/2012 -- If not in cluster, add config refs
        Config config = server.getConfig();
        if (config != null) {
            resourceRefs.addAll(config.getResourceRef());
        }
        return resourceRefs;
    }

    /**
     * Add listener to a generic resource
     * Used in the case of create asadmin command when listeners have to
     * be added to the specific resource
     *
     * @param instance instance to which listener will be registered
     */
    private void addListenerToResource(Object instance) {
        ObservableBean bean = null;
        //add listener to all types of Resource
        if (instance instanceof Resource) {
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) instance);
            bean.addListener(this);
        } else if (instance instanceof ResourceRef) {
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) instance);
            bean.addListener(this);
        }
    }


    /**
     * Remove listener from a resource
     * Used in the case of delete asadmin command
     *
     * @param instance remove the resource from listening to resource events
     */
    private void removeListenerForResource(Object instance) {
        ObservableBean bean = null;

        if (instance instanceof Resource) {
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) instance);
            bean.removeListener(this);
        } else if (instance instanceof ResourceRef) {
            bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) instance);
            bean.removeListener(this);
        }
    }

    /**
     * Remove listener from all resource refs.
     * Invoked from preDestroy().
     */
    private void removeListenerForResourceRefs() {
        for (ResourceRef ref : getResourceRefs()) {
            removeListenerForResource(ref);
        }
    }

    private void removeListenerForResources() {
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(domain.getResources());
        bean.removeListener(this);
    }

    /**
     * Remove listener from all resources
     */
    private void removeListenerForAllResources() {
        for (Resource configuredResource : domain.getResources().getResources()) {
            removeListenerForResource(configuredResource);
        }
    }

    /**
     * Given a <i>resource</i> instance, appropriate deployer will be provided
     *
     * @param resource resource instance
     * @return ResourceDeployer
     */
    private <T extends ConfigBeanProxy> ResourceDeployer<T> getResourceDeployer(T resource) {
        return resourceManagerFactoryProvider.get().getResourceDeployer(resource);
    }

    private boolean isServerResourceEnabled(Resource res) {
        ResourceDeployer<Resource> deployer = getResourceDeployer(res);
        return isServerResourceEnabled(res, deployer);
    }

    private boolean isServerResourceEnabled(Resource res, ResourceDeployer<Resource> deployer) {
        if (res instanceof ServerResource && deployer != null) {
            ResourceDeployerValidator deployerValidator = getResourceDeployerValidator(deployer);
            if (deployerValidator != null && deployerValidator.isEnabledLocally(res)) {
                return true;
            }
        }
        return false;
    }

    private boolean isServerResourceDeployed(Resource resource) {
        ResourceDeployer<Resource> deployer = getResourceDeployer(resource);
        if (resource instanceof ServerResource && deployer != null) {
            ResourceDeployerValidator deployerValidator = getResourceDeployerValidator(deployer);
            if (deployerValidator != null && deployerValidator.isDeployedLocally(resource)) {
                return true;
            }
        }
        return false;
    }

    private void deployServerResource(Resource resource) {
        ResourceDeployer<Resource> deployer = getResourceDeployer(resource);
        if (deployer == null) {
            logger.log(
                    Level.WARNING,
                    ResourceLoggingConstansts.UNABLE_TO_FIND_RESOURCEDEPLOYER,
                    new Object[] { resource.getIdentity() }
                    );
            return;
        }
        try {
            if (isServerResourceEnabled(resource, deployer)) {
                deployer.deployResource(resource); // deploy the resource; should be a no-op on the deployer if already deployed
            } else if (isServerResourceDeployed(resource)) {
                deployer.undeployResource(resource); // resource is not enabled in the config, undeploy it if the resource is already deployed
            }
        } catch (Exception e) {
            Object[] params = {
                    ResourceUtil.getGenericResourceInfo(resource), e };
            logger.log(
                    Level.WARNING,
                    ResourceLoggingConstansts.UNABLE_TO_DEPLOY,
                    params);
        }
    }

    private void undeployServerResource(Resource resource) {
        ResourceDeployer<Resource> deployer = getResourceDeployer(resource);
        if (deployer == null) {
            logger.log(
                    Level.WARNING,
                    ResourceLoggingConstansts.UNABLE_TO_FIND_RESOURCEDEPLOYER,
                    new Object[] { resource.getIdentity() }
                    );
            return;
        }
        try {
            ResourceDeployerValidator validator = getResourceDeployerValidator(deployer);
            if (validator != null) {
                boolean enabledLocally = validator.isEnabledLocally(resource);
                boolean deployed = validator.isDeployedLocally(resource);
                if (deployed && !enabledLocally) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Undeploy resource " + resource.getIdentity());
                    }
                    // resource is deployed locally and
                    // is no longer enabled in the config
                    deployer.undeployResource(resource);
                } else if (enabledLocally) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Deploy resource " + resource.getIdentity());
                    }
                    // a Resource or ResourceRef was removed,
                    // but it's now enabled in the config (e.g., through
                    // inheritance from a Config bean), so
                    // deploy the resource
                    deployer.deployResource(resource);
                }
            }
        } catch (Exception e) {
            Object[] params = {
                    ResourceUtil.getGenericResourceInfo(resource), e };
            logger.log(
                    Level.WARNING,
                    ResourceLoggingConstansts.UNABLE_TO_DEPLOY,
                    params);
        }
    }

    private void redeployResource(Resource resource) throws Exception {
        ResourceDeployer<Resource> resourceDeployer = getResourceDeployer(resource);
        if (resourceDeployer != null) {
            ResourceDeployerValidator validator = getResourceDeployerValidator(resourceDeployer);
            if (validator == null) {
                // No validator, just call deployer
                resourceDeployer.redeployResource(resource);
            } else {
                if (validator.isEnabledLocally(resource)) {
                    // Resource is currently enabled in the config,
                    // call the redeploy op; the ResourceDeployer should
                    // be able to handle this scenario
                    resourceDeployer.redeployResource(resource);
                } else if (validator.isDeployedLocally(resource)) {
                    // Resource is disabled in new config, and resource
                    // is currently active, so undeploy
                    resourceDeployer.undeployResource(resource);
                }
            }
        }
    }


    private ResourceDeployerValidator getResourceDeployerValidator(ResourceDeployer deployer) {
        ResourceDeployerInfo annotation = deployer.getClass().getAnnotation(ResourceDeployerInfo.class);
        ResourceDeployerValidator deployerValidator = locator.getService(annotation.validator());
        return deployerValidator;
    }
}
