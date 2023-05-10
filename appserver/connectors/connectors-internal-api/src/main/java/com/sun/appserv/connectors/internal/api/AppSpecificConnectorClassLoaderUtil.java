/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.deployment.runtime.connector.SunConnector;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.connectors.config.ConnectorService;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;

@Service
public class AppSpecificConnectorClassLoaderUtil {

    @Inject
    private ApplicationRegistry appRegistry;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Provider<ConnectorService> connectorServiceProvider;

    @Inject
    private Provider<ConnectorsClassLoaderUtil> connectorsClassLoaderUtilProvider;

    @Inject
    private Provider<Domain> domainProvider;

    @Inject
    private Provider<Applications> applicationsProvider;

    private static final Logger LOG = LogDomains.getLogger(AppSpecificConnectorClassLoaderUtil.class,
        LogDomains.RSR_LOGGER, false);


    public void detectReferredRARs(String appName) {
        ApplicationInfo appInfo = appRegistry.get(appName);

        //call to detectReferredRAs can be called only when appInfo is available
        if (appInfo == null) {
            throw new IllegalStateException("ApplicationInfo is not available for application [ " + appName + " ]");
        }
        Application app = appInfo.getMetaData(Application.class);

        if (!appInfo.isJakartaEEApp()) {
            LOG.log(Level.FINEST, "Application [{0}] is not a Jakarta EE application, skipping"
                + " resource-adapter references detection", appName);
            return;
        }

        // Iterate through all bundle descriptors, ejb-descriptors, managed-bean descriptors
        // for references to resource-adapters
        //
        // References can be via :
        // resource-ref
        // resource-env-ref
        // ra-mid
        //
        // Resource definition can be found in :
        // domain.xml
        // sun-ra.xml
        // default connector resource

        //handle application.xml bundle descriptor
        processDescriptorForRAReferences(app, null, app);

        Collection<BundleDescriptor> bundleDescriptors = app.getBundleDescriptors();

        //TODO Similar reference checking mechanism is used in DataSourceDefinitionDeployer. Merge them ?
        //bundle descriptors
        for (BundleDescriptor bundleDesc : bundleDescriptors) {
            String moduleName = getModuleName(bundleDesc, app);
            processDescriptorForRAReferences(app, bundleDesc, moduleName);
            Collection<RootDeploymentDescriptor> dds = bundleDesc.getExtensionsDescriptors();
            if(dds != null){
                for(RootDeploymentDescriptor dd : dds){
                    processDescriptorForRAReferences(app, dd, moduleName);
                }
            }
        }
    }

    private void processDescriptorForRAReferences(Application app, Descriptor descriptor, String moduleName) {
        if (descriptor instanceof JndiNameEnvironment) {
            processDescriptorForRAReferences(app, moduleName, (JndiNameEnvironment) descriptor);
        }
        // ejb descriptors
        if (descriptor instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbDesc = (EjbBundleDescriptor) descriptor;
            Set<? extends EjbDescriptor> ejbDescriptors = ejbDesc.getEjbs();
            for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                processDescriptorForRAReferences(app, moduleName, ejbDescriptor);

                if (ejbDescriptor instanceof EjbMessageBeanDescriptor) {
                    EjbMessageBeanDescriptor messageBeanDesc = (EjbMessageBeanDescriptor) ejbDescriptor;
                    String raMid = messageBeanDesc.getResourceAdapterMid();
                    //there seem to be applications that do not specify ra-mid
                    if (raMid != null) {
                        app.addResourceAdapter(raMid);
                    }
                }
            }
            //ejb interceptors
            Set<EjbInterceptor> ejbInterceptors = ejbDesc.getInterceptors();
            for (EjbInterceptor ejbInterceptor : ejbInterceptors) {
                processDescriptorForRAReferences(app, moduleName, ejbInterceptor);
            }

        }
        if(descriptor instanceof BundleDescriptor){
            // managed bean descriptors
            Set<ManagedBeanDescriptor> managedBeanDescriptors = ((BundleDescriptor)descriptor).getManagedBeans();
            for (ManagedBeanDescriptor mbd : managedBeanDescriptors) {
                processDescriptorForRAReferences(app, moduleName, mbd);
            }
        }
    }

    private String getModuleName(BundleDescriptor bundleDesc, Application app) {
        Set<ModuleDescriptor<BundleDescriptor>> moduleDescriptors = app.getModules();
        if(moduleDescriptors != null){
            for(ModuleDescriptor moduleDesc : moduleDescriptors){
                if(bundleDesc.equals(moduleDesc.getDescriptor())){
                    return moduleDesc.getModuleName();
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getRARsReferredByApplication(String appName) {
        ApplicationInfo appInfo = appRegistry.get(appName);
        if (appInfo != null) {
            Application app = appInfo.getMetaData(Application.class);
            if(appInfo.isJakartaEEApp()){
                return app.getResourceAdapters();
            }
        }
        return new HashSet<>();
    }


    private void processDescriptorForRAReferences(com.sun.enterprise.deployment.Application app, String moduleName,
        JndiNameEnvironment jndiEnv) {
        // resource-ref
        for (ResourceReferenceDescriptor resourceRef : jndiEnv.getResourceReferenceDescriptors()) {
            // ignore refs where jndi-name is not available
            if (resourceRef.getJndiName() != null) {
                detectResourceInRA(app, moduleName, resourceRef.getJndiName());
            }
        }

        // resource-env-ref
        for (ResourceEnvReferenceDescriptor resourceEnvRef : jndiEnv.getResourceEnvReferenceDescriptors()) {
            // ignore refs where jndi-name is not available
            if (resourceEnvRef.getJndiName() != null) {
                detectResourceInRA(app, moduleName, resourceEnvRef.getJndiName());
            }
        }
    }

    private void detectResourceInRA(Application app, String moduleName, SimpleJndiName jndiName) {
        //domain.xml
        Resource res = null;

        if (jndiName.isJavaApp()) {
            ApplicationInfo appInfo = appRegistry.get(app.getName());
            res = getApplicationScopedResource(jndiName, BindableResource.class, appInfo);
        } else if (jndiName.isJavaModule()) {
            ApplicationInfo appInfo = appRegistry.get(app.getName());
            res = getModuleScopedResource(jndiName, moduleName, BindableResource.class, appInfo);
        } else {
            res = getResources().getResourceByName(BindableResource.class, jndiName);
        }
        //embedded ra's resources may not be created yet as they can be created only after .ear deploy
        //  (and .ear may refer to these resources in DD)
        if (res != null) {
            if (ConnectorResource.class.isAssignableFrom(res.getClass())) {
                ConnectorResource connResource = (ConnectorResource)res;
                SimpleJndiName poolName = new SimpleJndiName(connResource.getPoolName());
                Resource pool ;
                ApplicationInfo appInfo = appRegistry.get(app.getName());
                if (jndiName.isJavaApp()) {
                    pool = getApplicationScopedResource(poolName, ResourcePool.class, appInfo);
                } else if (jndiName.isJavaModule()) {
                    pool = getModuleScopedResource(poolName, moduleName, ResourcePool.class, appInfo);
                } else {
                    pool = getResources().getResourceByName(ResourcePool.class, poolName);
                }
                if (ConnectorConnectionPool.class.isAssignableFrom(pool.getClass())) {
                    String raName = ((ConnectorConnectionPool) pool).getResourceAdapterName();
                    app.addResourceAdapter(raName);
                }
            } else if (AdminObjectResource.class.isAssignableFrom(res.getClass())) {
                String raName = ((AdminObjectResource) res).getResAdapter();
                app.addResourceAdapter(raName);
            }
        } else {
            boolean found = false;
            //detect sun-ra.xml

            // find all the standalone connector modules
            List<com.sun.enterprise.config.serverbeans.Application> applications =
                    getApplications().getApplicationsWithSnifferType(com.sun.enterprise.config.serverbeans.ServerTags.CONNECTOR, true);
            for (com.sun.enterprise.config.serverbeans.Application application : applications) {
                String appName = application.getName();
                        ApplicationInfo appInfo = appRegistry.get(appName);
                        // appInfo is null if application is not enabled or
                        // not referred in this instance.
                        if (appInfo == null) {
                            continue;
                        }
                        Application dolApp = appInfo.getMetaData(Application.class);
                        Collection<ConnectorDescriptor> rarDescriptors = dolApp.getBundleDescriptors(ConnectorDescriptor.class);
                        for (ConnectorDescriptor desc : rarDescriptors) {
                            SunConnector sunraDesc = desc.getSunDescriptor();
                            if (sunraDesc != null) {
                                SimpleJndiName sunRAJndiName = sunraDesc.getResourceAdapter()
                                    .getValue(ResourceAdapter.JNDI_NAME);
                                if (jndiName.equals(sunRAJndiName)) {
                                    app.addResourceAdapter(desc.getName());
                                    found = true;
                                    break;
                                }
                            } else {
                                //check whether it is default resource in the connector
                                if (desc.getDefaultResourcesNames().contains(jndiName)) {
                                    app.addResourceAdapter(desc.getName());
                                    found = true;
                                    break;
                                }
                            }
                        }
            }

            if (!found) {
                DOLUtils.getDefaultLogger().log(Level.FINEST, "Could not find resource by name: {0}", jndiName);
            }
        }
    }

    private <T> Resource getApplicationScopedResource(SimpleJndiName poolName, Class<T> class1, ApplicationInfo appInfo){
        Resource foundRes = null;
        if(appInfo != null){

            com.sun.enterprise.config.serverbeans.Application app =
                    appInfo.getTransientAppMetaData(com.sun.enterprise.config.serverbeans.ServerTags.APPLICATION,
                    com.sun.enterprise.config.serverbeans.Application.class);
            Resources resources = null;
            if(app != null){
                resources = appInfo.getTransientAppMetaData(app.getName()+"-resources", Resources.class);
            }
            if(resources != null){

            boolean bindableResource = BindableResource.class.isAssignableFrom(class1);
            boolean poolResource = ResourcePool.class.isAssignableFrom(class1);
            boolean workSecurityMap = WorkSecurityMap.class.isAssignableFrom(class1);
            boolean rac = ResourceAdapterConfig.class.isAssignableFrom(class1);

            for (Resource res : resources.getResources()) {
                    String resourceName = null;
                    if(bindableResource && res instanceof BindableResource){
                        resourceName = ((BindableResource)res).getJndiName();
                    } else if(poolResource && res instanceof ResourcePool){
                        resourceName = ((ResourcePool)res).getName();
                    } else if(rac && res instanceof ResourceAdapterConfig){
                        resourceName = ((ResourceAdapterConfig)res).getName();
                    } else if(workSecurityMap && res instanceof WorkSecurityMap){
                        resourceName = ((WorkSecurityMap)res).getName();
                    }
                    if (resourceName != null) {
                        if (!resourceName.startsWith(JNDI_CTX_JAVA_APP)) {
                            resourceName = JNDI_CTX_JAVA_APP + resourceName;
                        }
                        if (!poolName.isJavaApp()) {
                            poolName = new SimpleJndiName(JNDI_CTX_JAVA_APP + poolName);
                        }
                        if (poolName.toString().equals(resourceName)) {
                            foundRes = res;
                            break;
                        }
                    }
                }
            }
        }
        return foundRes;
    }

    private <T> Resource getModuleScopedResource(SimpleJndiName name, String moduleName, Class<T> type, ApplicationInfo appInfo){
        Resource foundRes = null;
        if(appInfo != null){

            com.sun.enterprise.config.serverbeans.Application app =
                    appInfo.getTransientAppMetaData(com.sun.enterprise.config.serverbeans.ServerTags.APPLICATION,
                    com.sun.enterprise.config.serverbeans.Application.class);
            Resources resources = null;
            if(app != null){
                Module module = null;
                List<Module> modules = app.getModule();
                for(Module m : modules){
                    if(ConnectorsUtil.getActualModuleName(m.getName()).equals(moduleName)){
                        module = m;
                        break;
                    }
                }
                if(module != null){
                    resources = appInfo.getTransientAppMetaData(module.getName()+"-resources", Resources.class);
                }
            }
            if(resources != null){

            boolean bindableResource = BindableResource.class.isAssignableFrom(type);
            boolean poolResource = ResourcePool.class.isAssignableFrom(type);
            boolean workSecurityMap = WorkSecurityMap.class.isAssignableFrom(type);
            boolean rac = ResourceAdapterConfig.class.isAssignableFrom(type);

            for (Resource res : resources.getResources()) {
                String resourceName = null;
                if(bindableResource && res instanceof BindableResource){
                    resourceName = ((BindableResource)res).getJndiName();
                } else if(poolResource && res instanceof ResourcePool){
                    resourceName = ((ResourcePool)res).getName();
                } else if(rac && res instanceof ResourceAdapterConfig){
                    resourceName = ((ResourceAdapterConfig)res).getName();
                } else if(workSecurityMap && res instanceof WorkSecurityMap){
                    resourceName = ((WorkSecurityMap)res).getName();
                }
                if (resourceName != null) {
                    if (!resourceName.startsWith(JNDI_CTX_JAVA_MODULE)) {
                        resourceName = JNDI_CTX_JAVA_MODULE + resourceName;
                    }
                    if (!name.isJavaModule()) {
                        name = new SimpleJndiName(JNDI_CTX_JAVA_MODULE + name);
                    }
                    if (name.toString().equals(resourceName)) {
                        foundRes = res;
                        break;
                    }
                }
            }
            }
        }
        return foundRes;
    }

    public Collection<ConnectorClassFinder> getSystemRARClassLoaders() {
        try {
            return getConnectorsClassLoaderUtil().getSystemRARClassLoaders();
        } catch (ConnectorRuntimeException cre) {
            throw new RuntimeException(cre.getMessage(), cre);
        }
    }

    public boolean useGlobalConnectorClassLoader() {
        boolean flag = false;
        ConnectorService connectorService = connectorServiceProvider.get();
        //it is possible that connector-service is not yet defined in domain.xml
        if(connectorService != null){
            String classLoadingPolicy = connectorService.getClassLoadingPolicy();
            if (classLoadingPolicy != null &&
                    classLoadingPolicy.equals(ConnectorConstants.CLASSLOADING_POLICY_GLOBAL_ACCESS)) {
                flag = true;
            }
        }
        return flag;
    }

    public Collection<String> getRequiredResourceAdapters(String appName) {
        List<String> requiredRars = new ArrayList<>();
        if (appName != null) {
            ConnectorService connectorService = connectorServiceProvider.get();
            //it is possible that connector-service is not yet defined in domain.xml

            if (connectorService != null) {
                if (appName.trim().length() > 0) {
                    Property property = connectorService.getProperty(appName.trim());
                    if (property != null) {
                        String requiredRarsString = property.getValue();
                        StringTokenizer tokenizer = new StringTokenizer(requiredRarsString, ",");
                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken().trim();
                            requiredRars.add(token);
                        }
                    }
                }
            }
        }
        return requiredRars;
    }

    private ConnectorsClassLoaderUtil getConnectorsClassLoaderUtil() {
        return connectorsClassLoaderUtilProvider.get();
    }

    private Resources getResources() {
        return domainProvider.get().getResources();
    }

    private Applications getApplications() {
        return applicationsProvider.get();
    }
}
