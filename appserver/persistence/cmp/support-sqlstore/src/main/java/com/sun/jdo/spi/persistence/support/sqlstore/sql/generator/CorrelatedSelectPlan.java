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
import com.sun.jdo.spi.persistence.support.sqlstore.model.ClassDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.ForeignFieldDesc;

import java.util.ArrayList;

import org.netbeans.modules.dbschema.ColumnElement;

/**
 * Implements the select plan for correlated subqueries.
 *
 * @author Mitesh Meswani
 * @author Markus Fuchs
 */
public abstract class CorrelatedSelectPlan extends SelectQueryPlan {
    /** The parent plan for this subquery */
    protected SelectQueryPlan parentPlan;

    public CorrelatedSelectPlan(RetrieveDesc desc,
                                SQLStoreManager store,
                                ForeignFieldDesc parentField,
                                SelectQueryPlan parentPlan) {

        super(desc, store, null);
        this.parentField = parentField;
        this.parentPlan = parentPlan;
    }

    /**
     * The constraints for correlated subqueries are added here.
     * The constraints are:
     * <ul>
     * <li>The correlated constraint joining this subquery with the parent plan</li>
     * <li>A join constraint if the parent field uses join table</li>
     * </ul>
     */
    protected void processConstraints() {

        // Process the constraint on the stack.`
        super.processConstraints();

        doCorrelatedJoin();

        processJoinTable();

        // Process any extra statement added as the result of #addTable.
        processStatements();
    }

    /**
     * Must be implemented by the sub classes.
     */
    protected abstract void doCorrelatedJoin();

    /**
     * Enhance the select statement to include the join table if the
     * relationship is mapped via a join table.
     */
    private void processJoinTable() {

        if (parentField.useJoinTable()) {
            addQueryTables(parentField.assocForeignColumns, config);

            // Put in a join for the association table.
            // Subqueries always join via equijoin.
            addJoinConstraint(this, this,
                    parentField.assocForeignColumns,
                    parentField.foreignColumns, ActionDesc.OP_EQUIJOIN);
        }
    }

    /**
     * Adds the query tables corresponding to the columns in <code>columnList</code>.
     *
     * @param columnList List of columns.
     * @param config Class configuration corresponding to columns.
     */
    protected void addQueryTables(ArrayList columnList, ClassDesc config) {
        for (int i = 0; i < columnList.size(); i++) {
            ColumnElement col = (ColumnElement) columnList.get(i);
            addQueryTable(col.getDeclaringTable(), config);
        }
    }

}
