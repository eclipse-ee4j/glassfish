/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.perf.local;

import jakarta.ejb.*;
import java.rmi.RemoteException;

public interface Hello extends EJBObject
{
    void warmup(int type, boolean local) throws RemoteException;

    float notSupported(int type, boolean tx) throws RemoteException;
    float required(int type, boolean tx) throws RemoteException;
    float requiresNew(int type, boolean tx) throws RemoteException;
    float mandatory(int type, boolean tx) throws RemoteException;
    float never(int type, boolean tx) throws RemoteException;
    float supports(int type, boolean tx) throws RemoteException;

    float notSupportedRemote(int type, boolean tx) throws RemoteException;
    float requiredRemote(int type, boolean tx) throws RemoteException;
    float requiresNewRemote(int type, boolean tx) throws RemoteException;
    float mandatoryRemote(int type, boolean tx) throws RemoteException;
    float neverRemote(int type, boolean tx) throws RemoteException;
    float supportsRemote(int type, boolean tx) throws RemoteException;
}
