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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
     * Whether the entity supported by this repository interface is supported by
     * this provide (e.g. the entity class contains {@link Entity} annotation.
     *
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
        return repositoriesStreamMatching(r -> true);
    }

    protected Stream<Class<?>> repositoriesStreamMatching(Predicate<GeneralInterfaceModel> predicate) {
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
                .map(intfModel -> new GeneralInterfaceModel((InterfaceModel) intfModel))
                .filter(predicate)
                .map(GeneralInterfaceModel::toTypeModel)
                .map(this::typeModelToClass);
    }

    protected boolean isSupportedBuiltInInterface(GeneralInterfaceModel interf) {
        if (interf.hasTypeParametersWithUnknownType()) {
            return false;
        }
        if (isDataRepositoryInterface(interf) && interf.isParameterized()) {
            final Collection<ParameterizedInterfaceModel> parameterizedTypes = interf.parametizedTypes();
            return !parameterizedTypes.isEmpty()
                    && isSupportedEntityType(parameterizedTypes.iterator().next());
        } else {
            final Stream<GeneralInterfaceModel> directlyImplementedInterfaces = interf.interfacesAsStream();
            return directlyImplementedInterfaces
                    .filter(this::isSupportedBuiltInInterface)
                    .count() > 0;
        }
    }

    protected boolean isSupportedStandardInterface(GeneralInterfaceModel interf) {
        if (interf.hasTypeParametersWithUnknownType()) {
            return false;
        }
        if (implementsStandardInterfaceDirectly(interf)) {
            return isSupportedBuiltInInterface(interf);
        } else {
            final Stream<GeneralInterfaceModel> directlyImplementedInterfaces = interf.interfacesAsStream();
            return directlyImplementedInterfaces
                    .filter(this::isSupportedStandardInterface)
                    .count() > 0;
        }
    }

    protected boolean isNotSupportedStandardInterface(GeneralInterfaceModel intfModel) {
        return !isSupportedStandardInterface(intfModel);
    }

    private boolean isDataRepositoryInterface(GeneralInterfaceModel interf) {
        return interf.interfaceName().equals(DataRepository.class.getName());
    }

    private boolean implementsStandardInterfaceDirectly(GeneralInterfaceModel interf) {
        return interf.interfacesAsStream()
                .filter(generalInterface
                        -> Set.of(
                        BasicRepository.class.getName(),
                        CrudRepository.class.getName(),
                        DataRepository.class.getName()
                )
                        .contains(generalInterface.interfaceName())
                ).count() > 0;
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

record GeneralInterfaceModel(InterfaceModel plainInterface, ParameterizedInterfaceModel parameterizedInterface, Collection<ParameterizedInterfaceModel> parameterizedTypes) {

    GeneralInterfaceModel(InterfaceModel plainInterface) {
        this(plainInterface, null, null);
    }

    GeneralInterfaceModel(ParameterizedInterfaceModel parameterizedInterfaceModel) {
        this(null, parameterizedInterfaceModel, null);
    }

    GeneralInterfaceModel(ParameterizedInterfaceModel parameterizedInterfaceModel, Collection<ParameterizedInterfaceModel> parameterizedTypes) {
        this(null, parameterizedInterfaceModel, parameterizedTypes);
    }

    boolean isParameterized() {
        return parameterizedInterface != null;
    }

    /*
      Is an interface with generics but no declared type parameters. E.g. it's List<T> but not List<String>
      - type of parameters is unknown.
    */
    boolean hasTypeParametersWithUnknownType() {
        return isParameterized() ? parametizedTypes().isEmpty() : plainInterface.getFormalTypeParameters() != null && !plainInterface.getFormalTypeParameters().isEmpty();
    }

    String interfaceName() {
        if (isParameterized()) {
            return parameterizedInterface.getRawInterfaceName();
        } else {
            return plainInterface.getName();
        }
    }

    ExtensibleType toTypeModel() {
        return isParameterized() ? parameterizedInterface.getRawInterface() : plainInterface;
    }

    Stream<GeneralInterfaceModel> interfacesAsStream() {
        final ExtensibleType typeModel = toTypeModel();
        final Collection<ParameterizedInterfaceModel> parameterizedInterfaces = typeModel.getParameterizedInterfaces();
        final Collection<InterfaceModel> plainInterfaces = typeModel.getInterfaces();

        return Stream.concat(
                parameterizedInterfaces.stream().map(parameterizedInterface -> GeneralInterfaceModel.parameterizedFromSubInterface(parameterizedInterface, this)),
                plainInterfaces.stream().map(GeneralInterfaceModel::new)
        );
    }

    Collection<ParameterizedInterfaceModel> parametizedTypes() {
        return parameterizedTypes != null ? parameterizedTypes : parameterizedInterface.getParametizedTypes();
    }

    /*
     Creates an instance for a give parameterized interface and captures parameter types from the interface that extends it. Parameter types are not defined on superinterfaces therefore the information about them is lost down the line. E.g. for IntfB extends IntfA<String>, IntfA<T>, the information about String type is present only on IntfB. IntfA only knows the parameter name is "T" but not that its type is String.
    */
    static GeneralInterfaceModel parameterizedFromSubInterface(ParameterizedInterfaceModel parameterizedInterface, GeneralInterfaceModel subInterface) {
        if (subInterface.isParameterized()) {
            List<ParameterizedInterfaceModel> parameterizedTypes = new ArrayList<>();
            final Map<String, ParameterizedInterfaceModel> formalTypeParametersOnSubInterface = subInterface.toTypeModel().getFormalTypeParameters();
            final Iterator<ParameterizedInterfaceModel> iteratorThroughParameterizedTypesOnSubInterface = subInterface.parametizedTypes().iterator();
            for (Map.Entry<String, ParameterizedInterfaceModel> formalTypeParameterOnSubInterface : formalTypeParametersOnSubInterface.entrySet() ) {

                if (!iteratorThroughParameterizedTypesOnSubInterface.hasNext()) {
                    throw new IllegalStateException("The number of parameterized types and formal type parameters is not the same, interface: " + parameterizedInterface);
                }
                final ParameterizedInterfaceModel parameterizedTypeOnSubInterface = iteratorThroughParameterizedTypesOnSubInterface.next();
                final Map formalTypeParametersOnInterface = parameterizedInterface.getRawInterface().getFormalTypeParameters();
                if (formalTypeParametersOnInterface.containsKey(formalTypeParameterOnSubInterface.getKey())) {
                    parameterizedTypes.add(parameterizedTypeOnSubInterface);
                }
            }
            return new GeneralInterfaceModel(parameterizedInterface, parameterizedTypes);
        } else {
            return new GeneralInterfaceModel(parameterizedInterface);
        }
    }
}
