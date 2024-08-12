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

import java.util.Map;

import org.glassfish.api.deployment.DeploymentContext;

/**
 * Processor for schema generation
 *
 * @author Mitesh Meswani
 */
public interface SchemaGenerationProcessor {

    /**
     * initialize the processor
     */
    void init(PersistenceUnitDescriptor pud, DeploymentContext deploymentContext);

    /**
     * @return overrides that will be supplied to EMF creation for schema generation
     */
    Map<String, Object> getOverridesForSchemaGeneration();

    /**
     * @return overrides that will be supplied to EMF creation for suppressing
     * schema generation
     */
    Map<String, Object> getOverridesForSuppressingSchemaGeneration();

    /**
     * @return whether ddl needs to be executed by container
     */
    boolean isContainerDDLExecutionRequired();

    /**
     * Execute create DDL statements
     */
    void executeCreateDDL();

}
