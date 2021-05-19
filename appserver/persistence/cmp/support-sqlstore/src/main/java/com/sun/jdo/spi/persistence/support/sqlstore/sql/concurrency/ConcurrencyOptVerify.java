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
 * ConcurrencyOptVerify.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.concurrency;

import com.sun.jdo.spi.persistence.support.sqlstore.model.FieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.QueryPlan;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.UpdateQueryPlan;

import java.util.ArrayList;
import java.util.BitSet;

/**
 */
public class ConcurrencyOptVerify extends ConcurrencyCheckDirty {

    /**
     * Find all the local fields that have been updated
     * and use their concurrencyGroup to set the verifyGroupMask.
     */
    protected BitSet prepareVerifyGroupMask(UpdateQueryPlan plan) {
        ArrayList fields;
        BitSet verifyGroupMask = new BitSet();
        int action = plan.getAction();

        for (int i = 0; i <= 1; i++) {
            if (i == 0) {
                fields = plan.getConfig().fields;
            } else {
                fields = plan.getConfig().hiddenFields;
            }

            if (fields == null) {
                continue;
            }

            for (int j = 0; j < fields.size(); j++) {
                FieldDesc f = (FieldDesc) fields.get(j);

                if ((f instanceof LocalFieldDesc) &&
                        (f.sqlProperties & FieldDesc.PROP_IN_CONCURRENCY_CHECK) > 0) {

                    // In the case of a deleted instance with no modified fields
                    // we use the present fields in the before image to perform
                    // the concurrency check.
                    if (afterImage.getSetMaskBit(f.absoluteID) ||
                            ((action == QueryPlan.ACT_DELETE) &&
                            beforeImage.getPresenceMaskBit(f.absoluteID))) {
                        if (f.concurrencyGroup != -1) {
                            verifyGroupMask.set(f.concurrencyGroup);
                        }
                    }
                }
            }
        }

        return verifyGroupMask;
    }

    protected boolean isFieldVerificationRequired(LocalFieldDesc lf,
                                                  BitSet verifyGroupMask) {
        boolean fieldVerificationRequired = true;

        if (lf.concurrencyGroup == -1) {
            if (!afterImage.getSetMaskBit(lf.absoluteID)) {
                fieldVerificationRequired = false;
            }
        } else {
            if (!verifyGroupMask.get(lf.concurrencyGroup)) {
                fieldVerificationRequired = false;
            }
        }

        return fieldVerificationRequired;
    }

    public Object clone() {
        return new ConcurrencyOptVerify();
    }
}






