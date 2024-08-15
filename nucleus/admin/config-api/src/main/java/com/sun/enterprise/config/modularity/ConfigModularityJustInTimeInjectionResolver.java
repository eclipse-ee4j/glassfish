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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.DomainExtension;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * create the default ejb-container configbean when non exists and an injection point requires it.
 *
 * @author Masoud Kalali
 */
@Singleton
@Service
public class ConfigModularityJustInTimeInjectionResolver implements JustInTimeInjectionResolver {

    @Inject
    private DynamicConfigurationService dcs;
    @Inject
    private ServiceLocator locator;
    @Inject
    private Config config;
    @Inject
    private Domain domain;

    @Override
    public boolean justInTimeResolution(Injectee injectee) {
        if (injectee == null || injectee.isOptional())
            return false;
        Class configBeanType;
        try {
            configBeanType = (Class) injectee.getRequiredType();
        } catch (Exception ex) {
            return false;
        }
        if (!ConfigExtension.class.isAssignableFrom(configBeanType) && !DomainExtension.class.isAssignableFrom(configBeanType)) {
            return false;
        }
        if (!isInjectionSupported(configBeanType))
            return false;

        if (domain == null) {
            return false;
        }
        if (ConfigExtension.class.isAssignableFrom(configBeanType)) {
            if (config == null) {
                config = locator.getService(Config.class, ServerEnvironmentImpl.DEFAULT_INSTANCE_NAME);
            }
            ConfigBeanProxy pr = config.getExtensionByType(configBeanType);
            return pr != null;

        } else if (DomainExtension.class.isAssignableFrom(configBeanType)) {
            ConfigBeanProxy pr = domain.getExtensionByType(configBeanType);
            return pr != null;
        }
        return false;

    }

    //Let's check if we support automatic creation of this type or not.
    //This method will go away eventually when we are done with supporting all types.
    private boolean isInjectionSupported(Class c) {
        return true;
    }
}
