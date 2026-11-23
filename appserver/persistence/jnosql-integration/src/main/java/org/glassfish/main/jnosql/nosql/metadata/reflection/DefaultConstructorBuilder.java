/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License, version 2.0, which is available at
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
package org.glassfish.main.jnosql.nosql.metadata.reflection;

import jakarta.data.exceptions.MappingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;

final class DefaultConstructorBuilder implements ConstructorBuilder {

    private final List<Object> values = new ArrayList<>();
    private final ConstructorMetadata metadata;

    private DefaultConstructorBuilder(ConstructorMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public List<ParameterMetaData> parameters() {
        return this.metadata.parameters();
    }

    @Override
    public void add(Object value) {
        this.values.add(value);
    }

    @Override
    public void addEmptyParameter() {
        Constructor<?> constructor = getConstructor();
        Class<?> type = constructor.getParameterTypes()[this.values.size()];
        if (boolean.class.equals(type)) {
            this.values.add(Boolean.FALSE);
        } else if (char.class.equals(type)) {
            this.values.add((char) 0);
        } else if (type.isPrimitive()) {
            this.values.add((byte) 0);
        } else {
            this.values.add(null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T build() {
        Constructor<?> constructor = getConstructor();
        try {
            if (!constructor.canAccess(null)) {
                constructor.setAccessible(true);
            }
            return (T) constructor.newInstance(values.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new MappingException("There is an issue to create a new instance of this class using this constructor: "
                    + constructor, e);
        }
    }

    private Constructor<?> getConstructor() {
        if (metadata instanceof DefaultConstructorMetadata defaultMetadata) {
            return defaultMetadata.constructor();
        }
        try {
            Method method = metadata.getClass().getMethod("constructor");
            method.setAccessible(true);
            return (Constructor<?>) method.invoke(metadata);
        } catch (Exception e) {
            throw new MappingException("Cannot obtain Constructor from " + metadata, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultConstructorBuilder that = (DefaultConstructorBuilder) o;
        return Objects.equals(values, that.values) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, metadata);
    }

    @Override
    public String toString() {
        return "ConstructorBuilder{"
                + "values=" + values
                + ", metadata=" + metadata
                + '}';
    }

    static ConstructorBuilder of(ConstructorMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata is required");
        return new DefaultConstructorBuilder(metadata);
    }
}
