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

package org.glassfish.main.itest.tools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * HTTP client using the HTTP protocol and form based authentication.
 */
public class FormAuthHttpClient {

    private final URL baseUrl;
    private final String user;
    private final String password;
    private final CookieManager cookieManager;


    /**
     * Read remaining bytes in the response as a response body.
     * The input stream will be closed then.
     *
     * @param connection
     * @return String.
     * @throws IOException
     */
    public static String readResponseBody(final HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getInputStream()) {
            return new String(is.readAllBytes(), UTF_8);
        }
    }


    /**
     * @param baseUrl target endpoint.
     * @param user
     * @param password
     */
    public FormAuthHttpClient(URL baseUrl, String user, String password) {
        this.baseUrl = baseUrl;
        this.user = user;
        this.password = password;
        this.cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }


    /**
     * Does following steps:
     * <ol>
     * <li>HTTP GET to the protected relative path - expects HTTP 200 and login page - and also id
     * of the new session.
     * <li>HTTP POST to the <code>/j_security_check</code> with the j_username and j_password
     * in the request body and JSESSIONID sent as a cookie. Expects the body for the original
     * protected relative path.
     * </ol>
     *
     * @param relativePath
     * @return connection with headers and body which can be parsed.
     * @throws IOException
     * @throws ProtocolException
     */
    public HttpURLConnection get(String relativePath) throws IOException {
        final HttpCookie sessionId = createSession(relativePath);
        final HttpURLConnection connection = openConnection(baseUrl.getPort(), baseUrl.getPath() + "/j_security_check");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionId.getValue(), UTF_8));
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Charset", UTF_8.name());
        try (DataOutputStream stream = new DataOutputStream(connection.getOutputStream())) {
            stream.writeBytes("j_username=" + user + "&j_password=" + password);
            stream.flush();
        }
        return connection;
    }


    private HttpCookie createSession(String relativePath) throws IOException {
        final HttpURLConnection connection = openConnection(baseUrl.getPort(), baseUrl.getPath() + "/" + relativePath);
        connection.setRequestMethod("GET");
        assertThat(connection.getResponseCode(), equalTo(200));
        try (InputStream is = connection.getInputStream()) {
            final String text = new String(is.readAllBytes(), UTF_8);
            assertThat(text,
                stringContainsInOrder("<title>Login Page</title>", "Please login", "j_username", "j_password"));
            final HttpCookie sessionId = cookieManager.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("JSESSIONID")).findFirst().get();
            is.readAllBytes();
            return sessionId;
        } finally {
            connection.disconnect();
        }
    }

}
