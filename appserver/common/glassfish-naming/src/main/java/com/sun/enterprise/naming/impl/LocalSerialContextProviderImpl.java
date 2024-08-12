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

import com.sun.enterprise.naming.util.NamingUtilsImpl;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.logging.Level;

import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.omg.CORBA.ORB;

import static com.sun.enterprise.naming.util.LogFacade.logger;

/**
 * This class is the implementation of the local SerialContextProvider
 *
 * @author Sheetal Vartak
 */

public class LocalSerialContextProviderImpl extends SerialContextProviderImpl {
    @LogMessageInfo(message = "Exception occurred when instantiating LocalSerialContextProviderImpl: {0}",
    cause = "java.rmi.RemoteException",
    action = "Check server.log for details")
    public static final String LOCAL_PROVIDER_NULL = "AS-NAMING-00003";

    private NamingUtilsImpl namingUtils = new NamingUtilsImpl();

    private LocalSerialContextProviderImpl(TransientContext rootContext) throws RemoteException {
        super(rootContext);
    }

    static LocalSerialContextProviderImpl initProvider(TransientContext rootContext) {
        try {
            return new LocalSerialContextProviderImpl(rootContext);
        } catch (RemoteException re) {
            logger.log(Level.SEVERE, LOCAL_PROVIDER_NULL, re);
            return null;
        }
    }

    /**
     * overriding the super.bind() since we need to make a copy of the object
     * before it gets put into the rootContext
     * Remote Provider already does that since when a method is called
     * on a remote object (in our case the remote provider),
     * the copies of the method arguments get passed and not the real objects.
     */

    public void bind(String name, Object obj)
            throws NamingException, RemoteException {
        Object copyOfObj = namingUtils.makeCopyOfObject(obj);
        super.bind(name, copyOfObj);
    }


    /**
     * overriding the super.rebind() since we need to make a copy of the object
     * before it gets put into the rootContext.
     * Remote Provider already does that since when a method is called
     * on a remote object (in our case the remote provider),
     * the copies of the method arguments get passed and not the real objects.
     */

    public void rebind(String name, Object obj)
            throws NamingException, RemoteException {
        Object copyOfObj = namingUtils.makeCopyOfObject(obj);
        super.rebind(name, copyOfObj);
    }

    public Object lookup(String name)
            throws NamingException, RemoteException {
        Object obj = super.lookup(name);

        try {
            if (obj instanceof Reference) {
                Reference ref = (Reference) obj;

                if (ref.getFactoryClassName().equals
                        (GlassfishNamingManagerImpl.IIOPOBJECT_FACTORY)) {

                    ORB orb = ProviderManager.getProviderManager().getORB();

                    Hashtable env = new Hashtable();
                    if( orb != null ) {

                        env.put("java.naming.corba.orb", orb);

                    }


                    obj = javax.naming.spi.NamingManager.getObjectInstance
                            (obj, new CompositeName(name), null, env);
                    // NOTE : No copy object performed in this case
                    return obj;
                }

            }

        } catch (Exception e) {
            RemoteException re = new RemoteException("", e);
            throw re;

        }

        return namingUtils.makeCopyOfObject(obj);
    }
}
