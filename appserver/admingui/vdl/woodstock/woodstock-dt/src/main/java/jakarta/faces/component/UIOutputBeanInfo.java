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
import static com.sun.rave.designtime.Constants.PropertyDescriptor.CATEGORY;
import static jakarta.faces.component.CategoryDescriptorsConstants.DATA;
import static jakarta.faces.component.PropertyEditorConstants.VALUEBINDING_EDITOR;

/**
 * BeanInfo for {@link jakarta.faces.component.UICommand}.
 */
public class UIOutputBeanInfo extends UIComponentBaseBeanInfo {

    /**
     * Create a new instance.
     */
    public UIOutputBeanInfo() {
        super(UIOutput.class);
    }

    /**
     * Create a new instance.
     * @param beanClass bean class
     */
    public UIOutputBeanInfo(final Class beanClass) {
        super(beanClass);
    }

    /**
     * The property descriptors.
     */
    private PropertyDescriptor[] propertyDescriptors;

    /**
     * This implementation lazily creates the property descriptors.
     * @return PropertyDescriptor[]
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors == null) {
            try {
                List<PropertyDescriptor> descList =
                        new ArrayList<PropertyDescriptor>();
                descList.addAll(Arrays.asList(super.getPropertyDescriptors()));
                AttributeDescriptor attrib;

                PropertyDescriptor propConverter =
                        new PropertyDescriptor("converter", UIOutput.class,
                                "getConverter", "setConverter");
                propConverter.setDisplayName(RESOURCE_BUNDLE
                        .getString("UIOutput_converter_DisplayName"));
                propConverter.setShortDescription(RESOURCE_BUNDLE
                        .getString("UIOutput_converter_Description"));
                attrib = new AttributeDescriptor("converter", false, null,
                        true);
                propConverter.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
                propConverter.setValue(CATEGORY, DATA);
                descList.add(propConverter);

                PropertyDescriptor propValue =
                        new PropertyDescriptor("value", UIOutput.class,
                                "getValue", "setValue");
                propValue.setDisplayName(RESOURCE_BUNDLE
                        .getString("UICommand_value_DisplayName"));
                propValue.setShortDescription(RESOURCE_BUNDLE
                        .getString("UICommand_value_Description"));
                propValue.setPropertyEditorClass(
                        loadClass(VALUEBINDING_EDITOR));
                attrib = new AttributeDescriptor("value", false, null, true);
                propValue.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
                propValue.setValue(CATEGORY, DATA);
                descList.add(propValue);
                propertyDescriptors = (PropertyDescriptor[]) descList.toArray(
                        new PropertyDescriptor[descList.size()]);
            } catch (IntrospectionException e) {
                e.printStackTrace();
                return null;
            }
        }
        return propertyDescriptors;
    }
}
