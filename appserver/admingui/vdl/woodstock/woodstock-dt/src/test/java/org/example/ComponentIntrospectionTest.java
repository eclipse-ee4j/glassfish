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
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test integrity of generated BeanInfo classes.
 */
public class ComponentIntrospectionTest {

    private static final Set<String> DISPLAY_NAMES = new HashSet<String>();

    @BeforeClass
    public static void setUp() throws Exception {
        DISPLAY_NAMES.add("The First");
        DISPLAY_NAMES.add("The Second");
        DISPLAY_NAMES.add("The Third");
    }

    @Test
    public void testInheritance01() throws Exception {
        introspectBean(Component01.class);
    }

    @Test
    public void testInheritance02() throws Exception {
        introspectBean(Component02.class);
    }

    @Test
    public void testInheritance03() throws Exception {
        introspectBean(Component03.class);
    }

    @Test
    public void testInheritance04() throws Exception {
        introspectBean(Component04.class);
    }

    @Test
    public void testInheritance05() throws Exception {
        introspectBean(Component05.class);
    }

    @Test
    public void testInheritance06() throws Exception {
        introspectBean(Component06.class);
    }

    @Test
    public void testInheritance07() throws Exception {
        introspectBean(Component07.class);
        introspectHiddenProperties(Component07.class);
    }

    @Test
    public void testInheritance08() throws Exception {
        introspectBean(Component08.class);
    }

    @Test
    public void testInheritance09() throws Exception {
        introspectBean(Component09.class);
    }

    @Test
    public void testInheritance10() throws Exception {
        introspectBean(Component10.class);
    }

    @Test
    public void testInheritance11() throws Exception {
        introspectBean(Component11.class);
    }

    @Test
    public void testInheritance12() throws Exception {
        introspectBean(Component12.class);
        introspectUnhiddenProperties(Component12.class);
    }

    @Test
    public void testInheritance13() throws Exception {
        introspectBean(Component13.class);
    }

    @Test
    public void testInheritance14() throws Exception {
        introspectBean(Component14.class);
        introspectUnhiddenProperties(Component14.class);
    }

    @Test
    public void testInheritance16() throws Exception {
        introspectBean(Component16.class);
        introspectUnhiddenProperties(Component16.class);
    }

    public void introspectHiddenProperties(Class c) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor p : propertyDescriptors) {
            assertTrue("Property not hidden: " + p.getName(), p.isHidden());
            assertNull("Property has attribute: " + p.getName(),
                    p.getValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR));
        }
    }

    public void introspectUnhiddenProperties(Class c) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor p : propertyDescriptors) {
            assertFalse("Property is hidden: " + p.getName(), p.isHidden());
            assertTrue("Missing category descriptor", CategoryDescriptors.FAVORITE_PROPS_CATEGORY
                    .equals(p.getValue(Constants.PropertyDescriptor.CATEGORY)));
            assertNotNull("Property missing attribute: " + p.getName(),
                    p.getValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR));
        }
    }

    public void introspectBean(Class c) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(c);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        assertEquals("Wrong number of property descriptors", 3, propertyDescriptors.length);
        for (PropertyDescriptor p : propertyDescriptors) {
            if (!p.isHidden()) {
                assertTrue("Incorrect property display name", DISPLAY_NAMES.contains(p.getDisplayName()));
            }
        }
    }

}
