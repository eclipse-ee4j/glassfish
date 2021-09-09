/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.utils;

import java.lang.reflect.Field;

/**
 * Utilities to create a configured Habitat and cache them
 *
 * @author David Matejcek
 * @deprecated Using reflection in tests is considered as bad practice. Use it only if you don't
 *             have another option.
 */
@Deprecated
public class ReflectionUtils {

    /**
     * Sets the static field of the class to the given value.
     */
    public static void setStaticField(final Class<?> clazz, final String fieldName, final Object value) {
        try {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (final Exception e) {
            throw new IllegalStateException(
                "Failed to set static field " + fieldName + " of " + clazz + " to " + value,
                e);
        }
    }


    /**
     * @param <T> an expected assignable type of the field value.
     * @return a value of the static field of the clazz
     */
    public static <T> T getStaticField(final Class<?> clazz, final String fieldName) {
        try {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to get static field " + fieldName + " of " + clazz, e);
        }
    }


    /**
     * Sets the field of the instance to the given value.
     */
    public static void setField(final Object instance, final String fieldName, final Object value) {
        try {
            final Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to set field " + fieldName + " of " + instance + " to " + value, e);
        }
    }


    /**
     * @param <T> an expected assignable type of the field value.
     * @param fieldName a field declared in the instance's class
     * @return a value of the field of the instance
     */
    public static <T> T getField(final Object instance, final String fieldName) {
        return getField(instance, fieldName, instance.getClass());
    }


    /**
     * @param <T> an expected assignable type of the field value.
     * @param fieldName a field declared in the ancestor of the instance
     * @return a value of the field of the instance
     */
    public static <T> T getField(final Object instance, final String fieldName, final Class<?> ancestorOfInstance) {
        try {
            final Field field = ancestorOfInstance.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to get a value of field " + fieldName + " of " + instance, e);
        }
    }
}
