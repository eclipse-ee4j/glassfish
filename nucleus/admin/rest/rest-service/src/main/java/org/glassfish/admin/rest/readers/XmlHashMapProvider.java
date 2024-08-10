/*
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
import java.util.HashMap;

import org.glassfish.admin.rest.provider.ProviderUtil;

/**
 * @author Rajeshwar Patil
 */
@Consumes(MediaType.APPLICATION_XML)
@Provider
public class XmlHashMapProvider extends ProviderUtil implements MessageBodyReader<HashMap<String, String>> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(HashMap.class);
    }

    @Override
    public HashMap<String, String> readFrom(Class<HashMap<String, String>> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in) throws IOException {
        try {
            XmlInputObject xmlObject = new XmlInputObject(in);
            return getStringMap((HashMap) xmlObject.initializeMap());
        } catch (InputException exception) {
            HashMap map = new HashMap();
            map.put("error", "Entity Parsing Error: " + exception.getMessage());

            return map;
            ///throw new RuntimeException(exception);
        }
    }
}
