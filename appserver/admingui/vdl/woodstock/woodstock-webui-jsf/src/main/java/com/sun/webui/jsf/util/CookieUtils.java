/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.util;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Cookie;

/**
 * Methods for working with cookies.
 */
public final class CookieUtils {

    /**
     * Cannot be instanciated.
     */
    private CookieUtils() {
    }

    /**
     * Characters not allowed to be part of a cookie name (RFC 2109).
     */
    private static final char[] BAD_COOKIE_CHARS = {
        '(', ')', '<', '>', '@', ',', ';', ':', '\\',
        '\'', '/', '[', ']', '?', '=', '{', '}', ' ', '\t'
    };

    /**
     * Gets the requested cookie name. This method will ensure invalid
     * characters are not used in the name.The corresponding
     * {@code setCookieValue} call should be used to ensure proper setting /
     * retrieval of your cookie.(NOTE: there is a JS version of these methods as
     * well.)
     *
     * @param context faces context
     * @param name cookie name
     * @return Cookie
     */
    public static Cookie getCookieValue(final FacesContext context,
            final String name) {

        String cookieName = CookieUtils.getValidCookieName(name);
        return (Cookie) context.getExternalContext().getRequestCookieMap()
                .get(cookieName);
    }

    /**
     * Sets the specified cookie name / value. This method will ensure invalid
     * characters are not used in the name. The corresponding
     * {@code getCookieValue} call should be used to ensure proper setting /
     * retrieval of your cookie. (NOTE: there is a JS version of these methods
     * as well.)
     * @param context faces context
     * @param name cookie name
     * @param value cookie value
     */
    public static void setCookieValue(final FacesContext context,
            final String name, final String value) {

        // FIXME: not quite implemented...
        String cookieName = CookieUtils.getValidCookieName(name);
        context.getExternalContext().getRequestCookieMap()
                .put(cookieName, value);
    }

    /**
     * Ensure we use a RFC 2109 compliant cookie name.
     * @param name cookie name
     * @return compliant cookie name
     */
    public static String getValidCookieName(final String name) {
        String cookieName = name;
        for (char ch : BAD_COOKIE_CHARS) {
            cookieName = cookieName.replace(ch, '_');
        }
        return cookieName;
    }
}
