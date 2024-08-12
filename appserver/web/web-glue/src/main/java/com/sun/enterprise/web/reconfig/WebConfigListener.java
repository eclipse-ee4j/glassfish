/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.reconfig;

import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.v3.services.impl.MapperUpdateListener;
import com.sun.enterprise.web.WebContainer;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.LifecycleException;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.glassfish.web.LogFacade;
import org.glassfish.web.config.serverbeans.ManagerProperties;
import org.glassfish.web.config.serverbeans.WebContainerAvailability;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;


/**
 * Web container dynamic configuration handler
 *
 * @author amyroh
 */
public class WebConfigListener implements ConfigListener, MapperUpdateListener {

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    public HttpService httpService;

    @Inject @Optional
    public ManagerProperties managerProperties;

    @Inject @Optional
    public List<Property> property;

    private WebContainer container;

    private Logger logger;

    private NetworkConfig networkConfig;

    /**
     * Set the Web Container for this ConfigListener.
     * Must be set in order to perform dynamic configuration
     * @param container the container to be set
     */
    public void setContainer(WebContainer container) {
        this.container = container;
    }

    public void setLogger(Logger logger) {
        synchronized (this) {
            this.logger = logger;
        }
    }

    public void setNetworkConfig(NetworkConfig config) {
        this.networkConfig = config;
    }

    /**
     * Handles HttpService change events
     * @param events the PropertyChangeEvent
     */
    @Override
    public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new Changed() {
            @Override
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> tClass, T t) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, LogFacade.CHANGE_INVOKED, new Object[] {type, tClass, t});
                }
                try {
                    if (tClass == HttpService.class) {
                        container.updateHttpService((HttpService) t);
                    } else if (tClass == NetworkListener.class) {
                        if (type==TYPE.ADD) {
                            container.addConnector((NetworkListener) t, httpService, true);
                        } else if (type==TYPE.REMOVE) {
                            container.deleteConnector((NetworkListener) t);
                        } else if (type==TYPE.CHANGE) {
                            container.updateConnector((NetworkListener) t, httpService);
                        }
                    } else if (tClass == VirtualServer.class) {
                        if (type==TYPE.ADD) {
                            container.createHost((VirtualServer) t, httpService, null);
                            container.loadDefaultWebModule((VirtualServer) t);
                        } else if (type==TYPE.REMOVE) {
                            container.deleteHost(httpService);
                        } else if (type==TYPE.CHANGE) {
                            container.updateHost((VirtualServer)t);
                        }
                    } else if (tClass == AccessLog.class) {
                        container.updateAccessLog(httpService);
                    } else if (tClass == ManagerProperties.class) {
                        return new NotProcessed("ManagerProperties requires restart");
                    } else if (tClass == WebContainerAvailability.class ||
                            tClass == AvailabilityService.class) {
                        // container.updateHttpService handles SingleSignOn valve configuration
                        container.updateHttpService(httpService);
                    } else if (tClass == NetworkListeners.class) {
                        // skip updates
                    } else if (tClass == Property.class) {
                        ConfigBeanProxy config = ((Property)t).getParent();
                        if (config instanceof HttpService) {
                            container.updateHttpService((HttpService)config);
                        } else if (config instanceof VirtualServer) {
                            container.updateHost((VirtualServer)config);
                        } else if (config instanceof NetworkListener) {
                            container.updateConnector((NetworkListener)config, httpService);
                        } else {
                            container.updateHttpService(httpService);
                        }
                    } else if (tClass == SystemProperty.class) {
                        if (((SystemProperty)t).getName().endsWith("LISTENER_PORT")) {
                            for (NetworkListener listener : networkConfig.getNetworkListeners().getNetworkListener()) {
                                if (listener.getPort().equals(((SystemProperty)t).getValue())) {
                                    container.updateConnector(listener, httpService);
                                }
                            }
                        }
                    } else if (tClass == JavaConfig.class) {
                        JavaConfig jc = (JavaConfig) t;
                        final List<String> jvmOptions = new ArrayList<String>(jc.getJvmOptions());
                        for (String jvmOption : jvmOptions) {
                            if (jvmOption.startsWith("-DjvmRoute=")) {
                                container.updateJvmRoute(httpService, jvmOption);
                            }
                        }
                    } else {
                        // Ignore other unrelated events
                    }
                } catch (LifecycleException le) {
                    logger.log(Level.SEVERE, LogFacade.EXCEPTION_WEB_CONFIG, le);
                }
                return null;
            }
        }
        , logger);
    }

    @Override
    public void update(HttpService httpService, NetworkListener httpListener,
            Mapper mapper) {
        container.updateMapper(httpService, httpListener, mapper);
    }
}
