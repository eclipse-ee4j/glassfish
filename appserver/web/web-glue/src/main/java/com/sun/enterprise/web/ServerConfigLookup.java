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

package com.sun.enterprise.web;

import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.web.session.PersistenceType;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.web.LogFacade;
import org.glassfish.web.config.serverbeans.ManagerProperties;
import org.glassfish.web.config.serverbeans.SessionConfig;
import org.glassfish.web.config.serverbeans.SessionManager;
import org.glassfish.web.config.serverbeans.SessionProperties;
import org.glassfish.web.config.serverbeans.StoreProperties;
import org.glassfish.web.config.serverbeans.WebContainer;
import org.glassfish.web.config.serverbeans.WebContainerAvailability;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

@Service
@PerLookup
public class ServerConfigLookup {

    private static final Logger _logger = LogFacade.getLogger();


    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config configBean;

    @Inject
    private ClassLoaderHierarchy clh;


    /**
     * Get the session manager bean from domain.xml
     * return null if not defined or other problem
     */
    public SessionManager getInstanceSessionManager() {
        if (configBean == null) {
            return null;
        }

        WebContainer webContainerBean
            = configBean.getExtensionByType(WebContainer.class);
        if (webContainerBean == null) {
            return null;
        }

        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if (sessionConfigBean == null) {
            return null;
        }

        return sessionConfigBean.getSessionManager();
    }

    /**
     * Get the manager properties bean from domain.xml
     * return null if not defined or other problem
     */
    public ManagerProperties getInstanceSessionManagerManagerProperties() {

        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getManagerProperties();
    }

    /**
     * Get the store properties bean from domain.xml
     * return null if not defined or other problem
     */
    public StoreProperties getInstanceSessionManagerStoreProperties() {

        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getStoreProperties();
    }

    /**
     * Get the session properties bean from server.xml
     * return null if not defined or other problem
     */
    public SessionProperties getInstanceSessionProperties() {
        if (configBean == null) {
            return null;
        }

        WebContainer webContainerBean
            = configBean.getExtensionByType(WebContainer.class);
        if (webContainerBean == null) {
            return null;
        }

        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if (sessionConfigBean == null) {
            return null;
        }

        return sessionConfigBean.getSessionProperties();
    }


    /**
     * Get the availability-service element from domain.xml.
     * return null if not found
     */
    protected AvailabilityService getAvailabilityService() {
        return configBean.getAvailabilityService();
    }

    /**
     * Get the availability-enabled from domain.xml.
     * return false if not found
     */
    public boolean getAvailabilityEnabledFromConfig() {
        AvailabilityService as = this.getAvailabilityService();
        if (as == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, LogFacade.AVAILABILITY_SERVICE_NOT_DEFINED);
            }
            return false;
        }

        if (as.getAvailabilityEnabled() == null) {
            return false;
        } else {
            return toBoolean(as.getAvailabilityEnabled());
        }
    }

    /**
     * Geo the web-container-availability element from domain.xml.
     * return null if not found
     */
    private WebContainerAvailability getWebContainerAvailability() {
        AvailabilityService as = getAvailabilityService();
        return ((as != null)? as.getExtensionByType(WebContainerAvailability.class) : null);
    }

    /**
     * Get the String value of the property under web-container-availability
     * element from domain.xml whose name matches propName
     * return null if not found
     * @param propName
     */
    protected String getWebContainerAvailabilityPropertyString(
                String propName) {
        return getWebContainerAvailabilityPropertyString(propName, null);
    }

    /**
     * Get the String value of the property under web-container-availability
     * element from domain.xml whose name matches propName
     * return defaultValue if not found
     * @param propName
     */
    protected String getWebContainerAvailabilityPropertyString(
                String propName,
                String defaultValue) {
        WebContainerAvailability wcAvailabilityBean = getWebContainerAvailability();
        if (wcAvailabilityBean == null) {
            return defaultValue;
        }

        List<Property> props = wcAvailabilityBean.getProperty();
        if (props == null) {
            return defaultValue;
        }

        for (Property prop : props) {
            String name = prop.getName();
            String value = prop.getValue();
            if (name.equalsIgnoreCase(propName)) {
                return value;
            }
        }

        return defaultValue;
    }


    /**
     * Get the availability-enabled for the web container from domain.xml.
     * return inherited global availability-enabled if not found
     */
    public boolean getWebContainerAvailabilityEnabledFromConfig() {
        boolean globalAvailabilityEnabled = getAvailabilityEnabledFromConfig();
        WebContainerAvailability was = getWebContainerAvailability();
        if (was == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, LogFacade.WEB_CONTAINER_AVAILABILITY_NOT_DEFINED);
            }
            return false;
        }

        if (was.getAvailabilityEnabled() == null) {
            return globalAvailabilityEnabled;
        } else {
            return toBoolean(was.getAvailabilityEnabled());
        }
    }

    /**
     * Get the sso-failover-enabled boolean from domain.xml.
     */
    public boolean isSsoFailoverEnabledFromConfig() {
        WebContainerAvailability webContainerAvailabilityBean = getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return false;
        }
        if (webContainerAvailabilityBean.getSsoFailoverEnabled() == null) {
            return false;
        } else {
            return toBoolean(webContainerAvailabilityBean.getSsoFailoverEnabled());
        }
    }

    /**
     * Get the availability-enabled from domain.xml.
     * This takes into account:
     * global
     * web-container-availability
     * return false if not found
     */
    public boolean calculateWebAvailabilityEnabledFromConfig() {
        // global availability from <availability-service> element
        boolean globalAvailability = getAvailabilityEnabledFromConfig();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, LogFacade.GLOBAL_AVAILABILITY, globalAvailability);
        }

        // web container availability from <web-container-availability>
        // sub-element

        boolean webContainerAvailability =
            getWebContainerAvailabilityEnabledFromConfig();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, LogFacade.WEB_CONTAINER_AVAILABILITY, webContainerAvailability);
        }

        return globalAvailability && webContainerAvailability;
    }

    /**
     * Get the availability-enabled from domain.xml.
     * This takes into account:
     * global
     * web-container-availability
     * web-module (if stand-alone)
     * return false if not found
     */
    public boolean calculateWebAvailabilityEnabledFromConfig(WebModule ctx) {
        boolean waEnabled = calculateWebAvailabilityEnabledFromConfig();

        boolean webModuleAvailability = false;
        DeploymentContext dc = ctx.getWebModuleConfig().getDeploymentContext();
        if (dc != null) {
            DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
            if (params != null) {
                webModuleAvailability = params.availabilityenabled;
            }
        }


        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, LogFacade.WEB_MODULE_AVAILABILITY, webModuleAvailability);
        }
        return waEnabled && webModuleAvailability;
    }


    public boolean getAsyncReplicationFromConfig(WebModule ctx) {
        boolean asyncReplication = true;
        DeploymentContext dc = ctx.getWebModuleConfig().getDeploymentContext();
        if (dc != null) {
            DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
            if (params != null) {
                asyncReplication = params.asyncreplication;
            }

        }
        return asyncReplication;
    }

    /**
     * Get the persistenceType from domain.xml.
     * return null if not found
     */
    public PersistenceType getPersistenceTypeFromConfig() {
        String persistenceTypeString = null;
        PersistenceType persistenceType = null;

        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        persistenceTypeString = webContainerAvailabilityBean.getPersistenceType();

        if (persistenceTypeString != null) {
            persistenceType = PersistenceType.parseType(persistenceTypeString);
        }
        if (persistenceType != null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, LogFacade.PERSISTENCE_TYPE, persistenceType.getType());
            }
        } else {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, LogFacade.PERSISTENCE_TYPE_MISSING);
            }
        }

        return persistenceType;
    }

    /**
     * Get the persistenceFrequency from domain.xml.
     * return null if not found
     */
    public String getPersistenceFrequencyFromConfig() {
        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceFrequency();
    }

    /**
     * Get the persistenceScope from domain.xml.
     * return null if not found
     */
    public String getPersistenceScopeFromConfig() {
        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceScope();
    }

    public boolean getDisableJreplicaFromConfig() {
        WebContainerAvailability webContainerAvailabilityBean = getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return false;
        }
        return toBoolean(webContainerAvailabilityBean.getDisableJreplica());

    }

    /**
     * convert the input value to the appropriate Boolean value
     * if input value is null, return null
     */
    protected Boolean toBoolean(String value) {
        if (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("1")) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    /**
     * Loads the requested class using the Common Classloader
     *
     * @param className the name of the class to load
     *
     * @return the loaded class
     */
    Class loadClass(String className) throws Exception {
        return clh.getCommonClassLoader().loadClass(className);
    }
}
