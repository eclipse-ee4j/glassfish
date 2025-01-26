/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.glassfish.api.admin.ParameterMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author mh124079
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Consumes({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_OCTET_STREAM })
@Provider
public class ParameterMapFormReader implements MessageBodyReader<ParameterMap> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(ParameterMap.class);
    }

    @Override
    public ParameterMap readFrom(Class<ParameterMap> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> headers, InputStream in) throws IOException {
        String formData = new String(in.readAllBytes(), UTF_8);
        ParameterMap map = new ParameterMap();
        StringTokenizer tokenizer = new StringTokenizer(formData, "&");
        String token;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            int idx = token.indexOf('=');
            if (idx < 0) {
                map.add(URLDecoder.decode(token, UTF_8), null);
            } else if (idx > 0) {
                map.add(URLDecoder.decode(token.substring(0, idx), UTF_8), URLDecoder.decode(token.substring(idx + 1), UTF_8));
            }
        }
        return map;
    }
}
