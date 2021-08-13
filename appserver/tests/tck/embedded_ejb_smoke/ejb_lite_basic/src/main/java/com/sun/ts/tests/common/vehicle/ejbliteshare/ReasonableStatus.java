/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * $Id$
 */
package com.sun.ts.tests.common.vehicle.ejbliteshare;

import com.sun.javatest.Status;

/**
 * This class is used to work around javatest bugs/features: javatest Status constructor replaces all unprintable chars with one
 * single space, making any multi-line reason unreadable; javatest Status does not have an overrideable setReason method.
 */
public class ReasonableStatus extends Status {
    private String reason;

    public ReasonableStatus(int c, String r) {
        super(c, "");
        reason = r;

        // print the status reason to console, regardless of same.jvm value.
        // If it were printed inside exit() method, it will not be called when
        // same.jvm is enabled (e.g., with -Dsame.jvm=true from command line)
        System.out.println(reason);
    }

    @Override
    public String getReason() {
        return reason;
    }
}
