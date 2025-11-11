/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import jakarta.nosql.Entity;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Embeddable;
import org.eclipse.jnosql.mapping.metadata.CollectionSupplier;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

final class DefaultCollectionFieldMetadata extends AbstractFieldMetadata implements CollectionFieldMetadata {

    @SuppressWarnings("rawtypes")
    private static final List<CollectionSupplier> COLLECTION_SUPPLIERS = ServiceLoader.load(CollectionSupplier.class) .stream()
            .map(ServiceLoader.Provider::get)
            .toList();

    private final TypeSupplier<?> typeSupplier;
    private final boolean entityField;
    private final boolean embeddableField;

    DefaultCollectionFieldMetadata(MappingType type, Field field, String name, TypeSupplier<?> typeSupplier,
                                   Class<? extends AttributeConverter<?, ?>> converter,
                                   FieldReader reader, FieldWriter writer, String udt) {
        super(type, field, name, converter, reader, writer, udt);
        this.typeSupplier = typeSupplier;
        this.entityField = hasFieldAnnotation(Entity.class);
        this.embeddableField = hasFieldAnnotation(Embeddable.class);
    }

    @Override
    public Object value(Value value) {
        if(value.get() instanceof Iterable) {
            return value.get(typeSupplier);
        } else {
            return Value.of(Collections.singletonList(value.get())).get(typeSupplier);
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
        DefaultCollectionFieldMetadata that = (DefaultCollectionFieldMetadata) o;
        return mappingType == that.mappingType &&
                Objects.equals(field, that.field) &&
                Objects.equals(typeSupplier, that.typeSupplier) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingType, field, name, typeSupplier);
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

    @SuppressWarnings("unchecked")
    private boolean hasFieldAnnotation(Class<?> annotation) {
        ParameterizedType collectionType = Reflections.findParameterizedType(this.field.getGenericType(), Collection.class)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to find parameterized Collection implementation for {0}", this.field)));
        return ((Class) collectionType
                .getActualTypeArguments()[0])
                .getAnnotation(annotation) != null;
    }

    @Override
    public Class<?> elementType() {
        return (Class<?>) ((ParameterizedType) this.field
                .getGenericType())
                .getActualTypeArguments()[0];
    }

    @Override
    public Collection<?> collectionInstance() {
        Class<?> type = type();
        final CollectionSupplier supplier =  COLLECTION_SUPPLIERS
                .stream()
                .filter(c -> c.test(type))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("This collection is not supported yet: " + type));
        return (Collection<?>) supplier.get();
    }

    @Override
    public String toString() {
        return "DefaultCollectionFieldMetadata{" + "typeSupplier=" + typeSupplier +
                ", type=" + mappingType +
                ", field=" + field +
                ", name='" + name + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", converter=" + converter +
                '}';
    }
}
