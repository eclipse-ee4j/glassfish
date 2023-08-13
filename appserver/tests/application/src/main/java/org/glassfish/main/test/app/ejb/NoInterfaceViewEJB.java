/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.ejb;

import jakarta.ejb.Stateless;

@Stateless
public class NoInterfaceViewEJB {

    public Object getObject() {
        return getClass().getName();
    }

    public boolean getBoolean() {
        return Boolean.TRUE;
    }

    public byte getByte() {
        return Byte.MAX_VALUE;
    }

    public short getShort() {
        return Short.MAX_VALUE;
    }

    public int getInt() {
        return Integer.MAX_VALUE;
    }

    public long getLong() {
        return Long.MAX_VALUE;
    }

    public float getFloat() {
        return Float.MAX_VALUE;
    }

    public double getDouble() {
        return Double.MAX_VALUE;
    }

    public void callVoidMethod() {
        // Do nothing
    }

    void callPackagePrivateMethod() {
        // Do nothing
    }

    protected void callProtectedMethod() {
        // Do nothing
    }

    static void callNonPublicStaticMethod() {
        // Do nothing
    }
}
