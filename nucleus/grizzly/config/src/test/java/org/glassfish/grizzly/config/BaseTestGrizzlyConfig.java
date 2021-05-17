/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.glassfish.grizzly.http.server.HttpHandler;

import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.junit.Assert;
import org.jvnet.hk2.config.Dom;

public class BaseTestGrizzlyConfig {
    protected String getContent(URLConnection connection) {
        try {
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            try {
                StringBuilder builder = new StringBuilder();
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    builder.append(buffer, 0, read);
                }
                return builder.toString();
            } finally {
                reader.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        return "";
    }

    protected void addStaticHttpHandler(GenericGrizzlyListener listener, int count) {
        final String name = "target/tmp/"
                + Dom.convertName(getClass().getSimpleName()) + count;
        File dir = new File(name);
        dir.mkdirs();
        dir.deleteOnExit();

        FileWriter writer;
        try {
            final File file = new File(dir, "index.html");
            file.deleteOnExit();

            writer = new FileWriter(file);
            try {
                writer.write("<html><body>You've found the server on port " + listener.getPort() + "</body></html>");
                writer.flush();
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        final List<HttpServerFilter> httpServerFilters = listener.getFilters(HttpServerFilter.class);

        for (HttpServerFilter httpServerFilter : httpServerFilters) {
            httpServerFilter.setHttpHandler(new StaticHttpHandler(name));
        }
    }

    protected void setHttpHandler(GenericGrizzlyListener listener, HttpHandler handler) {
        final List<HttpServerFilter> httpServerFilters = listener.getFilters(HttpServerFilter.class);

        for (HttpServerFilter httpServerFilter : httpServerFilters) {
            httpServerFilter.setHttpHandler(handler);
        }
    }

    public SSLSocketFactory getSSLSocketFactory() throws IOException {
        try {
            //---------------------------------
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(
                        X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            //---------------------------------
            return sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }
}
