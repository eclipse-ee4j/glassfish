/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.main.admin.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.fail;

public class ConnectionUtils {
    /**
     * This method opens a connection to the given URL and returns the string that is returned from that URL. This is
     * useful for simple servlet retrieval
     *
     * @param urlstr The URL to connect to
     * @return The string returned from that URL, or empty string if there was a problem contacting the URL
     */
    public static String getURL(String urlstr) {
        URLConnection urlc = openConnection(urlstr);
        return getContent(urlc);
    }

    /**
     * This method retrieves the content of the connection response as plain String
     *
     * @param connection
     * @return The string returned from that connection, or empty string if there was a problem contacting the URL
     */
    public static String getContent(URLConnection connection) {
        try (
                BufferedReader ir = new BufferedReader(new InputStreamReader(connection.getInputStream(), ISO_8859_1)); StringWriter ow = new StringWriter()) {
            String line;
            while ((line = ir.readLine()) != null) {
                ow.write(line);
                ow.write("\n");
            }
            return ow.getBuffer().toString();
        } catch (IOException ex) {
            return fail(ex);
        }
    }

    public static URLConnection openConnection(String url) {
        try {
            return new URL(url).openConnection();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }


}
