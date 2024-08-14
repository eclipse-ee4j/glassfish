/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import jakarta.ejb.EJBHome;

import java.rmi.RemoteException;
import java.util.concurrent.TimeoutException;

public interface GenericEJBHome extends EJBHome {

    public RemoteAsyncResult get(long asyncTaskID) throws RemoteException;

    // Need to pass TimeUnit as string due to RMI-IIOP enum marshalling problem
    public RemoteAsyncResult getWithTimeout(long asyncTaskID, long timeoutValue, String timeoutUnit)
            throws RemoteException, TimeoutException;

    /**
     * Remote Future.cancel() behavior.  If the task is already cancelled, the AsyncResult
     * is returned.  Otherwise, returns null.
     * @param asyncTaskID
     * @return
     * @throws RemoteException
     */
    public RemoteAsyncResult cancel(long asyncTaskID) throws RemoteException;

    /**
     *
     * @param asyncTaskID
     * @return if done, RemoteAsyncResult.  Else, returns null.
     * @throws RemoteException
     */
    public RemoteAsyncResult isDone(long asyncTaskID) throws RemoteException;

}
