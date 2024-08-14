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

import jakarta.ejb.EJBHome;
import jakarta.ejb.HomeHandle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.rmi.PortableRemoteObject;

/**
 * A portable, Serializable implementation of EJBMetaData. This class can potentially be instantiated in another
 * vendor's container so it must not refer to any non-portable RI-specific classes.
 *
 */
public final class EJBMetaDataImpl implements jakarta.ejb.EJBMetaData, Serializable {
    // for backward compatibility
    private static final long serialVersionUID = 5777657175353026918L;

    private Class keyClass;
    private Class homeClass;
    private Class remoteClass;
    private boolean isSessionBean;
    private boolean isStatelessSessionBean;
    private HomeHandle homeHandle;

    // Dont serialize the EJBHome ref directly, use the HomeHandle
    transient private EJBHome ejbHomeStub;

    // this constructor is only called by the EntityContainer
    public EJBMetaDataImpl(EJBHome ejbHomeStub, Class homeClass, Class remoteClass, Class keyClass) {
        this(ejbHomeStub, homeClass, remoteClass, keyClass, false, false);
    }

    // this constructor is only called by non-entity-bean containers
    public EJBMetaDataImpl(EJBHome ejbHomeStub, Class homeClass, Class remoteClass, boolean isSessionBean, boolean isStatelessSessionBean) {
        this(ejbHomeStub, homeClass, remoteClass, null, isSessionBean, isStatelessSessionBean);
    }

    // this constructor is only called in the RI's EJB container
    public EJBMetaDataImpl(EJBHome ejbHomeStub, Class homeClass, Class remoteClass, Class keyClass, boolean isSessionBean,
            boolean isStatelessSessionBean) {
        this.ejbHomeStub = ejbHomeStub;
        this.homeHandle = new HomeHandleImpl(ejbHomeStub);
        this.keyClass = keyClass;
        this.homeClass = homeClass;
        this.remoteClass = remoteClass;
        this.isSessionBean = isSessionBean;
        this.isStatelessSessionBean = isStatelessSessionBean;
    }

    /**
     *
     */
    @Override
    public Class getHomeInterfaceClass() {
        return homeClass;
    }

    /**
     *
     */
    @Override
    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }

    /**
     *
     */
    @Override
    public EJBHome getEJBHome() {
        return ejbHomeStub;
    }

    /**
     *
     */
    @Override
    public Class getPrimaryKeyClass() {
        if (keyClass == null) {
            // for SessionBeans there is no primary key
            throw new RuntimeException("SessionBeans do not have a primary key");
        }
        return keyClass;
    }

    /**
     *
     */
    @Override
    public boolean isSession() {
        return isSessionBean;
    }

    @Override
    public boolean isStatelessSession() {
        return isStatelessSessionBean;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        isSessionBean = in.readBoolean();
        isStatelessSessionBean = in.readBoolean();

        // Use thread context classloader to load home/remote/primarykey classes
        // See EJB2.0 spec section 18.4.4
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        remoteClass = loader.loadClass(in.readUTF());
        homeClass = loader.loadClass(in.readUTF());
        if (!isSessionBean)
            keyClass = loader.loadClass(in.readUTF());

        homeHandle = (HomeHandle) in.readObject();
        ejbHomeStub = homeHandle.getEJBHome();
        // narrow the home so that the application doesnt have to do
        // a narrow after EJBMetaData.getEJBHome().
        ejbHomeStub = (EJBHome) PortableRemoteObject.narrow(ejbHomeStub, homeClass);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(isSessionBean);
        out.writeBoolean(isStatelessSessionBean);

        // Write the String names of the Class objects,
        // since Class objects cant be serialized unless the classes
        // they represent are Serializable.
        out.writeUTF(remoteClass.getName());
        out.writeUTF(homeClass.getName());
        if (!isSessionBean)
            out.writeUTF(keyClass.getName());

        out.writeObject(homeHandle);
    }
}
