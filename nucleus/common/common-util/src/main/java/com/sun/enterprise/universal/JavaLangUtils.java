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

package com.sun.enterprise.universal;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for all trivial functions missing in Java.
 *
 * @author David Matejcek
 */
public final class JavaLangUtils {

    private JavaLangUtils() {
        // utility class
    }

    /**
     * If the value is null, calls the supplier to obtain the default value.
     *
     * @param <T> expected type
     * @param value provided value
     * @param supplierOfDefault must not be null.
     * @return value OR value provided by the supplier. Note that it may be null too.
     */
    public static <T> T nonNull(T value, Supplier<? extends T> supplierOfDefault) {
        return value == null ? supplierOfDefault.get() : value;
    }


    /**
     * If the value is null, calls the supplier to obtain the default value.
     * <p>
     * If the value is not null, applies the function.
     *
     * @param <V> input value type
     * @param <T> expected type
     * @param value provided value - can be null
     * @param function function to apply to the value. Must not be null.
     * @param supplierOfDefault can be null, then supplies null.
     * @return result of a function OR value provided by the supplier. Note that the returned value
     *         may be null too.
     */
    public static <V, T> T nonNull(V value, Function<V, T> function, Supplier<? extends T> supplierOfDefault) {
        if (value == null) {
            return supplierOfDefault == null ? null : supplierOfDefault.get();
        }
        return function.apply(value);
    }
}
