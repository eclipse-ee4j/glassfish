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

import java.rmi.RemoteException;
import java.util.Hashtable;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import javax.management.j2ee.Management;


/** 
 * ListenerRegistry provides an implementation of ListenerRegistration
 * This implementation creates instances of RemoteListenerConnectors which
 * are registered on the MEJB on behalf of the local listener.
 *
 * Note that this cannot possibly work for remote listeners due to MEJBUtility not supporting 
 * anything but the local MBeanServer.
 *
 * @author Hans Hrasna
 */
public final class ListenerRegistry implements javax.management.j2ee.ListenerRegistration {
    //private static final String OMG_ORB_INIT_PORT_PROPERTY = "org.omg.CORBA.ORBInitialPort";
    
    private static final boolean debug = true;
    private static void debug( final String s ) { if ( debug ) { System.out.println(s); } }

    private final Hashtable<NotificationListener, RemoteListenerConnector> listenerConnectors =
            new Hashtable<NotificationListener, RemoteListenerConnector>();
    private final String serverAddress; // the hostname or ip address of the MEJB

    public ListenerRegistry(String ip) {
        serverAddress = ip;
    }

    /**
     * Add a listener to a registered managed object.
     *
     * @param name The name of the managed object on which the listener should be added.
     * @param listener The listener object which will handle the notifications emitted by the registered managed object.
     * @param filter The filter object. If filter is null, no filtering will be performed before handling notifications.
     * @param handback The context to be sent to the listener when a notification is emitted.
     *
     * @exception InstanceNotFoundException The managed object name provided does not match any of the registered managed objects.
     *
     */
    public void addNotificationListener(
            final ObjectName name,
            final NotificationListener listener,
            final NotificationFilter filter,
            final Object handback)
            throws RemoteException {
        final String proxyAddress = EventListenerProxy.getEventListenerProxy().getProxyAddress();
        try {
            debug("ListenerRegistry:addNotificationListener() to " + name);
            final RemoteListenerConnector connector = new RemoteListenerConnector(proxyAddress);
            getMEJBUtility().addNotificationListener(name, connector, filter, connector.getId());
            EventListenerProxy.getEventListenerProxy().addListener(connector.getId(), listener, handback);
            listenerConnectors.put(listener, connector);
        } catch (final InstanceNotFoundException inf) {
            throw new RemoteException(inf.getMessage(), inf);
        }
    }

    /**
     * Remove a listener from a registered managed object.
     *
     * @param name The name of the managed object on which the listener should be removed.
     * @param listener The listener object which will handle the notifications emitted by the registered managed object.
     * This method will remove all the information related to this listener.
     *
     * @exception InstanceNotFoundException The managed object name provided does not match any of the registered managed objects.
     * @exception ListenerNotFoundException The listener is not registered in the managed object.
     */
    public void removeNotificationListener(
            final ObjectName name,
            final NotificationListener listener)
            throws RemoteException {
        final EventListenerProxy proxy = EventListenerProxy.getEventListenerProxy();
        try {
            debug("ListenerRegistry.removeNotificationListener: " + listener + " for " + name);
            //debug("ListenerRegistry.listenerProxy = " + listenerConnectors.get(((RemoteListenerConnector) listener).getId()));
            final RemoteListenerConnector connector = listenerConnectors.get(listener);
            getMEJBUtility().removeNotificationListener(name, connector);
            proxy.removeListener(connector.getId());
            listenerConnectors.remove(listener);
        } catch (final InstanceNotFoundException inf) {
            throw new RemoteException(inf.getMessage(), inf);
        } catch (final ListenerNotFoundException lnf) {
            throw new RemoteException(lnf.getMessage(), lnf);
        }
    }

    private MEJBUtility getMEJBUtility() {
        return MEJBUtility.getInstance();
    }
}
