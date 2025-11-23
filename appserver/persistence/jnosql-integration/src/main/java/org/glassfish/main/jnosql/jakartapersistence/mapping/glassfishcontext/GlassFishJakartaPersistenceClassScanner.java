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
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.JNoSQLJakartaPersistence;
import org.eclipse.jnosql.jakartapersistence.mapping.metadata.JakartaPersistenceClassScanner;
import org.glassfish.hk2.classmodel.reflect.ParameterizedInterfaceModel;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 *
 * @author Ondro Mihalyi
 */
public class GlassFishJakartaPersistenceClassScanner extends BaseGlassFishClassScanner implements JakartaPersistenceClassScanner {

    private static final System.Logger LOG = System.getLogger(GlassFishJakartaPersistenceClassScanner.class.getName());

    @Override
    public Set<Class<?>> entities() {
        final Set<Class<?>> result = findClassesWithAnnotation(Entity.class);
        LOG.log(DEBUG, () -> "Found entities: " + result);
        return result;
    }

    @Override
    public Set<Class<?>> repositories() {
        Set<Class<?>> result = repositoriesStream().collect(toUnmodifiableSet());
        LOG.log(DEBUG, () -> "Detected repository interfaces: " + result);
        return result;
    }

    @Override
    public Set<Class<?>> embeddables() {
        Set<Class<?>> result = findClassesWithAnnotation(Embeddable.class);
        LOG.log(DEBUG, () -> "Detected embeddables: " + result);
        return result;
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
        Set<Class<?>> result = repositoriesStreamMatching(this::isSupportedStandardInterface).collect(toUnmodifiableSet());
        LOG.log(DEBUG, () -> "Detected standard repository interfaces: " + result);
        return result;
    }

    @Override
    public Set<Class<?>> customRepositories() {
        Set<Class<?>> result = repositoriesStreamMatching(this::isNotSupportedStandardInterface).collect(toUnmodifiableSet());
        LOG.log(DEBUG, () -> "Detected custom interfaces: " + result);
        return result;
    }

    @Override
    protected boolean isSupportedEntityType(ParameterizedInterfaceModel entityType) {
        return null != entityType.getRawInterface().getAnnotation(Entity.class.getName());
    }

    @Override
    protected String getProviderName() {
        return JNoSQLJakartaPersistence.PROVIDER;
    }

}
