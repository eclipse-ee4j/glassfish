/*
 * Copyright (c) 2023, 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.rest;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.sse.Sse;

/**
 * This class is partially copied from <code>org.glassfish.jersey.ext.cdi1x.inject.internal.InjectExtension</code> to work around
 * https://github.com/jakartaee/cdi-tck/issues/474. Once 474 is addressed, we can return to adding :jersey-cdi-rs-inject to the
 * featureset pom again.
 *
 *
 * <p>
 * A utility class that makes sure {@code @Inject} can be used instead of {@code @Context} for the Jakarta REST API
 * classes and interfaces, such as for {@code Configuration}, or {@code Providers}.
 * </p>
 *
 * <p>
 * Note that {@code ContextResolver} can be injected using {@code @Context}, but the Jakarta REST specification does not
 * require the implementation to be capable of doing so. Since {@code ContextResolver} is parametrized type, the
 * injection using CDI's {@Inject} is not supported. The {@code ContextResolver} can be obtained from {@code Providers}.
 * </p>
 */
@SuppressWarnings("unused")
public class RestExtension implements Extension {

    private static final LazyValue<Set<Class<?>>> REST_BOUND_INJECTABLES = Values.lazy((Value<Set<Class<?>>>)
            () -> sumNonJerseyBoundInjectables());

    private void processAnnotatedType(@Observes ProcessAnnotatedType<? extends Application> processAnnotatedType, BeanManager beanManager) {
        final Class<?> baseClass = (Class<?>) processAnnotatedType.getAnnotatedType().getBaseType();

        if (Application.class.isAssignableFrom(baseClass) && Configuration.class.isAssignableFrom(baseClass)) {
            if (!baseClass.isAnnotationPresent(Alternative.class)) {
                processAnnotatedType.veto(); // Filter bean annotated ResourceConfig
            }
        }
    }

    private void beforeDiscoveryObserver(@Observes final BeforeBeanDiscovery bbf, final BeanManager beanManager) {
        CdiComponentProvider.addHK2DepenendencyCheck(RestExtension::isHK2Dependency);
    }

    private static final boolean isHK2Dependency(Class<?> clazz) {
        return REST_BOUND_INJECTABLES.get().contains(clazz);
    }

    private static Set<Class<?>> sumNonJerseyBoundInjectables() {
        final Set<Class<?>> injectables = new HashSet<>();

        // Jakarta REST
        injectables.add(Application.class);
        injectables.add(Configuration.class);
        injectables.add(ContainerRequestContext.class);
        injectables.add(HttpHeaders.class);
        injectables.add(ParamConverterProvider.class);
        injectables.add(Providers.class);
        injectables.add(Request.class);
        injectables.add(ResourceContext.class);
        injectables.add(ResourceInfo.class);
        injectables.add(SecurityContext.class);
        injectables.add(Sse.class);
        injectables.add(UriInfo.class);

        return injectables;
    }
}
