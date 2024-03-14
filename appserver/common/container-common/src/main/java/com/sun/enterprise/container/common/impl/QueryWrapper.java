/*
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

import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.container.common.spi.util.EntityManagerQueryMethod;
import com.sun.enterprise.container.common.impl.util.DummyCallFlowAgentImpl;

import jakarta.persistence.*;
import java.util.*;

/**
 * Wrapper class for jakarta.persistence.Query objects returned from
 * non-transactional access of a container-managed transactional
 * EntityManager.  Proxying the Query object allows us to clear persistence
 * context after execution to allow for returned objects to be detached
 *
 */
public class QueryWrapper <T extends Query> implements Query {

    private transient CallFlowAgent callFlowAgent;

    // Holds current query/em delegates.
    protected T queryDelegate;
    private EntityManager entityManagerDelegate;


    public static Query createQueryWrapper(Query queryDelegate, EntityManager emDelegate) {
        return new QueryWrapper<Query>(queryDelegate, emDelegate);
    }


    protected QueryWrapper(T qDelegate, EntityManager emDelegate)
    {
        queryDelegate = qDelegate;
        entityManagerDelegate = emDelegate;
        callFlowAgent = new DummyCallFlowAgentImpl();    //TODO get it from ContainerUtil
    }


    @Override
    public List getResultList() {
        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_RESULT_LIST);
            }
            List retVal =  queryDelegate.getResultList();
            entityManagerDelegate.clear();
            return retVal;
        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Object getSingleResult() {
        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_SINGLE_RESULT);
            }
            Object retVal =  queryDelegate.getSingleResult();
            entityManagerDelegate.clear();
            return retVal;
        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public int executeUpdate() {
        if(callFlowAgent.isEnabled()) {
            callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.EXECUTE_UPDATE);
            callFlowAgent.entityManagerQueryEnd();
        }
        throw new TransactionRequiredException("executeUpdate is not supported for a Query object obtained through non-transactional access of a container-managed transactional EntityManager");
    }

    @Override
    public Query setMaxResults(int maxResults) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_MAX_RESULTS);
            }
            if( maxResults < 0 ) {
                throw new IllegalArgumentException("maxResult cannot be negative");
            }

            queryDelegate.setMaxResults(maxResults);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public int getMaxResults() {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_MAX_RESULTS);
            }

            return queryDelegate.getMaxResults();

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Query setFirstResult(int startPosition) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_FIRST_RESULT);
            }
            if( startPosition < 0 ) {
                throw new IllegalArgumentException
                        ("startPosition cannot be negative");
            }

            queryDelegate.setFirstResult(startPosition);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public int getFirstResult() {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_FIRST_RESULT);
            }

            return queryDelegate.getFirstResult();

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Query setHint(String hintName, Object value) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_HINT);
            }
            queryDelegate.setHint(hintName, value);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public Map<String, Object> getHints() {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_HINTS);
            }

            return queryDelegate.getHints();

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public <T> Query setParameter(Parameter<T> param, T value) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_PARAMETER_OBJECT);
            }
            queryDelegate.setParameter(param, value);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public Query setParameter(Parameter<Date> param, Date value,  TemporalType temporalType) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_PARAMETER_DATE_TEMPORAL_TYPE);
            }
            queryDelegate.setParameter(param, value, temporalType);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public Query setParameter(Parameter<Calendar> param, Calendar value,  TemporalType temporalType) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_PARAMETER_CALENDAR_TEMPORAL_TYPE);
            }
            queryDelegate.setParameter(param, value, temporalType);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }



    @Override
    public Query setParameter(String name, Object value) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_STRING_OBJECT);
            }
            queryDelegate.setParameter(name, value);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public Query setParameter(String name, Date value,
                              TemporalType temporalType) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_STRING_DATE_TEMPORAL_TYPE);
            }
            queryDelegate.setParameter(name, value, temporalType);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
        return this;
    }

    @Override
    public Query setParameter(String name, Calendar value,
                              TemporalType temporalType) {
        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_STRING_CALENDAR_TEMPORAL_TYPE);
            }
            queryDelegate.setParameter(name, value, temporalType);
        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
        return this;
    }

    @Override
    public Query setParameter(int position, Object value) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_INT_OBJECT);
            }
        queryDelegate.setParameter(position, value);
        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public Query setParameter(int position, Date value,
                              TemporalType temporalType) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_INT_DATE_TEMPORAL_TYPE);
            }
            queryDelegate.setParameter(position, value, temporalType);
        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public Query setParameter(int position, Calendar value,
                              TemporalType temporalType) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_PARAMETER_INT_CALENDAR_TEMPORAL_TYPE);
            }
            queryDelegate.setParameter(position, value, temporalType);
        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public Set<Parameter<?>> getParameters() {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETERS);
            }

            return queryDelegate.getParameters();

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Parameter<?> getParameter(String name) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETER_NAME);
            }

            return queryDelegate.getParameter(name);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETER_NAME_TYPE);
            }

            return queryDelegate.getParameter(name, type);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Parameter<?> getParameter(int position) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETER_POSITION);
            }

            return queryDelegate.getParameter(position);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type)  {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETER_POSITION_CLASS);
            }

            return queryDelegate.getParameter(position, type);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public boolean isBound(Parameter<?> param) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.IS_BOUND_PARAMETER);
            }

            return queryDelegate.isBound(param);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETER_VALUE_PARAMETER);
            }

            return queryDelegate.getParameterValue(param);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Object getParameterValue(String name) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETER_VALUE_STRING);
            }

            return queryDelegate.getParameterValue(name);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Object getParameterValue(int position) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_PARAMETER_VALUE_INT);
            }

            return queryDelegate.getParameterValue(position);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Query setFlushMode(FlushModeType flushMode) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_FLUSH_MODE);
            }
            queryDelegate.setFlushMode(flushMode);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public FlushModeType getFlushMode() {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_FLUSH_MODE);
            }

            return queryDelegate.getFlushMode();

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public Query setLockMode(LockModeType lockModeType) {
        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.SET_LOCK_MODE);
            }
            queryDelegate.setLockMode(lockModeType);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }

        return this;
    }

    @Override
    public LockModeType getLockMode() {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.GET_LOCK_MODE);
            }

            return queryDelegate.getLockMode();

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    @Override
    public <T> T unwrap(Class<T> tClass) {

        try {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryStart(EntityManagerQueryMethod.UNWRAP);
            }

            return queryDelegate.unwrap(tClass);

        } finally {
            if(callFlowAgent.isEnabled()) {
                callFlowAgent.entityManagerQueryEnd();
            }
        }
    }

    // TODO ADD callflow

    @Override
    public Object getSingleResultOrNull() {
        return queryDelegate.getSingleResult();
    }


    @Override
    public Query setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        return queryDelegate.setCacheRetrieveMode(cacheRetrieveMode);
    }


    @Override
    public Query setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        return queryDelegate.setCacheStoreMode(cacheStoreMode);
    }


    @Override
    public CacheRetrieveMode getCacheRetrieveMode() {
        return queryDelegate.getCacheRetrieveMode();
    }


    @Override
    public CacheStoreMode getCacheStoreMode() {
        return queryDelegate.getCacheStoreMode();
    }


    @Override
    public Query setTimeout(Integer timeout) {
        return queryDelegate.setTimeout(timeout);
    }


    @Override
    public Integer getTimeout() {
        return queryDelegate.getTimeout();
    }


}
