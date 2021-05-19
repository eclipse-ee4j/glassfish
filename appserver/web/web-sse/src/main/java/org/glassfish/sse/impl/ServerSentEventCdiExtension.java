/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.sse.impl;

import org.glassfish.sse.api.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A CDI extension that creates ServerSentEventHandlerContext beans so that
 * they can be injected into other EE components
 *
 * @author Jitendra Kotamraju
 */
public class ServerSentEventCdiExtension implements Extension {

    private final Logger LOGGER = Logger.getLogger(ServerSentEventCdiExtension.class.getName());

    // path --> application
    private final Map<String, ServerSentEventApplication> applicationMap
        = new HashMap<String, ServerSentEventApplication>();

    public Map<String, ServerSentEventApplication> getApplicationMap() {
        return applicationMap;
    }

    @SuppressWarnings("UnusedDeclaration")
    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
        bbd.addQualifier(ServerSentEventContext.class);
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    static class WebHandlerContextAnnotationLiteral extends AnnotationLiteral<ServerSentEventContext> implements ServerSentEventContext {
        private final String path;

        WebHandlerContextAnnotationLiteral(String path) {
            this.path = path;
        }

        @Override
        public String value() {
            return path;
        }
    }

    static class ServerSentEventHandlerContextBean implements Bean<ServerSentEventHandlerContext> {

        private final String path;
        private final ServerSentEventHandlerContext instance;
        private final Class<?> handlerClass;

        ServerSentEventHandlerContextBean(String path, ServerSentEventHandlerContext instance, Class<?> handlerClass) {
            this.path = path;
            this.instance = instance;
            this.handlerClass = handlerClass;
        }

        @Override
        public Set<Type> getTypes() {
            Set<Type> types = new HashSet<Type>();
            types.add(new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[] {handlerClass};
                }

                @Override
                public Type getRawType() {
                    return ServerSentEventHandlerContext.class;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            });
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> qualifiers = new HashSet<Annotation>();
            qualifiers.add(new WebHandlerContextAnnotationLiteral(path));
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return ApplicationScoped.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public Class<?> getBeanClass() {
            return ServerSentEventHandlerContext.class;
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public boolean isNullable() {
            return false;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        @Override
        public ServerSentEventHandlerContext create(CreationalContext<ServerSentEventHandlerContext> context) {
            return instance;
        }

        @Override
        public void destroy(ServerSentEventHandlerContext instance, CreationalContext<ServerSentEventHandlerContext> context) {
        }

    }

    // For each ServerSentEvent hanlder, it creates a corresponding ServerSentHandlerContext
    // This context can be got anywhere from BeanManager
    @SuppressWarnings("UnusedDeclaration")
    void afterBeanDiscovery(@Observes AfterBeanDiscovery bbd) {
        for(Map.Entry<String, ServerSentEventApplication> entry : applicationMap.entrySet()) {
            bbd.addBean(new ServerSentEventHandlerContextBean(entry.getKey(), entry.getValue().getHandlerContext(),
                    entry.getValue().getHandlerClass()));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat,
            BeanManager beanManager) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("scanning type: " + pat.getAnnotatedType().getJavaClass().getName());
        }

        for (Annotation an : pat.getAnnotatedType().getAnnotations()) {
            Class clazz = pat.getAnnotatedType().getJavaClass();

            if (an instanceof ServerSentEvent) {
                if (!ServerSentEventHandler.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException("Invalid base class '"
                            + clazz.getName() + "' for handler.");
                }
                ServerSentEvent wh = (ServerSentEvent) an;
                String path = normalizePath(wh.value());
                ServerSentEventApplication app = applicationMap.get(path);
                if (app != null) {
                    throw new RuntimeException("Two ServerSentEvent handlers are mapped to same path="+path);
                }
                app = new ServerSentEventApplication(clazz, path);
                applicationMap.put(path, app);
            }
        }
    }

    private String normalizePath(String path) {
        path = path.trim();
        return path.startsWith("/") ? path : "/" + path;
    }

}
