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

import java.io.CharArrayReader;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.deployment.DeploymentContext;

/**
 * Schema generation processor while using standard JPA based schema generation
 *
 * @author Mitesh Meswani
 */
public class JPAStandardSchemaGenerationProcessor implements SchemaGenerationProcessor {
    private static final String SCHEMA_GENERATION_DATABASE_ACTION_PROPERTY = "jakarta.persistence.schema-generation.database.action";
    private static final String SCHEMA_GENERATION_SCRIPTS_ACTION_PROPERTY = "jakarta.persistence.schema-generation.scripts.action";
    private static final String SQL_LOAD_SCRIPT_SOURCE = "jakarta.persistence.sql_load-script-source";

    private static final String SCHEMA_GENERATION_ACTION_NONE = "none";

    @Override
    public void init(PersistenceUnitDescriptor pud, DeploymentContext deploymentContext) {
        // Nothing to init
    }

    @Override
    public Map<String, Object> getOverridesForSchemaGeneration() {
        // No override is needed now. When we wire in taking schema generation overrides
        // from deploy CLI, this method will return corresponding overrides.
        return null;
    }

    @Override
    public Map<String, Object> getOverridesForSuppressingSchemaGeneration() {
        Map<String, Object> overrides = new HashMap<>();

        overrides.put(SCHEMA_GENERATION_DATABASE_ACTION_PROPERTY, SCHEMA_GENERATION_ACTION_NONE); // suppress database action
        overrides.put(SCHEMA_GENERATION_SCRIPTS_ACTION_PROPERTY, SCHEMA_GENERATION_ACTION_NONE); // suppress script action
        overrides.put(SQL_LOAD_SCRIPT_SOURCE, new CharArrayReader(new char[0])); // suppress execution of load scripts

        return overrides;
    }

    @Override
    public boolean isContainerDDLExecutionRequired() {
        // DDL execution is done by JPA provider.
        return false;
    }

    @Override
    public void executeCreateDDL() {
        // We should never reach here as this processor returns false for
        // isContainerDDLExecutionRequired()
        throw new UnsupportedOperationException();
    }
}
