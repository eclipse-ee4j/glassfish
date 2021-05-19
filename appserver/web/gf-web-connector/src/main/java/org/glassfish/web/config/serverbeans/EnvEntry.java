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

package org.glassfish.web.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;

/**
 * Represents the env-entry web application configuration customization.
 *
 * @author tjquinn
 */
@Configured
public interface EnvEntry extends ConfigBeanProxy {

    @Element
    public String getDescription();
    public void setDescription(String value);

    @Element(required=true,key=true)
    public String getEnvEntryName();
    public void setEnvEntryName(String value);

    @Element(required=true)
    public String getEnvEntryType();
    public void setEnvEntryType(String value);

    @Element(required=true)
    public String getEnvEntryValue();
    public void setEnvEntryValue(String value);


    @Attribute(dataType=Boolean.class, defaultValue="false")
    public String getIgnoreDescriptorItem();
    public void setIgnoreDescriptorItem(String value);

    /**
     * Validates the value in the env-entry-value subelement against the
     * type stored in the env-entry-type subelement.
     * <p>
     * @throws IllegalArgumentException if the type does not match one of the legal ones
     * @throws NumberFormatException if the value cannot be parsed according to the type
     */
    @DuckTyped
    public void validateValue();

    public class Duck {
        public static void validateValue(final EnvEntry me) {
            String type = me.getEnvEntryType();
            String value = me.getEnvEntryValue();
            Util.validateValue(type, value);
        }
    }

    /**
     * Utility class.
     */
    public class Util {

        /**
         * Validates the specified value string against the indicated type.  The
         * recognized types are (from the spec):
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
        public static void validateValue(final String type, final String value) {
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
}
