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
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.glassfish.hk2.classmodel.reflect.Types;

/**
 *
 * @author Ondro Mihalyi
 */
public class JnosqlAnnotationsScannerExtension implements Extension {

    private Types types;

    public void setTypes(Types types) {
        this.types = types;
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {

        event.addBean()
                .types(EntitiesMetadata.class, GlassFishEntitiesMetadata.class)
                .qualifiers(Default.Literal.INSTANCE)
                .scope(ApplicationScoped.class)
                .produceWith(instance -> produceMetadataBean(instance, types, beanManager));
    }

    private GlassFishEntitiesMetadata produceMetadataBean(Instance<Object> instance, Types types, BeanManager beanManager) {
        final GlassFishEntitiesMetadata resultBean = new GlassFishEntitiesMetadata(types);

        AnnotatedType<GlassFishEntitiesMetadata> annotatedType = beanManager.createAnnotatedType(GlassFishEntitiesMetadata.class);

        InjectionTargetFactory<GlassFishEntitiesMetadata> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);

        InjectionTarget<GlassFishEntitiesMetadata> injectionTarget = injectionTargetFactory.createInjectionTarget(null);

        CreationalContext<GlassFishEntitiesMetadata> creationalContext = beanManager.createCreationalContext(null);

        injectionTarget.inject(resultBean, creationalContext);
        injectionTarget.postConstruct(resultBean);

        return resultBean;

    }

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
