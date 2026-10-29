/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

import java.util.Set;

import org.eclipse.jnosql.extensions.sql.repository.SqlRepositoryProducer;
import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManagerProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.EnsureTransactionInterceptor;
import org.eclipse.jnosql.jakartapersistence.mapping.EnsureTransactionInterceptor.RunInGlobalTransaction;
import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCacheProvider;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.jnosql.util.LibraryBeans.applicationScopedClassesIn;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * An application that bundles the JNoSQL Jakarta Persistence driver gets its beans from bean discovery.
 * GlassFish lists them instead, so a bean that a JNoSQL upgrade adds or renames has to be added to that list as well.
 */
class JakartaPersistenceIntegrationExtensionTest {

    /** Added by the extension after bean discovery */
    private static final Set<Class<?>> ADDED_AFTER_BEAN_DISCOVERY = Set.of(EntityManagerProvider.class,
            EnsureTransactionInterceptor.class, PersistenceDatabaseManagerProvider.class,
            PersistenceUnitCacheProvider.class, RunInGlobalTransaction.class);

    @Test
    void registersTheBeansOfTheDriver() throws Exception {
        Set<Class<?>> beans = applicationScopedClassesIn(SqlRepositoryProducer.class);
        beans.removeAll(ADDED_AFTER_BEAN_DISCOVERY);

        assertThat(Set.of(JakartaPersistenceIntegrationExtension.JAKARTA_PERSISTENCE_BEANS), equalTo(beans));
    }
}
