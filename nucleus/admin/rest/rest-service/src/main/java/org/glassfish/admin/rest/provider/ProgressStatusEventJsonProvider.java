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

package org.glassfish.admin.rest.provider;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.enterprise.util.StringUtils;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.glassfish.api.admin.progress.ProgressStatusEvent;
import org.glassfish.api.admin.progress.ProgressStatusEventComplete;
import org.glassfish.api.admin.progress.ProgressStatusEventCreateChild;
import org.glassfish.api.admin.progress.ProgressStatusEventProgress;
import org.glassfish.api.admin.progress.ProgressStatusEventSet;

/**
 *
 * @author mmares
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
public class ProgressStatusEventJsonProvider extends BaseProvider<ProgressStatusEvent> {

    private static final JsonFactory factory = new JsonFactory();

    public ProgressStatusEventJsonProvider() {
        super(ProgressStatusEvent.class, MediaType.APPLICATION_JSON_TYPE, new MediaType("application", "x-javascript"));
    }

    @Override
    protected boolean isGivenTypeWritable(Class<?> type, Type genericType) {
        return desiredType.isAssignableFrom(type);
    }

    @Override
    public void writeTo(ProgressStatusEvent proxy, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        JsonGenerator out = factory.createGenerator(entityStream, JsonEncoding.UTF8);
        out.writeStartObject();
        writePSEvent(proxy, out);
        out.writeEndObject();
        out.flush();
    }

    private void writePSEvent(ProgressStatusEvent event, JsonGenerator out) throws IOException {
        if (event == null) {
            return;
        }
        out.writeObjectFieldStart("progress-status-event");
        out.writeStringField("id", event.getSourceId());
        if (event instanceof ProgressStatusEventProgress) {
            writePSEventProgress((ProgressStatusEventProgress) event, out);
        } else if (event instanceof ProgressStatusEventSet) {
            writePSEventSet((ProgressStatusEventSet) event, out);
        } else if (event instanceof ProgressStatusEventComplete) {
            writePSEventComplete((ProgressStatusEventComplete) event, out);
        } else if (event instanceof ProgressStatusEventCreateChild) {
            writePSEventCreateChild((ProgressStatusEventCreateChild) event, out);
        } else {
            out.writeEndObject();
        }
    }

    private void writePSEventSet(ProgressStatusEventSet event, JsonGenerator out) throws IOException {
        out.writeObjectFieldStart("set");
        if (event.getTotalStepCount() != null) {
            out.writeNumberField("total-step-count", event.getTotalStepCount());
        }
        if (event.getCurrentStepCount() != null) {
            out.writeNumberField("current-step-count", event.getCurrentStepCount());
        }
        out.writeEndObject();
    }

    private void writePSEventProgress(ProgressStatusEventProgress event, JsonGenerator out) throws IOException {
        out.writeObjectFieldStart("progres");
        out.writeNumberField("steps", event.getSteps());
        if (StringUtils.ok(event.getMessage())) {
            out.writeStringField("message", event.getMessage());
        }
        if (event.isSpinner()) {
            out.writeBooleanField("spinner", event.isSpinner());
        }
        out.writeEndObject();
    }

    private void writePSEventComplete(ProgressStatusEventComplete event, JsonGenerator out) throws IOException {
        out.writeObjectFieldStart("complete");
        if (StringUtils.ok(event.getMessage())) {
            out.writeStringField("message", event.getMessage());
        }
        out.writeEndObject();
    }

    private void writePSEventCreateChild(ProgressStatusEventCreateChild event, JsonGenerator out) throws IOException {
        out.writeObjectFieldStart("create-child");
        out.writeStringField("id", event.getChildId());
        out.writeNumberField("allocated-steps", event.getAllocatedSteps());
        out.writeNumberField("total-step-count", event.getTotalSteps());
        if (StringUtils.ok(event.getName())) {
            out.writeStringField("name", event.getName());
        }
        out.writeEndObject();
    }

    @Override
    public String getContent(ProgressStatusEvent proxy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
