/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdo.spi.persistence.support.sqlstore.utility;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resources;
import org.glassfish.connectors.config.PersistenceManagerFactoryResource;
import org.glassfish.jdbc.config.JdbcResource;
import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.api.admin.config.ConfigurationUpgrade;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyVetoException;

/**
 * @author Mitesh Meswani
 */
@Service
public class PersistenceManagerFactoryResourceMigrator implements ConfigurationUpgrade, PostConstruct {
    @Inject
    Resources resources;

    public void postConstruct() {
        Collection<PersistenceManagerFactoryResource> pmfResources = resources.getResources(PersistenceManagerFactoryResource.class);
        for (final PersistenceManagerFactoryResource pmfResource : pmfResources) {
            String jdbcResourceName = pmfResource.getJdbcResourceJndiName();

            final JdbcResource jdbcResource = (JdbcResource) ConnectorsUtil.getResourceByName(resources, JdbcResource.class, jdbcResourceName);

            try {
                ConfigSupport.apply(new SingleConfigCode<Resources>() {

                    public Object run(Resources resources) throws PropertyVetoException, TransactionFailure {
                        // delete the persitence-manager-factory resource
                        resources.getResources().remove(pmfResource);

                        // create a jdbc resource which points to same connection pool and has same jndi name as pmf resource.
                        JdbcResource newResource = resources.createChild(JdbcResource.class);
                        newResource.setJndiName(pmfResource.getJndiName());
                        newResource.setDescription("Created to migrate persistence-manager-factory-resource from V2 domain");
                        newResource.setPoolName(jdbcResource.getPoolName());
                        newResource.setEnabled("true");
                        resources.getResources().add(newResource);
                        return newResource;
                    }
                }, resources);
            } catch (TransactionFailure tf) {
                Logger.getAnonymousLogger().log(Level.SEVERE,
                    "Failure while upgrading persistence-manager-factory-resource", tf);
                throw new RuntimeException(tf);

            }

        } // end of iteration
    }
}
