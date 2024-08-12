/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.config;

import jakarta.annotation.Priority;

import org.eclipse.microprofile.config.spi.Converter;

/**
 * A patched class converter to use the current context classloader, rather than the bundle classloader.
 * This will allow access to webapp classes
 */
@Priority(2)
@SuppressWarnings("rawtypes")
public class ApplicationClassConverter implements Converter<Class> {

    @Override
    public Class<?> convert(String stringValue) throws IllegalArgumentException, NullPointerException {
        try {
            return Class.forName(stringValue, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to convert property " + stringValue + " to class", e);
        }
    }

}
