/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.modularity.annotation.CustomConfiguration;
import com.sun.enterprise.config.modularity.customization.ConfigBeanDefaultValue;
import com.sun.enterprise.config.modularity.parser.ConfigurationParser;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.DomainExtension;
import com.sun.enterprise.module.bootstrap.StartupContext;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.lang.annotation.Annotation;
import java.util.List;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Masoud Kalali
 */
@Service
public class ConfigBeanInstaller implements PostConstruct {

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    StartupContext startupContext;

    @Inject
    private ConfigurationParser configurationParser;

    @Inject
    private ConfigModularityUtils configModularityUtils;

    @Inject
    private Domain domain;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Override
    public void postConstruct() {
        Class cbc = this.getClass().getDeclaringClass();
        if (cbc != null) {
            Annotation ann = cbc.getAnnotation(CustomConfiguration.class);
            if (ann != null) {
                applyConfigIfNeeded(cbc);
            }
        }
    }

    private void applyConfigIfNeeded(Class clz) {
        //TODO find a way to get the parent and do complete check for all config beans type rather than just these two
        if (!RankedConfigBeanProxy.class.isAssignableFrom(clz)) {
            if (DomainExtension.class.isAssignableFrom(clz) && (domain.getExtensionByType(clz) != null)) {
                return;
            }
            if (ConfigExtension.class.isAssignableFrom(clz) && (config.getExtensionByType(clz) != null)) {
                return;
            }
        }

        List<ConfigBeanDefaultValue> configBeanDefaultValueList = configModularityUtils.getDefaultConfigurations(clz,
                configModularityUtils.getRuntimeTypePrefix(startupContext));
        configurationParser.parseAndSetConfigBean(configBeanDefaultValueList);
    }
}
