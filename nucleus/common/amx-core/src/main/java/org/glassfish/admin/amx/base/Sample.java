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

package org.glassfish.admin.amx.base;

import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
Interface for a sample MBean , used as target for sample and test code.
Various Attributes of varying types are made available for testing.
 */
@Taxonomy(stability = Stability.NOT_AN_INTERFACE)
@AMXMBeanMetadata(singleton = true, globalSingleton = true, leaf = true, immutableMBeanInfo=false)
public interface Sample extends AMXProxy
{
    /**
    The type of Notification emitted by emitNotification().
     */
    public static final String SAMPLE_NOTIFICATION_TYPE = "Sample";

    /**
    The key to access user data within the Map obtained from Notification.getUserData().
     */
    public static final String USER_DATA_KEY = "UserData";

    /**
    Emit 'numNotifs' notifications of type
    SAMPLE_NOTIFICATION_TYPE at the specified interval.

    @param data arbitrary data which will be placed into the Notification's UserData field.
    @param numNotifs number of Notifications to issue >= 1
    @param intervalMillis interval at which Notifications should be issued >= 0
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    public void emitNotifications(final Object data, final int numNotifs, final long intervalMillis);

    /**
    Add a new Attribute. After this, the MBeanInfo will contain an MBeanAttributeInfo
    for this Attribute.

    @param name
    @param value
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    public void addAttribute(final String name, final Object value);

    /**
    Remove an Attribute. After this, the MBeanInfo will no longer
    contain an MBeanAttributeInfo for this Attribute.
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    public void removeAttribute(final String name);

    /**
    For testing bandwidth...
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    public void uploadBytes(final byte[] bytes);

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public byte[] downloadBytes(final int numBytes);

    /** explicity getter using an array, must work through proxy code */
    @ManagedAttribute
    public ObjectName[] getAllAMX();

    /** Attribute whose values will have a variety of types that should pass the AMXValidtor */
    @ManagedAttribute
    public Object[] getAllSortsOfStuff();
}










