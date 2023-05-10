/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.test;


import java.net.URL;
import java.util.List;
import java.util.Random;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.test.example.ConfigModule;
import org.jvnet.hk2.config.test.example.GenericConfig;
import org.jvnet.hk2.config.test.example.GenericContainer;
import org.jvnet.hk2.config.test.example.SimpleConnector;
import org.jvnet.hk2.config.test.example.SimpleDocument;
import org.jvnet.hk2.config.test.example.WebContainerAvailability;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigDisposalTest {
    private final static String TEST_NAME = "ConfigDisposal";
    private final static Random RANDOM = new Random();

    private ServiceLocator locator;

    @BeforeEach
    public void before() {
        String testName = TEST_NAME + RANDOM.nextInt();

        locator = ServiceLocatorFactory.getInstance().create(testName);
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        new ConfigModule(locator).configure(config);

        config.commit();
        parseDomainXml();
    }

    @AfterEach
    public void after() {
        ServiceLocatorFactory.getInstance().destroy(locator);
        locator = null;
    }

    public void parseDomainXml() {
        final ConfigParser parser = new ConfigParser(locator);
        final URL url = ConfigDisposalTest.class.getResource("/domain.xml");
        assertNotNull(url);
        final DomDocument<?> doc = parser.parse(url, new SimpleDocument(locator));
        assertNotNull(doc);
    }

    @Test
    public void testDisposedNestedAndNamed() throws TransactionFailure {
        SimpleConnector sc = locator.getService(SimpleConnector.class);
        assertEquals(1, sc.getExtensions().size(), "Extensions");
        assertEquals(2, sc.getExtensions().get(0).getExtensions().size(), "Nested children");

        SingleConfigCode<SimpleConnector> configCode = sc1 -> {
            List<GenericContainer> extensions = sc1.getExtensions();
            GenericContainer child = extensions.get(extensions.size() - 1);
            extensions.remove(child);
            return child;
        };
        ConfigSupport.apply(configCode, sc);

        assertAll(
            () -> assertThat("Removed extensions", sc.getExtensions(), hasSize(0)),
            // NOTE, habitat.getService(GenericConfig.class) creates new instance
            //       if not all instances of GenericConfig descriptors are removed
            () -> assertNull(locator.getService(GenericContainer.class), "GenericContainer descriptor still has " +
                locator.getDescriptors(BuilderHelper.createContractFilter(GenericContainer.class.getName()))),
            () -> assertNull(locator.getService(GenericConfig.class, "test"), "GenericConfig descriptor test still has " +
                locator.getDescriptors(BuilderHelper.createContractFilter(GenericConfig.class.getName()))),
            () -> assertNull(locator.getService(GenericConfig.class), "GenericConfig descriptor still has " +
                locator.getDescriptors(BuilderHelper.createContractFilter(GenericConfig.class.getName())))
        );
        // assert with VisualVm there is no GenericContainer and GenericConfig instances with OQL query:
        // select x.implementation.toString() from org.jvnet.hk2.config.test.SimpleConfigBeanWrapper x
    }

    @Test
    public void testRemoveNamed() throws TransactionFailure {
        SimpleConnector sc = locator.getService(SimpleConnector.class);
        assertAll(
            () -> assertThat("Extensions", sc.getExtensions(), hasSize(1)),
            () -> assertThat("Nested children", sc.getExtensions().get(0).getExtensions(), hasSize(2))
        );

        GenericContainer extension = sc.getExtensions().get(0);

        SingleConfigCode<GenericContainer> configCode = container -> {
            List<GenericConfig> childExtensions = container.getExtensions();
            GenericConfig nestedChild = childExtensions.get(childExtensions.size() - 1);
            childExtensions.remove(nestedChild);
            return nestedChild;
        };
        ConfigSupport.apply(configCode, extension);

        assertAll(
            () -> assertThat("Removed Extensions", sc.getExtensions(), hasSize(1)),
            () -> assertNull(locator.getService(GenericConfig.class, "test2"), "Removed nested named child"),
            // make sure other elements are not removed
            () -> assertNotNull(locator.getService(GenericConfig.class, "test1"), "Nested named child"),
            () -> assertNotNull(locator.getService(GenericConfig.class, "test"), "Nested named grand child")
        );
    }

    @Test
    public void testRemovedOne() throws TransactionFailure {
        SimpleConnector connector = locator.getService(SimpleConnector.class);
        assertEquals(1, connector.getExtensions().size(), "Extensions");

        SingleConfigCode<SimpleConnector> configCode = sc -> {
            List<GenericContainer> extensions = sc.getExtensions();
            GenericContainer child = sc.createChild(GenericContainer.class);
            WebContainerAvailability grandchild = child.createChild(WebContainerAvailability.class);
            child.setWebContainerAvailability(grandchild);
            extensions.add(child);
            return child;
        };
        ConfigSupport.apply(configCode, connector);
        assertEquals(2, connector.getExtensions().size(), "Added extensions");

        SingleConfigCode<SimpleConnector> configCode2 = (SingleConfigCode<SimpleConnector>) sc -> {
            List<GenericContainer> extensions = sc.getExtensions();
            GenericContainer child = extensions.get(extensions.size() - 1);
            extensions.remove(child);
            return child;
        };
        ConfigSupport.apply(configCode2, connector);
        assertAll(
            () -> assertThat("Removed extensions", connector.getExtensions(), hasSize(1)),
            () -> assertNotNull(locator.getService(GenericConfig.class, "test1"), "Nested named child 1"),
            () -> assertNotNull(locator.getService(GenericConfig.class, "test"), "Nested named grand child"),
            () -> assertNotNull(locator.getService(GenericConfig.class, "test2"), "Nested named child 2"),
            () -> assertNotNull(locator.getService(GenericContainer.class), "GenericContainer Service")
        );
    }

    @Test
    public void testReplaceNode() throws TransactionFailure {
        SimpleConnector sc = locator.getService(SimpleConnector.class);
        assertEquals(1, sc.getExtensions().size(), "Eextensions");

        GenericContainer extension = sc.getExtensions().get(0);
        assertEquals(2, extension.getExtensions().size(), "Child extensions");
        GenericConfig nestedChild = extension.getExtensions().get(0);

        SingleConfigCode<GenericConfig> configCode = nestedChild1 -> {
            nestedChild1.setGenericConfig(null);
            GenericConfig newChild = nestedChild1.createChild(GenericConfig.class);
            newChild.setName("test3");
            nestedChild1.setGenericConfig(newChild);
            return nestedChild1;
        };
        ConfigSupport.apply(configCode, nestedChild);

        assertAll(
            () -> assertNotNull(locator.getService(GenericConfig.class, "test1"), "Nested named child 1"),
            () -> assertNotNull(locator.getService(GenericConfig.class, "test2"), "Nested named child 2"),
            () -> assertNull(locator.getService(GenericConfig.class, "test"), "Nested named grand child replaced")
        );
    }

    @Test
    public void testReplaceChild() throws TransactionFailure {
        SimpleConnector sc = locator.getService(SimpleConnector.class);
        assertEquals(1, sc.getExtensions().size(), "Eextensions");

        GenericContainer extension = sc.getExtensions().get(0);
        assertEquals(2, extension.getExtensions().size(), "Child extensions");

        SingleConfigCode<GenericContainer> configCode = extension1 -> {
            GenericConfig newChild = extension1.createChild(GenericConfig.class);
            newChild.setName("test3");
            GenericConfig nestedChild = extension1.getExtensions().set(0, newChild);
            return nestedChild;
        };
        ConfigSupport.apply(configCode, extension);

        assertAll(
            () -> assertThat("Extensions", extension.getExtensions(), hasSize(2)),
            () -> assertNull(locator.getService(GenericConfig.class, "test1"), "Nested named child 1"),
            () -> assertNull(locator.getService(GenericConfig.class, "test"), "Nested named grand child replaced"),
            () -> assertEquals("test3", extension.getExtensions().get(0).getName(), "New Nested child"),
            () -> assertNotNull(locator.getService(GenericConfig.class, "test3"), "New Nested child"),
            () -> assertNotNull(locator.getService(GenericConfig.class, "test2"), "Nested named child 2")
        );
    }
}
