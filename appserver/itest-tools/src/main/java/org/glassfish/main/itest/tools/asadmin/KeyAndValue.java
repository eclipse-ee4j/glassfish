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

package org.glassfish.main.itest.tools.asadmin;

import java.util.Comparator;
import java.util.Objects;

import static java.util.Comparator.comparing;

/**
 * @param <T> value type
 *
 * @author David Matejcek
 */
public class KeyAndValue<T> implements Comparable<KeyAndValue<?>> {


    // note: toString should be fast.
    private static final Comparator<Object> NULL_SAFE_COMPARATOR = comparing(o -> o == null ? "" : o.toString(),
        String::compareTo);

    private final String key;
    private final T value;

    public KeyAndValue(final String key, final T value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return key;
    }


    public T getValue() {
        return value;
    }


    @Override
    public String toString() {
        return getKey() + '=' + getValue();
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof KeyAndValue<?>) {
            KeyAndValue<?> another = (KeyAndValue<?>) object;
            return Objects.equals(key, another.key) && Objects.equals(value, another.value);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return key.hashCode();
    }


    @Override
    public int compareTo(KeyAndValue<?> o) {
        int keysComparisonResult = key.compareTo(o.key);
        if (keysComparisonResult != 0) {
            return keysComparisonResult;
        }
        return Objects.compare(value, o.value, NULL_SAFE_COMPARATOR);
    }
}
