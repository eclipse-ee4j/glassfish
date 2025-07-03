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
package org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql;

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.ServiceLoader;

import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.mapping.metadata.CollectionParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.CollectionSupplier;
import org.eclipse.jnosql.mapping.metadata.MappingType;

class DefaultCollectionParameterMetaData extends DefaultParameterMetaData implements CollectionParameterMetaData {

    private final Class<?> elementType;

    private final boolean embeddableField;

    DefaultCollectionParameterMetaData(String name, Class<?> type, boolean id,
                                       Class<? extends AttributeConverter<?, ?>> converter,
                                       MappingType mappingType, TypeSupplier<?> typeSupplier) {
        super(name, type, id, converter, mappingType);
        this.elementType =  (Class<?>)  ((ParameterizedType) typeSupplier.get()).getActualTypeArguments()[0];
        this.embeddableField = hasFieldAnnotation(Embeddable.class) || hasFieldAnnotation(Entity.class);
    }

    @Override
    public boolean isEmbeddable() {
        return embeddableField;
    }

    @Override
    public Class<?> elementType() {
        return this.elementType;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<?> collectionInstance() {
        Class<?> type =  type();
        final CollectionSupplier supplier = ServiceLoader.load(CollectionSupplier.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(CollectionSupplier.class::cast)
                .filter(c -> c.test(type))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("This collection is not supported yet: " + type));
        return (Collection<?>) supplier.get();
    }

    private boolean hasFieldAnnotation(Class<? extends Annotation> annotation) {
        return this.elementType.getAnnotation(annotation) != null;
    }

}
