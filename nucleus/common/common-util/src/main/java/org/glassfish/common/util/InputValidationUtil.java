/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.common.util;

import java.util.regex.Pattern;

/*
Util class for static methods for handling encoding of invalid string characters.
Use recommendations from Open Web Application Security Project (see here
http://www.owasp.org/index.php/)
 */
public class InputValidationUtil {

    public static final String CRLF_ENCODED_STRING_LOWER = "%0d%0a";
    public static final String CRLF_ENCODED_STRING_UPPER = "%0D%0A";
    public static final String CR_ENCODED_STRING_LOWER = "%0d";
    public static final String CR_ENCODED_STRING_UPPER = "%0D";
    public static final String CRLF_STRING = "\"\\r\\n\"";

    /**
     Validate the String for Header Injection Attack.

     @param input        String to be validate
     @return                boolean
     */
    public static boolean validateStringforCRLF (String input) {
        if (input != null && (input.contains(CRLF_ENCODED_STRING_LOWER)
                || input.contains(CRLF_ENCODED_STRING_UPPER)
                || input.contains(CR_ENCODED_STRING_UPPER)
                || input.contains(CR_ENCODED_STRING_LOWER)
                || input.contains(CRLF_STRING))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove unwanted white spaces in the URL.
     *
     * @param input        String to be stripped with whitespaces
     * @return                String
     */
    public static String removeLinearWhiteSpaces(String input) {
        if (input != null) {
            input = Pattern.compile("\\s").matcher(input).replaceAll(" ");
        }
        return input;
    }

    /**
     * Return Http Header Name after suitable validation
     *
     * @param headerName Header Name which should be validated before being set
     * @return String Header Name sanitized for CRLF attack
     */
    public static String getSafeHeaderName(String headerName) throws Exception {
        headerName = removeLinearWhiteSpaces(headerName);
        if (validateStringforCRLF(headerName)) {
            throw new Exception("Header Name invalid characters");
        }
        return headerName;
    }

    /**
     * Return Http Header Value after suitable validation
     *
     * @param headerValue Header Value which should be validated before being set
     * @return String Header Value sanitized for CRLF attack
     */
    public static String getSafeHeaderValue(String headerValue) throws Exception {
        headerValue = removeLinearWhiteSpaces(headerValue);
        if (validateStringforCRLF(headerValue)) {
            throw new Exception("Header Value invalid characters");
        }
        return headerValue;
    }

    /**
     * Return Cookie Http Header Value after suitable validation
     *
     * @param headerValue Header Value which should be validated before being set
     * @return String Header Value sanitized for CRLF attack
     */
    public static String getSafeCookieHeaderValue(String headerValue) throws Exception {
        headerValue = removeLinearWhiteSpaces(headerValue);
        if (validateStringforCRLF(headerValue)) {
            throw new Exception (" Cookie Header Value has invalid characters");
        }
        return headerValue;
    }
}
