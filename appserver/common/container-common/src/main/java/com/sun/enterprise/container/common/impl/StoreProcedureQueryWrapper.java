/*
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


import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TemporalType;

import java.util.Calendar;
import java.util.Date;

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

    private StoreProcedureQueryWrapper(StoredProcedureQuery qDelegate, EntityManager emDelegate) {
        super(qDelegate, emDelegate);
    }

    @Override
    public StoredProcedureQuery setHint(String hintName, Object value) {
        super.setHint(hintName, value);
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
    public StoredProcedureQuery setParameter(int position, Calendar value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public StoredProcedureQuery setParameter(int position, Date value, TemporalType temporalType) {
        super.setParameter(position, value, temporalType);
        return this;
    }

    @Override
    public StoredProcedureQuery setFlushMode(FlushModeType flushMode) {
        super.setFlushMode(flushMode);
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

}
