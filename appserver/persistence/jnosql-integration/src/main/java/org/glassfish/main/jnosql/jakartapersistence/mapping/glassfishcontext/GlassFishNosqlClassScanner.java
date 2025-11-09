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

import jakarta.data.repository.DataRepository;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Server global implementation of class scanner for NoSQL repositories. Must be stateless.
 * If necessary, state can be set in the deployment context via transient metadata.
 *
 * @author Ondro Mihalyi
 */
public class GlassFishNosqlClassScanner extends BaseGlassFishClassScanner implements ClassScanner {

    @Override
    protected boolean isEnabled() {
        return false;
    }

    @Override
    public Set<Class<?>> entities() {
        return findClassesWithAnnotation(Entity.class);
    }

    @Override
    public Set<Class<?>> repositories() {
        return repositoriesStream()
                .collect(toUnmodifiableSet());
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
        return repositoriesStreamMatching(this::isSupportedStandardInterface)
                .collect(toUnmodifiableSet());
    }

    @Override
    public Set<Class<?>> customRepositories() {
        // TOTO Return NoSQL custom repositories if they are not supported by Jakarta Persistence
        return Set.of();
    }

    @Override
    protected boolean isSupportedEntityType(GeneralInterfaceModel entityType) {
        return null != entityType.getAnnotation(Entity.class);
    }

    @Override
    protected String getProviderName() {
        return "Eclipse_JNoSQL";
    }

}
