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

package org.glassfish.kernel.config;

import com.sun.enterprise.config.serverbeans.Config;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamReader;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigParser;
import org.glassfish.api.admin.config.Container;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * @author Jerome Dochez
 * @author Vivek Pandey
 */
@Service
public class DefaultConfigParser implements ConfigParser {

    @Inject @Named( ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    Logger logger = KernelLoggerInfo.getLogger();

    @Override
    public <T extends Container> T parseContainerConfig(ServiceLocator habitat, final URL configuration, Class<T> configType) throws IOException {
        // I don't use the GlassFish document here as I don't need persistence
        final DomDocument doc = new DomDocument<GlassFishConfigBean>(habitat) {
                @Override
            public Dom make(final ServiceLocator habitat, XMLStreamReader xmlStreamReader, GlassFishConfigBean dom, ConfigModel configModel) {
                // by default, people get the translated view.
                return new GlassFishConfigBean(habitat,this, dom, configModel, xmlStreamReader);
            }
        };

        // add the new container configuration to the server config
        final T container = doc.getRoot().createProxy(configType);

        try {
            ConfigSupport.apply(new SingleConfigCode<Config>() {
                @Override
                public Object run(Config config) throws PropertyVetoException, TransactionFailure {
                    config.getContainers().add(container);
                    return null;
                }
            }, config);
        } catch(TransactionFailure e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.exceptionAddContainer, e);
        }

        return  container;
    }
}
