/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.jms.system;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.connectors.jms.JMSLoggerInfo;
import com.sun.enterprise.connectors.jms.config.JmsHost;
import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.connectors.jms.util.JmsRaUtil;
import com.sun.enterprise.v3.services.impl.DummyNetworkListener;
import com.sun.enterprise.v3.services.impl.GrizzlyService;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.logging.LogHelper;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

@Service
public class JMSConfigListener implements ConfigListener {
    // Injecting @Configured type triggers the corresponding change
    // events to be sent to this instance

    private JmsService jmsService;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config serverConfig;

    private ActiveJmsResourceAdapter aresourceAdapter;

    private static final Logger LOG = JMSLoggerInfo.getLogger();


    public void setActiveResourceAdapter(ActiveJmsResourceAdapter aresourceAdapter) {
        this.aresourceAdapter = aresourceAdapter;
    }


    /** Implementation of org.jvnet.hk2.config.ConfigListener */
    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        LOG.log(Level.CONFIG, "changed(events={0})", events);
        //Events that we can't process now because they require server restart.
        jmsService = serverConfig.getExtensionByType(JmsService.class);
        List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<>();
        LOG.log(Level.FINE, "In JMSConfigListener - received config event");
        Domain domain = Globals.get(Domain.class);
        String jmsProviderPort = null;
        ServerContext serverContext = Globals.get(ServerContext.class);
        Server thisServer = domain.getServerNamed(serverContext.getInstanceName());
        for (int i = 0; i < events.length; i++) {
            PropertyChangeEvent event = events[i];
            String eventName = event.getPropertyName();
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();

            LOG.log(Level.FINE, "In JMSConfigListener. Event name={0}, oldValue={1}, newValue={2}",
                new Object[] {eventName, oldValue, newValue});
            if (oldValue != null && oldValue.equals(newValue)) {
                continue;
            }

            if (event.getSource().toString().indexOf("connectors.jms.config.JmsService") != -1) {
                boolean notifyFlag = true;
                if (oldValue != null && newValue == null && "jms-host".equals(event.getPropertyName())) {
                    JmsHost jmsHost = (JmsHost) oldValue;
                    String name = ActiveJmsResourceAdapter.GRIZZLY_PROXY_PREFIX + jmsHost.getName();
                    ActiveJmsResourceAdapter adapter = Globals.get(ActiveJmsResourceAdapter.class);
                    if (adapter.getGrizzlyListeners().contains(name)) {
                        GrizzlyService grizzlyService = Globals.get(GrizzlyService.class);
                        synchronized (adapter.getGrizzlyListeners()) {
                            LOG.log(Level.FINE, "Stopping Grizzly proxy {0}", name);
                            grizzlyService.removeNetworkProxy(name);
                            adapter.getGrizzlyListeners().remove(name);
                        }
                        notifyFlag = false;
                    }
                }
                if (notifyFlag) {
                    UnprocessedChangeEvent uchangeEvent = new UnprocessedChangeEvent(event, "restart required");
                    unprocessedEvents.add(uchangeEvent);
                }
            }
            else if (event.getSource().toString().indexOf("connectors.jms.config.JmsHost") != -1) {
                if (oldValue == null && newValue != null && "name".equals(event.getPropertyName())) {
                    JmsHost jmsHost = (JmsHost) event.getSource();
                    if (ActiveJmsResourceAdapter.EMBEDDED.equalsIgnoreCase(jmsService.getType())) {
                        ActiveJmsResourceAdapter adapter = Globals.get(ActiveJmsResourceAdapter.class);
                        if (!adapter.getDoBind()) {
                            if (Boolean.valueOf(jmsHost.getLazyInit())) {
                                String host = null;
                                if (jmsHost.getHost() != null && "localhost".equals(jmsHost.getHost())) {
                                    host = "0.0.0.0";
                                } else {
                                    host = jmsHost.getHost();
                                }
                                try {
                                    GrizzlyService grizzlyService = Globals.get(GrizzlyService.class);
                                    NetworkListener dummy = new DummyNetworkListener();
                                    dummy.setPort(jmsHost.getPort());
                                    dummy.setAddress(host);
                                    dummy.setType("proxy");
                                    dummy.setProtocol(ActiveJmsResourceAdapter.JMS_SERVICE);
                                    dummy.setTransport("tcp");
                                    String name = ActiveJmsResourceAdapter.GRIZZLY_PROXY_PREFIX + jmsHost.getName();
                                    dummy.setName(name);
                                    synchronized (adapter.getGrizzlyListeners()) {
                                        LOG.log(Level.FINE, "Starting Grizzly proxy {0} on port {1}",
                                            new Object[] {name, jmsHost.getPort()});
                                        grizzlyService.createNetworkProxy(dummy);
                                        adapter.getGrizzlyListeners().add(name);
                                    }
                                    return unprocessedEvents.size() > 0 ? new UnprocessedChangeEvents(unprocessedEvents) : null;
                                } catch (Exception e) {
                                    LogHelper.log(LOG, Level.WARNING, JMSLoggerInfo.GRIZZLY_START_FAILURE, e);
                                }
                            }
                        }
                    }
                }
            }

            if ("JMS_PROVIDER_PORT".equals(newValue)){
                //The value is in the next event
                PropertyChangeEvent nextevent = events[i+1] ;
                jmsProviderPort = (String) nextevent.getNewValue();
            }
            if (event.getSource() instanceof JmsService) {
                if (eventName.equals(ServerTags.MASTER_BROKER)) {
                    String oldMB = oldValue == null ? null : oldValue.toString();
                    String newMB = newValue == null ? null : newValue.toString();

                    LOG.log(Level.FINE,
                        "Got JmsService Master Broker change event. Source={0}, eventName={1}, oldMB={2}, newMB={3}",
                        new Object[] {event.getSource(), eventName, oldMB, newMB});

                    if (newMB != null) {
                        Server newMBServer = domain.getServerNamed(newMB);
                        if (newMBServer != null) {
                            Node node = domain.getNodeNamed(newMBServer.getNodeRef());
                            String newMasterBrokerPort = JmsRaUtil.getJMSPropertyValue(newMBServer);
                            if (newMasterBrokerPort == null) {
                                newMasterBrokerPort = getDefaultJmsHost(jmsService).getPort();
                            }
                            String newMasterBrokerHost = node.getNodeHost();
                            aresourceAdapter.setMasterBroker(newMasterBrokerHost + ":" + newMasterBrokerPort);
                        }
                    }
                }
            }

            if (eventName.equals(ServerTags.SERVER_REF)){
                String oldServerRef = oldValue != null ? oldValue.toString() : null;
                String newServerRef = newValue != null ? newValue.toString(): null;
                if (oldServerRef != null && newServerRef == null && !thisServer.isDas()) {
                    // instance has been deleted
                    LOG.log(Level.FINE,
                        "Got Cluster change event for server_ref. Server={0}, eventName={1}, oldServerRef={2}",
                        new Object[] {event.getSource(), eventName, oldServerRef});
                    String url = getBrokerList();
                    aresourceAdapter.setClusterBrokerList(url);
                    break;
                }
            }

            if (event.getSource() instanceof Server) {
                LOG.log(Level.FINE, "In JMSConfigListener - recieved cluster event. Event source={0}", event.getSource());
                Server changedServer = (Server) event.getSource();
                if (thisServer.isDas()) {
                    return null;
                }

                if (jmsProviderPort != null) {
                    String nodeName = changedServer.getNodeRef();
                    String nodeHost = null;

                    if(nodeName != null) {
                        nodeHost = domain.getNodeNamed(nodeName).getNodeHost();
                    }
                    String url = getBrokerList();
                    url = url + ",mq://" + nodeHost + ":" + jmsProviderPort;
                    aresourceAdapter.setClusterBrokerList(url);
                    break;
                }

            }

        }
        return unprocessedEvents.isEmpty() ? null : new UnprocessedChangeEvents(unprocessedEvents);
    }

    private String getBrokerList(){
        MQAddressList addressList = new MQAddressList();
        try {
            addressList.setup(true);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, JMSLoggerInfo.ADDRESSLIST_SETUP_FAIL, ex.getMessage());
            LOG.log(Level.SEVERE, "The addressList.setup(true) failed.", ex);
        }
        return addressList.toString();
    }


    private JmsHost getDefaultJmsHost(JmsService jmsService) {
        JmsHost jmsHost = null;
        String defaultJmsHostName = jmsService.getDefaultJmsHost();
        List<JmsHost> jmsHostsList = jmsService.getJmsHost();

        for (JmsHost tmpJmsHost : jmsHostsList) {
            if (tmpJmsHost != null && tmpJmsHost.getName().equals(defaultJmsHostName)) {
                jmsHost = tmpJmsHost;
            }
        }
        if (jmsHost == null && !jmsHostsList.isEmpty()) {
            jmsHost = jmsHostsList.get(0);
        }
        return jmsHost;
    }
}
