/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.readers;

import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.composite.CompositeUtil;
import org.glassfish.admin.rest.composite.RestModel;

/**
 *
 * @author jdlee
 */
@Provider
@Produces(Constants.MEDIA_TYPE_JSON)
@Consumes(Constants.MEDIA_TYPE_JSON)
public class RestModelReader<T extends RestModel> implements MessageBodyReader<T> {
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        String submittedType = mediaType.toString();
        int index = submittedType.indexOf(";");
        if (index > -1) {
            submittedType = submittedType.substring(0, index);
        }
        return submittedType.equals(Constants.MEDIA_TYPE_JSON) && RestModel.class.isAssignableFrom(type);
    }

    @Override
    public T readFrom(Class<T> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, String> mm,
            InputStream entityStream) throws WebApplicationException, IOException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(entityStream));
            StringBuilder sb = new StringBuilder();
            String line = in.readLine();
            while (line != null) {
                sb.append(line);
                line = in.readLine();
            }

            final Locale locale = CompositeUtil.instance().getLocale(mm);
            JSONObject o = new JSONObject(sb.toString());
            T model = CompositeUtil.instance().unmarshallClass(locale, type, o);
            Set<ConstraintViolation<T>> cv = CompositeUtil.instance().validateRestModel(locale, model);
            if (!cv.isEmpty()) {
                final Response response = Response.status(Status.BAD_REQUEST)
                        .entity(CompositeUtil.instance().getValidationFailureMessages(locale, cv, model)).build();
                throw new WebApplicationException(response);
            }
            return (T) model;
        } catch (JSONException ex) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getLocalizedMessage()).build());
        }
    }
}
