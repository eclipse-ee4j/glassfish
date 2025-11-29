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
package org.glassfish.main.jnosql.nosql.metadata.reflection;

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;

import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.MapParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.MappingType;

class DefaultMapParameterMetaData extends DefaultParameterMetaData implements MapParameterMetaData {

    private final TypeSupplier<?> typeSupplier;
    private final Class<?> keyType;
    private final Class<?> valueType;
    private final boolean embeddableField;

    DefaultMapParameterMetaData(String name, Class<?> type, boolean id,
                                Class<? extends AttributeConverter<?, ?>> converter,
                                MappingType mappingType, TypeSupplier<?> typeSupplier) {
        super(name, type, id, converter, mappingType);
        this.typeSupplier = typeSupplier;
        this.keyType = (Class<?>) ((ParameterizedType) typeSupplier.get()).getActualTypeArguments()[0];
        this.valueType = (Class<?>) ((ParameterizedType) typeSupplier.get()).getActualTypeArguments()[1];
        this.embeddableField = hasFieldAnnotation(Embeddable.class) || hasFieldAnnotation(Entity.class);
    }

    @Override
    public boolean isEmbeddable() {
        return embeddableField;
    }

    @Override
    public Class<?> keyType() {
       return this.keyType;
    }

    @Override
    public Class<?> valueType() {
        return this.valueType;
    }

    @Override
    public Object value(Value value) {
        return value.get(typeSupplier);
    }

    private boolean hasFieldAnnotation(Class<? extends Annotation> annotation) {
        return this.valueType.getAnnotation(annotation) != null;
    }

}
