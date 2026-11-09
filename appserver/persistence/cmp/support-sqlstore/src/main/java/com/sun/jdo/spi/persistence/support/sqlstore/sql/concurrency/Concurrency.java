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
 * Concurrency.java
 *
 * Create on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.concurrency;

import com.sun.jdo.spi.persistence.support.sqlstore.SQLStateManager;
import com.sun.jdo.spi.persistence.support.sqlstore.Transaction;
import com.sun.jdo.spi.persistence.support.sqlstore.UpdateObjectDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.model.ClassDesc;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.SelectQueryPlan;
import com.sun.jdo.spi.persistence.support.sqlstore.sql.generator.UpdateQueryPlan;

/**
 */
public interface Concurrency {
    int CUSTOM = 16;

    int DB_EXPLICIT = 2;

    int DB_NATIVE = 1;

    int NONE = 0;

    int OPT_MASK = 1;

    int OPT_UNIQUE_ID = 4;

    int OPT_VERIFY = 3;

    Transaction suspend();

    void resume(Transaction t);

    void commit(UpdateObjectDesc updateDesc,
                       SQLStateManager beforeImage,
                       SQLStateManager afterImage,
                       int logReason);

    void configPersistence(ClassDesc config);

    void select(SelectQueryPlan plan);

    void update(UpdateQueryPlan plan);

    Object clone();
}
