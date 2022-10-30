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

package com.sun.enterprise.deployment.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.naming.SimpleJndiName;

/**
 * @author David Matejcek
 */
public class EnvironmentPropertyValueTypes {
    /**
     * Map of all basic types that can be used in env-entry values and their corresponding types.
     */
    public static final Map<Class<?>, Class<?>> MAPPING;
    public static final Set<Class<?>> ALLOWED_TYPES;

    static {
        HashMap<Class<?>, Class<?>> types = new HashMap<>();

        types.put(String.class, String.class);

        types.put(Class.class, Class.class);

        types.put(Character.class, Character.class);
        types.put(Character.TYPE, Character.class);
        types.put(char.class, Character.class);

        types.put(Byte.class, Byte.class);
        types.put(Byte.TYPE, Byte.class);
        types.put(byte.class, Byte.class);

        types.put(Short.class, Short.class);
        types.put(Short.TYPE, Short.class);
        types.put(short.class, Short.class);

        types.put(Integer.class, Integer.class);
        types.put(Integer.TYPE, Integer.class);
        types.put(int.class, Integer.class);

        types.put(Long.class, Long.class);
        types.put(Long.TYPE, Long.class);
        types.put(long.class, Long.class);

        types.put(Boolean.class, Boolean.class);
        types.put(Boolean.TYPE, Boolean.class);
        types.put(boolean.class, Boolean.class);

        types.put(Double.class, Double.class);
        types.put(Double.TYPE, Double.class);
        types.put(double.class, Double.class);

        types.put(Float.class, Float.class);
        types.put(Float.TYPE, Float.class);
        types.put(float.class, Float.class);

        types.put(Number.class, Number.class);
        types.put(SimpleJndiName.class, SimpleJndiName.class);

        MAPPING = Collections.unmodifiableMap(types);
        ALLOWED_TYPES = Collections.unmodifiableSet(new HashSet<>(types.values()));
    }
}
