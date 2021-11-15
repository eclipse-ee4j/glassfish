/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.concurrency.spec.ContextService.tx;

import java.io.Serializable;
import java.sql.Connection;

public interface TestWorkInterface extends Runnable, Serializable {
    void doSomeWork();
    String getResult();
    void setConnection(Connection conn);
    void setSQLTemplate(String sqlTemplate);
    void needBeginTx(boolean beginTx);
    void needCommit(boolean commit);
    void needRollback(boolean rollback);
    void setUserName(String name);
    void setPassword(String pass);
}
