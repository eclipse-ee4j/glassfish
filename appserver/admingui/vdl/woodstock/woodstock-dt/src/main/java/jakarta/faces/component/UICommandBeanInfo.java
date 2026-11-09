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

import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

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
import static jakarta.faces.component.PropertyEditorConstants.METHODBINDING_EDITOR;
import static jakarta.faces.component.PropertyEditorConstants.VALUEBINDING_EDITOR;

/**
 * BeanInfo for {@link jakarta.faces.component.UICommand}.
 */
public final class UICommandBeanInfo extends UIComponentBaseBeanInfo {

    /**
     * Create a new instance.
     */
    public UICommandBeanInfo() {
        super(UICommand.class);
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
                descList.addAll(Arrays.asList(super.getPropertyDescriptors()));
                AttributeDescriptor attrib;

                PropertyDescriptor propActionExpr =
                        new PropertyDescriptor("actionExpression",
                                UICommand.class, "getActionExpression",
                                "setActionExpression");
                propActionExpr.setDisplayName(RESOURCE_BUNDLE
                        .getString("UICommand_actionExpression_DisplayName"));
                propActionExpr.setShortDescription(RESOURCE_BUNDLE
                        .getString("UICommand_actionExpression_Description"));
                propActionExpr.setPropertyEditorClass(
                        loadClass(METHODBINDING_EDITOR));
                attrib = new AttributeDescriptor("actionExpression", false,
                        null, true);
                propActionExpr.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
                descList.add(propActionExpr);

                PropertyDescriptor propImmediate =
                        new PropertyDescriptor("immediate", UICommand.class,
                                "isImmediate", "setImmediate");
                propImmediate.setDisplayName(RESOURCE_BUNDLE
                        .getString("UICommand_immediate_DisplayName"));
                propImmediate.setShortDescription(RESOURCE_BUNDLE
                        .getString("UICommand_immediate_Description"));
                attrib = new AttributeDescriptor("immediate", false, null,
                        true);
                propImmediate.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
                propImmediate.setValue(CATEGORY, ADVANCED);
                descList.add(propImmediate);

                PropertyDescriptor propValue =
                        new PropertyDescriptor("value", UICommand.class,
                                "getValue", "setValue");
                propValue.setDisplayName(RESOURCE_BUNDLE
                        .getString("UICommand_value_DisplayName"));
                propValue.setShortDescription(RESOURCE_BUNDLE
                        .getString("UICommand_value_Description"));
                propValue.setPropertyEditorClass(
                        loadClass(VALUEBINDING_EDITOR));
                attrib = new AttributeDescriptor("value", false, null, true);
                propValue.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
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

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSetDescriptors == null) {
            try {
                EventSetDescriptor actionEventDescriptor =
                        new EventSetDescriptor("action", ActionListener.class,
                                new Method[]{
                                    ActionListener.class
                                            .getMethod("processAction",
                                                    new Class[]{
                                                        ActionEvent.class,
                                                    })
                                },
                                UICommand.class
                                        .getMethod("addActionListener",
                                                new Class[]{
                                                    ActionListener.class,
                                                }),
                                UICommand.class
                                        .getMethod("removeActionListener",
                                                new Class[]{
                                                    ActionListener.class,
                                                }),
                                UICommand.class
                                        .getMethod("getActionListeners",
                                                new Class[]{}));
                eventSetDescriptors = new EventSetDescriptor[]{
                    actionEventDescriptor,
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
