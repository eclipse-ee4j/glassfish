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

package org.glassfish.loadbalancer.upgrade;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.loadbalancer.admin.cli.LbLogUtil;
import org.glassfish.loadbalancer.config.LoadBalancer;
import org.glassfish.loadbalancer.config.LoadBalancers;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * Upgrade load-balancer config from v2 to v3
 *
 * @author Kshitiz Saxena
 */
@Service(name = "loadbalancerConfigUpgrade")
public class LoadBalancerConfigUpgrade implements PostConstruct,
        ConfigurationUpgrade {

    @Inject
    Domain domain;
    public static final String DEVICE_HOST_PROPERTY = "device-host";
    public static final String DEVICE_ADMIN_PORT_PROPERTY = "device-admin-port";

    @Override
    public void postConstruct() {
        updateLoadBalancerElements();
    }

    private void updateLoadBalancerElements() {
        LoadBalancers loadBalancers = domain.getExtensionByType(LoadBalancers.class);
        if(loadBalancers == null){
            return;
        }

        List<LoadBalancer> loadBalancerList =
                loadBalancers.getLoadBalancer();

        for (LoadBalancer loadBalancer : loadBalancerList) {
            try {
                ConfigSupport.apply(new LoadBalancerConfigCode(), loadBalancer);
            } catch (TransactionFailure ex) {
                String msg = LbLogUtil.getStringManager().getString(
                        "ErrorDuringUpgrade", loadBalancer.getName(),
                        ex.getMessage());
                Logger.getAnonymousLogger().log(Level.SEVERE, msg);
                if (Logger.getAnonymousLogger().isLoggable(Level.FINE)) {
                    Logger.getAnonymousLogger().log(Level.FINE,
                            "Exception during upgrade operation", ex);
                }
            }
        }
    }

    private static class LoadBalancerConfigCode implements
            SingleConfigCode<LoadBalancer> {

        @Override
        public Object run(LoadBalancer loadBalancer) throws
                PropertyVetoException, TransactionFailure {
            List<Property> propertyList = loadBalancer.getProperty();
            Property deviceHostProperty = loadBalancer.getProperty(
                    DEVICE_HOST_PROPERTY);
            if (deviceHostProperty != null) {
                propertyList.remove(deviceHostProperty);
                loadBalancer.setDeviceHost(deviceHostProperty.getValue());
            } else {
                String msg = LbLogUtil.getStringManager().getString(
                        "DeviceHostNotFound", loadBalancer.getName());
                Logger.getAnonymousLogger().log(Level.SEVERE, msg);
                loadBalancer.setDeviceHost("localhost");
            }

            Property devicePortProperty = loadBalancer.getProperty(
                    DEVICE_ADMIN_PORT_PROPERTY);
            if (devicePortProperty != null) {
                propertyList.remove(devicePortProperty);
                loadBalancer.setDevicePort(devicePortProperty.getValue());
            } else {
                String msg = LbLogUtil.getStringManager().getString(
                        "DevicePortNotFound", loadBalancer.getName());
                Logger.getAnonymousLogger().log(Level.SEVERE, msg);
                loadBalancer.setDevicePort("443");
            }

            String autoApplyEnabled = loadBalancer.getAutoApplyEnabled();
            if (autoApplyEnabled != null) {
                loadBalancer.setAutoApplyEnabled(null);
                if (Boolean.parseBoolean(autoApplyEnabled)) {
                    String msg = LbLogUtil.getStringManager().getString(
                            "AutoApplyEnabled", loadBalancer.getName());
                    Logger.getAnonymousLogger().log(Level.WARNING, msg);
                }
            }
            return loadBalancer;
        }
    }
}
