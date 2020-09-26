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

import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;

import javax.naming.InitialContext;

@TransactionManagement(TransactionManagementType.BEAN)
@Stateful
public class BMTSfulBean
    implements BMTOperation {

    public boolean ping() {
        return true;
    }

    public boolean lookupUserTransaction() {
        boolean result = false;
        try {
            (new InitialContext()).lookup("java:comp/UserTransaction");
            result = true;
        } catch (Exception ex) {
            System.out.println("I am a BMT bean but couldn't lookup UTx");
        }

        return result;
    }

}
