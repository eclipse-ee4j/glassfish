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

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.util.Hashtable;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;


/** 
 * The EventListenerProxy recieves notifications from RemoteListenerConnectors
 * registered on the server managed objects and forwards them to the corresponding
 * local listeners
 *
 * @author Hans Hrasna
 */
public class EventListenerProxy extends UnicastRemoteObject implements RemoteEventListener {
    private Hashtable listenerTable = new Hashtable();
    private Hashtable handbackTable = new Hashtable();
    private String proxyAddress;
    private static EventListenerProxy eventProxy = null;
    private static int portnum=1100;
    private String rmiName;
    private static boolean debug = false;

    public synchronized static EventListenerProxy getEventListenerProxy() {
        if (eventProxy == null) {
            EventListenerProxy newProxy = null;
            try {
                newProxy = new EventListenerProxy();
                Naming.rebind(newProxy.proxyAddress, newProxy);
                eventProxy = newProxy;
                if(debug) System.out.println(eventProxy.rmiName + " bound to existing registry at port " + portnum );

            } catch (RemoteException re) {
                if(debug) System.out.println("Naming.rebind("+ (newProxy != null ? newProxy.proxyAddress : "null") +", eventProxy): " + re);
                try {
                    newProxy = new EventListenerProxy();
                    Registry r = LocateRegistry.createRegistry(portnum);
                    r.bind(newProxy.rmiName, newProxy);
                    eventProxy = newProxy;
                    if(debug) System.out.println(newProxy.rmiName + " bound to newly created registry at port " + portnum );
                } catch(Exception e) {
                    eventProxy = null;
                    if(debug) e.printStackTrace();
                }
            } catch (Exception e) {
                if(debug) e.printStackTrace();
            }
        }
        return eventProxy;
    }

    public EventListenerProxy() throws java.rmi.RemoteException {
        String hostName;
        rmiName = "RemoteEventListener" + hashCode() + System.currentTimeMillis();
        try {
            hostName = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e) {
            hostName = "localhost";
            System.out.println(e);
        }
        proxyAddress = "//"+ hostName + ":" + portnum + "/" + rmiName;
    }

	public void handleNotification(Notification n, Object h) throws RemoteException {
        if (debug) System.out.println("EventListenerProxy:handleNotification(" + n + ")");
        NotificationListener listener = (NotificationListener)listenerTable.get((String)h);
        if (listener != null) {
            Object handback = handbackTable.get((String)h);
            listener.handleNotification(n,handback);
        } else {
            System.out.println("EventListenerProxy: listener id " + h + " not found");
        }
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public void addListener(String id, NotificationListener l, Object handback) {
        if (debug) System.out.println("EventListenerProxy.addListener()");
        listenerTable.put(id, l);
        handbackTable.put(id, handback);
    }

    public void removeListener(String id) throws ListenerNotFoundException {
        if(listenerTable.remove(id) == null) {
            throw new ListenerNotFoundException();
        } else {
            handbackTable.remove(id);
        }
    }
}

