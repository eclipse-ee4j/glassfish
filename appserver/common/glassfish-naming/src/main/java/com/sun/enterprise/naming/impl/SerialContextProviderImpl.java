/*
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

import javax.naming.Context;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.Hashtable;

public class SerialContextProviderImpl implements SerialContextProvider {
    private TransientContext rootContext;

    protected SerialContextProviderImpl(TransientContext rootContext)
            throws RemoteException {
        this.rootContext = rootContext;
    }

    /**
     * Lookup the specified name.
     *
     * @return the object orK context bound to the name.
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public Object lookup(String name) throws NamingException, RemoteException {
        try {
            return rootContext.lookup(name);
        } catch (NamingException ne) {
            throw ne;
        } catch (Exception e) {
            RemoteException re = new RemoteException("", e);
            throw re;
        }
    }

    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void bind(String name, Object obj)
            throws NamingException, RemoteException {

        rootContext.bind(name, obj);
    }

    /**
     * Rebind the object to the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void rebind(String name, Object obj)
            throws NamingException, RemoteException {

        rootContext.rebind(name, obj);
    }

    /**
     * Unbind the specified object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void unbind(String name)
            throws NamingException, RemoteException {

        rootContext.unbind(name);
    }

    /**
     * Rename the bound object.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void rename(String oldname, String newname)
            throws NamingException, RemoteException {

        rootContext.rename(oldname, newname);
    }

    public Hashtable list() throws RemoteException {

        return rootContext.list();
    }

    /**
     * List the contents of the specified context.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public Hashtable list(String name) throws NamingException, RemoteException {
        Hashtable ne = rootContext.listContext(name);
        return ne;
    }

    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public Context createSubcontext(String name)
            throws NamingException, RemoteException {

        Context ctx = rootContext.createSubcontext(name);
        return ctx;
    }

    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     * @throws if              there is an RMI exception.
     */

    public void destroySubcontext(String name)
            throws NamingException, RemoteException {

        rootContext.destroySubcontext(name);
    }

}






