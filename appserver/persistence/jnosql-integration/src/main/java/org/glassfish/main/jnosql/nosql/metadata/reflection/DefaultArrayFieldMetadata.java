/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.ArrayFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

final class DefaultArrayFieldMetadata extends AbstractFieldMetadata implements ArrayFieldMetadata {

    private static final TypeReference<List<Object>> TYPE_SUPPLIER = new TypeReference<>() {};
    private final Class<?> elementType;
    private final boolean entityField;
    private final boolean embeddableField;

    DefaultArrayFieldMetadata(MappingType type, Field field, String name, Class<?> elementType,
                              Class<? extends AttributeConverter<?, ?>> converter,
                              FieldReader reader, FieldWriter writer, String udt) {
        super(type, field, name, converter, reader, writer, udt);
        this.elementType = elementType;
        this.entityField = hasFieldAnnotation(Entity.class);
        this.embeddableField = hasFieldAnnotation(Embeddable.class);
    }

    @Override
    public Object value(Value value) {
        Objects.requireNonNull(value, "value is required");
        if(value.get() instanceof Iterable) {
            return value.get(TYPE_SUPPLIER).toArray();
        } else {
            return Value.of(Collections.singletonList(value.get())).get(TYPE_SUPPLIER);
        }
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultArrayFieldMetadata that = (DefaultArrayFieldMetadata) o;
        return mappingType == that.mappingType &&
                Objects.equals(field, that.field) &&
                Objects.equals(elementType, that.elementType) &&
                Objects.equals(name, that.name);
    }



    @Override
    public int hashCode() {
        return Objects.hash(mappingType, field, name, elementType);
    }

    @Override
    public boolean isEmbeddable() {
        return isEmbeddableField() || isEntityField();
    }

    private boolean isEntityField() {
        return entityField;
    }

    private boolean isEmbeddableField() {
        return embeddableField;
    }

    private boolean hasFieldAnnotation(Class<? extends Annotation> annotation) {
        return elementType.getAnnotation(annotation) != null;
    }

    @Override
    public Class<?> elementType() {
        return elementType;
    }

    @Override
    public Object arrayInstance(Collection<?> collection) {
        var array = Array.newInstance(elementType, collection.size());
        int index = 0;
        for (Object item : collection) {
            Array.set(array, index++, item);
        }
        return array;
    }

}
