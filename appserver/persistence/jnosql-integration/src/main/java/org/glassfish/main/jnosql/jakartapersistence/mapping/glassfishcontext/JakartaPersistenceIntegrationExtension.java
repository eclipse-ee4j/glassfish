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

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.interceptor.Interceptor;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.EnsureTransactionInterceptor;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.AbstractRepositoryPersistenceBean;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.Deployment;

/**
 * TODO - consider moving to weld-integration module, following the existing
 * pattern of defining GlassFish extensions there, for example
 * PersistenceExtension. Or maybe CDI integration is better in this module, so
 * that it can be disabled independent of other modules. Then it's probably also
 * good to move CDI-JPA integration from weld-integration into the jpa-container
 * TODO - rename to jakarta-data container, and implement a container, following
 * the JPA container in the jpa-container module
 *
 * @author Ondro Mihalyi
 */
public class JakartaPersistenceIntegrationExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceIntegrationExtension.class.getName());

    /* Must be triggered before the JakartaPersistenceExtension from JNoSQL to register the GlassFishClassScanner
       before it's used there
     */
    void afterBeanDiscovery(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        final Types types = getTypes();

        afterBeanDiscovery.<ApplicationContext>addBean()
                .types(ApplicationContext.class)
                .scope(Dependent.class) // Dependent scope is OK because the state is provided via constructor
                .createWith(ctx -> new ApplicationContext(types));

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

    // exposes all beans we need from JNoSQL - defined in dependencies external to GlassFish
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

        addBean(Converters.class, afterBeanDiscovery, beanManager)
                .scope(ApplicationScoped.class);

        addBean(PersistenceDatabaseManager.class, afterBeanDiscovery, beanManager)
                .scope(ApplicationScoped.class);

        addBean(EntityManagerProvider.class, afterBeanDiscovery, beanManager)
                .scope(ApplicationScoped.class);

        addBean(EnsureTransactionInterceptor.class, afterBeanDiscovery, beanManager)
                .types(EnsureTransactionInterceptor.class)
                .alternative(true)
                .priority(Interceptor.Priority.PLATFORM_BEFORE)
                .scope(ApplicationScoped.class);

        addBean(EnsureTransactionInterceptor.RunInGlobalTransaction.class, afterBeanDiscovery, beanManager)
                .scope(ApplicationScoped.class);
    }

    private static Types getTypes() {
        DeploymentContext deploymentContext
                = Globals.getDefaultHabitat()
                        .getService(Deployment.class)
                        .getCurrentDeploymentContext();
        return deploymentContext.getTransientAppMetaData(Types.class.getName(), Types.class);
    }

    private <BEAN_TYPE> Function<CreationalContext<BEAN_TYPE>, BEAN_TYPE> createBeanProducer(Class<BEAN_TYPE> beanType, BeanManager beanManager) {
        return ctx -> {
            AnnotatedType<BEAN_TYPE> annotatedType = beanManager.createAnnotatedType(beanType);
            InjectionTarget<BEAN_TYPE> injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);

            BEAN_TYPE instance = injectionTarget.produce(ctx);
            injectionTarget.inject(instance, ctx);
            injectionTarget.postConstruct(instance);

            return instance;
        };
    }

    private <BEAN_TYPE> BiConsumer<BEAN_TYPE, CreationalContext<BEAN_TYPE>> createBeanDestroyer(Class<BEAN_TYPE> beanType, BeanManager beanManager) {
        return (instance, ctx) -> {
            AnnotatedType<BEAN_TYPE> annotatedType = beanManager.createAnnotatedType(beanType);
            InjectionTarget<BEAN_TYPE> injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);

            injectionTarget.preDestroy(instance);
            injectionTarget.dispose(instance);
        };
    }

    private <T> BeanConfigurator<T> addBean(Class<T> beanClass, AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        return afterBeanDiscovery.<T>addBean()
                .types(beanClass)
                .createWith(createBeanProducer(beanClass, beanManager))
                .destroyWith(createBeanDestroyer(beanClass, beanManager));
    }

}
