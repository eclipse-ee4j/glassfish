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

package com.sun.appserv.connectors.internal.spi;

/**
 * Class representing Connector Naming Event.<br>
 * Can be used to represent different states. Eg: Object-Bind, Object-Rebind, Object-Remove
 * @author Jagadish Ramu
 */
public class ConnectorNamingEvent{

    private String jndiName; //name of the object
    private int eventType;
    public static final int EVENT_OBJECT_REBIND = 0;


    public ConnectorNamingEvent(String jndiName, int eventType){
        this.jndiName=jndiName;
        this.eventType= eventType;
    }

    /**
     * To get JndiName of the object
     * @return   jndiName
     */
    public String getJndiName(){
        return jndiName;
    }

    /**
     * Info about the type of event that has occurred.
     * @return    eventType
     */
    public int getEventType(){
        return eventType;
    }

    /**
     * Returns the state of the object
     * @return  String
     */
    public String toString(){
        StringBuffer objectState = new StringBuffer(  "ConnectorNamingEvent : " +
                "{"+ jndiName +", " + getEventName(eventType) + "}" );

        return objectState.toString();
    }

    /**
     * returns the name of event type.
     * @param eventType
     * @return  eventName
     */
    private String getEventName(int eventType){

        String eventName = "Undefined";
        switch(eventType){
            case EVENT_OBJECT_REBIND :
                eventName= "OBJECT_REBIND_EVENT";
                break;
            default:
        }
        return eventName;
    }
}
