/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.modularity;

import com.sun.enterprise.config.modularity.parser.ModuleConfigurationLoader;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;

import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogHelper;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigExtensionHandler;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * This is to integrate the whole getExtensionByType with the config modularity irrelevant of the invocation point. More
 * explanation at Config Modularity one pager and
 *
 * @author Masoud Kalali
 */
@Service(name = "basic-config-extension-handler")
public class ExtensionPatternInvocationImpl implements ConfigExtensionHandler {

    private static final Logger LOG = ConfigApiLoggerInfo.getLogger();

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    private ModuleConfigurationLoader moduleConfigurationLoader;

    @Inject
    private ConfigModularityUtils configModularityUtils;

    @Override
    public ConfigBeanProxy handleExtension(Object owner, Class ownerType, Object[] params) {
        if (((Class) params[0]).getName().equals("com.sun.enterprise.config.serverbeans.SystemProperty"))
            return null;
        ConfigBeanProxy configExtension = null;
        List<ConfigBeanProxy> extensions = configModularityUtils.getExtensions(((ConfigBean) owner).createProxy(ownerType));
        for (ConfigBeanProxy extension : extensions) {
            try {
                configExtension = (ConfigBeanProxy) ((Class) params[0]).cast(extension);
                return configExtension;
            } catch (Exception e) {
                // ignore, not the right type.
            }
        }

        try {
            ConfigBeanProxy pr = ((ConfigBean) owner).createProxy(ownerType);
            ConfigBeanProxy returnValue = moduleConfigurationLoader.createConfigBeanForType((Class) params[0], pr);
            return returnValue;
        } catch (TransactionFailure transactionFailure) {
            LogHelper.log(LOG, Level.INFO, "Cannot get extension type {0} for {1}.", transactionFailure,
                    new Object[] { owner.getClass().getName(), ownerType.getName() });
            return null;
        }
    }
}
