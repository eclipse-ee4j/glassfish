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


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.PessimisticLockScope;
import jakarta.persistence.QueryFlushMode;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Timeout;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.Type;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Wrapper class for jakarta.persistence.TypedQuery objects returned from
 * non-transactional access of a container-managed transactional
 * EntityManager.
 *
 * @see QueryWrapper for more details about why the wrapper is needed
 */
public class TypedQueryWrapper<X> extends QueryWrapper<TypedQuery<X>> implements TypedQuery<X> {


    public static <X> TypedQuery<X> createQueryWrapper(TypedQuery<X> queryDelegate, EntityManager emDelegate) {
        return new TypedQueryWrapper<X>(queryDelegate, emDelegate);
    }

    private TypedQueryWrapper(TypedQuery<X> qDelegate, EntityManager emDelegate) {
        super(qDelegate, emDelegate);
    }


    @Override
    public TypedQuery<X> setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    public TypedQuery<X> setTimeout(Timeout timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        super.setCacheRetrieveMode(cacheRetrieveMode);
        return this;
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        super.setCacheStoreMode(cacheStoreMode);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated(since = "9", forRemoval = true)
    public X getSingleResultOrNull() {
        return (X) super.getSingleResultOrNull();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated(since = "9", forRemoval = true)
    public List<X> getResultList() {
        // If this method is called, the current instance is guarantied to be of type TypedQuery<X>
        // It is safe to cast here.
        return super.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated(since = "9", forRemoval = true)
    public X getSingleResult() {
        // If this method is called, the current instance is guarantied to be of type TypedQuery<X>
        // It is safe to cast here.
        return (X) super.getSingleResult();
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        return this;
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setFirstResult(int startPosition) {
        super.setFirstResult(startPosition);
        return this;
    }


    @Override
    public TypedQuery<X> setHint(String hintName, Object value) {
        super.setHint(hintName, value);
        return this;
    }


    @Override
    public TypedQuery<X> setParameters(Object... arguments) {
        super.setParameters(arguments);
        return this;
    }


    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        super.setParameter(param, value);
        return this;
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        super.setParameter(param, value, temporalType);
        return this;
    }


    @Override
    public TypedQuery<X> setParameter(String name, Object value) {
        super.setParameter(name, value);
        return this;
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
        super.setParameter(name, value, temporalType);
        return this;
    }


    @Override
    public TypedQuery<X> setParameter(int position, Object value) {
        super.setParameter(position, value);
        return this;
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }


    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }


    @Override
    public <P> TypedQuery<X> setParameter(String name, P value, Class<P> type) {
        super.setParameter(name, value, type);
        return this;
    }


    @Override
    public <P> TypedQuery<X> setParameter(String name, P value, Type<P> type) {
        super.setParameter(name, value, type);
        return this;
    }


    @Override
    public <P> TypedQuery<X> setParameter(int position, P value, Class<P> type) {
        super.setParameter(position, value, type);
        return this;
    }


    @Override
    public <P> TypedQuery<X> setParameter(int position, P value, Type<P> type) {
        super.setParameter(position, value, type);
        return this;
    }


    @Override
    public <P> TypedQuery<X> setConvertedParameter(String name, P value, Class<? extends AttributeConverter<P, ?>> converter) {
        super.setConvertedParameter(name, value, converter);
        return this;
    }


    @Override
    public <P> TypedQuery<X> setConvertedParameter(int position, P value, Class<? extends AttributeConverter<P, ?>> converter) {
        super.setConvertedParameter(position, value, converter);
        return this;
    }


    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return super.getParameter(name, type);
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
       return super.getParameter(position, type);
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
        super.setFlushMode(flushMode);
        return this;
    }

    @Override
    public TypedQuery<X> setQueryFlushMode(QueryFlushMode flushMode) {
        super.setQueryFlushMode(flushMode);
        return this;
    }

    @Override
    @Deprecated(since = "9", forRemoval = true)
    public TypedQuery<X> setLockMode(LockModeType lockModeType) {
        super.setLockMode(lockModeType);
        return this;
    }

    @Override
    public TypedQuery<X> setLockScope(PessimisticLockScope lockScope) {
        queryDelegate.setLockScope(lockScope);
        return this;
    }

    @Override
    public PessimisticLockScope getLockScope() {
        return queryDelegate.getLockScope();
    }

    @Override
    public TypedQuery<X> addOption(TypedQuery.Option option) {
        queryDelegate.addOption(option);
        return this;
    }

    @Override
    public Set<TypedQuery.Option> getOptions() {
        return queryDelegate.getOptions();
    }

    /**
     * Returns a count rather than entities, so unlike {@link #getResultList()}
     * there is nothing to detach afterwards.
     */
    @Override
    public long getResultCount() {
        return queryDelegate.getResultCount();
    }
}
