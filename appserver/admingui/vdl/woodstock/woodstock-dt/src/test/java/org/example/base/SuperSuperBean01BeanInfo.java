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
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SuperSuperBean01BeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descriptor = new BeanDescriptor(SuperSuperBean01.class);
        descriptor.setDisplayName("Super Super Bean 01");
        return descriptor;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor("one", SuperSuperBean01.class, "getOne", "setOne");
            descriptor.setDisplayName("The First");
            AttributeDescriptor attributeDescriptor = new AttributeDescriptor("one", false, null, true);
            descriptor.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attributeDescriptor);
            descriptor.setValue(Constants.PropertyDescriptor.CATEGORY, CategoryDescriptors.CATEGORY);
            return new PropertyDescriptor[]{descriptor};
        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
