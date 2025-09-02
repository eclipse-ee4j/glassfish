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
import jakarta.persistence.Entity;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.ExtensibleType;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;

/**
 *
 * @author Ondro Mihalyi
 */
abstract public class BaseGlassFishClassScanner {

    private static final Logger LOG = Logger.getLogger(BaseGlassFishClassScanner.class.getName());

    /**
     * Whether the entity supported by this repository interface is supported by this provide (e.g. the entity class contains {@link Entity} annotation.
     * @param entityType Type of the entity, analogous to the entity class
     * @return True if the entity is supported, false otherwise
     */
    abstract protected boolean isSupportedEntityType(ParameterizedInterfaceModel entityType);

    abstract protected String getProviderName();

    /* TODO: Optimization - initialize all sets returned from methods in the CDI extension and return them directly,
    avoid searching for them each time */
    protected Types getTypes() {
        final ServiceLocator locator = Globals.getDefaultHabitat();
        DeploymentContext deploymentContext
                = locator != null
                        ? locator
                                .getService(Deployment.class)
                                .getCurrentDeploymentContext()
                        : null;
        if (deploymentContext != null) {
            // During deployment, we can retrieve Types from the context.
            // We can't access CDI context at this point as this class is not in a bean archive.
            return deploymentContext.getTransientAppMetaData(Types.class.getName(), Types.class);
        } else {
            // After deployment, we retrieve types stored in the app context defined in the CDI extension
            final ApplicationContext appContext = CDI.current().select(ApplicationContext.class).get();
            return appContext.getTypes();
        }
    }

    protected Set<Class<?>> findClassesWithAnnotation(Class<?> annotation) {
        String annotationClassName = annotation.getName();
        return getTypes().getAllTypes()
                .stream()
                .filter(type -> type instanceof ClassModel)
                .filter(type -> null != type.getAnnotation(annotationClassName))
                .map(ClassModel.class::cast)
                .map(this::typeModelToClass)
                .collect(Collectors.toSet());
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

    protected Stream<Class<?>> repositoriesStream() {
        return repositoriesStreamMatching(intfModel ->
                intfModel.getParameterizedInterfaces().stream()
                .anyMatch(this::isSupportedBuiltInInterface));
    }

    protected Stream<Class<?>> repositoriesStreamMatching(Predicate<InterfaceModel> predicate) {
        // TODO: Prepare a map of types per annotation on the class to avoid iteration over all types
        return getTypes().getAllTypes()
                .stream()
                .filter(type -> type instanceof InterfaceModel)
                .filter(type -> {
                    final AnnotationModel repositoryAnnotation = type.getAnnotation(Repository.class.getName());
                    if (repositoryAnnotation != null) {
                        String provider = repositoryAnnotation.getValue("provider", String.class);
                        if (Objects.equals(Repository.ANY_PROVIDER, provider)
                                || getProviderName().equals(provider)) {
                            return true;
                        }
                    }
                    return false;
                        })
                .map(InterfaceModel.class::cast)
                .filter(predicate)
                .map(this::typeModelToClass);
    }

    protected boolean noneOfExtendedInterfacesIsStandard(InterfaceModel intfModel) {
        Predicate<ParameterizedInterfaceModel> directlyImplementsStandardInterface = this::directlyImplementsStandardInterface;
        return intfModel.getParameterizedInterfaces().isEmpty()
                || intfModel.getParameterizedInterfaces().stream()
                        .allMatch(directlyImplementsStandardInterface
                                .negate());
    }

    protected boolean isSupportedBuiltInInterface(ParameterizedInterfaceModel interf) {
        final Collection<ParameterizedInterfaceModel> parameterizedTypes = interf.getParametizedTypes();
        return !parameterizedTypes.isEmpty()
                && isSupportedEntityType(parameterizedTypes.iterator().next())
                && isDataRepositoryInterface(interf);
    }

    protected boolean directlyImplementsStandardInterface(ParameterizedInterfaceModel interf) {
        var types = getTypes();
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

    private boolean canBeAssignedToOneOf(Class<?> clazz, Class<?>... assignables) {
        for (Class<?> cls : assignables) {
            if (cls.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}
