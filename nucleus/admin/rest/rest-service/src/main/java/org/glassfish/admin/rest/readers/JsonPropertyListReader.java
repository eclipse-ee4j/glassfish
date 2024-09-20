/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.glassfish.admin.rest.client.utils.MarshallingUtils;
import org.glassfish.admin.rest.utils.Util;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author jasonlee
 */
@Consumes(MediaType.APPLICATION_JSON)
@Provider
public class JsonPropertyListReader implements MessageBodyReader<List<Map<String, String>>> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(List.class) && Map.class.isAssignableFrom(Util.getFirstGenericType(genericType));
    }

    @Override
    public List<Map<String, String>> readFrom(Class<List<Map<String, String>>> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in) throws IOException {
        try {
            return MarshallingUtils.getPropertiesFromJson(new String(in.readAllBytes(), UTF_8));
        } catch (Exception exception) {
            throw new WebApplicationException(exception, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
