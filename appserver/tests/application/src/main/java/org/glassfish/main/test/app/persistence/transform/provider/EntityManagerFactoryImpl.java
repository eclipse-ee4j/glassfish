/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.persistence.transform.provider;

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
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.sql.ResultSetMapping;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class EntityManagerFactoryImpl implements EntityManagerFactory {

    public EntityManagerFactoryImpl(PersistenceUnitInfo persistenceUnitInfo) {
        persistenceUnitInfo.addTransformer(new ClassTransformerImpl());
    }

    @Override
    public EntityManager createEntityManager(EntityManager.CreationOption... options) {
        return new EntityManagerImpl();
    }

    @Override
    public EntityManager createEntityManager(Map<?, ?> properties) {
        return null;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map properties) {
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return null;
    }

    @Override
    public Metamodel getMetamodel() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public Cache getCache() {
        return null;
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return null;
    }

    @Override
    public void addNamedQuery(String name, Query query) {

    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return null;
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return null;
    }

    @Override
    public SchemaManager getSchemaManager() {
        return null;
    }

    @Override
    public <R> Map<String, TypedQueryReference<R>> getNamedQueries(Class<R> resultType) {
        return null;
    }

    @Override
    public <E> Map<String, EntityGraph<? extends E>> getNamedEntityGraphs(Class<E> entityType) {
        return null;
    }

    @Override
    public void runInTransaction(Consumer<EntityManager> work) {

    }

    @Override
    public <R> R callInTransaction(Function<EntityManager, R> work) {
        return null;
    }

    @Override
    public <H extends EntityHandler> void runInTransaction(Class<H> handlerClass, Consumer<H> work) {
    }

    @Override
    public <R, H extends EntityHandler> R callInTransaction(Class<H> handlerClass, Function<H, R> work) {
        return null;
    }

    @Override
    public EntityAgent createEntityAgent(EntityAgent.CreationOption... options) {
        return null;
    }

    @Override
    public StatementReference addNamedStatement(String name, Statement statement) {
        return null;
    }

    @Override
    public Map<String, StatementReference> getNamedStatements() {
        return null;
    }

    @Override
    public <R> Map<String, ResultSetMapping<R>> getResultSetMappings(Class<R> resultType) {
        return null;
    }

    @Override
    public <E> EntityListenerRegistration addListener(Class<E> entityType,
            Class<? extends Annotation> callbackType, Consumer<? super E> listener) {
        return null;
    }

    @Override
    public EntityAgent createEntityAgent(Map<?, ?> properties) {
        return null;
    }

    @Override
    public <R> TypedQueryReference<R> addNamedQuery(String name, TypedQuery<R> query) {
        return null;
    }

}
