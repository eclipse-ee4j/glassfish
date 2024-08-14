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


import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Wrapper class for jakarta.persistence.TypedQuery objects returned from
 * non-transactional access of a container-managed transactional
 * EntityManager.
 *
 * @see QueryWrapper for more details about why the wrapper is needed
 */
public class TypedQueryWrapper<X> extends QueryWrapper<TypedQuery <X> > implements TypedQuery<X> {


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
    public TypedQuery<X> setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        super.setCacheRetrieveMode(cacheRetrieveMode);
        return this;
    }

    @Override
    public TypedQuery<X> setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        super.setCacheStoreMode(cacheStoreMode);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public X getSingleResultOrNull() {
        return (X) super.getSingleResultOrNull();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<X> getResultList() {
        // If this method is called, the current instance is guarantied to be of type TypedQuery<X>
        // It is safe to cast here.
        return super.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public X getSingleResult() {
        // If this method is called, the current instance is guarantied to be of type TypedQuery<X>
        // It is safe to cast here.
        return (X) super.getSingleResult();
    }

    @Override
    public TypedQuery<X> setMaxResults(int maxResults) {
        super.setMaxResults(maxResults);
        return this;
    }

    @Override
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
    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        super.setParameter(param, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value,  TemporalType temporalType) {
       super.setParameter(param, value, temporalType);
       return this;
   }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value,  TemporalType temporalType) {
       super.setParameter(param, value, temporalType);
       return this;
   }

    @Override
    public TypedQuery<X> setParameter(String name, Object value) {
       super.setParameter(name, value);
       return this;
   }

    @Override
    public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
       super.setParameter(name, value, temporalType);
       return this;
   }

    @Override
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
    public TypedQuery<X> setParameter(int position, Date value,  TemporalType temporalType) {
       super.setParameter(position, value, temporalType);
       return this;
   }

    @Override
    public TypedQuery<X> setParameter(int position, Calendar value,  TemporalType temporalType) {
       super.setParameter(position, value, temporalType);
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
    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
        super.setFlushMode(flushMode);
        return this;
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lockModeType) {
        super.setLockMode(lockModeType);
        return this;
    }
}
