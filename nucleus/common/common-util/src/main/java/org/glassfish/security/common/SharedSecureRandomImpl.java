/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.common;

import java.security.SecureRandom;

/**
 * An utility class that supplies an Initialized SecureRandom.
 */
public class SharedSecureRandomImpl {

    //the generator has a large period (in Sun's standard implementation, based on the 160-bit SHA1 hash function, the period is 2^160);
    private static final SecureRandom secureRandom = new SecureRandom();

    static {
        //always call java.security.SecureRandom.nextBytes(byte[])
        //immediately after creating a new instance of the PRNG.
        //This will force the PRNG to seed itself securely
        byte[] key = new byte[20];
        secureRandom.nextBytes(key);
    }

    /**
     * Can a single  java.security.SecureRandom instance be shared  safely by multiple threads ?.
     * Yes.  As far as I know.  nextBytes and setSeed are sync'd.
     */
    public static SecureRandom get() {
        return secureRandom;
    }

}
