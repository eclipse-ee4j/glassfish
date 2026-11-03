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
package org.example.base;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.markup.AttributeDescriptor;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

public class IntrospectedSuperBeanBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descriptor = new BeanDescriptor(IntrospectedSuperBean.class);
        descriptor.setDisplayName("Super Bean");
        return descriptor;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor prop = new PropertyDescriptor("introspectedListener2Expression", IntrospectedSuperBean.class, "getIntrospectedListener2Expression", "setIntrospectedListener2Expression");
            AttributeDescriptor attrib = new AttributeDescriptor("introspectedListener2Expression", false, null, true);
            prop.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib);
            return new PropertyDescriptor[]{prop};
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            EventSetDescriptor eventSetDescriptor01 = new EventSetDescriptor("introspectedAction01",
                    IntrospectedActionListener01.class,
                    new Method[]{IntrospectedActionListener01.class.getMethod(
                                "introspectedActionHappened",
                                new Class[]{IntrospectedActionEvent01.class,})},
                    IntrospectedSuperBean.class.getMethod("addIntrospectedActionListener01", new Class[]{IntrospectedActionListener01.class}),
                    IntrospectedSuperBean.class.getMethod("removeIntrospectedActionListener01", new Class[]{IntrospectedActionListener01.class}));
            EventSetDescriptor eventSetDescriptor02 = new EventSetDescriptor("introspectedAction02",
                    IntrospectedActionListener02.class,
                    new Method[]{IntrospectedActionListener02.class.getMethod(
                                "introspectedActionHappened",
                                new Class[]{IntrospectedActionEvent02.class,})},
                    IntrospectedSuperBean.class.getMethod("addIntrospectedActionListener02", new Class[]{IntrospectedActionListener02.class}),
                    IntrospectedSuperBean.class.getMethod("removeIntrospectedActionListener02", new Class[]{IntrospectedActionListener02.class}));
            eventSetDescriptor02.setValue(Constants.EventSetDescriptor.BINDING_PROPERTY, this.getPropertyDescriptors()[0]);
            return new EventSetDescriptor[]{eventSetDescriptor01, eventSetDescriptor02};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
