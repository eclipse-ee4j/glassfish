/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.timerStandalone;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

@Stateless
@Remote({Sless.class})
@DeclareRoles({"dummy"})
@RunAs("dummy")
public class SlessEJB implements Sless
{
    @Resource private TimerService timerSvc;
    @Resource private SessionContext sc;

    private static boolean timeoutWasCalled = false;

    @RolesAllowed("javaee")
    public String hello() {
        System.out.println("In SlessEJB:hello()");
        timerSvc.createTimer(1, "timer");
        return "hello";
    }

    public boolean timeoutCalled() {
        return timeoutWasCalled;
    }

    @Timeout 
    private void timeout(Timer t) {
        System.out.println("in SlessEJB:timeout");
        sc.isCallerInRole("dummy");
        timeoutWasCalled = true;
    }
    
}
