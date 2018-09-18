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

package com.sun.enterprise.tools.verifier.persistence;

import com.sun.enterprise.deployment.PersistenceUnitDescriptor;

import javax.sql.DataSource;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.EntityManagerFactory;
import javax.naming.NamingException;
import javax.validation.ValidatorFactory;

import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.persistence.jpa.PersistenceUnitInfoImpl;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.persistence.jpa.ProviderContainerContractInfoBase;

/**
 * This class implements {@link javax.persistence.spi.PersistenceUnitInfo}
 * It inherits most of the implementation from its super class, except the
 * implementation that depends on runtime environment. See the details of methods
 * overridden in this class.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class AVKPersistenceUnitInfoImpl extends PersistenceUnitInfoImpl
{
    public AVKPersistenceUnitInfoImpl(
            PersistenceUnitDescriptor persistenceUnitDescriptor,
            final String applicationLocation,
            final InstrumentableClassLoader classLoader) {
        super(persistenceUnitDescriptor, new ProviderContainerContractInfoBase(null) {
            public ClassLoader getClassLoader()
            {
                return (ClassLoader)classLoader;
            }

            public ClassLoader getTempClassloader()
            {
                // EclipseLink has started to use this even if we are just validating the PU.
                // See issue 15112 for a test case.
                return (ClassLoader)classLoader;
            }

            public void addTransformer(ClassTransformer transformer)
            {
                // NOOP
            }

            public String getApplicationLocation()
            {
                return applicationLocation;
            }

            public DataSource lookupDataSource(String dataSourceName) throws NamingException
            {
                return null;
            }

            public DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException
            {
                return null;
            }

            public ValidatorFactory getValidatorFactory() {
                // TODO: Need to implement this correctly.
                return null;
            }

            public DeploymentContext getDeploymentContext()
            {
                return null;
            }

            public boolean isJava2DBRequired()
            {
                return false;
            }

            public void registerEMF(String unitName, String persistenceRootUri,
                                    RootDeploymentDescriptor containingBundle,
                                    EntityManagerFactory emf)
            {
                // NOOP
            }

            @Override
            public String getJTADataSourceOverride() {
                return null;
            }
        }
        );
    }

}
