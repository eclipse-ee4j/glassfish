/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.api.admin.progress.ProgressStatusEventComplete;
import org.glassfish.api.admin.progress.ProgressStatusEventCreateChild;
import org.glassfish.api.admin.progress.ProgressStatusEventProgress;
import org.glassfish.api.admin.progress.ProgressStatusEventSet;

/**
 * @author mmares
 */
public final class ProgressStatusEventJsonProprietaryReader implements ProprietaryReader<ProgressStatusEvent> {

    private static final JsonFactory factory = new JsonFactory();

    @Override
    public boolean isReadable(Class<?> type, String mimetype) {
        return type.isAssignableFrom(ProgressStatusEvent.class);
    }

    public ProgressStatusEvent readFrom(HttpURLConnection urlConnection) throws IOException {
        return readFrom(urlConnection.getInputStream(), urlConnection.getContentType());
    }

    @Override
    public ProgressStatusEvent readFrom(final InputStream is, final String contentType) throws IOException {
        JsonParser jp = factory.createParser(is);
        try {
            JsonToken token = jp.nextToken(); //sorounding object
            jp.nextToken(); //Name progress-status-event
            JsonToken token2 = jp.nextToken();
            if (token != JsonToken.START_OBJECT || token2 != JsonToken.START_OBJECT
                    || !"progress-status-event".equals(jp.getCurrentName())) {
                throw new IOException("Not expected type (progress-status-event) but (" + jp.getCurrentName() + ")");
            }
            return readProgressStatusEvent(jp);
        } finally {
            jp.close();
            is.close();
        }
    }

    public static ProgressStatusEvent readProgressStatusEvent(JsonParser jp) throws IOException {
        String id = null;
        JsonToken token = null;
        ProgressStatusEvent result = null;
        while ((token = jp.nextToken()) != JsonToken.END_OBJECT) {
            if (token == JsonToken.START_OBJECT) {
                String nm = jp.getCurrentName();
                if ("set".equals(nm)) {
                    result = new ProgressStatusEventSet(id);
                    readToPSEventSet((ProgressStatusEventSet) result, jp);
                } else if ("progres".equals(nm)) {
                    result = new ProgressStatusEventProgress(id);
                    readToPSEventProgress((ProgressStatusEventProgress) result, jp);
                } else if ("complete".equals(nm)) {
                    result = new ProgressStatusEventComplete(id);
                    readToPSEventComplete((ProgressStatusEventComplete) result, jp);
                } else if ("create-child".equals(nm)) {
                    result = new ProgressStatusEventCreateChild(id);
                    readToPSEventCreateChild((ProgressStatusEventCreateChild) result, jp);
                }
            } else {
                String fieldname = jp.getCurrentName();
                if ("id".equals(fieldname)) {
                    jp.nextToken(); // move to value
                    id = jp.getText();
                }
            }
        }
        return result;
    }

    public static void readToPSEventSet(ProgressStatusEventSet event, JsonParser jp) throws IOException {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            jp.nextToken(); // move to value
            if ("total-step-count".equals(fieldname)) {
                event.setTotalStepCount(jp.getIntValue());
            } else if ("current-step-count".equals(fieldname)) {
                event.setCurrentStepCount(jp.getIntValue());
            }
        }
    }

    public static void readToPSEventProgress(ProgressStatusEventProgress event, JsonParser jp) throws IOException {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            jp.nextToken(); // move to value
            if ("steps".equals(fieldname)) {
                event.setSteps(jp.getIntValue());
            } else if ("message".equals(fieldname)) {
                event.setMessage(jp.getText());
            } else if ("spinner".equals(fieldname)) {
                event.setSpinner(jp.getBooleanValue());
            }
        }
    }

    public static void readToPSEventComplete(ProgressStatusEventComplete event, JsonParser jp) throws IOException {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            jp.nextToken(); // move to value
            if ("message".equals(fieldname)) {
                event.setMessage(jp.getText());
            }
        }
    }

    public static void readToPSEventCreateChild(ProgressStatusEventCreateChild event, JsonParser jp) throws IOException {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            jp.nextToken(); // move to value
            if ("id".equals(fieldname)) {
                event.setChildId(jp.getText());
            } else if ("allocated-steps".equals(fieldname)) {
                event.setAllocatedSteps(jp.getIntValue());
            } else if ("total-step-count".equals(fieldname)) {
                event.setTotalSteps(jp.getIntValue());
            } else if ("name".equals(fieldname)) {
                event.setName(jp.getText());
            }
        }
    }
}
