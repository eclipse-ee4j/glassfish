/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
 * This class provides an optimization for some methods in java.security.AccessController.
 *
 * @author Shing Wai Chan
 */
public final class AppservAccessController {
    private static final boolean SECURITY_MGR_DISABLED = System.getSecurityManager() == null;

    private AppservAccessController() {
    }


    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        if (SECURITY_MGR_DISABLED) {
            return action.run();
        }
        return AccessController.doPrivileged(action);
    }


    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        if (SECURITY_MGR_DISABLED) {
            try {
                return action.run();
            } catch (Exception e) {
                throw new PrivilegedActionException(e);
            }
        }
        return AccessController.doPrivileged(action);
    }
}
