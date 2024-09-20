/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote.sse;

import com.sun.enterprise.admin.remote.reader.ProprietaryReader;
import com.sun.enterprise.admin.remote.reader.ProprietaryReaderFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Incoming SSE event.
 */
public class GfSseInboundEvent {
    private String name;
    private String id;
    private ByteArrayOutputStream data;

    GfSseInboundEvent() {
    }

    void setName(String name) {
        this.name = name;
    }

    void setId(String id) {
        this.id = id;
    }

    void addData(byte[] data) {
        if (this.data == null) {
            this.data = new ByteArrayOutputStream();
        }

        try {
            this.data.write(data);
        } catch (IOException e) {
            this.data = null;
        }
    }

    boolean isEmpty() {
        return data == null;
    }

    /**
     * Get the event name.
     *
     * @return event name or {@code null} if it is not present.
     */
    public String getName() {
        return name;
    }

    /**
     * Get event data.
     *
     * @param messageType type of stored data content.
     * @throws IOException when provided type can't be read.
     */
    public <T> T getData(final Class<T> messageType, final String contentType) throws IOException {
        final ProprietaryReader<T> reader = ProprietaryReaderFactory.<T>getReader(messageType, contentType);
        if (reader != null) {
            return reader.readFrom(new ByteArrayInputStream(stripLastLineBreak(data.toByteArray())), contentType);
        } else {
            //TODO: Throw IOException here
            return null;
        }
    }

    /**
     * Get event data as {@link String}.
     *
     * @return event data de-serialized as string.
     * @throws IOException when provided type can't be read.
     */
    public String getData() throws IOException {
        return getData(String.class, "text/plain");
    }

    @Override
    public String toString() {
        String s;

        try {
            s = getData();
        } catch (IOException e) {
            s = "";
        }

        return "InboundEvent{" + "name='" + name + '\'' + ", id='" + id + '\'' + ", data=" + s + '}';
    }

    /**
     * String last line break from data. (Last line-break should not be considered as part of received data).
     *
     * @param data data
     * @return updated byte array.
     */
    private byte[] stripLastLineBreak(byte[] data) {
        if ((data.length >= 1) && (data[data.length - 1] == '\n')) {
            byte[] newArray = new byte[data.length - 1];
            System.arraycopy(data, 0, newArray, 0, data.length - 1);
            data = newArray;
        }

        return data;
    }
}
