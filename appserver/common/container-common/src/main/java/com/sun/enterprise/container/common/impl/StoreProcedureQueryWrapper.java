/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.QueryFlushMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Timeout;
import jakarta.persistence.metamodel.Type;
import jakarta.persistence.sql.ResultSetMapping;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Wrapper class for jakarta.persistence.StoredProcedyreQyery objects returned from
 * non-transactional access of a container-managed transactional
 * EntityManager.
 *
 * @see com.sun.enterprise.container.common.impl.QueryWrapper for more details about why the wrapper is needed
 */
public class StoreProcedureQueryWrapper extends QueryWrapper<StoredProcedureQuery> implements StoredProcedureQuery {


    public static StoredProcedureQuery createQueryWrapper(StoredProcedureQuery queryDelegate, EntityManager emDelegate) {
        return new StoreProcedureQueryWrapper(queryDelegate, emDelegate);
    }

    // QueryWrapper keeps its own EntityManager reference private, and the typed
    // result methods added in Jakarta Persistence 4.0 must detach their results too.
    private final EntityManager entityManagerDelegate;

    private StoreProcedureQueryWrapper(StoredProcedureQuery qDelegate, EntityManager emDelegate) {
        super(qDelegate, emDelegate);
        this.entityManagerDelegate = emDelegate;
    }

    @Override
    public StoredProcedureQuery setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        super.setCacheStoreMode(cacheStoreMode);
        return this;
    }

    @Override
    public StoredProcedureQuery setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        super.setCacheRetrieveMode(cacheRetrieveMode);
        return this;
    }

    @Override
    public StoredProcedureQuery setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    public StoredProcedureQuery setTimeout(Timeout timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    public StoredProcedureQuery setHint(String hintName, Object value) {
        super.setHint(hintName, value);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameters(Object... arguments) {
        super.setParameters(arguments);
        return this;
    }

    @Override
    public <T> StoredProcedureQuery setParameter(Parameter<T> param, T value) {
        super.setParameter(param, value);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameter(String name, Object value) {
        super.setParameter(name, value);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameter(String name, Calendar value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameter(String name, Date value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameter(int position, Object value) {
        super.setParameter(position, value);
        return this;
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public StoredProcedureQuery setParameter(int position, Calendar value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public StoredProcedureQuery setParameter(int position, Date value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }


    @Override
    public <P> StoredProcedureQuery setParameter(String name, P value, Class<P> type) {
        super.setParameter(name, value, type);
        return this;
    }


    @Override
    public <P> StoredProcedureQuery setParameter(String name, P value, Type<P> type) {
        super.setParameter(name, value, type);
        return this;
    }


    @Override
    public <P> StoredProcedureQuery setParameter(int position, P value, Class<P> type) {
        super.setParameter(position, value, type);
        return this;
    }


    @Override
    public <P> StoredProcedureQuery setParameter(int position, P value, Type<P> type) {
        super.setParameter(position, value, type);
        return this;
    }


    @Override
    public <P> StoredProcedureQuery setConvertedParameter(String name, P value, Class<? extends AttributeConverter<P, ?>> converter) {
        super.setConvertedParameter(name, value, converter);
        return this;
    }


    @Override
    public <P> StoredProcedureQuery setConvertedParameter(int position, P value, Class<? extends AttributeConverter<P, ?>> converter) {
        super.setConvertedParameter(position, value, converter);
        return this;
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public StoredProcedureQuery setFlushMode(FlushModeType flushMode) {
        super.setFlushMode(flushMode);
        return this;
    }

    @Override
    public StoredProcedureQuery setQueryFlushMode(QueryFlushMode flushMode) {
        super.setQueryFlushMode(flushMode);
        return this;
    }

    @Override
    public StoredProcedureQuery registerStoredProcedureParameter(int position, Class type, ParameterMode mode) {
        return queryDelegate.registerStoredProcedureParameter(position, type, mode);
    }

    @Override
    public StoredProcedureQuery registerStoredProcedureParameter(String parameterName, Class type, ParameterMode mode) {
        return queryDelegate.registerStoredProcedureParameter(parameterName, type, mode);
    }

    @Override
    public Object getOutputParameterValue(int position) {
        return queryDelegate.getOutputParameterValue(position);
    }

    @Override
    public Object getOutputParameterValue(String parameterName) {
        return queryDelegate.getOutputParameterValue(parameterName);
    }

    @Override
    public boolean execute() {
        return queryDelegate.execute();
    }

    @Override
    public boolean hasMoreResults() {
        return queryDelegate.hasMoreResults();
    }

    @Override
    public int getUpdateCount() {
        return queryDelegate.getUpdateCount();
    }

    @Override
    public <T> Parameter<T> registerResultParameter(Class<T> resultType) {
        return queryDelegate.registerResultParameter(resultType);
    }

    @Override
    public <T> Parameter<T> registerParameter(int position, Class<T> type, ParameterMode mode) {
        return queryDelegate.registerParameter(position, type, mode);
    }

    @Override
    public <T> Parameter<T> registerParameter(String parameterName, Class<T> type, ParameterMode mode) {
        return queryDelegate.registerParameter(parameterName, type, mode);
    }

    @Override
    public <T> Parameter<T> registerConvertedParameter(int position,
            Class<? extends AttributeConverter<T, ?>> converter, ParameterMode mode) {
        return queryDelegate.registerConvertedParameter(position, converter, mode);
    }

    @Override
    public <T> Parameter<T> registerConvertedParameter(String parameterName,
            Class<? extends AttributeConverter<T, ?>> converter, ParameterMode mode) {
        return queryDelegate.registerConvertedParameter(parameterName, converter, mode);
    }

    @Override
    public <T> T getOutputParameterValue(Parameter<T> parameter) {
        return queryDelegate.getOutputParameterValue(parameter);
    }

    @Override
    public <R> List<R> getResultList(Class<R> resultClass) {
        return detached(queryDelegate.getResultList(resultClass));
    }

    @Override
    public <R> List<R> getResultList(ResultSetMapping<R> mapping) {
        return detached(queryDelegate.getResultList(mapping));
    }

    @Override
    public <R> R getSingleResult(Class<R> resultClass) {
        return detached(queryDelegate.getSingleResult(resultClass));
    }

    @Override
    public <R> R getSingleResult(ResultSetMapping<R> mapping) {
        return detached(queryDelegate.getSingleResult(mapping));
    }

    @Override
    public <R> R getSingleResultOrNull(Class<R> resultClass) {
        return detached(queryDelegate.getSingleResultOrNull(resultClass));
    }

    @Override
    public <R> R getSingleResultOrNull(ResultSetMapping<R> mapping) {
        return detached(queryDelegate.getSingleResultOrNull(mapping));
    }

    @Override
    public void close() {
        queryDelegate.close();
    }

    @Override
    public StoredProcedureQuery addOption(StoredProcedureQuery.Option option) {
        queryDelegate.addOption(option);
        return this;
    }

    @Override
    public Set<StoredProcedureQuery.Option> getOptions() {
        return queryDelegate.getOptions();
    }

    /**
     * Detaches a result the same way {@code QueryWrapper.getResultList()} does:
     * clear the persistence context once the value has been read.
     */
    private <R> R detached(R result) {
        entityManagerDelegate.clear();
        return result;
    }
}
