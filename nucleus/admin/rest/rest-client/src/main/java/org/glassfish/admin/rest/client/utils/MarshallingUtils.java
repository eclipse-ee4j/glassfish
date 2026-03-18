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
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import static java.lang.System.Logger.Level.TRACE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Primitive implementation of JSON and XML unmarshaller.
 * It is used just internally.
 *
 * @deprecated Used just by two REST admin services and tests.
 * @author jasonlee
 */
@Deprecated
public class MarshallingUtils {

    private static final Logger LOG = System.getLogger(MarshallingUtils.class.getName());

    // Used just by JsonPropertyListReader
    @Deprecated
    public static List<Map<String, String>> getPropertiesFromJson(String json) {
        LOG.log(TRACE, "getPropertiesFromJson, json:\n{0}", json);
        json = json.trim();
        if (json.startsWith("{")) {
            return List.of(convertValuesToString(processJsonMap(json)));
        } else if (json.startsWith("[")) {
            try {
                return convertMapValuesToString(processJsonArray(new JSONArray(json)));
            } catch (Exception e) {
                throw new RuntimeException("Unprocessable JsonArray!", e);
            }
        } else {
            throw new RuntimeException("The JSON string must start with { or [");
        }
    }

    // Used just by XmlPropertyListReader
    @Deprecated
    public static List<Map<String, String>> getPropertiesFromXml(String xml) {
        LOG.log(TRACE, "getPropertiesFromXml, xml:\n{0}", xml);
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        try (InputStream input = new ByteArrayInputStream(xml.trim().getBytes(UTF_8))) {
            XMLStreamReader parser = inputFactory.createXMLStreamReader(input);
            while (parser.hasNext()) {
                int event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT && "list".equals(parser.getLocalName())) {
                    return convertMapValuesToString(processXmlList(parser));
                }
            }
            return List.of();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException("An error occurred while processing an XML document.", e);
        }
    }

    // Used just by our tests
    @Deprecated
    public static Map<String, Object> buildMapFromDocument(String text) {
        LOG.log(TRACE, "buildMapFromDocument, text:\n{0}", text);
        if (text == null || text.isEmpty()) {
            return Map.of();
        }
        if (text.startsWith("{")) {
            return processJsonMap(text);
        } else if (text.startsWith("<")) {
            XMLInputFactory inputFactory = XMLInputFactory.newFactory();
            inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            try (InputStream input = new ByteArrayInputStream(text.trim().getBytes(UTF_8))) {
                XMLStreamReader parser = inputFactory.createXMLStreamReader(input);
                while (parser.hasNext()) {
                    int event = parser.next();
                    if (event == XMLStreamConstants.START_ELEMENT && "map".equals(parser.getLocalName())) {
                        return processXmlMap(parser);
                    }
                }
                return Map.of();
            } catch (XMLStreamException | IOException ex) {
                throw new RuntimeException("Failed to parse text:\n" + text, ex);
            }
        } else {
            throw new RuntimeException("An unknown document type was provided: " + text);
        }
    }

    private static Map<String, Object> processJsonMap(String json) {
        try {
            return processJsonObject(new JSONObject(json));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> processJsonObject(JSONObject jo) {
        Map<String, Object> map = new HashMap<>();
        final Iterator<?> iterator = jo.keys();
        while (iterator.hasNext()) {
            final String key = iterator.next().toString();
            final Object value;
            try {
                value = jo.get(key);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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
        return map;
    }

    /**
     * @param jsonArray
     * @return list containing lists, maps and objects
     */
    private static List<Object> processJsonArray(JSONArray jsonArray) {
        final List<Object> results = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            final Object entry;
            try {
                entry = jsonArray.get(i);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (entry instanceof JSONArray) {
                results.add(processJsonArray((JSONArray) entry));
            } else if (entry instanceof JSONObject) {
                results.add(processJsonObject((JSONObject) entry));
            } else {
                results.add(entry);
            }
        }
        return results;
    }

    private static Map<String, Object>  processXmlMap(XMLStreamReader parser) throws XMLStreamException {
        boolean endOfMap = false;
        final Map<String, Object> entry = new HashMap<>();
        String key = null;
        String elementLocalName = null;
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
                        entry.put(key, processXmlMap(parser));
                    } else if ("list".equals(parser.getLocalName())) {
                        entry.put(key, processXmlList(parser));
                    } else {
                        elementLocalName = parser.getLocalName();
                    }
                    break;
                }
                case XMLStreamConstants.END_ELEMENT: {
                    if ("map".equals(parser.getLocalName())) {
                        endOfMap = true;
                    }
                    elementLocalName = null;
                    break;
                }
                default: {
                    String text = parser.getText();
                    if (elementLocalName != null) {
                        if ("number".equals(elementLocalName)) {
                            if (text.contains(".")) {
                                entry.put(key, Double.parseDouble(text));
                            } else {
                                entry.put(key, Long.parseLong(text));
                            }
                        } else if ("string".equals(elementLocalName)) {
                            entry.put(key, text);
                        }

                        elementLocalName = null;
                    }
                }
            }
        }
        return entry;
    }

    private static List<Object> processXmlList(XMLStreamReader parser) throws XMLStreamException {
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

    private static List<Map<String, String>> convertMapValuesToString(List<Object> list) {
        return list.stream().filter(m -> m instanceof Map).map(m -> (Map<String, Object>) m).map(MarshallingUtils::convertValuesToString).toList();
    }

    private static Map<String, String> convertValuesToString(Map<String, Object> map) {
        Map<String, String> strings = new HashMap<>();
        map.forEach((k, v) -> strings.put(k, v == null ? null : v.toString()));
        return strings;
    }
}
