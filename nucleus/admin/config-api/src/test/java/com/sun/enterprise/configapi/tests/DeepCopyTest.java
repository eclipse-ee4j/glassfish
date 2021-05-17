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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import org.glassfish.config.support.ConfigurationPersistence;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hk2.config.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * Test the deepCopy feature of ConfigBeans.
 *
 * @author Jerome Dochez
 */
public class DeepCopyTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void configCopy() throws Exception {
        final Config config = getHabitat().getService(Config.class);
        Assert.assertNotNull(config);
        String configName = config.getName();
        final Config newConfig = (Config) ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
            @Override
            public Object run(ConfigBeanProxy parent) throws PropertyVetoException, TransactionFailure {
                return config.deepCopy(parent);
            }
        }, config.getParent());
        Assert.assertNotNull(newConfig);
        try {
            newConfig.setName("some-config");
        } catch(Exception e) {
            // I was expecting this...
        }
        ConfigSupport.apply(new SingleConfigCode<Config>() {
            @Override
            public Object run(Config wConfig) throws PropertyVetoException, TransactionFailure {
                wConfig.setName("some-config");
                return null;
            }
        }, newConfig);
        Assert.assertEquals(newConfig.getName(), "some-config");
        Assert.assertEquals(config.getName(), configName);

        // add it the parent
        ConfigSupport.apply(new SingleConfigCode<Configs>() {
            @Override
            public Object run(Configs wConfigs) throws PropertyVetoException, TransactionFailure {
                wConfigs.getConfig().add(newConfig);
                return null;
            }
        }, getHabitat().<Configs>getService(Configs.class));
        String resultingXML = save(document).toString();
        Assert.assertTrue("Expecting some-config, got " + resultingXML, resultingXML.contains("some-config"));
    }

    @Test
    public void parentingTest() throws Exception {

        final Config config = getHabitat().getService(Config.class);
        Assert.assertNotNull(config);
        String configName = config.getName();
        final Config newConfig = (Config) ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
            @Override
            public Object run(ConfigBeanProxy parent) throws PropertyVetoException, TransactionFailure {
                Config newConfig = (Config) config.deepCopy(parent);
                newConfig.setName("cloned-config");
                return newConfig;
            }
        }, config.getParent());
        Assert.assertNotNull(newConfig);

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
        Assert.assertEquals(original.getClass(), cloned.getClass());
        for (String elementName : original.getElementNames()) {
            ConfigModel.Property property = original.model.getElement(elementName);
            if (property != null && property.isLeaf()) continue;
            Dom originalChild = original.element(elementName);
            Dom clonedChild = cloned.element(elementName);
            if (originalChild==null && clonedChild==null) continue;
            Assert.assertNotNull(originalChild);
            Assert.assertNotNull(clonedChild);
            assertTypes(originalChild, clonedChild);
        }
    }

    private void assertParenting(Dom dom) {

        for (String elementName : dom.model.getElementNames()) {
            ConfigModel.Property property = dom.model.getElement(elementName);
            if (property.isLeaf()) continue;
            Dom child = dom.element(elementName);
            if (child==null) continue;
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Parent of " + child.model.targetTypeName + ":" + child.getKey() + " is " + child.parent().getKey() + " while I am " + dom.getKey());
            }
            logger.info("Parent of " + child.model.targetTypeName + ":" + child.getKey() + " is " +
                    child.parent().model.targetTypeName + ":" + child.parent().getKey() + " while I am " +
                    dom.model.targetTypeName + ":" + dom.getKey());

            Assert.assertEquals(dom, child.parent());
            assertParenting(child);
        }
    }

    final DomDocument document = getDocument(getHabitat());

    public OutputStream save(DomDocument doc) throws IOException, XMLStreamException {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        outStream.reset();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(outStream);
        doc.writeTo(new IndentingXMLStreamWriter(writer));
        writer.close();
        return outStream;
    }
}
