/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.ResourcePoolReference;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchiveEntry;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.connectors.config.ConnectorService;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.deployment.common.InstalledLibrariesResolver;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resourcebase.resources.api.ResourceConstants.TriState;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static com.sun.appserv.connectors.internal.api.ConnectorConstants.DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.JNDI_SUFFIX_VALUES;
import static com.sun.enterprise.util.SystemPropertyConstants.SLASH;

/**
 * Util class for connector related classes
 */
public class ConnectorsUtil {

    private static final Logger LOG = LogDomains.getLogger(ConnectorsUtil.class, LogDomains.RSR_LOGGER);

    private static Collection<String> validSystemRARs = new HashSet<>();
    private static Collection<String> validNonJdbcSystemRARs = new HashSet<>();

    static{
        initializeSystemRars();
        initializeNonJdbcSystemRars();
    }

    /**
     * determine whether the RAR in question is a System RAR
     * @param raName RarName
     * @return boolean
     */
    public static boolean belongsToSystemRA(String raName) {
        boolean result = false;

        for (String systemRarName : ConnectorsUtil.getSystemRARs()) {
            if (systemRarName.equals(raName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean getPingDuringPoolCreation(PoolInfo poolInfo, Resources allResources) {
        ResourcePool pool = getConnectionPoolConfig(poolInfo, allResources);
        return Boolean.parseBoolean(pool.getPing());
    }

    /**
     * determine whether the RAR in question is a System RAR
     * @param raName RarName
     * @return boolean
     */
    public static boolean belongsToJdbcRA(String raName) {
        boolean result = false;

        for (String systemRarName : ConnectorConstants.jdbcSystemRarNames) {
            if (systemRarName.equals(raName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * get the installation directory of System RARs
     * @param moduleName RARName
     * @return directory location
     */
    public static String getSystemModuleLocation(String moduleName) {
        String j2eeModuleDirName = System.getProperty(ConnectorConstants.INSTALL_ROOT) +
                File.separator + "lib" +
                File.separator + "install" +
                File.separator + "applications" +
                File.separator + moduleName;

        return j2eeModuleDirName;
    }

    private static ConfigBeansUtilities getConfigBeansUtilities() {
        ServiceLocator locator = Globals.getDefaultHabitat();
        if (locator == null) {
            return null;
        }

        return locator.getService(ConfigBeansUtilities.class);
    }

    private static String internalGetLocation(String moduleName) {
        ConfigBeansUtilities configBeansUtilities = getConfigBeansUtilities();
        if (configBeansUtilities == null) {
            return null;
        }

        return configBeansUtilities.getLocation(moduleName);

    }

    public static String getLocation(String moduleName) throws ConnectorRuntimeException {
        String location = null;
        if (belongsToSystemRA(moduleName)) {
            location = getSystemModuleLocation(moduleName);
        } else {
            location = internalGetLocation(moduleName);
            if (location == null) {
                // check whether its embedded RAR
                String rarName = getRarNameFromApplication(moduleName);
                String appName = getApplicationNameOfEmbeddedRar(moduleName);
                location = internalGetLocation(appName);
                if (location != null) {
                    location = location + File.separator + rarName + "_rar";
                } else {
                    throw new ConnectorRuntimeException("Unable to find location for module : " + moduleName);
                }
            }
        }
        return location;
    }


    /**
     * Return the system PM name for the JNDI name
     *
     * @param jndiName jndi name
     * @return String non-prefixed jndi name for PM resource
     */
    public static SimpleJndiName getPMJndiName(SimpleJndiName jndiName) {
        return new SimpleJndiName(jndiName + ConnectorConstants.PM_JNDI_SUFFIX);
    }

    /**
     * Check whether the jndi Name has connector related suffix and return if any.
     * @param name jndi name
     * @return suffix, if found
     */
    public static String getValidSuffix(SimpleJndiName name) {
        if (name != null) {
            for (String validSuffix : ConnectorConstants.JNDI_SUFFIX_VALUES) {
                if (name.hasSuffix(validSuffix)) {
                    return validSuffix;
                }
            }
        }

        return null;
    }

    /**
     * If the suffix is one of the valid context return true.
     * Return false, if that is not the case.
     *
     * @param suffix __nontx / __pm
     * @return boolean whether the suffix is valid or not
     */
    public static boolean isValidJndiSuffix(String suffix) {
        LOG.log(Level.FINEST, "isValidJndiSuffix(suffix={0})", suffix);
        if (suffix != null) {
            for (String validSuffix : JNDI_SUFFIX_VALUES) {
                if (validSuffix.equals(suffix)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Given the name of the resource and its jndi env, derive the complete jndi name.
     * (eg; with __PM / __nontx)
     *
     * @param name name of the resource
     * @param env env
     * @return derived name
     */
    public static SimpleJndiName deriveJndiName(SimpleJndiName name, Hashtable<Object, Object> env) {
        String suffix = (String) env.get(ConnectorConstants.JNDI_SUFFIX_PROPERTY);
        if (isValidJndiSuffix(suffix)) {
            return new SimpleJndiName(name + suffix);
        }

        return name;
    }


    public static ResourcePool getConnectionPoolConfig(PoolInfo poolInfo, Resources allResources) {
        if (allResources == null || allResources.getResources() == null) {
            return null;
        }
        List<Resource> resources = allResources.getResources();
        for (Resource configuredResource : resources) {
            if (configuredResource instanceof ResourcePool) {
                ResourcePool resourcePool = (ResourcePool) configuredResource;
                if (resourcePool.getName().equals(poolInfo.getName().toString())) {
                    return resourcePool;
                }
            }
        }
        return null;
    }

    public static Collection<Resource> getAllResources(Collection<String> poolNames, Resources allResources) {
        if (allResources == null || allResources.getResources() == null) {
            return List.of();
        }
        List<Resource> connectorResources = new ArrayList<>();
        for (Resource resource : allResources.getResources()) {
            if (resource instanceof ConnectorResource) {
                ConnectorResource connectorResource = (ConnectorResource) resource;
                if (poolNames.contains(connectorResource.getPoolName())) {
                    connectorResources.add(connectorResource);
                }
            }
        }

        return connectorResources;
    }

    /**
     * get the list of pool names
     * @param connectionPools list of pools
     * @return list of pol names
     */
    public static Collection<String> getAllPoolNames(Collection<ConnectorConnectionPool> connectionPools) {
        Set<String> poolNames = new HashSet<>();
        for (ConnectorConnectionPool pool : connectionPools) {
            poolNames.add(pool.getName());
        }
        return poolNames;
    }

    public static Collection<WorkSecurityMap> getAllWorkSecurityMaps(Resources resources, String moduleName) {
        List<WorkSecurityMap> workSecurityMaps = new ArrayList<>();
        for (WorkSecurityMap resource : resources.getResources(WorkSecurityMap.class)) {
            if (resource.getResourceAdapterName().equals(moduleName)) {
                workSecurityMaps.add(resource);
            }
        }
        return workSecurityMaps;
    }

    /**
     * get the pools for a particular resource-adapter
     *
     * @param moduleName resource-adapter name
     * @return collection of connectorConnectionPool
     */
    public static Collection<ConnectorConnectionPool> getAllPoolsOfModule(String moduleName, Resources allResources) {
        List<ConnectorConnectionPool> connectorConnectionPools = new ArrayList<>();
        for (Resource resource : allResources.getResources()) {
            if (resource instanceof ConnectorConnectionPool) {
                ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool) resource;
                if (connectorConnectionPool.getResourceAdapterName().equals(moduleName)) {
                    connectorConnectionPools.add(connectorConnectionPool);
                }
            }
        }
        return connectorConnectionPools;
    }

    /**
     * Get all System RAR pools and resources
     *
     * @param allResources all configured resources
     * @return Collection of system RAR pools
     */
    public static Collection<Resource> getAllSystemRAResourcesAndPools(Resources allResources) {
        // Make sure that resources are added first and then pools.
        List<Resource> resources = new ArrayList<>();
        List<Resource> pools = new ArrayList<>();
        for (Resource resource : allResources.getResources()) {
            if (resource instanceof ConnectorConnectionPool) {
                String raName = ((ConnectorConnectionPool) resource).getResourceAdapterName();
                if (ConnectorsUtil.belongsToSystemRA(raName)) {
                    pools.add(resource);
                }
            } else if (resource instanceof ConnectorResource) {
                String poolName = ((ConnectorResource) resource).getPoolName();
                String raName = getResourceAdapterNameOfPool(poolName, allResources);
                if (ConnectorsUtil.belongsToSystemRA(raName)) {
                    resources.add(resource);
                }
            } else if (resource instanceof AdminObjectResource) { // jms-ra
                String raName = ((AdminObjectResource) resource).getResAdapter();
                if (ConnectorsUtil.belongsToSystemRA(raName)) {
                    resources.add(resource);
                }
            } // no need to list work-security-map as they are not deployable artifacts
        }
        resources.addAll(pools);
        return resources;
    }

    /**
     * Given the poolname, retrieve the resourceadapter name
     *
     * @param poolName connection pool name
     * @param allResources resources
     * @return resource-adapter name
     */
    public static String getResourceAdapterNameOfPool(String poolName, Resources allResources) {
        String raName = null;
        for (Resource resource : allResources.getResources()) {
            if (resource instanceof ConnectorConnectionPool) {
                ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool) resource;
                String name = connectorConnectionPool.getName();
                if (name.equalsIgnoreCase(poolName)) {
                    raName = connectorConnectionPool.getResourceAdapterName();
                    break;
                }
            }
        }
        return raName;
    }

    public static ResourceAdapterConfig getRAConfig(String raName, Resources allResources) {
        Collection<ResourceAdapterConfig> raConfigs = allResources.getResources(ResourceAdapterConfig.class);
        for (ResourceAdapterConfig resourceAdapterConfig : raConfigs) {
            if (resourceAdapterConfig.getResourceAdapterName().equals(raName)) {
                return resourceAdapterConfig;
            }
        }
        return null;
    }

    /**
     * given the ra-name, returns all the configured connector-work-security-maps for the .rar
     * @param raName resource-adapter name
     * @param allResources resources
     * @return list of work-security-maps
     */
    public static List<WorkSecurityMap> getWorkSecurityMaps(String raName, Resources allResources) {
        List<Resource> resourcesList = allResources.getResources();
        List<WorkSecurityMap> workSecurityMaps = new ArrayList<>();
        for (Resource resource : resourcesList) {
            if (resource instanceof WorkSecurityMap) {
                WorkSecurityMap workSecurityMap = (WorkSecurityMap) resource;
                if (workSecurityMap.getResourceAdapterName().equals(raName)) {
                    workSecurityMaps.add(workSecurityMap);
                }
            }
        }

        return workSecurityMaps;
    }


    public static boolean isDynamicReconfigurationEnabled(ResourcePool pool) {
        boolean enabled = false;
        if (pool instanceof PropertyBag) {
            PropertyBag properties = (PropertyBag) pool;
            Property property = properties.getProperty(ConnectorConstants.DYNAMIC_RECONFIGURATION_FLAG);
            if (property != null && property.getValue() != null) {
                try {
                    if (Long.parseLong(property.getValue()) > 0) {
                        enabled = true;
                    }
                } catch (NumberFormatException nfe) {
                    LOG.log(Level.WARNING,
                        "Invalid value for property 'dynamic-reconfiguration-wait-timeout-in-seconds': {0}",
                        property.getValue());
                }
            }
        }
        return enabled;
    }


    /**
     * Prepares the name/value pairs for ActivationSpec.
     * <p>
     * Rule:
     * <p>
     * 1. The name/value pairs are the union of activation-config on
     * standard DD (message-driven) and runtime DD (mdb-resource-adapter)
     * 2. If there are duplicate property settings, the value in runtime
     * activation-config will overwrite the one in the standard
     * activation-config.
     */
    public static Set<EnvironmentProperty> getMergedActivationConfigProperties(EjbMessageBeanDescriptor msgDesc) {
        Set<EnvironmentProperty> mergedProps = new HashSet<>();
        Set<String> runtimePropNames = new HashSet<>();
        Set<EnvironmentProperty> runtimeProps = msgDesc.getRuntimeActivationConfigProperties();
        if (runtimeProps != null) {
            for (EnvironmentProperty entry : runtimeProps) {
                mergedProps.add(entry);
                String propName = entry.getName();
                runtimePropNames.add(propName);
            }
        }

        Set<EnvironmentProperty> standardProps = msgDesc.getActivationConfigProperties();
        if (standardProps != null) {
            for (EnvironmentProperty entry : standardProps) {
                String propName = entry.getName();
                if (runtimePropNames.contains(propName)) {
                    continue;
                }
                mergedProps.add(entry);
            }
        }

        return mergedProps;
    }


    public static boolean isJMSRA(String moduleName) {
        if (ConnectorConstants.DEFAULT_JMS_ADAPTER.equals(moduleName)) {
            return true;
        }
        return false;
    }


    public static boolean parseBoolean(String enabled) {
        return Boolean.parseBoolean(enabled);
    }


    /**
     * Gets the shutdown-timeout attribute from domain.xml
     * via the connector server config bean.
     *
     * @param connectorService connector-service configuration
     * @return long shutdown timeout (in mill-seconds)
     */
    public static long getShutdownTimeout(ConnectorService connectorService) {
        int shutdownTimeout;

        try {
            if (connectorService == null) {
                // Connector service element is not specified in
                // domain.xml and hence going with the default time-out
                shutdownTimeout = DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Shutdown timeout set to " + shutdownTimeout + " through default");
                }
            } else {
                shutdownTimeout = Integer.parseInt(connectorService.getShutdownTimeoutInSeconds());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Shutdown timeout set to " + shutdownTimeout + " from domain.xml");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                "Error reading Connector Service Element from domain.xml while trying"
                    + " to get shutdown-timeout-in-seconds value. Continuing with the default shutdown timeout value "
                    + DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT,
                e);
            //Going ahead with the default timeout value
            shutdownTimeout = DEFAULT_RESOURCE_ADAPTER_SHUTDOWN_TIMEOUT;
        }
        return shutdownTimeout * 1000L;
    }


    /**
     * Provides the list of built in custom resources by
     * resource-type and factory-class-name pair.
     *
     * @return map of resource-type & factory-class-name
     */
    public static Map<String, String> getBuiltInCustomResources() {
        Map<String, String> resourcesMap = new HashMap<>();

        // user will have to provide the JavaBean Implementation class and hence we cannot list this factory
        // resourcesMap.put("JavaBean", ConnectorConstants.JAVA_BEAN_FACTORY_CLASS );

        resourcesMap.put("java.lang.Integer", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.Long", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.Double", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.Float", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.Character", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.Short", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.Byte", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.Boolean", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.lang.String", ConnectorConstants.PRIMITIVES_AND_STRING_FACTORY_CLASS);
        resourcesMap.put("java.net.URL", ConnectorConstants.URL_OBJECTS_FACTORY);
        resourcesMap.put("java.util.Properties", ConnectorConstants.PROPERTIES_FACTORY);
        return resourcesMap;
    }


    /**
     * @param compId can be null
     * @param resourceName must not be null
     * @param resType must not be null
     * @return prefixed {@link SimpleJndiName} respecting GlassFish internal rules.
     */
    public static SimpleJndiName deriveResourceName(String compId, SimpleJndiName resourceName, JavaEEResourceType resType) {
        LOG.log(Level.FINEST, "deriveResourceName(compId={0}, resourceName={1}, resType={2})",
            new Object[] {compId, resourceName, resType});

        final String prefixPart1;
        final String prefixPart2;
        switch (resType) {
            case DSD:
                prefixPart1 = ConnectorConstants.RESOURCE_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX;
                break;
            case MSD:
                prefixPart1 = ConnectorConstants.RESOURCE_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.MAILSESSION_DEFINITION_JNDINAME_PREFIX;
                break;
            case CFD:
                prefixPart1 = ConnectorConstants.RESOURCE_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX;
                break;
            case DSDPOOL:
                prefixPart1 = ConnectorConstants.POOLS_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX;
                break;
            case CFDPOOL:
                prefixPart1 = ConnectorConstants.POOLS_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX;
                break;
            case JMSCFDD:
                prefixPart1 = ConnectorConstants.RESOURCE_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.JMS_CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX;
                break;
            case JMSCFDDPOOL:
                prefixPart1 = ConnectorConstants.POOLS_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.JMS_CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX;
                break;
            case JMSDD:
                prefixPart1 = ConnectorConstants.RESOURCE_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.JMS_DESTINATION_DEFINITION_JNDINAME_PREFIX;
                break;
            case AODD:
                prefixPart1 = ConnectorConstants.RESOURCE_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.ADMINISTERED_OBJECT_DEFINITION_JNDINAME_PREFIX;
                break;
            case MEDD:
            case MSEDD:
            case MTFDD:
                prefixPart1 = ConnectorConstants.CONCURRENT_JNDINAME_PREFIX;
                prefixPart2 = "";
                break;
            case CSDD:
                prefixPart1 = ConnectorConstants.CONCURRENT_JNDINAME_PREFIX;
                prefixPart2 = ResourceConstants.CONCURRENT_CONTEXT_SERVICE_DEFINITION_JNDINAME_PREFIX;
                break;
            default:
                throw new IllegalArgumentException("The resource type " + resType + " is not supported!");
        }

        final String prefix;
        if (compId == null || compId.isEmpty()) {
            prefix = prefixPart1 + prefixPart2;
        } else {
            prefix = prefixPart1 + prefixPart2 + compId + '/';
        }
        return addResourceNamePrefix(prefix, resourceName);
    }


    public static SimpleJndiName addResourceNamePrefix(String prefix, SimpleJndiName resourceName) {
        // FIXME: dmatej breaks connectors_group_3, to be continued.
//        if (resourceName.contains(prefix)) {
//            LOG.log(Level.WARNING,
//                "The name {0} already contained prefix {1}."
//                    + " It may be a coincidence or you already added the prefix. I am returning the original name.",
//                new Object[] {resourceName, prefix});
//            LOG.log(Level.FINEST, "", new IllegalArgumentException("Stacktrace to find which method asked for this."));
//            return resourceName;
//        }
//        if (resourceName.hasJavaPrefix()) {
//            String javaCtxPrefix = resourceName.getPrefix();
//            SimpleJndiName jndiEnv = resourceName.removePrefix(javaCtxPrefix);
//            SimpleJndiName jndiSimple = jndiEnv.removePrefix("env/");
//            String javaCtxEnvPrefix = jndiEnv == jndiSimple ? javaCtxPrefix.toString() : (javaCtxPrefix + "env/");
//            return new SimpleJndiName(javaCtxEnvPrefix + prefix + jndiSimple);
//        }
        return new SimpleJndiName(prefix + resourceName);
    }


    public static Map<String, String> convertPropertiesToMap(Properties properties) {
        if (properties == null) {
            properties = new Properties();
        }
        return new TreeMap<String, String>((Map) properties);
    }


    public static String getEmbeddedRarModuleName(String applicationName, String moduleName) {
        String embeddedRarName = moduleName.substring(0,
            moduleName.indexOf(ConnectorConstants.EXPLODED_EMBEDDED_RAR_EXTENSION));

        moduleName = applicationName + ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER + embeddedRarName;
        return moduleName;
    }


    public static String getApplicationNameOfEmbeddedRar(String embeddedRarName) {
        int index = embeddedRarName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER);
        String applicationName = embeddedRarName;

        if (index != -1) {
            applicationName = embeddedRarName.substring(0, index);
        }
        return applicationName;
    }


    public static String getRarNameFromApplication(String appName) {
        int index = appName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER);
        String rarName = appName;

        if (index != -1 && appName.length() > index + 1) {
            rarName = appName.substring(index + 1);
        }
        return rarName;
    }


    public static boolean isEmbedded(DeploymentContext context) {
        ReadableArchive archive = context.getSource();
        return archive != null && archive.getParentArchive() != null;
    }


    public static String getApplicationName(DeploymentContext context) {
        String applicationName = null;
        ReadableArchive parentArchive = context.getSource().getParentArchive();
        if (parentArchive == null) {
            applicationName = context.getSource().getName();
        } else {
            applicationName = parentArchive.getName();
        }
        return applicationName;
    }


    public static List<URI> getInstalledLibrariesFromManifest(String moduleDirectory, ServerEnvironment env)
            throws ConnectorRuntimeException {

        // this method will be called during system-rar creation.
        // Though there are code paths that will call this method for creation of rars during recovery / via
        // API exposed for GUI, they will not call this method as non-system rars are always started during server startup
        // system-rars can specify only EXTENSTION_LIST in MANIFEST.MF and do not have a way to use --libraries option.
        // So, satisfying system-rars alone as of now.

        List<URI> libURIs = new ArrayList<>();
        if (moduleDirectory != null) {
            try {
                File module = new File(moduleDirectory);
                if (module.exists()) {

                    FileArchive fileArchive = new FileArchive();
                    fileArchive.open(module.toURI()); // directory where rar is exploded
                    Set<String> extensionList = InstalledLibrariesResolver.getInstalledLibraries(fileArchive);

                    URL[] extensionListLibraries = ASClassLoaderUtil.getLibrariesAsURLs(extensionList, env);
                    for (URL url : extensionListLibraries) {
                        libURIs.add(url.toURI());
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, "adding URL [ " + url + " ] to installedLibraries");
                        }
                    }
                }
            } catch (IOException ioe) {
                ConnectorRuntimeException cre = new ConnectorRuntimeException(ioe.getMessage());
                cre.initCause(ioe);
                throw cre;
            } catch (URISyntaxException e) {
                ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
                cre.initCause(e);
                throw cre;
            }
        }

        return libURIs;
    }

    public static SimpleJndiName getReservePrefixedJNDINameForDescriptor(String moduleName) {
        return new SimpleJndiName(ConnectorConstants.DD_PREFIX + moduleName);
    }

    public static boolean isStandAloneRA(String moduleName){
        ConfigBeansUtilities cbu = getConfigBeansUtilities();
        ApplicationName an = null;
        if (cbu != null) {
            an = cbu.getModule(moduleName);
        }
        return (an != null);
    }

    public static Collection<String> getSystemRARs() {
        return validSystemRARs;
    }

    private static void initializeSystemRars() {
        for (String rarName : ConnectorConstants.systemRarNames) {
            if (systemRarExists(getSystemModuleLocation(rarName))) {
                validSystemRARs.add(rarName);
            }
        }
        LOG.log(Level.CONFIG, "Valid system RARs for this runtime are : {0}", validSystemRARs);
    }

    public static Collection<String> getNonJdbcSystemRars() {
        return validNonJdbcSystemRARs;
    }

    private static void initializeNonJdbcSystemRars() {
        Collection<String> systemRars = getSystemRARs();
        for (String rarName : systemRars) {
            if (!ConnectorConstants.jdbcSystemRarNames.contains(rarName)) {
                validNonJdbcSystemRARs.add(rarName);
            }
        }
    }


    public static boolean systemRarExists(String location) {
        boolean result = false;
        try {
            File file = new File(location);
            result = file.exists();
        } catch (Exception e) {
            LOG.log(Level.FINEST, "Exception occurred while checking System RAR location: [" + location + "]", e);
        }
        return result;
    }


    /**
     * GlassFish (Embedded) Uber jar will have .rar bundled in it.
     * This method will extract the .rar from the uber jar into specified directory.
     * As of now, this method is only used in EMBEDDED mode
     *
     * @param fileName rar-directory-name
     * @param rarName resource-adapter name
     * @param destDir destination directory
     * @return status indicating whether .rar is exploded successfully or not
     */
    public static boolean extractRar(String fileName, String rarName, String destDir) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(rarName)) {
            if (is == null) {
                LOG.log(Level.INFO, "could not find RAR [ " + rarName + " ] in the archive, skipping .rar extraction");
                return false;
            }
            try (FileArchive fa = new FileArchive();
                WritableArchiveEntry os = fa.putNextEntry(fileName)) {
                FileUtils.copy(is, os);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Exception while extracting RAR " + rarName + " from archive " + fileName, e);
                return false;
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Exception while closing archive [ " + rarName + " ]", e);
        }

        File file = new File(fileName);
        if (file.exists()) {
            try {
                extractJar(file, destDir);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Exception while extracting archive [ " + file + " ]", e);
            }
            return true;
        }
        LOG.log(Level.INFO,
            "could not find RAR [ " + rarName + " ] location [ " + fileName + " ] " + "after extraction");
        return false;
    }

    private static void extractJar(File jarFile, String destDir) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> enum1 = jar.entries();
            while (enum1.hasMoreElements()) {
                JarEntry file = enum1.nextElement();
                File f = new File(destDir, file.getName());
                if (file.isDirectory() && f.mkdir()) {
                    continue;
                }
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = jar.getInputStream(file);
                    fos = new FileOutputStream(f);
                    while (is.available() > 0) {
                        fos.write(is.read());
                    }
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "exception while closing archive [ " + f.getName() + " ]", e);
                    }

                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "exception while closing archive [ " + file.getName() + " ]", e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "exception while closing archive [ " + jarFile + " ]", e);
        }
    }

    public static ResourceInfo getResourceInfo(BindableResource resource){
        return ResourceUtil.getResourceInfo(resource);
    }

    public static String getApplicationName(Resource resource){
        String applicationName = null;
        if(resource.getParent() != null && resource.getParent().getParent() instanceof Application){
            Application application = (Application)resource.getParent().getParent();
            applicationName = application.getName();
        }
        return applicationName;
    }

    public static String getApplicationName(PoolInfo poolInfo){
        return poolInfo.getApplicationName();
    }

    public static boolean isApplicationScopedResource(GenericResourceInfo resourceInfo){
        return ResourceUtil.isApplicationScopedResource(resourceInfo);
    }

    public static boolean isModuleScopedResource(GenericResourceInfo resourceInfo){
        return ResourceUtil.isModuleScopedResource(resourceInfo);
    }

    public static String escapeResourceNameForMonitoring(SimpleJndiName name){
        return name.toString().replaceAll("/", SLASH);
    }

    public static String getPoolMonitoringSubTreeRoot(PoolInfo poolInfo, boolean escapeSlashes) {
        String resourcesPrefix = "resources/";

        final String suffix;
        if (escapeSlashes) {
            suffix = escapeResourceNameForMonitoring(poolInfo.getName());
        } else {
            suffix = poolInfo.getName().toString();
        }

        String subTreeRoot = resourcesPrefix + suffix;
        if (ConnectorsUtil.isModuleScopedResource(poolInfo)) {
            subTreeRoot = "applications/" + poolInfo.getApplicationName() + "/" + poolInfo.getModuleName() + "/"
                + resourcesPrefix + "/" + suffix;
        } else if (ConnectorsUtil.isApplicationScopedResource(poolInfo)) {
            subTreeRoot = "applications/" + poolInfo.getApplicationName() + "/" + resourcesPrefix + "/" + suffix;
        }
        return subTreeRoot;
    }


    public static String getActualModuleName(String moduleName) {
        return ResourceUtil.getActualModuleName(moduleName);
    }


    public static String getModuleName(EjbDescriptor descriptor) {
        String appName = descriptor.getApplication().getAppName();
        String moduleName = descriptor.getEjbBundleDescriptor().getModuleID();
        String actualModuleName = moduleName;
        if (moduleName != null) {
            String prefix = appName + "#";
            if (moduleName.startsWith(prefix)) {
                actualModuleName = moduleName.substring(prefix.length());
            }
        }
        return actualModuleName;
    }


    public static Collection<BindableResource> getResourcesOfPool(Resources resources, SimpleJndiName connectionPoolName) {
        Set<BindableResource> resourcesReferringPool = new HashSet<>();
        ResourcePool pool = resources.getResourceByName(ResourcePool.class, connectionPoolName);
        if (pool != null) {
            Collection<BindableResource> bindableResources = resources.getResources(BindableResource.class);
            for (BindableResource resource : bindableResources) {
                if (ResourcePoolReference.class.isAssignableFrom(resource.getClass())) {
                    if (((ResourcePoolReference) resource).getPoolName().equals(connectionPoolName.toString())) {
                        resourcesReferringPool.add(resource);
                    }
                }
            }
        }
        return resourcesReferringPool;
    }


    // TODO what if the module being deployed is a RAR and has gf-resources.xml ?
    // TODO can the RAR define its own resources ? eg: connector-resource, pool, a-o-r ?
    public static TriState isEmbeddedRarResource(Resource configBeanResource, Collection<Resource> configBeanResources) {
        TriState result = TriState.FALSE;
        if (configBeanResource instanceof ConnectorResource) {
            String poolName = ((ConnectorResource) configBeanResource).getPoolName();
            ConnectorConnectionPool pool = getPool(configBeanResources, poolName);
            if (pool != null) {
                if (pool.getResourceAdapterName().contains(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER)) {
                    result = TriState.TRUE;
                }
            } else {
                result = TriState.UNKNOWN;
            }
        } else if (configBeanResource instanceof AdminObjectResource) {
            AdminObjectResource aor = (AdminObjectResource) configBeanResource;
            if (aor.getResAdapter().contains(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER)) {
                result = TriState.TRUE;
            }
        } else if (configBeanResource instanceof ConnectorConnectionPool) {
            ConnectorConnectionPool ccp = (ConnectorConnectionPool) configBeanResource;
            if (ccp.getResourceAdapterName().contains(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER)) {
                result = TriState.TRUE;
            }
        } else if (configBeanResource instanceof WorkSecurityMap) {
            WorkSecurityMap wsm = (WorkSecurityMap) configBeanResource;
            if (wsm.getResourceAdapterName().contains(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER)) {
                result = TriState.TRUE;
            }
        }
        return result;
    }


    public static ConnectorConnectionPool getPool(Collection<Resource> configBeanResources, String poolName) {
        ConnectorConnectionPool result = null;
        for (Resource res : configBeanResources) {
            if (res instanceof ConnectorConnectionPool) {
                if (((ConnectorConnectionPool) res).getName().equals(poolName)) {
                    result = (ConnectorConnectionPool) res;
                    break;
                }
            }
        }
        return result;
    }


    public static boolean isRARResource(Resource resource) {
        return resource instanceof ConnectorResource || resource instanceof AdminObjectResource
            || resource instanceof ConnectorConnectionPool || resource instanceof ResourceAdapterConfig
            || resource instanceof WorkSecurityMap;
    }


    public static String getRarNameOfResource(Resource resource, Resources resources) {
        if (isRARResource(resource)) {
            if (resource instanceof ConnectorResource) {
                String poolName = ((ConnectorResource) resource).getPoolName();
                for (Resource res : resources.getResources()) {
                    if (res instanceof ConnectorConnectionPool) {
                        ConnectorConnectionPool ccp = ((ConnectorConnectionPool) res);
                        if (ccp.getName().equals(poolName)) {
                            return ccp.getResourceAdapterName();
                        }
                    }
                }
            } else if (resource instanceof ConnectorConnectionPool) {
                ConnectorConnectionPool ccp = ((ConnectorConnectionPool) resource);
                return ccp.getResourceAdapterName();
            } else if (resource instanceof AdminObjectResource) {
                AdminObjectResource aor = (AdminObjectResource) resource;
                return aor.getResAdapter();
            } else if (resource instanceof ResourceAdapterConfig) {
                ResourceAdapterConfig rac = (ResourceAdapterConfig) resource;
                return rac.getResourceAdapterName();
            } else if (resource instanceof WorkSecurityMap) {
                WorkSecurityMap wsm = (WorkSecurityMap) resource;
                return wsm.getResourceAdapterName();
            }
        }
        return null;
    }
}
