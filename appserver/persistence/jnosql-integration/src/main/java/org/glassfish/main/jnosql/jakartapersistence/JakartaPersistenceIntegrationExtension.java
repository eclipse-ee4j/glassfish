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

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import java.lang.System.Logger;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jnosql.extensions.sql.SqlTemplateFactory;
import org.eclipse.jnosql.extensions.sql.repository.SqlCountAllOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlCountByOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlCursorPaginationOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlDeleteByOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlDeleteOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlExistsByOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlFindAllOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlFindByOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlParameterBasedOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlQueryBuilder;
import org.eclipse.jnosql.extensions.sql.repository.SqlQueryOperation;
import org.eclipse.jnosql.extensions.sql.repository.SqlRepositoryOperationProvider;
import org.eclipse.jnosql.extensions.sql.repository.SqlRepositoryProducer;
import org.eclipse.jnosql.extensions.sql.repository.SqlReturnType;
import org.eclipse.jnosql.extensions.sql.repository.spi.AbstractRepositoryPersistenceBean;
import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManagerProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.EnsureTransactionInterceptor;
import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCacheProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.PersistenceRepositoryProducer;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.main.jnosql.nosql.GlassFishNoSqlClassScanner;

import static jakarta.interceptor.Interceptor.Priority.LIBRARY_BEFORE;
import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.jnosql.util.CdiExtensionUtil.INTEGRATION_BEANS_PRIORITY;
import static org.glassfish.main.jnosql.util.CdiExtensionUtil.addAnnotatedTypes;
import static org.glassfish.main.jnosql.util.CdiExtensionUtil.addBean;

/**
 * TODO - consider moving to weld-integration module, following the existing
 * pattern of defining GlassFish extensions there, for example
 * PersistenceExtension. Or maybe CDI integration is better in this module, so
 * that it can be disabled independent of other modules. Then it's probably also
 * good to move CDI-Persistence integration from weld-integration into the
 * Jakarta Persistence container
 *
 * <p>
 * TODO - rename to jakarta-data container, and implement a container, following
 * the Jakarta Persistence container in the jpa-container module
 *
 * <p>
 * TODO - If delegation is
 * disabled, do not override JNoSQL beans if they are defined in the app
 *
 * @author Ondro Mihalyi
 */
// TODO - activate this extension and JNoSQL extensions from a sniffer only if interfaces with @Repository annotation exist in the app
public class JakartaPersistenceIntegrationExtension implements Extension {

    private static final Logger LOG = System.getLogger(JakartaPersistenceIntegrationExtension.class.getName());

    /*
     * The CDI beans of the JNoSQL Jakarta Persistence driver, except those added after bean discovery below.
     * An application that bundles the driver gets the beans from bean discovery; GlassFish adds them for
     * applications that don't. The driver also uses the beans of the JNoSQL libraries, see JNoSqlIntegrationExtension.
     */
    static final Class<?>[] JAKARTA_PERSISTENCE_BEANS = {
            PersistenceRepositoryProducer.class,
            SqlCountAllOperation.class,
            SqlCountByOperation.class,
            SqlCursorPaginationOperation.class,
            SqlDeleteByOperation.class,
            SqlDeleteOperation.class,
            SqlExistsByOperation.class,
            SqlFindAllOperation.class,
            SqlFindByOperation.class,
            SqlParameterBasedOperation.class,
            SqlQueryBuilder.class,
            SqlQueryOperation.class,
            SqlRepositoryOperationProvider.class,
            SqlRepositoryProducer.class,
            SqlReturnType.class,
            SqlTemplateFactory.class,

            PersistenceDatabaseManagerProvider.class,
            EntityManagerProvider.class,
            EnsureTransactionInterceptor.RunInGlobalTransaction.class,
            PersistenceUnitCacheProvider.class
    };

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        if (new GlassFishJakartaPersistenceClassScanner().isEnabled()) {
            addAnnotatedTypes(beforeBeanDiscovery, JAKARTA_PERSISTENCE_BEANS);
        }
    }

    /*
     * Must be triggered before the JakartaPersistenceExtension from JNoSQL to register
     * the GlassFishClassScanner before it's used there
     */
    void afterBeanDiscovery(
        @Observes
        @Priority(LIBRARY_BEFORE)
        AfterBeanDiscovery afterBeanDiscovery,
        BeanManager beanManager
    ) {
        boolean jpaEnabled = new GlassFishJakartaPersistenceClassScanner().isEnabled();
        boolean noSqlEnabled = new GlassFishNoSqlClassScanner().isEnabled();
        LOG.log(INFO, () -> "Jakarta Persistence: " + toEnabled(jpaEnabled) + ", NoSQL: " + toEnabled(noSqlEnabled));

        // We use empty types if we don't need real types instance to free some memory after deployment
        // TODO: Keep only required types - entities and repositories, to avoid keeping the whole types instance after deployment
        // TODO: optionally create repositories at deployment, to fail deployment if an error in repository definition
        final Types types = (jpaEnabled || noSqlEnabled) ? getTypes() : emptyTypes();

        afterBeanDiscovery.<JakartaDataApplicationContext>addBean()
                .types(JakartaDataApplicationContext.class)
                .scope(Dependent.class) // Dependent scope is OK because the state is provided via constructor
                .createWith(ctx -> new JakartaDataApplicationContext(types));

        addBean(GlassFishRepositoryInterceptor.class, afterBeanDiscovery, beanManager)
                .types(MethodInterceptor.class)
                .qualifiers(MethodInterceptor.Repository.INSTANCE)
                .alternative(true)
                .priority(PLATFORM_BEFORE)
                .scope(ApplicationScoped.class);

        defineJNoSqlBeans(afterBeanDiscovery, beanManager);
    }

    /*
     * Exposes all beans we need from JNoSQL - defined in dependencies external to GlassFish
     * Exposes them as alternatives so that they override any beans defined in the app
     */
    private void defineJNoSqlBeans(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        LOG.log(DEBUG, "defineJNoSqlBeans(afterBeanDiscovery={0}, beanManager={1})", afterBeanDiscovery, beanManager);
        // This is just to define beanManager for some classes in an EE context, they shouldn't be injected.
        // In Java SE context, the whole JVM is a single bean archive, so it's not needed there. But in EE,
        // only beans in the deployed app are added to a bean archive. Beans defined by an EE container
        // don't automatically have a bean archive.
        Class<?>[] dummyBeansClasses = {AbstractBean.class, AbstractRepositoryPersistenceBean.class};
        for (var dummyBeanClass : dummyBeansClasses) {
            afterBeanDiscovery.addBean()
                              .types(dummyBeanClass)
                              .scope(ApplicationScoped.class)
                              .qualifiers(Default.Literal.INSTANCE)
                              .createWith(ctx -> null);
        }

        addBean(EnsureTransactionInterceptor.class, afterBeanDiscovery, beanManager)
            .scope(ApplicationScoped.class)
            // enable as alternative to override beans in case they are added as application libraries
            .alternative(true)
            .priority(INTEGRATION_BEANS_PRIORITY);
    }

    private static String toEnabled(boolean enabled) {
        return enabled ? "Enabled" : "Disabled";
    }

    private static Types getTypes() {
        DeploymentContext deploymentContext =
            Globals.get(Deployment.class)
                   .getCurrentDeploymentContext();

        return deploymentContext.getTransientAppMetaData(Types.class.getName(), Types.class);
    }

    private static Types emptyTypes() {
        return new Types() {
            @Override
            public Collection<Type> getAllTypes() {
                return Set.of();
            }

            @Override
            public Type getBy(String string) {
                return null;
            }

            @Override
            public <T extends Type> T getBy(Class<T> type, String string) {
                return null;
            }

        };
    }
}
