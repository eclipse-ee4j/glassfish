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

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        ,
        "an attribute on an undefined node,"
        + "CERT_NICKNAME,"
        + "configs.config.server-config.network-config.protocols.protocol.http-listener-1.ssl.cert-nickname,"
        + "s1as"

    })
    public void setListenerAttribute(String testDescription, ListenerAttributeType attributeType, String propertyName, String propertyValue)
            throws InterruptedException, TimeoutException, ExecutionException {
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

        String oldAttributeValue = attributeType.getAttributeValue(listener, locator);

        // Let's register a listener
        final PropertyChangeFuture beanEvents = attributeType.addEventListener(listener, locator);

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
            String newAttributeValue = attributeType.getAttributeValue(listener, locator);
            assertEquals(propertyValue, newAttributeValue);

            PropertyChangeEvent event = waitForTheEvent(beanEvents, attributeType);

            assertNotNull(event);
            assertAll(
                    () -> assertEquals(oldAttributeValue, event.getOldValue()),
                    () -> assertEquals(propertyValue, event.getNewValue()),
                    () -> assertEquals(attributeType.getEventPropertyName(), event.getPropertyName())
            );
        } finally {
            attributeType.removeEventListener(beanEvents, listener, locator);
        }

    }

    private enum ListenerAttributeType {
        PORT {
            @Override
            String getAttributeValue(NetworkListener listener, ServiceLocator locator) {
                return listener.getPort();
            }

            @Override
            String getEventPropertyName() {
                return "port";
            }

        },
        PROPERTY {
            @Override
            String getAttributeValue(NetworkListener listener, ServiceLocator locator) {
                return listener.getPropertyValue("a");
            }

            @Override
            String getEventPropertyName() {
                return "value";
            }

        },
        CERT_NICKNAME {
            @Override
            String getAttributeValue(NetworkListener listener, ServiceLocator locator) {
                Ssl ssl = getProtocolNode(listener, locator).getSsl();
                return ssl != null ? ssl.getCertNickname() : null;
            }

            @Override
            String getEventPropertyName() {
                return "cert-nickname";
            }

            /*
                Add event listener which is triggered when the new Ssl node is added to the protocol.
                This listener registers another listener which is then triggered when the Ssl attribute is added.
                It wouldn't work to listen to the Ssl attribute directly because the Ssl node isn't
                present in the beginning and there's no node to bind the listener to.
            */
            @Override
            PropertyChangeFuture addEventListener(NetworkListener listener, ServiceLocator locator) {
                ObservableBean protocolBean = (ObservableBean) ConfigSupport.getImpl(getProtocolNode(listener, locator));
                final PropertyChangeFuture protocolEvents = new PropertyChangeFuture();
                final PropertyChangeFuture sslEvents = new PropertyChangeFuture();
                protocolBean.addListener(protocolEvents);
                protocolEvents.thenAccept(events -> {
                    protocolBean.removeListener(protocolEvents);
                    assertThat(events,arrayWithSize(1));
                    PropertyChangeEvent event = events[0];
                    ObservableBean sslBean = (ObservableBean) (event.getNewValue());
                    sslBean.addListener(sslEvents);
                }).exceptionally(e -> {
                    e.printStackTrace();
                    sslEvents.completeExceptionally(e);
                    return null;
                });
                return sslEvents;
            }

            /*
                Remove the listener on the new Ssl node
            */
            @Override
            void removeEventListener(PropertyChangeFuture beanEvents, NetworkListener listener, ServiceLocator locator) {
                final Protocol protocolNode = getProtocolNode(listener, locator);
                ObservableBean sslBean = (ObservableBean) ConfigSupport.getImpl(protocolNode.getSsl());
                sslBean.removeListener(beanEvents);
            }

            private Protocol getProtocolNode(NetworkListener listener, ServiceLocator locator) {
                return locator.getService(Protocols.class).findProtocol(listener.getProtocol());
            }

        };

        abstract String getAttributeValue(NetworkListener listener, ServiceLocator locator);

        abstract String getEventPropertyName();

        boolean isForEvent(PropertyChangeEvent event) {
            return this.getEventPropertyName().equals(event.getPropertyName());
        }

        PropertyChangeFuture addEventListener(NetworkListener listener, ServiceLocator locator) {
            ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
            final PropertyChangeFuture beanEvents = new PropertyChangeFuture();
            bean.addListener(beanEvents);
            return beanEvents;
        }

        void removeEventListener(PropertyChangeFuture beanEvents, NetworkListener listener, ServiceLocator locator) {
            ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(listener);
            bean.removeListener(beanEvents);
        }
    }

    private static class PropertyChangeFuture extends CompletableFuture<PropertyChangeEvent[]> implements ConfigListener {

        @Override
        public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
            this.complete(propertyChangeEvents);
            return null;
        }
    }

    private PropertyChangeEvent waitForTheEvent(final PropertyChangeFuture beanEvents, ListenerAttributeType attributeType) throws ExecutionException, InterruptedException, TimeoutException {
        // check we recevied the event
        List<PropertyChangeEvent> events = Arrays.stream(
                beanEvents.get(10, TimeUnit.SECONDS))
                .filter(attributeType::isForEvent)
                .collect(Collectors.toList());
        assertThat(events, hasSize(1));
        PropertyChangeEvent event = events.get(0);
        return event;
    }

}
