/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.main.jnosql.jakartapersistence.mapping.glassfishcontext;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Repository;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.ExtensibleType;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 *
 * @author Ondro Mihalyi
 */
public class GlassFishClassScanner implements ClassScanner {

    private static final Logger LOG = Logger.getLogger(GlassFishClassScanner.class.getName());

    /* TODO: Optimization - initialize all sets returned from methods in the CDI extension and return them directly,
    avoid searching for them each time */
    Types types;

    public GlassFishClassScanner() {
        DeploymentContext deploymentContext
                = Globals.getDefaultHabitat()
                        .getService(Deployment.class)
                        .getCurrentDeploymentContext();
        if (deploymentContext != null) {
            // During deployment, we can retrieve Types from the context.
            // We can't access CDI context at this point as this class is not in a bean archive.
            this.types = deploymentContext.getTransientAppMetaData(Types.class.getName(), Types.class);
        } else {
            // After deployment, we retrieve types stored in the app context defined in the CDI extension
            final ApplicationContext appContext = CDI.current().select(ApplicationContext.class).get();
            this.types = appContext.getTypes();
        }
    }

    @Override
    public Set<Class<?>> entities() {
        return findClassesWithAnnotation(Entity.class);
    }

    private Set<Class<?>> findClassesWithAnnotation(Class<?> annotation) {
        String annotationClassName = annotation.getName();
        return types.getAllTypes()
                .stream()
                .filter(type -> type instanceof ClassModel)
                .filter(type -> null != type.getAnnotation(annotationClassName))
                .map(intfModel -> {
                    try {
                        return Thread.currentThread().getContextClassLoader().loadClass(intfModel.getName());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> repositories() {
        return repositoriesMatching(this::isSupportedBuiltInInterface);
    }

    @Override
    public Set<Class<?>> embeddables() {
        return findClassesWithAnnotation(Embeddable.class);
    }

    @Override
    public <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> filter) {
        Objects.requireNonNull(filter, "filter is required");
        return repositories().stream().filter(filter::isAssignableFrom)
                .filter(c -> Arrays.asList(c.getInterfaces()).contains(filter))
                .collect(toUnmodifiableSet());
    }

    @Override
    public Set<Class<?>> repositoriesStandard() {
        return repositories().stream()
                .filter(c -> {
                    List<Class<?>> interfaces = Arrays.asList(c.getInterfaces());
                    return interfaces.contains(CrudRepository.class)
                            || interfaces.contains(BasicRepository.class)
                            || interfaces.contains(DataRepository.class);
                }).collect(toUnmodifiableSet());
    }

    @Override
    public Set<Class<?>> customRepositories() {
        return repositoriesMatching(this::isCustomInterface);
    }

    private Set<Class<?>> repositoriesMatching(Predicate<ParameterizedInterfaceModel> predicate) {
        // TODO: Prepare a map of types per annotation on the class to avoid iteration over all types
        return types.getAllTypes()
                .stream()
                .filter(type -> type instanceof InterfaceModel)
                .filter(type -> null != type.getAnnotation(Repository.class.getName()))
                .map(InterfaceModel.class::cast)
                .filter(intfModel -> intfModel.getParameterizedInterfaces().stream()
                        .anyMatch(predicate))
                .map(intfModel -> {
                    try {
                        return Thread.currentThread().getContextClassLoader().loadClass(intfModel.getName());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    private boolean isSupportedBuiltInInterface(ParameterizedInterfaceModel interf) {
        final Collection<ParameterizedInterfaceModel> parametizedTypes = interf.getParametizedTypes();
        return !parametizedTypes.isEmpty()
                && isDataRepositoryInterface(interf.getRawInterface())
                && isSupportedEntityType(parametizedTypes.iterator().next());
    }

    private boolean isCustomInterface(ParameterizedInterfaceModel interf) {
        return !isDataRepositoryInterface(interf.getRawInterface());
    }

    private boolean isDataRepositoryInterface(ExtensibleType<?> interf) {
        return interf.isInstanceOf(DataRepository.class.getName());
    }

    private boolean isSupportedEntityType(ParameterizedInterfaceModel entityType) {
        return null != entityType.getRawInterface().getAnnotation(Entity.class.getName());
    }
}
