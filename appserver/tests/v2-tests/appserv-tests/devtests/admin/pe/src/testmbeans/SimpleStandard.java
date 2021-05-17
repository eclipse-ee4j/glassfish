/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * SimpleStandard.java
 *
 * Created on Sat Jul 02 01:54:54 PDT 2005
 */
package testmbeans;
import javax.management.*;

/**
 * Class SimpleStandard
 * SimpleStandard Description
 */
public class SimpleStandard implements SimpleStandardMBean, NotificationEmitter
{
    /** Attribute : Color */
    private String color = null;

    /** Attribute : State */
    private boolean state = false;

   /* Creates a new instance of SimpleStandard */
    public SimpleStandard()
    {
    }

   /**
    * Get This is the Color Attribute.
    */
    public String getColor()
    {
        return color;
    }

   /**
    * Set This is the Color Attribute.
    */
    public void setColor(String value)
    {
        color = value;
    }

   /**
    * Get This is the State Attribute
    */
    public boolean getState()
    {
        return state;
    }

   /**
    * Set This is the State Attribute
    */
    public void setState(boolean value)
    {
        state = value;
    }

   /**
    * Greets someone
    * @param name <code>String</code> The person to greet
    */
    public void greet(String name)
    {
        System.out.println("Hello, " + name);
    }

   /**
    * MBean Notification support
    * You shouldn't update these methods
    */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    public void addNotificationListener(NotificationListener listener,
       NotificationFilter filter, Object handback)
       throws IllegalArgumentException {
         broadcaster.addNotificationListener(listener, filter, handback);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
         return new MBeanNotificationInfo[] {
               new MBeanNotificationInfo(new String[] {
                      AttributeChangeNotification.ATTRIBUTE_CHANGE},
                      javax.management.AttributeChangeNotification.class.getName(),
                      "Usual Attribute Change Notification")
                };
    }

    public void removeNotificationListener(NotificationListener listener)
       throws ListenerNotFoundException {
         broadcaster.removeNotificationListener(listener);
    }

    public void removeNotificationListener(NotificationListener listener,
       NotificationFilter filter, Object handback)
       throws ListenerNotFoundException {
         broadcaster.removeNotificationListener(listener, filter, handback);
    }
    // </editor-fold>

    private synchronized long getNextSeqNumber() {
         return seqNumber++;
    }

    private long seqNumber;
    private final NotificationBroadcasterSupport broadcaster =
               new NotificationBroadcasterSupport();

}
