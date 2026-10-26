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
package org.glassfish.main.jnosql.jakartapersistence;

import org.eclipse.jnosql.jakartapersistence.JNoSQLJakartaPersistence;
import org.eclipse.jnosql.jakartapersistence.mapping.metadata.JakartaPersistenceClassScanner;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.main.jnosql.hk2types.Hk2TypesUtil;
import org.glassfish.persistence.jpa.JPADeployer;

/**
 * Server global implementation of class scanner for JPA repositories. Must be stateless.
 * If necessary, state can be set in the deployment context via transient metadata.
 *
 * @author Ondro Mihalyi
 */
public class GlassFishJakartaPersistenceClassScanner extends BaseGlassFishClassScanner implements JakartaPersistenceClassScanner {

    public static final String JPA_DATA_ENABLED_META_DATA_KEY = "JPADataEnabled";

    private static final Class<jakarta.persistence.Entity> CLASS_ENTITY = jakarta.persistence.Entity.class;
    private static final Class<jakarta.persistence.Embeddable> CLASS_EMBEDDABLE = jakarta.persistence.Embeddable.class;

    @Override
    public boolean isEnabled() {
        final DeploymentContext deploymentContext = Hk2TypesUtil.getDeploymentContext();
        if (deploymentContext != null) {
            Boolean enabled = deploymentContext.getTransientAppMetaData(JPA_DATA_ENABLED_META_DATA_KEY, Boolean.class);
            if (enabled == null) {
                ApplicationInfo appInfo = deploymentContext.getModuleMetaData(ApplicationInfo.class);
                final JPADeployer jpaDeployer = getJPADeployer();
                enabled = !jpaDeployer.getEntityManagerFactories(appInfo).isEmpty() && super.isEnabled();
                deploymentContext.addTransientAppMetaData(JPA_DATA_ENABLED_META_DATA_KEY, enabled);
            }
            return enabled;
        }
        return true;
    }

    @Override
    protected String getProviderName() {
        return JNoSQLJakartaPersistence.PROVIDER;
    }

    @Override
    protected Class<?> getSupportedEntityAnnotation() {
        return CLASS_ENTITY;
    }

    @Override
    protected Class<?> getSupportedEmbeddableAnnotation() {
        return CLASS_EMBEDDABLE;
    }

    private static JPADeployer getJPADeployer() {
        final ServiceLocator locator = Globals.getDefaultHabitat();
        return locator == null ? null : locator.getService(JPADeployer.class);
    }
}
