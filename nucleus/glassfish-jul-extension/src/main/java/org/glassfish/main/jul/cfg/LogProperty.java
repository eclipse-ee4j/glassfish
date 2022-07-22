/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.cfg;

/**
 * Representation of a key in logging.properties file.
 * <p>
 * The representation is basically relative to some bussiness object of the JUL system..
 *
 * @author David Matejcek
 */
@FunctionalInterface
public interface LogProperty {
    // note: usual usage is implementing by an enum, so don't shorten getter names,
    // it would be confusing with enum.name().

    /**
     * @return a name of the property, used as a last part of property name in logging.properties
     */
    String getPropertyName();


    /**
     * Concatenates the {@link Class#getName()} with a dot and {@link #getPropertyName()}
     *
     * @param bussinessObjectClass
     * @return complete name of the property, used to access the value.
     */
    default String getPropertyFullName(final Class<?> bussinessObjectClass) {
        return getPropertyFullName(bussinessObjectClass.getName());
    }


    /**
     * Concatenates the prefix with a dot and {@link #getPropertyName()}.
     * If the prefix is null or an empty String, returns just {@link #getPropertyName()} without
     * the dot.
     *
     * @param prefix
     * @return complete name of the property, used to access the value.
     */
    default String getPropertyFullName(final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getPropertyName();
        }
        return prefix + "." + getPropertyName();
    }
}
