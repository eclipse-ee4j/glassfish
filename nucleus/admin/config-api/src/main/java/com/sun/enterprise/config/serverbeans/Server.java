/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.config.util.InstanceRegisterInstanceCommandParameters;
import com.sun.enterprise.config.util.PortBaseHelper;
import com.sun.enterprise.config.util.PortManager;
import com.sun.enterprise.config.util.ServerHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.net.NetUtils;

import jakarta.inject.Inject;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.api.logging.LogHelper;
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

import static com.sun.enterprise.config.util.RegisterInstanceCommandParameters.ParameterNames.OPERAND_NAME;
import static com.sun.enterprise.config.util.RegisterInstanceCommandParameters.ParameterNames.PARAM_CHECKPORTS;
import static com.sun.enterprise.config.util.RegisterInstanceCommandParameters.ParameterNames.PARAM_CLUSTER;
import static com.sun.enterprise.config.util.RegisterInstanceCommandParameters.ParameterNames.PARAM_CONFIG;
import static com.sun.enterprise.config.util.RegisterInstanceCommandParameters.ParameterNames.PARAM_LBENABLED;
import static com.sun.enterprise.config.util.RegisterInstanceCommandParameters.ParameterNames.PARAM_NODE;
import static com.sun.enterprise.config.util.RegisterInstanceCommandParameters.ParameterNames.PARAM_PORTBASE;
import static org.glassfish.config.support.Constants.NAME_SERVER_REGEX;

/**
 * Jakarta EE Application Server Configuration.
 *
 * <p>Each Application Server instance is a Jakarta EE compliant container.
 * One server instance is specially designated as the Administration Server in SE/EE
 * User applications cannot be deployed to an Administration Server instance
 */
@Configured
@ConfigRefConstraint(message = "{configref.invalid}", payload = ConfigRefValidator.class)
@NotDuplicateTargetName(message = "{server.duplicate.name}", payload = Server.class)
@ReferenceConstraint(skipDuringCreation = true, payload = Server.class)
public interface Server extends ConfigBeanProxy, PropertyBag, Named, SystemPropertyBag, ReferenceContainer, RefContainer, Payload {

    String lbEnabledSystemProperty = "org.glassfish.lb-enabled-default";

    @Param(name = OPERAND_NAME, primary = true)
    @Override
    void setName(String name) throws PropertyVetoException;

    @NotTargetKeyword(message = "{server.reserved.name}", payload = Server.class)
    @Pattern(regexp = NAME_SERVER_REGEX, message = "{server.invalid.name}", payload = Server.class)
    @Override
    String getName();

    /**
     * Gets the value of the {@code configRef} property.
     *
     * <p>Points to a named config. Needed for stand-alone servers.
     * If server instance is part of a cluster, then it points to the cluster config.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    @NotTargetKeyword(message = "{server.reserved.name}", payload = Server.class)
    @Pattern(regexp = NAME_SERVER_REGEX, message = "Pattern: " + NAME_SERVER_REGEX)
    @ReferenceConstraint.RemoteKey(message = "{resourceref.invalid.configref}", type = Config.class)
    String getConfigRef();

    /**
     * Sets the value of the {@code configRef} property.
     *
     * @param configRef allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = PARAM_CONFIG, optional = true)
    void setConfigRef(String configRef) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nodeAgentRef} property.
     *
     * <p>SE/EE only. Specifies name of node agent where server instance is hosted.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Deprecated
    String getNodeAgentRef();

    /**
     * Sets the value of the {@code nodeAgentRef} property.
     *
     * @param agentRef allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Deprecated
    void setNodeAgentRef(String agentRef) throws PropertyVetoException;

    /**
     * Sets the value of the {@code nodeRef} property.
     *
     * @param nodeRef allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    @Param(name = PARAM_NODE, optional = true)
    void setNodeRef(String nodeRef) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nodeRef} property.
     *
     * <p>SE/EE only. Specifies name of node agent where server instance is hosted.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getNodeRef();

    /**
     * Gets the value of the {@code lbWeight} property.
     *
     * <p>Each server instance in a cluster has a weight, which may be used to represent
     * the relative processing capacity of that instance. Default weight is {@code 100}
     * for every instance. Weighted load balancing policies will use this weight while
     * load balancing requests within the cluster. It is the responsibility of the
     * administrator to set the relative weights correctly, keeping in mind deployed
     * hardware capacity.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "100")
    @Min(value = 1)
    String getLbWeight();

    /**
     * Sets the value of the {@code lbWeight} property.
     *
     * @param lbWeight allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setLbWeight(String lbWeight) throws PropertyVetoException;

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
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal system properties")
    @Element
    @Param(name = InstanceRegisterInstanceCommandParameters.ParameterNames.PARAM_SYSTEMPROPERTIES, optional = true)
    @Override
    List<SystemProperty> getSystemProperty();

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    @Param(name = "properties", optional = true)
    @Override
    List<Property> getProperty();

    @Override
    default String getReference() {
        return getConfigRef();
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

    default void deleteResourceRef(SimpleJndiName refName) throws TransactionFailure {
        final ResourceRef resourceRef = getResourceRef(refName);
        if (resourceRef != null) {
            ConfigSupport.apply(param -> param.getResourceRef().remove(resourceRef), this);
        }
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

    default ApplicationRef getApplicationRef(String appName) {
        for (ApplicationRef appRef : getApplicationRef()) {
            if (appRef.getRef().equals(appName)) {
                return appRef;
            }
        }
        return null;
    }

    /**
     * Returns the cluster instance this instance is referenced in or null if there is no cluster referencing this server
     * instance.
     *
     * @return the cluster owning this instance or null if this is a standalone instance
     */
    default Cluster getCluster() {
        ServiceLocator habitat = Objects.requireNonNull(Dom.unwrap(this)).getHabitat();
        Clusters clusters = habitat.getService(Clusters.class);
        if (clusters != null) {
            for (Cluster cluster : clusters.getCluster()) {
                for (ServerRef serverRef : cluster.getServerRef()) {
                    if (serverRef.getRef().equals(getName())) {
                        return cluster;
                    }
                }
            }
        }
        return null;
    }

    // four trivial methods that ReferenceContainer's need to implement
    @Override
    default boolean isCluster() {
        return false;
    }

    @Override
    default boolean isServer() {
        return true;
    }

    @Override
    default boolean isDas() {
        return "server".equals(getName());
    }

    @Override
    default boolean isInstance() {
        String name = getName();
        return name != null && !name.equals("server");
    }

    default String getAdminHost() {
        try {
            ServerHelper helper = new ServerHelper(this, getConfig());
            return helper.getAdminHost();
        } catch (Exception e) {
            // drop through...
        }
        return null;
    }

    default int getAdminPort() {
        try {
            ServerHelper helper = new ServerHelper(this, getConfig());
            return helper.getAdminPort();
        } catch (Exception e) {
            // drop through...
        }
        return -1;
    }

    default Config getConfig() {
        try {
            ServiceLocator habitat = Objects.requireNonNull(Dom.unwrap(this)).getHabitat();
            Configs configs = habitat.getService(Configs.class);
            String configName = getReference();
            for (Config config : configs.getConfig()) {
                if (configName.equals(config.getName())) {
                    return config;
                }
            }
        } catch (Exception e) {
            // drop through...
        }
        return null;
    }

    /**
     * @return true if this server instance is listening on the admin port, false otherwise.
     */
    default boolean isListeningOnAdminPort() {
        return new ServerHelper(this, getConfig()).isListeningOnAdminPort();
    }

    @Service
    @PerLookup
    class CreateDecorator implements CreationDecorator<Server> {

        @Param(name = PARAM_CLUSTER, optional = true)
        String clusterName;

        @Param(name = PARAM_NODE, optional = true)
        String node = null;

        @Param(name = PARAM_LBENABLED, optional = true)
        String lbEnabled = null;

        @Param(name = PARAM_CHECKPORTS, optional = true, defaultValue = "true")
        boolean checkPorts = true;

        @Param(name = PARAM_PORTBASE, optional = true)
        private String portBase;

        @Param(optional = true, defaultValue = "false", shortName = "t")
        public Boolean terse = false;

        @Inject
        Domain domain;

        @Inject
        private ServerEnvironment env;

        @Inject
        CommandRunner runner;

        @Override
        public void decorate(AdminCommandContext context, final Server instance) throws TransactionFailure, PropertyVetoException {
            Config ourConfig = null;
            Cluster ourCluster = null;
            Logger logger = ConfigApiLoggerInfo.getLogger();
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Server.class);
            Transaction tx = Transaction.getTransaction(instance);
            String configRef = instance.getConfigRef();
            Clusters clusters = domain.getClusters();

            if (tx == null) {
                throw new TransactionFailure(
                        localStrings.getLocalString("noTransaction", "Internal Error - Cannot obtain transaction object"));
            }

            if (node != null) {
                Node theNode = domain.getNodeNamed(node);
                if (theNode == null) {
                    throw new TransactionFailure(localStrings.getLocalString("noSuchNode", "Node {0} does not exist.", node));
                }

                /* 16034: see if instance creation is turned off on node */
                if (!theNode.instanceCreationAllowed()) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("instanceCreationNotAllowed", "Instance creation is disabled on node {0}.", node));
                }
            }

            if (portBase != null) {
                PortBaseHelper pbh = new PortBaseHelper(instance, portBase, false, logger);
                pbh.verifyPortBase();
                pbh.setPorts();
            }

            // cluster instance using cluster config
            if (clusterName != null) {
                if (configRef != null) {
                    throw new TransactionFailure(localStrings.getLocalString("Server.cannotSpecifyBothConfigAndCluster",
                            "A configuration name and cluster name cannot both be specified."));
                }

                if (clusters != null) {
                    for (Cluster cluster : clusters.getCluster()) {
                        if (cluster != null && clusterName.equals(cluster.getName())) {
                            ourCluster = cluster;
                            String configName = cluster.getConfigRef();
                            instance.setConfigRef(configName);
                            ourConfig = domain.getConfigNamed(configName);
                            break;
                        }
                    }
                }

                if (ourCluster == null) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("noSuchCluster", "Cluster {0} does not exist.", clusterName));
                }

                /*
                 * We are only setting this when the discovery uri list
                 * is set to "generate." Otherwise, the user must set this
                 * properly to match the discovery uri list.
                 */
                if (ourCluster.getProperty("GMS_DISCOVERY_URI_LIST") != null
                        && "generate".equals(ourCluster.getProperty("GMS_DISCOVERY_URI_LIST").getValue())) {

                    final String propName = "GMS_LISTENER_PORT-" + ourCluster.getName();

                    /*
                     * Currently all the instances will use the same port
                     * as the DAS. When/if we move to allow more than one
                     * instance/machine, the value here will need to be
                     * calculated differently.
                     */
                    Config serverConf = domain.getConfigNamed("server-config");
                    SystemProperty dasGmsPortProp = serverConf.getSystemProperty(propName);
                    if (dasGmsPortProp != null) {
                        SystemProperty gmsListenerPortProp = instance.createChild(SystemProperty.class);
                        gmsListenerPortProp.setName(propName);
                        gmsListenerPortProp.setValue(dasGmsPortProp.getValue());
                        instance.getSystemProperty().add(gmsListenerPortProp);
                    }
                }

                final String instanceName = instance.getName();
                File configConfigDir = new File(env.getConfigDirPath(), ourCluster.getConfigRef());
                File docroot = new File(configConfigDir, "docroot");
                if (!docroot.exists() && !docroot.mkdirs()) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("noMkdir", "Cannot create configuration specific directory {0}", "docroot"));
                }
                File lib = new File(configConfigDir, "lib/ext");
                if (!lib.exists() && !lib.mkdirs()) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("noMkdir", "Cannot create configuration specific directory {0}", "lib/ext"));
                }

                Cluster c = tx.enroll(ourCluster);
                ServerRef newServerRef = c.createChild(ServerRef.class);
                newServerRef.setRef(instanceName);
                if (lbEnabled != null) {
                    newServerRef.setLbEnabled(lbEnabled);
                } else {
                    //check whether all instances in cluster had lb-enabled set to false
                    List<ServerRef> serverRefs = c.getServerRef();
                    boolean allLBEnabled = false;
                    for (ServerRef serverRef : serverRefs) {
                        allLBEnabled = Boolean.parseBoolean(serverRef.getLbEnabled());
                    }
                    //if there are existing instances in cluster
                    //and they all have lb-enabled to false, set it
                    //false for new instance as well
                    if (!allLBEnabled && serverRefs.size() > 0) {
                        newServerRef.setLbEnabled("false");
                    } else {
                        //check if system property exists and use that
                        String lbEnabledDefault = System.getProperty(lbEnabledSystemProperty);
                        if (lbEnabledDefault != null) {
                            newServerRef.setLbEnabled(lbEnabledDefault);
                        }
                    }
                }
                c.getServerRef().add(newServerRef);
            }

            // instance using specified config
            if (configRef != null) {
                Config specifiedConfig = domain.getConfigs().getConfigByName(configRef);
                if (specifiedConfig == null) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("noSuchConfig", "Configuration {0} does not exist.", configRef));
                }
                ourConfig = specifiedConfig;
                File configConfigDir = new File(env.getConfigDirPath(), specifiedConfig.getName());
                File docroot = new File(configConfigDir, "docroot");
                if (!docroot.exists() && !docroot.mkdirs()) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("noMkdir", "Cannot create configuration specific directory {0}", "docroot"));
                }
                File lib = new File(configConfigDir, "lib/ext");
                if (!lib.exists() && !lib.mkdirs()) {
                    throw new TransactionFailure(
                            localStrings.getLocalString("noMkdir", "Cannot create configuration specific directory {0}", "lib/ext"));
                }
            }

            //stand-alone instance using default-config if config not specified
            if (configRef == null && clusterName == null) {
                Config defaultConfig = domain.getConfigs().getConfigByName("default-config");

                if (defaultConfig == null) {
                    final String msg = localStrings.getLocalString(Server.class, "Cluster.noDefaultConfig",
                            "Can''t find the default config (an element named \"default-config\") "
                                    + "in domain.xml.  You may specify the name of an existing config element next time.");

                    logger.log(Level.SEVERE, ConfigApiLoggerInfo.noDefaultConfig);
                    throw new TransactionFailure(msg);
                }
                final String configName = instance.getName() + "-config";
                instance.setConfigRef(configName);
                final CopyConfig command = (CopyConfig) runner.getCommand("copy-config", context.getActionReport(), context.getLogger());

                Configs configs = domain.getConfigs();
                Configs writableConfigs = tx.enroll(configs);

                ourConfig = command.copyConfig(writableConfigs, defaultConfig, configName, logger);

            }

            for (Resource resource : domain.getResources().getResources()) {
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
                    // todo : what about virtual-servers ?
                    instance.getApplicationRef().add(newAppRef);
                }
            }

            this.addClusterRefs(ourCluster, instance);
            if (checkPorts) {

                PortManager pm = new PortManager(ourCluster, ourConfig, domain, instance);
                String message = pm.process();

                if (message != null && !terse) {
                    ActionReport report = context.getActionReport();
                    report.setMessage(message);
                }
            }

            checkAdminPort(instance, ourConfig, localStrings);
            setupSupplemental(context, instance);
        }

        private void checkAdminPort(final Server instance, final Config config, LocalStringManagerImpl localStrings)
                throws TransactionFailure {
            if (node != null) {
                Node n = domain.getNodeNamed(node);
                if (n != null) {
                    String nodeHost = n.getNodeHost();
                    if (NetUtils.isThisHostLocal(nodeHost)) { // instance on same host as DAS
                        int dasAdminPort = domain.getServerNamed("server").getAdminPort();
                        // Don't use the getAdminPort default method directly on the instance being created
                        int instanceAdminPort = new ServerHelper(instance, config).getAdminPort();
                        if (instanceAdminPort != -1 && dasAdminPort != -1) {
                            if (instanceAdminPort == dasAdminPort) {
                                throw new TransactionFailure(localStrings.getLocalString("Server.cannotHaveSameAdminPortAsDAS",
                                        "Cannot create an instance on the same host as DAS with the same admin port as DAS: {0}.",
                                        instanceAdminPort + ""));
                            }
                        }
                    }
                }
            }
        }

        private void setupSupplemental(AdminCommandContext context, final Server instance) {
            if (clusterName != null) {
                InstanceRegisterInstanceCommandParameters cp = new InstanceRegisterInstanceCommandParameters();
                context.getActionReport().setResultType(InstanceRegisterInstanceCommandParameters.class, cp);

                Node instNode = domain.getNodeNamed(node);

                cp.config = instance.getConfigRef();
                cp.nodehost = instNode.getNodeHost();
                cp.nodedir = instNode.getNodeDir();
                cp.installdir = instNode.getInstallDir();
                List<SystemProperty> spList = instance.getSystemProperty();

                if (spList != null) {
                    Properties p = new Properties();
                    for (SystemProperty sp : spList) {
                        p.put(sp.getName(), sp.getValue());
                    }
                    cp.systemProperties = p;
                }
            }
        }

        private void addClusterRefs(Cluster cluster, Server instance) throws TransactionFailure, PropertyVetoException {
            if (cluster != null) {
                for (ApplicationRef appRef : cluster.getApplicationRef()) {
                    if (instance.getApplicationRef(appRef.getRef()) == null) {
                        ApplicationRef newAppRef = instance.createChild(ApplicationRef.class);
                        newAppRef.setRef(appRef.getRef());
                        newAppRef.setDisableTimeoutInMinutes(appRef.getDisableTimeoutInMinutes());
                        newAppRef.setEnabled(appRef.getEnabled());
                        newAppRef.setLbEnabled(appRef.getLbEnabled());
                        newAppRef.setVirtualServers(appRef.getVirtualServers());
                        instance.getApplicationRef().add(newAppRef);
                    }
                }
                for (ResourceRef rr : cluster.getResourceRef()) {
                    if (instance.getResourceRef(SimpleJndiName.of(rr.getRef())) == null) {
                        ResourceRef newRR = instance.createChild(ResourceRef.class);
                        newRR.setRef(rr.getRef());
                        newRR.setEnabled(rr.getEnabled());
                        instance.getResourceRef().add(newRR);
                    }
                }
            }
        }
    }

    @Service
    @PerLookup
    class DeleteDecorator implements DeletionDecorator<Servers, Server> {

        @Inject
        Configs configs;

        @Inject
        private Domain domain;

        @Inject
        private ServerEnvironment env;

        @Override
        public void decorate(AdminCommandContext context, Servers parent, final Server child) throws PropertyVetoException, TransactionFailure {
            final Logger logger = ConfigApiLoggerInfo.getLogger();
            LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Server.class);
            final ActionReport report = context.getActionReport();
            Transaction t = Transaction.getTransaction(parent);
            Cluster cluster = domain.getClusterForInstance(child.getName());
            boolean isStandAlone = cluster == null;

            /* setup supplemental */
            if (!isStandAlone && env.isDas()) {
                context.getActionReport().setResultType(String.class, cluster.getName());
            }

            if (isStandAlone) { // remove config <instance>-config
                String instanceConfig = child.getConfigRef();
                final Config config = configs.getConfigByName(instanceConfig);

                // bnevins June 2010
                // don't delete the config is someone else holds a reference to it!
                if (config != null && domain.getReferenceContainersOf(config).size() > 1) {
                    return;
                }

                // bnevins September 30, 2010
                // don't delete the config if it wasn't auto-generated.
                final String autoGeneratedName = child.getName() + "-config";
                if (!autoGeneratedName.equals(instanceConfig)) {
                    return;
                }

                try {
                    if (config != null) {
                        File configConfigDir = new File(env.getConfigDirPath(), config.getName());
                        FileUtils.whack(configConfigDir);
                    }
                } catch (Exception e) {
                    // no big deal - just ignore
                }
                try {
                    if (t != null) {
                        Configs c = t.enroll(configs);
                        List<Config> configList = c.getConfig();
                        configList.remove(config);
                    }
                } catch (TransactionFailure ex) {
                    LogHelper.log(logger, Level.SEVERE, ConfigApiLoggerInfo.deleteConfigFailed, ex, instanceConfig);
                    String msg = ex.getMessage() != null ? ex.getMessage()
                            : localStrings.getLocalString("deleteConfigFailed", "Unable to remove config {0}", instanceConfig);
                    report.setMessage(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setFailureCause(ex);
                    throw ex;
                }
            } else { // remove server-ref from cluster
                final String instanceName = child.getName();
                if (t != null) {
                    try {
                        Cluster c = t.enroll(cluster);

                        List<ServerRef> serverRefList = c.getServerRef();
                        ServerRef serverRef = null;

                        for (ServerRef sr : serverRefList) {
                            if (sr.getRef().equals(instanceName)) {
                                serverRef = sr;
                                break;
                            }
                        }
                        if (serverRef != null) {
                            serverRefList.remove(serverRef);
                        }
                    } catch (TransactionFailure ex) {
                        LogHelper.log(logger, Level.SEVERE, ConfigApiLoggerInfo.deleteServerRefFailed, ex, instanceName, cluster.getName());
                        String msg = ex.getMessage() != null ? ex.getMessage()
                                : localStrings.getLocalString("deleteServerRefFailed", "Unable to remove server-ref {0} from cluster {1}",
                                        instanceName, cluster.getName());
                        report.setMessage(msg);
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setFailureCause(ex);
                        throw ex;
                    }
                }
            }
        }
    }
}
