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

package com.sun.jdo.spi.persistence.support.sqlstore.sql.generator;

import com.sun.jdo.spi.persistence.support.sqlstore.RetrieveDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.SQLStoreManager;
import com.sun.jdo.spi.persistence.support.sqlstore.model.ForeignFieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;

import java.util.List;

/**
 * Implements the select plan for In-Subqueries.
 *
 * @author Markus Fuchs
 * @author Mitesh Meswani
 */
public class CorrelatedInSelectPlan extends CorrelatedSelectPlan {

    public CorrelatedInSelectPlan(RetrieveDesc desc,
                                  SQLStoreManager store,
                                  ForeignFieldDesc parentField,
                                  SelectQueryPlan parentPlan) {

        super(desc, store, parentField, parentPlan);
    }

    /**
     * Add the fields joining the subquery to the list of selected fields.
     * The joined table is added as a side-effect.
     */
    protected void processFields() {
        List subqueryFieldsToSelect;

        if (parentField.useJoinTable()) {
            subqueryFieldsToSelect = parentField.getAssocLocalFields();
        } else {
            subqueryFieldsToSelect = parentField.getForeignFields();
        }

        // Add the columns and tables to be selected in the subquery
        for (int i = 0; i < subqueryFieldsToSelect.size(); i++) {
            addColumn((LocalFieldDesc) subqueryFieldsToSelect.get(i));
        }
    }

    /**
     * No-Op. No join condition is added for correlated in selects,
     * as the queries are joined on the selected fields.
     */
    protected void doCorrelatedJoin() {}

}
