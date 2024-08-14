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

package org.glassfish.jms.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * The <code>MQJMXConnectorInfo</code> holds MBean Server connection information
 * to a SJSMQ broker instance. This API is used by the admin infrastructure for
 * performing MQ administration/configuration operations on a broker instance.
 *
 * @author Sivakumar Thyagarajan
 * @since SJSAS 9.0
 */
public class MQJMXConnectorInfo implements AutoCloseable {
    private static final Logger _logger = Logger.getLogger(LogUtils.JMS_ADMIN_LOGGER);
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MQJMXConnectorInfo.class);

    private final String jmxServiceURL;
    private final Map<String,?> jmxConnectorEnv;
    private final String asInstanceName;
    private final String brokerInstanceName;
    private final String brokerType;
    private JMXConnector connector;

    public MQJMXConnectorInfo(String asInstanceName, String brokerInstanceName, String brokerType, String jmxServiceURL,
        Map<String, ?> jmxConnectorEnv) {
        this.brokerInstanceName = brokerInstanceName;
        this.asInstanceName = asInstanceName;
        this.jmxServiceURL = jmxServiceURL;
        this.brokerType = brokerType;
        this.jmxConnectorEnv = jmxConnectorEnv;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
                "MQJMXConnectorInfo : brokerInstanceName " + brokerInstanceName + " ASInstanceName " + asInstanceName
                    + " jmxServiceURL " + jmxServiceURL + " BrokerType " + brokerType + " jmxConnectorEnv "
                    + jmxConnectorEnv);
        }
    }

    public String getBrokerInstanceName(){
        return this.brokerInstanceName;
    }

    public String getBrokerType(){
        return this.brokerType;
    }

    public String getASInstanceName(){
        return this.asInstanceName;
    }

    public String getJMXServiceURL(){
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"MQJMXConnectorInfo :: JMXServiceURL is " + this.jmxServiceURL);
        }
        return this.jmxServiceURL;
    }

    public Map<String, ?> getJMXConnectorEnv(){
        return this.jmxConnectorEnv;
    }

    /**
     * @return an <code>MBeanServerConnection</code> representing the MQ broker instance's MBean server.
     * @throws ConnectorRuntimeException
     */
    //XXX:Enhance to support SSL (once MQ team delivers support in the next drop)
    //XXX: Discuss how <code>ConnectionNotificationListeners</code> could
    //be shared with the consumer of this API
    public MBeanServerConnection getMQMBeanServerConnection() throws ConnectorRuntimeException {
        try {
            if (connector == null) {
                if (getJMXServiceURL() == null || getJMXServiceURL().isEmpty()) {
                    String msg = localStrings.getLocalString("error.get.jmsserviceurl",
                        "Failed to get MQ JMXServiceURL of {0}.", getASInstanceName());
                    throw new ConnectorRuntimeException(msg);
                }
                _logger.log(Level.FINE, "creating MBeanServerConnection to MQ JMXServer with {0}", getJMXServiceURL());
                JMXServiceURL serviceURL = new JMXServiceURL(getJMXServiceURL());
                connector = JMXConnectorFactory.connect(serviceURL, this.jmxConnectorEnv);
            }
            //XXX: Do we need to pass in a Subject?
            MBeanServerConnection mbsc = connector.getMBeanServerConnection();
            return mbsc;
        } catch (Exception e) {
            throw new ConnectorRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Closes the connector.
     */
    @Override
    public void close() throws ConnectorRuntimeException {
        try {
            if (connector != null) {
                connector.close();
                connector = null;
            }
        } catch (IOException e) {
            throw new ConnectorRuntimeException(e.getMessage(), e);
        }
    }
 }
