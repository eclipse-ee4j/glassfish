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

package com.sun.enterprise.universal.security;

import java.security.SecureRandom;

/**
 * Created October 28, 2010
 * @author Byron Nevins
 */
public final class SecurityUtils {

    public static String getSecureRandomHexString(int numBytes) {
        SecureRandom random = new SecureRandom();
        byte[] bb = new byte[numBytes];
        random.nextBytes(bb);
        return toHexString(bb);
    }

    /**
     * No instances allowed.
     */
    private SecurityUtils() {
    }

    static private String toHexString(byte b) {
        String s = Integer.toHexString((int) b + 128);

        if (s.length() == 1)
            s = "0" + s;

        return s;
    }

    static private String toHexString(byte[] bb) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bb)
            sb.append(toHexString(b));

        return sb.toString();
    }
}
