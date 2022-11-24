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

import org.glassfish.api.admin.progress.ProgressStatusDTO;
import org.glassfish.api.admin.progress.ProgressStatusDTO.ChildProgressStatusDTO;

/**
 *
 * @author mmares
 */
public final class ProgressStatusDTOJsonProprietaryReader implements ProprietaryReader<ProgressStatusDTO> {

    private static final JsonFactory factory = new JsonFactory();

    @Override
    public boolean isReadable(Class<?> type, String mimetype) {
        return type.isAssignableFrom(ProgressStatusDTO.class);
    }

    public ProgressStatusDTO readFrom(final HttpURLConnection urlConnection) throws IOException {
        return readFrom(urlConnection.getInputStream(), urlConnection.getContentType());
    }

    @Override
    public ProgressStatusDTO readFrom(final InputStream is, final String contentType) throws IOException {
        JsonParser jp = factory.createParser(is);
        try {
            JsonToken token = jp.nextToken(); //sorounding object
            jp.nextToken(); //Name progress-status
            JsonToken token2 = jp.nextToken();
            if (token != JsonToken.START_OBJECT || token2 != JsonToken.START_OBJECT || !"progress-status".equals(jp.getCurrentName())) {
                throw new IOException("Not expected type (progress-status) but (" + jp.getCurrentName() + ")");
            }
            return readProgressStatus(jp);
        } finally {
            jp.close();
            is.close();
        }
    }

    public static ProgressStatusDTO readProgressStatus(JsonParser jp) throws IOException {
        ChildProgressStatusDTO child = readChildProgressStatus(jp);
        return child.getProgressStatus();
    }

    public static ChildProgressStatusDTO readChildProgressStatus(JsonParser jp) throws IOException {
        ProgressStatusDTO psd = new ProgressStatusDTO();
        int allocatedSteps = 0;
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            jp.nextToken(); // move to value
            if ("name".equals(fieldname)) {
                psd.setName(jp.getText());
            } else if ("id".equals(fieldname)) {
                psd.setId(jp.getText());
            } else if ("total-step-count".equals(fieldname)) {
                psd.setTotalStepCount(jp.getIntValue());
            } else if ("current-step-count".equals(fieldname)) {
                psd.setCurrentStepCount(jp.getIntValue());
            } else if ("complete".equals(fieldname)) {
                psd.setCompleted(jp.getBooleanValue());
            } else if ("allocated-steps".equals(fieldname)) {
                allocatedSteps = jp.getIntValue();
            } else if ("children".equals(fieldname)) {
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                        ProgressStatusDTO.ChildProgressStatusDTO child = readChildProgressStatus(jp);
                        psd.getChildren().add(child);
                    }
                }
            }
        }
        return new ChildProgressStatusDTO(allocatedSteps, psd);
    }

}
