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

package com.sun.ejb.containers;

import com.sun.ejb.EJBUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.WriteAbortedException;
import java.rmi.Remote;

public class RemoteBusinessWrapperBase implements java.io.Serializable {

    /** This is the name of the developer-written business interface. */
    private String businessInterface_;

    private Remote stub_;

    private transient int hashCode_;

    public RemoteBusinessWrapperBase(Remote stub, String busIntf) {
        stub_ = stub;
        businessInterface_ = busIntf;
        this.hashCode_ = busIntf.hashCode();
    }


    public Remote getStub() {
        return stub_;
    }


    @Override
    public int hashCode() {
        return hashCode_;
    }


    @Override
    public boolean equals(Object obj) {
        boolean result = (obj == this); //Most efficient
        if ((result == false) && (obj != null)) { //Do elaborate checks
            if (obj instanceof RemoteBusinessWrapperBase) {
                RemoteBusinessWrapperBase remoteBWB =
                        (RemoteBusinessWrapperBase) obj;
                boolean hasSameBusinessInterface =
                        (remoteBWB.hashCode_ == hashCode_) &&
                        remoteBWB.businessInterface_.equals(businessInterface_);
                if (hasSameBusinessInterface) {
                    org.omg.CORBA.Object other = (org.omg.CORBA.Object) remoteBWB.stub_;
                    org.omg.CORBA.Object me = (org.omg.CORBA.Object) stub_;
                    result = me._is_equivalent(other);
                }
            }
        }

        return result;
    }


    public String getBusinessInterfaceName() {
        return businessInterface_;
    }


    public Object writeReplace() throws ObjectStreamException {
        return new RemoteBusinessWrapperBase(stub_, businessInterface_);
    }


    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeObject(businessInterface_);
        oos.writeObject(stub_);
    }


    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            businessInterface_ = (String) ois.readObject();
            hashCode_ = businessInterface_.hashCode();

            EJBUtils.loadGeneratedRemoteBusinessClasses(businessInterface_);

            stub_ = (Remote) ois.readObject();
        } catch (Exception e) {
            throw new IOException("RemoteBusinessWrapper.readObj error", e);
        }
    }


    public Object readResolve() throws ObjectStreamException {
        try {
            return EJBUtils.createRemoteBusinessObject(businessInterface_, stub_);
        } catch (Exception e) {
            WriteAbortedException wae = new WriteAbortedException("RemoteBusinessWrapper.readResolve error", e);
            throw wae;
        }

    }
}

