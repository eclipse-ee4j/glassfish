/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.web.admin.monitor;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigurationException;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name = "http-service")
@Singleton
public class HttpServiceStatsProviderBootstrap implements PostConstruct {

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    public void postConstruct() {

        if (config == null) {
            Object[] params = {VirtualServerInfoStatsProvider.class.getName(),
                    HttpServiceStatsProvider.class.getName(),
                    "http service", "virtual server"};
            logger.log(Level.SEVERE, LogFacade.UNABLE_TO_REGISTER_STATS_PROVIDERS, params);
            throw new ConfigurationException(rb.getString(LogFacade.NULL_CONFIG));
        }

        HttpService httpService = config.getHttpService();
        for (VirtualServer vs : httpService.getVirtualServer()) {
            StatsProviderManager.register(
                    "http-service",
                    PluginPoint.SERVER,
                    "http-service/" + vs.getId(),
                    new VirtualServerInfoStatsProvider(vs));
            StatsProviderManager.register(
                    "http-service",
                    PluginPoint.SERVER,
                    "http-service/" + vs.getId() + "/request",
                    new HttpServiceStatsProvider(vs.getId(), vs.getNetworkListeners(), config.getNetworkConfig()));
        }
    }
}
