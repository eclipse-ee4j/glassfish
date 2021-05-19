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

package org.glassfish.enterprise.iiop.impl;

import java.io.*;

import jakarta.ejb.*;
import jakarta.ejb.spi.HandleDelegate;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.ORB;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

/**
 * An implementation of HandleDelegate for the IIOP Protocol.
 *
 */

public final class IIOPHandleDelegate
    implements HandleDelegate
{

    public static HandleDelegate getHandleDelegate() {
        HandleDelegate handleDelegate =
            (HandleDelegate) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            ClassLoader cl = new HandleDelegateClassLoader();
                            Class c = cl.loadClass(
                                "org.glassfish.enterprise.iiop.impl.IIOPHandleDelegate");
                            return c.newInstance();
                        } catch ( Exception ex ) {
                            throw new RuntimeException("Error creating HandleDelegate", ex);
                        }
                    }
                }
            );
        return handleDelegate;
    }


    public void writeEJBObject(jakarta.ejb.EJBObject ejbObject,
            java.io.ObjectOutputStream ostream)
        throws java.io.IOException
    {
        ostream.writeObject(ejbObject); // IIOP stubs are Serializable
    }

    public jakarta.ejb.EJBObject readEJBObject(java.io.ObjectInputStream istream)
        throws java.io.IOException, ClassNotFoundException
    {
        return (EJBObject)getStub(istream, EJBObject.class);
    }

    public void writeEJBHome(jakarta.ejb.EJBHome ejbHome,
            java.io.ObjectOutputStream ostream)
        throws java.io.IOException
    {
        ostream.writeObject(ejbHome); // IIOP stubs are Serializable
    }

    public jakarta.ejb.EJBHome readEJBHome(java.io.ObjectInputStream istream)
        throws java.io.IOException, ClassNotFoundException
    {
        return (EJBHome)getStub(istream, EJBHome.class);
    }

    private Object getStub(java.io.ObjectInputStream istream, Class stubClass)
        throws IOException, ClassNotFoundException
    {
        // deserialize obj
        Object obj = istream.readObject();

        if( StubAdapter.isStub(obj) ) {

            try {

                // Check if it is already connected to the ORB by getting
                // the delegate.  If BAD_OPERATION is not thrown, then the
                // stub is connected.  This will happen if istream is an
                // IIOP input stream.
                StubAdapter.getDelegate(obj);

            } catch(org.omg.CORBA.BAD_OPERATION bo) {

                // TODO Temporary way to get the ORB.  Will need to
                // replace with an approach that goes through the habitat
                ORB orb = null;
                try {

                    orb = (ORB) new InitialContext().lookup("java:comp/ORB");

                } catch(NamingException ne) {

                    throw new IOException("Error acquiring orb", ne);
                }

                // Stub is not connected. This can happen if istream is
                // not an IIOP input stream (e.g. it's a File stream).
                StubAdapter.connect(obj, (com.sun.corba.ee.spi.orb.ORB) orb);
            }

        } else {
            throw new IOException("Unable to create stub for class " +
                stubClass.getName() +
                ", object deserialized is not a CORBA object, it's type is " +
                obj.getClass().getName());
        }

        // narrow it
        Object stub = PortableRemoteObject.narrow(obj, stubClass);

        return stub;
    }

}
