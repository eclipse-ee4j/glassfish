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

package org.glassfish.admin.rest.provider;

import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.jvnet.hk2.config.Dom;

/**
 *
 * @author mh124079
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Produces("application/x-www-form-urlencoded")
@Provider
public class FormWriter implements MessageBodyWriter<Dom> {
    @Inject
    private UriInfo uriInfo;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Dom.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Dom data, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Dom data, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream out) throws IOException {
        //        out.write(preamble.getBytes());
        //        for (String name : data.keySet()) {
        //            out.write("<tr><td>".getBytes());
        //            out.write(name.getBytes());
        //            out.write("</td><td>".getBytes());
        //            out.write(data.get(name).getBytes());
        //            out.write("</td></tr>".getBytes());
        //        }
        //        out.write(postamble.getBytes());
        out.write(constructForm(data).getBytes());
    }

    private String constructForm(Dom data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>\n").append("<html><head><title>Data</title></head>\n")
                .append("<body><p>Change ").append(data.toString()).append(":</p>\n").append("<form name='pair' action='")
                .append(uriInfo.getAbsolutePath()).append("' method='POST'>\n").append("<table>\n");

        Set<String> ss = data.model.getAttributeNames();

        for (String name : ss) {
            sb.append("   <tr>\n").append("       <td align='right'>").append(name).append(":</td>\n")
                    .append("       <td><input type='text' name='").append(name).append("' value='").append(data.attribute(name))
                    .append("' size='30' /></td>\n").append("   </tr>\n");
        }

        sb.append("   <tr><td></td><td><input type='submit' value='Set' name='submit' /></td></tr>\n")
                .append("</table></form>\n</body></html>");

        return sb.toString();
    }

}
