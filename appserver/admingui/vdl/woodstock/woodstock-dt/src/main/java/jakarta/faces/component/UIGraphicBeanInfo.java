/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package jakarta.faces.component;

import com.sun.rave.designtime.markup.AttributeDescriptor;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.rave.designtime.Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR;

/**
 * BeanInfo for {@link jakarta.faces.component.UIGraphic}.
 */
public final class UIGraphicBeanInfo extends UIComponentBaseBeanInfo {

    /**
     * Create a new instance.
     */
    public UIGraphicBeanInfo() {
        super(UIGraphic.class);
    }

    /**
     * The property descriptors.
     */
    private PropertyDescriptor[] propertyDescriptors;

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors == null) {
            try {
                List<PropertyDescriptor> descList =
                        new ArrayList<PropertyDescriptor>();
                descList.addAll(Arrays
                        .asList(super.getPropertyDescriptors()));
                AttributeDescriptor attrib;

                PropertyDescriptor propValue = new PropertyDescriptor("value",
                        UIOutput.class, "getValue", "setValue");
                propValue.setDisplayName(RESOURCE_BUNDLE
                        .getString("UICommand_value_DisplayName"));
                propValue.setShortDescription(RESOURCE_BUNDLE
                        .getString("UICommand_value_Description"));
                attrib = new AttributeDescriptor("value", false, null, true);
                propValue.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
                descList.add(propValue);
                propertyDescriptors = (PropertyDescriptor[])
                        descList.toArray(new PropertyDescriptor[0]);
            } catch (IntrospectionException e) {
                e.printStackTrace();
                return null;
            }
        }
        return propertyDescriptors;
    }
}
