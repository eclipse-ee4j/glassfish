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

package com.oracle.hk2.devtest.cdi.ejb1.scoped;

import jakarta.annotation.ManagedBean;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * This is a CDI ApplicationScoped (a NormalScope) class that will be injected
 * into an HK2 service
 *
 * @author jwells
 *
 */
@ApplicationScoped
@ManagedBean
public class CountingApplicationScopedCDIService {
    private static int constructedCount;  // static to be class wide

    private int methodCalledCount;  // not static, to be instance wide

    public CountingApplicationScopedCDIService() {
        synchronized (CountingApplicationScopedCDIService.class) {
            constructedCount++;
        }
    }

    public int getConstructedCount() {
        synchronized (CountingApplicationScopedCDIService.class) {
            return constructedCount;
        }
    }

    public int getNumberOfTimesMethodCalled() {
        return ++methodCalledCount;
    }

}
