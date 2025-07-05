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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql.DefaultEntitiesMetadata;
import org.glassfish.main.jnosql.jakartapersistence.mapping.reflection.nosql.ReflectionGroupEntityMetadata;

/**
 *
 * @author Ondro Mihalyi
 */
public class JnosqlAnnotationsScannerExtension implements Extension {

    private Types types;

    public void setTypes(Types types) {
        this.types = types;
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        afterBeanDiscovery.<ReflectionGroupEntityMetadata>addBean()
                .types(GroupEntityMetadata.class)
                .scope(ApplicationScoped.class)
                .createWith(createBeanProducer(ReflectionGroupEntityMetadata.class, beanManager))
                .destroyWith(createBeanDestroyer(ReflectionGroupEntityMetadata.class, beanManager));

        afterBeanDiscovery.<DefaultEntitiesMetadata>addBean()
                .types(EntitiesMetadata.class)
                .scope(ApplicationScoped.class)
                .createWith(createBeanProducer(DefaultEntitiesMetadata.class, beanManager))
                .destroyWith(createBeanDestroyer(DefaultEntitiesMetadata.class, beanManager));

        defineJNoSqlBeans(afterBeanDiscovery, beanManager);
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

    // exposes all beans we need from JNoSQL - defined in dependencies external to GlassFish
    private void defineJNoSqlBeans(AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        /* This is just to define beanManager for AbstractBean in an EE context, it shouldn't be injected
           In Java SE context, the whole JVM is a single bean archive, so it's not needed there. But in EE,
           only beans in the deployed app are added to a bean archive. Beans defined by an EE container
           don't automatically have bean archive.
         */
        afterBeanDiscovery.addBean()
                .types(AbstractBean.class)
                .scope(ApplicationScoped.class)
                .qualifiers(Default.Literal.INSTANCE)
                .createWith(ctx -> {
                    return null;
                });

        afterBeanDiscovery.<PersistenceDocumentTemplate>addBean()
                .types(DocumentTemplate.class, PersistenceDocumentTemplate.class)
                .scope(ApplicationScoped.class)
                .createWith(createBeanProducer(PersistenceDocumentTemplate.class, beanManager))
                .destroyWith(createBeanDestroyer(PersistenceDocumentTemplate.class, beanManager));

        afterBeanDiscovery.<Converters>addBean()
                .types(Converters.class)
                .scope(ApplicationScoped.class)
                .createWith(createBeanProducer(Converters.class, beanManager))
                .destroyWith(createBeanDestroyer(Converters.class, beanManager));

        afterBeanDiscovery.<PersistenceDatabaseManager>addBean()
                .types(PersistenceDatabaseManager.class)
                .scope(ApplicationScoped.class)
                .createWith(createBeanProducer(PersistenceDatabaseManager.class, beanManager))
                .destroyWith(createBeanDestroyer(PersistenceDatabaseManager.class, beanManager));

    }

    // this is meant to replace DefaultEntitiesMetadata with data from GlassFish's Types
    public static class GlassFishEntitiesMetadata implements EntitiesMetadata {

        private Types types;

        private final Map<String, EntityMetadata> mappings;
        private final Map<Class<?>, EntityMetadata> classes;
        private final Map<String, EntityMetadata> findBySimpleName;
        private final Map<String, EntityMetadata> findByClassName;

        @Inject
        private GroupEntityMetadata extension;

        public GlassFishEntitiesMetadata(Types types) {
            this.types = types;
            this.mappings = new ConcurrentHashMap<>();
            this.classes = new ConcurrentHashMap<>();
            this.findBySimpleName = new ConcurrentHashMap<>();
            this.findByClassName = new ConcurrentHashMap<>();
        }

        @Override
        public EntityMetadata get(Class<?> entity) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public Map<String, InheritanceMetadata> findByParentGroupByDiscriminatorValue(Class<?> parent) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public EntityMetadata findByName(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public Optional<EntityMetadata> findBySimpleName(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public Optional<EntityMetadata> findByClassName(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

    }
}
