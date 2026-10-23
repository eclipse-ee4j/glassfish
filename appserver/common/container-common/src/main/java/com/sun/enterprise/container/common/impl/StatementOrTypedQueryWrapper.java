/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Parameter;
import jakarta.persistence.QueryFlushMode;
import jakarta.persistence.Statement;
import jakarta.persistence.StatementOrTypedQuery;
import jakarta.persistence.Timeout;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.Type;
import jakarta.persistence.sql.ResultSetMapping;

/**
 * Wrapper class for jakarta.persistence.StatementOrTypedQuery objects returned from
 * non-transactional access of a container-managed transactional EntityManager.
 * <p>
 * Executing it clears the persistence context afterwards, like {@link QueryWrapper}, so the returned
 * entities are detached. The typed queries derived from it are wrapped as well, so that results obtained
 * through them are detached too.
 *
 * @see QueryWrapper for more details about why the wrapper is needed
 */
public class StatementOrTypedQueryWrapper extends QueryWrapper<StatementOrTypedQuery> implements StatementOrTypedQuery {

    public static StatementOrTypedQuery createQueryWrapper(StatementOrTypedQuery queryDelegate, EntityManager emDelegate) {
        return new StatementOrTypedQueryWrapper(queryDelegate, emDelegate);
    }

    private StatementOrTypedQueryWrapper(StatementOrTypedQuery qDelegate, EntityManager emDelegate) {
        super(qDelegate, emDelegate);
    }

    /**
     * Not wrapped: a statement returns an update count, not entities, so there is nothing to detach,
     * and executing it requires a transaction, which a non-transactional access does not have.
     */
    @Override
    public Statement asStatement() {
        return queryDelegate.asStatement();
    }

    @Override
    public <R> TypedQuery<R> ofType(Class<R> resultType) {
        return TypedQueryWrapper.createQueryWrapper(queryDelegate.ofType(resultType), entityManagerDelegate);
    }

    @Override
    public <R> TypedQuery<R> withEntityGraph(EntityGraph<R> graph) {
        return TypedQueryWrapper.createQueryWrapper(queryDelegate.withEntityGraph(graph), entityManagerDelegate);
    }

    @Override
    public <R> TypedQuery<R> withResultSetMapping(ResultSetMapping<R> mapping) {
        return TypedQueryWrapper.createQueryWrapper(queryDelegate.withResultSetMapping(mapping), entityManagerDelegate);
    }

    @Override
    public StatementOrTypedQuery setHint(String hintName, Object value) {
        super.setHint(hintName, value);
        return this;
    }

    @Override
    public <T> StatementOrTypedQuery setParameter(Parameter<T> param, T value) {
        super.setParameter(param, value);
        return this;
    }

    @Override
    public StatementOrTypedQuery setParameter(String name, Object value) {
        super.setParameter(name, value);
        return this;
    }

    @Override
    public <P> StatementOrTypedQuery setParameter(String name, P value, Class<P> type) {
        super.setParameter(name, value, type);
        return this;
    }

    @Override
    public <P> StatementOrTypedQuery setParameter(String name, P value, Type<P> type) {
        super.setParameter(name, value, type);
        return this;
    }

    @Override
    public <P> StatementOrTypedQuery setConvertedParameter(String name, P value,
            Class<? extends AttributeConverter<P, ?>> converter) {
        super.setConvertedParameter(name, value, converter);
        return this;
    }

    @Override
    public StatementOrTypedQuery setParameter(int position, Object value) {
        super.setParameter(position, value);
        return this;
    }

    @Override
    public <P> StatementOrTypedQuery setParameter(int position, P value, Class<P> type) {
        super.setParameter(position, value, type);
        return this;
    }

    @Override
    public <P> StatementOrTypedQuery setParameter(int position, P value, Type<P> type) {
        super.setParameter(position, value, type);
        return this;
    }

    @Override
    public <P> StatementOrTypedQuery setConvertedParameter(int position, P value,
            Class<? extends AttributeConverter<P, ?>> converter) {
        super.setConvertedParameter(position, value, converter);
        return this;
    }

    @Override
    public StatementOrTypedQuery setParameters(Object... arguments) {
        super.setParameters(arguments);
        return this;
    }

    @Override
    public StatementOrTypedQuery setQueryFlushMode(QueryFlushMode flushMode) {
        super.setQueryFlushMode(flushMode);
        return this;
    }

    @Override
    public StatementOrTypedQuery setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    @Override
    public StatementOrTypedQuery setTimeout(Timeout timeout) {
        super.setTimeout(timeout);
        return this;
    }
}
