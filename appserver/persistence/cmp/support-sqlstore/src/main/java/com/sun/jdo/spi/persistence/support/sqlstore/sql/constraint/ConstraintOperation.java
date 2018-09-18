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

/*
 * ConstraintOperation.java
 *
 * Create on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.constraint;

import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

/**
 */
public class ConstraintOperation extends ConstraintNode {
    public static final int PROP_ORDERBY = 1;

    public static final int PROP_ORDERBY_DESC = 2;

    public int operation;

    public ConstraintOperation() {
        super();
    }

    public ConstraintOperation(int operation) {
        super();

        this.operation = operation;
    }


    public int hasProperty(int propertyKey) {
        // RESOLVE:  We should probably get rid of this and standardize on the
        // SqlRetrieveDesc.GetOperationInfo method.
        if (propertyKey == PROP_ORDERBY) {
            if (operation == ActionDesc.OP_ORDERBY
                    || operation == ActionDesc.OP_ORDERBY_DESC) {
                return ConstraintNode.PROP_TRUE;
            } else {
                return ConstraintNode.PROP_FALSE;
            }
        }
        return ConstraintNode.PROP_UNKNOWN;
    }
}
