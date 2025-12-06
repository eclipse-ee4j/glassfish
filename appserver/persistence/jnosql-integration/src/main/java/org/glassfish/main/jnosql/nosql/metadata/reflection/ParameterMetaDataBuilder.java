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

import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Id;

import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;

class ParameterMetaDataBuilder {

    private final Parameter parameter;

    private ParameterMetaDataBuilder(Parameter parameter) {
        this.parameter = parameter;
    }

    ParameterMetaData build() {
        Id id = parameter.getAnnotation(Id.class);
        Column column = parameter.getAnnotation(Column.class);
        Convert convert = parameter.getAnnotation(Convert.class);
        Class<?> type = parameter.getType();
        String name = Optional.ofNullable(id)
                .map(Id::value)
                .or(() -> Optional.ofNullable(column).map(Column::value))
                .orElse(null);
        if ((Objects.isNull(name) || name.isBlank())
                && parameter.getDeclaringExecutable().getDeclaringClass().isRecord()) {
            name = parameter.getName();
        }
        MappingType mappingType = MappingType.of(parameter.getType());
        return switch (mappingType) {
            case COLLECTION -> new DefaultCollectionParameterMetaData(name, type,
                    id != null,
                    Optional.ofNullable(convert).map(Convert::value).orElse(null),
                    mappingType, parameter::getParameterizedType);
            case ARRAY -> new DefaultArrayParameterMetaData(name, type,
                    id != null,
                    Optional.ofNullable(convert).map(Convert::value).orElse(null),
                    mappingType, parameter.getType().getComponentType());
            case MAP -> new DefaultMapParameterMetaData(name, type,
                    id != null,
                    Optional.ofNullable(convert).map(Convert::value).orElse(null),
                    mappingType, parameter::getParameterizedType);
            default -> new DefaultParameterMetaData(name, type,
                    id != null,
                    Optional.ofNullable(convert).map(Convert::value).orElse(null),
                    mappingType);
        };

    }

    public static ParameterMetaData of(Parameter parameter) {
        ParameterMetaDataBuilder builder = new ParameterMetaDataBuilder(parameter);
        return builder.build();
    }
}
