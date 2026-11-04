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

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.markup.AttributeDescriptor;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Non default property test.
 */
public class NonDefaultPropertyTest {

    private static BeanInfo beanInfo;
    private static final Map<String, PropertyDescriptor> PROPERTIES
            = new HashMap<String, PropertyDescriptor>();

    @BeforeClass
    public static void setUp() throws Exception {
        beanInfo = Introspector.getBeanInfo(NonDefaultPropertyComponent.class);
        for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
            PROPERTIES.put(p.getName(), p);
        }
    }

    @Test
    public void testNonAttributeProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("nonAttribute");
        assertNull(p.getValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR));
    }

    @Test
    public void testNamedAttributeProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("namedAttribute");
        assertEquals(((AttributeDescriptor) p.getValue(
                (Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR))).getName(),
                "specialNamedAttribute");
    }

    @Test
    public void testRequiredAttributeProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("requiredAttribute");
        assertTrue(((AttributeDescriptor) p.getValue(
                (Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR))).isRequired());
    }

    @Test
    public void testReadOnlyProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("readOnlyProperty");
        assertNull(p.getWriteMethod());
    }

    @Test
    public void testWriteOnlyProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("writeOnlyProperty");
        assertNull(p.getReadMethod());
    }

    @Test
    public void testCategorizedProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("categorizedProperty");
        CategoryDescriptor c = (CategoryDescriptor) p
                .getValue(Constants.PropertyDescriptor.CATEGORY);
        assertNotNull(c);
        assertEquals("category", c.getName());
    }

    @Test
    public void testHiddenProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("hiddenProperty");
        assertTrue(p.isHidden());
    }

    @Test
    public void testDefaultProperty() throws Exception {
        int index = beanInfo.getDefaultPropertyIndex();
        assertTrue("No default property index", index >= 0);
        assertEquals("Wrong default property name", "defaultProperty",
                beanInfo.getPropertyDescriptors()[index].getName());
    }

    @Test
    public void testNamedProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("namedProperty");
        assertEquals("Wrong read method name", "getNamedProperty",
                p.getReadMethod().getName());
        assertEquals("Wrong write method name", "setNamedProperty",
                p.getWriteMethod().getName());
    }

    @Test
    public void testDifferentReadNamedProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("readNamedProperty");
        assertEquals("Wrong read method name", "getDifferentReadNamedProperty",
                p.getReadMethod().getName());
        assertEquals("Wrong write method name", "setReadNamedProperty",
                p.getWriteMethod().getName());
    }

    @Test
    public void testDifferentWriteNamedProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("writeNamedProperty");
        assertEquals("Wrong read method name", "getWriteNamedProperty",
                p.getReadMethod().getName());
        assertEquals("Wrong write method name", "setDifferentWriteNamedProperty",
                p.getWriteMethod().getName());
    }

    @Test
    public void testDifferentBothNamedProperty() throws Exception {
        PropertyDescriptor p = getPropertyDescriptor("bothNamedProperty");
        assertEquals("Wrong read method name", "getDifferentBothNamedProperty",
                p.getReadMethod().getName());
        assertEquals("Wrong write method name", "setDifferentBothNamedProperty",
                p.getWriteMethod().getName());
    }

    private PropertyDescriptor getPropertyDescriptor(String name) throws Exception {
        PropertyDescriptor p = PROPERTIES.get(name);
        assertNotNull("No such property: " + name, p);
        return p;
    }
}
