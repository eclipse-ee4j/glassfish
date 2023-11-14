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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * HTTP client using the HTTP 1.0 protocol, allowing to break some standards.
 * Useful for testing of server reactions to malformed or obsoleted requests.
 * <p>
 * Example: Request with nonstandard method, prohibited in later HTTP versions.
 */
public class HttpClient10 {

    private final URL url;
    private final String user;
    private final String password;

    /**
     * @param url target endpoint.
     * @param user
     * @param password
     */
    public HttpClient10(URL url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }


    /**
     * Sends the HTTP 1.0 request and reads the response.
     *
     * @param method GET/PUT/... or your custom method.
     * @param content the body
     * @return {@link HttpResponse}
     * @throws Exception
     */
    public HttpResponse send(String method, String content) throws Exception {
        try (Socket socket = new Socket(url.getHost(), url.getPort())) {
            InputStream inputStream = socket.getInputStream();
            socket.setSoLinger(true, 1000);
            PrintWriter outputWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            outputWriter.println(method + " " + url.getPath() + " HTTP/1.0");
            outputWriter.println("Host: " + url.getHost() + ':' + url.getPort());
            if (content != null) {
                outputWriter.println("Content-Length: " + content.length());
            }
            if (user != null && password != null) {
                String basicAuth = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(UTF_8));
                outputWriter.println("Authorization: Basic " + basicAuth);
            }
            outputWriter.println("");
            if (content != null) {
                outputWriter.print(content);
            }
            outputWriter.flush();
            return HttpResponse.parse(inputStream);
        }
    }

    /**
     * Parsed HTTP response.
     */
    public static class HttpResponse {

        /**
         * Example: <code>HTTP/1.0 403 Forbidden</copde>
         */
        public final String responseLine;
        /**
         * Response headers.
         */
        public final Map<String, String> headers;
        /**
         * Response body
         */
        public final String body;


        private HttpResponse(String responseLine, Map<String, String> headers, String body) {
            this.responseLine = responseLine;
            this.headers = headers;
            this.body = body;
        }


        private static HttpResponse parse(InputStream inputStream) throws IOException {
            String responseLine = parseResponseLine(inputStream);
            Map<String, String> headers = parseHeaders(inputStream);
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
            return new HttpResponse(responseLine, headers, body);
        }


        private static Map<String, String> parseHeaders(InputStream is) throws IOException {
            Map<String, String> headers = new HashMap<>();
            while (true) {
                String line = parseResponseLine(is);
                if (line == null || line.isEmpty()) {
                    break;
                }
                String[] header = parseHeader(line);
                headers.put(header[0], header[1]);
            }
            return headers;
        }


        private static String[] parseHeader(String line) {
            int colon = line.indexOf(':');
            if (colon < 0) {
                throw new IllegalStateException("Invalid header: " + line);
            }
            String name = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            return new String[] {name, value};
        }


        private static String parseResponseLine(InputStream input) throws IOException {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int ch = input.read();
                if (ch < 0) {
                    break;
                } else if (ch == '\n') {
                    break;
                }
                sb.append((char) ch);
            }
            // Strip any trailing carriage return
            int n = sb.length();
            if (n > 0 && sb.charAt(n - 1) == '\r') {
                sb.setLength(n - 1);
            }
            return sb.toString();
        }
    }
}
