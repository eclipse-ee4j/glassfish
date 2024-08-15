/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.AddingDefaultInstanceIndexFor;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.removingDefaultInstanceIndexFor;

/**
 * Listens for changes to the Config for the current server and adds an index for the name
 * ServerEnvironment.DEFAULT_INSTANCE_NAME to any objects that are added.
 */
@Service
@RunLevel(mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING, value = StartupRunLevel.VAL)
public final class ConfigConfigBeanListener implements ConfigListener {

    @Inject
    private ServiceLocator habitat;
    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    static final Logger logger = ConfigApiLoggerInfo.getLogger();

    /* force serial behavior; don't allow more than one thread to make a mess here */
    @Override
    public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        for (PropertyChangeEvent e : events) {
            // ignore all events for which the source isn't the Config
            if (e.getSource().getClass() != config.getClass()) {
                continue;
            }

            // remove the DEFAULT_INSTANCE_NAME entry for an old value
            Object ov = e.getOldValue();
            if (ov instanceof ConfigBeanProxy) {
                ConfigBeanProxy ovbp = (ConfigBeanProxy) ov;
                logger.log(Level.FINE, removingDefaultInstanceIndexFor, ConfigSupport.getImpl(ovbp).getProxyType().getName());
                ServiceLocatorUtilities.removeFilter(habitat, BuilderHelper.createNameAndContractFilter(
                        ConfigSupport.getImpl(ovbp).getProxyType().getName(), ServerEnvironment.DEFAULT_INSTANCE_NAME));
            }

            // add the DEFAULT_INSTANCE_NAME entry for a new value
            Object nv = e.getNewValue();
            if (nv instanceof ConfigBean) {
                ConfigBean nvb = (ConfigBean) nv;
                ConfigBeanProxy nvbp = nvb.getProxy(nvb.getProxyType());
                logger.log(Level.FINE, AddingDefaultInstanceIndexFor, nvb.getProxyType().getName());
                ServiceLocatorUtilities.addOneConstant(habitat, nvbp, ServerEnvironment.DEFAULT_INSTANCE_NAME, nvb.getProxyType());
            }
        }
        return null;
    }
}
