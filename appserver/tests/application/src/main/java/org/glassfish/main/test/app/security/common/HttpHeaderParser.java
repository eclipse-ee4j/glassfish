/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.security.common;

import com.sun.enterprise.util.Utility;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.System.Logger;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Base64;

import org.glassfish.security.common.UserNameAndPassword;

import static java.lang.System.Logger.Level.INFO;

/**
 *
 */
public final class HttpHeaderParser {

    private static final Logger LOG = System.getLogger(HttpHeaderParser.class.getName());

    private HttpHeaderParser() {

    }

    public static UserNameAndPassword parseBasicAuthorizationHeader(HttpServletRequest request)
        throws CharacterCodingException {
        final String authorization = request.getHeader("Authorization");
        LOG.log(INFO, "Received authorization: {0}", authorization);
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return null;
        }
        final int charsetIndex = authorization.indexOf("; charset=");
        final Charset authCharset = Utility.getCharsetFromHeader(authorization);
        final String base64 = charsetIndex > 0 ? authorization.substring(6, charsetIndex) : authorization.substring(6);
        byte[] bs = Base64.getDecoder().decode(base64.trim());
        final String decodedString = new String(bs, authCharset);
        final int ind = decodedString.indexOf(':');
        if (ind > 0) {
            String username = decodedString.substring(0, ind);
            String password = decodedString.substring(ind + 1);
            if (username != null && password != null) {
                return new UserNameAndPassword(username, password);
            }
        }
        return null;
    }

}
