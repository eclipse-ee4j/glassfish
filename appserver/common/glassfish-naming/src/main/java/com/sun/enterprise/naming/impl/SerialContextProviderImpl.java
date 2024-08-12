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

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

public class SerialContextProviderImpl implements SerialContextProvider {

    private final TransientContext rootContext;

    protected SerialContextProviderImpl(TransientContext rootContext) throws RemoteException {
        this.rootContext = rootContext;
    }


    @Override
    public Object lookup(String name) throws NamingException, RemoteException {
        try {
            return rootContext.lookup(name);
        } catch (NamingException ne) {
            throw ne;
        } catch (Exception e) {
            throw new RemoteException("Lookup failed for " + name, e);
        }
    }


    @Override
    public void bind(String name, Object obj) throws NamingException, RemoteException {
        rootContext.bind(name, obj);
    }


    @Override
    public void rebind(String name, Object obj) throws NamingException, RemoteException {
        rootContext.rebind(name, obj);
    }


    @Override
    public void unbind(String name) throws NamingException, RemoteException {
        rootContext.unbind(name);
    }


    @Override
    public void rename(String oldname, String newname) throws NamingException, RemoteException {
        rootContext.rename(oldname, newname);
    }


    @Override
    public Hashtable<Object, Object> list() throws RemoteException {
        return rootContext.list();
    }


    @Override
    public Hashtable<Object, Object> list(String name) throws NamingException, RemoteException {
        return rootContext.listContext(name);
    }


    @Override
    public Context createSubcontext(String name) throws NamingException, RemoteException {
        return rootContext.createSubcontext(name);
    }


    @Override
    public void destroySubcontext(String name) throws NamingException, RemoteException {
        rootContext.destroySubcontext(name);
    }
}
