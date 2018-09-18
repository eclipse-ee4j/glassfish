/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.devtest;

import javax.ejb.Stateful;
import javax.naming.InitialContext;

@Stateful
public class CMTSfulBean
    implements CMTOperation {

    public boolean ping() {
        return true;
    }

    public boolean lookupUserTransaction() {
        boolean result = false;
        try {
            InitialContext ctx = new InitialContext();
            Object obj = ctx.lookup("java:comp/UserTransaction");
            System.out.println("CMT  Also got UT");
        } catch (Exception ex) {
            result = true;
            System.out.println("**CMT Got expected exception");
        }

        return result;
    }

}
