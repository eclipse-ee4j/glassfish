/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018-2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutionException;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.StringsubsDefinition;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class parses the string substitution XML.
 */
public class StringSubstitutionParser {

    private static final Logger LOG = System.getLogger(StringSubstitutionParser.class.getName());

    private static final LocalStringsImpl _strings = new LocalStringsImpl(StringSubstitutionParser.class);
    // Path where schema resides i.e Parent directory for schema.
    private final static String DEFAULT_SCHEMA = "xsd/schema/stringsubs.xsd";

    /**
     * Parse the configuration stream against the string-subs schema and then closes the stream.
     *
     * @param configStream InputStream of stringsubs.xml file.
     * @return Parsed Object.
     * @throws StringSubstitutionException If any error occurs in parsing.
     */
    public static StringsubsDefinition parse(InputStream configStream) throws StringSubstitutionException {
        // If schema information is missing
        if (configStream == null) {
            throw new StringSubstitutionException(_strings.get("invalidStream"));
        }
        try {
            URL schemaUrl = StringSubstitutionParser.class.getClassLoader().getResource(DEFAULT_SCHEMA);
            JAXBContext context = JAXBContext.newInstance(StringsubsDefinition.class.getPackage().getName());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Schema schema = schemaFactory.newSchema(schemaUrl);
            unmarshaller.setSchema(schema);
            InputSource is = new InputSource(configStream);
            SAXSource source = new SAXSource(is);
            Object obj = unmarshaller.unmarshal(source);
            return obj instanceof JAXBElement
                ? (StringsubsDefinition) ((JAXBElement<?>) obj).getValue()
                : (StringsubsDefinition) obj;
        } catch (SAXException se) {
            throw new StringSubstitutionException(_strings.get("failedToParse", DEFAULT_SCHEMA), se);
        } catch (JAXBException jaxbe) {
            throw new StringSubstitutionException(_strings.get("failedToParse", DEFAULT_SCHEMA), jaxbe);
        } finally {
            try {
                configStream.close();
            } catch (IOException e) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, _strings.get("errorInClosingStream"));
                }
            }
        }
    }
}
