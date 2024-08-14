/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.omg.CORBA.ORB;


/**
 * This class is a facade for the remote and local SerialContextProvider The
 * need for this class arose from the fact that we wanted to have a way of
 * lazily initializing the Remote SerialContextProvider. The TransientContext
 * member field has to be shared across both remote and local
 * SerialContextProvider. It could have been a static variable but to avoid
 * issues of static variables with multiple threads, etc, this class has been
 * designed. The ORB needs to be initialized before the call to
 * initRemoteProvider()
 *
 * @author Sheetal Vartak
 */
public final class ProviderManager {

    private static ProviderManager providerManager;

    private final TransientContext rootContext = new TransientContext();

    private SerialContextProvider localProvider;

    // Set lazily once initRemoteProvider is called.
    // Only available in server.
    private ORB orb;

    private ProviderManager() {
    }


    public synchronized static ProviderManager getProviderManager() {
        if (providerManager == null) {
            providerManager = new ProviderManager();
        }
        return providerManager;
    }


    public TransientContext getTransientContext() {
        return rootContext;
    }


    public synchronized SerialContextProvider getLocalProvider() {
        if (localProvider == null) {
            localProvider = LocalSerialContextProviderImpl.initProvider(rootContext);
        }
        return localProvider;
    }


    public Remote initRemoteProvider(ORB orb) throws RemoteException {
        this.orb = orb;
        return RemoteSerialContextProviderImpl.initSerialContextProvider(orb, rootContext);
    }


    ORB getORB() {
        return orb;
    }
}
