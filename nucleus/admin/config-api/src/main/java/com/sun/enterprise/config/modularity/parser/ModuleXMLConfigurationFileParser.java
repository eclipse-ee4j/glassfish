/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.modularity.parser;

import com.sun.enterprise.config.modularity.customization.ConfigBeanDefaultValue;
import com.sun.enterprise.config.modularity.customization.ConfigCustomizationToken;
import com.sun.enterprise.config.modularity.customization.FileTypeDetails;
import com.sun.enterprise.config.modularity.customization.PortTypeDetails;
import com.sun.enterprise.config.modularity.customization.TokenTypeDetails;
import com.sun.enterprise.util.LocalStringManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Masoud Kalali
 */
public class ModuleXMLConfigurationFileParser {
    private static final String CONFIG_BUNDLE = "config-bundle";
    private static final String LOCATION = "location";
    private static final String REPLACE_IF_EXISTS = "replace-if-exist";
    private static final String NAME = "name";
    private static final String DEFAULT_VALUE = "default-value";
    private static final String DESCRIPTION = "description";
    private static final String CONFIGURATION_ELEMENT = "configuration-element";
    private static final String CUSTOMIZATION_TOKEN = "customization-token";
    private static final String TITLE = "title";
    private static final String CONFIG_BEAN_CLASS_NAME = "config-bean-class-name";
    private static final String MUST_EXIST = "must-exist";
    private static final String BASE_OFFSET = "base-offset";
    private static final String FILE = "file";
    private static final String PORT = "port";
    private static final String VALIDATION_EXPRESSION = "validation-expression";

    private final LocalStringManager localStrings;

    public ModuleXMLConfigurationFileParser(LocalStringManager localStrings) {
        this.localStrings = localStrings;
    }

    public List<ConfigBeanDefaultValue> parseServiceConfiguration(InputStream xmlDocumentStream) throws XMLStreamException {

        List<ConfigBeanDefaultValue> configBeans = new ArrayList<>();
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        XMLEventReader eventReader = inputFactory.createXMLEventReader(xmlDocumentStream);
        ConfigBeanDefaultValue configValue = null;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                // If we have a item element we create a new item
                if (startElement.getName().getLocalPart().equalsIgnoreCase(CONFIG_BUNDLE)) {

                    configValue = new ConfigBeanDefaultValue();
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals(LOCATION)) {
                            configValue.setLocation(attribute.getValue());
                        } else if (attribute.getName().toString().equals(REPLACE_IF_EXISTS)) {
                            configValue.setReplaceCurrentIfExists(Boolean.parseBoolean(attribute.getValue()));
                        }
                    } //attributes
                    continue;
                } //config bundle

                if (startElement.getName().getLocalPart().equalsIgnoreCase(CUSTOMIZATION_TOKEN)) {
                    ConfigCustomizationToken token;
                    String value = null;
                    String description = null;
                    String name = null;
                    String title = null;
                    String validationExpression = null;
                    ConfigCustomizationToken.CustomizationType type = ConfigCustomizationToken.CustomizationType.STRING;
                    TokenTypeDetails tokenTypeDetails = null;

                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals(DEFAULT_VALUE)) {
                            value = attribute.getValue();
                        } else if (attribute.getName().toString().equals(DESCRIPTION)) {
                            description = getLocalizedValue(attribute.getValue());
                        } else if (attribute.getName().toString().equals(NAME)) {
                            name = attribute.getValue();
                        } else if (attribute.getName().toString().equals(TITLE)) {
                            title = getLocalizedValue(attribute.getValue());
                        } else if (attribute.getName().toString().equals(VALIDATION_EXPRESSION)) {
                            validationExpression = getLocalizedValue(attribute.getValue());
                        }

                    } //attributes
                    event = eventReader.nextEvent();
                    while (!event.isStartElement() && !event.isEndElement()) {
                        event = eventReader.nextEvent();
                    }
                    if (event.isStartElement()) {
                        startElement = event.asStartElement();
                        // If we have a item element we create a new item
                        if (startElement.getName().getLocalPart().equalsIgnoreCase(FILE)) {
                            type = ConfigCustomizationToken.CustomizationType.FILE;
                            String tokVal = startElement.getAttributeByName(QName.valueOf(MUST_EXIST)).getValue();
                            FileTypeDetails.FileExistCondition cond = FileTypeDetails.FileExistCondition.NO_OP;
                            if (tokVal.equalsIgnoreCase("true")) {
                                cond = FileTypeDetails.FileExistCondition.MUST_EXIST;
                            } else if (tokVal.equalsIgnoreCase("false")) {
                                cond = FileTypeDetails.FileExistCondition.MUST_NOT_EXIST;
                            }
                            tokenTypeDetails = new FileTypeDetails(cond);
                        } else if (startElement.getName().getLocalPart().equalsIgnoreCase(PORT)) {
                            type = ConfigCustomizationToken.CustomizationType.PORT;
                            tokenTypeDetails = new PortTypeDetails(startElement.getAttributeByName(QName.valueOf(BASE_OFFSET)).getValue());
                        }
                    }

                    token = new ConfigCustomizationToken(name, title, description, value, validationExpression, tokenTypeDetails, type);
                    //TODO check that ConfigValue is not null
                    configValue.addCustomizationToken(token);
                    continue;
                }
                if (startElement.getName().getLocalPart().equalsIgnoreCase(CONFIGURATION_ELEMENT)) {
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals(CONFIG_BEAN_CLASS_NAME)) {
                            configValue.setConfigBeanClassName(attribute.getValue());
                        }
                    } //attributes
                    event = eventReader.nextEvent();
                    if (event.isCharacters()) {
                        String str = event.asCharacters().getData();
                        configValue.setXmlConfiguration(str);
                    }
                    continue;
                }
            } //isStartElement
            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equalsIgnoreCase(CONFIG_BUNDLE)) {
                    configBeans.add(configValue);
                }
            }
        } //eventReader
        return configBeans;
    }

    private String getLocalizedValue(String value) {
        if (value.startsWith("$")) {
            value = localStrings.getLocalString(value.substring(1, value.length()), value.substring(1, value.length()));
        }
        return value;
    }
}
