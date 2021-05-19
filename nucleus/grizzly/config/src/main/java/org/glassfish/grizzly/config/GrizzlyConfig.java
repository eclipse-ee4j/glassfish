/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LoggerInfo;

public class GrizzlyConfig {

    @LoggerInfo(subsystem = "NETCONFIG", description = "Network config", publish = false)
    private static final String LOGGER_NAME = "jakarta.enterprise.network.config";

    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
    private final NetworkConfig config;
    private final ServiceLocator serviceLocator;
    private final List<GrizzlyListener> listeners = new ArrayList<GrizzlyListener>();

    public static Logger logger() {
        return LOGGER;
    }

    public GrizzlyConfig(String file) {
        serviceLocator = Utils.getServiceLocator(file);
        config = serviceLocator.getService(NetworkConfig.class);
    }

    public NetworkConfig getConfig() {
        return config;
    }

    public List<GrizzlyListener> getListeners() {
        return listeners;
    }

    public void setupNetwork() throws IOException {
        validateConfig(config);
        synchronized (listeners) {
            for (final NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
                final GenericGrizzlyListener grizzlyListener = new GenericGrizzlyListener();
                grizzlyListener.configure(serviceLocator, listener);
                listeners.add(grizzlyListener);

                try {
                    grizzlyListener.start();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    public void shutdownNetwork() {
        synchronized (listeners) {
            for (GrizzlyListener listener : listeners) {
                try {
                    listener.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            listeners.clear();
        }
    }

    private static void validateConfig(NetworkConfig config) {
        for (final NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
            listener.findHttpProtocol();
        }
    }

    public void shutdown() throws IOException {
        synchronized (listeners) {
            for (GrizzlyListener listener : listeners) {
                try {
                    listener.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            listeners.clear();
        }
    }

    public static boolean toBoolean(String value) {
        if (value == null || value.length() == 0) {
            return false;
        }
        final String v = value.trim();
        return "true".equals(v) || "yes".equals(v) || "on".equals(v) || "1".equals(v);
    }
}
