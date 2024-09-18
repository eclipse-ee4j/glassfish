/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Ludovic Champenois
 */
@Consumes(MediaType.APPLICATION_JSON)
@Provider
public class JsonHashMapProvider implements MessageBodyReader<HashMap<String, String>> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(HashMap.class);
    }

    @Override
    public HashMap<String, String> readFrom(Class<HashMap<String, String>> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in) throws IOException {
        HashMap map = new HashMap();
        try {
            JSONObject obj = new JSONObject(new String(in.readAllBytes(), UTF_8));
            Iterator iter = obj.keys();

            while (iter.hasNext()) {
                String k = (String) iter.next();
                map.put(k, String.valueOf(obj.get(k)));

            }
            return map;

        } catch (IOException | JSONException ex) {
            //            map.put("error", "Entity Parsing Error: " + ex.getMessage());
            return map;
        }
    }
}
