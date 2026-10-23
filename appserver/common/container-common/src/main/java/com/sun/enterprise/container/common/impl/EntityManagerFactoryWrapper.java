/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityHandler;
import jakarta.persistence.EntityListenerRegistration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SchemaManager;
import jakarta.persistence.Statement;
import jakarta.persistence.StatementReference;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.TypedQueryReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.sql.ResultSetMapping;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

/**
 * Wrapper for application references to entity manager factories. A new instance of this class will be created for each
 * injected EntityManagerFactory reference or each lookup of an EntityManagerFactory reference within the component jndi
 * environment.
 *
 * @author Kenneth Saks
 */
public class EntityManagerFactoryWrapper implements EntityManagerFactory, Serializable {

    private static final long serialVersionUID = 4L;

    private final String unitName;
    private transient InvocationManager invocationManager;
    private transient ComponentEnvManager componentEnvManager;

    private transient EntityManagerFactory entityManagerFactory;

    public EntityManagerFactoryWrapper(String unitName, InvocationManager invMgr, ComponentEnvManager compEnvMgr) {
        this.unitName = unitName;
        this.invocationManager = invMgr;
        this.componentEnvManager = compEnvMgr;
    }

    private EntityManagerFactory getDelegate() {
        if (entityManagerFactory == null) {
            entityManagerFactory = lookupEntityManagerFactory(invocationManager, componentEnvManager, unitName);
            if (entityManagerFactory == null) {
                throw new IllegalStateException("Unable to retrieve EntityManagerFactory for unitName " + unitName);
            }
        }
        return entityManagerFactory;
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    @Nonnull
    public EntityAgent createEntityAgent(@Nullable EntityAgent.CreationOption... options) {
        return getDelegate().createEntityAgent(options);
    }

    @Override
    @Nonnull
    public EntityAgent createEntityAgent(@Nullable Map<?, ?> properties) {
        return getDelegate().createEntityAgent(properties);
    }

    @Override
    public EntityManager createEntityManager(@Nullable EntityManager.CreationOption... options) {
        return getDelegate().createEntityManager(options);
    }

    @Override
    public EntityManager createEntityManager(@Nullable Map<?, ?> map) {
        return getDelegate().createEntityManager(map);
    }

    @Override
    public EntityManager createEntityManager(@Nonnull SynchronizationType synchronizationType, @Nullable Map<?, ?> map) {
        return getDelegate().createEntityManager(synchronizationType, map);
    }

    @Nonnull
    @Override
    public <E> EntityListenerRegistration addListener(
            @Nonnull Class<E> entityType,
            @Nonnull Class<? extends Annotation> callbackType,
            @Nonnull Consumer<? super E> listener) {
        return getDelegate().addListener(entityType, callbackType, listener);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        getDelegate().addNamedEntityGraph(graphName, entityGraph);
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        getDelegate().addNamedQuery(name, query);
    }

    @Override
    @Nonnull
    public <R> TypedQueryReference<R> addNamedQuery(@Nonnull String name, @Nonnull TypedQuery<R> query) {
        return getDelegate().addNamedQuery(name, query);
    }

    @Override
    @Nonnull
    public StatementReference addNamedStatement(@Nonnull String name, @Nonnull Statement statement) {
        return getDelegate().addNamedStatement(name, statement);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return getDelegate().unwrap(cls);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getDelegate().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return getDelegate().getMetamodel();
    }

    @Override
    public Map<java.lang.String, java.lang.Object> getProperties() {
        return getDelegate().getProperties();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public Cache getCache() {
        return getDelegate().getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return getDelegate().getPersistenceUnitUtil();
    }

    @Override
    public <R> R callInTransaction(Function<EntityManager, R> work) {
        return getDelegate().callInTransaction(work);
    }

    @Override
    public <R, H extends EntityHandler> R callInTransaction(@Nonnull Class<H> handlerClass, @Nonnull Function<H,R> work) {
        return getDelegate().callInTransaction(handlerClass, work);
    }

    @Override
    public <E> Map<String, EntityGraph<? extends E>> getNamedEntityGraphs(Class<E> entityType) {
        return getDelegate().getNamedEntityGraphs(entityType);
    }

    @Override
    public <R> Map<String, TypedQueryReference<R>> getNamedQueries(Class<R> resultType) {
        return getDelegate().getNamedQueries(resultType);
    }

    @Override
    @Nonnull
    public Map<String, StatementReference> getNamedStatements() {
        return getDelegate().getNamedStatements();
    }

    @Override
    @Nonnull
    public <R> Map<String, ResultSetMapping<R>> getResultSetMappings(@Nonnull Class<R> resultType) {
        return getDelegate().getResultSetMappings(resultType);
    }

    @Override
    public SchemaManager getSchemaManager() {
        return getDelegate().getSchemaManager();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return getDelegate().getTransactionType();
    }

    @Override
    public void runInTransaction(Consumer<EntityManager> work) {
        getDelegate().runInTransaction(work);
    }

    @Override
    public <H extends EntityHandler> void runInTransaction(@Nonnull Class<H> handlerClass, @Nonnull Consumer<H> work) {
        getDelegate().runInTransaction(handlerClass, work);
    }

    /**
     * Lookup physical EntityManagerFactory based on current component invocation.
     *
     * @param invMgr invocationmanager
     * @param emfUnitName unit name of entity manager factory or null if not specified.
     * @return EntityManagerFactory or null if no matching factory could be found.
     **/
    static EntityManagerFactory lookupEntityManagerFactory(InvocationManager invMgr, ComponentEnvManager compEnvMgr, String emfUnitName) {
        ComponentInvocation componentInvocation = invMgr.getCurrentInvocation();

        EntityManagerFactory entityManagerFactory = null;

        if (componentInvocation != null) {
            Object desc = compEnvMgr.getCurrentJndiNameEnvironment();
            if (desc != null) {
                entityManagerFactory = lookupEntityManagerFactory(componentInvocation.getInvocationType(), emfUnitName, desc);
            }
        }

        return entityManagerFactory;
    }

    public static EntityManagerFactory lookupEntityManagerFactory(ComponentInvocationType componentInvocationType, String emfUnitName, Object descriptor) {
        Application application = null;
        BundleDescriptor module = null;
        EntityManagerFactory entityManagerFactory = null;

        switch (componentInvocationType) {

            case EJB_INVOCATION:

                if (descriptor instanceof EjbDescriptor) {
                    EjbDescriptor ejbDesc = (EjbDescriptor) descriptor;
                    module = (BundleDescriptor) ejbDesc.getEjbBundleDescriptor().getModuleDescriptor().getDescriptor();
                    application = module.getApplication();
                    break;
                }
                // EJB invocation in web bundle?
                // fall through into web case...

            case SERVLET_INVOCATION:

                module = (WebBundleDescriptor) descriptor;
                application = module.getApplication();

                break;

            case APP_CLIENT_INVOCATION:

                module = (ApplicationClientDescriptor) descriptor;
                application = module.getApplication();

                break;

            default:

                break;
        }

        // First check module-level for a match.
        if (module != null) {
            if (emfUnitName != null) {
                entityManagerFactory = module.getEntityManagerFactory(emfUnitName);
            } else {
                Set<EntityManagerFactory> emFactories = module.getEntityManagerFactories();
                if (emFactories.size() == 1) {
                    entityManagerFactory = emFactories.iterator().next();
                }
            }
        }

        // If we're in an .ear and no module-level persistence unit was
        // found, check for an application-level match.
        if ((application != null) && (entityManagerFactory == null)) {
            if (emfUnitName != null) {

                entityManagerFactory = application.getEntityManagerFactory(emfUnitName, module);

            } else {
                Set<EntityManagerFactory> entityManagerFactories = application.getEntityManagerFactories();
                if (entityManagerFactories.size() == 1) {
                    entityManagerFactory = entityManagerFactories.iterator().next();
                }
            }
        }

        return entityManagerFactory;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        // Initialize the transients that were passed at ctor.
        ServiceLocator defaultServiceLocator = Globals.getDefaultHabitat();
        invocationManager = defaultServiceLocator.getService(InvocationManager.class);
        componentEnvManager = defaultServiceLocator.getService(ComponentEnvManager.class);
    }
}
