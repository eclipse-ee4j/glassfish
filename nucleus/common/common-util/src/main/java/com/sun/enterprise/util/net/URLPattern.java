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

package com.sun.enterprise.util.net;

public class URLPattern {

    // In Ascii table, New Line (NL) decimal value is 10
    private final static int NL = 10;
    // In Ascii table, Carriage Return (CR) decimal value is 13
    private final static int CR = 13;

    /**
     *  This method is used to check the validity of url pattern
     *  according to the spec. It is used in the following places:
     *
     *  1. in WebResourceCollection
     *  2. in ServletMapping
     *  3. in ServletFilterMapping
     *  (above three see Servlet Spec, from version 2.3 on,
     *  Secion 13.2: "Rules for Processing the Deployment Descriptor")
     *
     *  4. in jsp-property-group
     *  (see JSP.3.3: "JSP Property Groups")
     *
     *  @param urlPattern the url pattern
     *  @return false for invalid url pattern
     */
    public static boolean isValid(String urlPattern) {
        // URL Pattern should not contain New Line (NL) or
        // Carriage Return (CR)
        if (containsCRorLF(urlPattern)) {
            return false;
        }

        // Check validity for extension mapping
        if (urlPattern.startsWith("*.")) {
            if (urlPattern.indexOf('/') < 0) {
                return true;
            } else {
                return false;
            }
        }

        // check validity for path mapping
        if (urlPattern.isEmpty()) {
            return true;
        } else if (urlPattern.startsWith("/") &&
                urlPattern.indexOf("*.") < 0) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * This method is used to check whether a url pattern contains a CR(#xD) or
     * LF (#xA). According to the Servlet spec the developer must be informed
     * when it does.
     *
     * @param urlPattern
     *            the url pattern (must not be null)
     * @return true if it contains one or more CRs or LFs
     */
    public static boolean containsCRorLF(String urlPattern) {
        return (urlPattern.indexOf(NL) != -1  || urlPattern.indexOf (CR) != -1);
    }
}
