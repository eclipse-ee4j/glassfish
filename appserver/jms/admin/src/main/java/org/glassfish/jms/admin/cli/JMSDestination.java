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

package org.glassfish.jms.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.jms.config.JmsHost;
import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.connectors.jms.system.ActiveJmsResourceAdapter;
import com.sun.enterprise.connectors.jms.system.MQAddressList;
import com.sun.enterprise.connectors.jms.util.JmsRaUtil;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.spi.ResourceAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.glassfish.config.support.CommandTarget;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;

import static com.sun.enterprise.connectors.jms.system.ActiveJmsResourceAdapter.ADMINPASSWORD;
import static com.sun.enterprise.connectors.jms.system.ActiveJmsResourceAdapter.ADMINUSERNAME;

/**
 * Common parent for JMS Destination admin commands
 */
public abstract class JMSDestination {

    protected static final Logger logger = Logger.getLogger(LogUtils.JMS_ADMIN_LOGGER);
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJMSDestination.class);

    public static final String JMS_DEST_TYPE_TOPIC = "topic";
    public static final String JMS_DEST_TYPE_QUEUE = "queue";

    public static final String DEFAULT_MAX_ACTIVE_CONSUMERS = "-1";
    public static final String MAX_ACTIVE_CONSUMERS_ATTRIBUTE = "MaxNumActiveConsumers";
    public static final String MAX_ACTIVE_CONSUMERS_PROPERTY = "maxNumActiveConsumers";
    public static final String JMXSERVICEURLLIST = "JMXServiceURLList";
    public static final String JMXCONNECTORENV = "JMXConnectorEnv";

    // Following properties are from com.sun.messaging.jms.management.server.MQObjectName
    /** Domain name for MQ MBeans */
    protected static final String MBEAN_DOMAIN_NAME = "com.sun.messaging.jms.server";
    /** String representation of the ObjectName for the DestinationManager Config MBean. */
    protected static final String DESTINATION_MANAGER_CONFIG_MBEAN_NAME = MBEAN_DOMAIN_NAME
        + ":type=DestinationManager,subtype=Config";

    protected static final String CLUSTER_CONFIG_MBEAN_NAME = MBEAN_DOMAIN_NAME + ":type=Cluster,subtype=Config";
    /** Queue destination type */
    protected static final String DESTINATION_TYPE_QUEUE= "q";
    /** Topic destination type */
    protected static final String DESTINATION_TYPE_TOPIC = "t";

    protected void validateJMSDestName(final String destName) {
        if (destName == null || destName.length() <= 0) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("admin.mbeans.rmb.invalid_jms_destname", destName));
        }
    }


    protected void validateJMSDestType(final String destType) {
        if (destType == null || destType.isEmpty()) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("admin.mbeans.rmb.invalid_jms_desttype", destType));
        }
        if (!JMS_DEST_TYPE_QUEUE.equals(destType) && !JMS_DEST_TYPE_TOPIC.equals(destType)) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("admin.mbeans.rmb.invalid_jms_desttype", destType));
        }
    }


    protected MQJMXConnectorInfo createMQJMXConnectorInfo(final String targetName, final Config config,
        final ServerContext serverContext, final Domain domain, final ConnectorRuntime connectorRuntime)
            throws ConnectorRuntimeException {
        logger.log(Level.FINE, "createMQJMXConnectorInfo for {0}", targetName);
        try {
            final JmsService jmsService = config.getExtensionByType(JmsService.class);
            final ActiveJmsResourceAdapter air = getMQAdapter(connectorRuntime);
            final Class<? extends ResourceAdapter> mqRAClassName = air.getResourceAdapter().getClass();
            final CommandTarget ctarget = this.getTypeForTarget(targetName);
            final PrivilegedExceptionAction<MQJMXConnectorInfo> action = () -> {
                if (ctarget == CommandTarget.CLUSTER || ctarget == CommandTarget.CLUSTERED_INSTANCE) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Getting JMX connector for cluster target " + targetName);
                    }
                    return _getMQJMXConnectorInfoForCluster(targetName, jmsService, mqRAClassName, serverContext);
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Getting JMX connector for standalone target " + targetName);
                }
                return createMQJMXConnectorInfo(targetName, jmsService, mqRAClassName, serverContext, config, domain);
            };
            return AccessController.doPrivileged(action);
        } catch (final Exception e) {
            throw new ConnectorRuntimeException(e);
        }
    }


    private MQJMXConnectorInfo createMQJMXConnectorInfo(final String targetName, final JmsService jmsService,
        final Class<? extends ResourceAdapter> mqRAClassName, final ServerContext serverContext, final Config config,
        final Domain domain) throws ConnectorRuntimeException {
        try {
            final MQAddressList mqadList = new MQAddressList();

            final String connectionURL;
            if (getTypeForTarget(targetName) == CommandTarget.DAS) {
                connectionURL = getDefaultAddressList(jmsService).toString();
            } else {
                logger.log(Level.FINEST," _getMQJMXConnectorInfo - standalone JMS service, NOT in DAS");
                final JmsService serverJmsService= getJmsServiceOfStandaloneServerInstance(targetName, config, domain);
                mqadList.setJmsService(serverJmsService);
                mqadList.setTargetName(targetName);
                mqadList.setup(false);
                connectionURL = mqadList.toString();
            }
            logger.log(Level.FINE, " _getMQJMXConnectorInfo - connection URL {0}", connectionURL);
            final String adminUserName;
            final String adminPassword;
            final JmsHost jmsHost = mqadList.getDefaultJmsHost(jmsService);
            if (jmsHost == null) {
                logger.log(Level.FINE, " _getMQJMXConnectorInfo, using default jms admin user and password ");
                adminUserName = null;
                adminPassword = null;
            } else {
                adminUserName = jmsHost.getAdminUserName();
                adminPassword = JmsRaUtil.getUnAliasedPwd(jmsHost.getAdminPassword());
            }

            final ResourceAdapter raInstance = getConfiguredRA(mqRAClassName, connectionURL, adminUserName, adminPassword);
            final String jmxServiceURLList = getJmxServiceUrlList(raInstance);
            final Map<String, ?> jmxConnectorEnv = getJmxConnectorEnv(raInstance);
            logger.log(Level.CONFIG, " _getMQJMXConnectorInfo - jmxServiceURLList {0}", jmxServiceURLList);
            logger.log(Level.CONFIG, " _getMQJMXConnectorInfo - jmxConnectorEnv {0}", jmxConnectorEnv);
            final String jmxServiceURL = getFirstJMXServiceURL(jmxServiceURLList);
            return new MQJMXConnectorInfo(targetName, ActiveJmsResourceAdapter.getBrokerInstanceName(jmsService),
                jmsService.getType(), jmxServiceURL, jmxConnectorEnv);
        } catch (final Exception e) {
            throw new ConnectorRuntimeException(e);
        }
    }


    private String getJmxServiceUrlList(final ResourceAdapter raInstance) {
        final String methodName = "get" + JMXSERVICEURLLIST;
        try {
            final Method method = raInstance.getClass().getMethod(methodName);
            return (String) method.invoke(raInstance);
        } catch (final ReflectiveOperationException e) {
            logger.log(Level.INFO, "Invocation of " + methodName + " failed, returning null.", e);
            return null;
        }
    }


    private Map<String, ?> getJmxConnectorEnv(final ResourceAdapter raInstance) {
        final String methodName = "get" + JMXCONNECTORENV;
        try {
            final Method method = raInstance.getClass().getMethod(methodName);
            return (Map<String, ?>) method.invoke(raInstance);
        } catch (final ReflectiveOperationException e) {
            logger.log(Level.INFO, "Invocation of " + methodName + " failed, returning null.", e);
            return null;
        }
    }


    /**
     *  Gets the <code>MQJMXConnector</code> object for a cluster. Since this code is
     *  executed in DAS, an admin API is used to resolve hostnames and ports of
     *  cluster instances for LOCAL type brokers while creating the connectionURL.
     */
    protected MQJMXConnectorInfo _getMQJMXConnectorInfoForCluster(final String target, final JmsService jmsService,
        final Class<? extends ResourceAdapter> mqRAClass, final ServerContext serverContext)
            throws ConnectorRuntimeException {
        final ResourceAdapter raInstance;
        try {
            final MQAddressList list;
            if (jmsService.getType().equalsIgnoreCase(ActiveJmsResourceAdapter.REMOTE)) {
                list = getDefaultAddressList(jmsService);
            } else {
                list = new MQAddressList();
                final CommandTarget ctarget = this.getTypeForTarget(target);
                if (ctarget == CommandTarget.CLUSTER) {
                    final Server[] servers = list.getServersInCluster(target);
                    if (servers != null && servers.length > 0) {
                        list.setInstanceName(servers[0].getName());
                    }
                } else if (ctarget == CommandTarget.CLUSTERED_INSTANCE) {
                    list.setInstanceName(target);
                }
                final Map<String, JmsHost> hostMap = list.getResolvedLocalJmsHostsInMyCluster(true);
                if (hostMap.isEmpty()) {
                    final String msg = localStrings.getLocalString("mqjmx.no_jms_hosts", "No JMS Hosts Configured");
                    throw new ConnectorRuntimeException(msg);
                }
                for (final JmsHost host : hostMap.values()) {
                    list.addMQUrl(host);
                }
            }

            final String connectionUrl = list.toString();
            final String adminUserName;
            final String adminPassword;
            final JmsHost jmsHost = list.getDefaultJmsHost(jmsService);
            if (jmsHost == null) {
                logger.log(Level.FINE, " _getMQJMXConnectorInfo, using default jms admin user and password ");
                adminUserName = null;
                adminPassword = null;
            } else {
                adminUserName = jmsHost.getAdminUserName();
                adminPassword = JmsRaUtil.getUnAliasedPwd(jmsHost.getAdminPassword());
            }
            raInstance = getConfiguredRA(mqRAClass, connectionUrl, adminUserName, adminPassword);
        } catch (final Exception e) {
            throw new ConnectorRuntimeException(e);
        }

        try {
            final String jmxServiceURLList = getJmxServiceUrlList(raInstance);
            final Map<String, ?> jmxConnectorEnv = getJmxConnectorEnv(raInstance);
            final String jmxServiceURL = getFirstJMXServiceURL(jmxServiceURLList);
            return new MQJMXConnectorInfo(target, ActiveJmsResourceAdapter.getBrokerInstanceName(jmsService),
                jmsService.getType(), jmxServiceURL, jmxConnectorEnv);
        } catch (final Exception e) {
            throw new ConnectorRuntimeException(e);
        }
    }


    /**
     * Configures an instance of MQ-RA with the connection URL passed in.
     * This configured RA is then used to obtain the JMXServiceURL/JMXServiceURLList
     */
    protected ResourceAdapter getConfiguredRA(final Class<? extends ResourceAdapter> mqRAclassname,
        final String connectionURL, final String adminuser, final String adminpasswd) throws Exception {
        final ResourceAdapter raInstance = mqRAclassname.getDeclaredConstructor().newInstance();
        final Method setConnectionURL = mqRAclassname.getMethod(
            "set" + ActiveJmsResourceAdapter.CONNECTION_URL, new Class[] {String.class});
        setConnectionURL.invoke(raInstance, new Object[] {connectionURL});
        logger.log(Level.FINE, "getConfiguredRA - set connectionURL as {0}", connectionURL);
        if (adminuser != null) {
            final Method setAdminUser = mqRAclassname.getMethod("set" + ADMINUSERNAME, new Class[] {String.class});
            setAdminUser.invoke(raInstance, new Object[] {adminuser});
            logger.log(Level.FINE, "getConfiguredRA - set admin user as {0}", adminuser);
        }
        if (adminpasswd != null) {
            final Method setAdminPasswd = mqRAclassname.getMethod("set" + ADMINPASSWORD, new Class[] {String.class});
            setAdminPasswd.invoke(raInstance, new Object[] {adminpasswd});
            logger.log(Level.FINE, "getConfiguredRA - set admin passwd");
        }
        return raInstance;
    }


    private JmsService getJmsServiceOfStandaloneServerInstance(final String target, final Config cfg, final Domain domain) throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "getJMSServiceOfSI target: {0}", target);
            logger.log(Level.FINE, "cfg " + cfg);
        }
        final JmsService jmsService = cfg.getExtensionByType(JmsService.class);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "jmsservice " + jmsService);
        }
        return jmsService;
    }


    private String getFirstJMXServiceURL(final String jmxServiceURLList) {
        if (jmxServiceURLList == null || jmxServiceURLList.isBlank()) {
            return null;
        }
        final StringTokenizer tokenizer = new StringTokenizer(jmxServiceURLList, " ");
        return tokenizer.nextToken();
    }


    protected CommandTarget getTypeForTarget(final String target){
        final Domain domain = Globals.get(Domain.class);
        final Config config = domain.getConfigNamed(target);
        if (config != null) {
            return CommandTarget.CONFIG;
        }
        final Server targetServer = domain.getServerNamed(target);
        if (targetServer != null) {
            if (targetServer.isDas()) {
                return CommandTarget.DAS;
            }
            return CommandTarget.STANDALONE_INSTANCE;
        }
        final Cluster cluster = domain.getClusterNamed(target);
        if (cluster != null) {
            return CommandTarget.CLUSTER;
        }
        return CommandTarget.DAS;
    }


    /**
     * Starts the MQ RA in the DAS, as all MQ related operations are
     * performed in DAS.
     */
    protected ActiveJmsResourceAdapter getMQAdapter(final ConnectorRuntime connectorRuntime) throws Exception {
        PrivilegedExceptionAction<ActiveJmsResourceAdapter> action = () -> {
            final String module = ConnectorConstants.DEFAULT_JMS_ADAPTER;
            final String loc = ConnectorsUtil.getSystemModuleLocation(module);
            connectorRuntime.createActiveResourceAdapter(loc, module, null);
            return (ActiveJmsResourceAdapter) ConnectorRegistry.getInstance().getActiveResourceAdapter(module);
        };
        return AccessController.doPrivileged(action);
    }


    protected MQAddressList getDefaultAddressList(final JmsService jmsService) throws Exception {
        final MQAddressList list = new MQAddressList(jmsService);
        list.setup(false);
        return list;
    }


    protected final JMSAdminException logAndHandleException(final Exception cause, final String messageKey)
        throws JMSAdminException {
        return handleException(new Exception(localStrings.getLocalString(messageKey, null), cause));
    }


    /**
     * @param e original cause; it is not included in the result, because the caller's classloader
     *            might not know all internal exception.
     * @return JMSAdminException to throw
     */
    protected final JMSAdminException handleException(final Exception e) {
        logger.log(Level.WARNING, "Handling exception to be thrown.", e);
        if (e instanceof JMSAdminException)  {
            return ((JMSAdminException)e);
        }
        final String msg = e.getMessage();
        if (msg == null) {
            try (PrintWriter writer = new PrintWriter(new StringWriter())) {
                e.printStackTrace(writer);
                return new JMSAdminException(writer.toString());
            }
        }
        return new JMSAdminException(msg);
    }


    // XXX: To refactor into a Generic attribute type mapper, so that it is extensible later.
    protected AttributeList convertProp2Attrs(final Properties destProps) {
        final AttributeList destAttrs = new AttributeList();

        String propName = null;
        String propValue = null;

        for (final Enumeration e = destProps.propertyNames(); e.hasMoreElements();) {
            propName = (String) e.nextElement();
            if (propName.equals("AutoCreateQueueMaxNumActiveConsumers")) {
                destAttrs.add(new Attribute("AutoCreateQueueMaxNumActiveConsumers",
                    Integer.valueOf(destProps.getProperty("AutoCreateQueueMaxNumActiveConsumers"))));
            } else if (propName.equals("maxNumActiveConsumers")) {
                destAttrs.add(new Attribute("MaxNumActiveConsumers",
                    Integer.valueOf(destProps.getProperty("maxNumActiveConsumers"))));
            } else if (propName.equals("MaxNumActiveConsumers")) {
                destAttrs.add(new Attribute("MaxNumActiveConsumers",
                    Integer.valueOf(destProps.getProperty("MaxNumActiveConsumers"))));
            } else if (propName.equals("AutoCreateQueueMaxNumBackupConsumers")) {
                destAttrs.add(new Attribute("AutoCreateQueueMaxNumBackupConsumers",
                    Integer.valueOf(destProps.getProperty("AutoCreateQueueMaxNumBackupConsumers"))));
            } else if (propName.equals("AutoCreateQueues")) {
                boolean b = false;
                propValue = destProps.getProperty("AutoCreateQueues");
                if (propValue.equalsIgnoreCase("true")) {
                    b = true;
                }
                destAttrs.add(new Attribute("AutoCreateQueues", Boolean.valueOf(b)));
            } else if (propName.equals("AutoCreateTopics")) {
                boolean b = false;
                propValue = destProps.getProperty("AutoCreateTopics");
                if (propValue.equalsIgnoreCase("true")) {
                    b = true;
                }
                destAttrs.add(new Attribute("AutoCreateTopics", Boolean.valueOf(b)));
            } else if (propName.equals("DMQTruncateBody")) {
                boolean b = false;
                propValue = destProps.getProperty("DMQTruncateBody");
                if (propValue.equalsIgnoreCase("true")) {
                    b = true;
                }
                destAttrs.add(new Attribute("DMQTruncateBody", Boolean.valueOf(b)));
            } else if (propName.equals("LogDeadMsgs")) {
                boolean b = false;
                propValue = destProps.getProperty("LogDeadMsgs");
                if (propValue.equalsIgnoreCase("true")) {
                    b = true;
                }
                destAttrs.add(new Attribute("LogDeadMsgs", Boolean.valueOf(b)));
            } else if (propName.equals("MaxBytesPerMsg")) {
                destAttrs.add(new Attribute("MaxBytesPerMsg",
                    Long.valueOf(destProps.getProperty("MaxBytesPerMsg"))));
            } else if (propName.equals("MaxNumMsgs")) {
                destAttrs.add(new Attribute("MaxNumMsgs",
                    Long.valueOf(destProps.getProperty("MaxNumMsgs"))));
            } else if (propName.equals("MaxTotalMsgBytes")) {
                destAttrs.add(new Attribute("MaxTotalMsgBytes",
                    Long.valueOf(destProps.getProperty("MaxTotalMsgBytes"))));
            } else if (propName.equals("NumDestinations")) {
                destAttrs.add(new Attribute("NumDestinations",
                    Integer.valueOf(destProps.getProperty("NumDestinations"))));
            } else if (propName.equals("ConsumerFlowLimit")) {
                destAttrs.add(new Attribute("ConsumerFlowLimit",
                    Long.valueOf(destProps.getProperty("ConsumerFlowLimit"))));
            } else if (propName.equals("LocalDeliveryPreferred")) {
                destAttrs.add(new Attribute("LocalDeliveryPreferred",
                    getBooleanValue(destProps.getProperty("LocalDeliveryPreferred"))));
            } else if (propName.equals("ValidateXMLSchemaEnabled")) {
                destAttrs.add(new Attribute("ValidateXMLSchemaEnabled",
                    getBooleanValue(destProps.getProperty("ValidateXMLSchemaEnabled"))));
            } else if (propName.equals("UseDMQ")) {
                destAttrs.add(new Attribute("UseDMQ",
                    getBooleanValue(destProps.getProperty("UseDMQ"))));
            } else if (propName.equals("LocalOnly")) {
                destAttrs.add(new Attribute("LocalOnly",
                    getBooleanValue(destProps.getProperty("LocalOnly"))));
            } else if (propName.equals("ReloadXMLSchemaOnFailure")) {
                destAttrs.add(new Attribute("ReloadXMLSchemaOnFailure",
                    getBooleanValue(destProps.getProperty("ReloadXMLSchemaOnFailure"))));
            } else if (propName.equals("MaxNumProducers")) {
                destAttrs.add(new Attribute("MaxNumProducers",
                    Integer.valueOf(destProps.getProperty("MaxNumProducers"))));
            } else if (propName.equals("MaxNumBackupConsumers")) {
                destAttrs.add(new Attribute("MaxNumBackupConsumers",
                    Integer.valueOf(destProps.getProperty("MaxNumBackupConsumers"))));
            } else if (propName.equals("LimitBehavior")) {
                destAttrs.add(new Attribute("LimitBehavior", destProps.getProperty("LimitBehavior")));
            }
        }
        return destAttrs;
    }

    private Boolean getBooleanValue(final String propValue) {
        return propValue.equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE;
    }
}
