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

package com.sun.jdo.api.persistence.enhancer.util;

/**
 * Support for signalling internal implementation errors.
 */
//@olsen: added class
public class Assertion {
    static protected final void affirm(boolean condition) {
        if (!condition)
            throw new InternalError("assertion failed.");//NOI18N
    }

    static protected final void affirm(boolean condition, String msg) {
        if (!condition)
            throw new InternalError("assertion failed: " + msg);//NOI18N
    }

    static protected final void affirm(Object object) {
        if (object == null)
            throw new InternalError("assertion failed.");//NOI18N
    }

    static protected final void affirm(Object object, String msg) {
        if (object == null)
            throw new InternalError("assertion failed: " + msg);//NOI18N
    }
}
