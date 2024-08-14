/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.portable;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import jakarta.ejb.spi.HandleDelegate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

/**
 * A portable implementation of Handle using the HandleDelegate SPI. This class can potentially be instantiated in
 * another vendor's container so it must not refer to any non-portable RI-specific classes.
 *
 * @author Kenneth Saks
 */
public final class HandleImpl implements Handle, Serializable {
    private EJBObject ejbObject;

    // This constructor will only be used by the EJB container in the RI.
    public HandleImpl(EJBObject ejbObject) {
        this.ejbObject = ejbObject;
    }

    // This is the public API from jakarta.ejb.Handle
    @Override
    public EJBObject getEJBObject() throws RemoteException {
        return ejbObject;
    }

    private void writeObject(ObjectOutputStream ostream) throws IOException {
        HandleDelegate handleDelegate;
        try {
            handleDelegate = HandleDelegateUtil.getHandleDelegate();
        } catch (NamingException ne) {
            throw new EJBException("Unable to lookup HandleDelegate", ne);
        }
        handleDelegate.writeEJBObject(ejbObject, ostream);
    }

    private void readObject(ObjectInputStream istream) throws IOException, ClassNotFoundException {
        HandleDelegate handleDelegate;
        try {
            handleDelegate = HandleDelegateUtil.getHandleDelegate();
        } catch (NamingException ne) {
            throw new EJBException("Unable to lookup HandleDelegate", ne);
        }
        ejbObject = handleDelegate.readEJBObject(istream);
    }
}
