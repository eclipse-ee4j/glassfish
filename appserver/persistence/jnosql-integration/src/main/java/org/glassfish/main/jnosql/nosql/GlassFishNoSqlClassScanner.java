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
package org.glassfish.main.jnosql.nosql;

import jakarta.data.repository.DataRepository;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.main.jnosql.hk2types.GeneralInterfaceModel;
import org.glassfish.main.jnosql.jakartapersistence.BaseGlassFishClassScanner;
import org.glassfish.main.jnosql.jakartapersistence.GlassFishJakartaPersistenceClassScanner;
import org.glassfish.persistence.jpa.JPADeployer;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.glassfish.main.jnosql.hk2types.Hk2TypesUtil.getDeploymentContext;
import static org.glassfish.main.jnosql.hk2types.Hk2TypesUtil.getTypes;
import static org.glassfish.main.jnosql.jakartapersistence.GlassFishJakartaPersistenceClassScanner.JPA_DATA_ENABLED_META_DATA_KEY;

/**
 * Server global implementation of class scanner for NoSQL repositories. Must be stateless.
 * If necessary, state can be set in the deployment context via transient metadata.
 *
 * @author Ondro Mihalyi
 */
public class GlassFishNoSqlClassScanner extends BaseGlassFishClassScanner implements ClassScanner {

    public static final String NOSQL_DATA_ENABLED_META_DATA_KEY = "NoSqlDataEnabled";

    private static final System.Logger LOG = System.getLogger(GlassFishNoSqlClassScanner.class.getName());

    @Override
    public boolean isEnabled() {
        final DeploymentContext deploymentContext = getDeploymentContext();
        if (deploymentContext != null) {
            Boolean enabled = deploymentContext.getTransientAppMetaData(NOSQL_DATA_ENABLED_META_DATA_KEY, Boolean.class);
            if (enabled == null) {
                ApplicationInfo appInfo = deploymentContext.getModuleMetaData(ApplicationInfo.class);
                enabled = isAnyEntityFound() && super.isEnabled();
                deploymentContext.addTransientAppMetaData(NOSQL_DATA_ENABLED_META_DATA_KEY, enabled);
            }
            return enabled;
        }
        return true;

    }

    private boolean isAnyEntityFound() {
        return getTypes().getAllTypes()
                .stream()
                .filter(ClassModel.class::isInstance)
                .anyMatch(type -> type.getAnnotation(Entity.class.getName()) != null);
    }

    @Override
    public Set<Class<?>> entities() {
        final Set<Class<?>> result = findClassesWithAnnotation(Entity.class);
        LOG.log(DEBUG, () -> "Found NoSql entities: " + result);
        return result;
    }

    @Override
    public Set<Class<?>> repositories() {
        Set<Class<?>> result = repositoriesStream().collect(toUnmodifiableSet());
        LOG.log(DEBUG, () -> "Detected NoSql repository interfaces: " + result);
        return result;
    }

    @Override
    public Set<Class<?>> embeddables() {
        Set<Class<?>> result = findClassesWithAnnotation(Embeddable.class);
        LOG.log(DEBUG, () -> "Detected NoSql embeddables: " + result);
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
        Set<Class<?>> result = enabledRepositoriesStreamMatching(this::isSupportedStandardInterface).collect(toUnmodifiableSet());
        LOG.log(DEBUG, () -> "Detected standard NoSql repository interfaces: " + result);
        return result;
    }

    @Override
    public Set<Class<?>> customRepositories() {
        Set<Class<?>> result = enabledRepositoriesStreamMatching(this::isNotSupportedStandardInterface).collect(toUnmodifiableSet());
        LOG.log(DEBUG, () -> "Detected custom NoSql interfaces: " + result);
        return result;
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
