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
package org.glassfish.main.jnosql.jakartapersistence;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Repository;
import jakarta.persistence.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;
import org.glassfish.internal.api.Globals;
import org.glassfish.main.jnosql.hk2types.GeneralInterfaceModel;
import org.glassfish.main.jnosql.hk2types.Hk2TypesUtil;
import org.glassfish.persistence.jpa.JPADeployer;

import static org.glassfish.main.jnosql.hk2types.Hk2TypesUtil.getTypes;

/**
 *
 * @author Ondro Mihalyi
 */
abstract public class BaseGlassFishClassScanner {

    private static final System.Logger LOG = System.getLogger(BaseGlassFishClassScanner.class.getName());

    /**
     * Whether the entity supported by this repository interface is supported by
     * this provide (e.g. the entity class contains {@link Entity} annotation.
     *
     * @param entityType Type of the entity, analogous to the entity class
     * @return True if the entity is supported, false otherwise
     */
    abstract protected boolean isSupportedEntityType(GeneralInterfaceModel entityType);

    abstract protected String getProviderName();

    /**
     * Whether this scanner is enabled or not. If not enabled, all methods return values as if there were not repository interfaces found.
     * @return Enabled - will return found repository interfaces, not enabled - will not return any repositories. Enabled by default
     */
    protected boolean isEnabled() {
        return true;
    }

    protected JPADeployer getJPADeployer() {
        final ServiceLocator locator = Globals.getDefaultHabitat();
        return locator != null
                ? locator
                        .getService(JPADeployer.class)
                : null;
    }

    protected Set<Class<?>> findClassesWithAnnotation(Class<?> annotation) {
        if (!isEnabled()) {
            return Set.of();
        }
        String annotationClassName = annotation.getName();
        return getTypes().getAllTypes()
                .stream()
                .filter(ClassModel.class::isInstance)
                .filter(type -> type.getAnnotation(annotationClassName) != null)
                .map(ClassModel.class::cast)
                .map(Hk2TypesUtil::typeModelToClass)
                .collect(Collectors.toSet());
    }

    protected Stream<Class<?>> repositoriesStream() {
        return repositoriesStreamMatching(r -> true);
    }

    protected Stream<Class<?>> repositoriesStreamMatching(Predicate<GeneralInterfaceModel> predicate) {
        if (!isEnabled()) {
            return Stream.of();
        }
        // TODO: Prepare a map of types per annotation on the class to avoid iteration over all types
        // TODO: Categorize standard and custom repositories in a single run through the stream
        // - save the result into deployment context, not into this instance, which is shared by all apps
        String providerName = getProviderName();
        return getTypes().getAllTypes()
                .stream()
                .filter(InterfaceModel.class::isInstance)
                .filter(type -> {
                    final AnnotationModel repositoryAnnotation = type.getAnnotation(Repository.class.getName());
                    if (repositoryAnnotation != null) {
                        String provider = repositoryAnnotation.getValue("provider", String.class);
                        return Objects.equals(Repository.ANY_PROVIDER, provider) || providerName.equals(provider);
                    }
                    return false;
                })
                .map(InterfaceModel.class::cast)
                .map(GeneralInterfaceModel::new)
                .filter(this::doesNotHaveUnsupportedMainEntity)
                .filter(predicate)
                .map(GeneralInterfaceModel::toTypeModel)
                .map(Hk2TypesUtil::typeModelToClass);
    }

    private boolean doesNotHaveUnsupportedMainEntity(GeneralInterfaceModel interf) {
        final GeneralInterfaceModel mainEntityType = getMainEntityOfInterface(interf);
        return mainEntityType == null || isSupportedEntityType(mainEntityType);

    }

    protected boolean isSupportedStandardInterface(GeneralInterfaceModel interf) {
        final GeneralInterfaceModel mainEntityType = getMainEntityIfSupportedStandardInterface(interf);
        return null != mainEntityType && isSupportedEntityType(mainEntityType);
    }

    protected GeneralInterfaceModel getMainEntityIfSupportedStandardInterface(GeneralInterfaceModel interf) {
        if (interf.hasTypeParametersWithUnknownType()) {
            return null;
        }
        GeneralInterfaceModel entityCandidate = getMainEntityIfDirectStandardInterface(interf);
        if (entityCandidate != null) {
            return entityCandidate;
        } else {
            final List<GeneralInterfaceModel> entityCandidates = interf.interfacesAsStream()
                    .map(this::getMainEntityIfSupportedStandardInterface)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(2)
                    .toList();
            return entityCandidates.size() == 1
                    ? entityCandidates.getFirst()
                    : null; // either no entity or more than 1
        }
    }

    protected boolean isNotSupportedStandardInterface(GeneralInterfaceModel intfModel) {
        return !isSupportedStandardInterface(intfModel);
    }

    private boolean isDataRepositoryInterface(GeneralInterfaceModel interf) {
        return interf.interfaceName().equals(DataRepository.class.getName());
    }

    private static final Set<String> STANDARD_INTERFACES = Set.of(
            BasicRepository.class.getName(),
            CrudRepository.class.getName(),
            DataRepository.class.getName()
    );

    private GeneralInterfaceModel getMainEntityIfDirectStandardInterface(GeneralInterfaceModel interf) {
        return STANDARD_INTERFACES.contains(interf.interfaceName())
                ? getMainEntityOfInterface(interf)
                : null;
    }

    protected GeneralInterfaceModel getMainEntityOfInterface(GeneralInterfaceModel interf) {
        if (interf.hasTypeParametersWithUnknownType()) {
            return null;
        }
        if (interf.isParameterized()) {
            final Collection<ParameterizedInterfaceModel> parameterizedTypes = interf.parametizedTypes();
            return parameterizedTypes.isEmpty()
                    ? null
                    : new GeneralInterfaceModel(parameterizedTypes.iterator().next());
        } else {
            final List<GeneralInterfaceModel> entityCandidates = interf.interfacesAsStream()
                    .map(this::getMainEntityOfInterface)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(2)
                    .toList();
            return entityCandidates.size() == 1
                    ? entityCandidates.getFirst()
                    : null; // either no entity or more than 1
        }
    }

}

