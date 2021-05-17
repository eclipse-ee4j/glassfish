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

package com.acme.ejb32.timer.getalltimers;

import jakarta.ejb.*;
import jakarta.annotation.Resource;

import java.util.Set;
import java.util.Collection;
import java.util.HashSet;

@Stateless
public class StlesNonTimeoutEJB implements StlesNonTimeout {

    @Resource
    private TimerService timerSvc;

    @EJB(lookup = "java:module/SingletonTimeoutEJB")
    SingletonTimeoutEJB singletonTimeoutEJB;

    static Set<String> expected_infos = new HashSet<String>();
    private static Set<String> errors = new HashSet<String>();

    /*
     * this includes "Sglt.timeout" and "Sglt.timeout.nonpersist" because
     * programmatic timers of Singleton are tested here.
     * It doesn't include "Stles.timeout.cancel" or "Stles.timeout.nonpersist"
     */
    static {
        expected_infos.add("Stles.schedule.anno");
        expected_infos.add("Stles.schedule.anno.nonpersist");
        expected_infos.add("Sglt.schedule.anno");
        expected_infos.add("Sglt.schedule.anno.nonpersist");
        expected_infos.add("Sglt.timeout");
        expected_infos.add("Sglt.timeout.nonpersist");
    }

    public void verifyAllTimers() {
        singletonTimeoutEJB.createTimerForTimeout();

        try {
            // waiting for creation
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new EJBException(e);
        }
        Collection<Timer> ts = timerSvc.getAllTimers();
        for(Timer t : ts) {
            String info = "" + t.getInfo();
            if (!expected_infos.contains(info)) {
                errors.add(info);
            }
        }

        if (ts.size() != expected_infos.size()) {
            printTimerInfos(ts);
            throw new EJBException("timerSvc.getAllTimers().size() = "
                    + ts.size() + " but we expect " + expected_infos.size());
        }

        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String e : errors) {
                sb.append("" + e).append(", ");
            }
            throw new EJBException("Timers SHOULD NOT found for infos: " + sb.toString() );
        }
    }

    static void printTimerInfos(Collection<Timer> ts){
        StringBuffer sb = new StringBuffer("<");
        for(Timer t:ts) {
            sb.append(t.getInfo().toString()+", ");
        }
        sb.append(">");
        System.out.println(sb);
    }
}
