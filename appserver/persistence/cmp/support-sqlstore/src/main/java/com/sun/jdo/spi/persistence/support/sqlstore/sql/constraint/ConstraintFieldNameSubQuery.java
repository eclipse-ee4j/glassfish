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
 * ConstraintFieldNameSubQuery.java
 *
 * Create on May 23, 2002
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.constraint;

import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

/**
 * <P>This class represents a constrint on field that is represented by
 * a subquery
 */
public class ConstraintFieldNameSubQuery extends ConstraintField {

    public ActionDesc desc;
    public String fieldName;


    public ConstraintFieldNameSubQuery(String name,
                               ActionDesc desc) {
        super();
        this.fieldName = name;
        this.desc = desc;
    }

}
