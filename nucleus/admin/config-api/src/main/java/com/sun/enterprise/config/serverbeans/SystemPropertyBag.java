/*
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

import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.util.List;

/**
 * Base interface for those configuration objects that has nested &lt;system-property> elements.
 * <p>
 * <b>Important: document legal properties using PropertiesDesc, one PropertyDesc for each legal system-property</b>.
 */
public interface SystemPropertyBag extends ConfigBeanProxy {
    /**
     * Gets the list of system-property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object. This is why there is not a <CODE>set</CODE> method for the property
     * property.
     * <p/>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getSystemProperty().add(newItem);
     * </pre>
     *
     * Objects of the following type(s) are allowed in the list {@link SystemProperty }
     */
    @Element("system-property")
    List<SystemProperty> getSystemProperty();

    @DuckTyped
    SystemProperty getSystemProperty(String name);

    /**
     * Returns a property value if the bean has system properties and one of its system-property names is equal to the one
     * passed.
     *
     * @param name the system property name requested
     * @return the property value or null if not found
     */
    @DuckTyped
    String getSystemPropertyValue(String name);

    /**
     * Returns a property value if the bean has properties and one of its properties name is equal to the one passed.
     * Otherwise return the default value.
     *
     * @param name the property name requested
     */
    @DuckTyped
    String getPropertyValue(String name, String defaultValue);

    @DuckTyped
    boolean containsProperty(String name);

    class Duck {
        public static SystemProperty getSystemProperty(final SystemPropertyBag me, final String name) {
            for (final SystemProperty prop : me.getSystemProperty()) {
                if (prop.getName().equals(name)) {
                    return prop;
                }
            }
            return null;
        }

        public static String getSystemPropertyValue(final SystemPropertyBag me, final String name) {
            return getSystemPropertyValue(me, name, null);
        }

        public static String getSystemPropertyValue(final SystemPropertyBag me, final String name, final String defaultValue) {
            final SystemProperty prop = getSystemProperty(me, name);
            if (prop != null) {
                return prop.getValue();
            }
            return defaultValue;
        }

        public static boolean containsProperty(SystemPropertyBag me, String name) {
            return me.getSystemProperty(name) != null;
        }
    }
}
