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

package com.sun.enterprise.config.modularity.parser;

import com.sun.enterprise.config.modularity.ConfigModularityUtils;
import com.sun.enterprise.config.modularity.RankedConfigBeanProxy;
import com.sun.enterprise.config.modularity.annotation.HasNoDefaultConfiguration;
import com.sun.enterprise.config.modularity.customization.ConfigBeanDefaultValue;
import com.sun.enterprise.module.bootstrap.StartupContext;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;

/**
 * Containing shared functionalists between different derived classes like ConfigSnippetLoader and so on. Shared
 * functionalists includes finding, loading the configuration and creating a ConFigBean from it.
 *
 * @author Masoud Kalali
 */
@Service
public class ModuleConfigurationLoader<C extends ConfigBeanProxy, U extends ConfigBeanProxy> {

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private ConfigurationParser configurationParser;

    @Inject
    private ConfigModularityUtils configModularityUtils;

    private C extensionOwner;

    public <U extends ConfigBeanProxy> U createConfigBeanForType(Class<U> configExtensionType, C extensionOwner) throws TransactionFailure {
        this.extensionOwner = extensionOwner;
        if (configModularityUtils.hasCustomConfig(configExtensionType)) {
            addConfigBeanFor(configExtensionType);
        } else {
            if (configExtensionType.getAnnotation(HasNoDefaultConfiguration.class) != null) {
                return null;
            }
            final Class<U> childElement = configExtensionType;
            synchronized (configModularityUtils) {
                boolean oldIP = configModularityUtils.isIgnorePersisting();
                try {
                    configModularityUtils.setIgnorePersisting(true);
                    ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
                        @Override
                        public Object run(ConfigBeanProxy parent) throws PropertyVetoException, TransactionFailure {
                            U child = parent.createChild(childElement);
                            Dom unwrappedChild = Dom.unwrap(child);
                            unwrappedChild.addDefaultChildren();
                            configModularityUtils.getExtensions(parent).add(child);
                            return child;
                        }
                    }, extensionOwner);
                } finally {
                    configModularityUtils.setIgnorePersisting(oldIP);
                }
            }
        }

        return getExtension(configExtensionType, extensionOwner);
    }

    private <U extends ConfigBeanProxy> U getExtension(Class<U> configExtensionType, C extensionOwner) {
        List<U> extensions = configModularityUtils.getExtensions(extensionOwner);
        for (ConfigBeanProxy extension : extensions) {
            try {
                U configBeanInstance = configExtensionType.cast(extension);
                if (configBeanInstance instanceof ConfigExtension) {
                    ServiceLocatorUtilities.addOneDescriptor(serviceLocator, BuilderHelper.createConstantDescriptor(configBeanInstance,
                            ServerEnvironment.DEFAULT_INSTANCE_NAME, ConfigSupport.getImpl(configBeanInstance).getProxyType()));
                }
                return configBeanInstance;
            } catch (Exception e) {
                // ignore, not the right type.
            }
        }
        return null;
    }

    protected <U extends ConfigBeanProxy> void addConfigBeanFor(Class<U> extensionType) {
        if (!RankedConfigBeanProxy.class.isAssignableFrom(extensionType)) {
            if (getExtension(extensionType, extensionOwner) != null) {
                return;
            }
        }
        StartupContext context = serviceLocator.getService(StartupContext.class);
        List<ConfigBeanDefaultValue> configBeanDefaultValueList = configModularityUtils.getDefaultConfigurations(extensionType,
                configModularityUtils.getRuntimeTypePrefix(context));
        configurationParser.parseAndSetConfigBean(configBeanDefaultValueList);
    }

}
