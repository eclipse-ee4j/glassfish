/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.common;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * This class provides an optimization for some methods in
 * java.security.AccessController.
 * @author Shing Wai Chan
 */
public final class AppservAccessController {
    private static boolean isSecMgrOff = (System.getSecurityManager() == null);

    private AppservAccessController() {
    }

    public static Object doPrivileged(PrivilegedAction action) {
        if (isSecMgrOff) {
            return action.run();
        } else {
            return AccessController.doPrivileged(action);
        }
    }

    public static Object doPrivileged(PrivilegedExceptionAction action)
             throws PrivilegedActionException {

        if (isSecMgrOff) {
            try {
                return action.run();       
            } catch(Exception e) {
                throw new PrivilegedActionException(e);
            }
        } else {
            return AccessController.doPrivileged(action);
        }
    }
}
