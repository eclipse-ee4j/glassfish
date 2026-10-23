/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.container.common.spi.JavaEEContainer;
import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.logging.LogDomains;

import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.ConnectionConsumer;
import jakarta.persistence.ConnectionFunction;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FindOption;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockOption;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.Query;
import jakarta.persistence.RefreshOption;
import jakarta.persistence.Statement;
import jakarta.persistence.StatementOrTypedQuery;
import jakarta.persistence.StatementReference;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.TransactionRequiredException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.TypedQueryReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaSelect;
import jakarta.persistence.criteria.CriteriaStatement;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.sql.ResultSetMapping;
import jakarta.transaction.TransactionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.ComponentInvocationHandler;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CLEAR;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CLOSE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CONTAINS;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_ENTITY_GRAPH_CLASS;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_ENTITY_GRAPH_STRING;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_NAMED_QUERY;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_NAMED_STORED_PROCEDURE_QUERY;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_NATIVE_QUERY_STRING;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_NATIVE_QUERY_STRING_CLASS;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_NATIVE_QUERY_STRING_STRING;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_QUERY;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_QUERY_CRITERIA_DELETE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_QUERY_CRITERIA_QUERY;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_QUERY_CRITERIA_UPDATE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_QUERY_STRING_CLASS;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_STORED_PROCEDURE_QUERY;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_STORED_PROCEDURE_QUERY_STRING_CLASS;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.CREATE_STORED_PROCEDURE_QUERY_STRING_STRING;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.DETATCH;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.FIND;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.FIND_CLASS_OBJECT_LOCKMODETYPE_PROPERTIES;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.FLUSH;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_CRITERIA_BUILDER;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_DELEGATE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_ENTITY_GRAPH;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_ENTITY_GRAPHS;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_ENTITY_MANAGER_FACTORY;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_FLUSH_MODE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_LOCK_MODE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_METAMODEL;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_PROPERTIES;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_REFERENCE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.GET_TRANSACTION;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.IS_JOINED_TO_TRANSACTION;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.IS_OPEN;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.JOIN_TRANSACTION;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.LOCK;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.LOCK_LOCKMODETYPE_MAP;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.MERGE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.PERSIST;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.REFRESH;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.REFRESH_OBJECT_LOCKMODETYPE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.REFRESH_OBJECT_LOCKMODETYPE_MAP;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.REFRESH_OBJECT_PROPERTIES;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.REMOVE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.SET_FLUSH_MODE;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.SET_PROPERTY;
import static com.sun.enterprise.container.common.spi.util.EntityManagerMethod.UNWRAP;
import static jakarta.persistence.SynchronizationType.SYNCHRONIZED;
import static jakarta.persistence.SynchronizationType.UNSYNCHRONIZED;
import static java.util.logging.Level.FINE;

/**
 * Implementation of a container-managed entity manager. A new instance of this class will be created for each injected
 * EntityManager reference or each lookup of an EntityManager reference within the component jndi environment. The
 * underlying EntityManager object does not support concurrent access. Likewise, this wrapper does not support
 * concurrent access.
 *
 * @author Kenneth Saks
 */
public class EntityManagerWrapper implements EntityManager, Serializable {

    private static final Logger LOG = LogDomains.getLogger(EntityManagerWrapper.class, LogDomains.UTIL_LOGGER, false);

    // Serializable state

    private String unitName;
    private PersistenceContextType contextType;
    private Map emProperties;
    private SynchronizationType synchronizationType;

    // transient state

    transient private EntityManagerFactory entityManagerFactory;
    transient private TransactionManager transactionManager;
    transient private InvocationManager invocationManager;

    // Only used to cache entity manager with EXTENDED persistence context
    transient private EntityManager extendedEntityManager;

    transient private ComponentEnvManager compEnvMgr;
    transient private CallFlowAgent callFlowAgent;

    public EntityManagerWrapper(TransactionManager txManager, InvocationManager invMgr, ComponentEnvManager compEnvMgr, CallFlowAgent callFlowAgent) {
        this.transactionManager = txManager;
        this.invocationManager = invMgr;
        this.compEnvMgr = compEnvMgr;
        this.callFlowAgent = callFlowAgent;
    }

    public void initializeEMWrapper(String unitName, PersistenceContextType contextType, SynchronizationType synchronizationType, Map emProperties) {
        this.unitName = unitName;
        this.contextType = contextType;
        this.synchronizationType = synchronizationType;
        this.emProperties = emProperties;

        if (contextType == PersistenceContextType.EXTENDED) {
            // We are initializing an extended EM. The physical em is already created and stored in SessionContext to
            // enable persistence context propagation.
            // Initialize the delegate eagerly to support use cases like issue 11805
            _getDelegate();
        }
    }

    private void init() {
        entityManagerFactory = EntityManagerFactoryWrapper.lookupEntityManagerFactory(invocationManager, compEnvMgr, unitName);

        if (entityManagerFactory == null) {
            throw new IllegalStateException("Unable to retrieve EntityManagerFactory for unitName " + unitName);
        }

    }

    private void doTransactionScopedTxCheck() {
        if (contextType != PersistenceContextType.TRANSACTION) {
            return;
        }

        doTxRequiredCheck();

    }

    private void doTxRequiredCheck() {
        if (entityManagerFactory == null) {
            init();
        }

        if (getCurrentTransaction() == null) {
            throw new TransactionRequiredException();
        }

    }

    private EntityManager _getDelegate() {

        // Populate any transient objects the first time
        // this method is called.

        if (entityManagerFactory == null) {
            init();
        }

        EntityManager delegate;

        if (contextType == PersistenceContextType.TRANSACTION) {

            JavaEETransaction tx = getCurrentTransaction();

            if (tx != null) {

                // If there is an active extended persistence context
                // for the same entity manager factory and the same tx,
                // it takes precedence.
                PhysicalEntityManagerWrapper propagatedPersistenceContext = getExtendedEntityManager(tx, entityManagerFactory);
                if (propagatedPersistenceContext == null) {

                    propagatedPersistenceContext = getTxEntityManager(tx, entityManagerFactory);

                    if (propagatedPersistenceContext == null) {

                        // If there is a transaction and this is the first access of the wrapped entity manager, create an
                        // actual entity manager and associate it with the entity manager factory.

                        EntityManager entityManager = entityManagerFactory.createEntityManager(synchronizationType, emProperties);
                        propagatedPersistenceContext = new PhysicalEntityManagerWrapper(entityManager, synchronizationType);
                        tx.addTxEntityManagerMapping(entityManagerFactory, propagatedPersistenceContext);
                    } else {

                        // Check if sync type of current persistence context is compatible with persistence context being propagated
                        if (synchronizationType == SYNCHRONIZED && propagatedPersistenceContext.getSynchronizationType() == UNSYNCHRONIZED) {
                            throw new IllegalStateException(
                                    "Detected an UNSYNCHRONIZED persistence context being propagated to SYNCHRONIZED persistence context.");
                        }

                    }
                }
                delegate = propagatedPersistenceContext.getEM();

            } else {
                // Get non transactional entity manager corresponding to this wrapper from current invocation
                delegate = getNonTxEMFromCurrentInvocation();
            }

        } else {

            // EXTENDED Persistence Context

            if (extendedEntityManager == null) {
                ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
                if (componentInvocation != null) {
                    Object container = componentInvocation.getContainer();
                    if (container instanceof JavaEEContainer eeContainer) {
                        extendedEntityManager = eeContainer.lookupExtendedEntityManager(entityManagerFactory);
                    }
                }
            }

            delegate = extendedEntityManager;

        }

        LOG.log(FINE, "In EntityManagerWrapper::_getDelegate(). Logical entity manager={0}. Physical entity manager={1}",
                new Object[] { this, delegate });

        return delegate;
    }

    private JavaEETransaction getCurrentTransaction() {
        try {
            return (JavaEETransaction) transactionManager.getTransaction();
        } catch (Exception e) {
            throw new IllegalStateException("exception retrieving tx", e);
        }
    }

    private static final Class INVOCATION_PAYLOAD_KEY = EntityManagerWrapper.class; // Key used to look up payload from current invocation

    private EntityManager getNonTxEMFromCurrentInvocation() {
        // We store nonTxEM as a payload in a map from EMF to EM inside current invocation.
        // It will be closed during NonTxEntityManagerCleaner.beforePostInvoke() below

        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();
        Map<EntityManagerFactory, EntityManager> nonTxEMs = getNonTxEMsFromCurrentInvocation(currentInvocation);
        if (nonTxEMs == null) {
            nonTxEMs = new HashMap<>();
            currentInvocation.setRegistryFor(INVOCATION_PAYLOAD_KEY, nonTxEMs);
        }
        EntityManager nonTxEM = nonTxEMs.get(entityManagerFactory);
        if (nonTxEM == null) {
            // Could not find one, create new one and store it within current invocation for cleanup
            nonTxEM = entityManagerFactory.createEntityManager(synchronizationType, emProperties);
            nonTxEMs.put(entityManagerFactory, nonTxEM);
        }
        return nonTxEM;
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityManagerFactory, EntityManager> getNonTxEMsFromCurrentInvocation(ComponentInvocation currentInvocation) {
        return (Map<EntityManagerFactory, EntityManager>) currentInvocation.getRegistryFor(INVOCATION_PAYLOAD_KEY);
    }

    @Override
    public void persist(Object entity) {
        doTransactionScopedTxCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(PERSIST);
            }
            _getDelegate().persist(entity);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public <T> T merge(T entity) {
        doTransactionScopedTxCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(MERGE);
            }
            return _getDelegate().merge(entity);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public void remove(Object entity) {
        doTransactionScopedTxCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(REMOVE);
            }
            _getDelegate().remove(entity);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        T returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(FIND);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.find(entityClass, primaryKey);
            clearDetachedPersistenceContext(delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        T returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(FIND);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.find(entityClass, primaryKey, properties);
            clearDetachedPersistenceContext(delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        T returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(FIND_CLASS_OBJECT_LOCKMODETYPE_PROPERTIES);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.find(entityClass, primaryKey, lockMode, properties);
            clearDetachedPersistenceContext(delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        T returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_REFERENCE);
            }
            returnValue = _getDelegate().getReference(entityClass, primaryKey);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public void flush() {
        // tx is ALWAYS required, regardless of persistence context type.
        doTxRequiredCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(FLUSH);
            }
            _getDelegate().flush();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public StatementOrTypedQuery createQuery(String ejbqlString) {
        StatementOrTypedQuery returnValue = null;
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_QUERY);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createQuery(ejbqlString);
            returnValue = wrapStatementOrTypedQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String ejbqlString, Class<T> resultClass) {
        TypedQuery<T> returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_QUERY_STRING_CLASS);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createQuery(ejbqlString, resultClass);

            returnValue = wrapQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public Statement createQuery(CriteriaStatement<?> criteriaQuery) {
        return createStatement(criteriaQuery);
    }

    @Override
    public Statement createStatement(CriteriaStatement<?> criteriaQuery) {
        Statement returnValue = null;
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_QUERY_CRITERIA_QUERY);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createStatement(criteriaQuery);
            returnValue = wrapStatementIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public StatementOrTypedQuery createNamedQuery(String name) {
        StatementOrTypedQuery returnValue = null;
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_NAMED_QUERY);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createNamedQuery(name);
            returnValue = wrapStatementOrTypedQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        TypedQuery<T> returnValue = null;
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_NAMED_QUERY);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createNamedQuery(name, resultClass);
            returnValue = wrapQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public StatementOrTypedQuery createNativeQuery(String sqlString) {
        StatementOrTypedQuery returnValue = null;
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_NATIVE_QUERY_STRING);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createNativeQuery(sqlString);
            returnValue = wrapStatementOrTypedQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public <T> TypedQuery<T> createNativeQuery(String sqlString, Class<T> resultClass) {
        TypedQuery<T> returnValue = null;
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_NATIVE_QUERY_STRING_CLASS);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createNativeQuery(sqlString, resultClass);
            returnValue = wrapQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public StatementOrTypedQuery createNativeQuery(String sqlString, String resultSetMapping) {
        StatementOrTypedQuery returnValue = null;
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_NATIVE_QUERY_STRING_STRING);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createNativeQuery(sqlString, resultSetMapping);
            returnValue = wrapStatementOrTypedQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public void refresh(Object entity) {
        doTransactionScopedTxCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(REFRESH);
            }
            _getDelegate().refresh(entity);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        doTransactionScopedTxCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(REFRESH_OBJECT_PROPERTIES);
            }
            _getDelegate().refresh(entity, properties);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }


    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        doTransactionScopedTxCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(REFRESH_OBJECT_LOCKMODETYPE_MAP);
            }
            _getDelegate().refresh(entity, lockMode, properties);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public boolean contains(Object entity) {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CONTAINS);
            }
            EntityManager delegate = _getDelegate();
            return delegate.contains(entity);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public LockModeType getLockMode(Object o) {

        doTxRequiredCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_LOCK_MODE);
            }
            return _getDelegate().getLockMode(o);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public void setProperty(String propertyName, Object value) {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(SET_PROPERTY);
            }
            _getDelegate().setProperty(propertyName, value);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public Map<String, Object> getProperties() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_PROPERTIES);
            }
            return _getDelegate().getProperties();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public void close() {

        if (callFlowAgent.isEnabled()) {
            callFlowAgent.entityManagerMethodStart(CLOSE);
            callFlowAgent.entityManagerMethodEnd();
        }
        // close() not allowed on container-managed EMs.
        throw new IllegalStateException();
    }

    @Override
    public boolean isOpen() {

        if (callFlowAgent.isEnabled()) {
            callFlowAgent.entityManagerMethodStart(IS_OPEN);
            callFlowAgent.entityManagerMethodEnd();
        }
        // Not relevant for container-managed EMs. Just return true.
        return true;
    }

    @Override
    public EntityTransaction getTransaction() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_TRANSACTION);
            }
            return _getDelegate().getTransaction();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_ENTITY_MANAGER_FACTORY);
            }
            if (entityManagerFactory == null) {
                init();
            }

            // Spec requires to throw IllegalStateException if this em has been closed.
            // No need to perform the check here as this can not happen for managed em.
            // No need to go to delegate for this.
            return entityManagerFactory;
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_CRITERIA_BUILDER);
            }
            return _getDelegate().getCriteriaBuilder();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

    }

    @Override
    public Metamodel getMetamodel() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_METAMODEL);
            }
            return _getDelegate().getMetamodel();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(LOCK);
            }
            _getDelegate().lock(entity, lockMode);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(LOCK_LOCKMODETYPE_MAP);
            }
            _getDelegate().lock(entity, lockMode, properties);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public void clear() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CLEAR);
            }
            _getDelegate().clear();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public void detach(Object o) {

        // TODO revisit this check once Linda confirms whether it is required or not.
        doTransactionScopedTxCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(DETATCH);
            }
            _getDelegate().detach(o);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

        // tx is required so there's no need to do any non-tx cleanup
    }

    @Override
    public Object getDelegate() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_DELEGATE);
            }
            return _getDelegate();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

    }

    @Override
    public FlushModeType getFlushMode() {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_FLUSH_MODE);
            }
            return _getDelegate().getFlushMode();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(SET_FLUSH_MODE);
            }
            _getDelegate().setFlushMode(flushMode);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public void joinTransaction() {
        // Doesn't apply to the container-managed case, but all the
        // spec says is that an exception should be thrown if called
        // without a tx.
        doTxRequiredCheck();

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(JOIN_TRANSACTION);
            }
            _getDelegate().joinTransaction();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }

    }

    @Override
    public <T> T unwrap(Class<T> tClass) {

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(UNWRAP);
            }
            return _getDelegate().unwrap(tClass);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        StoredProcedureQuery returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_NAMED_STORED_PROCEDURE_QUERY);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createNamedStoredProcedureQuery(name);

            returnValue = wrapQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        StoredProcedureQuery returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_STORED_PROCEDURE_QUERY);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createStoredProcedureQuery(procedureName);

            returnValue = wrapQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        StoredProcedureQuery returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_STORED_PROCEDURE_QUERY_STRING_CLASS);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createStoredProcedureQuery(procedureName, resultClasses);

            returnValue = wrapQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        StoredProcedureQuery returnValue = null;

        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_STORED_PROCEDURE_QUERY_STRING_STRING);
            }
            EntityManager delegate = _getDelegate();
            returnValue = delegate.createStoredProcedureQuery(procedureName, resultSetMappings);

            returnValue = wrapQueryIfDetached(returnValue, delegate);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
        return returnValue;
    }

    @Override
    public boolean isJoinedToTransaction() {
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(IS_JOINED_TO_TRANSACTION);
            }
            return _getDelegate().isJoinedToTransaction();
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_ENTITY_GRAPH_CLASS);
            }
            return _getDelegate().createEntityGraph(rootType);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(CREATE_ENTITY_GRAPH_STRING);
            }
            return _getDelegate().createEntityGraph(graphName);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_ENTITY_GRAPH);
            }
            return _getDelegate().getEntityGraph(graphName);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        try {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodStart(GET_ENTITY_GRAPHS);
            }
            return _getDelegate().getEntityGraphs(entityClass);
        } finally {
            if (callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerMethodEnd();
            }
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        // Initialize the transients that were passed at ctor.
        ServiceLocator defaultServiceLocator = Globals.getDefaultHabitat();
        transactionManager = defaultServiceLocator.getService(TransactionManager.class);
        invocationManager = defaultServiceLocator.getService(InvocationManager.class);
        compEnvMgr = defaultServiceLocator.getService(ComponentEnvManager.class);
        callFlowAgent = defaultServiceLocator.getService(CallFlowAgent.class);
    }

    private boolean isDetached() {
        return getCurrentTransaction() == null && contextType != PersistenceContextType.EXTENDED;
    }

    private StoredProcedureQuery wrapQueryIfDetached(StoredProcedureQuery query, EntityManager delegate) {
        return isDetached() ? StoreProcedureQueryWrapper.createQueryWrapper(query, delegate) : query;
    }

    private <T> TypedQuery<T> wrapQueryIfDetached(TypedQuery<T> query, EntityManager delegate) {
        return isDetached() ? TypedQueryWrapper.createQueryWrapper(query, delegate) : query;
    }

    private Query wrapQueryIfDetached(Query query, EntityManager delegate) {
        return isDetached() ? QueryWrapper.createQueryWrapper(query, delegate) : query;
    }

    private Statement wrapStatementIfDetached(Statement statement, EntityManager delegate) {
        return isDetached() ? StatementWrapper.createQueryWrapper(statement, delegate) : statement;
    }

    private StatementOrTypedQuery wrapStatementOrTypedQueryIfDetached(StatementOrTypedQuery statement, EntityManager delegate) {
        return isDetached() ? StatementOrTypedQueryWrapper.createQueryWrapper(statement, delegate) : statement;
    }

    private void clearDetachedPersistenceContext(EntityManager em) {
        if (isDetached()) {
            em.clear();
        }
    }

    public static PhysicalEntityManagerWrapper getExtendedEntityManager(JavaEETransaction transaction, EntityManagerFactory factory) {
        return (PhysicalEntityManagerWrapper) transaction.getExtendedEntityManagerResource(factory);
    }

    public static PhysicalEntityManagerWrapper getTxEntityManager(JavaEETransaction transaction, EntityManagerFactory factory) {
        return (PhysicalEntityManagerWrapper) transaction.getTxEntityManagerResource(factory);

    }

    @Service
    /**
     * NonTxEMs are saved as payload in current invocation. Clean them up at end of any component invocation
     */
    public static class NonTxEMCleaner implements ComponentInvocationHandler {

        @Override
        public void beforePreInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                ComponentInvocation newInv) throws InvocationException {
        }

        @Override
        public void afterPreInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                ComponentInvocation curInv) throws InvocationException {
        }

        @Override
        public void beforePostInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                ComponentInvocation curInv) throws InvocationException {
            // Close all the NonTxEMs for current invocation
            Map<EntityManagerFactory, EntityManager> nonTxEMs = getNonTxEMsFromCurrentInvocation(curInv);

            if (nonTxEMs != null) {
                for (EntityManager nonTxEM : nonTxEMs.values()) {
                    nonTxEM.close();
                }
            }
        }

        @Override
        public void afterPostInvoke(ComponentInvocation.ComponentInvocationType invType, ComponentInvocation prevInv,
                ComponentInvocation curInv) throws InvocationException {
        }
    }

    // TODO ADD callflow!

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, FindOption... options) {
        // TODO ADD callflow
        return _getDelegate().find(entityClass, primaryKey, options);
    }

    @Override
    public <T> T find(EntityGraph<T> entityGraph, Object primaryKey, FindOption... options) {
        return _getDelegate().find(entityGraph, primaryKey, options);
    }

    @Override
    public <T> T getReference(T entity) {
        return _getDelegate().getReference(entity);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, LockOption... options) {
        _getDelegate().lock(entity, lockMode, options);

    }

    @Override
    public void refresh(Object entity, RefreshOption... options) {
        _getDelegate().refresh(entity, options);
    }

    @Override
    public void setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        _getDelegate().setCacheRetrieveMode(cacheRetrieveMode);
    }

    @Override
    public void setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        _getDelegate().setCacheStoreMode(cacheStoreMode);
    }

    @Override
    public CacheRetrieveMode getCacheRetrieveMode() {
        return _getDelegate().getCacheRetrieveMode();
    }

    @Override
    public CacheStoreMode getCacheStoreMode() {
        return _getDelegate().getCacheStoreMode();
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaSelect<T> selectQuery) {
        return _getDelegate().createQuery(selectQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(TypedQueryReference<T> reference) {
        return _getDelegate().createQuery(reference);
    }

    @Override
    public <C> void runWithConnection(ConnectionConsumer<C> action) {
        _getDelegate().runWithConnection(action);
    }

    @Override
    public <C, T> T callWithConnection(ConnectionFunction<C, T> function) {
        return _getDelegate().callWithConnection(function);
    }















    @Override
    public <T> T get(Class<T> entityClass, Object id) {
        return _getDelegate().get(entityClass, id);
    }

    @Override
    public <T> T get(Class<T> entityClass, Object id, FindOption... options) {
        return _getDelegate().get(entityClass, id, options);
    }

    @Override
    public <T> T get(EntityGraph<T> graph, Object id, FindOption... options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> getMultiple(Class<T> entityClass, List<?> ids, FindOption... options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> getMultiple(EntityGraph<T> graph, List<?> ids, FindOption... options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> findMultiple(Class<T> entityClass, List<?> ids, FindOption... options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> findMultiple(EntityGraph<T> graph, List<?> ids, FindOption... options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Statement createStatement(String qlString) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, EntityGraph<T> resultGraph) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Statement createNamedStatement(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Statement createStatement(StatementReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Statement createNativeStatement(String sqlString) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> TypedQuery<T> createNativeQuery(String sqlString, ResultSetMapping<T> resultSetMapping) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> EntityGraph<T> getEntityGraph(Class<T> rootType, String graphName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOption(Option option) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<Option> getOptions() {
        // TODO Auto-generated method stub
        return null;
    }
}
