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

package org.glassfish.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;

import org.glassfish.security.common.UserNameAndPassword;

import static java.lang.System.Logger.Level.DEBUG;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * HTTP helper class for parsing HTTP headers, content types, reading responses.
 */
public final class HttpParser {

    private static final Logger LOG = System.getLogger(HttpParser.class.getName());

    private HttpParser() {
    }

    /**
     * Reads response error input stream from the connection to a String value.
     * Respects charset from content type header, if not set, uses <code>UTF-8</code>.
     *
     * @param connection
     * @return String parsed from {@link HttpURLConnection#getErrorStream()}
     * @throws IOException
     */
    public static String readResponseErrorStream(HttpURLConnection connection) throws IOException {
        Charset charset = getCharsetFromHeader(connection.getContentType());
        try (InputStream in = connection.getErrorStream()) {
            return new String(in.readAllBytes(), charset);
        }
    }

    /**
     * Reads response input stream from the connection to a String value.
     * Respects charset from content type header, if not set, uses <code>UTF-8</code>.
     *
     * @param connection
     * @return String parsed from {@link URLConnection#getInputStream()}
     * @throws IOException
     */
    public static String readResponseInputStream(URLConnection connection) throws IOException {
        Charset charset = getCharsetFromHeader(connection.getContentType());
        try (InputStream in = connection.getInputStream()) {
            return new String(in.readAllBytes(), charset);
        }
    }

    /**
     * @param authorization Value of the Authorization HTTP header.
     * @return {@link UserNameAndPassword} if the header starts with Basic and can be parsed to
     *         username and password. Null otherwise.
     * @throws CharacterCodingException
     */
    public static UserNameAndPassword parseBasicAuthorizationHeader(String authorization)
        throws CharacterCodingException {
        LOG.log(DEBUG, "Parsing expected Basic authorization header: {0}", authorization);
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return null;
        }
        final int charsetIndex = authorization.indexOf("; charset=");
        final Charset authCharset = getCharsetFromHeader(authorization);
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

    /**
     * Parse the character encoding from the specified content type, authorization header
     * or any header using same rules when they contain ie.
     * <code>header="value"; charset=utf-8</code> .
     * If the header is null, or there is no explicit character encoding,
     * <code>UTF-8</code> is returned.
     *
     * @param header a content type header
     * @return {@link Charset} or UTF-8 if the charset is null, empty string or unknown.
     * @throws CharacterCodingException if the charset is not supported.
     */
    public static Charset getCharsetFromHeader(String header) throws CharacterCodingException {
        if (header == null) {
            return getCharset(null);
        }
        int start = header.indexOf("charset=");
        if (start < 0) {
            return getCharset(null);
        }
        String charset = header.substring(start + 8);
        int end = charset.indexOf(';');
        if (end >= 0) {
            charset = charset.substring(0, end);
        }
        charset = charset.trim();
        if (charset.length() > 2 && charset.startsWith("\"") && charset.endsWith("\"")) {
            charset = charset.substring(1, charset.length() - 1);
        }
        return getCharset(charset.trim());
    }

    /**
     * Parses the parameter. If it is null or empty string, returns {@link StandardCharsets#UTF_8}.
     * @param charset
     * @return specified {@link Charset} or UTF-8 if the charset is null, empty string or unknown.
     * @throws CharacterCodingException if the charset is not supported.
     */
    public static Charset getCharset(String charset) throws CharacterCodingException {
        if (charset == null || charset.isEmpty()) {
            return UTF_8;
        }
        try {
            return Charset.forName(charset);
        } catch (UnsupportedCharsetException uce) {
            CharacterCodingException e = new CharacterCodingException();
            e.initCause(uce);
            throw e;
        }
    }
}
