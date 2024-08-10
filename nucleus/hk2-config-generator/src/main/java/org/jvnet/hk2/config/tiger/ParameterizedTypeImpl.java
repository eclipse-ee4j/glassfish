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

package org.jvnet.hk2.config.tiger;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

/**
 * {@link ParameterizedType} implementation.
 */
final class ParameterizedTypeImpl implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Class<?> rawType;
    private Type ownerType;

    ParameterizedTypeImpl(Class<?> rawType,
                                  Type[] actualTypeArguments,
                                  Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        if (ownerType != null) {
            this.ownerType = ownerType;
        } else {
            this.ownerType = rawType.getDeclaringClass();
        }
        validateConstructorArguments();
    }

    private void validateConstructorArguments() {
        TypeVariable/*<?>*/[] formals = rawType.getTypeParameters();
        // check correct arity of actual type args
        if (formals.length != actualTypeArguments.length) {
            throw new MalformedParameterizedTypeException();
        }
        for (Type actualTypeArgument : actualTypeArguments) {
            // check actuals against formals' bounds
        }

    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    @Override
    public Class<?> getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    /*
     * From the JavaDoc for java.lang.reflect.ParameterizedType
     * "Instances of classes that implement this interface must
     * implement an equals() method that equates any two instances
     * that share the same generic type declaration and have equal
     * type parameters."
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterizedType) {
            // Check that information is equivalent
            ParameterizedType that = (ParameterizedType) o;

            if (this == that) {
                return true;
            }

            Type thatOwner = that.getOwnerType();
            Type thatRawType = that.getRawType();
            return (ownerType == null ? thatOwner == null : ownerType.equals(thatOwner))
                && (rawType == null ? thatRawType == null : rawType.equals(thatRawType))
                && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(actualTypeArguments)
            ^ (ownerType == null ? 0 : ownerType.hashCode())
            ^ (rawType == null ? 0 : rawType.hashCode());
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (ownerType != null) {
            if (ownerType instanceof Class) {
                sb.append(((Class) ownerType).getName());
            } else {
                sb.append(ownerType.toString());
            }

            sb.append(".");

            if (ownerType instanceof ParameterizedTypeImpl) {
                // Find simple name of nested type by removing the
                // shared prefix with owner.
                sb.append(rawType.getName().replace(((ParameterizedTypeImpl) ownerType).rawType.getName() + "$",
                        ""));
            } else {
                sb.append(rawType.getName());
            }
        } else {
            sb.append(rawType.getName());
        }

        if (actualTypeArguments != null &&
                actualTypeArguments.length > 0) {
            sb.append("<");
            boolean first = true;
            for (Type t : actualTypeArguments) {
                if (!first) {
                    sb.append(", ");
                }
                if (t instanceof Class) {
                    sb.append(((Class) t).getName());
                } else {
                    sb.append(t.toString());
                }
                first = false;
            }
            sb.append(">");
        }

        return sb.toString();
    }
}
