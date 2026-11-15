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

package org.glassfish.orb.http.client;

import jakarta.ejb.EJBException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Decides what a caller of a proxy sees when the transport fails.
 *
 * <p>A timeout or a protocol error is an {@code IOException}, which a
 * business interface rarely declares; thrown through a proxy it becomes an
 * {@code UndeclaredThrowableException}. The caller's own contract decides:
 * the exception itself if the method declares it, a {@code RemoteException}
 * for an RMI-style interface, and an {@code EJBException} otherwise.
 */
final class TransportErrors {

    private TransportErrors() {
    }

    /**
     * @param failure what the transport threw
     * @param method the interface method the caller invoked
     * @param view the interface the proxy implements
     * @return what to throw to that caller
     */
    static Exception translate(IOException failure, Method method, Class<?> view) {
        // The RMI-style test comes first: RemoteException is assignable to any
        // declared supertype (IOException, Exception), so a method of a Remote
        // view that declares one of those still has to receive a RemoteException.
        boolean rmiStyle = Remote.class.isAssignableFrom(view);
        for (Class<?> declared : method.getExceptionTypes()) {
            if (RemoteException.class.isAssignableFrom(declared)) {
                rmiStyle = true;
            }
        }
        if (rmiStyle) {
            return failure instanceof RemoteException ? failure : new RemoteException(failure.getMessage(), failure);
        }
        for (Class<?> declared : method.getExceptionTypes()) {
            if (declared.isInstance(failure)) {
                return failure;
            }
        }
        return new EJBException(failure.getMessage(), failure);
    }
}
