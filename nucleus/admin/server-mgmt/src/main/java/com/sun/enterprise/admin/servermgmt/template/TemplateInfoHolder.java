/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.template;

import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.admin.servermgmt.xml.templateinfo.TemplateInfo;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;

public class TemplateInfoHolder {

    private static final LocalStringsImpl _strings = new LocalStringsImpl(TemplateInfoHolder.class);
    //Path where schema resides.
    private final static String TEMPLATE_INFO_SCHEMA_PATH = "xsd/schema/template-info.xsd";
    private TemplateInfo _templateInfo;
    private String _location;

    public TemplateInfoHolder(InputStream inputSteam, String location) throws DomainException {
        try {
            _templateInfo = parse(inputSteam);
        } catch (Exception e) {
            throw new DomainException(_strings.get("failedToParse", TEMPLATE_INFO_SCHEMA_PATH));
        }
        _location = location;
    }

    public TemplateInfo getTemplateInfo() {
        return _templateInfo;
    }

    public String getLocation() {
        return _location;
    }

    /**
     * Parse the configuration stream against the template-info schema.
     *
     * @param configStream InputStream of template-info.xml file.
     * @return Parsed Object.
     * @throws Exception If any error occurs in parsing.
     */
    @SuppressWarnings("rawtypes")
    private TemplateInfo parse(InputStream configStream) throws Exception {
        if (configStream == null) {
            throw new DomainException("Invalid stream");
        }
        try {
            URL schemaUrl = getClass().getClassLoader().getResource(TEMPLATE_INFO_SCHEMA_PATH);
            JAXBContext context = JAXBContext.newInstance(TemplateInfo.class.getPackage().getName());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaUrl);
            unmarshaller.setSchema(schema);
            InputSource is = new InputSource(configStream);
            SAXSource source = new SAXSource(is);
            Object obj = unmarshaller.unmarshal(source);
            return obj instanceof JAXBElement ? (TemplateInfo) (((JAXBElement) obj).getValue()) : (TemplateInfo) obj;
        } finally {
            try {
                configStream.close();
                configStream = null;
            } catch (IOException e) {
                /** Ignore */
            }
        }
    }
}
