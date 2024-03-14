/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.kernel.deployment;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.core.kernel.test.KernelJUnitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.api.event.EventTypes.PREPARE_SHUTDOWN;
import static org.glassfish.api.event.EventTypes.PREPARE_SHUTDOWN_NAME;
import static org.glassfish.api.event.EventTypes.SERVER_READY;
import static org.glassfish.api.event.EventTypes.SERVER_READY_NAME;
import static org.glassfish.api.event.EventTypes.SERVER_SHUTDOWN;
import static org.glassfish.api.event.EventTypes.SERVER_SHUTDOWN_NAME;
import static org.glassfish.api.event.EventTypes.SERVER_STARTUP;
import static org.glassfish.api.event.EventTypes.SERVER_STARTUP_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(KernelJUnitExtension.class)
public class EventFilteringTest {

    @Inject
    private ServiceLocator serviceLocator;

    @Test
    public void restrictedEventListenerTest() {
        Events events = serviceLocator.getService(Events.class);

        List<EventTypes<?>> eventTypes = new ArrayList<>();

        // Do not replace with lambda because annotation will be lost
        EventListener listener = new EventListener() {
            @Override
            public void event(@RestrictTo(PREPARE_SHUTDOWN_NAME) Event<?> event) {
                eventTypes.add(event.type());
            }
        };

        events.register(listener);

        events.send(new Event<>(SERVER_STARTUP), false);
        events.send(new Event<>(SERVER_READY), false);
        events.send(new Event<>(PREPARE_SHUTDOWN), false);
        events.send(new Event<>(SERVER_SHUTDOWN), false);

        assertAll(
                () -> assertThat(eventTypes, hasSize(1)),
                () -> assertSame(eventTypes.get(0), PREPARE_SHUTDOWN)
        );

        events.unregister(listener);
    }

    @Test
    public void repeatableRestrictedEventListenerTest() {
        Events events = serviceLocator.getService(Events.class);

        List<EventTypes<?>> eventTypes = new ArrayList<>();

        // Do not replace with lambda because annotations will be lost
        EventListener listener = new EventListener() {

            @Override
            public void event(@RestrictTo(SERVER_STARTUP_NAME) @RestrictTo(SERVER_SHUTDOWN_NAME) Event<?> event) {
                eventTypes.add(event.type());
            }
        };

        events.register(listener);

        events.send(new Event<>(SERVER_STARTUP), false);
        events.send(new Event<>(SERVER_READY), false);
        events.send(new Event<>(PREPARE_SHUTDOWN), false);
        events.send(new Event<>(SERVER_SHUTDOWN), false);

        assertAll(
                () -> assertThat(eventTypes, hasSize(2)),
                () -> assertThat(eventTypes, contains(SERVER_STARTUP, SERVER_SHUTDOWN))
        );

        events.unregister(listener);
    }

    @Test
    public void nonMatchingEventListenerTest() {
        Events events = serviceLocator.getService(Events.class);

        List<EventTypes<?>> eventTypes = new ArrayList<>();

        // Do not replace with lambda because annotation will be lost
        EventListener listener = new EventListener() {

            @Override
            public void event(@RestrictTo(SERVER_STARTUP_NAME) Event<?> event) {
                eventTypes.add(event.type());
            }
        };

        events.register(listener);

        events.send(new Event<>(SERVER_SHUTDOWN), false);

        assertThat(eventTypes, empty());

        events.unregister(listener);
    }

    @Test
    public void nonMatchingRepeatableEventListenerTest() {
        Events events = serviceLocator.getService(Events.class);

        List<EventTypes<?>> eventTypes = new ArrayList<>();

        // Do not replace with lambda because annotations will be lost
        EventListener listener = new EventListener() {

            @Override
            public void event(@RestrictTo(SERVER_READY_NAME) @RestrictTo(PREPARE_SHUTDOWN_NAME) Event<?> event) {
                eventTypes.add(event.type());
            }
        };

        events.register(listener);

        events.send(new Event<>(SERVER_STARTUP), false);
        events.send(new Event<>(SERVER_SHUTDOWN), false);

        assertThat(eventTypes, empty());

        events.unregister(listener);
    }

    @Test
    public void unrestrictedEventListenerTest() {
        Events events = serviceLocator.getService(Events.class);

        List<EventTypes<?>> eventTypes = new ArrayList<>();

        EventListener listener = event -> eventTypes.add(event.type());

        events.register(listener);

        events.send(new Event<>(SERVER_STARTUP), false);
        events.send(new Event<>(SERVER_READY), false);
        events.send(new Event<>(PREPARE_SHUTDOWN), false);
        events.send(new Event<>(SERVER_SHUTDOWN), false);

        assertAll(
                () -> assertThat(eventTypes, hasSize(4)),
                () -> assertThat(eventTypes,
                        contains(SERVER_STARTUP, SERVER_READY, PREPARE_SHUTDOWN, SERVER_SHUTDOWN))
        );

        events.unregister(listener);
    }
}
