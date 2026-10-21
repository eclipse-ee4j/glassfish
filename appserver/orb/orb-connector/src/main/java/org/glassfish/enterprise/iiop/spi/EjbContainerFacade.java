/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.spi;

import com.sun.enterprise.deployment.EjbDescriptor;

import jakarta.ejb.CreateException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Provides ejb-specific services to iiop glue code.
 */
public interface EjbContainerFacade {

    EjbDescriptor getEjbDescriptor();

    ClassLoader getClassLoader();

    Remote getTargetObject(byte[] instanceKey,
                           String generatedRemoteBusinessIntf);

    void releaseTargetObject(Remote remoteObj);

    /**
     * Creates a stateful session and returns the key that identifies it.
     * <p>
     * The rest of this interface is shaped by IIOP, where a session key never
     * has to be named: the client calls {@code create} on the home, gets back
     * a reference, and the key travels inside that reference where only the
     * ORB reads it. A transport that is not IIOP has nowhere to hide it - the
     * key has to come back as a value the client can hold and send again.
     * <p>
     * Only a stateful session container can answer this. Every other container
     * throws, rather than returning a shared instance and letting a caller
     * believe it has a conversation.
     *
     * @param generatedRemoteBusinessIntf the generated remote business
     *                                    interface name, or {@code null} to
     *                                    create through the remote home view
     * @return the instance key of the new session
     * @throws CreateException if the bean refused to be created
     * @throws RemoteException if this container has no stateful sessions
     */
    byte[] createSession(String generatedRemoteBusinessIntf) throws CreateException, RemoteException;

    /**
     * Discards a stateful session, running its pre-destroy callback.
     * <p>
     * Removal is idempotent: a session that is already gone is not an error,
     * because a client that retries a removal after a timeout should not be
     * told its own cleanup failed.
     *
     * @param instanceKey the key returned by {@link #createSession}
     * @throws RemoteException if this container has no stateful sessions
     */
    void removeSession(byte[] instanceKey) throws RemoteException;

    String getUseThreadPoolId();

    boolean getPassByReference();

}


