/*
 *  Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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
 *   Ondro Mihalyi
 */
package org.glassfish.main.jnosql.jakartapersistence.mapping.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * A filter to validate Repository that either Eclipse JNoSQL or the Jakarta Persistence extension support. It will
 * check the first parameter on the repository, and if the entity has not had an unsupported annotation, it will return
 * false and true to supported Repository.
 */
enum PersistenceRepositoryFilter implements Predicate<Class<?>> {

    INSTANCE;

    @Override
    public boolean test(Class<?> type) {
        Optional<Class<?>> entity = getEntityClass(type);
        return entity
                .map(this::toSupportedAnnotation)
                .isPresent();
    }

    private Annotation toSupportedAnnotation(Class<?> c) {
        final Annotation annotation = c.getAnnotation(jakarta.persistence.Entity.class);
        return annotation != null ? annotation : c.getAnnotation(jakarta.nosql.Entity.class);
    }

    private Optional<Class<?>> getEntityClass(Class<?> repository) {
        Type[] interfaces = repository.getGenericInterfaces();
        if (interfaces.length == 0) {
            return empty();
        }

        if (interfaces[0] instanceof ParameterizedType interfaceType) {
            return ofNullable(getEntityFromInterface(interfaceType));
        }

        return empty();
    }

    private Class<?> getEntityFromInterface(ParameterizedType param) {
        Type[] arguments = param.getActualTypeArguments();
        if (arguments.length == 0) {
            return null;
        }

        Type argument = arguments[0];
        if (argument instanceof Class<?> entity) {
            return entity;
        }

        return null;
    }

}
