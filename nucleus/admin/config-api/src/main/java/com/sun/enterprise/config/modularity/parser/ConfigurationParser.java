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
import com.sun.enterprise.config.modularity.customization.ConfigBeanDefaultValue;
import com.sun.enterprise.config.serverbeans.ConfigLoader;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import jakarta.inject.Inject;
import javax.xml.stream.XMLStreamReader;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a class to create the ConfigBeanProxy from the xml an xml snippet
 *
 * @author Bhakti Mehta
 * @author Masoud Kalali
 */
@Service
public class ConfigurationParser<C extends ConfigLoader> {

    private static final Logger LOG = ConfigApiLoggerInfo.getLogger();

    //TODO Until the TranslatedView issue is fixed this remain true.
    private static boolean replaceSystemProperties = false;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private ConfigModularityUtils configModularityUtils;

    /**
     * @param <T> the ConfigBeanProxy type we are looking for
     */
    public <T extends ConfigBeanProxy> void parseAndSetConfigBean(List<ConfigBeanDefaultValue> values) {

        ConfigParser configParser = new ConfigParser(serviceLocator);
        // I don't use the GlassFish document here as I don't need persistence
        final DomDocument doc = new DomDocument<GlassFishConfigBean>(serviceLocator) {
            @Override
            public Dom make(final ServiceLocator serviceLocator, XMLStreamReader xmlStreamReader, GlassFishConfigBean dom,
                    ConfigModel configModel) {
                return new GlassFishConfigBean(serviceLocator, this, dom, configModel, xmlStreamReader);
            }
        };

        //TODO requires rework to put all the changes that a service may introduce into one transaction
        //the solution is to put the loop into the apply method...  But it would be some fine amount of work
        for (final ConfigBeanDefaultValue configBeanDefaultValue : values) {
            final ConfigBeanProxy parent = configModularityUtils.getOwningObject(configBeanDefaultValue.getLocation());
            if (parent == null)
                continue;
            ConfigurationPopulator populator = null;
            if (replaceSystemProperties)
                try {
                    populator = new ConfigurationPopulator(configModularityUtils.replacePropertiesWithCurrentValue(
                            configBeanDefaultValue.getXmlConfiguration(), configBeanDefaultValue), doc, parent);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, ConfigApiLoggerInfo.CFG_EXT_ADD_FAILED, e);
                }
            else {
                //Check that parent is not null!
                populator = new ConfigurationPopulator(configBeanDefaultValue.getXmlConfiguration(), doc, parent);
            }
            populator.run(configParser);
            synchronized (configModularityUtils) {
                boolean oldValue = configModularityUtils.isIgnorePersisting();
                try {
                    Class configBeanClass = configModularityUtils.getClassForFullName(configBeanDefaultValue.getConfigBeanClassName());
                    final ConfigBeanProxy pr = doc.getRoot().createProxy(configBeanClass);
                    configModularityUtils.setIgnorePersisting(true);
                    ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
                        public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {
                            configModularityUtils.setConfigBean(pr, configBeanDefaultValue, param);
                            return param;
                        }
                    }, parent);
                } catch (TransactionFailure e) {
                    LOG.log(Level.SEVERE, ConfigApiLoggerInfo.CFG_EXT_ADD_FAILED, e);
                } finally {
                    configModularityUtils.setIgnorePersisting(oldValue);
                }
            }
        }
    }
}
