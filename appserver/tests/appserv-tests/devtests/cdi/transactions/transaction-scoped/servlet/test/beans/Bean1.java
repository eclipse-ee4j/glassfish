/*
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

package test.beans;

import test.servlet.TransactionScopedTestServlet;

import jakarta.annotation.PreDestroy;
import jakarta.transaction.TransactionScoped;
import java.io.Serializable;

@TransactionScoped
public class Bean1 implements Serializable {
    public String foo() {
        return this + ".foo()";
    }

    @PreDestroy
    public void preDestroy() {
        TransactionScopedTestServlet.bean1PreDestroyCalled = true;
    }
}
