/*
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation
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

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Converter;

import java.util.Set;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.main.jnosql.hk2types.Hk2TypesUtil;
import org.glassfish.main.jnosql.jakartapersistence.BaseGlassFishClassScanner;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Server global implementation of class scanner for NoSQL repositories. Must be stateless.
 * If necessary, state can be set in the deployment context via transient metadata.
 *
 * @author Ondro Mihalyi
 */
public class GlassFishNoSqlClassScanner extends BaseGlassFishClassScanner {

    public static final String NOSQL_DATA_ENABLED_META_DATA_KEY = "NoSqlDataEnabled";
    public static final String NOSQL_AUTO_APPLY_CONVERTERS_META_DATA_KEY = "NoSqlAutoApplyConverters";

    private static final Class<jakarta.nosql.Entity> CLASS_ENTITY = jakarta.nosql.Entity.class;
    private static final Class<jakarta.nosql.Embeddable> CLASS_EMBEDDABLE = jakarta.nosql.Embeddable.class;

    @Override
    public boolean isEnabled() {
        final DeploymentContext deploymentContext = Hk2TypesUtil.getDeploymentContext();
        if (deploymentContext == null) {
            return true;
        }
        Boolean enabled = deploymentContext.getTransientAppMetaData(NOSQL_DATA_ENABLED_META_DATA_KEY, Boolean.class);
        if (enabled == null) {
            enabled = isAnyEntityFound() && super.isEnabled();
            deploymentContext.addTransientAppMetaData(NOSQL_DATA_ENABLED_META_DATA_KEY, enabled);
        }
        return enabled;
    }

    /*
     * JNoSQL asks for these for every field it maps, so during deployment they are kept in the deployment context
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<? extends AttributeConverter<?, ?>>> autoApplyConverters() {
        final DeploymentContext deploymentContext = Hk2TypesUtil.getDeploymentContext();
        if (deploymentContext == null) {
            return findAutoApplyConverters();
        }
        Set<Class<? extends AttributeConverter<?, ?>>> converters =
                deploymentContext.getTransientAppMetaData(NOSQL_AUTO_APPLY_CONVERTERS_META_DATA_KEY, Set.class);
        if (converters == null) {
            converters = findAutoApplyConverters();
            deploymentContext.addTransientAppMetaData(NOSQL_AUTO_APPLY_CONVERTERS_META_DATA_KEY, converters);
        }
        return converters;
    }

    @Override
    protected String getProviderName() {
        return "Eclipse_JNoSQL";
    }

    @Override
    protected Class<?> getSupportedEntityAnnotation() {
        return CLASS_ENTITY;
    }

    @Override
    protected Class<?> getSupportedEmbeddableAnnotation() {
        return CLASS_EMBEDDABLE;
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends AttributeConverter<?, ?>>> findAutoApplyConverters() {
        return findClassesWithAnnotation(Converter.class)
                .stream()
                .filter(AttributeConverter.class::isAssignableFrom)
                .filter(type -> type.getAnnotation(Converter.class).autoApply())
                .map(type -> (Class<? extends AttributeConverter<?, ?>>) type)
                .collect(toUnmodifiableSet());
    }

    private static boolean isAnyEntityFound() {
        return Hk2TypesUtil.getTypes().getAllTypes()
                .stream()
                .filter(ClassModel.class::isInstance)
                .anyMatch(type -> type.getAnnotation(CLASS_ENTITY.getName()) != null);
    }
}
