/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.validator.Validator;

import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.rave.designtime.Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR;
import static com.sun.rave.designtime.Constants.PropertyDescriptor.CATEGORY;
import static jakarta.faces.component.CategoryDescriptorsConstants.ADVANCED;
import static jakarta.faces.component.CategoryDescriptorsConstants.DATA;

/**
 * BeanInfo for {@link jakarta.faces.component.UIInput}.
 */
public final class UIInputBeanInfo extends UIOutputBeanInfo {

    /**
     * Create a new instance.
     */
    public UIInputBeanInfo() {
        super(UIInput.class);
    }

    /**
     * The property descriptors.
     */
    private PropertyDescriptor[] propertyDescriptors;

    /**
     * The event descriptors.
     */
    private EventSetDescriptor[] eventSetDescriptors;

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors == null) {
            try {
                List<PropertyDescriptor> descList =
                        new ArrayList<PropertyDescriptor>();
                descList.addAll(Arrays
                        .asList(super.getPropertyDescriptors()));
                AttributeDescriptor attrib;

                PropertyDescriptor propImmediate =
                        new PropertyDescriptor("immediate", UIInput.class,
                                "isImmediate", "setImmediate");
                propImmediate.setDisplayName(RESOURCE_BUNDLE
                        .getString("UIInput_immediate_DisplayName"));
                propImmediate.setShortDescription(RESOURCE_BUNDLE
                        .getString("UIInput_immediate_Description"));
                attrib = new AttributeDescriptor("immediate", false, null,
                        true);
                propImmediate.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
                propImmediate.setValue(CATEGORY, ADVANCED);
                descList.add(propImmediate);

                PropertyDescriptor propRequired =
                        new PropertyDescriptor("required", UIInput.class,
                                "isRequired", "setRequired");
                propRequired.setDisplayName(RESOURCE_BUNDLE
                        .getString("UIInput_required_DisplayName"));
                propRequired.setShortDescription(RESOURCE_BUNDLE
                        .getString("UIInput_required_Description"));
                attrib = new AttributeDescriptor("required", false, null, true);
                propRequired.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
                propRequired.setValue(CATEGORY, DATA);
                descList.add(propRequired);

                PropertyDescriptor propSubmittedValue =
                        new PropertyDescriptor("submittedValue", UIInput.class,
                                "getSubmittedValue", "setSubmittedValue");
                propSubmittedValue.setHidden(true);
                descList.add(propSubmittedValue);

                PropertyDescriptor propLocalValue =
                        new PropertyDescriptor("localValue", UIInput.class,
                                "getlocalValue", null);
                propLocalValue.setHidden(true);
                descList.add(propLocalValue);
                propertyDescriptors = (PropertyDescriptor[]) descList.toArray(
                        new PropertyDescriptor[descList.size()]);
            } catch (IntrospectionException e) {
                e.printStackTrace();
                return null;
            }
        }
        return propertyDescriptors;
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSetDescriptors == null) {
            try {
                EventSetDescriptor valueChangeEventDescriptor =
                        new EventSetDescriptor("valueChange",
                                ValueChangeListener.class, new Method[]{
                                    ValueChangeListener.class
                                            .getMethod("processValueChange",
                                                    new Class[]{
                                                        ValueChangeEvent.class,
                                                    })},
                                UIInput.class
                                        .getMethod("addValueChangeListener",
                                                new Class[]{
                                                    ValueChangeListener.class,
                                                }),
                                UIInput.class
                                        .getMethod("removeValueChangeListener",
                                                new Class[]{
                                                    ValueChangeListener.class,
                                                }),
                                UIInput.class
                                        .getMethod("getValueChangeListeners",
                                                new Class[]{}));
                EventSetDescriptor validateEventDescriptor =
                        new EventSetDescriptor("validate", Validator.class,
                                new Method[]{
                                    Validator.class
                                        .getMethod("validate", new Class[]{
                                            FacesContext.class,
                                            UIComponent.class,
                                            Object.class,
                                        })
                                },
                                null, null);
                eventSetDescriptors = new EventSetDescriptor[]{
                    valueChangeEventDescriptor,
                    validateEventDescriptor,
                };
            } catch (IntrospectionException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            }
        }
        return eventSetDescriptors;
    }
}
