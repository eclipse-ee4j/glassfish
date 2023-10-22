/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import java.util.List;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Element;

/**
 * Base interface for those configuration objects that has nested
 * {@literal <system-property>} elements.
 */
public interface SystemPropertyBag extends ConfigBeanProxy {
    /**
     * Gets the list of system-property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the object. This is why there is not a {@code set} method for the {@code property}
     * property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getSystemProperty().add(newItem);
     * </pre>
     *
     * <p></p>Objects of the following type(s) are allowed in the list {@link SystemProperty}
     */
    @Element("system-property")
    List<SystemProperty> getSystemProperty();

    default SystemProperty getSystemProperty(String name) {
        for (final SystemProperty property : getSystemProperty()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Returns a property value if the bean has system properties and one of its
     * system-property names is equal to the one passed.
     *
     * @param name the system property name requested
     * @return the property value or null if not found
     */
    default String getSystemPropertyValue(String name) {
        return getSystemPropertyValue(name, null);
    }

    /**
     * Returns a property value if the bean has properties and one of its properties
     * name is equal to the one passed. Otherwise, return the default value.
     *
     * @param name the property name requested
     */
    default String getSystemPropertyValue(String name, String defaultValue) {
        final SystemProperty property = getSystemProperty(name);
        if (property != null) {
            return property.getValue();
        }
        return defaultValue;
    }

    default boolean containsProperty(String name) {
        return getSystemProperty(name) != null;
    }
}
