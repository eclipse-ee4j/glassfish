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

package test.ejb.timer;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.inject.Inject;

import test.beans.BeanToTestTimerUse;
import test.beans.TestApplicationScopedBean;
import test.beans.TestRequestScopedBean;

@Stateless
public class TestEJBTimerBean {
    @Inject
    BeanToTestTimerUse bean;
    @Inject
    TestApplicationScopedBean tasb;
    @Inject
    TestRequestScopedBean trsb;

    /**
     * Default constructor.
     */
    public TestEJBTimerBean() {
    }

    // We assume that the other tests would atleast take 2 seconds
    // and this timer bean would get invoked
    @SuppressWarnings("unused")
    @Schedule(second = "*/2", minute = "*", hour = "*", dayOfWeek = "*", dayOfMonth = "*", month = "*", year = "*", info = "MyTimer")
    private void scheduledTimeout(final Timer t) {
        System.out.println("@Schedule called at: " + new java.util.Date());
        System.out.println("trsb: " + trsb + " tasb " + tasb);
        if (tasb != null && trsb != null) {
            bean.setResult(true);
        }
    }
}
