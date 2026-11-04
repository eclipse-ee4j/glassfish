/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.kernel.event;

import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.deployment.common.DeploymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A {@link DeploymentException} thrown by a synchronous listener must abort the deployment,
 * whatever its root cause is.
 */
class OSGiAwareEventsImplTest {

    private static final EventTypes<?> TEST_EVENT = EventTypes.create("OSGiAwareEventsImplTest");

    private OSGiAwareEventsImpl events;

    @BeforeEach
    void createEvents() {
        events = new OSGiAwareEventsImpl();
    }

    @Test
    void deploymentExceptionWithoutCauseIsRethrown() {
        DeploymentException thrown = new DeploymentException("CDI deployment failure");
        assertListenerExceptionIsRethrown(thrown);
    }

    @Test
    void deploymentExceptionWithErrorRootCauseIsRethrown() {
        // What WeldDeployer throws when the bean archive validation or an extension fails.
        DeploymentException thrown = new DeploymentException("CDI deployment failure",
            new NoClassDefFoundError("Could not initialize class org.example.Literal"));
        assertListenerExceptionIsRethrown(thrown);
    }

    @Test
    void deploymentExceptionWithUnknownClassNotFoundIsRethrown() {
        DeploymentException thrown = new DeploymentException("Class loading failure",
            new ClassNotFoundException("org.example.Missing"));
        assertListenerExceptionIsRethrown(thrown);
    }

    private void assertListenerExceptionIsRethrown(DeploymentException thrown) {
        events.register(event -> {
            throw thrown;
        });

        DeploymentException caught = assertThrows(DeploymentException.class,
            () -> events.send(new Event<>(TEST_EVENT), false));
        assertSame(thrown, caught);
    }
}
