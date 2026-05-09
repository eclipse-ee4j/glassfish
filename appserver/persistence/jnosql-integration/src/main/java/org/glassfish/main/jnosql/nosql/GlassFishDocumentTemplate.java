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
package org.glassfish.main.jnosql.nosql;

import jakarta.inject.Inject;
import jakarta.nosql.Query;
import jakarta.nosql.QueryMapper.MapperUpdateFrom;
import jakarta.nosql.TypedQuery;

import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;

import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;

/**
 *
 * @author Ondro Mihalyi
 */
public class GlassFishDocumentTemplate extends AbstractSemiStructuredTemplate implements DocumentTemplate {

    private final EntityConverter converter;
    private final DatabaseManager manager;
    private final EventPersistManager eventManager;
    private final EntitiesMetadata entities;
    private final Converters converters;

    @Inject
    GlassFishDocumentTemplate(EntityConverter converter, @Database(DOCUMENT) DatabaseManager manager,
            EventPersistManager eventManager, EntitiesMetadata entities, Converters converters) {
        this.converter = converter;
        this.manager = manager;
        this.eventManager = eventManager;
        this.entities = entities;
        this.converters = converters;
    }

    GlassFishDocumentTemplate() {
        this(null, null, null, null, null);
    }

    @Override
    protected EntityConverter converter() {
        return converter;
    }

    @Override
    protected DatabaseManager manager() {
        return manager;
    }

    @Override
    protected EventPersistManager eventManager() {
        return eventManager;
    }

    @Override
    protected EntitiesMetadata entities() {
        return entities;
    }

    @Override
    protected Converters converters() {
        return converters;
    }

    @Override
    public <T> void delete(T entity) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> void delete(Iterable<? extends T> entities) {
        // TODO Auto-generated method stub
    }

    @Override
    public <T> MapperUpdateFrom update(Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> TypedQuery<T> typedQuery(String query, Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Query query(String query) {
        // TODO Auto-generated method stub
        return null;
    }


}