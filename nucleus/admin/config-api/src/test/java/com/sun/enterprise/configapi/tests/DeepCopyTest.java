/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;
import org.jvnet.hk2.config.SingleConfigCode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the deepCopy feature of ConfigBeans.
 *
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class DeepCopyTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;
    @Inject
    private DomDocument document;

    @Test
    public void configCopy() throws Exception {
        final Config config = locator.getService(Config.class);
        assertNotNull(config);
        String configName = config.getName();
        final Config newConfig = (Config) ConfigSupport.apply(parent -> config.deepCopy(parent), config.getParent());
        assertNotNull(newConfig);
        assertThrows(IllegalStateException.class, () -> newConfig.setName("some-config"));
        SingleConfigCode<Config> configCode = cfg -> {
            cfg.setName("some-config");
            return null;
        };
        ConfigSupport.apply(configCode, newConfig);
        assertEquals("some-config", newConfig.getName());
        assertEquals(configName, config.getName());

        // add it the parent
        SingleConfigCode<Configs> configCodeParent = configs -> {
            configs.getConfig().add(newConfig);
            return null;
        };
        ConfigSupport.apply(configCodeParent, locator.<Configs>getService(Configs.class));
        String resultingXML = save(document);
        assertThat("Expecting some-config, got " + resultingXML, resultingXML, stringContainsInOrder("some-config"));
    }

    @Test
    public void parentingTest() throws Exception {

        final Config config = locator.getService(Config.class);
        assertNotNull(config);
        assertEquals("server-config", config.getName());
        SingleConfigCode<ConfigBeanProxy> configCode = configProxy -> {
            Config newConfig1 = (Config) config.deepCopy(configProxy);
            newConfig1.setName("cloned-config");
            return newConfig1;
        };
        final Config newConfig = (Config) ConfigSupport.apply(configCode, config.getParent());
        assertNotNull(newConfig);

        // now let's check the parents are correct.
        Dom original = Dom.unwrap(config);
        Dom cloned = Dom.unwrap(newConfig);

        assertTypes(original, cloned);

        logger.info("types equality passed");
        assertParenting(original);
        assertParenting(cloned);
    }

    private void assertTypes(Dom original, Dom cloned) {
        logger.info(original.model.getTagName()+":" + original.getKey() + ":" + original.getClass().getSimpleName() +
                " and " + cloned.model.getTagName()+":" + cloned.getKey() + ":" + cloned.getClass().getSimpleName());
        assertEquals(original.getClass(), cloned.getClass());
        for (String elementName : original.getElementNames()) {
            ConfigModel.Property property = original.model.getElement(elementName);
            if (property != null && property.isLeaf()) {
                continue;
            }
            Dom originalChild = original.element(elementName);
            Dom clonedChild = cloned.element(elementName);
            if (originalChild==null && clonedChild==null) {
                continue;
            }
            assertNotNull(originalChild);
            assertNotNull(clonedChild);
            assertTypes(originalChild, clonedChild);
        }
    }

    private void assertParenting(Dom dom) {
        for (String elementName : dom.model.getElementNames()) {
            ConfigModel.Property property = dom.model.getElement(elementName);
            if (property.isLeaf()) {
                continue;
            }
            Dom child = dom.element(elementName);
            if (child==null) {
                continue;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Parent of " + child.model.targetTypeName + ":" + child.getKey() + " is "
                    + child.parent().getKey() + " while I am " + dom.getKey());
            }
            logger.info("Parent of " + child.model.targetTypeName + ":" + child.getKey() + " is " +
                    child.parent().model.targetTypeName + ":" + child.parent().getKey() + " while I am " +
                    dom.model.targetTypeName + ":" + dom.getKey());

            assertEquals(dom, child.parent());
            assertParenting(child);
        }
    }

    private String save(DomDocument doc) throws Exception {
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(outStream);
            try {
                doc.writeTo(new IndentingXMLStreamWriter(writer));
            } finally {
                writer.close();
            }
            return outStream.toString();
        }
    }
}
