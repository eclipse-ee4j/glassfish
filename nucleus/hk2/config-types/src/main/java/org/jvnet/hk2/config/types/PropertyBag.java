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

package org.jvnet.hk2.config.types;

import org.glassfish.hk2.api.Customize;
import org.glassfish.hk2.api.Customizer;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.DuckTyped;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;

/**
 * Base interface for those configuration objects that has nested &lt;property> elements.
 * @author Kohsuke Kawaguchi
 */
@Customizer(PropertyBagCustomizer.class)
public interface PropertyBag {
    /**
     * Gets the value of the property property.
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * @return the property list
     */
    @XmlElement(name="property")
    @Element("property")
    List<Property> getProperty();
    Property addProperty(Property property);
    Property lookupProperty(String name);
    Property removeProperty(String name);
    Property removeProperty(Property removeMe);

    @Customize
    @DuckTyped
    Property getProperty(String name);

    /**
     * Returns a property value if the bean has properties and one of its
     * properties name is equal to the one passed.
     *
     * @param name the property name requested
     * @return the property value or null if not found
     */
    @Customize
    @DuckTyped
    String getPropertyValue(String name);

    /**
     * Returns a property value if the bean has properties and one of its
     * properties name is equal to the one passed. Otherwise return
     * the default value.
     *
     * @param name the property name requested
     * @param defaultValue is the default value to return in case the property
     * of that name does not exist in this bag
     * @return the property value
     */
    @Customize
    @DuckTyped
    String getPropertyValue(String name, String defaultValue);

    public class Duck {
        public static Property getProperty(PropertyBag me, String name) {
            for (Property prop : me.getProperty()) {
                if (prop.getName().equals(name)) {
                    return prop;
                }
            }
            return null;
        }

        public static String getPropertyValue(PropertyBag me, String name) {
            return getPropertyValue(me,name,null);
        }

        public static String getPropertyValue(PropertyBag me, String name, String defaultValue) {
            Property prop = getProperty(me, name);
            if (prop != null) {
                return prop.getValue();
            }
            return defaultValue;
        }
    }
}
