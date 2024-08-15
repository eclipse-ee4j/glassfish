/*
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

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.RestLogging;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;

/**
 * Abstract implementation for entity writers to STaX API. This supports XML and JSON.
 *
 * @author mmares
 */
public abstract class AbstractStaxProvider<T> extends BaseProvider<T> {

    private static final XMLOutputFactory XML_FACTORY = XMLOutputFactory.newInstance();
    private static final MappedNamespaceConvention JSON_CONVENTION = new MappedNamespaceConvention();

    protected class PrePostFixedWriter {
        private String prefix;
        private String postfix;
        private XMLStreamWriter writer;

        public PrePostFixedWriter(XMLStreamWriter writer, String prefix, String postfix) {
            this.prefix = prefix;
            this.postfix = postfix;
            this.writer = writer;
        }

        public PrePostFixedWriter(XMLStreamWriter writer) {
            this(writer, null, null);
        }

        /**
         * Must be written after marshaled entity
         */
        public String getPostfix() {
            return postfix;
        }

        /**
         * Must be written before marshaled entity
         */
        public String getPrefix() {
            return prefix;
        }

        public XMLStreamWriter getWriter() {
            return writer;
        }

    }

    public AbstractStaxProvider(Class desiredType, MediaType... mediaType) {
        super(desiredType, mediaType);
    }

    @Override
    protected boolean isGivenTypeWritable(Class<?> type, Type genericType) {
        return desiredType.isAssignableFrom(type);
    }

    protected static XMLStreamWriter getXmlWriter(final OutputStream os, boolean indent) throws XMLStreamException {
        XMLStreamWriter wr = XML_FACTORY.createXMLStreamWriter(os, Constants.ENCODING);
        if (indent) {
            wr = new IndentingXMLStreamWriter(wr);
        }
        return wr;
    }

    protected static XMLStreamWriter getJsonWriter(final OutputStream os, boolean indent) throws UnsupportedEncodingException {
        return new MappedXMLStreamWriter(JSON_CONVENTION, new OutputStreamWriter(os, Constants.ENCODING));
    }

    /**
     * Returns XML StAX API for any media types with "xml" subtype. Otherwise returns JSON StAX API
     */
    protected PrePostFixedWriter getWriter(final MediaType mediaType, final OutputStream os, boolean indent) throws IOException {
        if (mediaType != null && "xml".equals(mediaType.getSubtype())) {
            try {
                return new PrePostFixedWriter(getXmlWriter(os, indent));
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        } else {
            String callBackJSONP = getCallBackJSONP();
            if (callBackJSONP != null) {
                return new PrePostFixedWriter(getJsonWriter(os, indent), callBackJSONP + "(", ")");
            } else {
                return new PrePostFixedWriter(getJsonWriter(os, indent));
            }
        }
    }

    /**
     * Marshalling implementation here.
     *
     * @param proxy object to marshal
     * @param wr STaX for marshaling
     * @throws XMLStreamException
     */
    protected abstract void writeContentToStream(T proxy, final XMLStreamWriter wr) throws XMLStreamException;

    @Override
    public String getContent(T proxy) {
        throw new UnsupportedOperationException("Provides only streaming implementation");
    }

    /**
     * Faster with direct stream writing
     */
    @Override
    public void writeTo(T proxy, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            PrePostFixedWriter writer = getWriter(mediaType, entityStream, super.getFormattingIndentLevel() > -1);
            //Write it
            if (writer.getPrefix() != null) {
                entityStream.write(writer.getPrefix().getBytes(Constants.ENCODING));
            }
            writeContentToStream(proxy, writer.getWriter());
            if (writer.getPostfix() != null) {
                entityStream.write(writer.getPostfix().getBytes(Constants.ENCODING));
            }
        } catch (XMLStreamException uee) {
            RestLogging.restLogger.log(Level.SEVERE, RestLogging.CANNOT_MARSHAL, uee);
            throw new WebApplicationException(uee, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
