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
package org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;

final class ConstructorMetadataBuilder {

    private final Reflections reflections;

    ConstructorMetadataBuilder(Reflections reflections) {
        this.reflections = reflections;
    }

    <T> ConstructorMetadata build(Class<T> entity) {
        Constructor<T> constructor = reflections.getConstructor(entity);
        if (constructor.getParameterCount() == 0) {
            return new DefaultConstructorMetadata(constructor, Collections.emptyList());
        }

        List<ParameterMetaData> parameters = Stream.of(constructor.getParameters())
                .map(ParameterMetaDataBuilder::of)
                .toList();
        return new DefaultConstructorMetadata(constructor, parameters);
    }
}
