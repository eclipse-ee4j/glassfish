/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.example;

import com.sun.rave.designtime.Constants;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test integrity of generated BeanInfo classes.
 */
public class EventInheritanceTest {

    @Test
    public void testInheritanceComponent01() throws Exception {
        this.introspectBeanTest01(ComponentBean01.class);
    }

    @Test
    public void testInheritanceComponent02() throws Exception {
        this.introspectBeanTest01(ComponentBean02.class);
    }

    @Test
    public void testInheritanceComponent03() throws Exception {
        this.introspectBeanTest01(ComponentBean03.class);
    }

    @Test
    public void testInheritanceComponent04() throws Exception {
        this.introspectBeanTest01(ComponentBean04.class);
    }

    @Test
    public void testInheritanceComponent05() throws Exception {
        this.introspectBeanTest02(ComponentBean05.class);
    }

    @Test
    public void testInheritanceComponent06() throws Exception {
        this.introspectBeanTest02(ComponentBean06.class);
    }

    public void introspectBeanTest01(Class c) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        EventSetDescriptor[] eventDescriptors = beanInfo
                .getEventSetDescriptors();
        PropertyDescriptor[] propertyDescriptors = beanInfo
                .getPropertyDescriptors();
        assertEquals("Wrong number of event descriptors", 2,
                eventDescriptors.length);
        assertEquals("Wrong number of property descriptors", 1,
                propertyDescriptors.length);
        EventSetDescriptor introspectedAction01Event = findEventDescriptors(
                eventDescriptors, "introspectedAction01");
        assertNotNull("No such event: introspectedAction01",
                introspectedAction01Event);
        EventSetDescriptor introspectedAction02Event = findEventDescriptors(
                eventDescriptors, "introspectedAction02");
        assertNotNull("No such event: introspectedAction02",
                introspectedAction02Event);
        PropertyDescriptor introspectedAction02EventListener =
                propertyDescriptors[0];
        assertTrue(introspectedAction02EventListener.equals(
                introspectedAction02Event.getValue(
                        Constants.EventSetDescriptor.BINDING_PROPERTY)));
    }

    public void introspectBeanTest02(Class c) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        EventSetDescriptor[] eventSetDescriptors = beanInfo
                .getEventSetDescriptors();
        PropertyDescriptor[] propertyDescriptors = beanInfo
                .getPropertyDescriptors();
        assertEquals("Wrong number of event descriptors", 2,
                eventSetDescriptors.length);
        assertEquals("Wrong number of property descriptors", 2,
                propertyDescriptors.length);
        EventSetDescriptor introspectedAction01Event = findEventDescriptors(
                eventSetDescriptors, "introspectedAction01");
        assertNotNull("No such event: introspectedAction01",
                introspectedAction01Event);
        EventSetDescriptor introspectedAction02Event = findEventDescriptors(
                eventSetDescriptors, "introspectedAction02");
        assertNotNull("No such event: introspectedAction02",
                introspectedAction02Event);
        PropertyDescriptor introspectedAction01EventListener =
                findPropertyDescriptor(propertyDescriptors,
                        "introspectedListener1Expression");
        assertTrue(introspectedAction01EventListener.equals(
                introspectedAction01Event.getValue(Constants.EventSetDescriptor
                        .BINDING_PROPERTY)));
        PropertyDescriptor introspectedAction02EventListener
                = findPropertyDescriptor(propertyDescriptors,
                        "introspectedListener2Expression");
        assertTrue(introspectedAction02EventListener.equals(
                introspectedAction02Event.getValue(Constants.EventSetDescriptor
                        .BINDING_PROPERTY)));
    }

    private static EventSetDescriptor findEventDescriptors(
            EventSetDescriptor[] eventSetDescriptors, String name) {

        for (EventSetDescriptor d : eventSetDescriptors) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }

    private static  PropertyDescriptor findPropertyDescriptor(
            PropertyDescriptor[] propertyDescriptors, String name) {

        for (PropertyDescriptor d : propertyDescriptors) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }
}
