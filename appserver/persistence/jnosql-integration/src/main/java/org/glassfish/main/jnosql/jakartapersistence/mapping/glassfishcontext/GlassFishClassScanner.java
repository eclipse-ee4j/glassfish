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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.ExtensibleType;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;
import org.glassfish.hk2.classmodel.reflect.Type;
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

    public GlassFishClassScanner(Types types) {
        this.types = types;
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
                .map(ClassModel.class::cast)
                .map(this::typeModelToClass)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<?>> repositories() {
        return repositoriesStream()
                .collect(toUnmodifiableSet());
    }

    private Stream<Class<?>> repositoriesStream() {
        return repositoriesStreamMatching(intfModel ->
                intfModel.getParameterizedInterfaces().stream()
                .anyMatch(this::isSupportedBuiltInInterface)
                || DataRepository.class.isAssignableFrom(typeModelToClass(intfModel)));
    }

    @Override
    public Set<Class<?>> embeddables() {
        return findClassesWithAnnotation(Embeddable.class);
    }

    @Override
    public <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> filter) {
        Objects.requireNonNull(filter, "filter is required");
        return repositoriesStream()
                .filter(filter::isAssignableFrom)
                .collect(toUnmodifiableSet());
    }

    @Override
    public Set<Class<?>> repositoriesStandard() {
        Predicate<ParameterizedInterfaceModel> isSupportedBuiltInInterface = this::isSupportedBuiltInInterface;
        Predicate<ParameterizedInterfaceModel> directlyImplementsStandardInterface = this::directlyImplementsStandardInterface;
        return repositoriesStreamMatching(intfModel -> intfModel.getParameterizedInterfaces().stream()
                .anyMatch(isSupportedBuiltInInterface.and(directlyImplementsStandardInterface)))
                .collect(toUnmodifiableSet());
    }

    @Override
    public Set<Class<?>> customRepositories() {
        return repositoriesStreamMatching(this::noneOfExtendedInterfacesIsStandard)
                .collect(toUnmodifiableSet());
    }

    private boolean noneOfExtendedInterfacesIsStandard(InterfaceModel intfModel) {
        Predicate<ParameterizedInterfaceModel> directlyImplementsStandardInterface = this::directlyImplementsStandardInterface;
        return intfModel.getParameterizedInterfaces().isEmpty()
                || intfModel.getParameterizedInterfaces().stream()
                        .allMatch(directlyImplementsStandardInterface
                                .negate());
    }

    private Stream<Class<?>> repositoriesStreamMatching(Predicate<InterfaceModel> predicate) {
        // TODO: Prepare a map of types per annotation on the class to avoid iteration over all types
        return types.getAllTypes()
                .stream()
                .filter(type -> type instanceof InterfaceModel)
                .filter(type -> null != type.getAnnotation(Repository.class.getName()))
                .map(InterfaceModel.class::cast)
                .filter(predicate)
                .map(this::typeModelToClass);
    }

    private Class<?> typeModelToClass(ExtensibleType<?> type) throws RuntimeException {
        return classForName(type.getName());
    }

    private Class<?> classForName(String name) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSupportedBuiltInInterface(ParameterizedInterfaceModel interf) {
        final Collection<ParameterizedInterfaceModel> parameterizedTypes = interf.getParametizedTypes();
        return !parameterizedTypes.isEmpty()
                && isSupportedEntityType(parameterizedTypes.iterator().next())
                && isDataRepositoryInterface(interf);
    }

    private boolean directlyImplementsStandardInterface(ParameterizedInterfaceModel interf) {
        Type basicRepositoryType = types.getBy(BasicRepository.class.getName());
        Type crudRepositoryType = types.getBy(CrudRepository.class.getName());
        Type dataRepositoryType = types.getBy(DataRepository.class.getName());
        final Collection implementedInterfaces = interf.getRawInterface().getInterfaces();
        return implementedInterfaces.contains(basicRepositoryType)
                || implementedInterfaces.contains(crudRepositoryType)
                || implementedInterfaces.contains(dataRepositoryType)
                || canBeAssignedToOneOf(classForName(interf.getRawInterfaceName()),
                        BasicRepository.class, CrudRepository.class, DataRepository.class);
    }

    private boolean isDataRepositoryInterface(ParameterizedInterfaceModel interf) {
        return interf.getRawInterfaceName().equals(DataRepository.class.getName())
                || DataRepository.class.isAssignableFrom(classForName(interf.getRawInterfaceName()));
    }

    private boolean isSupportedEntityType(ParameterizedInterfaceModel entityType) {
        return null != entityType.getRawInterface().getAnnotation(Entity.class.getName());
    }

    private boolean canBeAssignedToOneOf(Class<?> clazz, Class<?>... assignables) {
        for (Class<?> cls : assignables) {
            if (cls.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}
