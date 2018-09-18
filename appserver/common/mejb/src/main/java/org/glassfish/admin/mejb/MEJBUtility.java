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

package org.glassfish.admin.mejb;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.j2ee.ListenerRegistration;
import javax.management.j2ee.ManagementHome;
import javax.management.j2ee.Management;
import javax.naming.Context;
import javax.naming.InitialContext;


/**
 */
public final class MEJBUtility {
    private final MBeanServer mServer;
    private final ListenerRegistry listenerRegistry;
    private Management  mMEJB;
    
    private static final MEJBUtility INSTANCE = new MEJBUtility();

    private MEJBUtility() {
        mServer = ManagementFactory.getPlatformMBeanServer();
        listenerRegistry = _getListenerRegistry();
    }

    public static MEJBUtility getInstance() {
        return INSTANCE;
    }


    public static final String MEJB_NAME_PROP = "mejb.name";
    public static final String MEJB_DEFAULT_NAME = "ejb/mgmt/MEJB";

    public synchronized Management getMEJB() throws RemoteException {
        if (mMEJB == null) {
            try {
                final Context ic = new InitialContext();
                final String ejbName = System.getProperty( MEJB_NAME_PROP, MEJB_DEFAULT_NAME);
                final Object objref = ic.lookup(ejbName);
                final ManagementHome home = (ManagementHome) PortableRemoteObject.narrow(objref, ManagementHome.class);
                mMEJB = (Management) home.create();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return mMEJB;
    }

    public MBeanServer getMBeanServer()
    {
        return mServer;
    }

    public ListenerRegistration getListenerRegistry()
    {
        return listenerRegistry;
    }

    private static ListenerRegistry _getListenerRegistry() {
        ListenerRegistry reg = null;
        try {
            reg = new ListenerRegistry( InetAddress.getLocalHost().getHostAddress());
        } catch ( final UnknownHostException e) {
            reg = new ListenerRegistry( MEJBUtility.class.getName() );
        }
        return reg;
    }
    

    public void addNotificationListener( final ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException, RemoteException {

        if ( ! mServer.isRegistered(name) )
        {
            System.out.println( "addNotificationListener: NOT REGISTERED: " + name );
        }
        else
        {
            System.out.println( "addNotificationListener: REGISTERED: " + name );
        }
        mServer.addNotificationListener(name, listener, filter, handback);
    }

    public void removeNotificationListener( final ObjectName name, NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException, RemoteException {
        mServer.removeNotificationListener(name, listener);
    }
}
