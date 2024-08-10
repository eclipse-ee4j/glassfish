/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.util;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DomainXmlSAXParser extends DefaultHandler {
    private static final String PROPERTY = "property";
    private int level = 0;
    private String domainXmlEventListenerClass = null;

    public String getDomainXmlEventListenerClass() {
        return domainXmlEventListenerClass;
    }

    public void parse(java.io.File domainXml)
            throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        SAXParser saxParser;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        saxParser = factory.newSAXParser();
        saxParser.getXMLReader().setEntityResolver((EntityResolver) this);
        saxParser.parse(domainXml, this);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
        level++;
        if (level == 2 && PROPERTY.equals(qName)) {
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String aName = attrs.getQName(i); // Attr name
                    String aValue = attrs.getValue(aName);
                    if ("DomainXmlEventListenerClass".equals(aValue)) {
                        domainXmlEventListenerClass = attrs.getValue("value");
                    }
                }
            }
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        level--;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        return null;
    }
}
