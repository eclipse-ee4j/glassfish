/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.naming.impl;


import com.sun.enterprise.util.Utility;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.CompositeName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.glassfish.api.naming.NamingObjectProxy;
import org.omg.CORBA.ORB;

/**
 * This class is the implementation of the Remote SerialContextProvider
 *
 * @author Sheetal Vartak
 */
public class RemoteSerialContextProviderImpl
    extends SerialContextProviderImpl {

    static public final String SERIAL_CONTEXT_PROVIDER_NAME =
        "SerialContextProvider";

    private ORB orb;

    private RemoteSerialContextProviderImpl(ORB orb, TransientContext rootContext)
        throws RemoteException {

        super(rootContext);

        this.orb = orb;
    }

    @Override
    public Hashtable list() throws RemoteException {
        try {
            return list("");
        } catch (NamingException ex) {
            throw new RemoteException(ex.getMessage(), ex);
        }
    }

    @Override
    public Hashtable list(String name) throws NamingException, RemoteException {
        Hashtable ne = super.list(name);
        Set<Map.Entry> entrySet = ne.entrySet();
        for(Iterator<Map.Entry> it = entrySet.iterator(); it.hasNext();) {
            Object val = it.next().getValue();
            // Issue 17219 skip non-serializable values for remote client.
            if(!(val instanceof java.io.Serializable)) {
                it.remove();
            }
        }
        return ne;
    }

   /**
     * Create the remote object and publish it in the CosNaming name service.
     */
    static public Remote initSerialContextProvider(ORB orb, TransientContext rootContext)
        throws RemoteException {
       return new RemoteSerialContextProviderImpl(orb, rootContext);
    }

    @Override
    public Object lookup(String name) throws NamingException, RemoteException {
        Object obj = super.lookup(name);

        // If CORBA object, resolve here in server to prevent a
        // another round-trip to CosNaming.

        ClassLoader originalClassLoader = null;

        try {
            if( obj instanceof Reference ) {
                Reference ref = (Reference) obj;

                if( ref.getFactoryClassName().equals(GlassfishNamingManagerImpl.IIOPOBJECT_FACTORY) ) {

                    // Set CCL to this CL so it's guaranteed to be able to find IIOPObjectFactory
                    originalClassLoader = Utility.setContextClassLoader(getClass().getClassLoader());

                    Hashtable env = new Hashtable();
                    env.put("java.naming.corba.orb", orb);

                    obj = javax.naming.spi.NamingManager.getObjectInstance
                            (obj, new CompositeName(name), null, env);
                }

            } else if (obj instanceof NamingObjectProxy) {

                NamingObjectProxy namingProxy = (NamingObjectProxy) obj;

                //this call will make sure that the actual object is initialized
                obj  = ((NamingObjectProxy) obj).create(new InitialContext());

        // If it's an InitialNamingProxy, ignore the result of the
        // create() call and re-lookup the name.
                if( namingProxy instanceof NamingObjectProxy.InitializationNamingObjectProxy ) {
                    return super.lookup(name);
                }
            }
        } catch(Exception e) {
            RemoteException re = new RemoteException("", e);
            throw re;
        }  finally {
            if( originalClassLoader != null ) {
                Utility.setContextClassLoader(originalClassLoader);
            }
        }

        return obj;
   }
}
