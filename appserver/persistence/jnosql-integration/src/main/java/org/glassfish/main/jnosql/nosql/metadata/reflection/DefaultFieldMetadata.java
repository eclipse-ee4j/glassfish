/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

/**
 * Class that represents {@link FieldMetadata} a default field
 */
public class DefaultFieldMetadata extends AbstractFieldMetadata {


    private final boolean id;

    DefaultFieldMetadata(MappingType type, Field field, String name,
                         Class<? extends AttributeConverter<?, ?>> converter, boolean id,
                         FieldReader reader, FieldWriter writer, String udt) {
        super(type, field, name, converter, reader, writer, udt);
        this.id = id;
    }

    @Override
    public boolean isId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y, T extends AttributeConverter<X, Y>> Optional<T> newConverter() {
        return (Optional<T>) Optional.ofNullable(Reflections.newInstance(converter));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractFieldMetadata that = (AbstractFieldMetadata) o;
        return mappingType == that.mappingType &&
                Objects.equals(field, that.field) &&
                Objects.equals(name, that.name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(mappingType, field, name);
    }

    @Override
    public String toString() {
        return  "DefaultFieldMapping{" + "id=" + id +
                ", type=" + mappingType +
                ", field=" + field +
                ", name='" + name + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", converter=" + converter +
                '}';
    }
}
