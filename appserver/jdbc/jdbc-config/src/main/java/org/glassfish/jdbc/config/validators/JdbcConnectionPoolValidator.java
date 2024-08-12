/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbc.config.validators;

import com.sun.enterprise.config.serverbeans.ResourcePool;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.glassfish.config.support.Constants;
import org.glassfish.connectors.config.validators.ConnectionPoolErrorMessages;
import org.glassfish.jdbc.config.JdbcConnectionPool;

/**
 * Implementation for Connection Pool validation.
 * Following validations are done :
 * - Validation of datasource/driver classnames when resource type is not null
 * - Max pool size to be always higher than steady pool size
 * - Check if statement wrapping is on when certain features are enabled.
 *
 * @author Shalini M
 */
public class JdbcConnectionPoolValidator
    implements ConstraintValidator<JdbcConnectionPoolConstraint, ResourcePool> {

    protected ConnectionPoolErrorMessages poolFaults;

    public void initialize(final JdbcConnectionPoolConstraint constraint) {
        this.poolFaults = constraint.value();
    }

    @Override
    public boolean isValid(final ResourcePool pool,
        final ConstraintValidatorContext constraintValidatorContext) {

        if(poolFaults == ConnectionPoolErrorMessages.MAX_STEADY_INVALID) {
            if(pool instanceof JdbcConnectionPool) {
                JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
                String maxPoolSize = jdbcPool.getMaxPoolSize();
                String steadyPoolSize = jdbcPool.getSteadyPoolSize();
                if(steadyPoolSize == null) {
                    steadyPoolSize = Constants.DEFAULT_STEADY_POOL_SIZE;
                }
                if (maxPoolSize == null) {
                    maxPoolSize = Constants.DEFAULT_MAX_POOL_SIZE;
                }
                if (Integer.parseInt(maxPoolSize) <
                        (Integer.parseInt(steadyPoolSize))) {
                    //max pool size fault
                    return false;
                }
            }
        }

        if(poolFaults == ConnectionPoolErrorMessages.STMT_WRAPPING_DISABLED) {
            if(pool instanceof JdbcConnectionPool) {
                JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
                String stmtCacheSize = jdbcPool.getStatementCacheSize();
                String stmtLeakTimeout = jdbcPool.getStatementLeakTimeoutInSeconds();
                if (jdbcPool.getSqlTraceListeners() != null) {
                    if (!Boolean.valueOf(jdbcPool.getWrapJdbcObjects())) {
                        return false;
                    }
                }
                if (stmtCacheSize != null && Integer.parseInt(stmtCacheSize) != 0) {
                    if (!Boolean.valueOf(jdbcPool.getWrapJdbcObjects())) {
                        return false;
                    }
                }
                if (stmtLeakTimeout != null && Integer.parseInt(stmtLeakTimeout) != 0) {
                    if (!Boolean.parseBoolean(jdbcPool.getWrapJdbcObjects())) {
                        return false;
                    }
                }
                if (Boolean.valueOf(jdbcPool.getStatementLeakReclaim())) {
                    if (!Boolean.valueOf(jdbcPool.getWrapJdbcObjects())) {
                        return false;
                    }
                }
            }
        }

        if(poolFaults == ConnectionPoolErrorMessages.TABLE_NAME_MANDATORY){
            if(pool instanceof JdbcConnectionPool){
                JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
                if (Boolean.valueOf(jdbcPool.getIsConnectionValidationRequired())) {
                    if ("table".equals(jdbcPool.getConnectionValidationMethod())) {
                        if(jdbcPool.getValidationTableName() == null || jdbcPool.getValidationTableName().equals("")){
                            return false;
                        }
                    }
                }
            }
        }

        if(poolFaults == ConnectionPoolErrorMessages.CUSTOM_VALIDATION_CLASS_NAME_MANDATORY){
            if(pool instanceof JdbcConnectionPool){
                JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
                if (Boolean.valueOf(jdbcPool.getIsConnectionValidationRequired())) {
                    if ("custom-validation".equals(jdbcPool.getConnectionValidationMethod())) {
                        if(jdbcPool.getValidationClassname() == null || jdbcPool.getValidationClassname().equals("")){
                            return false;
                        }
                    }
                }
            }
        }

        if (poolFaults == ConnectionPoolErrorMessages.RES_TYPE_MANDATORY) {
            if (pool instanceof JdbcConnectionPool) {
                JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
                String resType = jdbcPool.getResType();
                String dsClassName = jdbcPool.getDatasourceClassname();
                String driverClassName = jdbcPool.getDriverClassname();
                if (resType == null) {
                    //One of datasource/driver classnames must be provided.
                    if ((dsClassName == null || dsClassName.equals("")) &&
                            (driverClassName == null || driverClassName.equals(""))) {
                        return false;
                    } else {
                        //Check if both are provided and if so, return false
                        if (dsClassName != null && driverClassName != null) {
                            return false;
                        }
                    }
                } else if (resType.equals("javax.sql.DataSource") ||
                        resType.equals("javax.sql.ConnectionPoolDataSource") ||
                        resType.equals("javax.sql.XADataSource")) {
                    //Then datasourceclassname cannot be empty
                    if (dsClassName == null || dsClassName.equals("")) {
                        return false;
                    }
                } else if (resType.equals("java.sql.Driver")) {
                    //Then driver classname cannot be empty
                    if (driverClassName == null || driverClassName.equals("")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}





