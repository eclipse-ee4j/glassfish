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
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/** 
 * RemoteListenerConnectors are instantiated by the ListenerRegistry
 * with the address of the callback port of the RemoteEventListener
 * and then registered with the MBeanServer by the MEJB.
 *
 * @author Hans Hrasna
 */
public final class RemoteListenerConnector implements NotificationListener, java.io.Serializable {
    private static final boolean debug = true;
    private static void debug( final String s ) { if ( debug ) { System.out.println(s); } }

    private final String proxyAddress;	// the RMI address of the remote event listener
    private RemoteEventListener listener = null;  //the remote event listener
    private transient MBeanServer server = null;    // the MBeanServer holds the object this is listening to
                                   		  // which is set when this is registered in the MEJB
    private final String id = hashCode() + ":" + System.currentTimeMillis();

    public RemoteListenerConnector(String address) {
        proxyAddress = address;
    }

    public synchronized void handleNotification(Notification evt, Object h) {
        try {
            debug("RemoteListenerConnector.handleNotification()");
            if (listener == null) {
            	listener = (RemoteEventListener)Naming.lookup(proxyAddress);
            }
            listener.handleNotification(evt, h);
        } catch (java.rmi.RemoteException ce) {
            if (server != null) {
                debug("RemoteListenerConnector.server.removeNotificationListener("+ (ObjectName)evt.getSource() + ", " + this + ")");
                try {
                    server.removeNotificationListener((ObjectName)evt.getSource(), this);
                } catch (javax.management.ListenerNotFoundException e) {
                    debug(toString() + ": " + e); //occurs normally if event was fowarded from J2EEDomain
                } catch (Exception e1) {
                    debug(toString() + ": " + e1);
                }
            }
        } catch (Exception e) {
            debug(toString() + ": " + e);
            if (debug) {
            	try {
                	debug("Naming.list(\"//localhost:1100\")");
                	String [] names = Naming.list("//localhost:1100");
            		for(int x=0;x<names.length;x++) {
                		debug("names["+x+"] = " + names[x]);
            		}
            	} catch(Exception e1) {
                	e1.printStackTrace();
            	}
            }
        }
    }

    public String getId() {
        return id;
    }

    public synchronized void setMBeanServer(MBeanServer s) {
        server = s;
    }
}
