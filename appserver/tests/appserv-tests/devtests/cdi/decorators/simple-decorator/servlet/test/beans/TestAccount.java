/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.math.BigDecimal;

public class TestAccount implements Account {
    BigDecimal currentBal = new BigDecimal(100);

    @Override
    public void deposit(BigDecimal amount) {
        System.out.println("TestAccount::deposit+" + amount);

        //get a bonus of 5
        currentBal = currentBal.add(amount.add(new BigDecimal(5)));
        System.out.println("new bal:" + currentBal);
    }

    @Override
    public BigDecimal getBalance() {
        System.out.println("TestAccount::getBalance");
        return currentBal;
    }

    @Override
    public String getOwner() {
        return "test user";
    }

    @Override
    public void withdraw(BigDecimal amount) {
        System.out.println("TestAccount::withdraw-" + amount);
        currentBal = currentBal.subtract(amount);
        System.out.println("new bal:" + currentBal);
    }

}
