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
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

public interface SerialContextProvider extends Remote {

    /**
     * Lookup the specified name.
     *
     * @return the object or context bound to the name.
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    Object lookup(String name) throws NamingException, RemoteException;

    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    void bind(String name, Object obj) throws NamingException, RemoteException;

    /**
     * Rebind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    void rebind(String name, Object obj) throws NamingException, RemoteException;

    /**
     * Unbind the specified object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    void unbind(String name) throws NamingException, RemoteException;

    /**
     * Rename the bound object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    void rename(String oldname, String newname) throws NamingException, RemoteException;

    Hashtable<Object, Object> list() throws RemoteException;

    /**
     * List the contents of the specified context.
     *
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    Hashtable<Object, Object> list(String name) throws NamingException, RemoteException;

    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    Context createSubcontext(String name) throws NamingException, RemoteException;

    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws RemoteException there is an RMI exception.
     */
    void destroySubcontext(String name) throws NamingException, RemoteException;
}
