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

package com.sun.enterprise.connectors.jms.system;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.connectors.inbound.ActiveInboundResourceAdapterImpl;
import com.sun.enterprise.connectors.jms.JMSLoggerInfo;
import com.sun.enterprise.connectors.jms.config.JmsAvailability;
import com.sun.enterprise.connectors.jms.config.JmsHost;
import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.connectors.jms.inflow.MdbContainerProps;
import com.sun.enterprise.connectors.jms.util.JmsRaUtil;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.connectors.util.SetMethodAction;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.JMSDestinationDefinitionDescriptor;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.v3.services.impl.DummyNetworkListener;
import com.sun.enterprise.v3.services.impl.GrizzlyService;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapterInternalException;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.rmi.Naming;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.glassfish.admin.mbeanserver.JMXStartupService;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.logging.LogHelper;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.grizzly.LazyServiceInitializer;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

import static com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils.getReservePrefixedJNDINameForDescriptor;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Represents an active JMS resource adapter. This does
 * additional configuration to ManagedConnectionFactory
 * and ResourceAdapter java beans.
 *
 * XXX: For code management reasons, think about splitting this
 * to a preHawk and postHawk RA (with postHawk RA extending preHawk RA).
 *
 * @author Satish Kumar
 */
@Service
@Singleton
@Named(ActiveJmsResourceAdapter.JMS_SERVICE)
public class ActiveJmsResourceAdapter extends ActiveInboundResourceAdapterImpl implements LazyServiceInitializer, PostConstruct {

    private static final Logger LOG = JMSLoggerInfo.getLogger();
    private static final String SETTER = "setProperty";
    private static final String SEPARATOR = "#";

    //RA Javabean properties.
    public static final String CONNECTION_URL = "ConnectionURL";
    private static final String RECONNECTENABLED = "ReconnectEnabled";
    private static final String RECONNECTINTERVAL = "ReconnectInterval";
    private static final String RECONNECTATTEMPTS = "ReconnectAttempts";
    private static final String GROUPNAME = "GroupName";
    private static final String CLUSTERCONTAINER = "InClusteredContainer";

    //Lifecycle RA JavaBean properties
    private static final String BROKERTYPE="BrokerType";
    private static final String BROKERINSTANCENAME="BrokerInstanceName";
    private static final String BROKERBINDADDRESS="BrokerBindAddress";
    private static final String BROKERPORT="BrokerPort";
    private static final String BROKERARGS="BrokerArgs";
    private static final String BROKERHOMEDIR="BrokerHomeDir";
    private static final String BROKERLIBDIR ="BrokerLibDir";
    private static final String BROKERVARDIR="BrokerVarDir";
    private static final String BROKERJAVADIR="BrokerJavaDir";
    private static final String BROKERSTARTTIMEOUT="BrokerStartTimeOut";
    public static final String ADMINUSERNAME="AdminUsername";
    public static final String ADMINPASSWORD="AdminPassword";
    private static final String USERNAME="UserName";
    private static final String PASSWORD="Password";
    private static final String MQ_PORTMAPPER_BIND = "doBind";//"imq.portmapper.bind";
    private static final String MASTERBROKER="MasterBroker";

    //JMX properties
    private static final String RMIREGISTRYPORT="RmiRegistryPort";
    private static final String USEEXTERNALRMIREGISTRY="startRMIRegistry";
    private static final int DEFAULTRMIREGISTRYPORT =7776;
    private static final int BROKERRMIPORTOFFSET=100;

    //Availability properties
    private static final String CONVENTIONAL_CLUSTER__OF_PEER_BROKERS_DB_PREFIX= "imq.cluster.sharecc.persist.jdbc.";
    private static final String ENHANCED_CLUSTER_DB_PREFIX= "imq.persist.jdbc.";
    private static final String HAREQUIRED = "HARequired";
    private static final String CLUSTERID = "ClusterId";
    private static final String BROKERID = "BrokerId";
    private static final String BROKERENABLEHA = "BrokerEnableHA";

    private static final String DB_HADB_PROPS = "DBProps";

    //Activation config properties of MQ resource adapter.
    public static final String DESTINATION = "destination";
    public static final String DESTINATION_TYPE = "destinationType";
    private static String SUBSCRIPTION_NAME = "SubscriptionName";
    private static final String PHYSICAL_DESTINATION = "Name";
    private static String MAXPOOLSIZE = "EndpointPoolMaxSize";
    private static String MINPOOLSIZE = "EndpointPoolSteadySize";
    private static String RESIZECOUNT = "EndpointPoolResizeCount";
    private static String RESIZETIMEOUT = "EndpointPoolResizeTimeout";
    private static String REDELIVERYCOUNT = "EndpointExceptionRedeliveryAttempts";
    private static String LOWERCASE_REDELIVERYCOUNT = "endpointExceptionRedeliveryAttempts";
    public static final String ADDRESSLIST = "AddressList";
    private static String ADRLIST_BEHAVIOUR = "AddressListBehavior";
    private static String ADRLIST_ITERATIONS = "AddressListIterations";
    private static final String MDBIDENTIFIER = "MdbName";
    public static final String JMS_SERVICE = "mq-service";

    //MCF properties
    private static final String MCFADDRESSLIST = "MessageServiceAddressList";

    //private static final String XA_JOIN_ALLOWED= "imq.jmsra.isXAJoinAllowed";

    private static final StringManager I18N = StringManager.getManager(ActiveJmsResourceAdapter.class);

    //Lifecycle properties
    public static final String EMBEDDED="EMBEDDED";
    public static final String LOCAL="LOCAL";
    public static final String REMOTE="REMOTE";
    public static final String DIRECT="DIRECT";

    // Both the properties below are hacks. These will be changed later on.
    private static String MQRmiPort = System.getProperty("com.sun.enterprise.connectors.system.MQRmiPort");
    private static final String DASRMIPORT = "31099";

    private static final String REVERT_TO_EMBEDDED_PROPERTY = "com.sun.enterprise.connectors.system.RevertToEmbedded";
    private static final String BROKER_RMI_PORT = "com.sun.enterprise.connectors.system.mq.rmiport";

    private static final String DEFAULT_SERVER = "server";
    private static final String DEFAULT_MQ_INSTANCE = "imqbroker";
    private static final String MQ_DIR_NAME = "imq";

    public static final String GRIZZLY_PROXY_PREFIX = "JMS_PROXY_";

    private enum ClusterMode {
      ENHANCED, CONVENTIONAL_WITH_MASTER_BROKER, CONVENTIONAL_OF_PEER_BROKERS;
    }

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Inject
    private GlassfishNamingManager nm;

    @Inject
    private Provider<JMSConfigListener> jmsConfigListenerProvider;

    @Inject
    private Provider<ServerEnvironmentImpl> serverEnvironmentImplProvider;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Provider<AdminService> adminServiceProvider;

    @Inject
    private Provider<ServerContext> serverContextProvider;

    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;

    @Inject
    private ServiceLocator habitat;

    @Inject
    private ApplicationRegistry appRegistry;

    private MQAddressList urlList;
    private String addressList;
    private String brkrPort;
    private boolean doBind;

    private Properties dbProps;
    private String brokerInstanceName;

    private boolean grizzlyListenerInit;
    private final Set<String> grizzlyListeners = new HashSet<>();


    @Override
    public void postConstruct() {
        /*
         * If any special handling is required for the system resource
         * adapter, then ActiveResourceAdapter implementation for that
         * RA should implement additional functionality by extending
         * ActiveInboundResourceAdapter or ActiveOutboundResourceAdapter.
         * For example ActiveJmsResourceAdapter extends
         * ActiveInboundResourceAdapter.
         */
        // if (moduleName.equals(ConnectorConstants.DEFAULT_JMS_ADAPTER)) {
        // Upgrade jms resource adapter, if necessary before starting the RA.
        final JMSConfigListener jmsConfigListener;
        try {
            jmsConfigListener = jmsConfigListenerProvider.get();
        } catch (RuntimeException e) {
            LOG.log(FINE, "No JMSConfigListener provided, nothing to do.", e);
            return;
        }
        try {
            jmsConfigListener.setActiveResourceAdapter(this);
            JmsRaUtil raUtil = new JmsRaUtil();
            raUtil.upgradeIfNecessary();
        } catch (Exception e) {
            LOG.log(SEVERE, "Cannot upgrade jmsra!", e);
        }
    }

    /**
     * Loads RA configuration for MQ Resource adapter.
     *
     * @throws ConnectorRuntimeException in case of an exception.
     */
    @Override
    protected void loadRAConfiguration() throws ConnectorRuntimeException{
        if (connectorRuntime.isServer()) {
            // Check whether MQ has started up or not.
            try {
                setLifecycleProperties();
            } catch (Exception e) {
                throw new ConnectorRuntimeException(e.getMessage(), e);
            }

            setMdbContainerProperties();
            setJmsServiceProperties(null);
            setClusterRABeanProperties();
            setAvailabilityProperties();
        } else {
            setAppClientRABeanProperties();
        }
        super.loadRAConfiguration();
        postRAConfiguration();
    }

    @Override
    public void destroy() {
        try {
            JmsService jmsService = getJmsService();
            if (connectorRuntime.isServer() && grizzlyListenerInit && jmsService != null
                    && EMBEDDED.equalsIgnoreCase(jmsService.getType())) {
                GrizzlyService grizzlyService = null;
                try {
                    grizzlyService = Globals.get(GrizzlyService.class);
                } catch (MultiException rle) {
                    // if GrizzlyService was shut down already, skip removing the proxy.
                }
                if (grizzlyService != null) {
                    synchronized (grizzlyListeners) {
                        if (!grizzlyListeners.isEmpty()) {
                            String[] listeners = grizzlyListeners.toArray(new String[grizzlyListeners.size()]);
                            for (String listenerName : listeners) {
                                try {
                                    grizzlyService.removeNetworkProxy(listenerName);
                                    grizzlyListeners.remove(listenerName);
                                } catch (Exception e) {
                                    LogHelper.log(LOG, WARNING,
                                            JMSLoggerInfo.SHUTDOWN_FAIL_GRIZZLY, e, listenerName);
                                }
                            }
                        }
                    }
                }
                grizzlyListenerInit = false;
            }
        } catch (Throwable th) {
            // Destroy should not escalate here, there's nothing much we can do,
            // just let parents destroy too.
            LogHelper.log(LOG, SEVERE, JMSLoggerInfo.SHUTDOWN_FAIL_JMSRA, th);
        }
        super.destroy();
    }

    public Set<String> getGrizzlyListeners() {
        return grizzlyListeners;
    }

    /**
     * Start Grizzly based JMS lazy listener, which is going to initialize
     * JMS container on first request.
     */
    public void initializeLazyListener(JmsService jmsService) {
        if (jmsService != null) {
            if (EMBEDDED.equalsIgnoreCase(jmsService.getType()) && !grizzlyListenerInit) {
                GrizzlyService grizzlyService = Globals.get(GrizzlyService.class);
                if (grizzlyService != null) {
                    List<JmsHost> jmsHosts = jmsService.getJmsHost();
                    for (JmsHost oneHost : jmsHosts) {
                        if (Boolean.parseBoolean(oneHost.getLazyInit()) && !doBind) {
                            String jmsHost = null;
                            if (oneHost.getHost() != null && "localhost".equals(oneHost.getHost())) {
                                jmsHost = "0.0.0.0";
                            } else {
                                jmsHost = oneHost.getHost();
                            }
                            NetworkListener dummy = new DummyNetworkListener();
                            dummy.setPort(oneHost.getPort());
                            dummy.setAddress(jmsHost);
                            dummy.setType("proxy");
                            dummy.setProtocol(JMS_SERVICE);
                            dummy.setTransport("tcp");
                            String name = GRIZZLY_PROXY_PREFIX + oneHost.getName();
                            dummy.setName(name);
                            synchronized (grizzlyListeners) {
                                grizzlyService.createNetworkProxy(dummy);
                                grizzlyListeners.add(name);
                            }
                            grizzlyListenerInit = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void startResourceAdapter(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        try {
            if (this.moduleName_.equals(ConnectorConstants.DEFAULT_JMS_ADAPTER)) {
                if (connectorRuntime.isServer()) {
                    Domain domain = Globals.get(Domain.class);
                    ServerContext serverContext = Globals.get(ServerContext.class);
                    Server server = domain.getServerNamed(serverContext.getInstanceName());
                    JmsService jmsService = server.getConfig().getExtensionByType(JmsService.class);
                    initializeLazyListener(jmsService);
                }
                PrivilegedExceptionAction<Void> action = () -> {
                    resourceadapter_.start(bootStrapContextImpl);
                    return null;
                };
                AccessController.doPrivileged(action);
            } else {
                resourceadapter_.start(bootStrapContextImpl);
            }
        } catch (PrivilegedActionException ex) {
            throw new ResourceAdapterInternalException(ex);
        }
    }

    /**
     * This is a HACK to remove the connection URL
     * in the case of PE LOCAL/EMBEDDED before setting the properties
     * to the RA. If this was not done, MQ RA incorrectly assumed
     * that the passed in connection URL is one additional
     * URL, apart from the default URL derived from brokerhost:brokerport
     * and reported a PE connection url limitation.
     */
    @Override
    protected Set mergeRAConfiguration(ResourceAdapterConfig raConfig, List<Property> raConfigProps) {
        // private void hackMergedProps(Set mergedProps) {
        if (!(connectorRuntime.isServer())) {
            return super.mergeRAConfiguration(raConfig, raConfigProps);
        }
        Set<ConnectorConfigProperty> mergedProps = super.mergeRAConfiguration(raConfig, raConfigProps);
        String brokerType = null;

        for (ConnectorConfigProperty element : mergedProps) {
            if (element.getName().equals(ActiveJmsResourceAdapter.BROKERTYPE)) {
                brokerType = element.getValue();
            }
        }
        boolean cluster = false;
        try {
            cluster = isClustered();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // hack is required only for nonclustered nonremote brokers.
        if (!cluster && brokerType != null) {
            if (brokerType.equals(ActiveJmsResourceAdapter.LOCAL)
                || brokerType.equals(ActiveJmsResourceAdapter.EMBEDDED)
                || brokerType.equals(ActiveJmsResourceAdapter.DIRECT)) {
                for (Iterator<ConnectorConfigProperty> iter = mergedProps.iterator(); iter.hasNext();) {
                    ConnectorConfigProperty element = iter.next();
                    if (element.getName().equals(ActiveJmsResourceAdapter.CONNECTION_URL)) {
                        iter.remove();
                    }
                }
            }
        }
        return mergedProps;
    }

    //Overriding ActiveResourceAdapterImpl.setup() as a work around for
    //this condition - connectionDefs_.length != 1
    //Need to remove this once the original problem is fixed
    @Override
    public void setup() throws ConnectorRuntimeException {
        //TODO NEED TO REMOVE ONCE THE ActiveResourceAdapterImpl.setup() is fixed
        if (connectionDefs_ == null) {
            throw new ConnectorRuntimeException("No Connection Defs defined in the RA.xml");
        }
        if (isServer() && !isSystemRar(moduleName_)) {
            createAllConnectorResources();
        }
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "Completed Active Resource adapter setup", moduleName_);
        }
    }

    /**
     * Set Availability related properties
     * If JMS availability true set availability properties
     * read configured pool information and set.
     */
    private void setAvailabilityProperties() throws ConnectorRuntimeException {
        if(!isClustered()) {
            return;
        }
        try {
            Domain domain = Globals.get(Domain.class);
            ServerContext serverContext = Globals.get(ServerContext.class);
            Server server = domain.getServerNamed(serverContext.getInstanceName());

            JmsService jmsService = server.getConfig().getExtensionByType(JmsService.class);
            if (jmsService.getType().equals(REMOTE)) {
                // If REMOTE, the broker cluster instances already have
                // been configured with the right properties.
                return;
            }

            AvailabilityService as = server.getConfig().getAvailabilityService();
            if (as == null) {
                LOG.log(CONFIG, "Availability Service is null. Not setting AvailabilityProperties.");
                return;
            }

            boolean useMasterBroker = true;
            if (as.getExtensionByType(JmsAvailability.class) != null
                && !MASTERBROKER.equalsIgnoreCase(as.getExtensionByType(JmsAvailability.class).getConfigStoreType())) {
                useMasterBroker = false;
            }

            // jmsService.getUseMasterBroker() != null ?
            // Boolean.valueOf(jmsService.getUseMasterBroker()) :true;
            boolean isJmsAvailabilityEnabled = this.isJMSAvailabilityOn(as);

            LOG.log(FINEST, "Setting AvailabilityProperties ...");
            if (!useMasterBroker || isJmsAvailabilityEnabled) {
                // For conventional cluster of peer brokers and Enhanced Broker Cluster.
                ConnectorDescriptor cd = getDescriptor();
                String clusterName = getMQClusterName();
                ConnectorConfigProperty envProp1 = new ConnectorConfigProperty(
                    CLUSTERID, clusterName, "Cluster Id", String.class.getName());
                setProperty(cd, envProp1);

                if (brokerInstanceName == null) {
                    brokerInstanceName = getBrokerInstanceName(jmsService);
                }
                ConnectorConfigProperty envProp2 = new ConnectorConfigProperty(
                    BROKERID, brokerInstanceName, "Broker Id", String.class.getName());
                setProperty(cd, envProp2);

                // Only if JMS availability is true - Enhanced Broker Cluster only.
                if (isJmsAvailabilityEnabled) {
                    // Set HARequired as true - irrespective of whether it is REMOTE or
                    // LOCAL
                    ConnectorConfigProperty envProp3 = new ConnectorConfigProperty(
                        HAREQUIRED, "true", "HA Required", String.class.getName());
                    setProperty(cd, envProp3);

                    /*
                     * The broker has a property to control whether
                     * it starts in HA mode or not and that's represented on
                     * the RA by BrokerEnableHA.
                     * On the MQ Client connection side it is HARequired -
                     * this does not control the broker, it just is a client
                     * side requirement.
                     * So for AS EE, if BrokerType is LOCAL or EMBEDDED,
                     * and AS HA is enabled for JMS then both these must be
                     * set to true.
                     */
                    ConnectorConfigProperty envProp4 = new ConnectorConfigProperty(
                        BROKERENABLEHA, "true", "BrokerEnableHA flag", "java.lang.Boolean");
                    setProperty(cd, envProp4);

                    String nodeHostName = domain.getNodeNamed(server.getNodeRef()).getNodeHost();
                    if (nodeHostName != null) {
                        ConnectorConfigProperty envProp5 = new ConnectorConfigProperty(
                            BROKERBINDADDRESS, nodeHostName, "Broker Bind Address", String.class.getName());
                        setProperty(cd, envProp5);
                    }
                    loadDBProperties(as.getExtensionByType(JmsAvailability.class), ClusterMode.ENHANCED);
                } else {
                    // Conventional cluster of peer brokers
                    JmsAvailability jmsAvailability = as.getExtensionByType(JmsAvailability.class);
                    if ("jdbc".equals(jmsAvailability.getMessageStoreType())) {
                        loadDBProperties(jmsAvailability, ClusterMode.ENHANCED);
                    }
                    loadDBProperties(jmsAvailability, ClusterMode.CONVENTIONAL_OF_PEER_BROKERS);
                }
            } else {
                // Conventional cluster with master broker.
                if ("jdbc".equals(as.getExtensionByType(JmsAvailability.class).getMessageStoreType())) {
                    loadDBProperties(as.getExtensionByType(JmsAvailability.class),
                        ClusterMode.CONVENTIONAL_WITH_MASTER_BROKER);
                }
            }
        } catch (Exception e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(e.getMessage());
            throw (ConnectorRuntimeException)crex.initCause(e);
        }
    }

    private void loadDBProperties(JmsAvailability jmsAvailability, ClusterMode clusterMode) {
        final String prefix;
        if (ClusterMode.CONVENTIONAL_WITH_MASTER_BROKER == clusterMode) {
            prefix = ENHANCED_CLUSTER_DB_PREFIX;
        } else if (ClusterMode.CONVENTIONAL_OF_PEER_BROKERS == clusterMode) {
            prefix = CONVENTIONAL_CLUSTER__OF_PEER_BROKERS_DB_PREFIX;
        } else if (ClusterMode.ENHANCED == clusterMode) {
            prefix = ENHANCED_CLUSTER_DB_PREFIX;
        } else {
            LOG.log(FINE, () -> "Unknown cluster mode: " + clusterMode.name() + ", imq DB properties are not set.");
            return;
        }
        if (dbProps == null) {
            dbProps = new Properties();
        }
        dbProps.setProperty("imq.cluster.clusterid", getMQClusterName());
        dbProps.setProperty("imq.persist.store", jmsAvailability.getMessageStoreType());
        if (ClusterMode.CONVENTIONAL_WITH_MASTER_BROKER == clusterMode) {
            dbProps.setProperty("imq.cluster.nomasterbroker", "false");
        } else {
            dbProps.setProperty("imq.cluster.nomasterbroker", "true");
        }
        if (Boolean.parseBoolean(jmsAvailability.getAvailabilityEnabled())
            || "jdbc".equals(jmsAvailability.getMessageStoreType())) {
            dbProps.setProperty("imq.brokerid", getBrokerInstanceName(getJmsService()));
        }

        String dbVendor = jmsAvailability.getDbVendor();
        String dbuser = jmsAvailability.getDbUsername();
        String dbPassword = jmsAvailability.getDbPassword();
        String dbJdbcUrl = jmsAvailability.getDbUrl();

        dbProps.setProperty(prefix + "dbVendor", dbVendor);

        String fullprefix = prefix + dbVendor + ".";
        if (dbuser != null) {
            dbProps.setProperty(fullprefix + "user", dbuser);
        }
        if (dbPassword != null) {
            dbProps.setProperty(fullprefix + "password", dbPassword);
        }
        List<Property> dbprops = jmsAvailability.getProperty();

        String propertyPrefix = fullprefix + "property.";

        if (dbJdbcUrl != null) {
            if ("derby".equals(dbVendor)) {
                dbProps.setProperty(fullprefix + "opendburl", dbJdbcUrl);
            } else {
                dbProps.setProperty(propertyPrefix + "url", dbJdbcUrl);
            }
        }

        for (Property prop : dbprops) {
            String key = prop.getName();
            String value = prop.getValue();
            // don't set a prefix if the property name is already prefixed with "imq."
            if (key.startsWith("imq.")) {
                dbProps.setProperty(key, value);
            } else {
                dbProps.setProperty(propertyPrefix + key, value);
            }
        }
    }

    /**
     * Method to perform any post RA configuration action by derivative subclasses.
     * For example, this method is used by <code>ActiveJMSResourceAdapter</code>
     * to set unsupported javabean property types on its RA JavaBean runtime
     * instance.
     * @throws ConnectorRuntimeException
     */
    protected void postRAConfiguration() throws ConnectorRuntimeException {
        //Set all non-supported javabean property types in the JavaBean
        try {
            if (dbProps == null) {
                dbProps = new Properties();
            }
            dbProps.setProperty("imq.cluster.dynamicChangeMasterBrokerEnabled", "true");

            Method mthds = this.resourceadapter_.getClass().getMethod("setBrokerProps", Properties.class);
            LOG.log(FINE, () -> "Setting property: " + DB_HADB_PROPS + "=" + dbProps);
            mthds.invoke(this.resourceadapter_, new Object[]{dbProps});
        } catch (Exception e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(e.getMessage());
            throw (ConnectorRuntimeException) crex.initCause(e);
        }
    }


    private boolean isJMSAvailabilityOn(AvailabilityService as) {
        if (as == null) {
            return false;
        }
        JmsAvailability ja = as.getExtensionByType(JmsAvailability.class);
        boolean jmsAvailability = false;
        /*
         * JMS Availability should be false if its not present in
         * domain.xml,
         */
        if (ja != null) {
            jmsAvailability = Boolean.parseBoolean(ja.getAvailabilityEnabled());
        }
        LOG.log(FINE, "JMS availability: {0}", jmsAvailability);
        return jmsAvailability;
    }

    /**
     * Set MQ4.0 RA lifecycle properties
     */
    private void setLifecycleProperties() throws Exception, ConnectorRuntimeException {
        //If PE:
        //EMBEDDED/LOCAL goto jms-service, get defaultjmshost info and set
        //accordingly
        //if EE:
        //EMBEDDED/LOCAL get this instance and cluster name, search for a
        //jms-host wth this this name in jms-service gets its proeprties
        //and set
        //@siva As of now use default JMS host. As soon as changes for modifying EE
        //cluster to LOCAL is brought in, change this to use system properties
        //for EE to get port, host, adminusername, adminpassword.
        //JmsService jmsService = ServerBeansFactory.getJmsServiceBean(ctx);
        String defaultJmsHost = getJmsService().getDefaultJmsHost();
        LOG.log(FINE, "Default JMS Host: {0}", defaultJmsHost);

        JmsHost jmsHost = getJmsHost();

        if (jmsHost != null) {//todo: && jmsHost.isEnabled()) {
            JavaConfig javaConfig = Globals.get(JavaConfig.class);
            String java_home = javaConfig.getJavaHome();

            //Get broker type from JMS Service.
            // String brokerType = jmsService.getType();
            /*
             * XXX: adjust the brokertype for the new DIRECT mode in 4.1
             * uncomment the line below once we have an MQ integration
             * that has DIRECT mode support
             */
            String brokerType = adjustForDirectMode(getJmsService().getType());

            String brokerPort = jmsHost.getPort();
            brkrPort = brokerPort;
            String adminUserName = jmsHost.getAdminUserName();
            String adminPassword = JmsRaUtil.getUnAliasedPwd(jmsHost.getAdminPassword());
            List<Property> jmsHostProps= getJmsService().getProperty();

            String username = null;
            String password = null;
            if (jmsHostProps != null) {
                for (Property jmsProp : jmsHostProps) {
                    String propName = jmsProp.getName();
                    String propValue = jmsProp.getValue();
                    if ("user-name".equals(propName)) {
                        username = propValue;
                    } else if ("password".equals(propName)) {
                        password = propValue;
                    }
                    // Add more properties as and when you want.
                }
           }

            LOG.log(FINE, "Broker UserName={0}", username);
            String brokerVarDir = getMQVarDir();
            createMQVarDirectoryIfNecessary(brokerVarDir);

            String tmpString = getJmsService().getStartArgs();
            if (tmpString == null) {
                tmpString = "";
            }
            String brokerArgs = tmpString;

            // Extract the information from the optional properties.
            List<Property> jmsProperties = getJmsService().getProperty();
            List<Property> jmsHostProperties = jmsHost.getProperty();
            Properties jmsServiceProp = listToProperties(jmsProperties);
            Properties jmsHostProp = listToProperties(jmsHostProperties);

            jmsServiceProp.putAll(jmsHostProp);
            if (!jmsServiceProp.isEmpty()) {
                if (dbProps == null) {
                    dbProps = new Properties();
                }
                dbProps.putAll(jmsServiceProp);
            }
            String brokerHomeDir = getBrokerHomeDir();
            String brokerLibDir = getBrokerLibDir();
            if (brokerInstanceName == null) {
                brokerInstanceName = getBrokerInstanceName(getJmsService());
            }

            long brokerTimeOut = getBrokerTimeOut(getJmsService());

            ConnectorDescriptor cd = getDescriptor();
            ConnectorConfigProperty  envProp1 = new ConnectorConfigProperty  (
                    BROKERTYPE, brokerType, "Broker Type", String.class.getName());
            setProperty(cd, envProp1);
            ConnectorConfigProperty  envProp2 = new ConnectorConfigProperty  (
                    BROKERINSTANCENAME, brokerInstanceName ,
                    "Broker Instance Name", String.class.getName());
            setProperty(cd, envProp2);
            ConnectorConfigProperty  envProp3 = new ConnectorConfigProperty  (
                    BROKERPORT , brokerPort ,
                    "Broker Port", "java.lang.Integer");
            setProperty(cd, envProp3);
            ConnectorConfigProperty  envProp4 = new ConnectorConfigProperty  (
                    BROKERARGS , brokerArgs ,
                    "Broker Args", String.class.getName());
            setProperty(cd, envProp4);
            ConnectorConfigProperty  envProp5 = new ConnectorConfigProperty  (
                    BROKERHOMEDIR , brokerHomeDir ,
                    "Broker Home Dir", String.class.getName());
            setProperty(cd, envProp5);
            ConnectorConfigProperty  envProp14 = new ConnectorConfigProperty  (
                    BROKERLIBDIR , brokerLibDir ,
                    "Broker Lib Dir", String.class.getName());
            setProperty(cd, envProp14);
            ConnectorConfigProperty  envProp6 = new ConnectorConfigProperty  (
                    BROKERJAVADIR , java_home ,
                    "Broker Java Dir", String.class.getName());
                    setProperty(cd, envProp6);
            ConnectorConfigProperty  envProp7 = new ConnectorConfigProperty  (
                    BROKERVARDIR , brokerVarDir ,
                    "Broker Var Dir", String.class.getName());
            setProperty(cd, envProp7);
            ConnectorConfigProperty  envProp8 = new ConnectorConfigProperty  (
                    BROKERSTARTTIMEOUT , "" + brokerTimeOut ,
                    "Broker Start Timeout", "java.lang.Integer");
            setProperty(cd, envProp8);
            ConnectorConfigProperty  envProp9 = new ConnectorConfigProperty  (
                    ADMINUSERNAME , adminUserName,
                    "Broker admin username", String.class.getName());
            setProperty(cd, envProp9);
            ConnectorConfigProperty  envProp10 = new ConnectorConfigProperty  (
                    ADMINPASSWORD , adminPassword ,
                    "Broker admin password", String.class.getName());
            setProperty(cd, envProp10);
            ConnectorConfigProperty  envProp11 = new ConnectorConfigProperty  (
                    USERNAME , username,
                    "Broker username", String.class.getName());
            setProperty(cd, envProp11);
            ConnectorConfigProperty  envProp12 = new ConnectorConfigProperty  (
                    PASSWORD , password,
                    "Broker password", String.class.getName());
            setProperty(cd, envProp12);
        }
    }


    private Properties listToProperties(List<Property> props) {
        Properties properties = new Properties();
        if (props != null) {
            for (Property prop : props) {
                String key = prop.getName();
                String value = prop.getValue();

                properties.setProperty(key, value);
            }
        }

        return properties;
    }

    private String adjustForDirectMode(String brokerType) {
        if (!isClustered() && brokerType.equals(EMBEDDED)) {
            String revertToEmbedded = System.getProperty(REVERT_TO_EMBEDDED_PROPERTY);
            if ((revertToEmbedded != null) && (revertToEmbedded.equals("true"))){
                return EMBEDDED;
            }
            return DIRECT;
        }
        return brokerType;
    }

    private long getBrokerTimeOut(JmsService jmsService) {
        //@@remove
        long defaultTimeout = 30 * 1000; //30 seconds
        long timeout = defaultTimeout;

        String specifiedTimeOut = jmsService.getInitTimeoutInSeconds();
        if (specifiedTimeOut != null) {
            timeout = Integer.parseInt(specifiedTimeOut) * 1000L;
        }
        return timeout;
    }

    public static String getBrokerInstanceName(JmsService js) {
        ServerEnvironmentImpl serverenv = Globals.get(ServerEnvironmentImpl.class);
        Domain domain = Globals.get(Domain.class);
        String asInstance = serverenv.getInstanceName();
        String domainName = null;
        if (isClustered()) {
            Server server = domain.getServerNamed(asInstance);
            domainName = server.getCluster().getName();
        } else {
            domainName = serverenv.getDomainName();
        }
        String s = getBrokerInstanceName(domainName, asInstance, js);
        LOG.log(FINE, "Got broker Instancename as {0}", s);
        String converted = convertStringToValidMQIdentifier(s);
        LOG.log(FINE, "converted instance name {0}", converted);
        return converted;
    }

    @Override
    public boolean handles(ConnectorDescriptor cd, String moduleName) {
        return ConnectorsUtil.isJMSRA(moduleName);
    }

    @Override
    public void validateActivationSpec(ActivationSpec spec) {
        boolean validate =  "true".equals(System.getProperty("validate.jms.ra"));
        if (validate) {
            try {
                spec.validate();
            } catch (Exception ex) {
                LogHelper.log(LOG, SEVERE, JMSLoggerInfo.ENDPOINT_VALIDATE_FAILED, ex);
            }
        }
    }

     /**
     * Computes the instance name for the MQ broker.
     */
     private static String getBrokerInstanceName(String asDomain, String asInstance, JmsService js) {
        List<Property> jmsProperties = js.getProperty();
        String instanceName = null;
        String suffix = null;

        if (jmsProperties != null) {
            for (Property p : jmsProperties) {
                String name = p.getName();
                if (name.equals("instance-name")) {
                    instanceName = p.getValue();
                }
                if (name.equals("instance-name-suffix")) {
                    suffix = p.getValue();
                }
                if (name.equals("append-version") && Boolean.parseBoolean(p.getValue())) {
                    suffix = Version.getMajorVersion() + "_" + Version.getMinorVersion();
                }
            }
        }

        if (instanceName != null) {
            return instanceName;
        }

        if (asInstance.equals(DEFAULT_SERVER)) {
            instanceName = DEFAULT_MQ_INSTANCE;
        } else {
            instanceName = asDomain + "_" + asInstance;
        }

        if (suffix != null) {
            instanceName = instanceName + "_" + suffix;
        }

        return instanceName;
    }


    private void createMQVarDirectoryIfNecessary(String mqInstanceDir) {
        // If the directory doesnt exist, create it.
        // It is necessary for windows.
        File imqVarDir = new File(mqInstanceDir);
        if (imqVarDir.exists() && imqVarDir.isDirectory()) {
            LOG.log(FINEST, "IMQ VAR directory already exists: {0}", imqVarDir);
            return;
        }
        if (!imqVarDir.mkdirs()) {
            LOG.log(WARNING, "Failed to create dir: {0}", imqVarDir);
        }
    }


    private String getMQVarDir() {
        return new File(getServerEnvironment().getInstanceRoot(), MQ_DIR_NAME).getAbsolutePath();
    }


    private String getBrokerLibDir() {
        String brokerLibDir = System.getProperty(SystemPropertyConstants.IMQ_LIB_PROPERTY);
        LOG.log(FINE, "Broker lib dir from system property {0}", brokerLibDir);
        return brokerLibDir;
    }

    private String getBrokerHomeDir() {
        // If the property was not specified, then look for the
        // imqRoot as defined by the com.sun.aas.imqRoot property
        String brokerBinDir = System.getProperty(SystemPropertyConstants.IMQ_BIN_PROPERTY);
        LOG.log(FINEST, "Broker bin dir from system property {0}", brokerBinDir);

        // Finally if all else fails (though this should never happen)
        // look for IMQ relative to the installation directory
        final String brokerHomeDir;
        if (brokerBinDir == null) {
            brokerHomeDir = new File(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY)).toPath()
                .resolve(Path.of("..", "mq")).normalize().toFile().getAbsolutePath();
        } else {
            brokerHomeDir = new File(brokerBinDir).getParentFile().getAbsolutePath();
        }

        LOG.log(INFO, "Broker Home Directory: {0}", brokerHomeDir);
        return brokerHomeDir;

    }


    /**
     * Sets the SE/EE specific MQ-RA bean properties
     * @throws ConnectorRuntimeException
     */
    private void setClusterRABeanProperties() throws ConnectorRuntimeException {
        ConnectorDescriptor cd = super.getDescriptor();
        try {
            if (!isClustered()) {
                LOG.log(FINE, "Instance not Clustered and hence not setting groupname");
                return;
            }
            JmsService jmsService = Globals.get(JmsService.class);
            String val = getGroupName();
            ConnectorConfigProperty envProp = new ConnectorConfigProperty(
                GROUPNAME, val, "Group Name", String.class.getName());
            setProperty(cd, envProp);
            LOG.log(FINE, "CLUSTERED instance - setting groupname as {0}", val);

            boolean inClusteredContainer = false;
            if (EMBEDDED.equals(jmsService.getType()) || LOCAL.equals(jmsService.getType())) {
                inClusteredContainer = true;
            }

            ConnectorConfigProperty envProp1 = new ConnectorConfigProperty(CLUSTERCONTAINER,
                Boolean.toString(inClusteredContainer), "Cluster container flag", "java.lang.Boolean");
            setProperty(cd, envProp1);
            LOG.log(FINE, "CLUSTERED instance - setting inclusteredcontainer as {0}", inClusteredContainer);
            if (REMOTE.equals(jmsService.getType())) {
                // Do not set master broker for remote broker.
                // The RA might ignore it if we set, but we have to be certain from our end.
                 return;
            }
            if (!isDBEnabled()) {
                String masterbrkr = getMasterBroker();
                ConnectorConfigProperty envProp2 = new ConnectorConfigProperty(
                    MASTERBROKER, masterbrkr, "Master  Broker", String.class.getName());
                setProperty(cd, envProp2);
                LOG.log(FINE, "MASTERBROKER - setting master broker val {0}", masterbrkr);
            }
        } catch (Exception e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(e.getMessage());
            throw (ConnectorRuntimeException) crex.initCause(e);
        }
    }

    private boolean isDBEnabled(){
        ServerContext serverContext = Globals.get(ServerContext.class);
        Server server = domain.getServerNamed(serverContext.getInstanceName());
        AvailabilityService as = server.getConfig().getAvailabilityService();
        if (as != null) {
            JmsAvailability jmsAvailability = as.getExtensionByType(JmsAvailability.class);
            if (jmsAvailability.getAvailabilityEnabled() != null
                && Boolean.parseBoolean(jmsAvailability.getAvailabilityEnabled())) {
                return true;
            } else if (jmsAvailability.getConfigStoreType() != null
                && !"MASTERBROKER".equalsIgnoreCase(jmsAvailability.getConfigStoreType())) {
                return true;
            }
        }

        return false;
    }


    /**
     * Sets the SE/EE specific MQ-RA bean properties
     * @throws ConnectorRuntimeException
     */
    private void setAppClientRABeanProperties() throws ConnectorRuntimeException {
        LOG.log(FINE, "In Appclient container!!!");
        ConnectorDescriptor cd = super.getDescriptor();
        ConnectorConfigProperty envProp1 = new ConnectorConfigProperty(
            BROKERTYPE, REMOTE, "Broker Type", String.class.getName());
        setProperty(cd, envProp1);

        ConnectorConfigProperty envProp2 = new ConnectorConfigProperty(
            GROUPNAME, "", "Group Name", String.class.getName());
        cd.removeConfigProperty(envProp2);
        ConnectorConfigProperty envProp3 = new ConnectorConfigProperty(
            CLUSTERCONTAINER, "false", "Cluster flag", "java.lang.Boolean");
        setProperty(cd, envProp3);
    }

    Domain domain = Globals.get(Domain.class);

    private static boolean isClustered()  {
        Domain domain = Globals.get(Domain.class);
        Clusters clusters = domain.getClusters();
        if (clusters == null) {
            return false;
        }

        List<Cluster> clusterList = clusters.getCluster();
        ServerContext serverctx = Globals.get(ServerContext.class);
        return JmsRaUtil.isClustered(clusterList, serverctx.getInstanceName());
    }

    private String getGroupName() throws Exception{
        return getDomainName() + SEPARATOR + getClusterName();
    }

    private String getClusterName() {

        ServerContext serverctx = Globals.get(ServerContext.class);
        String instanceName = serverctx.getInstanceName();

        Domain domain = Globals.get(Domain.class);
        Server server = domain.getServerNamed(instanceName);

        return server.getCluster() == null ? null : server.getCluster().getName();
    }

    /**
     * Generates an Name for the MQ Cluster associated with the
     * application server cluster.
     */
    private String getMQClusterName() {
        return convertStringToValidMQIdentifier(getClusterName()) + "_MQ";
    }

    /**
     * Master Broker name in the cluster, assumes the first broker in
     * in the list is the master broker , and this consistency has to
     * be maintained in all the instances.
     */
     private String getMasterBroker() throws Exception {
         return urlList.getMasterBroker(getClusterName());
     }

    //All Names passed into MQ needs to be valid Java Identifiers
    //so as of now replacing all characters that are not valid
    //java identifier components with '_'
    private static String convertStringToValidMQIdentifier(String s) {
        if (s == null) {
            return "";
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            if(Character.isLetterOrDigit(s.charAt(i))){
                            //|| s.charAt(i) == '_'){
                buf.append(s.charAt(i));
            }
        }
        return buf.toString();
    }

    private String getDomainName() throws Exception {
        return domain.getName();
    }

    /**
     * Recreates the ResourceAdapter using new values from JmsSerice.
     *
     * @param js JmsService element of the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void reloadRA(JmsService js) throws ConnectorRuntimeException {
        setMdbContainerProperties();
        setJmsServiceProperties(js);

        super.loadRAConfiguration();
        rebindDescriptor();
    }

    /**
     * Adds the JmsHost to the MQAddressList of the resource adapter.
     *
     * @param host JmsHost element in the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void addJmsHost(JmsHost host) throws ConnectorRuntimeException {
        urlList.addMQUrl(host);
        setAddressList();
    }

    /**
     * Removes the JmsHost from the MQAddressList of the resource adapter.
     *
     * @param host JmsHost element in the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void deleteJmsHost(JmsHost host) throws ConnectorRuntimeException {
        urlList.removeMQUrl(host);
        setAddressList();
    }

    protected JmsHost getJmsHost() {
        String defaultJmsHost = getJmsService().getDefaultJmsHost();
        if (defaultJmsHost == null || defaultJmsHost.isEmpty()) {
            return Globals.get(JmsHost.class);
        }

        List jmsHostsList = getJmsService().getJmsHost();
        if (jmsHostsList == null || jmsHostsList.isEmpty()) {
            return Globals.get(JmsHost.class);
        }

        JmsHost jmsHost = null;
        for (Object element : jmsHostsList) {
            JmsHost tmpJmsHost = (JmsHost) element;
            if (tmpJmsHost != null && tmpJmsHost.getName().equals(defaultJmsHost)) {
                jmsHost = tmpJmsHost;
                break;
            }
        }
        if (jmsHost == null) {
            jmsHost = (JmsHost) jmsHostsList.get(0);
        }
        return jmsHost;
    }

    /**
     * Updates the JmsHost information in the MQAddressList of the resource adapter.
     *
     * @param host JmsHost element in the domain.xml
     * @throws ConnectorRuntimeException in case of any backend error.
     */
    public void updateJmsHost(JmsHost host) throws ConnectorRuntimeException {
        urlList.updateMQUrl(host);
        setAddressList();
    }

    public boolean getDoBind() {
        return doBind;
    }

    private void setMdbContainerProperties() throws ConnectorRuntimeException {
        JmsRaUtil raUtil = new JmsRaUtil(null);

        ConnectorDescriptor cd = super.getDescriptor();
        raUtil.setMdbContainerProperties();

        String val = "" + MdbContainerProps.getReconnectEnabled();
        ConnectorConfigProperty envProp2 = new ConnectorConfigProperty(RECONNECTENABLED, val, val, "java.lang.Boolean");
        setProperty(cd, envProp2);

        val = "" + MdbContainerProps.getReconnectDelay();
        ConnectorConfigProperty envProp3 = new ConnectorConfigProperty(RECONNECTINTERVAL, val, val, "java.lang.Integer");
        setProperty(cd, envProp3);

        val = "" + MdbContainerProps.getReconnectMaxRetries();
        ConnectorConfigProperty envProp4 = new ConnectorConfigProperty(RECONNECTATTEMPTS, val, val, "java.lang.Integer");
        setProperty(cd, envProp4);

        String integrationMode = getJmsService().getType();
        boolean lazyInit = Boolean.parseBoolean(getJmsHost().getLazyInit());
        val = "true";
        if (EMBEDDED.equals(integrationMode) && lazyInit) {
            val = "false";
        }
        doBind = Boolean.parseBoolean(val);
        ConnectorConfigProperty envProp5 = new ConnectorConfigProperty(MQ_PORTMAPPER_BIND, val, val, "java.lang.Boolean");
        setProperty(cd, envProp5);


    // The above properties will be set in ConnectorDescriptor and
    // will be bound in JNDI. This will be available to appclient
    // and standalone client.
    }

    private void setAddressList() throws ConnectorRuntimeException {
        //@Siva: Enhance setting AddressList. [Ignore this machines jms-host while
        //constructing addresslist]
        try {
            JmsService jmsService = Globals.get(JmsService.class);
            setConnectionURL(jmsService, urlList);
        } catch (Exception e) {
            LOG.log(SEVERE, "setAddressList failed.", e);
        }
        super.loadRAConfiguration();
    }

    //This is a MQ workaround. In PE, when the broker type is
    //EMBEDDED or LOCAL, do not set the addresslist, else
    //MQ RA assumes that there are two URLs and fails (EE limitation).
    private void setConnectionURL(JmsService jmsService, MQAddressList urlList) {
        ConnectorDescriptor cd = super.getDescriptor();
        String val = urlList.toString();
        LOG.log(INFO, JMSLoggerInfo.JMS_CONNECTION_URL, val);
        ConnectorConfigProperty  envProp1 = new ConnectorConfigProperty(
           CONNECTION_URL, val, val, String.class.getName());
        setProperty(cd, envProp1);
    }


    private void setJmsServiceProperties(JmsService service) throws ConnectorRuntimeException {
        JmsRaUtil jmsraUtil = new JmsRaUtil(service);
        jmsraUtil.setupAddressList();
        urlList = jmsraUtil.getUrlList();
        addressList = urlList.toString();
        LOG.log(INFO, JMSLoggerInfo.ADDRESSLIST_JMSPROVIDER, addressList);
        ConnectorDescriptor cd = super.getDescriptor();
        setConnectionURL(service, urlList);

        String val = ""+jmsraUtil.getReconnectEnabled();
        ConnectorConfigProperty  envProp2 = new ConnectorConfigProperty(
            RECONNECTENABLED, val, val, "java.lang.Boolean");
        setProperty(cd, envProp2);

        //convert to milliseconds
        int newval = Integer.parseInt(jmsraUtil.getReconnectInterval()) * 1000;
        val = "" + newval;
        ConnectorConfigProperty envProp3 = new ConnectorConfigProperty(
            RECONNECTINTERVAL, val, val, "java.lang.Integer");
        setProperty(cd, envProp3);

        val = ""+jmsraUtil.getReconnectAttempts();
        ConnectorConfigProperty  envProp4 = new ConnectorConfigProperty(
            RECONNECTATTEMPTS, val, val, "java.lang.Integer");
        setProperty(cd, envProp4);

        val = ""+jmsraUtil.getAddressListBehaviour();
        ConnectorConfigProperty envProp5 = new ConnectorConfigProperty(
            ADRLIST_BEHAVIOUR, val, val, String.class.getName());
        setProperty(cd, envProp5);

        val = ""+jmsraUtil.getAddressListIterations();
        ConnectorConfigProperty envProp6 = new ConnectorConfigProperty(
            ADRLIST_ITERATIONS, val, val, "java.lang.Integer");
        setProperty(cd, envProp6);

        boolean useExternal = shouldUseExternalRmiRegistry(jmsraUtil);
        val = Boolean.toString(useExternal);
        ConnectorConfigProperty envProp7 = new ConnectorConfigProperty(
            USEEXTERNALRMIREGISTRY, val, val, "java.lang.Boolean");
        setProperty(cd, envProp7);

        LOG.log(FINE, "Start RMI registry set as {0}", val);
        //If MQ RA needs to use AS RMI Registry Port, then set
        //the RMI registry port, else MQ RA uses its default RMI
        //Registry port  [as of now 1099]
        String configuredRmiRegistryPort = null ;
        if (!useExternal) {
            configuredRmiRegistryPort = getRmiRegistryPort();
        } else {
            /*
             * We will be here if we are LOCAL or REMOTE, standalone
             * or clustered. We could set the Rmi registry port.
             * The RA should ignore the port if REMOTE and use it only
             * for LOCAL cases.
             */
            configuredRmiRegistryPort = getUniqueRmiRegistryPort();
        }
        val = configuredRmiRegistryPort;
        if (val == null) {
            LOG.log(WARNING, JMSLoggerInfo.INVALID_RMI_PORT);
        } else {
            ConnectorConfigProperty envProp8 = new ConnectorConfigProperty(
                RMIREGISTRYPORT, val, val, "java.lang.Integer");
            setProperty(cd, envProp8);
            LOG.log(FINE, "RMI registry port set as {0}", val);
        }
    }

    /**
     * Checks if AS RMI registry is started and available for use.
     */
    private boolean shouldUseExternalRmiRegistry (JmsRaUtil jmsraUtil) {
        boolean useExternalRmiRegistry = !isASRmiRegistryPortAvailable(jmsraUtil);
        //System.out.println("========== " + useExternalRmiRegistry);
        return useExternalRmiRegistry;
    }

    /**
     * This method should return a unique and unused port , so that
     * the broker can use this to start its Rmi registry.
     * Used only for LOCAL mode
     */
    private String getUniqueRmiRegistryPort() {
        String configuredport = System.getProperty(BROKER_RMI_PORT);
        int mqrmiport = DEFAULTRMIREGISTRYPORT;
        try {
            if (configuredport == null) {
                mqrmiport = Integer.parseInt(brkrPort) + BROKERRMIPORTOFFSET;
            } else {
                mqrmiport = Integer.parseInt(configuredport);
            }
        } catch (Exception e) {
            LOG.log(WARNING, "Invalid IMQ Broker port: {0}", configuredport);
        }
        return Integer.toString(mqrmiport);
    }

    /**
     * Get the AS RMI registry port for MQ RA to use.
     */
    private String getRmiRegistryPort() {
        if (MQRmiPort != null && !MQRmiPort.isBlank()){
            return MQRmiPort;
        }
        String configuredPort = null;
        try {
            configuredPort = getConfiguredRmiRegistryPort();
        } catch (Exception ex) {
            LOG.log(WARNING, JMSLoggerInfo.GET_RMIPORT_FAIL, ex);
        }
        if (configuredPort != null) {
            return configuredPort;
        }

        //Finally if DAS and configured port doesn't work, return DAS'
        //RMI registry port as a fallback option.

        if (isDAS()) {
            return DASRMIPORT;
        }
        return null;
    }

    private boolean isDAS() {
        return SystemPropertyConstants.DAS_SERVER_NAME.equals(getServerContext().getInstanceName());
    }

    private String getConfiguredRmiRegistryHost() throws Exception {
        String hostName = getJmxConnector().getAddress();
        if (hostName.isEmpty() || hostName.equals("0.0.0.0")) {
            try {
                hostName = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                hostName = "localhost";
            }
        } else if (hostName.contains(":") && !hostName.startsWith("[")) {
            return "[" + hostName + "]";
        }
        return hostName;
    }

    private String getConfiguredRmiRegistryPort() throws Exception {
        return getJmxConnector().getPort();
    }


    private JmxConnector getJmxConnector() throws Exception {
        List<JmxConnector> jmxConnectors = getAdminService().getJmxConnector();
        String sysJmsConnectorName = getAdminService().getSystemJmxConnectorName();
        if (jmxConnectors != null) {
            for (JmxConnector jmxConnector : jmxConnectors) {
                if (sysJmsConnectorName.equals(jmxConnector.getName())) {
                    return jmxConnector;
                }
            }
        }
        return null;
    }

    private boolean isASRmiRegistryPortAvailable(JmsRaUtil jmsraUtil) {
         if (LOG.isLoggable(FINE)) {
             LOG.log(FINE, "isASRmiRegistryPortAvailable - JMSService Type: " + jmsraUtil.getJMSServiceType());
        }
         //If JMSServiceType is LOCAL or REMOTE, then we need not ask the MQ RA to use the
         //AS RMI Registry. So the check below is not necessary.
         if (REMOTE.equals(jmsraUtil.getJMSServiceType()) || LOCAL.equals(jmsraUtil.getJMSServiceType())) {
             return false;
         }

        try {
            JmxConnector jmxConnector = getJmxConnector();
            if (!"true".equals(jmxConnector.getEnabled())) {
                return false;
            }

            if ("true".equals(jmxConnector.getSecurityEnabled())) {
                return false;
            }

            // Attempt to detect JMXStartupService for RMI registry
            LOG.fine("Detecting JMXStartupService...");
            JMXStartupService jmxservice = Globals.get(JMXStartupService.class);
            if (jmxservice == null) {
                return false;
            }

            jmxservice.waitUntilJMXConnectorStarted();

            LOG.fine("Found JMXStartupService");

            String name = "rmi://"+getConfiguredRmiRegistryHost() + ":" + getConfiguredRmiRegistryPort() + "/jmxrmi";
            LOG.log(FINE, "Attempting to list {0}", name);
            Naming.list(name);
            LOG.log(FINE, "List on {0} succeeded", name);

            //return configured port only if RMI registry is available
            return true;
        } catch (Exception e) {
            LOG.log(FINE, "Failed to detect JMX RMI Registry.", e);
            return false;
        }
    }

    private void setProperty(ConnectorDescriptor cd, ConnectorConfigProperty  envProp){
        cd.removeConfigProperty(envProp);
        cd.addConfigProperty(envProp);
    }


    private void rebindDescriptor() throws ConnectorRuntimeException {
        try {
            SimpleJndiName descriptorJNDIName = getReservePrefixedJNDINameForDescriptor(super.getModuleName());
            nm.publishObject(descriptorJNDIName, super.getDescriptor(), true);
        } catch (NamingException ne) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException (ne.getMessage());
            throw (ConnectorRuntimeException) cre.initCause(ne);
        }
    }


    /**
     * This is a temporay solution for obtaining all the MCFs
     * corresponding to a JMS RA pool, this is to facilitate the
     * recovery process where the XA resources of all RMs in the
     * broker cluster are required. Should be removed when a permanent
     * solutuion is available from the broker.
     *
     * @param cpr <code>ConnectorConnectionPool</code> object
     * @param loader Class Loader.
     */
    @Override
    public ManagedConnectionFactory[] createManagedConnectionFactories(
        com.sun.enterprise.connectors.ConnectorConnectionPool cpr, ClassLoader loader) {
        LOG.log(FINE, "RECOVERY : Entering createMCFS in AJMSRA");
        ArrayList<ManagedConnectionFactory> mcfs = new ArrayList<>();
        if (getAddressListCount() < 2) {
            mcfs.add(createManagedConnectionFactory(cpr, loader));
            LOG.log(FINE, "Brokers are not clustered, so doing normal recovery");
        } else {
            String addlist = null;
            Set<ConnectorConfigProperty> s = cpr.getConnectorDescriptorInfo().getMCFConfigProperties();
            for (ConnectorConfigProperty prop : s) {
                String propName = prop.getName();
                if (propName.equalsIgnoreCase("imqAddressList") || propName.equalsIgnoreCase("Addresslist")) {
                    addlist = prop.getValue();
                }
            }
            StringTokenizer tokenizer = null;
            if (addlist == null || (addlist.trim().equalsIgnoreCase("localhost"))) {
                tokenizer = new StringTokenizer(addressList, ",");
            } else {
                tokenizer = new StringTokenizer(addlist, ",");
            }
            LOG.log(FINE, "No of addresses found {0}", tokenizer.countTokens());
            while (tokenizer.hasMoreTokens()) {
                String brokerurl = tokenizer.nextToken();
                ManagedConnectionFactory mcf = super.createManagedConnectionFactory(cpr, loader);
                for (ConnectorConfigProperty prop : s) {
                    String propName = prop.getName();
                    String propValue = prop.getValue();
                    if (propName.startsWith("imq") && !"".equals(propValue)) {
                        try {
                            Method meth = mcf.getClass().getMethod(SETTER,
                                new Class[] {java.lang.String.class, java.lang.String.class});
                            if (propName.trim().equalsIgnoreCase("imqAddressList")) {
                                meth.invoke(mcf, new Object[] {prop.getName(), brokerurl});
                            } else {
                                meth.invoke(mcf, new Object[] {prop.getName(), prop.getValueObject(String.class)});
                            }
                        } catch (NoSuchMethodException ex) {
                            if (LOG.isLoggable(WARNING)) {
                                LOG.log(WARNING, JMSLoggerInfo.NO_SUCH_METHOD,
                                    new Object[] {SETTER, mcf.getClass().getName()});
                            }
                        } catch (Exception ex) {
                            LogHelper.log(LOG, SEVERE, JMSLoggerInfo.ERROR_EXECUTE_METHOD, ex, SETTER,
                                mcf.getClass().getName());
                        }
                    }
                }
                ConnectorConfigProperty addressProp3 = new ConnectorConfigProperty(ADDRESSLIST, brokerurl,
                    "Address List", String.class.getName());
                LOG.log(INFO, JMSLoggerInfo.ADDRESSLIST, brokerurl);

                HashSet<ConnectorConfigProperty> addressProp = new HashSet<>();
                addressProp.add(addressProp3);
                SetMethodAction<ConnectorConfigProperty> setMethodAction = new SetMethodAction<>(mcf, addressProp);
                try {
                    setMethodAction.run();
                } catch (Exception e) {
                    LOG.log(FINEST, "SetMethodAction failed for " + ADDRESSLIST + " value " + brokerurl, e);
                }
                mcfs.add(mcf);
            }
        }
        return mcfs.toArray(new ManagedConnectionFactory[mcfs.size()]);
    }


    @Override
    protected ManagedConnectionFactory instantiateMCF(final String mcfClass, final ClassLoader loader)
        throws Exception {
        if (moduleName_.equals(ConnectorConstants.DEFAULT_JMS_ADAPTER)) {
            PrivilegedExceptionAction<ManagedConnectionFactory> action = () -> instantiateManagedConnectionFactory(
                mcfClass, loader);
            return AccessController.doPrivileged(action);
        }
        return null;
    }


    private ManagedConnectionFactory instantiateManagedConnectionFactory(final String mcfClass,
        final ClassLoader loader) throws Exception {
        return super.instantiateMCF(mcfClass, loader);
    }

    /**
     * Creates ManagedConnection Factory instance. For any property that is
     * for supporting AS7 imq properties, resource adapter has a set method
     * setProperty(String,String). All as7 properties starts with "imq".
     * MQ Adapter supports this only for backward compatibility.
     *
     * @param cpr <code>ConnectorConnectionPool</code> object
     * @param loader Class Loader.
     */
    @Override
    public ManagedConnectionFactory createManagedConnectionFactory(
        com.sun.enterprise.connectors.ConnectorConnectionPool cpr, ClassLoader loader) {
        ManagedConnectionFactory mcf = super.createManagedConnectionFactory(cpr, loader);
        if (mcf != null) {
            Set<ConnectorConfigProperty> s = cpr.getConnectorDescriptorInfo().getMCFConfigProperties();
            for (ConnectorConfigProperty prop : s) {
                String propName = prop.getName();

                // If the property has started with imq, then it should go to
                // setProperty(String,String) method.
                if (propName.startsWith("imq") && !"".equals(prop.getValue())) {
                    try {
                        Method meth = mcf.getClass().getMethod(SETTER,
                            new Class[] {java.lang.String.class, java.lang.String.class});
                        meth.invoke(mcf, new Object[] {prop.getName(), prop.getValueObject(String.class)});
                    } catch (NoSuchMethodException ex) {
                        if (LOG.isLoggable(WARNING)) {
                            LOG.log(WARNING, JMSLoggerInfo.NO_SUCH_METHOD,
                                    new Object[] {SETTER, mcf.getClass().getName()});
                        }
                    } catch (Exception ex) {
                        LogHelper.log(LOG, SEVERE, JMSLoggerInfo.ERROR_EXECUTE_METHOD,
                                ex, SETTER, mcf.getClass().getName());
                    }
                }
            }

            // CR 6591307- Fix for properties getting overridden when setRA is called. Resetting the
            // properties if the RA is the JMS RA
            String moduleName = this.getModuleName();
            if (ConnectorAdminServiceUtils.isJMSRA(moduleName)) {
                try {
                    Set<ConnectorConfigProperty> configProperties = cpr.getConnectorDescriptorInfo()
                        .getMCFConfigProperties();
                    ConnectorConfigProperty[] array = configProperties.toArray(ConnectorConfigProperty[]::new);
                    for (ConnectorConfigProperty property : array) {
                        if (ActiveJmsResourceAdapter.ADDRESSLIST.equals(property.getName())) {
                            if (property.getValue() == null || "".equals(property.getValue())
                                || "localhost".equals(property.getValue())) {
                                LOG.log(FINE, "{0} default value: {1}", new Object[] {ADDRESSLIST, property.getValue()});
                                configProperties.remove(property);
                            }
                        }
                    }
                    SetMethodAction<ConnectorConfigProperty> setMethodAction = new SetMethodAction<>(mcf, configProperties);
                    setMethodAction.run();
                } catch (Exception e) {
                    LOG.log(FINE, "Resource adapter connection factory initialization failed. ", e);
                }
            }
        }
        return mcf;
    }


    /**
     * This is the most appropriate time (??) to update the runtime
     * info of a 1.3 MDB into 1.4 MDB.  <p>
     *
     * Assumptions : <p>
     * 0. Assume it is a 1.3 MDB if no RA mid is specified.
     * 1. Use the default system JMS resource adapter. <p>
     * 2. The ActivationSpec of the default JMS RA will provide the
     *    setDestination, setDestinationType, setSubscriptionName methods.
     * 3. The jndi-name of the 1.3 MDB is the value for the Destination
     *    property for the ActivationSpec.
     * 4. The ActivationSpec provides setter methods for the properties
     *    defined in the CF that corresponds to the mdb-connection-factory
     *    JNDI name.
     */
    @Override
    public void updateMDBRuntimeInfo(EjbMessageBeanDescriptor ejbDescriptor, BeanPoolDescriptor poolDescriptor)
        throws ConnectorRuntimeException {
        final SimpleJndiName jndiName = getJndiName(ejbDescriptor);
        String destinationLookup = ejbDescriptor.getActivationConfigValue("destinationLookup");
        String destinationProp = ejbDescriptor.getActivationConfigValue("destination");

        if (destinationLookup == null &&  destinationProp == null && (jndiName == null || jndiName.isEmpty())) {
            LOG.log(SEVERE, JMSLoggerInfo.ERROR_IN_DD);
            String msg = I18N.getString("ajra.error_in_dd");
            throw new ConnectorRuntimeException(msg);
        }

        String resourceAdapterMid = ConnectorConstants.DEFAULT_JMS_ADAPTER;

        ejbDescriptor.setResourceAdapterMid(resourceAdapterMid);

        if (destinationLookup == null && destinationProp == null) {
            String appName = ejbDescriptor.getApplication().getAppName();
            String moduleName = ConnectorsUtil.getModuleName(ejbDescriptor);

            JMSDestinationDefinitionDescriptor destination = getJMSDestinationFromDescriptor(jndiName, ejbDescriptor);
            String destName = null;
            if (isValidDestination(destination)) {
                destName = destination.getDestinationName();
            } else {
                destName = getPhysicalDestinationFromConfiguration(jndiName, appName, moduleName);
            }

            //1.3 jndi-name ==> 1.4 setDestination
            ejbDescriptor.putRuntimeActivationConfigProperty(new EnvironmentProperty(DESTINATION, destName, null));

            //1.3 (standard) destination-type == 1.4 setDestinationType
            //XXX Do we really need this???
            if (ejbDescriptor.getDestinationType() != null && !ejbDescriptor.getDestinationType().isEmpty()) {
                ejbDescriptor.putRuntimeActivationConfigProperty(
                    new EnvironmentProperty(DESTINATION_TYPE, ejbDescriptor.getDestinationType(), null));
                if (LOG.isLoggable(INFO)) {
                    LOG.log(INFO, JMSLoggerInfo.ENDPOINT_DEST_NAME,
                            new Object[]{ejbDescriptor.getDestinationType(), jndiName, ejbDescriptor.getName()});
                }
            } else if (isValidDestination(destination)
                && ConnectorConstants.DEFAULT_JMS_ADAPTER.equals(destination.getResourceAdapter())) {
                ejbDescriptor.putRuntimeActivationConfigProperty(
                    new EnvironmentProperty(DESTINATION_TYPE, destination.getInterfaceName(), null));
                if (LOG.isLoggable(INFO)) {
                    LOG.log(INFO, JMSLoggerInfo.ENDPOINT_DEST_NAME,
                            new Object[]{destination.getInterfaceName(), destination.getName(), ejbDescriptor.getName()});
                }
            } else {
                /*
                 * If destination type is not provided by the MDB component
                 * [typically used by EJB3.0 styled MDBs which create MDBs without
                 * a destination type activation-config property] and the MDB is for
                 * the default JMS RA, attempt to infer the destination type by trying
                 * to find out if there has been any JMS destination resource already
                 * defined for default JMS RA. This is a best attempt guess and if there
                 * are no JMS destination resources/admin-objects defined, AS would pass
                 * the properties as defined by the MDB.
                 */
                try {
                    AdminObjectResource aor = ResourcesUtil.createInstance().getResource(jndiName, appName, moduleName,
                        AdminObjectResource.class);
                    if (aor != null && ConnectorConstants.DEFAULT_JMS_ADAPTER.equals(aor.getResAdapter())) {
                        ejbDescriptor.putRuntimeActivationConfigProperty(
                            new EnvironmentProperty(DESTINATION_TYPE, aor.getResType(), null));
                        LOG.log(INFO, JMSLoggerInfo.ENDPOINT_DEST_NAME,
                            new Object[] {aor.getResType(), aor.getJndiName(), ejbDescriptor.getName()});
                    }
                } catch (Exception e) {
                    LOG.log(FINEST, "Failed to put runtime activation config property for default JMS adapter.", e);
                }
            }
        }


        //1.3 durable-subscription-name == 1.4 setSubscriptionName
        ejbDescriptor.putRuntimeActivationConfigProperty(
            new EnvironmentProperty(SUBSCRIPTION_NAME, ejbDescriptor.getDurableSubscriptionName(), null));

        String mdbCF;
        try {
            mdbCF = ejbDescriptor.getMdbConnectionFactoryJndiName();
        } catch (NullPointerException ne) {
            // Don't process connection factory.
            mdbCF = null;
        }

        if (mdbCF != null && !mdbCF.isEmpty()) {
            setValuesFromConfiguration(mdbCF, ejbDescriptor);
        }

        // a null object is passes as a PoolDescriptor during recovery.
        // See com/sun/enterprise/resource/ResourceInstaller

        if (poolDescriptor != null) {
            ejbDescriptor.putRuntimeActivationConfigProperty(
                new EnvironmentProperty(MAXPOOLSIZE, "" + poolDescriptor.getMaxPoolSize(), "", "java.lang.Integer"));
            ejbDescriptor.putRuntimeActivationConfigProperty(
                new EnvironmentProperty(MINPOOLSIZE, "" + poolDescriptor.getSteadyPoolSize(), "", "java.lang.Integer"));
            ejbDescriptor.putRuntimeActivationConfigProperty(new EnvironmentProperty(RESIZECOUNT,
                "" + poolDescriptor.getPoolResizeQuantity(), "", "java.lang.Integer"));
            ejbDescriptor.putRuntimeActivationConfigProperty(new EnvironmentProperty(RESIZETIMEOUT,
                "" + poolDescriptor.getPoolIdleTimeoutInSeconds(), "", "java.lang.Integer"));

            // The runtime activation config property holds all the
            // vendor specific properties, unfortunately the vendor
            // specific way of configuring exception count and the
            // standard way of configuring redelivery attempts is
            // through the same property REDELIVERYCOUNT . So, we first
            // check if the user (MDB assember) has configured a value
            // if not we set the one from mdb-container props
            // We have to check for both cases here because it has been
            // documented as "endpointExceptionRedeliveryAttempts" but
            // used in the code as "EndpointExceptionRedeliveryAttempts"
            if ((ejbDescriptor.getActivationConfigValue(REDELIVERYCOUNT) == null)
                && (ejbDescriptor.getActivationConfigValue(LOWERCASE_REDELIVERYCOUNT) == null)) {
                ejbDescriptor.putRuntimeActivationConfigProperty(
                    new EnvironmentProperty(
                        REDELIVERYCOUNT, "" + MdbContainerProps.getMaxRuntimeExceptions(), "", "java.lang.Integer"));
            }
        }

        //Set SE/EE specific MQ-RA ActivationSpec properties
        try {
            boolean clustered = isClustered();
            LOG.log(FINE, "Are we in a Clustered contained? {0}", clustered);
            if (clustered) {
                setClusterActivationSpecProperties(ejbDescriptor);
            }
        } catch (Exception e) {
            ConnectorRuntimeException crex = new ConnectorRuntimeException(e.getMessage());
            throw (ConnectorRuntimeException)crex.initCause(e);
        }
    }

    /**
     * Set SE/EE specific MQ-RA ActivationSpec properties
     * @param descriptor_
     * @throws Exception
     */
    private void setClusterActivationSpecProperties(EjbMessageBeanDescriptor descriptor_) throws Exception {
        // Set MDB Identifier in a clustered instance.
        String identifier = getMDBIdentifier(descriptor_);
        descriptor_.putRuntimeActivationConfigProperty(
            new EnvironmentProperty(MDBIDENTIFIER, identifier, "MDB Identifier", String.class.getName()));
        LOG.log(FINE, "CLUSTERED instance - setting MDB identifier as {0}", identifier);
    }


    /**
     * Gets the MDBIdentifier for the message bean endpoint
     * @param descriptor
     * @return
     * @throws Exception
     */
    private String getMDBIdentifier(EjbDescriptor descriptor) throws Exception {
        return getDomainName() + SEPARATOR + getClusterName() + SEPARATOR + descriptor.getUniqueId() ;
    }


    private String getPhysicalDestinationFromConfiguration(SimpleJndiName logicalDest, String appName, String moduleName)
        throws ConnectorRuntimeException {
        Property ep = null;
        try {
            AdminObjectResource res = ResourcesUtil.createInstance().getResource(logicalDest, appName, moduleName,
                AdminObjectResource.class);
            if (res == null) {
                String msg = I18N.getString("ajra.err_getting_dest", logicalDest);
                throw new ConnectorRuntimeException(msg);
            }

            ep = res.getProperty(PHYSICAL_DESTINATION);
        } catch (Exception ce) {
            String msg = I18N.getString("ajra.err_getting_dest", logicalDest);
            ConnectorRuntimeException cre = new ConnectorRuntimeException(msg);
            cre.initCause(ce);
            throw cre;
        }

        if (ep == null) {
            String msg = I18N.getString("ajra.cannot_find_phy_dest", null);
            throw new ConnectorRuntimeException(msg);
        }

        return ep.getValue();
    }


    private JMSDestinationDefinitionDescriptor getJMSDestinationFromDescriptor(SimpleJndiName jndiName,
        EjbMessageBeanDescriptor ejbMessageBeanDescriptor) {
        JMSDestinationDefinitionDescriptor destination = null;
        if (jndiName.isJavaComponent() || !jndiName.hasJavaPrefix()) {
            if (isEjbInWar(ejbMessageBeanDescriptor)) {
                destination = getJMSDestination(jndiName, ejbMessageBeanDescriptor.getEjbBundleDescriptor().getModuleDescriptor());
            } else {
                destination = getJMSDestination(jndiName, ejbMessageBeanDescriptor);
            }
        } else if (jndiName.isJavaModule()) {
            if (isEjbInWar(ejbMessageBeanDescriptor)) {
                destination = getJMSDestination(jndiName, ejbMessageBeanDescriptor.getEjbBundleDescriptor().getModuleDescriptor());
            } else {
                destination = getJMSDestination(jndiName, ejbMessageBeanDescriptor.getEjbBundleDescriptor());
            }
        } else if (jndiName.isJavaApp()) {
            destination = getJMSDestination(jndiName, ejbMessageBeanDescriptor.getApplication());
        } else if (jndiName.isJavaGlobal()) {
            destination = getJMSDestination(jndiName, ejbMessageBeanDescriptor.getApplication());
            if (!isValidDestination(destination)) {
                destination = getJMSDestination(jndiName);
            }
        }
        if (isValidDestination(destination)) {
            return destination;
        }
        return null;
    }

    private boolean isValidDestination(JMSDestinationDefinitionDescriptor descriptor) {
        return (descriptor != null) && (descriptor.getName() != null) && !"".equals(descriptor.getName());
    }

    private boolean isEjbInWar(EjbBundleDescriptor ejbBundleDescriptor) {
        Object rootDeploymentDescriptor = ejbBundleDescriptor.getModuleDescriptor().getDescriptor();
        if ((rootDeploymentDescriptor != ejbBundleDescriptor) && (rootDeploymentDescriptor instanceof WebBundleDescriptor)) {
            return true;
        }
        return false;
    }

    private boolean isEjbInWar(EjbMessageBeanDescriptor ejbMessageBeanDescriptor) {
        return isEjbInWar(ejbMessageBeanDescriptor.getEjbBundleDescriptor());
    }

    /**
     * Get JMS destination resource from component
     */
    private JMSDestinationDefinitionDescriptor getJMSDestination(SimpleJndiName logicalDestination, EjbMessageBeanDescriptor ejbMessageBeanDescriptor) {
        return getJMSDestination(logicalDestination, ejbMessageBeanDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
    }

    /**
     * Get JMS destination resource from ejb module
     */
    private JMSDestinationDefinitionDescriptor getJMSDestination(SimpleJndiName logicalDestination, EjbBundleDescriptor ejbBundleDescriptor) {
        JMSDestinationDefinitionDescriptor destination =
                getJMSDestination(logicalDestination, ejbBundleDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
        if (isValidDestination(destination)) {
            return destination;
        }

        Set<EjbDescriptor> ejbDescriptors = (Set<EjbDescriptor>) ejbBundleDescriptor.getEjbs();
        for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
            destination = getJMSDestination(logicalDestination, ejbDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
            if (isValidDestination(destination)) {
                return destination;
            }
        }

        return null;
    }

    /**
     * Get JMS destination resource from web module
     */
    private JMSDestinationDefinitionDescriptor getJMSDestination(SimpleJndiName logicalDestination, ModuleDescriptor moduleDescriptor) {
        WebBundleDescriptor webBundleDescriptor = (WebBundleDescriptor) moduleDescriptor.getDescriptor();
        JMSDestinationDefinitionDescriptor destination =
                getJMSDestination(logicalDestination, webBundleDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
        if (isValidDestination(destination)) {
            return destination;
        }

        Collection<EjbBundleDescriptor> ejbBundleDescriptors = moduleDescriptor.getDescriptor().getExtensionsDescriptors(EjbBundleDescriptor.class);
        for (EjbBundleDescriptor ejbBundleDescriptor : ejbBundleDescriptors) {
            destination = getJMSDestination(logicalDestination, ejbBundleDescriptor);
            if (isValidDestination(destination)) {
                return destination;
            }
        }

        return null;
    }

    /**
     * Get JMS destination resource from application
     */
    private JMSDestinationDefinitionDescriptor getJMSDestination(SimpleJndiName logicalDestination, Application application) {
        if (application == null) {
            return null;
        }

        JMSDestinationDefinitionDescriptor destination =
                getJMSDestination(logicalDestination, application.getResourceDescriptors(JavaEEResourceType.JMSDD));
        if (isValidDestination(destination)) {
            return destination;
        }

        Set<WebBundleDescriptor> webBundleDescriptors = application.getBundleDescriptors(WebBundleDescriptor.class);
        for (WebBundleDescriptor webBundleDescriptor : webBundleDescriptors) {
            destination = getJMSDestination(logicalDestination, webBundleDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
            if (isValidDestination(destination)) {
                return destination;
            }
        }

        Set<EjbBundleDescriptor> ejbBundleDescriptors = application.getBundleDescriptors(EjbBundleDescriptor.class);
        for (EjbBundleDescriptor ejbBundleDescriptor : ejbBundleDescriptors) {
            destination = getJMSDestination(logicalDestination, ejbBundleDescriptor);
            if (isValidDestination(destination)) {
                return destination;
            }
        }

        Set<ApplicationClientDescriptor> appClientDescriptors = application.getBundleDescriptors(ApplicationClientDescriptor.class);
        for (ApplicationClientDescriptor appClientDescriptor : appClientDescriptors) {
            destination = getJMSDestination(logicalDestination, appClientDescriptor.getResourceDescriptors(JavaEEResourceType.JMSDD));
            if (isValidDestination(destination)) {
                return destination;
            }
        }

        return null;
    }

    /**
     * Get JMS destination resource from deployed applications
     */
    private JMSDestinationDefinitionDescriptor getJMSDestination(SimpleJndiName logicalDestination) {
        Domain domain = Globals.get(Domain.class);
        Applications applications = domain.getApplications();
        for (com.sun.enterprise.config.serverbeans.Application app : applications.getApplications()) {
            ApplicationInfo appInfo = appRegistry.get(app.getName());
            if (appInfo != null) {
                Application application = appInfo.getMetaData(Application.class);
                JMSDestinationDefinitionDescriptor destination = getJMSDestination(logicalDestination, application);
                if (isValidDestination(destination)) {
                    return destination;
                }
            }
        }
        return null;
    }

    /**
     * Get JMS destination resource from descriptor set
     */
    private JMSDestinationDefinitionDescriptor getJMSDestination(SimpleJndiName jndiName, Set<? extends Descriptor> descriptors) {
        for (Descriptor descriptor : descriptors) {
            if (descriptor instanceof JMSDestinationDefinitionDescriptor) {
                if (jndiName.toString().equals(((JMSDestinationDefinitionDescriptor) descriptor).getName())) {
                    return (JMSDestinationDefinitionDescriptor) descriptor;
                }
            }
        }
        return null;
    }


    private void setValuesFromConfiguration(String cfName, EjbMessageBeanDescriptor descriptor_) throws ConnectorRuntimeException {
        //todo: need to enable
        List <Property> ep = null;
        try {
            String appName = descriptor_.getApplication().getAppName();
            String moduleName = ConnectorsUtil.getModuleName(descriptor_);
            ResourcesUtil resourcesUtil = ResourcesUtil.createInstance();
            ConnectorResource res = resourcesUtil.getResource(new SimpleJndiName(cfName), appName, moduleName,
                ConnectorResource.class);

            if (res == null) {
                String msg = I18N.getString("ajra.mdb_cf_not_created", cfName);
                throw new ConnectorRuntimeException(msg);
            }

            ConnectorConnectionPool ccp = resourcesUtil.getResource(new SimpleJndiName(res.getPoolName()), appName,
                moduleName, ConnectorConnectionPool.class);

            ep = ccp.getProperty();
        } catch(Exception ce) {
            String msg = I18N.getString("ajra.mdb_cf_not_created", cfName);
            ConnectorRuntimeException cre = new ConnectorRuntimeException( msg );
            cre.initCause( ce );
            throw cre;
        }

        if (ep == null) {
            String msg = I18N.getString("ajra.cannot_find_phy_dest");
            throw new ConnectorRuntimeException( msg );
        }

        for (Property prop : ep) {
            String name = prop.getName();
            if (name.equals(MCFADDRESSLIST)) {
                name = ADDRESSLIST;
            }
            String val = prop.getValue();
            if (val == null || val.isEmpty()) {
                continue;
            }
            descriptor_.putRuntimeActivationConfigProperty(new EnvironmentProperty(name, val, null));
        }

    }

    public int getAddressListCount() {
        StringTokenizer tokenizer = null;
        int count = 1;
        if (addressList != null) {
            tokenizer = new StringTokenizer(addressList, ",");
            count = tokenizer.countTokens();
        }
        LOG.log(FINE, "Address list count is {0}", count);
        return count;
    }

    private ServerEnvironmentImpl getServerEnvironment(){
        return serverEnvironmentImplProvider.get();
    }

    private AdminService getAdminService() {
        return adminServiceProvider.get();
    }

    private JmsService getJmsService(){
        return habitat.getService(JmsService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
    }

    private ServerContext getServerContext(){
        return serverContextProvider.get();
    }

    //methods from LazyServiceIntializer
    @Override
    public boolean initializeService(){
        try {
            String module = ConnectorConstants.DEFAULT_JMS_ADAPTER;
            String loc = ConnectorsUtil.getSystemModuleLocation(module);
            ConnectorRuntime connectorRuntime = connectorRuntimeProvider.get();
            connectorRuntime.createActiveResourceAdapter(loc, module, null);
            return true;
        } catch (ConnectorRuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void handleRequest(SelectableChannel selectableChannel){
        SocketChannel socketChannel = null;
        if (selectableChannel instanceof SocketChannel) {
            socketChannel = (SocketChannel) selectableChannel;
            try {
                Class<?> c = resourceadapter_.getClass();
                Method m = c.getMethod("getPortMapperClientHandler");
                Object handler = m.invoke(resourceadapter_);
                m = handler.getClass().getMethod("handleRequest", SocketChannel.class);
                m.invoke(handler, socketChannel);
            } catch (Exception ex){
                String message = I18N.getString("error.invoke.portmapper", ex.getLocalizedMessage());
                throw new RuntimeException (message, ex);
            }
        } else {
            throw new IllegalArgumentException(I18N.getString("invalid.socket.channel"));
        }
    }

    public void setMasterBroker(String newMasterBroker){
        try{
            Class<?> c = resourceadapter_.getClass();
            Method m = c.getMethod("setMasterBroker", String.class);
            m.invoke(resourceadapter_, newMasterBroker);
            LOG.log(INFO, JMSLoggerInfo.MASTER_BROKER_SUCCESS, new Object[] {newMasterBroker});
        } catch (Exception ex) {
            if (LOG.isLoggable(INFO)) {
                LOG.log(INFO, JMSLoggerInfo.MASTER_BROKER_FAILURE,
                    new Object[] {newMasterBroker, ex.getMessage()});
            }
        }
    }

    protected void setClusterBrokerList(String brokerList){
        try{
            Class<?> c = resourceadapter_.getClass();
            Method m = c.getMethod("setClusterBrokerList", String.class);
            m.invoke(resourceadapter_, brokerList);
            LOG.log(INFO, JMSLoggerInfo.CLUSTER_BROKER_SUCCESS, new Object[] {brokerList});
        } catch (Exception ex){
            if (LOG.isLoggable(WARNING)) {
                LOG.log(WARNING, JMSLoggerInfo.CLUSTER_BROKER_FAILURE,
                    new Object[] {brokerList, ex.getMessage()});
            }
        }
    }


    private SimpleJndiName getJndiName(EjbMessageBeanDescriptor ejbDescriptor) {
        final SimpleJndiName jndiName = ejbDescriptor.getJndiName();
        if (jndiName != null && !jndiName.isEmpty()) {
            return jndiName;
        }
        MessageDestinationDescriptor destDescriptor = ejbDescriptor.getMessageDestination();
        if (destDescriptor != null) {
            return destDescriptor.getJndiName();
        }
        return jndiName;
    }
}
