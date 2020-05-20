/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.persistence.jpa;

import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.naming.NamingException;
import jakarta.validation.ValidatorFactory;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.RootDeploymentDescriptor;


/**
 * @author Mitesh Meswani
 * This encapsulates information needed  to load or unload persistence units.
 */
public interface ProviderContainerContractInfo {

    static final String DEFAULT_DS_NAME = "jdbc/__default";

    /**
     *
     * @return a class loader that is used to load persistence entities
     * bundled in this application.
     */
    ClassLoader getClassLoader();

    /**
     *
     * @return a temp class loader that is used to load persistence entities
     * bundled in this application.
     */
    ClassLoader getTempClassloader();

    /**
     *
     * Adds ClassTransformer to underlying Application's classloader
     */
    void addTransformer(ClassTransformer transformer);


    /**
     * @return absolute path of the location where application is exploded.
     */
    String getApplicationLocation();

    /**
     * Looks up DataSource with JNDI name given by <code>dataSourceName</code>
     * @param dataSourceName
     * @return DataSource with JNDI name given by <code>dataSourceName</code>
     * @throws javax.naming.NamingException
     */
    DataSource lookupDataSource(String dataSourceName) throws NamingException;

    /**
     * Looks up Non transactional DataSource with JNDI name given by <code>dataSourceName</code>
     * @param dataSourceName
     * @return Non transactional DataSource with JNDI name given by <code>dataSourceName</code>
     * @throws NamingException
     */
    DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException;

    /**
     * get instance of ValidatorFactory for this environment
     */
    ValidatorFactory getValidatorFactory();

    /**
     * Will be called while loading an application.
     * @return true if java2DB is required false otherwise
     */
    boolean isJava2DBRequired();

    /**
     * @return DeploymentContext associated with this instance.
     */
    DeploymentContext getDeploymentContext();


    /**
     * Register the give emf with underlying container
     * @param unitName Name of correspoding PersistenceUnit
     * @param persistenceRootUri URI within application (excluding META-INF) for root of corresponding PersistenceUnit
     * @param containingBundle The bundle that contains PU for the given EMF
     * @param emf The emf that needs to be registered
     */
    void registerEMF(String unitName, String persistenceRootUri, RootDeploymentDescriptor containingBundle, EntityManagerFactory emf);

    /**
     * @return JTA DataSource override if any
     */
    String getJTADataSourceOverride();

    /**
     *
     * @return default data source name to be used if user has not defined a data source
     */
    String getDefaultDataSourceName();

    /**
     * @return true if weaving is enabled for the current environment false otherwise
     */
    boolean isWeavingEnabled();
}
