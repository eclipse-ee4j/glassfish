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

package org.glassfish.web.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * Represents the env-entry web application configuration customization.
 *
 * @author tjquinn
 */
@Configured
public interface EnvEntry extends ConfigBeanProxy {

    @Element
    String getDescription();

    void setDescription(String description);

    @Element(required = true, key = true)
    String getEnvEntryName();

    void setEnvEntryName(String entryName);

    @Element(required = true)
    String getEnvEntryType();

    void setEnvEntryType(String entryType);

    @Element(required = true)
    String getEnvEntryValue();

    void setEnvEntryValue(String entryValue);

    @Attribute(dataType = Boolean.class, defaultValue = "false")
    String getIgnoreDescriptorItem();

    void setIgnoreDescriptorItem(String ignoreDescriptor);

    /**
     * Validates the value in the env-entry-value sub-element against the
     * type stored in the env-entry-type sub-element.
     * <p>
     * @throws IllegalArgumentException if the type does not match one of the legal ones
     * @throws NumberFormatException if the value cannot be parsed according to the type
     */
    default void validateValue() {
        validateValue(getEnvEntryType(), getEnvEntryValue());
    }

    /**
     * Validates the specified value string against the indicated type.
     *
     * <p>The recognized types are (from the spec):
     *
     * <ul>
     * <li>java.lang.Boolean
     * <li>java.lang.Byte
     * <li>java.lang.Character
     * <li>java.lang.Double
     * <li>java.lang.Float
     * <li>java.lang.Integer
     * <li>java.lang.Long
     * <li>java.lang.Short
     * <li>java.lang.String
     * </ul>
     *
     * @param type valid type for env-entry-type (from the spec)
     * @param value value to be checked
     * @throws IllegalArgumentException if the type does not match one of the legal ones
     * @throws NumberFormatException if the value cannot be parsed according to the type
     */
    static void validateValue(final String type, final String value) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }
        if (value == null) {
            throw new IllegalArgumentException("value");
        }
        if (type.equals("java.lang.Boolean")) {
            Boolean.parseBoolean(value);
        } else if (type.equals("java.lang.Byte")) {
            Byte.parseByte(value);
        } else if (type.equals("java.lang.Character")) {
            if (value.length() > 1) {
                throw new IllegalArgumentException("length(\"" + value + "\") > 1");
            }
        } else if (type.equals("java.lang.Double")) {
            Double.parseDouble(value);
        } else if (type.equals("java.lang.Float")) {
            Float.parseFloat(value);
        } else if (type.equals("java.lang.Integer")) {
            Integer.parseInt(value);
        } else if (type.equals("java.lang.Long")) {
            Long.parseLong(value);
        } else if (type.equals("java.lang.Short")) {
            Short.parseShort(value);
        } else if (type.equals("java.lang.String")) {
            // no-op
        } else {
            throw new IllegalArgumentException(type);
        }
    }
}
