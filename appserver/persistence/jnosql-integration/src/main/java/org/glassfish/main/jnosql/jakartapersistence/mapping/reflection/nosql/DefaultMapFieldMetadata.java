/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql;

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

final class DefaultMapFieldMetadata extends AbstractFieldMetadata implements MapFieldMetadata {

    private final TypeSupplier<?> typeSupplier;

    private final Class<?> keyType;

    private final Class<?> valueType;

    private final boolean embeddableField;

    DefaultMapFieldMetadata(MappingType type, Field field, String name, TypeSupplier<?> typeSupplier,
                            Class<? extends AttributeConverter<?, ?>> converter,
                            FieldReader reader, FieldWriter writer, String udt) {
        super(type, field, name, converter, reader, writer, udt);
        this.typeSupplier = typeSupplier;
        ParameterizedType mapType = Reflections.findParameterizedType(this.field.getGenericType(), Map.class)
            .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to find parameterized Map implementation for {0}", this.field)));
        this.keyType = (Class<?>) mapType.getActualTypeArguments()[0];
        this.valueType = (Class<?>) mapType.getActualTypeArguments()[1];
        this.embeddableField = hasFieldAnnotation(Embeddable.class) || hasFieldAnnotation(Entity.class);
    }

    @Override
    public Object value(Value value) {
        return value.get(typeSupplier);
    }

    @Override
    public boolean isId() {
        return false;
    }


    @Override
    public boolean isEmbeddable() {
        return embeddableField;
    }

    @Override
    public Class<?> keyType() {
        return keyType;
    }

    @Override
    public Class<?> valueType() {
        return valueType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultMapFieldMetadata that = (DefaultMapFieldMetadata) o;
        return Objects.equals(typeSupplier, that.typeSupplier) && Objects.equals(keyType, that.keyType) && Objects.equals(valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeSupplier, keyType, valueType);
    }

    private boolean hasFieldAnnotation(Class<? extends Annotation> annotation) {
        return this.valueType.getAnnotation(annotation) != null;
    }
}
