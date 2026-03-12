/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.rest.client.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import static java.lang.System.Logger.Level.ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jasonlee
 */
// FIXME: This should be rewritten as it is guessing types and swallows exception.
public class MarshallingUtils {

    private static final Logger LOG = System.getLogger(MarshallingUtils.class.getName());

    public static List<Map<String, String>> getPropertiesFromJson(String json) {
        List<Object> properties = null;
        json = json.trim();
        if (json.startsWith("{")) {
            properties = new ArrayList<>();
            properties.add(processJsonMap(json));
        } else if (json.startsWith("[")) {
            try {
                properties = processJsonArray(new JSONArray(json));
            } catch (JSONException e) {
                LOG.log(ERROR, e);
            }
        } else {
            throw new RuntimeException("The JSON string must start with { or ["); // i18n
        }

        return properties == null ? null : properties.stream().map(x -> (Map<String, String>) x).toList();
    }

    public static List<Map<String, String>> getPropertiesFromXml(String xml) {
        List<Map<String, String>> list = new ArrayList<>();
            XMLInputFactory inputFactory = XMLInputFactory.newFactory();
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            try (InputStream input = new ByteArrayInputStream(xml.trim().getBytes(UTF_8))) {
            XMLStreamReader parser = inputFactory.createXMLStreamReader(input);
            while (parser.hasNext()) {
                int event = parser.next();
                switch (event) {
                case XMLStreamConstants.START_ELEMENT: {
                    if ("list".equals(parser.getLocalName())) {
                        list = processXmlList(parser).stream().map(x -> (Map<String, String>) x).toList();
                    }
                    break;
                }
                default: {
                    // no-op
                }
                }
            }
        } catch (XMLStreamException | IOException ex) {
            LOG.log(ERROR, "An error occurred while processing an XML document.", ex);
        }
        return list;
    }

    public static String getXmlForProperties(final Map<String, String> properties) {
        return getXmlForProperties(new ArrayList<Map<String, String>>() {
            {
                add(properties);
            }
        });
    }

    public static String getXmlForProperties(List<Map<String, String>> properties) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            StringWriter sw = new StringWriter();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(sw);
            try {
                writer.writeStartDocument(UTF_8.name(), "1.0");
                writer.writeStartElement("list");
                for (Map<String, String> property : properties) {
                    writer.writeStartElement("map");
                    for (Map.Entry<String, String> entry : property.entrySet()) {
                        writer.writeStartElement("entry");
                        writer.writeAttribute("key", entry.getKey());
                        writer.writeAttribute("value", entry.getValue());
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                writer.writeEndDocument();
                writer.flush();
            } finally {
                writer.close();
            }
            return sw.toString();
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getJsonForProperties(final Map<String, String> properties) {
        return getJsonForProperties(new ArrayList<Map<String, String>>() {
            {
                add(properties);
            }
        });
    }

    public static String getJsonForProperties(List<Map<String, String>> properties) {
        JSONArray list = new JSONArray();

        for (Map<String, String> property : properties) {
            try {
                list.put(property);
            } catch (JSONException e) {
                throw new IllegalStateException(e);
            }
        }

        return list.toString();
    }

    public static Map buildMapFromDocument(String text) {
        if (text == null || text.isEmpty()) {
            return new HashMap();
        }

        text = text.trim();

        Map map = null;
        if (text.startsWith("{")) {
            map = processJsonMap(text);
        } else if (text.startsWith("<")) {
            XMLInputFactory inputFactory = XMLInputFactory.newFactory();
            inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            try (InputStream input = new ByteArrayInputStream(text.trim().getBytes(UTF_8))) {
                XMLStreamReader parser = inputFactory.createXMLStreamReader(input);
                while (parser.hasNext()) {
                    int event = parser.next();
                    switch (event) {
                        case XMLStreamConstants.START_ELEMENT: {
                            if ("map".equals(parser.getLocalName())) {
                                map = processXmlMap(parser);
                            }
                            break;
                        }
                        default: {
                            // No-op
                        }
                    }
                }
            } catch (XMLStreamException | IOException ex) {
                throw new RuntimeException("Failed to parse text:\n" + text, ex);
            }
        } else {
            System.out.println(text);
            throw new RuntimeException("An unknown document type was provided:  " + text);
        }

        return map;
    }

    private static Map<String, Object> processJsonMap(String json) {
        try {
            return processJsonObject(new JSONObject(json));
        } catch (JSONException e) {
            // FIXME: Really swallow exception?
            return new HashMap<>();
        }
    }

    private static Map<String, Object> processJsonObject(JSONObject jo) {
        Map<String, Object> map = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            Iterator<String> i = jo.keys();
            while (i.hasNext()) {
                String key = i.next();
                Object value = jo.get(key);
                if (value instanceof JSONArray) {
                    map.put(key, processJsonArray((JSONArray) value));
                } else if (value instanceof JSONObject) {
                    map.put(key, processJsonObject((JSONObject) value));
                } else if (value.getClass().getSimpleName().equalsIgnoreCase("null")) {
                    // The Map may not store null values, but we shouldn't rely on
                    // that behavior, just to be safe
                    map.put(key, null);
                } else {
                    map.put(key, value);
                }
            }
        } catch (JSONException e) {
            LOG.log(ERROR, "An error occurred while processing a JSON object.", e);
        }

        return map;
    }

    private static List<Object> processJsonArray(JSONArray ja) {
        List<Object> results = new ArrayList<>();

        try {
            for (int i = 0; i < ja.length(); i++) {
                Object entry = ja.get(i);
                if (entry instanceof JSONArray) {
                    results.add(processJsonArray((JSONArray) entry));
                } else if (entry instanceof JSONObject) {
                    results.add(processJsonObject((JSONObject) entry));
                } else {
                    results.add(entry);
                }
            }
        } catch (JSONException e) {
            LOG.log(ERROR, "An error occurred while processing a JSON object.", e);
        }

        return results;
    }

    private static Map<String, Object>  processXmlMap(XMLStreamReader parser) throws XMLStreamException {
        boolean endOfMap = false;
        Map<String, Object> entry = new HashMap<>();
        String key = null;
        String element = null;
        while (!endOfMap) {
            int event = parser.next();
            switch (event) {
            case XMLStreamConstants.START_ELEMENT: {
                if ("entry".equals(parser.getLocalName())) {
                    key = parser.getAttributeValue(null, "key");
                    String value = parser.getAttributeValue(null, "value");
                    if (value != null) {
                        entry.put(key, value);
                        key = null;
                    }
                } else if ("map".equals(parser.getLocalName())) {
                    Map<String, Object> value = processXmlMap(parser);
                    entry.put(key, value);
                } else if ("list".equals(parser.getLocalName())) {
                    List<?> value = processXmlList(parser);
                    entry.put(key, value);
                } else {
                    element = parser.getLocalName();
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT: {
                if ("map".equals(parser.getLocalName())) {
                    endOfMap = true;
                }
                element = null;
                break;
            }
            default: {
                String text = parser.getText();
                if (element != null) {
                    if ("number".equals(element)) {
                        if (text.contains(".")) {
                            entry.put(key, Double.parseDouble(text));
                        } else {
                            entry.put(key, Long.parseLong(text));
                        }
                    } else if ("string".equals(element)) {
                        entry.put(key, text);
                    }

                    element = null;
                }
            }
            }
        }
        return entry;
    }

    private static List<?> processXmlList(XMLStreamReader parser) throws XMLStreamException {
        List<Object> list = new ArrayList<>();
        boolean endOfList = false;
        String element = null;
        while (!endOfList) {
            int event = parser.next();
            switch (event) {
            case XMLStreamConstants.START_ELEMENT: {
                if ("map".equals(parser.getLocalName())) {
                    list.add(processXmlMap(parser));
                } else {
                    element = parser.getLocalName();
                }
                break;
            }
            case XMLStreamConstants.END_ELEMENT: {
                if ("list".equals(parser.getLocalName())) {
                    endOfList = true;
                }
                element = null;
                break;
            }
            default: {
                String text = parser.getText();
                if (element != null) {
                    if ("number".equals(element)) {
                        if (text.contains(".")) {
                            list.add(Double.valueOf(text));
                        } else {
                            list.add(Long.valueOf(text));
                        }
                    } else if ("string".equals(element)) {
                        list.add(text);
                    }

                    element = null;
                }
            }
            }
        }
        return list;
    }
}
