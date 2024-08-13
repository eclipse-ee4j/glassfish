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
 * ConcurrencyCheckDirty.java
 *
 * Created on March 19, 2002
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.concurrency;

import com.sun.jdo.spi.persistence.support.sqlstore.SQLStateManager;
import com.sun.jdo.spi.persistence.support.sqlstore.UpdateObjectDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.FieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.QueryTable;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.Statement;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.UpdateQueryPlan;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import org.netbeans.modules.dbschema.ColumnElement;

/**
 */
public class ConcurrencyCheckDirty extends ConcurrencyDBNative {

    public void commit(UpdateObjectDesc updateDesc,
                       SQLStateManager beforeImage,
                       SQLStateManager afterImage,
                       int logReason) {
        this.beforeImage = beforeImage;
        this.afterImage = afterImage;
    }

    public void update(UpdateQueryPlan plan) {
        boolean debug = logger.isLoggable();

        if (debug) {
            logger.fine("sqlstore.sql.concurrency.concurrencychkdirty", beforeImage); // NOI18N
        }

        if (beforeImage != null) {
            ArrayList fields = plan.getConfig().fields;
            BitSet verifyGroupMask = prepareVerifyGroupMask(plan);

            for (int i = 0; i < 2; i++) {

                if (i == 0) {
                    fields = plan.getConfig().fields;
                } else if (i == 1) {
                    fields = plan.getConfig().hiddenFields;
                }

                if (fields == null) {
                    continue;
                }

                for (int j = 0; j < fields.size(); j++) {
                    FieldDesc f = (FieldDesc) fields.get(j);

                    if (f instanceof LocalFieldDesc) {
                        LocalFieldDesc lf = (LocalFieldDesc) f;

                        // Make sure the field is marked for concurrency check and is present
                        // Also, skip all fields that are marked as secondary tracked fields.
                        //
                        // RESOLVE: we need to fetch the fields that are not present.
                        if (((lf.sqlProperties & FieldDesc.PROP_IN_CONCURRENCY_CHECK) > 0) &&
                                ((lf.sqlProperties & FieldDesc.PROP_SECONDARY_TRACKED_FIELD) == 0) &&
                                beforeImage.getPresenceMaskBit(lf.absoluteID)) {

                            if (isFieldVerificationRequired(lf, verifyGroupMask)) {
                                Object val = null;
                                val = lf.getValue(this.beforeImage);
                                addConstraint(plan, lf, val);
                            }
                        }
                    }
                }
            }
        }

        if (debug) {
            logger.fine("sqlstore.sql.concurrency.concurrencychkdirty.exit"); // NOI18N
        }
    }

    protected BitSet prepareVerifyGroupMask(UpdateQueryPlan plan) {
        return null;
    }

    protected boolean isFieldVerificationRequired(LocalFieldDesc lf,
                                                  BitSet verifyGroupMask) {
         return true;
    }

    /**
     * Adds a comparison for local field <CODE>lf</CODE> and value <CODE>val</CODE>
     * to the corresponding statements in UpdateQueryPlan <CODE>plan</CODE>.
     */
    private static void addConstraint(UpdateQueryPlan plan, LocalFieldDesc lf, Object val) {
        for (Iterator iter = lf.getColumnElements(); iter.hasNext(); ) {
            ColumnElement c = (ColumnElement) iter.next();

            for (int i = 0; i < plan.statements.size(); i++) {
                Statement s = (Statement) plan.statements.get(i);

                for (int j = 0; j < s.tableList.size(); j++) {
                    QueryTable t = (QueryTable) s.tableList.get(j);

                    if (t.getTableDesc().getTableElement() == c.getDeclaringTable()) {
                        s.addConstraint(lf, val);
                    }
                }
            }
        }
    }

    public Object clone() {
        return new ConcurrencyCheckDirty();
    }
}






