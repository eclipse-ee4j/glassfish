/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.v3.admin;

import com.sun.enterprise.v3.common.PlainTextActionReporter;

import java.beans.PropertyChangeEvent;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * test the set command
 *
 * @author Jerome Dochez
 */
@ExtendWith(KernelJUnitExtension.class)
public class ConfigAttributeSetTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private MockGenerator mockGenerator;
    private PropertyChangeEvent event;
    private Subject adminSubject;

    @BeforeEach
    public void addMissingServices() {
        adminSubject = mockGenerator.createAsadminSubject();
    }

    @ParameterizedTest(name = "{index}: Test setting {0}")
    @CsvSource({
        "a direct attribute,"
        + "PORT,"
        + "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.port,"
        + "8090"
        ,
        "an aliased attribute,"
        + "PORT,"
        + "server.network-config.network-listeners.network-listener.http-listener-1.port,"
        + "8090"
        ,
        "a property,"
        + "PROPERTY,"
        + "server.network-config.network-listeners.network-listener.http-listener-1.property.a,"
        + "b"

    })
    public void setListenerAttribute(String testDescription, ListenerAttributeType attributeType, String propertyName, String propertyValue) {
        CommandRunnerImpl runner = locator.getService(CommandRunnerImpl.class);
        assertNotNull(runner);

        // let's find our target
        NetworkListener listener = null;
        NetworkListeners service = locator.getService(NetworkListeners.class);
        for (NetworkListener l : service.getNetworkListener()) {
            if ("http-listener-1".equals(l.getName())) {
                listener = l;
                break;
            }
        }
        assertNotNull(listener);

        String oldAttributeValue = attributeType.getAttributeValue(listener);

        // Let's register a listener
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
        final PropertyChangeListener beanListener = new PropertyChangeListener(attributeType.getEventPropertyName());
        bean.addListener(beanListener);

        try {
            // parameters to the command
            ParameterMap parameters = new ParameterMap();
            parameters.set("DEFAULT", propertyName + "=" + propertyValue);
            // execute the set command.
            PlainTextActionReporter reporter = new PlainTextActionReporter();
            CommandInvocation invocation = runner.getCommandInvocation("set", reporter, adminSubject).parameters(parameters);
            invocation.execute();

            assertEquals(ExitCode.SUCCESS, reporter.getActionExitCode());
            assertEquals("", reporter.getMessage());

            // ensure events are delivered.
            locator.<Transactions>getService(Transactions.class).waitForDrain();

            // check the result.
            String newAttributeValue = attributeType.getAttributeValue(listener);
            assertEquals(propertyValue, newAttributeValue);

            // check we recevied the event
            assertNotNull(event);
            assertAll(
                    () -> assertEquals(oldAttributeValue, event.getOldValue()),
                    () -> assertEquals(propertyValue, event.getNewValue()),
                    () -> assertEquals(attributeType.getEventPropertyName(), event.getPropertyName())
            );
        } finally {
            bean.removeListener(beanListener);
        }

    }

    private enum ListenerAttributeType {
        PORT {
            @Override
            String getAttributeValue(NetworkListener listener) {
                return listener.getPort();
            }

            @Override
            String getEventPropertyName() {
                return "port";
            }

        },
        PROPERTY {
            @Override
            String getAttributeValue(NetworkListener listener) {
                return listener.getPropertyValue("a");
            }

            @Override
            String getEventPropertyName() {
                return "value";
            }

        };

        abstract String getAttributeValue(NetworkListener listener);

        abstract String getEventPropertyName();
    }

    private class PropertyChangeListener implements ConfigListener {
        
        private String propertyName;

        public PropertyChangeListener(String propertyName) {
            this.propertyName = propertyName;
        }
        
        @Override
        public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
            List<PropertyChangeEvent> events = Arrays.stream(propertyChangeEvents)
                    .filter(event -> propertyName.equals(event.getPropertyName()))
                    .collect(Collectors.toList());
            assertThat(events, hasSize(1));
            event = events.get(0);
            return null;
        }
    }
}
