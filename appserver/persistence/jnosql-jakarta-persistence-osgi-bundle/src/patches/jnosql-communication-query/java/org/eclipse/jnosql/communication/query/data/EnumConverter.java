/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import java.util.function.Function;

import org.eclipse.jnosql.communication.QueryException;

enum EnumConverter implements Function<String, Enum<?>> {

    INSTANCE;

    @Override
    public Enum<?> apply(String text) {
        try {
            var lastDotIndex = text.lastIndexOf('.');
            var enumClassName = text.substring(0, lastDotIndex);
            var enumValueName = text.substring(lastDotIndex + 1);

            // Try loading the class directly
            try {
                return getEnumValue(enumClassName, enumValueName);
            } catch (ClassNotFoundException e) {
                // Replace last '.' with '$' for inner classes and try again
                int secondLastDotIndex = enumClassName.lastIndexOf('.');
                if (secondLastDotIndex != -1) {
                    enumClassName = enumClassName.substring(0, secondLastDotIndex) + '$' + enumClassName.substring(secondLastDotIndex + 1);
                    return getEnumValue(enumClassName, enumValueName);
                } else {
                    throw e;
                }
            }
        } catch (ClassNotFoundException | IllegalArgumentException exp) {
            throw new QueryException("There is an issue to load class because: " + text, exp);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Enum<?> getEnumValue(String enumClassName, String enumValueName) throws ClassNotFoundException {
        Class<?> enumClass = Class.forName(enumClassName, true, Thread.currentThread().getContextClassLoader());
        if (enumClass.isEnum()) {
            Class<? extends Enum> enumType = enumClass.asSubclass(Enum.class);
            return Enum.valueOf(enumType, enumValueName);
        } else {
            throw new QueryException("There is an issue to load class because it is not an enum: " + enumClassName);
        }
    }
}
