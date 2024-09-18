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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com), Martin Mares (martin.mares at oracle.com)
 */
public final class GfSseEventReceiver implements Closeable {

    private enum State {
        START,

        COMMENT, FIELD_NAME, FIELD_VALUE_FIRST, FIELD_VALUE,

        EVENT_FIRED
    }

    private final InputStream inputStream;
    private final Charset charset;
    private boolean closed;

    /**
     * Constructor.
     */
    GfSseEventReceiver(InputStream inputStream, Charset charset) {
        this.inputStream = inputStream;
        this.charset = charset;
    }

    public GfSseInboundEvent readEvent() throws IOException {
        GfSseInboundEvent inboundEvent = new GfSseInboundEvent();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        State currentState = State.START;
        String fieldName = null;

        try {
            int data = 0;
            while ((data = inputStream.read()) != -1) {

                switch (currentState) {

                    case START:
                        if (data == ':') {
                            currentState = State.COMMENT;
                        } else if (data != '\n') {
                            baos.write(data);
                            currentState = State.FIELD_NAME;
                        } else {
                            // System.out.println(sbr);
                            if (!inboundEvent.isEmpty()) {
                                return inboundEvent;
                            }
                            inboundEvent = new GfSseInboundEvent();
                        }
                        break;
                    case COMMENT:
                        if (data == '\n') {
                            currentState = State.START;
                        }
                        break;
                    case FIELD_NAME:
                        if (data == ':') {
                            fieldName = baos.toString(charset);
                            baos.reset();
                            currentState = State.FIELD_VALUE_FIRST;
                        } else if (data == '\n') {
                            processField(inboundEvent, baos.toString(charset), new byte[0], charset);
                            baos.reset();
                            currentState = State.START;
                        } else {
                            baos.write(data);
                        }
                        break;
                    case FIELD_VALUE_FIRST:
                        // first space has to be skipped
                        if (data != ' ') {
                            baos.write(data);
                        }

                        if (data == '\n') {
                            processField(inboundEvent, fieldName, baos.toByteArray(), charset);
                            baos.reset();
                            currentState = State.START;
                            break;
                        }

                        currentState = State.FIELD_VALUE;
                        break;
                    case FIELD_VALUE:
                        if (data == '\n') {
                            processField(inboundEvent, fieldName, baos.toByteArray(), charset);
                            baos.reset();
                            currentState = State.START;
                        } else {
                            baos.write(data);
                        }
                        break;
                    default:
                        // No-op default
                }
            }
            if (data == -1) {
                closed = true;
            }
            return null;
        } catch (IOException e) {
            closed = true;
            throw e;
        }

    }

    private void processField(GfSseInboundEvent inboundEvent, String name, byte[] value, Charset valueCharset) {
        if (name.equals("event")) {
            inboundEvent.setName(new String(value, valueCharset));
        } else if (name.equals("data")) {
            inboundEvent.addData(value);
            inboundEvent.addData(new byte[] { '\n' });
        } else if (name.equals("id")) {
            String s;
            if (value != null) {
                s = new String(value, valueCharset).trim();
                if (!s.matches("\\-?\\d+")) {
                    s = "";
                }
            } else {
                s = "";
            }
            inboundEvent.setId(s);
        }
    }

    /**
     * Get object state.
     *
     * @return true if no new {@link InboundEvent} can be received, false otherwise.
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        inputStream.close();
    }

}
