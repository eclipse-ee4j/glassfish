/*
 *  Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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


import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Repository;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Scanner classes that will load entities with both Entity and Embeddable
 * annotations and repositories: interfaces that extend DataRepository
 * and has the Repository annotation.
 */
enum PersistenceClassScannerSingleton implements ClassScanner {

    INSTANCE;

    private final Set<Class<?>> entities= new HashSet<>();
    private final Set<Class<?>> repositories = new HashSet<>();
    private final Set<Class<?>> embeddables = new HashSet<>();
    private final Set<Class<?>> customRepositories = new HashSet<>();

    PersistenceClassScannerSingleton() {
        Logger logger = Logger.getLogger(PersistenceClassScannerSingleton.class.getName());
        logger.fine("Starting scan class to find entities, embeddable and repositories.");

        try (ScanResult result = new ClassGraph().enableAllInfo().scan()) {
            var notSupportedRepositories = loadNotSupportedRepositories(result);
            logger.warning(() -> "The following repositories are not supported: " + notSupportedRepositories);

            entities.addAll(loadEntities(result));
            embeddables.addAll(loadEmbeddable(result));
            repositories.addAll(loadRepositories(result));
            customRepositories.addAll(loadCustomRepositories(result));

            notSupportedRepositories.forEach(repositories::remove);
        }

        logger.fine(String.format("Finished the class scan with entities %d, embeddables %d and repositories: %d"
                , entities.size(), embeddables.size(), repositories.size()));
    }

    @Override
    public Set<Class<?>> entities() {
        return unmodifiableSet(entities);
    }

    @Override
    public Set<Class<?>> repositories() {
        return unmodifiableSet(repositories);
    }

    @Override
    public Set<Class<?>> embeddables() {
        return unmodifiableSet(embeddables);
    }

    @Override
    public <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> filter) {
        requireNonNull(filter, "filter is required");

        return repositories.stream()
                .filter(filter::isAssignableFrom)
                .filter(c -> asList(c.getInterfaces()).contains(filter))
                .collect(toUnmodifiableSet());
    }


    @Override
    public Set<Class<?>> repositoriesStandard() {
        return repositories.stream()
                .filter(c -> {
                    List<Class<?>> interfaces = asList(c.getInterfaces());
                    return interfaces.contains(CrudRepository.class)
                            || interfaces.contains(BasicRepository.class)
                            || interfaces.contains(NoSQLRepository.class)
                            || interfaces.contains(DataRepository.class);
                }).collect(toUnmodifiableSet());
    }

    @Override
    public Set<Class<?>> customRepositories() {
        return customRepositories;
    }


    @SuppressWarnings("rawtypes")
    private static List<Class<DataRepository>> loadRepositories(ScanResult scan) {
        return scan.getClassesWithAnnotation(Repository.class)
                .getInterfaces()
                .filter(c -> c.implementsInterface(DataRepository.class))
                .loadClasses(DataRepository.class)
                .stream()
                .filter(PersistenceRepositoryFilter.INSTANCE)
                .toList();
    }

    private static List<Class<?>> loadCustomRepositories(ScanResult scan) {
        return scan.getClassesWithAnnotation(Repository.class)
                .getInterfaces()
                .filter(c -> !c.implementsInterface(DataRepository.class))
                .loadClasses()
                .stream()
                .toList();
    }

    @SuppressWarnings("rawtypes")
    private static List<Class<DataRepository>> loadNotSupportedRepositories(ScanResult scan) {
        return scan.getClassesWithAnnotation(Repository.class)
                .getInterfaces()
                .filter(c -> c.implementsInterface(DataRepository.class))
                .loadClasses(DataRepository.class)
                .stream()
                .filter(PersistenceRepositoryFilter.INSTANCE.negate())
                .toList();
    }

    private static List<Class<?>> loadEmbeddable(ScanResult scan) {
        return scan.getClassesWithAnnotation(Embeddable.class).loadClasses();
    }

    private static List<Class<?>> loadEntities(ScanResult scan) {
        return scan.getClassesWithAnnotation(Entity.class).loadClasses();
    }
}
