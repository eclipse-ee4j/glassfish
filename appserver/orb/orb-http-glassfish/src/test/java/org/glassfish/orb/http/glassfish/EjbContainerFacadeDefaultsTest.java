/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.glassfish;

import com.sun.enterprise.deployment.EjbDescriptor;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@code EjbContainerFacade} is a public SPI. A container written against it
 * before the session methods existed must still compile and must refuse them
 * the way every non-stateful container does, rather than fail to load.
 */
class EjbContainerFacadeDefaultsTest {

    /** Implements only what the interface required before the session methods were added. */
    private static final class OldFacade implements EjbContainerFacade {

        @Override
        public EjbDescriptor getEjbDescriptor() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public Remote getTargetObject(byte[] instanceKey, String generatedRemoteBusinessIntf) {
            return null;
        }

        @Override
        public void releaseTargetObject(Remote remoteObj) {
        }

        @Override
        public String getUseThreadPoolId() {
            return null;
        }

        @Override
        public boolean getPassByReference() {
            return false;
        }
    }

    @Test
    @DisplayName("a facade that predates the session methods refuses to create a session")
    void createSessionIsRefusedByDefault() {
        assertThrows(RemoteException.class, () -> new OldFacade().createSession(null));
    }

    @Test
    @DisplayName("a facade that predates the session methods refuses to remove a session")
    void removeSessionIsRefusedByDefault() {
        assertThrows(RemoteException.class, () -> new OldFacade().removeSession(new byte[] { 1 }));
    }
}
