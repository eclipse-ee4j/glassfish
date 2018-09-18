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

import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.RetrieveDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.SQLStoreManager;
import com.sun.jdo.spi.persistence.support.sqlstore.model.ForeignFieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.constraint.ConstraintFieldDesc;

import java.util.ArrayList;

/**
 * Implements the select plan for Exist-Subqueries.
 *
 * @author Mitesh Meswani
 * @author Markus Fuchs
 */
public class CorrelatedExistSelectPlan extends CorrelatedSelectPlan {

    public CorrelatedExistSelectPlan(RetrieveDesc desc,
                                     SQLStoreManager store,
                                     ForeignFieldDesc parentField,
                                     SelectQueryPlan parentPlan) {

        super(desc, store, parentField, parentPlan);
    }

    /**
     * There are no real fields to be selected for an (NOT)EXIST query.
     * This method just adds the table for the nested select.
     * The statement for nested select is created as a side effect.
     */
    protected void processFields() {
        for (int i = 0; i < parentField.foreignFields.size(); i++) {
            LocalFieldDesc field = (LocalFieldDesc) parentField.foreignFields.get(i);
            addTable(field);
        }
    }

    /**
     * The correlated constraint joining this subquery with the parent field.
     * The joined table is added as a side-effect.
     */
    protected void doCorrelatedJoin() {
        ArrayList foreignFields = null;

        if (parentField.useJoinTable()) {
            foreignFields = parentField.assocLocalFields;
            // The join table is included in #processJoinTable
        } else {
            foreignFields = parentField.foreignFields;
        }

        ArrayList localFields = parentField.localFields;
        // Add the constraint linking the parent query with the subquery.
        for (int i = 0; i < localFields.size(); i++) {
            LocalFieldDesc la = (LocalFieldDesc) localFields.get(i);
            LocalFieldDesc fa = (LocalFieldDesc) foreignFields.get(i);

            ConstraintFieldDesc lcfd = new ConstraintFieldDesc(la, parentPlan);
            ConstraintFieldDesc fcfd = new ConstraintFieldDesc(fa, this);

            constraint.addField(lcfd);
            constraint.addField(fcfd);
            // Subqueries always join via equijoin.
            constraint.addOperation(ActionDesc.OP_EQ);
        }
    }

    protected Statement newStatement() {
        return new SelectOneStatement(store.getVendorType(), this);
    }

}
