/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.test.example;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AliasDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorBuilder;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigInjector;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigurationPopulator;
import org.jvnet.hk2.config.DomDecorator;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.Populator;
import org.jvnet.hk2.config.Transactions;

import jakarta.inject.Singleton;

/**
 * TODO:  This should be done via auto-depends (via Service and contract
 * and all that).  However, since those don't work yet with the new
 * API, we must code this up by hand.
 *
 * @author jwells
 *
 */
public class ConfigModule {

    ServiceLocator serviceLocator;

    public ConfigModule(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    /**
     * Configures the HK2 instance
     *
     * @param configurator
     */
    public void configure(DynamicConfiguration configurator) {

//        configurator.bind(
//                BuilderHelper.link(XmlPopulator.class).
//                        to(Populator.class).
//                        in(Singleton.class.getName()).
//                        build());

        configurator.bind(BuilderHelper.link(ConfigSupport.class)
                .in(Singleton.class.getName())
                .build());
        configurator.bind(BuilderHelper.link(Transactions.class)
                .in(Singleton.class.getName())
                .build());
        configurator.bind(BuilderHelper.link(SimpleConfigBeanDomDecorator.class)
                .to(DomDecorator.class).in(Singleton.class.getName())
                .build());
        configurator.bind(BuilderHelper.link(ConfigurationPopulator.class)
                .in(Singleton.class.getName())
                .build());
        configurator.bind(BuilderHelper.link(DummyPopulator.class)
                .to(Populator.class).in(Singleton.class.getName())
                .build());
        configurator.addActiveDescriptor(ConfigErrorService.class);
        bindInjector(configurator, "simple-connector",           SimpleConnector.class,          SimpleConnectorInjector.class);
        bindInjector(configurator, "ejb-container-availability", EjbContainerAvailability.class, EjbContainerAvailabilityInjector.class);
        bindInjector(configurator, "web-container-availability", WebContainerAvailability.class, WebContainerAvailabilityInjector.class);
        bindInjector(configurator, "generic-container",          GenericContainer.class,         GenericContainerInjector.class);
        bindInjector(configurator, "generic-config",             GenericConfig.class,            GenericConfigInjector.class);

    }

    private void bindInjector(DynamicConfiguration configurator, String elementName, Class contract, final Class clz) {
        DescriptorBuilder db = BuilderHelper.link(clz).
                to(ConfigInjector.class).to(InjectionTarget.class).to(contract).
                in(Singleton.class.getName()).
                qualifiedBy(clz.getAnnotation(InjectionTarget.class)).
                named(elementName).andLoadWith(new MyHk2Loader(clz.getClassLoader()));

        String metaData = ((Service) clz.getAnnotation(Service.class)).metadata();
        Map<String, List<String>> metaMap = new HashMap<>();
        for (StringTokenizer st = new StringTokenizer(metaData, ","); st.hasMoreTokens(); ) {
            String tok = st.nextToken();
            int index = tok.indexOf('=');
            if (index > 0) {
                String key = tok.substring(0, index);
                String value = tok.substring(index + 1);
                List<String> lst = metaMap.get(key);
                if (lst == null) {
                    lst = new LinkedList<>();
                    metaMap.put(key, lst);
                }
                lst.add(value);
                //System.out.println("**     Added Metadata: " + tok.substring(0, index) + "  : " + tok.substring(index+1));
            }
            //db.andLoadWith(new MyHk2Loader(clz.getClassLoader()));
        }

        for (String key : metaMap.keySet()) {
            db.has(key, metaMap.get(key));
        }
        ActiveDescriptor desc = configurator.bind(db.build());
        configurator.bind(new AliasDescriptor(serviceLocator, desc, InjectionTarget.class.getName(), contract.getName()));
        System.out.println("**Successfully bound an alias descriptor for: " + elementName);
    }

    class MyHk2Loader
        implements HK2Loader {


        private final ClassLoader loader;

        MyHk2Loader(ClassLoader cl) {
            loader = cl;
        }

        @Override
        public Class<?> loadClass(final String className) throws MultiException {
            try {
                return loader.loadClass(className);
            } catch (Exception ex) {
                throw new MultiException(ex);
            }
        }
    }

}
