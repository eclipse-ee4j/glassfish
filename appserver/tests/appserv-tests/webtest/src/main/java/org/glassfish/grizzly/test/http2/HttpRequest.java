/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.test.http2;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpTrailer;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;

/**
 * A simple Http2 request based on Grizzly runtime.
 *
 * @author Shing Wai Chan
 */
public class HttpRequest {
    private HttpRequestPacket httpRequestPacket;
    private Connection connection;
    private Buffer contentBuffer = Buffers.EMPTY_BUFFER;
    private Map<String, String> trailerFields = null;

    private HttpRequest(HttpRequestPacket httpRequestPacket,
            String charEncoding, String content,
            Map<String, String> trailerFields, Connection connection) {

        this.httpRequestPacket = httpRequestPacket;
        httpRequestPacket.setCharacterEncoding(charEncoding);
        this.connection = connection;
        this.trailerFields = trailerFields;

        if (content != null) {
            MemoryManager mm = MemoryManager.DEFAULT_MEMORY_MANAGER;
            if (charEncoding != null) {
                try {
                    byte[] bytes = content.getBytes(charEncoding);
                    contentBuffer = Buffers.wrap(mm, bytes);
                } catch(UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                contentBuffer = Buffers.wrap(mm, content);
            }
        }

        if (!httpRequestPacket.isChunked()) {
            httpRequestPacket.setContentLength(contentBuffer.remaining());
        }
    }

    public void send() {
        boolean hasTrailer = httpRequestPacket.isChunked() &&
            trailerFields != null && !trailerFields.isEmpty();

        connection.write(HttpContent.builder(httpRequestPacket)
                .content(contentBuffer)
                .last(!hasTrailer)
                .build());

        if (hasTrailer) {
            HttpTrailer.Builder httpTrailerBuilder = HttpTrailer.builder(httpRequestPacket)
                .content(Buffers.EMPTY_BUFFER);
            for (Map.Entry<String, String> trailerField : trailerFields.entrySet()) {
                httpTrailerBuilder.header(
                    trailerField.getKey(), trailerField.getValue());
            }
            connection.write(httpTrailerBuilder.last(true).build());
        }
    }

    // ----- inner class -----
    public static class Builder {
        private HttpRequestPacket.Builder reqPacketBuilder = new  HttpRequestPacket.Builder();
        private Boolean chunked = null;
        private String charEncoding = null;
        private String content = null;
        private Map<String, String> trailerFields = null;
        private Connection connection = null;

        Builder(String host, int port, Connection connection) {
            reqPacketBuilder.method("GET").protocol(Protocol.HTTP_2_0)
                .host(host + ":" + port);
            this.connection = connection;
        }

        public Builder method(String method) {
            reqPacketBuilder.method(method);
            return this;
        }

        public Builder path(String path) {
            reqPacketBuilder.uri(path);
            return this;
        }

        public Builder contentType(String contentType) {
            reqPacketBuilder.contentType(contentType);
            return this;
        }

        public Builder chunked(Boolean c) {
            reqPacketBuilder.chunked(c);
            return this;
        }

        public Builder characterEncoding(String charEncoding) {
            this.charEncoding = charEncoding;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder header(String name, String value) {
            reqPacketBuilder.header(name, value);
            return this;
        }

        public Builder trailerField(String name, String value) {
            if (trailerFields == null) {
                trailerFields = new HashMap<>();
            }

            trailerFields.put(name, value);
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(reqPacketBuilder.build(),
                charEncoding, content, trailerFields, connection);
        }
    }
}
