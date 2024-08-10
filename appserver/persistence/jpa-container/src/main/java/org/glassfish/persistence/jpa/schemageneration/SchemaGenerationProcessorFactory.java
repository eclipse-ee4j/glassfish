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

package org.glassfish.persistence.jpa.schemageneration;

import com.sun.enterprise.deployment.PersistenceUnitDescriptor;

import org.glassfish.persistence.jpa.PersistenceUnitInfoImpl;

import static org.glassfish.persistence.jpa.schemageneration.EclipseLinkSchemaGenerationProcessor.isSupportedPersistenceProvider;

/**
 * Factory for creating SchemaGenerationProcessor
 *
 * @author Mitesh Meswani
 */
public class SchemaGenerationProcessorFactory {

    private static final String STANDARD_SCHEMA_GENERATION_PREFIX = "jakarta.persistence.schema-generation";

    /**
     * @return EclipseLink specific schema generation iff provider is EclipseLink or
     * Toplink, and user has not specified any standard JPA schema generation
     * property else return JPAStandardSchemaGenerationProcessor
     */
    public static SchemaGenerationProcessor createSchemaGenerationProcessor(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        String providerClassName = PersistenceUnitInfoImpl.getPersistenceProviderClassNameForPuDesc(persistenceUnitDescriptor);

        boolean useJPA21Processor = true;

        if (isSupportedPersistenceProvider(providerClassName)) {
            if (!containsStandardSchemaGenerationProperty(persistenceUnitDescriptor)) {
                useJPA21Processor = false;
            }
        }

        return useJPA21Processor ? new JPAStandardSchemaGenerationProcessor() : new EclipseLinkSchemaGenerationProcessor(providerClassName);
    }

    /**
     * @return true if the given <code>pud</code> contains a JPA standard property
     * for schema generation
     */
    private static boolean containsStandardSchemaGenerationProperty(PersistenceUnitDescriptor persistenceUnitDescriptor) {
        boolean containsStandardSchemaGenerationProperty = false;
        for (Object puPropertyName : persistenceUnitDescriptor.getProperties().keySet()) {
            if (puPropertyName instanceof String && String.class.cast(puPropertyName).startsWith(STANDARD_SCHEMA_GENERATION_PREFIX)) {
                containsStandardSchemaGenerationProperty = true;
                break;
            }
        }

        return containsStandardSchemaGenerationProperty;
    }

}
