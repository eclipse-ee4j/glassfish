/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.glassfish.main.jnosql.nosql.metadata.reflection;

import jakarta.nosql.AttributeConverter;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;

class DefaultParameterMetaData implements ParameterMetaData {

    private final String name;

    private final Class<?> type;

    private final boolean id;

    private final MappingType mappingType;

    private final Class<? extends AttributeConverter<?, ?>> converter;

    DefaultParameterMetaData(String name,
                             Class<?> type, boolean id,
                             Class<? extends AttributeConverter<?, ?>> converter,
                             MappingType mappingType) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.converter = converter;
        this.mappingType = mappingType;
    }

    @Override
    public MappingType mappingType() {
        return mappingType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public boolean isId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y, T extends AttributeConverter<X, Y>> Optional<Class<T>> converter() {
        return Optional.ofNullable((Class<T>)converter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y, T extends AttributeConverter<X, Y>> Optional<T> newConverter() {
        return Optional.ofNullable(converter).map(c -> (T) Reflections.newInstance(c));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultParameterMetaData that = (DefaultParameterMetaData) o;
        return id == that.id && Objects.equals(name, that.name)
                && Objects.equals(type, that.type)
                && Objects.equals(converter, that.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, id, converter);
    }

    @Override
    public String toString() {
        return "ParameterMetaData{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", id=" + id +
                ", converter=" + converter +
                '}';
    }
}
