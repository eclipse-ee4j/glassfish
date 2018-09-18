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
 * ConstraintValue.java
 *
 * Create on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.constraint;

import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;


/**
 */
public class ConstraintValue extends ConstraintNode {
    private Object value;

    /** The local field to which this value constraint is bound. Please note
     * that we have this information only for constraints that corresponds to simple
     * expressions ( like <field> <op> <value | param> ) in query filter.
     * Do not rely on this information to be always present for any optimizations.
     */    
    private LocalFieldDesc localField;

    public ConstraintValue(Object value, LocalFieldDesc localField) {
        super();

        this.value = value;
        this.localField = localField;
    }
    
    public Object getValue() {
        return value;
    }
    
    public LocalFieldDesc getLocalField() {
        return localField;
    }
}
