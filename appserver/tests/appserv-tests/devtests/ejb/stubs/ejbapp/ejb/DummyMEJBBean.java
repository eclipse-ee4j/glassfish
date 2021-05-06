/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.stubs.ejbapp;

import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;

import java.util.*;
//import javax.management.*;
//import javax.management.j2ee.*;

public class DummyMEJBBean {} /**
implements SessionBean {

    private SessionContext sc;

    public DummyMEJBBean() {}

    public void ejbCreate() throws RemoteException {
        System.out.println("In DummyMEJBBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public Set queryNames(ObjectName name, QueryExp query) {
        return new HashSet();
    }

    public boolean isRegistered(ObjectName name) {
        return false;
    }

    public Integer getMBeanCount() {

        // Invoking this method from another app will verify that
        // the context class loader for this app is set appropriately during
        // the invocation.   The javax.management.j2ee apis are packaged
        // as part of the server's classpath.  That matches the behavior of
        // Bug 6342495, which showed that the context class loader was still
        // set to the calling application when the Home/Remote interfaces were
        // loaded from above the application classloader level.
        //
        // Calling getClassLoader tests that the context classloader is set
        // appropriately because of the security requirements that J2SE
        // imposes on requesting a class loader.  By default application code
        // in the appserver does not have the RuntimePermission
        // "getClassLoader".   In that case, requesting a class loader is only
        // allowed if the returned class loader matches the requesting
        // class' class loader(or is a child of it).  So, if the context
        // class loader is still incorrectly set to the calling app's
        // class loader, the call to getContextClassLoader() should throw
        // a security exception.

        // The advantage to writing the test using
        // javax.management APIs and this behavior is it will work on an
        // out-of-the-box installation of the appserver.  No additions to the
        // server's classpath or the default server.policy file are needed.


        System.out.println("In DummyEJBBean::getMBeanCount()");

        System.out.println("My classloader = " +
                           DummyMEJBBean.class.getClassLoader());

        // This should fail with a security exception if the caller is in
        // a separate app within the same server and the context class
        // loader is still incorrectly set to the caller app's classloader.
        System.out.println("context class loader = " +
                           Thread.currentThread().getContextClassLoader());


        return new Integer(0);
    }


    public MBeanInfo getMBeanInfo(ObjectName name) throws IntrospectionException, InstanceNotFoundException, ReflectionException {
        return null;
    }

    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return null;
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
        return null;
    }

    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        return;
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        return null;
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws  InstanceNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    public String getDefaultDomain() {
        return null;
    }

    public ListenerRegistration getListenerRegistry() {
        return null;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
                              */
