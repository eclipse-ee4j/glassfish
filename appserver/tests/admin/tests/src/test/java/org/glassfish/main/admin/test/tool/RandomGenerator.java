/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test.tool;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author David Matejcek
 */
public final class RandomGenerator {

    private RandomGenerator() {
        // hidden
    }

    public static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        // backslash and square braces are not valid for some values.
        return new BigInteger(130, random).toString(16)/*.replaceAll("[\\[\\]\\\\]", "_")*/;
    }

    public static int generateRandomNumber() {
        Random r = new Random();
        return Math.abs(r.nextInt()) + 1;
    }

    public static int generateRandomNumber(int max) {
        Random r = new Random();
        return Math.abs(r.nextInt(max - 1)) + 1;
    }
}
