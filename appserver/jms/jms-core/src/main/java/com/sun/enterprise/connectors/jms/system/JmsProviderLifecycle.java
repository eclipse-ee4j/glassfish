/*
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

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.connectors.jms.config.JmsHost;
import com.sun.enterprise.connectors.jms.config.JmsService;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.List;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.jvnet.hk2.annotations.Service;

@Service
@RunLevel(value=PostStartupRunLevel.VAL, mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class JmsProviderLifecycle implements PostConstruct{
    private static final String JMS_INITIALIZE_ON_DEMAND = "org.glassfish.jms.InitializeOnDemand";
    //Lifecycle properties
    public static final String EMBEDDED="EMBEDDED";
    public static final String LOCAL="LOCAL";
    public static final String REMOTE="REMOTE";
    public static final String JMS_SERVICE = "jms-service";
    //static Logger _logger = LogDomains.getLogger(JmsProviderLifecycle.class, LogDomains.RSR_LOGGER);

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    private Provider<JMSConfigListener> jmsConfigListenerProvider;

    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;

    @Inject
    private ActiveJmsResourceAdapter activeJmsResourceAdapter;

    public void postConstruct()
    {
       final JmsService jmsService = config.getExtensionByType(JmsService.class);
       if (eagerStartupRequired())
       {
        try {
                initializeBroker();
               } catch (ConnectorRuntimeException e) {
                   e.printStackTrace();
                   //_logger.log(Level.WARNING, "Failed to start JMS RA");
                   e.printStackTrace();
               }
       }
       activeJmsResourceAdapter.initializeLazyListener(jmsService);
       configureConfigListener();
       //createMonitoringConfig();

    }
    private void configureConfigListener(){
        //do a lookup of the config listener to get it started
        jmsConfigListenerProvider.get();
    }
    public void initializeBroker () throws ConnectorRuntimeException
    {
            String module = ConnectorConstants.DEFAULT_JMS_ADAPTER;
            String loc = ConnectorsUtil.getSystemModuleLocation(module);
            ConnectorRuntime connectorRuntime = connectorRuntimeProvider.get();
            connectorRuntime.createActiveResourceAdapter(loc, module, null);
    }
    private boolean eagerStartupRequired(){
        JmsService jmsService = getJmsService();
        if(jmsService == null) return false;
        String integrationMode =jmsService.getType();
        List <JmsHost> jmsHostList = jmsService.getJmsHost();
        if (jmsHostList == null) return false;

        String defaultJmsHostName = jmsService.getDefaultJmsHost();
        JmsHost defaultJmsHost = null;
        for (JmsHost host : jmsHostList){
            if(defaultJmsHostName != null && defaultJmsHostName.equals(host.getName())) {
                    defaultJmsHost = host;
                break;
            }
        }
        if(defaultJmsHost == null && jmsHostList.size() >0)  {
            defaultJmsHost = jmsHostList.get(0);
        }
        boolean lazyInit = false;
        if (defaultJmsHost != null)
                lazyInit = Boolean.parseBoolean(defaultJmsHost.getLazyInit());


        //we don't manage lifecycle of remote brokers
        if(REMOTE.equals(integrationMode))
                return false;

         //Initialize on demand is currently enabled based on a system property
        String jmsInitializeOnDemand = System.getProperty(JMS_INITIALIZE_ON_DEMAND);
        //if the system property is true, don't do eager startup
        if ("true".equals(jmsInitializeOnDemand))
            return false;

        if (EMBEDDED.equals(integrationMode) && (!lazyInit))
            return true;

        //local broker has eager startup by default
        if(LOCAL.equals(integrationMode))
            return true;


        return false;
    }

    private JmsService getJmsService() {
        return config.getExtensionByType(JmsService.class);
    }
}
