/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.utils;

import java.security.Principal;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamReader;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Transactions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Super class for all config-api related tests, give access to a configured habitat
 */
public abstract class ConfigApiTest {

    public static final Logger logger = Logger.getAnonymousLogger();

    private static class PrincipalImpl implements Principal {
        private final String name;

        private PrincipalImpl(final String name) {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }


    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
    }

    /**
     * Returns a configured Habitat with the configuration.
     *
     * @return a configured Habitat
     */
    public ServiceLocator getHabitat() {
        ServiceLocator habitat = Utils.instance.getHabitat(this);
        assertNotNull(habitat.getService(Transactions.class),
            "Transactions service from Configuration subsystem is null");
        return habitat;
    }

    public ServiceLocator getBaseServiceLocator() {
        return getHabitat();
    }

    /**
     *  Override it when needed, see config-api/ConfigApiTest.java for example.
     */
    public DomDocument getDocument(ServiceLocator habitat) {
        TestDocument doc = habitat.getService(TestDocument.class);
        if (doc == null) {
            doc = new TestDocument(habitat);
        }
        return doc;
    }

    /**
     * Decorate the habitat after parsing.  This is called on the habitat
     * just after parsing of the XML file is complete.
     */
    public void decorate(ServiceLocator locator) {
        // override it
    }

    public static class TestDocument extends DomDocument<ConfigBean> {

        public TestDocument(ServiceLocator habitat) {
            super(habitat);
        }

        @Override
        public ConfigBean make(final ServiceLocator habitat, XMLStreamReader xmlStreamReader,
                ConfigBean dom, ConfigModel configModel) {
            return new ConfigBean(habitat,this, dom, configModel, xmlStreamReader);
        }
    }
}
