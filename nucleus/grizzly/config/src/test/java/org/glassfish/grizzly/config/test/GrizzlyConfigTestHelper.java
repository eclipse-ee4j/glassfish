/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.grizzly.config.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.glassfish.grizzly.config.GenericGrizzlyListener;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.jvnet.hk2.config.Dom;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class GrizzlyConfigTestHelper {

    private final Class<?> test;

    public GrizzlyConfigTestHelper(final Class<?> test) {
        this.test = test;
    }


    public String getContent(URLConnection connection) {
        try {
            return new String(connection.getInputStream().readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get content from " + connection, e);
        }
    }

    public void addStaticHttpHandler(GenericGrizzlyListener listener, int count) {
        final String name = "target/tmp/" + Dom.convertName(test.getSimpleName()) + count;
        final File dir = new File(name);
        dir.mkdirs();
        dir.deleteOnExit();

        final File file = new File(dir, "index.html");
        file.deleteOnExit();
        try (FileWriter writer = new FileWriter(file, UTF_8)) {
            writer.write("<html><body>You've found the server on port " + listener.getPort() + "</body></html>");
        } catch (IOException e) {
            throw new IllegalStateException("Could not add static http handler.", e);
        }

        final List<HttpServerFilter> httpServerFilters = listener.getFilters(HttpServerFilter.class);
        for (HttpServerFilter httpServerFilter : httpServerFilters) {
            httpServerFilter.setHttpHandler(new StaticHttpHandler(name));
        }
    }

    public void setHttpHandler(GenericGrizzlyListener listener, HttpHandler handler) {
        final List<HttpServerFilter> httpServerFilters = listener.getFilters(HttpServerFilter.class);
        for (HttpServerFilter httpServerFilter : httpServerFilters) {
            httpServerFilter.setHttpHandler(handler);
        }
    }

    /**
     * @return a {@link SSLSocketFactory} using a trust manager that does not validate certificate chains
     */
    public SSLSocketFactory getSSLSocketFactory() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            final SSLContext sc = SSLContext.getInstance("TLSv1.3");
            sc.init(null, trustAllCerts, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Wasn't possible to create a SSL Socket Factory", e);
        }
    }
}
