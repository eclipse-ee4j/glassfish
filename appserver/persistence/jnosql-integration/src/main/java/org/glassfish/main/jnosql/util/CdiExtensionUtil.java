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
package org.glassfish.main.jnosql.util;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.interceptor.Interceptor;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 *
 * @author Ondro Mihalyi
 */
public final class CdiExtensionUtil {

    public static final int INTEGRATION_BEANS_PRIORITY = Interceptor.Priority.PLATFORM_AFTER;

    private CdiExtensionUtil() {
        // utility class
    }

    /**
     * Adds the given classes as beans, as bean discovery would if they were part of the application.
     * They are enabled as alternatives to override the same beans in case they are added as application libraries.
     */
    public static void addAnnotatedTypes(BeforeBeanDiscovery beforeBean, Class<?>... types) {
        for (Class<?> type : types) {
            beforeBean.addAnnotatedType(type, "GlassFish " + type.getName())
                    .add(Alternative.Literal.INSTANCE)
                    .add(IntegrationBeansPriority.LITERAL);
        }
    }

    public static <T> BeanConfigurator<T> addBean(Class<T> beanClass, AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        return afterBeanDiscovery.<T>addBean()
                .types(beanClass)
                .createWith(createBeanProducer(beanClass, beanManager))
                .destroyWith(createBeanDestroyer(beanClass, beanManager));
    }

    public static <BEAN_TYPE> BiConsumer<BEAN_TYPE, CreationalContext<BEAN_TYPE>> createBeanDestroyer(Class<BEAN_TYPE> beanType, BeanManager beanManager) {
        return (instance, ctx) -> {
            AnnotatedType<BEAN_TYPE> annotatedType = beanManager.createAnnotatedType(beanType);
            InjectionTarget<BEAN_TYPE> injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);

            injectionTarget.preDestroy(instance);
            injectionTarget.dispose(instance);
        };
    }

    public static <BEAN_TYPE> Function<CreationalContext<BEAN_TYPE>, BEAN_TYPE> createBeanProducer(Class<BEAN_TYPE> beanType, BeanManager beanManager) {
        return ctx -> {
            AnnotatedType<BEAN_TYPE> annotatedType = beanManager.createAnnotatedType(beanType);
            InjectionTarget<BEAN_TYPE> injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);

            BEAN_TYPE instance = injectionTarget.produce(ctx);
            injectionTarget.inject(instance, ctx);
            injectionTarget.postConstruct(instance);

            return instance;
        };
    }

    // jakarta.annotation.Priority has no literal
    @Priority(INTEGRATION_BEANS_PRIORITY)
    private static final class IntegrationBeansPriority {
        static final Priority LITERAL = IntegrationBeansPriority.class.getAnnotation(Priority.class);
    }

}
