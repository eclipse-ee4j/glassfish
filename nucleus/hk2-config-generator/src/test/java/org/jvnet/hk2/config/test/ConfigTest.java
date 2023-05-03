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

package org.jvnet.hk2.config.test;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigInjector;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigView;
import org.jvnet.hk2.config.ConfigurationPopulator;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.HK2DomConfigUtilities;
import org.jvnet.hk2.config.InjectionTarget;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.Populator;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.test.example.ConfigModule;
import org.jvnet.hk2.config.test.example.DummyPopulator;
import org.jvnet.hk2.config.test.example.EjbContainerAvailability;
import org.jvnet.hk2.config.test.example.EjbContainerAvailabilityInjector;
import org.jvnet.hk2.config.test.example.GenericConfigInjector;
import org.jvnet.hk2.config.test.example.GenericContainer;
import org.jvnet.hk2.config.test.example.GenericContainerInjector;
import org.jvnet.hk2.config.test.example.SimpleConfigBeanWrapper;
import org.jvnet.hk2.config.test.example.SimpleConnector;
import org.jvnet.hk2.config.test.example.SimpleConnectorInjector;
import org.jvnet.hk2.config.test.example.WebContainerAvailabilityInjector;

import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addClasses;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.getOneMetadataField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This runs a set of tests to test various habitat and Dom APIs
 *  used by the config subsystem
 *
 *  @author Mahesh Kannan
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigTest {
    private static final String TEST_NAME = "";
    private static final ServiceLocator locator = ServiceLocatorFactory.getInstance().create(TEST_NAME);

    @BeforeAll
    public static void beforeAll() {
        final DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        final DynamicConfiguration config = dcs.createDynamicConfiguration();
        new ConfigModule(locator).configure(config);
        config.commit();
    }

    @Test
    @Order(1)
    public void lookupAllInjectors() {
        final String[] expected = {
                SimpleConnectorInjector.class.getName(), EjbContainerAvailabilityInjector.class.getName(),
                WebContainerAvailabilityInjector.class.getName(), GenericContainerInjector.class.getName(),
                GenericConfigInjector.class.getName()
        };
        final List<ServiceHandle<ConfigInjector>> inhabitants = locator.getAllServiceHandles(ConfigInjector.class);
        final Set<String> inhabitantNames = new HashSet<>();
        for (final ServiceHandle<?> inh : inhabitants) {
            inhabitantNames.add(inh.getActiveDescriptor().getImplementation());
        }

        assertAll(
            () -> assertThat(inhabitants, hasSize(expected.length)),
            () -> assertThat(inhabitantNames, containsInAnyOrder(expected))
        );
    }

    @Test
    @Order(2)
    public void lookupInjectorByName() {
        final ServiceHandle<?> inhabitant1 = locator.getServiceHandle(ConfigInjector.class, "simple-connector");
        final ServiceHandle<?> inhabitant2 = locator.getServiceHandle(ConfigInjector.class, "ejb-container-availability");

        assertAll(
            () -> assertNotNull(inhabitant1),
            () -> assertNotNull(inhabitant2)
        );
        assertAll(
            () -> assertEquals(SimpleConnectorInjector.class.getName(),
                inhabitant1.getActiveDescriptor().getImplementation()),
            () -> assertEquals(EjbContainerAvailabilityInjector.class.getName(),
                inhabitant2.getActiveDescriptor().getImplementation()),
            () -> assertFalse(inhabitant1.isActive()),
            () -> assertFalse(inhabitant2.isActive())

        );
    }

    @Test
    @Order(4)
    public void lookupInjectorByFilter() {
        final ActiveDescriptor<?> desc = locator
            .getBestDescriptor(new InjectionTargetFilter(EjbContainerAvailability.class.getName()));
        assertNotNull(desc);
        assertEquals(EjbContainerAvailabilityInjector.class.getName(), desc.getImplementation());
    }

    @Test
    @Order(5)
    public void parseDomainXml() {
        final ConfigParser parser = new ConfigParser(locator);
        final URL url = this.getClass().getResource("/domain.xml");
        assertNotNull(url, "url");
        final DomDocument<?> doc = parser.parse(url);
        assertNotNull(doc, "parsed document");
    }

    @Test
    @Order(6)
    public void lookupConnectorServiceAndEnsureNotActive() {
        final SimpleConnector connector = locator.getService(SimpleConnector.class);
        assertNotNull(connector, "simple connector");
        System.out.println("[lookupConnectorService] Got connector : " + connector.getClass().getName());
        assertNotNull(connector);
        assertAll(
            () -> assertEquals("8080", connector.getPort()),
            () -> assertTrue(Proxy.isProxyClass(connector.getClass()))
        );
        final ServiceHandle<?> inhabitant1 = locator.getServiceHandle(ConfigInjector.class, "simple-connector");
        assertFalse(inhabitant1.isActive());
    }


    @Test
    @Order(9)
    public void testDefaultValuesFromConfig() {
        final SimpleConnector connector = locator.getService(SimpleConnector.class);
        assertNotNull(connector);
        assertAll(
            () -> assertEquals("web-method", connector.getWebContainerAvailability().getPersistenceFrequency()),
            () -> assertEquals("true", connector.getEjbContainerAvailability().getAvailabilityEnabled()),
            () -> assertEquals("file", connector.getEjbContainerAvailability().getSfsbPersistenceType()),
            () -> assertEquals("replicated", connector.getEjbContainerAvailability().getSfsbHaPersistenceType())
        );
    }

    @Test
    @Order(10)
    public void testDomTx() throws Exception {
        final SimpleConnector connector = locator.getService(SimpleConnector.class);
        final EjbContainerAvailability ejb = connector.getEjbContainerAvailability();
        assertAll(
            () -> assertTrue(Dom.class.isAssignableFrom(Dom.unwrap(ejb).getClass()), "Dom.class"),
            () -> assertTrue(ConfigBeanProxy.class.isAssignableFrom(ejb.getClass()), "ConfigBeanProxy.class")
        );
        final Dom ejbDom = Dom.unwrap(ejb);
        assertNotNull(ejbDom.getHabitat(), "ejbDom.habitat");

        final String avEnabled = ejb.getAvailabilityEnabled();
        final SingleConfigCode<EjbContainerAvailability> configCode = p -> {
            p.setSfsbHaPersistenceType("coherence");
            p.setSfsbCheckpointEnabled("**MUST BE**");
            return null;
        };
        ConfigSupport.apply(configCode, ejb);

        assertAll(
            () -> assertEquals("coherence", ejb.getSfsbHaPersistenceType()),
            () -> assertEquals("**MUST BE**", ejb.getSfsbCheckpointEnabled()),
            () -> assertEquals(avEnabled, ejb.getAvailabilityEnabled())
        );
    }

    @Test
    @Order(13)
    public void testDomTxReadOnlyAttributes() throws Exception {
        final SimpleConnector connector = locator.getService(SimpleConnector.class);
        final EjbContainerAvailability ejb = connector.getEjbContainerAvailability();
        final Dom ejbDom = Dom.unwrap(ejb);
        assertNotNull(ejbDom.getHabitat(), "ejbDom.habitat");

        final String origAVEnabled = ejb.getAvailabilityEnabled();
        final String origSFSBHaPersistenceType = ejb.getSfsbHaPersistenceType();
        final SingleConfigCode<EjbContainerAvailability> configCode = p -> {
           p.setSfsbHaPersistenceType("99999.999");
           p.setSfsbCheckpointEnabled("**MUST BE**");
           assertAll(
               () -> assertEquals(origSFSBHaPersistenceType, ejb.getSfsbHaPersistenceType()),
               () -> assertNotEquals(p.getSfsbHaPersistenceType(), ejb.getSfsbHaPersistenceType())
           );
           return null;
        };
        ConfigSupport.apply(configCode, ejb);

        assertAll(
            () -> assertEquals("99999.999", ejb.getSfsbHaPersistenceType()),
            () -> assertEquals("**MUST BE**", ejb.getSfsbCheckpointEnabled()),
            () -> assertEquals(origAVEnabled, ejb.getAvailabilityEnabled())
        );
    }

    @Test
    @Order(14)
    public void testGetImplAndAddListener() throws Exception {
        final SimpleConnector connector = locator.getService(SimpleConnector.class);
        final EjbContainerAvailability ejb = connector.getEjbContainerAvailability();
        final ObservableBean obean = (ObservableBean) ConfigSupport.getImpl(ejb);
        final EjbObservableBean ejbBean = new EjbObservableBean();

        assertEquals(0, ejbBean.getCount());
        obean.addListener(ejbBean);
        final SingleConfigCode<EjbContainerAvailability> configCode = p -> {
            p.setSfsbHaPersistenceType("DynamicData");
            p.setSfsbCheckpointEnabled("**MUST BE**");
            assertNotEquals(ejb.getSfsbHaPersistenceType(), p.getSfsbHaPersistenceType());
            return null;
        };
        ConfigSupport.apply(configCode, ejb);

        assertAll(
            () -> assertEquals("DynamicData", ejb.getSfsbHaPersistenceType()),
            () -> assertEquals("**MUST BE**", ejb.getSfsbCheckpointEnabled()),
            () -> assertEquals(1, ejbBean.getCount())
        );

        final SingleConfigCode<EjbContainerAvailability> configCode2 = p -> {
           p.setSfsbHaPersistenceType("DynamicData2");
           p.setSfsbCheckpointEnabled("**MUST BE**");
           assertNotEquals(ejb.getSfsbHaPersistenceType(), p.getSfsbHaPersistenceType());
           return null;
        };
        ConfigSupport.apply(configCode2, ejb);

        final ConfigView impl = ConfigSupport.getImpl(ejb);
        assertAll(
            () -> assertEquals("DynamicData2", ejb.getSfsbHaPersistenceType()),
            () -> assertEquals("**MUST BE**", ejb.getSfsbCheckpointEnabled()),
            () -> assertEquals(2, ejbBean.getCount()),
            () -> assertEquals(SimpleConfigBeanWrapper.class, impl.getClass()),
            () -> assertEquals(SimpleConfigBeanWrapper.class, impl.getMasterView().getClass()),
            () -> assertEquals(Class.class, impl.getProxyType().getClass())
        );
    }


    @Test
    @Order(16)
    public void testGenericContainerInjector() {
        assertNotNull(locator.getServiceHandle(ConfigInjector.class, "generic-container"));

    }

    @Test
    @Order(17)
    public void testLongDataType() {
        final GenericContainer gc = locator.getService(GenericContainer.class);
        assertEquals(1234L, gc.getStartupTime());
    }

    @Test
    @Order(18)
    public void testIntDataType() {
        final GenericContainer gc = locator.getService(GenericContainer.class);
        assertEquals(1234, gc.getIntValue());
    }

    @Test
    @Order(19)
    public void testConfigurationPopulator() {
        final DummyPopulator dummyPopulator = (DummyPopulator) locator.getService(Populator.class);
        assertNotNull(dummyPopulator, "dummy populator");
        final ConfigurationPopulator confPopulator = locator.getService(ConfigurationPopulator.class);
        confPopulator.populateConfig(locator);
        assertTrue(dummyPopulator.isPopulateCalled());
    }

    @Test
    @Order(20)
    public void testSingletonProxy() {
        final SimpleConnector simpleConnector1 = locator.getService(SimpleConnector.class);
        final SimpleConnector simpleConnector2 = locator.getService(SimpleConnector.class);

        assertAll(
            () -> assertNotNull(simpleConnector1),
            () -> assertThat(simpleConnector1.getClass().getName(), containsString("$Proxy")),
            () -> assertSame(simpleConnector1, simpleConnector2)
        );
    }


    /**
     * Ensures that even the non-standard format of metadata from the hk2-config subsystem can
     * be read from the service in addClasses.  addClasses will now read both forms, if the
     * documented form fails, it'll try the hk2-config form
     */
    @Test
    @Order(21)
    public void testAddClassOfInjector() {
        final ServiceLocator myLocator = ServiceLocatorFactory.getInstance().create(null);

        final List<ActiveDescriptor<?>> added = addClasses(myLocator, EjbContainerAvailabilityInjector.class);
        final ActiveDescriptor<?> descriptor = added.get(0);

        assertEquals(EjbContainerAvailability.class.getName(), getOneMetadataField(descriptor, "target"));
    }

    @Test
    @Order(22)
    public void testEnableConfigUtilities() {
        final ServiceLocator myLocator = ServiceLocatorFactory.getInstance().create(null);

        assertAll(
            () -> assertNull(myLocator.getService(ConfigSupport.class)),
            () -> assertNull(myLocator.getService(ConfigurationPopulator.class)),
            () -> assertNull(myLocator.getService(Transactions.class)),
            () -> assertNull(myLocator.getService(InstanceLifecycleListener.class))
        );

        // Twice to check idempotence
        HK2DomConfigUtilities.enableHK2DomConfiguration(myLocator);
        HK2DomConfigUtilities.enableHK2DomConfiguration(myLocator);

        assertAll(
            () -> assertThat(myLocator.getAllServices(ConfigSupport.class), hasSize(1)),
            () -> assertThat(myLocator.getAllServices(ConfigurationPopulator.class), hasSize(1)),
            () -> assertThat(myLocator.getAllServices(Transactions.class), hasSize(1)),
            () -> assertThat(myLocator.getAllServices(InstanceLifecycleListener.class), hasSize(1))
        );
    }


    private static class InjectionTargetFilter implements Filter {

        private final String targetName;

        InjectionTargetFilter(final String targetName) {
            this.targetName = targetName;
        }

        @Override
        public boolean matches(final Descriptor d) {
            if (d.getQualifiers().contains(InjectionTarget.class.getName())) {
                final List<String> list = d.getMetadata().get("target");
                if (list != null && list.get(0).equals(targetName)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static class EjbObservableBean implements ConfigListener {

        private final AtomicInteger count = new AtomicInteger();

        @Override
        public UnprocessedChangeEvents changed(final PropertyChangeEvent[] events) {
            System.out.println("** EjbContainerAvailability changed ==> " + count.incrementAndGet());
            return null;
        }

        public int getCount() {
            return count.get();
        }
    }

}
