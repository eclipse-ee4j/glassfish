/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.MessageBodyReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.composite.CompositeUtil;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.utils.Util;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author jdlee
 */
@Consumes(Constants.MEDIA_TYPE_JSON)
public class RestModelListReader implements MessageBodyReader<List<RestModel>> {
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        String submittedType = mediaType.toString();
        int index = submittedType.indexOf(";");
        if (index > -1) {
            submittedType = submittedType.substring(0, index);
        }
        return submittedType.equals(Constants.MEDIA_TYPE_JSON) && List.class.isAssignableFrom(type)
                && RestModel.class.isAssignableFrom(Util.getFirstGenericType(genericType));
    }

    @Override
    public List<RestModel> readFrom(Class<List<RestModel>> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            Locale locale = CompositeUtil.instance().getLocale(httpHeaders);
            List<RestModel> list = new ArrayList<>();
            JSONArray array = new JSONArray(new String(entityStream.readAllBytes(), UTF_8));
            Class<?> modelType = null;
            if (genericType instanceof ParameterizedType) {
                final ParameterizedType pt = (ParameterizedType) genericType;
                modelType = (Class) pt.getActualTypeArguments()[0];
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                RestModel model = (RestModel) CompositeUtil.instance().unmarshallClass(locale, modelType, o);
                Set<ConstraintViolation<RestModel>> cv = CompositeUtil.instance().validateRestModel(locale, model);
                if (!cv.isEmpty()) {
                    final Response response = Response.status(Status.BAD_REQUEST)
                            .entity(CompositeUtil.instance().getValidationFailureMessages(locale, cv, model)).build();
                    throw new WebApplicationException(response);
                }
                list.add(model);
            }
            return list;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }

    }
}
