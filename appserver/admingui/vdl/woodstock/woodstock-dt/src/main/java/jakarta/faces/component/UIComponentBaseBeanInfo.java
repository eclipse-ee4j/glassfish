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

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.sun.rave.designtime.Constants.BeanDescriptor.INSTANCE_NAME;
import static com.sun.rave.designtime.Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR;
import static com.sun.rave.designtime.Constants.PropertyDescriptor.CATEGORY;
import static jakarta.faces.component.CategoryDescriptorsConstants.ADVANCED;

/**
 * BeanInfo for {@link jakarta.faces.component.UIComponentBase}.
 */
public class UIComponentBaseBeanInfo extends SimpleBeanInfo {

    /**
     * Resource bundle.
     */
    protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle("jakarta.faces.component.bundle", Locale.getDefault());

    /**
     * Component class.
     */
    private final Class componentClass;

    /**
     * Unqualified class name.
     */
    private final String unqualifiedClassName;

    /**
     * The instance name.
     */
    private String instanceName;

    /**
     * The bean descriptor.
     */
    private BeanDescriptor beanDescriptor;

    /**
     * The property descriptors.
     */
    private PropertyDescriptor[] propertyDescriptors;

    /**
     * Create a new instance.
     */
    public UIComponentBaseBeanInfo() {
        this.componentClass = UIComponentBase.class;
        String className = componentClass.getName();
        unqualifiedClassName = className.substring(className.lastIndexOf(".")
                + 1);
    }

    /**
     * Create a new instance.
     *
     * @param compClass component class
     */
    protected UIComponentBaseBeanInfo(final Class compClass) {
        this.componentClass = compClass;
        String className = compClass.getName();
        unqualifiedClassName = className.substring(className.lastIndexOf(".")
                + 1);
    }

    /**
     * Get the instance name.
     * @return String
     */
    protected String getInstanceName() {
        if (instanceName == null) {
            String str = this.componentClass.getName();
            StringBuilder buffer = new StringBuilder();
            buffer.append(str.substring(0, 1).toLowerCase());
            buffer.append(str.substring(1));
            instanceName = buffer.toString();
        }
        return instanceName;
    }

    /**
     * This implementation lazily creates a new bean descriptor instance.
     * @return BeanDescriptor
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor != null) {
            return beanDescriptor;
        }
        beanDescriptor = new BeanDescriptor(componentClass);
        String name = getInstanceName();
        if (name != null) {
            beanDescriptor.setValue(INSTANCE_NAME, name);
        }
        return beanDescriptor;
    }

    /**
     * This implementation lazily creates the property descriptors.
     * @return PropertyDescriptor[]
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }

        try {
            AttributeDescriptor attrib;
            PropertyDescriptor propId = new PropertyDescriptor("id",
                    UIComponent.class, "getId", "setId");
            propId.setDisplayName(RESOURCE_BUNDLE
                    .getString("UIComponent_id_DisplayName"));
            propId.setShortDescription(RESOURCE_BUNDLE
                    .getString("UIComponent_id_Description"));
            propId.setHidden(true);
            attrib = new AttributeDescriptor("id", false, null, true);
            propId.setValue(ATTRIBUTE_DESCRIPTOR, attrib);

            PropertyDescriptor propRendered = new PropertyDescriptor(
                    "rendered", UIComponent.class, "isRendered",
                    "setRendered");
            propRendered.setDisplayName(RESOURCE_BUNDLE
                    .getString("UIComponent_rendered_DisplayName"));
            propRendered.setShortDescription(RESOURCE_BUNDLE
                    .getString("UIComponent_rendered_Description"));
            attrib = new AttributeDescriptor("rendered", false, null, true);
            propRendered.setValue(ATTRIBUTE_DESCRIPTOR, attrib);
            propRendered.setValue(CATEGORY, ADVANCED);

            propertyDescriptors = new PropertyDescriptor[]{
                propId,
                propRendered,
            };
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Utility method that returns a class loaded by name via the class-loader.
     *
     * @param name class to load
     * @return Class
     */
    protected static Class loadClass(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
