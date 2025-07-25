/*
 * Copyright (c)  2024 Contributors to the Eclipse Foundation
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

package org.glassfish.embeddable.client;


/**
 * This class is used to share arguments as a string property between Java Agent
 * and Java Main class.
 */
public final class ApplicationClientCLIEncoding {

    private final static String COMMA_IN_ARG_PLACEHOLDER = "+-+-+-+";

    private ApplicationClientCLIEncoding() {
        // utility class
    }

    /**
     * Replaces commas in an argument value (which can confuse the ACC agent argument parsing because shells strip out
     * double-quotes) with a special sequence.
     *
     * @param string string to encode
     * @return encoded string
     */
    public static String encodeArg(String string) {
        return string.replace(",", COMMA_IN_ARG_PLACEHOLDER);
    }

    /**
     * Replaces occurrences of comma encoding with commas.
     *
     * @param string possibly encoded string
     * @return decoded string
     */
    public static String decodeArg(String string) {
        return string.replace(COMMA_IN_ARG_PLACEHOLDER, ",");
    }

}
