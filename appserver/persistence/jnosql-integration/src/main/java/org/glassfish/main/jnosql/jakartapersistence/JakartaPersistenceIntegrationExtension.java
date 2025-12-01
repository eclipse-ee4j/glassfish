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

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.interceptor.Interceptor;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManagerProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.EnsureTransactionInterceptor;
import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCacheProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.AbstractRepositoryPersistenceBean;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.main.jnosql.util.CdiExtensionUtil;

import static org.glassfish.main.jnosql.util.CdiExtensionUtil.addBean;

/**
 * TODO - consider moving to weld-integration module, following the existing
 * pattern of defining GlassFish extensions there, for example
 * PersistenceExtension. Or maybe CDI integration is better in this module, so
 * that it can be disabled independent of other modules. Then it's probably also
 * good to move CDI-JPA integration from weld-integration into the jpa-container
 * TODO - rename to jakarta-data container, and implement a container, following
 * the JPA container in the jpa-container module TODO - If delegation is
 * disabled, do not override JNoSQL beans if they are defined in the app
 *
 * @author Ondro Mihalyi
 */
// TODO - activate this extension and JNoSQL extensions from a sniffer only if interfaces with @Repository annotation exist in the app
public class JakartaPersistenceIntegrationExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceIntegrationExtension.class.getName());

    /* Must be triggered before the JakartaPersistenceExtension from JNoSQL to register the GlassFishClassScanner
       before it's used there
     */
    void afterBeanDiscovery(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        final Types types = getTypes();

        afterBeanDiscovery.<JakartaDataApplicationContext>addBean()
                .types(JakartaDataApplicationContext.class)
                .scope(Dependent.class) // Dependent scope is OK because the state is provided via constructor
                .createWith(ctx -> new JakartaDataApplicationContext(types));

        // is this needed? Isn't it loaded by service loader?
        addBean(GlassFishJakartaPersistenceClassScanner.class, afterBeanDiscovery, beanManager)
                .types(ClassScanner.class, GlassFishJakartaPersistenceClassScanner.class)
                .scope(ApplicationScoped.class);

        addBean(GlassFishRepositoryInterceptor.class, afterBeanDiscovery, beanManager)
                .types(MethodInterceptor.class)
                .qualifiers(MethodInterceptor.Repository.INSTANCE)
                .alternative(true)
                .priority(Interceptor.Priority.PLATFORM_BEFORE)
                .scope(ApplicationScoped.class);

        defineJNoSqlBeans(afterBeanDiscovery, beanManager);
    }

    /*
     * Exposes all beans we need from JNoSQL - defined in dependencies external to GlassFish
     * Exposes them as alternatives so that they override any beans defined in the app
     */
    private void defineJNoSqlBeans(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        /* This is just to define beanManager for some classes in an EE context, they shouldn't be injected.
           In Java SE context, the whole JVM is a single bean archive, so it's not needed there. But in EE,
           only beans in the deployed app are added to a bean archive. Beans defined by an EE container
           don't automatically have bean archive.
         */
        Class<?>[] dummyBeansClasses = {AbstractBean.class, AbstractRepositoryPersistenceBean.class};
        for (var dummyBeanClass : dummyBeansClasses) {
            afterBeanDiscovery.addBean()
                    .types(dummyBeanClass)
                    .scope(ApplicationScoped.class)
                    .qualifiers(Default.Literal.INSTANCE)
                    .createWith(ctx -> null);
        }

        List<BeanConfigurator<?>> configurations = List.of(
                addBean(Converters.class, afterBeanDiscovery, beanManager),
                addBean(PersistenceDatabaseManagerProvider.class, afterBeanDiscovery, beanManager),
                addBean(EntityManagerProvider.class, afterBeanDiscovery, beanManager),
                addBean(EnsureTransactionInterceptor.class, afterBeanDiscovery, beanManager),
                addBean(EnsureTransactionInterceptor.RunInGlobalTransaction.class, afterBeanDiscovery, beanManager),
                addBean(PersistenceUnitCacheProvider.class, afterBeanDiscovery, beanManager)
        );

        for (BeanConfigurator<?> configurator : configurations) {
            configurator
                    .scope(ApplicationScoped.class)
                    // enable as alternative to override beans in case they are added as application libraries
                    .alternative(true)
                    .priority(CdiExtensionUtil.INTEGRATION_BEANS_PRIORITY);
        }
    }

    private static Types getTypes() {
        DeploymentContext deploymentContext
                = Globals.getDefaultHabitat()
                        .getService(Deployment.class)
                        .getCurrentDeploymentContext();
        return deploymentContext.getTransientAppMetaData(Types.class.getName(), Types.class);
    }

}
