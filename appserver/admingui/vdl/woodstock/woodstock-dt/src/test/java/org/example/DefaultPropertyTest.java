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
import com.sun.rave.designtime.markup.AttributeDescriptor;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test integrity of generated BeanInfo classes.
 */
public class DefaultPropertyTest {

    @Test
    public void testDefaultPropertyComponent01() throws Exception {
        this.introspectBean(DefaultPropertyComponent01.class);
    }

    @Test
    public void testDefaultPropertyComponent02() throws Exception {
        this.introspectBean(DefaultPropertyComponent02.class);
    }

    @Test
    public void testDefaultPropertyComponent03() throws Exception {
        this.introspectBean(DefaultPropertyComponent03.class);
    }

    @Test
    public void testDefaultPropertyComponent04() throws Exception {
        this.introspectBean(DefaultPropertyComponent04.class);
    }

    @Test
    public void testDefaultPropertyComponent05() throws Exception {
        this.introspectBean(DefaultPropertyComponent05.class);
    }

    @Test
    public void testDefaultPropertyComponent06() throws Exception {
        this.introspectBean(DefaultPropertyComponent06.class);
    }

    @Test
    public void testDefaultPropertyComponent07() throws Exception {
        this.introspectBean(DefaultPropertyComponent07.class);
    }

    @Test
    public void testDefaultPropertyComponent08() throws Exception {
        this.introspectBean(DefaultPropertyComponent08.class);
    }

    @Test
    public void testDefaultPropertyComponent09() throws Exception {
        this.introspectBean(DefaultPropertyComponent09.class);
    }

    @Test
    public void testDefaultPropertyComponent10() throws Exception {
        this.introspectBean(DefaultPropertyComponent10.class);
    }

    @Test
    public void testDefaultPropertyComponent11() throws Exception {
        this.introspectBean(DefaultPropertyComponent11.class);
    }

    @Test
    public void testDefaultPropertyComponent12() throws Exception {
        this.introspectBean(DefaultPropertyComponent12.class);
    }

    @Test
    public void testDefaultPropertyComponent13() throws Exception {
        this.introspectBean(DefaultPropertyComponent13.class);
    }

    @Test
    public void testDefaultPropertyComponent14() throws Exception {
        this.introspectBean(DefaultPropertyComponent14.class);
    }

    @Test
    public void testDefaultPropertyComponent15() throws Exception {
        this.introspectBean(DefaultPropertyComponent15.class);
    }

    public void introspectBean(Class c) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        assertEquals("Wrong number of property descriptors", 1, propertyDescriptors.length);
        PropertyDescriptor propertyDescriptor = propertyDescriptors[0];
        assertEquals("Wrong property name: " + propertyDescriptor.getName(),
                "defaultProperty", propertyDescriptor.getName());
        assertEquals("Wrong property display name: " + propertyDescriptor.getDisplayName(),
                "defaultProperty", propertyDescriptor.getDisplayName());
        assertEquals("Wrong property short description name: " + propertyDescriptor.getShortDescription(),
                "defaultProperty", propertyDescriptor.getShortDescription());
        assertEquals("Wrong read method name: " + propertyDescriptor.getReadMethod().getName(),
                "getDefaultProperty", propertyDescriptor.getReadMethod().getName());
        assertEquals("Wrong write method name: " + propertyDescriptor.getWriteMethod().getName(),
                "setDefaultProperty", propertyDescriptor.getWriteMethod().getName());
        AttributeDescriptor attributeDescriptor
                = (AttributeDescriptor) propertyDescriptor.getValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR);
        assertNotNull("No attribute descriptor", attributeDescriptor);
        assertEquals("Wrong attribute name: " + attributeDescriptor.getName(),
                "defaultProperty", attributeDescriptor.getName());
        assertTrue("Attribute is not bindable", attributeDescriptor.isBindable());
        assertFalse("Attribute is required", attributeDescriptor.isRequired());
    }

}
