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
package org.glassfish.main.jnosql.nosql.metadata;

import jakarta.annotation.PostConstruct;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.glassfish.main.jnosql.nosql.metadata.reflection.ReflectionClassConverter;


/**
 * The default implementation of {@link EntityMetadata}. It's storage the class
 * information in a {@link ConcurrentHashMap}
 */
public class NoSqlEntitiesMetadata implements EntitiesMetadata {

    private final Map<String, EntityMetadata> mappings;

    private final Map<Class<?>, EntityMetadata> classes;

    private final Map<String, EntityMetadata> findBySimpleName;

    private final Map<String, EntityMetadata> findByClassName;

    private final Map<Class<?>, Map<String, InheritanceMetadata>> findDiscriminatorByParent;

    public NoSqlEntitiesMetadata() {
        this.mappings = new ConcurrentHashMap<>();
        this.classes = new ConcurrentHashMap<>();
        this.findBySimpleName = new ConcurrentHashMap<>();
        this.findByClassName = new ConcurrentHashMap<>();
        this.findDiscriminatorByParent = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
    }

    EntityMetadata load(Class<?> type) {
        ClassConverter converter = new ReflectionClassConverter();
        EntityMetadata metadata = converter.apply(type);
        if (metadata.hasEntityName()) {
            mappings.put(metadata.name().toUpperCase(Locale.US), metadata);
        }
        this.findBySimpleName.put(type.getSimpleName(), metadata);
        this.findByClassName.put(type.getName(), metadata);
        return metadata;
    }

    @Override
    public EntityMetadata get(Class<?> entity) {
        return classes.computeIfAbsent(entity, this::load);
    }

    @Override
    public Map<String, InheritanceMetadata> findByParentGroupByDiscriminatorValue(Class<?> parent) {
        Objects.requireNonNull(parent, "parent is required");
        return findDiscriminatorByParent.computeIfAbsent(parent, this::getDiscriminatorFromParent);
    }

    private Map<String, InheritanceMetadata> getDiscriminatorFromParent(Class<?> parent) {
        return this.classes.values().stream()
                .flatMap(c -> c.inheritance().stream())
                .filter(p -> p.isParent(parent))
                .collect(Collectors.toMap(InheritanceMetadata::discriminatorValue, Function.identity()));
    }

    @Override
    public EntityMetadata findByName(String name) {
        Objects.requireNonNull(name, "name is required");
        return Optional.ofNullable(mappings.get(name.toUpperCase(Locale.US)))
                .orElseThrow(() -> new ClassInformationNotFoundException("No entity found with the name: " + name));

    }

    @Override
    public Optional<EntityMetadata> findBySimpleName(String name) {
        Objects.requireNonNull(name, "name is required");
        return Optional.ofNullable(findBySimpleName.get(name));
    }

    @Override
    public Optional<EntityMetadata> findByClassName(String name) {
        Objects.requireNonNull(name, "name is required");
        return Optional.ofNullable(findByClassName.get(name));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "mappings-size=" + mappings.size()
                + ", classes=" + classes
                + '}';
    }
}
