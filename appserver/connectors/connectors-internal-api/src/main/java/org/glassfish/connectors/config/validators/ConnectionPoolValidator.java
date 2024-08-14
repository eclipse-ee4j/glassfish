/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.config.validators;

import com.sun.enterprise.config.serverbeans.ResourcePool;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.glassfish.config.support.Constants;
import org.glassfish.connectors.config.ConnectorConnectionPool;

/**
 * Implementation for Connection Pool validation.
 * Following validations are done :
 * - Validation of datasource/driver classnames when resource type is not null
 * - Max pool size to be always higher than steady pool size
 * - Check if statement wrapping is on when certain features are enabled.
 *
 * @author Shalini M
 */
public class ConnectionPoolValidator
    implements ConstraintValidator<ConnectionPoolConstraint, ResourcePool> {

    protected ConnectionPoolErrorMessages poolFaults;

    public void initialize(final ConnectionPoolConstraint constraint) {
        this.poolFaults = constraint.value();
    }

    @Override
    public boolean isValid(final ResourcePool pool,
        final ConstraintValidatorContext constraintValidatorContext) {

        if (poolFaults == ConnectionPoolErrorMessages.MAX_STEADY_INVALID) {
            if (pool instanceof ConnectorConnectionPool) {
                ConnectorConnectionPool connPool = (ConnectorConnectionPool) pool;
                String maxPoolSize = connPool.getMaxPoolSize();
                String steadyPoolSize = connPool.getSteadyPoolSize();
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
        return true;
    }
}





