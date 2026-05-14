/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.util.StringUtils;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigExtensionMethod;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.errorGettingCluster;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.errorGettingServers;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.getLogger;
import static java.util.logging.Level.WARNING;

/**
 * Top level Domain Element that includes applications, resources, configs, servers, clusters
 * and node-agents, load balancer configurations and load balancers.
 *
 * <p>Node-agents and load balancers are SE/EE related entities only.
 */
@Configured
public interface Domain extends ConfigBeanProxy, PropertyBag, SystemPropertyBag, ConfigLoader {

    String DOMAIN_NAME_PROPERTY = "administrative.domain.name";

    /**
     * Gets the value of the {@code applicationRoot} property.
     *
     * <p>For PE this defines the location where applications are deployed
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getApplicationRoot();

    /**
     * Sets the value of the {@code applicationRoot} property.
     *
     * @param applicationRoot allowed object is {@link String}
     */
    void setApplicationRoot(String applicationRoot) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logRoot} property.
     *
     * <p>Specifies where the server instance's log files are kept, including HTTP access logs, server logs,
     * and transaction logs. Default is {@code $INSTANCE-ROOT/logs}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getLogRoot();

    /**
     * Sets the value of the {@code logRoot} property.
     *
     * @param logRoot allowed object is {@link String}
     */
    void setLogRoot(String logRoot) throws PropertyVetoException;

    /**
     * Gets the value of the {@code locale} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getLocale();

    /**
     * Sets the value of the {@code locale} property.
     *
     * @param locale allowed object is {@link String}
     */
    void setLocale(String locale) throws PropertyVetoException;

    /**
     * Gets the value of the {@code version} property. It is read-only.
     *
     * <p>Tools are not to depend on this property. It is only for reference.
     *
     * @return String representing version of the Domain.
     */
    @Attribute
    String getVersion();

    /**
     * Gets the {@code SecureAdmin} value defined in the domain.
     *
     * @return {@link SecureAdmin}
     */
    @Element
    SecureAdmin getSecureAdmin();

    /**
     * Sets the {@code SecureAdmin} value.
     *
     * @param secureAdmin the new {@code SecuredAdmin} value
     */
    void setSecureAdmin(SecureAdmin secureAdmin);

    /**
     * Gets the value of the {@code applications} property.
     *
     * @return possible object is {@link Applications}
     */
    @Element
    @NotNull
    Applications getApplications();

    /**
     * Sets the value of the {@code applications} property.
     *
     * @param applications allowed object is {@link Applications}
     */
    void setApplications(Applications applications) throws PropertyVetoException;

    /**
     * Gets the value of the {@code system-applications} property.
     *
     * @return possible object is {@link SystemApplications}
     */
    @Element
    @NotNull
    SystemApplications getSystemApplications();

    /**
     * Sets the value of the {@code system-applications} property.
     *
     * @param systemApplications allowed object is {@link SystemApplications}
     */
    void setSystemApplications(SystemApplications systemApplications) throws PropertyVetoException;

    /**
     * Gets the value of the {@code resources} property.
     *
     * @return possible object is {@link Resources}
     */
    @Element
    @NotNull
    Resources getResources();

    /**
     * Sets the value of the {@code resources} property.
     *
     * @param resources allowed object is {@link Resources}
     */
    void setResources(Resources resources) throws PropertyVetoException;

    /**
     * Gets the value of the {@code configs} property.
     *
     * @return possible object is {@link Configs}
     */
    @Element(required = true)
    @NotNull
    Configs getConfigs();

    /**
     * Sets the value of the {@code configs} property.
     *
     * @param configs allowed object is {@link Configs}
     */
    void setConfigs(Configs configs) throws PropertyVetoException;

    /**
     * Gets the value of the {@code servers} property.
     *
     * @return possible object is {@link Servers}
     */
    @Element(required = true)
    @NotNull
    Servers getServers();

    /**
     * Sets the value of the {@code servers} property.
     *
     * @param servers allowed object is {@link Servers}
     */
    void setServers(Servers servers) throws PropertyVetoException;

    /**
     * Gets the value of the {@code clusters} property.
     *
     * @return possible object is {@link Clusters}
     */
    @Element
    @NotNull
    Clusters getClusters();

    /**
     * Sets the value of the {@code clusters} property.
     *
     * @param clusters allowed object is {@link Clusters}
     */
    void setClusters(Clusters clusters) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nodes} property.
     *
     * @return possible object is {@link Nodes}
     */
    @Element
    Nodes getNodes();

    /**
     * Sets the value of the {@code nodes} property.
     *
     * @param nodes allowed object is {@link Nodes}
     */
    void setNodes(Nodes nodes) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nodeAgents} property.
     *
     * @return possible object is {@link NodeAgents}
     */
    @Element
    NodeAgents getNodeAgents();

    /**
     * Sets the value of the {@code nodeAgents} property.
     *
     * @param nodeAgents allowed object is {@link NodeAgents}
     */
    void setNodeAgents(NodeAgents nodeAgents) throws PropertyVetoException;

    /**
     * Gets the value of the {@code systemProperty} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification
     * you make to the returned list will be present inside the JAXB object. This is why there is not a {@code set}
     * method for the {@code systemProperty} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getSystemProperty().add(newItem);
     * </pre>
     *
     * Objects of the following type(s) are allowed in the list {@link SystemProperty }
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Any more legal system properties?")
    @PropertiesDesc(systemProperties = true, props = {
            @PropertyDesc(
                    name = "com.sun.aas.installRoot",
                    description = "Operating system dependent. Path to the directory where the server is installed"
            ),
            @PropertyDesc(
                    name = "com.sun.aas.instanceRoot",
                    description = "Operating system dependent. Path to the top level directory for a server instance"
            ),
            @PropertyDesc(
                    name = "com.sun.aas.hostName",
                    description = "Operating system dependent. Path to the name of the host (machine)"
            ),
            @PropertyDesc(
                    name = "com.sun.aas.javaRoot",
                    description = "Operating system dependent. Path to the JDK root directory."
            ),
            @PropertyDesc(
                    name = "com.sun.aas.imqBin",
                    description = "Operating system dependent. Path to the bin directory for the IMQ runtime"
            ),
            @PropertyDesc(
                    name = "com.sun.aas.imqLib",
                    description = "Operating system dependent. Path to the lib directory for the IMQ runtime"
            ),
            @PropertyDesc(
                    name = "com.sun.aas.configName",
                    defaultValue = "server-config",
                    description = "Name of the <config> used by a server instance"
            ),
            @PropertyDesc(
                    name = "com.sun.aas.instanceName",
                    defaultValue = "server1",
                    description = "Name of the server instance. Not used in the default configuration, but can be used to customize configuration"
            ),
            @PropertyDesc(
                    name = "com.sun.aas.domainName",
                    defaultValue = "domain1",
                    description = "Name of the domain. Not used in the default configuration, but can be used to customize configuration"
            )
    })
    @Element
    List<SystemProperty> getSystemProperty();

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    @Element("*")
    List<DomainExtension> getExtensions();

    default String getName() {
        return getPropertyValue(DOMAIN_NAME_PROPERTY);
    }

    default List<Application> getAllDefinedSystemApplications() {
        List<Application> allSysApps = new ArrayList<>();
        SystemApplications sysApps = getSystemApplications();
        if (sysApps != null) {
            for (ApplicationName module : sysApps.getModules()) {
                if (module instanceof Application) {
                    allSysApps.add((Application) module);
                }
            }
        }
        return Collections.unmodifiableList(allSysApps);
    }

    default ApplicationRef getApplicationRefInServer(String serverName, String appName) {
        Server server = null;
        for (Server srv : getServers().getServer()) {
            if (srv.getName().equals(serverName)) {
                server = srv;
                break;
            }
        }

        ApplicationRef appRef = null;
        if (server != null) {
            for (ApplicationRef ref : server.getApplicationRef()) {
                if (ref.getRef().equals(appName)) {
                    appRef = ref;
                    break;
                }
            }
        }
        return appRef;
    }

    default List<ApplicationRef> getApplicationRefsInServer(String serverName) {
        Server server = getServerNamed(serverName);
        if (server != null) {
            return server.getApplicationRef();
        } else {
            return List.of();
        }
    }

    /**
     * Returns the list of system-applications that are referenced from the given server. A server references an
     * application, if the server has an element named {@code <application-ref>} in it that points to given
     * application. The given server is a {@code <server>} element inside domain.
     *
     * @param serverName the string denoting name of the server
     * @return List of system-applications for that server, an empty list in case there is none
     */
    default List<Application> getSystemApplicationsReferencedFrom(String serverName) {
        if (serverName == null) {
            throw new IllegalArgumentException("Null argument");
        }

        List<Application> allSysApps = getAllDefinedSystemApplications();
        if (allSysApps.isEmpty()) {
            return allSysApps; // if there are no sys-apps, none can reference one :)
        }

        // allSysApps now contains ALL the system applications
        List<Application> referencedApps = new ArrayList<>();
        for (ApplicationRef ref : getServerNamed(serverName).getApplicationRef()) {
            for (Application app : allSysApps) {
                if (ref.getRef().equals(app.getName())) {
                    referencedApps.add(app);
                }
            }
        }
        return Collections.unmodifiableList(referencedApps);
    }

    default Application getSystemApplicationReferencedFrom(String serverName, String appName) {
        // returns null in case there is none
        for (Application app : getSystemApplicationsReferencedFrom(serverName)) {
            if (app.getName().equals(appName)) {
                return app;
            }
        }
        return null;
    }

    default boolean isNamedSystemApplicationReferencedFrom(String appName, String serverName) {
        for (Application app : getSystemApplicationsReferencedFrom(serverName)) {
            if (app.getName().equals(appName)) {
                return true;
            }
        }
        return false;
    }

    default Server getServerNamed(String serverName) {
        Servers servers = getServers();
        if (servers == null || serverName == null) {
            throw new IllegalArgumentException("no <servers> element");
        }

        for (Server server : servers.getServer()) {
            if (serverName.equals(server.getName().trim())) {
                return server;
            }
        }
        return null;
    }

    default boolean isServer(String serverName) {
        return getServerNamed(serverName) != null;
    }

    default Config getConfigNamed(String configName) {
        Configs configs = getConfigs();
        if (configs == null || configName == null) {
            throw new IllegalArgumentException("no <config> element");
        }

        for (Config config : configs.getConfig()) {
            if (configName.equals(config.getName().trim())) {
                return config;
            }
        }
        return null;
    }

    default Cluster getClusterNamed(String clusterName) {
        Clusters clusters = getClusters();
        if (clusters == null || clusterName == null) {
            return null;
        }

        for (Cluster cluster : clusters.getCluster()) {
            if (clusterName.equals(cluster.getName().trim())) {
                return cluster;
            }
        }
        return null;
    }

    default Node getNodeNamed(String nodeName) {
        Nodes nodes = getNodes();
        if (nodes == null || nodeName == null) {
            return null;
        }

        for (Node node : nodes.getNode()) {
            if (nodeName.equals(node.getName().trim())) {
                return node;
            }
        }
        return null;
    }

    default boolean isCurrentInstanceMatchingTarget(String target, String appName, String currentInstance, List<String> referencedTargets) {
        if (target == null || currentInstance == null) {
            return false;
        }

        List<String> targets = new ArrayList<>();
        if (!target.equals("domain")) {
            targets.add(target);
        } else {
            if (referencedTargets == null) {
                referencedTargets = getAllReferencedTargetsForApplication(appName);
            }
            targets = referencedTargets;
        }

        for (String tgt : targets) {
            if (currentInstance.equals(tgt)) {
                // standalone instance case
                return true;
            }

            Cluster cluster = getClusterNamed(tgt);

            if (cluster != null) {
                for (Server server : cluster.getInstances()) {
                    if (server.getName().equals(currentInstance)) {
                        // cluster instance case
                        return true;
                    }
                }
            }
        }
        return false;
    }

    default List<Server> getServersInTarget(String target) {
        List<Server> servers = new ArrayList<>();
        Server server = getServerNamed(target);
        if (server != null) {
            servers.add(server);
        } else {
            Cluster cluster = getClusterNamed(target);
            if (cluster != null) {
                servers.addAll(cluster.getInstances());
            }
        }
        return servers;
    }

    default List<ApplicationRef> getApplicationRefsInTarget(String target) {
        return getApplicationRefsInTarget(target, false);
    }

    default List<ApplicationRef> getApplicationRefsInTarget(String target, boolean includeInstances) {
        List<ApplicationRef> allAppRefs = new ArrayList<>();

        for (String tgt : getTargets(target)) {
            Server server = getServerNamed(tgt);
            if (server != null) {
                allAppRefs.addAll(server.getApplicationRef());
            } else {
                Cluster cluster = getClusterNamed(tgt);
                if (cluster != null) {
                    allAppRefs.addAll(cluster.getApplicationRef());
                    if (includeInstances) {
                        for (Server srv : cluster.getInstances()) {
                            allAppRefs.addAll(srv.getApplicationRef());
                        }
                    }
                }
            }
        }
        return allAppRefs;
    }

    default ApplicationRef getApplicationRefInTarget(String appName, String target) {
        for (ApplicationRef ref : getApplicationRefsInTarget(target)) {
            if (ref.getRef().equals(appName)) {
                return ref;
            }
        }
        return null;
    }

    default ApplicationRef getApplicationRefInTarget(String appName, String target, boolean includeInstances) {
        for (ApplicationRef appRef : getApplicationRefsInTarget(target, includeInstances)) {
            if (appRef.getRef().equals(appName)) {
                return appRef;
            }
        }
        return null;
    }

    default boolean isAppRefEnabledInTarget(String appName, String target) {
        boolean found = false;

        Cluster containingCluster = getClusterForInstance(target);
        if (containingCluster != null) {
            // if this is a clustered instance, check the enable
            // attribute of its enclosing cluster first
            // and return false if the cluster level enable attribute
            // is false
            ApplicationRef clusterRef = getApplicationRefInTarget(appName, containingCluster.getName());
            if (clusterRef == null || !Boolean.parseBoolean(clusterRef.getEnabled())) {
                return false;
            }
        }

        for (ApplicationRef ref : getApplicationRefsInTarget(target, true)) {
            if (ref.getRef().equals(appName)) {
                found = true;
                if (!Boolean.parseBoolean(ref.getEnabled())) {
                    return false;
                }
            }
        }
        // return true if we found the ref(s)
        // and the enable attribute(s) is/are true,
        // false otherwise
        return found;
    }

    default boolean isAppEnabledInTarget(String appName, String target) {
        Application application = getApplications().getApplication(appName);
        if (application != null && Boolean.parseBoolean(application.getEnabled())) {
            List<String> targets = new ArrayList<>();
            if (!target.equals("domain")) {
                targets.add(target);
            } else {
                targets = getAllReferencedTargetsForApplication(appName);
            }
            for (String tgt : targets) {
                if (!isAppRefEnabledInTarget(appName, tgt)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    default boolean isAppReferencedByPaaSTarget(String appName) {
        for (String target : getAllReferencedTargetsForApplication(appName)) {
            Cluster cluster = getClusterNamed(target);
            if (cluster != null) {
                if (cluster.isVirtual()) {
                    return true;
                }
            }
        }
        return false;
    }

    default List<String> getAllReferencedTargetsForApplication(String appName) {
        List<String> referencedTargets = new ArrayList<>();
        for (String target : getAllTargets()) {
            if (getApplicationRefInTarget(appName, target) != null) {
                referencedTargets.add(target);
            }
        }
        return referencedTargets;
    }

    default List<String> getAllTargets() {
        List<String> targets = new ArrayList<>();
        // only add non-clustered servers as the cluster
        // targets will be separately added
        for (Server server : getServers().getServer()) {
            if (server.getCluster() == null) {
                targets.add(server.getName());
            }
        }

        Clusters clusters = getClusters();
        if (clusters != null) {
            for (Cluster cluster : clusters.getCluster()) {
                targets.add(cluster.getName());
            }
        }
        return targets;
    }

    default List<String> getTargets(String target) {
        List<String> targets = new ArrayList<>();
        if (!target.equals("domain")) {
            targets.add(target);
        } else {
            targets = getAllTargets();
        }
        return targets;
    }

    default List<Application> getApplicationsInTarget(String target) {
        if (target.equals("domain")) {
            // special target domain
            return getApplications().getApplications();
        }

        List<Application> apps = new ArrayList<>();

        for (ApplicationRef ref : getApplicationRefsInTarget(target)) {
            Application app = getApplications().getApplication(ref.getRef());
            if (app != null) {
                apps.add(app);
            }
        }
        return apps;
    }

    default String getVirtualServersForApplication(String target, String appName) {
        ApplicationRef appRef = getApplicationRefInTarget(appName, target);
        if (appRef != null) {
            return appRef.getVirtualServers();
        } else {
            return null;
        }
    }

    default String getEnabledForApplication(String target, String appName) {
        ApplicationRef appRef = getApplicationRefInTarget(appName, target);
        if (appRef != null) {
            return appRef.getEnabled();
        } else {
            return null;
        }
    }

    default ReferenceContainer getReferenceContainerNamed(String containerName) {
        // Clusters and Servers are ReferenceContainers
        Cluster cluster = getClusterNamed(containerName);

        if (cluster != null) {
            return cluster;
        }

        return getServerNamed(containerName);
    }

    default Cluster getClusterForInstance(String instanceName) {
        for (Cluster cluster : getClusters().getCluster()) {
            for (ServerRef serverRef : cluster.getServerRef()) {
                if (serverRef.getRef().equals(instanceName)) {
                    return cluster;
                }
            }
        }
        return null;
    }

    default List<ReferenceContainer> getAllReferenceContainers() {
        List<ReferenceContainer> referenceContainers = new LinkedList<>(getServers().getServer());
        if (getClusters() != null) {
            referenceContainers.addAll(getClusters().getCluster());
        }
        return referenceContainers;
    }

    default List<ReferenceContainer> getReferenceContainersOf(Config config) {
        // Clusters and Servers are ReferenceContainers
        List<ReferenceContainer> referenceContainers = new LinkedList<>();

        // both the config and its name need to be sanity-checked
        String configName = null;

        if (config != null) {
            configName = config.getName();
        }

        if (!StringUtils.ok(configName)) { // we choose to make this not an error
            return referenceContainers;
        }

        for (ReferenceContainer referenceContainer : getAllReferenceContainers()) {
            if (configName.equals(referenceContainer.getReference())) {
                referenceContainers.add(referenceContainer);
            }
        }
        return referenceContainers;
    }

    default List<Server> getInstancesOnNode(String nodeName) {
        List<Server> servers = new LinkedList<>();
        try {
            if (!StringUtils.ok(nodeName)) {
                return servers;
            }

            for (Server server : getServers().getServer()) {
                if (nodeName.equals(server.getNodeRef())) {
                    servers.add(server);
                }
            }
        } catch (Exception e) {
            getLogger().log(WARNING, errorGettingServers, e.getLocalizedMessage());
        }
        return servers;
    }

    default List<Cluster> getClustersOnNode(String nodeName) {
        HashMap<String, Cluster> clusters = new HashMap<>();
        try {
            for (Server server : getInstancesOnNode(nodeName)) {
                Cluster cluster = server.getCluster();
                if (nodeName.equals(server.getNodeRef())) {
                    clusters.put(cluster.getName(), cluster);
                }
            }
        } catch (Exception e) {
            getLogger().log(WARNING, errorGettingCluster, e.getLocalizedMessage());
        }
        return new ArrayList<>(clusters.values());
    }

    @ConfigExtensionMethod
    <T extends DomainExtension> T getExtensionByType(Class<T> type);

    /**
     * @param configBeanType The config bean type we want to check whether the configuration exists for it or not.
     * @param <P> Type that extends the ConfigBeanProxy which is the type of class we accept as parameter
     * @return true if configuration for the type exists in the target area of domain.xml and false if not.
     */
    default <P extends ConfigBeanProxy> boolean checkIfExtensionExists(Class<P> configBeanType) {
        for (DomainExtension extension : getExtensions()) {
            try {
                configBeanType.cast(extension);
                return true;
            } catch (Exception e) {
                // ignore, not the right type.
            }
        }
        return false;
    }
}
