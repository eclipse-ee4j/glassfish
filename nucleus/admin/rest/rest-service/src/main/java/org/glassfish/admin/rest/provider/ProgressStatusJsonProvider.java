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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.glassfish.api.admin.progress.ProgressStatusBase;
import org.glassfish.api.admin.progress.ProgressStatusBase.ChildProgressStatus;

/**
 * Marshal ProgressStatus to JSON
 *
 * @author mmares
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON, "application/x-javascript" })
public class ProgressStatusJsonProvider extends BaseProvider<ProgressStatusBase> {

    private static final JsonFactory factory = new JsonFactory();

    public ProgressStatusJsonProvider() {
        super(ProgressStatusBase.class, MediaType.APPLICATION_JSON_TYPE, new MediaType("application", "x-javascript"));
    }

    @Override
    protected boolean isGivenTypeWritable(Class<?> type, Type genericType) {
        return desiredType.isAssignableFrom(type);
    }

    @Override
    public void writeTo(ProgressStatusBase proxy, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        JsonGenerator out = factory.createGenerator(entityStream, JsonEncoding.UTF8);
        out.writeStartObject();
        writeJson("progress-status", proxy, -1, out);
        out.writeEndObject();
        out.flush();
    }

    public void writeJson(String name, ProgressStatusBase ps, int allocatedSteps, JsonGenerator out) throws IOException {
        if (ps == null) {
            return;
        }
        if (name != null) {
            out.writeObjectFieldStart(name);
        } else {
            out.writeStartObject();
        }
        out.writeStringField("name", ps.getName());
        out.writeStringField("id", ps.getId());
        if (allocatedSteps >= 0) {
            out.writeNumberField("allocated-steps", allocatedSteps);
        }
        out.writeNumberField("total-step-count", ps.getTotalStepCount());
        out.writeNumberField("current-step-count", ps.getCurrentStepCount());
        out.writeBooleanField("complete", ps.isComplete());
        Set<ChildProgressStatus> children = ps.getChildProgressStatuses();
        if (children != null && !children.isEmpty()) {
            out.writeArrayFieldStart("children");
            for (ChildProgressStatus child : children) {
                writeJson(null, child.getProgressStatus(), child.getAllocatedSteps(), out);
            }
            out.writeEndArray();
        }
        out.writeEndObject();
    }

    @Override
    public String getContent(ProgressStatusBase proxy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
