/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.customvalidators.ConfigRefConstraint;
import com.sun.enterprise.config.serverbeans.customvalidators.ConfigRefValidator;
import com.sun.enterprise.config.serverbeans.customvalidators.NotDuplicateTargetName;
import com.sun.enterprise.config.serverbeans.customvalidators.NotTargetKeyword;
import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CreationDecorator;
import org.glassfish.config.support.DeletionDecorator;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static org.glassfish.config.support.Constants.NAME_SERVER_REGEX;
import static org.glassfish.security.common.SharedSecureRandom.SECURE_RANDOM;

/**
 * A cluster defines a homogeneous set of server instances that share the same applications,
 * resources, and configuration.
 */
@Configured
@ConfigRefConstraint(message = "{configref.invalid}", payload = ConfigRefValidator.class)
@NotDuplicateTargetName(message = "{cluster.duplicate.name}", payload = Cluster.class)
@ReferenceConstraint(skipDuringCreation = true, payload = Cluster.class)
public interface Cluster extends ConfigBeanProxy, PropertyBag, Named, SystemPropertyBag, ReferenceContainer, RefContainer, Payload {

    /**
     * Sets the cluster {@code name}.
     *
     * @param name cluster name
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "name", primary = true)
    @Override
    void setName(String name) throws PropertyVetoException;

    @NotTargetKeyword(message = "{cluster.reserved.name}", payload = Cluster.class)
    @Pattern(regexp = NAME_SERVER_REGEX, message = "{cluster.invalid.name}", payload = Cluster.class)
    @Override
    String getName();

    /**
     * Points to a named config.
     *
     * <p>All server instances in the cluster will share this config.
     *
     * @return a named config name
     */
    @Attribute
    @NotNull
    @Pattern(regexp = NAME_SERVER_REGEX, message = "{config.invalid.name}")
    @ReferenceConstraint.RemoteKey(message = "{resourceref.invalid.configref}", type = Config.class)
    String getConfigRef();

    /**
     * Sets the value of the {@code configRef} property.
     *
     * @param configRef allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "config", optional = true)
    @I18n("generic.config")
    void setConfigRef(String configRef) throws PropertyVetoException;

    /**
     * Gets the value of the {@code gmsEnabled} property.
     *
     * <p>When {@code gms-enabled} is set to {@code true}, the GMS services will be started
     * as a lifecycle module in each the application server in the cluster.
     *
     * @return {@code true} | {@code false} as a string, {@code null} means false
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class, required = true)
    @NotNull
    String getGmsEnabled();

    /**
     * Sets the value of the {@code gmsEnabled} property.
     *
     * @param gmsEnabled allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "gmsenabled", optional = true)
    void setGmsEnabled(String gmsEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code broadcast} property.
     *
     * <p>When {@code broadcast} is set to default of {@code udpmulticast} and GmsMulticastPort
     * GMSMulticastAddress are not set, then their values are generated. When {@code broadcast}
     * is set to implied unicast using udp or tcp protocol, then the {@code VIRUTAL_MUTLICAST_URI_LIST}
     * is generated for virtual broadcast over unicast mode.
     *
     * @return {@code true} | {@code false} as a string, {@code null} means false
     */
    @Attribute(defaultValue = "udpmulticast", required = true)
    @NotNull
    String getBroadcast();

    /**
     * Sets the value of the {@code broadcast} property.
     *
     * @param broadcast allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "gmsbroadcast", optional = true)
    void setBroadcast(String broadcast) throws PropertyVetoException;

    /**
     * Gets the value of the {@code gmsMulticastPort} property.
     *
     * <p>This is the communication port GMS uses to listen for group events.
     *
     * <p>This should be a valid port number.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Min(value = 2048)
    @Max(value = 49151) // fix bug 13475586
    String getGmsMulticastPort();

    /**
     * Sets the value of the {@code gmsMulticastPort} property.
     *
     * @param multicastPort allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "multicastport", optional = true, alias = "heartbeatport")
    void setGmsMulticastPort(String multicastPort) throws PropertyVetoException;

    /**
     * Gets the value of the {@code gmsMulticastAddress} property.
     *
     * <p>This is the address (only multicast supported) at which GMS will listen for group events.
     *
     * <p>Must be unique for each cluster.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getGmsMulticastAddress();

    /**
     * Sets the value of the {@code gmsMulticastAddress} property.
     *
     * @param multicastAddress allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "multicastaddress", optional = true, alias = "heartbeataddress")
    void setGmsMulticastAddress(String multicastAddress) throws PropertyVetoException;

    /**
     * Gets the value of the {@code gmsBindInterfaceAddress} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getGmsBindInterfaceAddress();

    /**
     * Sets the value of the {@code gmsBindInterfaceAddress} property.
     *
     * @param bindAddress allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = "bindaddress", optional = true)
    void setGmsBindInterfaceAddress(String bindAddress) throws PropertyVetoException;

    /**
     * Gets the value of the {@code heartbeatEnabled} property.
     *
     * <p>When {@code heartbeat-enabled} is set to {@code true}, the GMS services
     * will be started as a lifecycle module in each the application server in the cluster.
     * When {@code heartbeat-enabled} is set to {@code false}, GMS will not be started and its
     * services will be unavailable. Clusters should function albeit with reduced functionality.
     *
     * @return {@code true} | {@code false} as a string, {@code null} means false
     */
    @Deprecated
    @Attribute
    String getHeartbeatEnabled();

    /**
     * Sets the value of the {@code heartbeatEnabled} property.
     *
     * @param heartbeatEnabled allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Deprecated
    void setHeartbeatEnabled(String heartbeatEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code heartbeatPort} property.
     *
     * <p>This is the communication port GMS uses to listen for group events.
     *
     * <p>This should be a valid port number.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Deprecated
    String getHeartbeatPort();

    /**
     * Sets the value of the {@code heartbeatPort} property.
     *
     * @param heartbeatPort allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Deprecated
    void setHeartbeatPort(String heartbeatPort) throws PropertyVetoException;

    /**
     * Gets the value of the {@code heartbeatAddress} property.
     *
     * <p>This is the address (only multicast supported) at which GMS will listen for group events.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Deprecated
    String getHeartbeatAddress();

    /**
     * Sets the value of the {@code heartbeatAddress} property.
     *
     * @param heartbeatAddress allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Deprecated
    void setHeartbeatAddress(String heartbeatAddress) throws PropertyVetoException;

    /**
     * Gets the value of the {@code serverRef} property.
     *
     * <p>List of servers in the cluster
     *
     * @return list of configured {@link ServerRef}
     */
    @Element
    List<ServerRef> getServerRef();

    /**
     * Gets the value of the {@code systemProperty} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code systemProperty} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getSystemProperty().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link SystemProperty}
     */
    @Element
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal system props")
    @Param(name = "systemproperties", optional = true)
    @Override
    List<SystemProperty> getSystemProperty();

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Complete PropertyDesc for legal props")
    @PropertiesDesc(props = {
            @PropertyDesc(name = "GMS_LISTENER_PORT", defaultValue = "9090", description = "GMS listener port")
    })
    @Element
    @Param(name = "properties", optional = true)
    @Override
    List<Property> getProperty();

    @Element("*")
    List<ClusterExtension> getExtensions();

    /**
     * Returns the cluster configuration reference
     *
     * @return the {@code config-ref} attribute
     */
    @Override
    default String getReference() {
        return getConfigRef();
    }

    default List<Server> getInstances() {
        ServiceLocator habitat = Objects.requireNonNull(Dom.unwrap(this)).getHabitat();
        Domain domain = habitat.getService(Domain.class);

        ArrayList<Server> instances = new ArrayList<>();
        for (ServerRef serverRef : getServerRef()) {
            Server server = domain.getServerNamed(serverRef.getRef());
            // the instance's domain.xml only has its own server
            // element and not other server elements in the cluster
            if (server != null) {
                instances.add(domain.getServerNamed(serverRef.getRef()));
            }
        }
        return instances;
    }

    default ServerRef getServerRefByRef(String refName) {
        for (ServerRef serverRef : getServerRef()) {
            if (serverRef.getRef().equals(refName)) {
                return serverRef;
            }
        }
        return null;
    }

    // four trivial methods that ReferenceContainer's need to implement
    @Override
    default boolean isCluster() {
        return true;
    }

    @Override
    default boolean isServer() {
        return false;
    }

    @Override
    default boolean isDas() {
        return false;
    }

    @Override
    default boolean isInstance() {
        return false;
    }

    default boolean isVirtual() {
        return !getExtensionsByType(VirtualMachineExtension.class).isEmpty();
    }

    default ApplicationRef getApplicationRef(String appName) {
        for (ApplicationRef appRef : getApplicationRef()) {
            if (appRef.getRef().equals(appName)) {
                return appRef;
            }
        }
        return null;
    }

    default ResourceRef getResourceRef(SimpleJndiName refName) {
        for (ResourceRef resourceRef : getResourceRef()) {
            if (resourceRef.getRef().equals(refName.toString())) {
                return resourceRef;
            }
        }
        return null;
    }

    default boolean isResourceRefExists(SimpleJndiName refName) {
        return getResourceRef(refName) != null;
    }

    default void createResourceRef(String enabled, SimpleJndiName refName) throws TransactionFailure {
        ConfigSupport.apply(param -> {
            ResourceRef newResourceRef = param.createChild(ResourceRef.class);
            newResourceRef.setEnabled(enabled);
            newResourceRef.setRef(refName.toString());
            param.getResourceRef().add(newResourceRef);
            return newResourceRef;
        }, this);
    }

    default void deleteResourceRef(SimpleJndiName refName) throws TransactionFailure {
        final ResourceRef resourceRef = getResourceRef(refName);
        if (resourceRef != null) {
            ConfigSupport.apply(param -> param.getResourceRef().remove(resourceRef), this);
        }
    }

    default <T extends ClusterExtension> List<T> getExtensionsByType(Class<T> type) {
        List<T> extensions = new ArrayList<>();
        for (ClusterExtension extension : getExtensions()) {
            try {
                type.cast(extension);
                extensions.add((T) extension);
            } catch (ClassCastException e) {
                // ignore, not the right type
            }
        }
        return extensions;
    }

    default <T extends ClusterExtension> T getExtensionsByTypeAndName(Class<T> type, String name) {
        for (ClusterExtension extension : getExtensions()) {
            try {
                if (extension.getName().equals(name)) {
                    return type.cast(extension);
                }
            } catch (ClassCastException e) {
                // ignore, not the right type
            }
        }
        return null;
    }

    @Service
    @PerLookup
    class Decorator implements CreationDecorator<Cluster> {

        @Param(name = "config", optional = true)
        String configRef;

        @Param(optional = true, obsolete = true)
        String hosts;

        @Param(optional = true, obsolete = true)
        String haagentport;

        @Param(optional = true, obsolete = true)
        String haadminpassword;

        @Param(optional = true, obsolete = true)
        String haadminpasswordfile;

        @Param(optional = true, obsolete = true)
        String devicesize;

        @Param(optional = true, obsolete = true)
        String haproperty;

        @Param(optional = true, obsolete = true)
        String autohadb;

        @Param(optional = true, obsolete = true)
        String portbase;

        @Inject
        ServiceLocator habitat;

        @Inject
        ServerEnvironment env;

        @Inject
        Domain domain;

        @Inject
        CommandRunner runner;

        /**
         * Decorates the newly CRUD created cluster configuration instance. tasks : - ensures that
         * it references an existing configuration - creates a new config from the {@code default-config}
         * if no {@code config-ref} was provided. - check for deprecated parameters.
         *
         * @param context administration command context
         * @param instance newly created configuration element
         */
        @Override
        public void decorate(AdminCommandContext context, final Cluster instance) throws TransactionFailure, PropertyVetoException {
            Logger logger = ConfigApiLoggerInfo.getLogger();
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Cluster.class);
            Transaction tx = Transaction.getTransaction(instance);
            //check if cluster software is installed else fail , see issue 12023
            final CopyConfig command = (CopyConfig) runner.getCommand("copy-config", context.getActionReport());
            if (command == null) {
                throw new TransactionFailure(
                        localStrings.getLocalString("cannot.execute.command", "Cluster software is not installed"));
            }
            final String instanceName = instance.getName();
            if (instance.getGmsBindInterfaceAddress() == null) {
                instance.setGmsBindInterfaceAddress(String.format("${GMS-BIND-INTERFACE-ADDRESS-%s}", instanceName));
            }

            if (configRef == null) {
                Config config = habitat.getService(Config.class, "default-config");
                if (config == null) {
                    config = habitat.getAllServices(Config.class).iterator().next();
                    logger.log(Level.WARNING, ConfigApiLoggerInfo.noDefaultConfigFound,
                            new Object[] { config.getName(), instance.getName() });
                }

                Configs configs = domain.getConfigs();
                Configs writableConfigs = tx.enroll(configs);
                final String configName = instance.getName() + "-config";
                instance.setConfigRef(configName);
                command.copyConfig(writableConfigs, config, configName, logger);
            } else {
                // cluster using specified config
                Config specifiedConfig = domain.getConfigs().getConfigByName(configRef);
                if (specifiedConfig == null) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("noSuchConfig", "Configuration {0} does not exist.", configRef));
                }
            }

            Property gmsListenerPort = instance.getProperty("GMS_LISTENER_PORT");
            boolean needToAddGmsListenerPort = false;
            if (gmsListenerPort == null) {
                needToAddGmsListenerPort = true;
                gmsListenerPort = instance.createChild(Property.class);
                gmsListenerPort.setName("GMS_LISTENER_PORT");
                gmsListenerPort.setValue(String.format("${GMS_LISTENER_PORT-%s}", instanceName));
                // do not add gmsListenerPort until know whether it needs to be fixed or symbolic.
                // for non-multicast with generate or list of ip addresses, port needs to be a fixed value
                // all members of cluster. for non-multicast with list of uri, the GMS_LISTENER_PORT is
                // set to symbolic system environment variable that is set different for each instance of cluster.
            }

            // handle generation of udp multicast and non-multicast mode for DAS managed cluster.
            // inspect cluster attribute broadcast and cluster property GMS_DISCOVERY_URI_LIST.
            String DEFAULT_BROADCAST = "udpmulticast";
            String broadcastProtocol = instance.getBroadcast();
            Property discoveryUriListProp = instance.getProperty("GMS_DISCOVERY_URI_LIST");
            String discoveryUriList = discoveryUriListProp != null ? discoveryUriListProp.getValue() : null;
            if (discoveryUriList != null && DEFAULT_BROADCAST.equals(broadcastProtocol)) {

                // override default broadcast protocol of udp multicast when GMS_DISCOVERY_URI_LIST has been set.
                instance.setBroadcast("tcp");
                broadcastProtocol = "tcp";
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, ConfigApiLoggerInfo.clusterGSMBroadCast, instance.getBroadcast());
                logger.log(Level.FINE, ConfigApiLoggerInfo.clusterGSMDeliveryURI, discoveryUriList);
            }
            if (DEFAULT_BROADCAST.equals(broadcastProtocol)) {

                // only generate these values when they are not set AND broadcastProtocol is set to enable UDP multicast.
                // Note: that this is the default for DAS controlled clusters.
                if (instance.getGmsMulticastAddress() == null) {
                    instance.setGmsMulticastAddress(generateHeartbeatAddress());
                }
                if (instance.getGmsMulticastPort() == null) {
                    instance.setGmsMulticastPort(generateHeartbeatPort());
                }

                if (needToAddGmsListenerPort) {
                    instance.getProperty().add(gmsListenerPort);
                }
            } else {

                final String GENERATE = "generate";

                // cover case that broadcast is set to non-multicast and no
                // cluster property GMS_DISCOVERY_URI_LIST exists.
                // create the property and set to "generate".
                // gms-adapter will handle generation of the list when needed
                if (discoveryUriListProp == null) {
                    discoveryUriListProp = instance.createChild(Property.class);
                    discoveryUriListProp.setName("GMS_DISCOVERY_URI_LIST");
                    discoveryUriListProp.setValue(GENERATE);
                    instance.getProperty().add(discoveryUriListProp);
                }

                String TCPPORT = gmsListenerPort.getValue();
                if (GENERATE.equals(discoveryUriListProp.getValue())) {

                    // TODO: implement UDP unicast.

                    // Only tcp mode is supported now.
                    // So either "udpunicast" or "tcp" for broadcast mode is treated the same.
                    if (TCPPORT == null || TCPPORT.trim().charAt(0) == '$') {

                        // generate a random port since user did not provide one.
                        // better fix in future would be to walk existing clusters and pick an unused port.
                        TCPPORT = Integer.toString(SECURE_RANDOM.nextInt(9200 - 9090) + 9090);

                        // hardcode all instances to use same default port.
                        // generate mode does not support multiple instances on one machine.
                        gmsListenerPort.setValue(TCPPORT);
                        if (needToAddGmsListenerPort) {
                            instance.getProperty().add(gmsListenerPort);
                        }
                    }
                } else {
                    // lookup server-config and set environment property value
                    // GMS_LISTENER_PORT-clusterName to fixed value.
                    Config config = habitat.getService(Config.class, "server-config");
                    if (config != null) {
                        String propName = String.format("GMS_LISTENER_PORT-%s", instanceName);
                        if (config.getProperty(propName) == null) {
                            Config writeableConfig = tx.enroll(config);
                            SystemProperty gmsListenerPortSysProp = instance.createChild(SystemProperty.class);
                            gmsListenerPortSysProp.setName(propName);
                            if (TCPPORT == null || TCPPORT.trim().charAt(0) == '$') {
                                String generateGmsListenerPort = Integer
                                    .toString(SECURE_RANDOM.nextInt(9200 - 9090) + 9090);
                                gmsListenerPortSysProp.setValue(generateGmsListenerPort);
                            } else {
                                gmsListenerPortSysProp.setValue(TCPPORT);
                            }
                            writeableConfig.getSystemProperty().add(gmsListenerPortSysProp);
                        }
                    }
                    if (needToAddGmsListenerPort) {
                        instance.getProperty().add(gmsListenerPort);
                    }
                }
            }

            Resources resources = domain.getResources();
            for (Resource resource : resources.getResources()) {
                if (Resource.copyToInstance(resource)) {
                    String name = null;
                    if (resource instanceof BindableResource) {
                        name = ((BindableResource) resource).getJndiName();
                    }
                    if (resource instanceof Named) {
                        name = ((Named) resource).getName();
                    }
                    if (name == null) {
                        throw new TransactionFailure("Cannot add un-named resources to the new server instance");
                    }
                    ResourceRef newResourceRef = instance.createChild(ResourceRef.class);
                    newResourceRef.setRef(name);
                    instance.getResourceRef().add(newResourceRef);
                }
            }
            for (Application application : domain.getApplications().getApplications()) {
                if (application.getObjectType().equals("system-all") || application.getObjectType().equals("system-instance")) {
                    ApplicationRef newAppRef = instance.createChild(ApplicationRef.class);
                    newAppRef.setRef(application.getName());
                    instance.getApplicationRef().add(newAppRef);
                }
            }

            if (hosts != null || haagentport != null || haadminpassword != null || haadminpasswordfile != null || devicesize != null
                    || haproperty != null || autohadb != null || portbase != null) {
                context.getActionReport().setActionExitCode(ActionReport.ExitCode.WARNING);
                context.getActionReport().setMessage("Obsolete options used.");
            }

        }

        private String generateHeartbeatPort() {
            final int MIN_GMS_MULTICAST_PORT = 2048;
            final int MAX_GMS_MULTICAST_PORT = 32000; // be pessimistic for generation of random port and assume
                                                      // ephemeral port range between 32k to 64k.

            int portInterval = MAX_GMS_MULTICAST_PORT - MIN_GMS_MULTICAST_PORT;
            return Integer.toString(Math.round((float) (Math.random() * portInterval)) + MIN_GMS_MULTICAST_PORT);
        }

        private String generateHeartbeatAddress() {
            final int MAX_GMS_MULTICAST_ADDRESS_SUBRANGE = 255;

            final StringBuffer heartbeatAddressBfr = new StringBuffer("228.9.");
            heartbeatAddressBfr.append(Math.round(Math.random() * MAX_GMS_MULTICAST_ADDRESS_SUBRANGE)).append('.')
                    .append(Math.round(Math.random() * MAX_GMS_MULTICAST_ADDRESS_SUBRANGE));
            return heartbeatAddressBfr.toString();
        }
    }

    @Service
    @PerLookup
    class DeleteDecorator implements DeletionDecorator<Clusters, Cluster> {

        @Param(name = "nodeagent", optional = true, obsolete = true)
        String nodeagent;

        // for backward compatibility, ignored.
        @Param(name = "autohadboverride", optional = true, obsolete = true)
        String autohadboverride;

        @Inject
        private Domain domain;

        @Inject
        Configs configs;

        @Inject
        private ServerEnvironment env;

        @Inject
        CommandRunner runner;

        @Override
        public void decorate(AdminCommandContext context, Clusters parent, Cluster child) throws PropertyVetoException, TransactionFailure {
            Logger logger = ConfigApiLoggerInfo.getLogger();
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Cluster.class);
            final ActionReport report = context.getActionReport();

            // check to see if the clustering software is installed
            AdminCommand command = runner.getCommand("copy-config", report);
            if (command == null) {
                String msg = localStrings.getLocalString("cannot.execute.command", "Cluster software is not installed");
                throw new TransactionFailure(msg);
            }

            String instanceConfig = child.getConfigRef();
            final Config config = configs.getConfigByName(instanceConfig);
            Transaction tx = Transaction.getTransaction(parent);

            //check if the cluster contains instances throw error that cluster
            //cannot be deleted
            //issue 12172
            List<ServerRef> serverRefs = child.getServerRef();
            StringBuffer namesOfServers = new StringBuffer();
            if (serverRefs.size() > 0) {
                for (ServerRef serverRef : serverRefs) {
                    namesOfServers.append(new StringBuffer(serverRef.getRef()).append(','));
                }

                final String msg = localStrings.getLocalString("Cluster.hasInstances",
                        "Cluster {0} contains server instances {1} and must not contain any instances", child.getName(),
                        namesOfServers.toString());

                logger.log(Level.SEVERE, ConfigApiLoggerInfo.clusterMustNotContainInstance,
                        new Object[] { child.getName(), namesOfServers.toString() });
                throw new TransactionFailure(msg);
            }

            // remove GMS_LISTENER_PORT-clusterName prop from server config
            Config serverConfig = configs.getConfigByName("server-config");
            String propName = String.format("GMS_LISTENER_PORT-%s", child.getName());
            SystemProperty gmsProp = serverConfig.getSystemProperty(propName);
            if (gmsProp != null && tx != null) {
                Config c = tx.enroll(serverConfig);
                List<SystemProperty> propList = c.getSystemProperty();
                propList.remove(gmsProp);
            }

            // check if the config is null or still in use by some other
            // ReferenceContainer or is not <cluster-name>-config -- if so just return...
            if (config == null || domain.getReferenceContainersOf(config).size() > 1 || !instanceConfig.equals(child.getName() + "-config")) {
                return;
            }

            try {
                File configConfigDir = new File(env.getConfigDirPath(), config.getName());
                FileUtils.whack(configConfigDir);
            } catch (Exception e) {
                // no big deal - just ignore
            }

            try {
                if (tx != null) {
                    Configs c = tx.enroll(configs);
                    List<Config> configList = c.getConfig();
                    configList.remove(config);
                }
            } catch (TransactionFailure ex) {
                logger.log(Level.SEVERE, ConfigApiLoggerInfo.deleteConfigFailed, new Object[] { instanceConfig, ex });
                String msg = ex.getMessage() != null ? ex.getMessage()
                        : localStrings.getLocalString("deleteConfigFailed", "Unable to remove config {0}", instanceConfig);
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(ex);
                throw ex;
            }
        }
    }
}
