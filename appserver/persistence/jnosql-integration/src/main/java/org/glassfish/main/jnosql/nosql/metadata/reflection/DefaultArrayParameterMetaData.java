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

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;
import org.eclipse.jnosql.mapping.metadata.ArrayParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;

class DefaultArrayParameterMetaData extends DefaultParameterMetaData implements ArrayParameterMetaData {


    private final Class<?> elementType;

    private final boolean embeddableField;

    DefaultArrayParameterMetaData(String name, Class<?> type, boolean id,
                                  Class<? extends AttributeConverter<?, ?>> converter,
                                  MappingType mappingType, Class<?> elementType) {
        super(name, type, id, converter, mappingType);
        this.elementType = elementType;
        this.embeddableField = hasFieldAnnotation(Embeddable.class) || hasFieldAnnotation(Entity.class);
    }

    @Override
    public boolean isEmbeddable() {
        return embeddableField;
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

    private boolean hasFieldAnnotation(Class<? extends Annotation> annotation) {
        return this.elementType.getAnnotation(annotation) != null;
    }


}
