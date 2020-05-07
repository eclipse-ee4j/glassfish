/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.monitor;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.logging.LogDomains;
import org.glassfish.server.ServerEnvironmentImpl;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import jakarta.inject.Singleton;
import org.jvnet.hk2.config.*;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;

@Service
@Singleton
public class PoolMonitoringLevelListener implements PostConstruct, PreDestroy, ConfigListener {

    @Inject
    private ServerEnvironmentImpl serverEnvironment;

    @Inject
    private Domain domain;

    private ModuleMonitoringLevels monitoringLevel;

    private boolean jdbcPoolMonitoringEnabled ;
    private boolean connectorPoolMonitoringEnabled;

    private static final Logger _logger = LogDomains.getLogger(PoolMonitoringLevelListener.class, LogDomains.RSR_LOGGER);

    public void postConstruct() {
        String instanceName = serverEnvironment.getInstanceName();
        Server server = domain.getServerNamed(instanceName);
        Config config = server.getConfig();
        if (config != null) {
            MonitoringService monitoringService = config.getMonitoringService();
            if (monitoringService != null) {
                ModuleMonitoringLevels monitoringLevel = monitoringService.getModuleMonitoringLevels();
                if (monitoringLevel != null) {
                    this.monitoringLevel = monitoringLevel;
                    ObservableBean bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy) monitoringLevel);
                    bean.addListener(this);
                    jdbcPoolMonitoringEnabled = !monitoringLevel.getJdbcConnectionPool().equalsIgnoreCase("OFF");
                    connectorPoolMonitoringEnabled = !monitoringLevel.getConnectorConnectionPool().equalsIgnoreCase("OFF");
                }
            }
        }
    }

    public void preDestroy() {
        if(monitoringLevel != null){
            ObservableBean bean = (ObservableBean) ConfigSupport.getImpl((ConfigBeanProxy)monitoringLevel);
            bean.removeListener(this);
        }
    }

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new PropertyChangeHandler(events), _logger);
    }

    class PropertyChangeHandler implements Changed {

            private PropertyChangeHandler(PropertyChangeEvent[] events) {
            }

            /**
             * Notification of a change on a configuration object
             *
             * @param type            type of change : ADD mean the changedInstance was added to the parent
             *                        REMOVE means the changedInstance was removed from the parent, CHANGE means the
             *                        changedInstance has mutated.
             * @param changedType     type of the configuration object
             * @param changedInstance changed instance.
             */
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                NotProcessed np = null;

                    switch (type) {
                        case CHANGE:
                            if(_logger.isLoggable(Level.FINE)) {
                                _logger.fine("A " + changedType.getName() + " was changed : " + changedInstance);
                            }
                            np = handleChangeEvent(changedInstance);
                            break;
                        default:
                    }
                    return np;
            }
            private <T extends ConfigBeanProxy> NotProcessed handleChangeEvent(T instance) {
                NotProcessed np = null;
                if(instance instanceof ModuleMonitoringLevels){
                    ModuleMonitoringLevels mml = (ModuleMonitoringLevels)instance;
                    connectorPoolMonitoringEnabled = !mml.getConnectorConnectionPool().equalsIgnoreCase("OFF");
                    jdbcPoolMonitoringEnabled = !mml.getJdbcConnectionPool().equalsIgnoreCase("OFF");
                }
                return np;
            }
        }

    public boolean getJdbcPoolMonitoringEnabled(){
        return jdbcPoolMonitoringEnabled;
    }

    public boolean getConnectorPoolMonitoringEnabled(){
        return connectorPoolMonitoringEnabled;
    }
}
