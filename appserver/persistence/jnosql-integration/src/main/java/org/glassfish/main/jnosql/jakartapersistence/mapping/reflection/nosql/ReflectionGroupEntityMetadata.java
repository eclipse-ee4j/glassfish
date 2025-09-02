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
 *   Maximillian Arruda
 */
package org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jnosql.jakartapersistence.mapping.metadata.JakartaPersistenceClassScanner;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;

/**
 * The default implementation of {@link GroupEntityMetadata}.
 * It will load all Classes and put in a {@link ConcurrentHashMap}.
 * Where the key is {@link Class#getName()} and the value is {@link EntityMetadata}
 *
 * TODO: Replace with an impl that queries an EntityManagerFactory directly
 */
@ApplicationScoped
public class ReflectionGroupEntityMetadata implements GroupEntityMetadata {

    private final Map<Class<?>, EntityMetadata> classes=new ConcurrentHashMap<>();
    private final Map<String, EntityMetadata> mappings=new ConcurrentHashMap<>();

    @PostConstruct
    public void postConstruct() {
        var converter = ClassConverter.load();
        var scanner = JakartaPersistenceClassScanner.load();
        for (Class<?> entity : scanner.entities()) {
            EntityMetadata entityMetadata = converter.apply(entity);
            if (entityMetadata.hasEntityName()) {
                mappings.put(entityMetadata.name(), entityMetadata);
            }
            classes.put(entity, entityMetadata);
        }
        for (Class<?> embeddable : scanner.embeddables()) {
            EntityMetadata entityMetadata = converter.apply(embeddable);
            classes.put(embeddable, entityMetadata);
        }
    }

    @Override
    public Map<String, EntityMetadata> mappings() {
        return this.mappings;
    }

    @Override
    public Map<Class<?>, EntityMetadata> classes() {
        return this.classes;
    }

}