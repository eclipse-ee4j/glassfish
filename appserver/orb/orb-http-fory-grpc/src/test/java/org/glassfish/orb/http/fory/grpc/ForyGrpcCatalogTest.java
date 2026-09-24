/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.glassfish.orb.http.server.ServerExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A client has to be able to find the document it generates from, without
 * being told the names the deployment happens to use.
 */
class ForyGrpcCatalogTest {

    private static final String CONTEXT = "/glassfish-services";

    @Test
    void listsWhatThisServerPublishes() throws IOException {
        ForyGrpcCatalog catalog = new ForyGrpcCatalog();
        catalog.register(ForyGrpcCatalog.PREFIX + "app/module/Bean/demo.Greeter.fdl", "package demo;\n");
        catalog.register(ForyGrpcCatalog.PREFIX + "app/module/Bean/demo.Farewell.fdl", "package demo;\n");
        Exchange exchange = new Exchange("GET", CONTEXT + ForyGrpcCatalog.PREFIX);

        catalog.dispatch(exchange);

        assertEquals(200, exchange.status);
        assertEquals(CONTEXT + ForyGrpcCatalog.PREFIX + "app/module/Bean/demo.Farewell.fdl\n"
                + CONTEXT + ForyGrpcCatalog.PREFIX + "app/module/Bean/demo.Greeter.fdl\n",
                new String(exchange.body, StandardCharsets.UTF_8));
    }

    @Test
    void stillServesASingleDocument() throws IOException {
        ForyGrpcCatalog catalog = new ForyGrpcCatalog();
        String path = ForyGrpcCatalog.PREFIX + "app/module/Bean/demo.Greeter.fdl";
        catalog.register(path, "package demo;\n");
        Exchange exchange = new Exchange("GET", path);

        catalog.dispatch(exchange);

        assertEquals(200, exchange.status);
        assertEquals("package demo;\n", new String(exchange.body, StandardCharsets.UTF_8));
    }

    @Test
    void rebuildsWhenAskedForSomethingItDoesNotHaveYet() throws IOException {
        // The catalog is built while the server starts, before anything is
        // deployed: a document it has never seen may belong to an application
        // that arrived afterwards.
        ForyGrpcCatalog catalog = new ForyGrpcCatalog();
        String path = ForyGrpcCatalog.PREFIX + "late/module/Bean/demo.Greeter.fdl";
        catalog.onMiss(() -> catalog.register(path, "package late;\n"));
        Exchange exchange = new Exchange("GET", path);

        catalog.dispatch(exchange);

        assertEquals(200, exchange.status);
        assertEquals("package late;\n", new String(exchange.body, StandardCharsets.UTF_8));
    }

    @Test
    void rebuildsBeforeListing() throws IOException {
        ForyGrpcCatalog catalog = new ForyGrpcCatalog();
        catalog.onMiss(() -> catalog.register(ForyGrpcCatalog.PREFIX + "late/module/Bean/demo.Greeter.fdl",
                "package late;\n"));
        Exchange exchange = new Exchange("GET", CONTEXT + ForyGrpcCatalog.PREFIX);

        catalog.dispatch(exchange);

        assertEquals(CONTEXT + ForyGrpcCatalog.PREFIX + "late/module/Bean/demo.Greeter.fdl\n",
                new String(exchange.body, StandardCharsets.UTF_8));
    }

    @Test
    void answersNotFoundForADocumentItDoesNotHave() throws IOException {
        ForyGrpcCatalog catalog = new ForyGrpcCatalog();
        Exchange exchange = new Exchange("GET", ForyGrpcCatalog.PREFIX + "app/module/Bean/absent.fdl");

        catalog.dispatch(exchange);

        assertEquals(404, exchange.status);
    }

    private static final class Exchange implements ServerExchange {
        private final String method;
        private final String path;
        private int status;
        private byte[] body = new byte[0];

        Exchange(String method, String path) {
            this.method = method;
            this.path = path;
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public byte[] pathBytes() {
            return path.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return pathBytes().length;
        }

        @Override
        public String requestHeader(String name) {
            return null;
        }

        @Override
        public String queryParameter(String name) {
            return null;
        }

        @Override
        public InputStream requestBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setResponseHeader(String name, String value) {
        }

        @Override
        public void writeBody(ByteBuffer[] buffers) {
            int total = 0;
            for (ByteBuffer buffer : buffers) {
                total += buffer.remaining();
            }
            byte[] out = new byte[total];
            int at = 0;
            for (ByteBuffer buffer : buffers) {
                int length = buffer.remaining();
                buffer.get(out, at, length);
                at += length;
            }
            body = out;
        }

        @Override
        public String authenticatedUser() {
            return null;
        }
    }
}
