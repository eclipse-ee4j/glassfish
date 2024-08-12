/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.naming;

import com.sun.appserv.connectors.internal.spi.ConnectorNamingEvent;
import com.sun.appserv.connectors.internal.spi.ConnectorNamingEventListener;

import java.util.ArrayList;

/**
 * Notifier for connector resource naming events
 * @author Jagadish Ramu
 */
public class ConnectorResourceNamingEventNotifier implements ConnectorNamingEventNotifier{

     private ArrayList<ConnectorNamingEventListener> listeners;
     private static ConnectorResourceNamingEventNotifier notifier;

     private ConnectorResourceNamingEventNotifier(){
        listeners= new ArrayList<>();
     }

      /**
       * Returns an instance of event notifier (singleton)
       * @return  ConnectorNamingEventNotifier
       */
     public static synchronized ConnectorNamingEventNotifier getInstance(){
         if(notifier == null){
            notifier = new ConnectorResourceNamingEventNotifier();
         }
        return notifier;
     }

    /**
     * To add Listener which gets notified when the event happens
     * @param listener
     */
    @Override
    public void addListener(ConnectorNamingEventListener listener){
        listeners.add(listener);
    }

    /**
     * To remove listener such that it wont be notified anymore.
     * @param listener
     */
    @Override
    public void removeListener(ConnectorNamingEventListener listener){
        listeners.remove(listener);
    }

    /**
     * Notifies all the registered listeners about the naming event.
     * @param event
     */
    @Override
    public void notifyListeners(ConnectorNamingEvent event){

        for(ConnectorNamingEventListener listener : listeners){
            listener.connectorNamingEventPerformed(event);
        }
    }
}
