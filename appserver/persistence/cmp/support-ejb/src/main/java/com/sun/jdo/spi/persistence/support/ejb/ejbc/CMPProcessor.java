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

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.jdo.spi.persistence.support.sqlstore.ejb.DeploymentHelper;
import com.sun.jdo.spi.persistence.utility.logging.Logger;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.persistence.common.DatabaseConstants;
import org.glassfish.persistence.common.Java2DBProcessorHelper;

/**
 * If the application contains cmp 2.x beans process them. Check if
 * tables have to created or dropped depending on where we are called
 * in a deploy/undeploy case.
 * @author pramodg
 */
public class CMPProcessor {

    private static Logger logger = LogHelperEJBCompiler.getLogger();

    private Java2DBProcessorHelper helper = null;

    private DeploymentContext ctx;

    /**
     * Creates a new instance of CMPProcessor
     * @param ctx the deployment context object.
     */
    public CMPProcessor(DeploymentContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Create and execute the files.
     */
    public void process() {

        EjbBundleDescriptorImpl bundle = ctx.getModuleMetaData(EjbBundleDescriptorImpl.class);
        ResourceReferenceDescriptor cmpResource = bundle.getCMPResourceReference();

        // If this bundle's beans are not created by Java2DB, there is nothing to do.
        if (!DeploymentHelper.isJavaToDatabase(
                cmpResource.getSchemaGeneratorProperties())) {
            return;
        }

        helper = new Java2DBProcessorHelper(ctx);
        helper.init();

        String resourceName = cmpResource.getJndiName();
        helper.setProcessorType("CMP", bundle.getName()); // NOI18N
        helper.setJndiName(resourceName, bundle.getName());

        // If CLI options are not set, use value from the create-tables-at-deploy
        // or drop-tables-at-undeploy elements of the sun-ejb-jar.xml
        boolean userCreateTables = cmpResource.isCreateTablesAtDeploy();
        boolean createTables = helper.getCreateTables(userCreateTables);

        boolean userDropTables = cmpResource.isDropTablesAtUndeploy();

        if (logger.isLoggable(logger.FINE)) {
            logger.fine("ejb.CMPProcessor.createanddroptables", //NOI18N
                new Object[] {new Boolean(createTables), new Boolean(userDropTables)});
        }

        if (!createTables && !userDropTables) {
            // Nothing to do.
            return;
        }

        helper.setCreateTablesValue(userCreateTables, bundle.getName());
        helper.setDropTablesValue(userDropTables, bundle.getName());

        constructJdbcFileNames(bundle);
        if (logger.isLoggable(logger.FINE)) {
            logger.fine("ejb.CMPProcessor.createanddropfilenames",
                helper.getCreateJdbcFileName(bundle.getName()),
                helper.getDropJdbcFileName(bundle.getName()));
        }

        if (createTables) {
            helper.createOrDropTablesInDB(true, "CMP"); // NOI18N
        }
    }

    /**
     * Drop files on undeploy
     */
    public void clean() {
        helper = new Java2DBProcessorHelper(ctx);
        helper.init();

        helper.createOrDropTablesInDB(false, "CMP"); // NOI18N
    }

    /**
     * Construct the name of the create and
     * drop jdbc ddl files that would be
     * created. These name would be either
     * obtained from the persistence.xml file
     * (if the user has defined them) or we would
     * create default filenames
     * @param ejbBundle the ejb bundle descriptor being worked on.
     */
    private void  constructJdbcFileNames(EjbBundleDescriptorImpl ejbBundle) {
        String filePrefix = DeploymentHelper.getDDLNamePrefix(ejbBundle);

        helper.setCreateJdbcFileName(filePrefix + DatabaseConstants.CREATE_DDL_JDBC_FILE_SUFFIX,
                ejbBundle.getName());
        helper.setDropJdbcFileName(filePrefix + DatabaseConstants.DROP_DDL_JDBC_FILE_SUFFIX,
                ejbBundle.getName());
    }

}
