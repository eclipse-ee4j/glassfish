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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeExtension;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.RelativePathResolver;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resources.api.ResourcesRegistry;
import org.jvnet.hk2.config.ConfigBeanProxy;


public class ResourcesUtil {

    static Logger _logger = LogDomains.getLogger(ResourcesUtil.class,LogDomains.RSR_LOGGER);

    static StringManager localStrings = StringManager.getManager(ResourcesUtil.class);

    static ServerContext sc_;

    private volatile static ResourcesUtil resourcesUtil;


    protected Domain domain;
    private Server server;
    private ConnectorRuntime runtime;

    private ResourcesUtil(){
    }

    public Resources getGlobalResources(){
        return getRuntime().getResources();
    }
    public Resources getResources(ResourceInfo resourceInfo) {
        return getRuntime().getResources(resourceInfo);
    }

    public Resources getResources(PoolInfo poolInfo) {
        return getRuntime().getResources(poolInfo);
    }

    private Domain getDomain(){
        if(domain == null){
            domain = getRuntime().getDomain();
        }
        return domain;
    }

    private ConnectorRuntime getRuntime(){
        if(runtime == null){
            runtime = ConnectorRuntime.getRuntime();
        }
        return runtime;
    }

    private Server getServer(){
        if(server == null){
            server = getDomain().getServerNamed(getRuntime().getServerEnvironment().getInstanceName());
        }
        return server;
    }

    private Applications getApplications(){
        return getRuntime().getApplications();
    }

    private Application getApplicationByName(String name){
        List<Application> apps = getApplications().getApplications();
        for(Application app : apps){
            if(app.getName().equals(name)){
                return app;
            }
        }
        return null;
    }

    /**
     * Gets the deployment location for a J2EE application.
     * @param appName application name
     * @return application deploy location
     */
    public String getApplicationDeployLocation(String appName) {
        String location = null;
        Application app = getApplicationByName(appName);
        if(app != null){
            //TODO V3 with annotations, is this right location ?
            location = RelativePathResolver.resolvePath(app.getLocation());
        }
        return location;
    }


    public boolean belongToStandAloneRar(String resourceAdapterName) {
        return false;
    }

    public static ResourcesUtil createInstance() {
        //stateless, no synchronization needed
        if(resourcesUtil == null){
            synchronized(ResourcesUtil.class) {
                if(resourcesUtil == null) {
                    resourcesUtil = new ResourcesUtil();
                }
            }
        }
        return resourcesUtil;
    }

    public DeferredResourceConfig getDeferredResourceConfig(Object resource, Object pool, String resType, String raName)
            throws ConnectorRuntimeException {
        String resourceAdapterName ;
        DeferredResourceConfig resConfig = null;
        //TODO V3 there should not be res-type related check, refactor deferred-ra-config
        //TODO V3 (not to hold specific resource types)
        if (resource instanceof ConnectorResource || pool instanceof ConnectorConnectionPool) {
            ConnectorConnectionPool connPool = (ConnectorConnectionPool) pool;
            ConnectorResource connResource = (ConnectorResource) resource;
            resourceAdapterName = connPool.getResourceAdapterName();

            //TODO V3 need to get AOR & RA-Config later
            resConfig = new DeferredResourceConfig(resourceAdapterName, null, connPool, connResource, null);

            Resource[] resourcesToload = new Resource[]{connPool, connResource};
            resConfig.setResourcesToLoad(resourcesToload);

        } else {
            Collection<ConnectorRuntimeExtension> extensions =
                    Globals.getDefaultHabitat().getAllServices(ConnectorRuntimeExtension.class);
            for (ConnectorRuntimeExtension extension : extensions) {
                return extension.getDeferredResourceConfig(resource, pool, resType, raName);
            }
        }
        return resConfig;
    }


    /**
     * Returns true if the given resource is referenced by this server.
     *
     * @param resourceInfo the name of the resource
     * @return true if the named resource is used/referred by this server
     */
    public boolean isReferenced(ResourceInfo resourceInfo) {
        boolean refExists = false;
        if (ConnectorsUtil.isModuleScopedResource(resourceInfo) ||
                ConnectorsUtil.isApplicationScopedResource(resourceInfo)) {
            refExists = getServer().getApplicationRef(resourceInfo.getApplicationName()) != null;
        } else {
            refExists = getServer().isResourceRefExists(resourceInfo.getName());
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("isReferenced :: " + resourceInfo + " - " + refExists);
        }
        return refExists;
    }

    public boolean isEnabled(Application application){
        if(application == null){
            return false;
        }
        boolean appEnabled = Boolean.parseBoolean(application.getEnabled());
        ApplicationRef appRef = getServer().getApplicationRef(application.getName());
        boolean appRefEnabled = false;
        if(appRef != null ){
            appRefEnabled = Boolean.parseBoolean(appRef.getEnabled());
        }
        return appEnabled && appRefEnabled;
    }

    /**
     * Checks if a Resource is enabled.
     * <p/>
     * Since 8.1 PE/SE/EE, A resource [except resource adapter configs, connector and
     * JDBC connection pools which are global and hence enabled always] is enabled
     * only when the resource is enabled and there exists a resource ref to this
     * resource in this server instance and that resource ref is enabled.
     * <p/>
     * Before a resource is loaded or deployed, it is checked to see if it is
     * enabled.
     *
     * @since 8.1 PE/SE/EE
     */
    public boolean isEnabled(Resource resource) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourcesUtil :: isEnabled");
        }
        if (resource == null) {
            return false;
        } else if (resource instanceof BindableResource) {
            BindableResource bindableResource = (BindableResource) resource;
            if (bindableResource.getJndiName().contains(ResourceConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX)) {
                return Boolean.parseBoolean(bindableResource.getEnabled());
            }
            ResourceRef resRef = getServer().getResourceRef(SimpleJndiName.of(bindableResource.getJndiName()));
            return isEnabled(bindableResource) && resRef != null && parseBoolean(resRef.getEnabled());
        } else if (resource instanceof ResourcePool) {
            return isEnabled((ResourcePool) resource);
        } else if (resource instanceof WorkSecurityMap || resource instanceof ResourceAdapterConfig) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isEnabled(ResourcePool pool) {
        boolean enabled = true;
        if(pool == null) {
            return false;
        }
        if(pool instanceof ConnectorConnectionPool) {
            ConnectorConnectionPool ccpool = (ConnectorConnectionPool) pool;
            String raName = ccpool.getResourceAdapterName();
            enabled = isRarEnabled(raName);
        }
        //JDBC RA is system RA and is always enabled
        return enabled;
    }

    public boolean isEnabled(BindableResource br, ResourceInfo resourceInfo){
        boolean enabled = false;
        //this cannot happen? need to remove later?
        if (br == null) {
            return false;
        }
        boolean resourceEnabled = ConnectorsUtil.parseBoolean(br.getEnabled());

        //TODO can we also check whether the application in which it is defined is enabled (app and app-ref) ?
        if(resourceInfo.getName().contains(ResourceConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX)){
            return resourceEnabled;
        }

        boolean refEnabled = isResourceReferenceEnabled(resourceInfo);

        if(br instanceof ConnectorResource) {
            ConnectorResource cr = (ConnectorResource) br;
            SimpleJndiName poolName = new SimpleJndiName(cr.getPoolName());
            ConnectorConnectionPool ccp = getResources(resourceInfo).getResourceByName(ConnectorConnectionPool.class,
                poolName);
            if (ccp == null) {
                return false;
            }
            boolean poolEnabled = isEnabled(ccp);
            enabled  = poolEnabled && resourceEnabled && refEnabled ;
        } else if(br instanceof AdminObjectResource) {
            //AdminObjectResource aor = (AdminObjectResource) br;
           // String raName = aor.getResAdapter();
            if(/* TODO isRarEnabled &&*/ resourceEnabled && refEnabled){
                enabled = true;
            }
        } else if(refEnabled && resourceEnabled){
            //other bindable resources need to be checked for "resource.enabled" and "resource-ref.enabled"
            enabled = true;
        }
        return enabled;
    }

    public boolean isEnabled(BindableResource br) {
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(br);
        return isEnabled(br, resourceInfo);
    }

    private boolean isRarEnabled(String raName) {
        if(raName == null || raName.isEmpty()) {
            return false;
        }
        Application application = getDomain().getApplications().getApplication(raName);
        if(application != null) {
            return isApplicationReferenceEnabled(raName);
        } else if(ConnectorsUtil.belongsToSystemRA(raName)) {
            return true;
        } else {
            return belongToEmbeddedRarAndEnabled(raName);
        }
    }

    /**
     * Checks if the application reference is enabled
     * @param appName application-name
     * @since SJSAS 9.1 PE/SE/EE
     * @return boolean indicating the status
     */
    private boolean isApplicationReferenceEnabled(String appName) {
        ApplicationRef appRef = getServer().getApplicationRef(appName);
        if (appRef == null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled null ref");
            }
            return false;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ResourcesUtil :: isApplicationReferenceEnabled appRef enabled ?" + appRef.getEnabled());
        }
        return ConnectorsUtil.parseBoolean(appRef.getEnabled());
    }

    public Collection<AdminObjectResource> getEnabledAdminObjectResources(String raName)  {
        Collection<AdminObjectResource> allResources = new ArrayList<>();
        allResources.addAll(getEnabledAdminObjectResources(raName, getGlobalResources()));
        return allResources;
    }

    //TODO can be made generic
    //TODO probably, default methods for resources
    public Collection<AdminObjectResource> getEnabledAdminObjectResources(String raName, Resources resources)  {
        List<AdminObjectResource> adminObjectResources = new ArrayList<>();
        for(Resource resource : resources.getResources(AdminObjectResource.class)) {

            AdminObjectResource adminObjectResource = (AdminObjectResource)resource;
            String resourceAdapterName = adminObjectResource.getResAdapter();

            // skips the admin resource if it is not referenced by the server
            if((resourceAdapterName == null) || (raName!= null && !raName.equals(resourceAdapterName)) || !isEnabled(adminObjectResource)) {
                continue;
            }
            adminObjectResources.add(adminObjectResource);
        }
        return adminObjectResources;
    }

    private boolean belongToEmbeddedRarAndEnabled(String resourceAdapterName)  {
        String appName = getAppNameToken(resourceAdapterName);
        if(appName==null) {
            return false;
        }
        Applications apps = getDomain().getApplications();
        Application app = apps.getApplication(appName);
        if(app == null || !ConnectorsUtil.parseBoolean(app.getEnabled())) {
            return false;
        }
        return isApplicationReferenceEnabled(appName);
    }

    private String getAppNameToken(String rarName) {
        if (rarName == null) {
            return null;
        }
        int index = rarName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER);
        return index == -1 ? null : rarName.substring(0, index);
    }


    /**
     * Checks if a resource reference is enabled
     * For application-scoped-resource, checks whether application-ref is enabled
     *
     * @param resourceInfo resourceInfo ResourceInfo
     * @return boolean indicating whether the resource-ref/application-ref is enabled.
     */
    private boolean isResourceReferenceEnabled(ResourceInfo resourceInfo) {
        String enabled = "false";
        if (ConnectorsUtil.isModuleScopedResource(resourceInfo) ||
                ConnectorsUtil.isApplicationScopedResource(resourceInfo)) {
            ApplicationRef appRef = getServer().getApplicationRef(resourceInfo.getApplicationName());
            if (appRef != null) {
                enabled = appRef.getEnabled();
            } else {
                // for an application-scoped-resource, if the application is being deployed,
                // <application> element and <application-ref> will be null until deployment
                // is complete. Hence this workaround.
                enabled = "true";
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("ResourcesUtil :: isResourceReferenceEnabled null app-ref");
                }
            }
        } else {
            ResourceRef ref = getServer().getResourceRef(resourceInfo.getName());
            if (ref == null) {
                _logger.fine("ResourcesUtil :: isResourceReferenceEnabled null ref");
            } else {
                enabled = ref.getEnabled();
            }
        }
        _logger.log(Level.FINE, "ResourcesUtil :: isResourceReferenceEnabled ref enabled? {0}", enabled);

        return ConnectorsUtil.parseBoolean(enabled);
    }


    public String getResourceType(ConfigBeanProxy cb) {
        if (cb instanceof ConnectorConnectionPool) {
            return ResourceConstants.RES_TYPE_CCP;
        } else if (cb instanceof ConnectorResource) {
            return ResourceConstants.RES_TYPE_CR;
        }
        Collection<ConnectorRuntimeExtension> extensions =
                Globals.getDefaultHabitat().getAllServices(ConnectorRuntimeExtension.class);
        for (ConnectorRuntimeExtension extension : extensions) {
            return extension.getResourceType(cb);
        }
        return null;
    }

    private boolean parseBoolean(String enabled) {
        return Boolean.parseBoolean(enabled);
    }

    public ConnectorDescriptor getConnectorDescriptorFromUri(String rarName, String raLoc) {
        try {
            String appName = rarName.substring(0, rarName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER));
            //String actualRarName = rarName.substring(rarName.indexOf(ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER) + 1);
            String appDeployLocation = ResourcesUtil.createInstance().getApplicationDeployLocation(appName);
            FileArchive in = ConnectorRuntime.getRuntime().getFileArchive();
            in.open(new URI(appDeployLocation));
            ApplicationArchivist archivist = ConnectorRuntime.getRuntime().getApplicationArchivist();
            com.sun.enterprise.deployment.Application application = archivist.open(in);
            return application.getModuleByTypeAndUri(ConnectorDescriptor.class, raLoc);
        } catch (Exception e) {
            Object params[] = new Object[]{rarName, e};
            _logger.log(Level.WARNING, "error.getting.connector.descriptor", params);
        }
        return null;
    }

    /**
     * Determines if a connector connection pool is referred in a
     * server-instance via resource-refs
     * @param poolInfo pool-name
     * @return boolean true if pool is referred in this server instance as well enabled, false
     * otherwise
     */
    public boolean isPoolReferredInServerInstance(PoolInfo poolInfo) {

        Collection<ConnectorResource> connectorResources = getRuntime().getResources(poolInfo).
                getResources(ConnectorResource.class);
        for (ConnectorResource resource : connectorResources) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("poolname " + resource.getPoolName() + " resource " + resource.getJndiName());
            }
            ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(resource);
            if (resource.getPoolName().equalsIgnoreCase(poolInfo.getName().toString()) && isReferenced(resourceInfo)
                && isEnabled(resource)) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Connector resource "  + resource.getJndiName() + " refers "
                        + poolInfo + " in this server instance and is enabled");
                }
                return true;
            }
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("No Connector resource refers [ " + poolInfo + " ] in this server instance");
        }
        return false;
    }

    public ResourcePool getPoolConfig(PoolInfo poolInfo){
        Resources resources = getResources(poolInfo);
        ResourcePool pool = null;
        if (resources != null) {
            pool = resources.getResourceByName(ResourcePool.class, poolInfo.getName());
        }
        return pool;
    }

    public ConnectorConnectionPool getConnectorConnectionPoolOfResource(ResourceInfo resourceInfo) {
        ConnectorResource resource = null;
        ConnectorConnectionPool pool = null;
        Resources resources = getResources(resourceInfo);
        if (resources != null) {
            resource = resources.getResourceByName(ConnectorResource.class, resourceInfo.getName());
            if (resource != null) {
                SimpleJndiName poolName = new SimpleJndiName(resource.getPoolName());
                pool = resources.getResourceByName(ConnectorConnectionPool.class, poolName);
            }
        }
        return pool;
    }

    public boolean isRARResource(Resource resource){
        return ConnectorsUtil.isRARResource(resource);
    }

    public String getRarNameOfResource(Resource resource, Resources resources){
        return ConnectorsUtil.getRarNameOfResource(resource, resources);
    }


    public <T extends Resource> T getResource(ResourceInfo resourceInfo, Class<T> resourceType) {
        String appName = resourceInfo.getApplicationName();
        SimpleJndiName jndiName = resourceInfo.getName();
        String moduleName = resourceInfo.getModuleName();
        Resources resources = null;
        if (ConnectorsUtil.isApplicationScopedResource(resourceInfo) ||
                ConnectorsUtil.isModuleScopedResource(resourceInfo)) {
            if (getApplicationByName(appName) != null) {
                resources = getResources(resourceInfo);
            }
        } else {
            resources = getResources(resourceInfo);
        }
        if (resources != null) {
            return resources.getResourceByName(resourceType, jndiName);
        }
        if (ConnectorsUtil.isApplicationScopedResource(resourceInfo)
            || ConnectorsUtil.isModuleScopedResource(resourceInfo)) {
            // it is possible that "application" is being deployed (eg: during deployment "prepare"
            // or application "start")

            // for app-scoped-resource, resource is stored in "app-name" key
            if (ConnectorsUtil.isApplicationScopedResource(resourceInfo)) {
                moduleName = appName;
            }

            resources = ResourcesRegistry.getResources(appName, moduleName);
            if (resources != null) {
                return resources.getResourceByName(resourceType, jndiName);
            }
        }
        return null;
    }

    public <T extends Resource> T getResource(SimpleJndiName jndiName, String appName, String moduleName, Class<T> resourceType) {
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, appName, moduleName);
        return getResource(resourceInfo, resourceType);
    }

    public Collection<Resource> filterConnectorResources(Resources allResources, String moduleName, boolean includePools) {
        //TODO V3 needed for redeploy of module, what happens to the listeners of these resources ?
        Collection<ConnectorConnectionPool> connectionPools =
                ConnectorsUtil.getAllPoolsOfModule(moduleName, allResources);
        Collection<String> poolNames = ConnectorsUtil.getAllPoolNames(connectionPools);
        Collection<Resource> resources = ConnectorsUtil.getAllResources(poolNames, allResources);
        Collection<AdminObjectResource> adminObjectResources = ResourcesUtil.createInstance()
            .getEnabledAdminObjectResources(moduleName);
        resources.addAll(adminObjectResources);
        if (includePools) {
            Collection<ConnectorConnectionPool> allPoolsOfModule = ConnectorsUtil.getAllPoolsOfModule(moduleName, allResources);
            resources.addAll(allPoolsOfModule);
        }
        return resources;
    }

}
