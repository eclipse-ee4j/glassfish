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
 * ConstraintFieldName.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.constraint;

import com.sun.jdo.spi.persistence.support.sqlstore.ActionDesc;

/**
 */
public class ConstraintFieldName extends ConstraintField {

    public ActionDesc desc;

    public String name;

    private boolean projection;

    private final boolean prefetched;

    public ConstraintFieldName(String name,
                               ActionDesc desc) {
        super();
        this.name = name;
        this.desc = desc;
        this.prefetched = false;
    }

    public ConstraintFieldName(String name, ActionDesc desc, boolean prefetched) {
        super();
        this.name = name;
        this.desc = desc;
        // Resolve: This constructor should always be called with parameter prefetched
        // set to true. Ideally, we should have an inheritance hierachy as follows
        //              ConstraintFieldName
        //                    |
        //   ----------------------------------
        //   |                                 |
        // ProjectionFieldName         PrefetchedFieldName
        // then isPrefetched and isProjection can be turned into abstract methods
        // Till we do the cleanup, parameter prefetched will just serve as a
        // marker and its value will always be ignored.
        assert prefetched == true;
        this.prefetched = prefetched;
    }

    public boolean isProjection() {
        return projection;
    }

    public void setProjection() {
        this.projection = true;
    }

    public boolean isPrefetched() {
        return prefetched;
    }
}
