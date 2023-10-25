/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.admingui.common.util;

import jakarta.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * This class abstracts the response from the admin console code so that we can use JSON / REST interchangeably.
 * </p>
 *
 * @author jasonlee
 * @author Ken Paulsen (ken.paulsen@oracle.com)
 */
public abstract class RestResponse {
    public abstract int getResponseCode();

    public abstract String getResponseBody();

    public static RestResponse getRestResponse(Response response) {
        return new JerseyRestResponse(response);
    }

    public boolean isSuccess() {
        int status = getResponseCode();
        return (status >= 200) && (status <= 299);
    }

    /**
     * <p>
     * This method abstracts the physical response to return a consistent data structure. For many responses, this data
     * structure may look like:
     * </p>
     * <p/>
     * <p>
     * <code>
     * Map&lt;String, Object&gt;
     * {
     * "responseCode" : Integer    // HTTP Response code, ie. 200
     * "output" : String            // The Raw Response Body
     * "description" : String            // Command Description
     * // 0 or more messages returned from the command
     * "messages" : List&lt;Map&lt;String, Object&gt;&gt;
     * [
     * {
     * "message" : String  // Raw Message String
     * "..."          : String  // Additional custom attributes
     * // List of properties for this message
     * "properties" : List&lt;Map&lt;String, Object&gt;&gt;
     * [
     * {
     * "name"  : String    // The Property Name
     * "value" : String    // The Property Value
     * "properties" : List // Child Properties
     * }, ...
     * ]
     * }, ...
     * ]
     * }
     * </code>
     * </p>
     */
    public abstract Map<String, Object> getResponse();

    public abstract void close();

}

class JerseyRestResponse extends RestResponse {
    protected Response response;
    private String body = null;

    public JerseyRestResponse(Response response) {
        this.response = response;
    }

    @Override
    public String getResponseBody() {
        if (body == null) {
            body = response.readEntity(String.class);
        }
        return body;
    }

    @Override
    public int getResponseCode() {
        return response.getStatus();
    }

    /**
     * <p>
     * This method abstracts the physical response to return a consistent data structure.
     * </p>
     */
    @Override
    public Map<String, Object> getResponse() {
        // Prepare the result object
        Map<String, Object> result = new HashMap<>(5);

        // Add the Response Code
        result.put("responseCode", getResponseCode());
        // Add the Response Body
// FIXME: Do not put responseBody into the Map... too big, not needed
        result.put("responseBody", getResponseBody());

        String contentType = response.getHeaderString("Content-type");
        if (contentType != null) {
            String responseBody = getResponseBody();
            contentType = contentType.toLowerCase(GuiUtil.guiLocale);
            if (contentType.startsWith("application/xml")) {
                InputStream input = null;
                try {
                    XMLInputFactory inputFactory = XMLInputFactory.newFactory();
                    inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
                    input = new ByteArrayInputStream(responseBody.trim().getBytes("UTF-8"));
                    XMLStreamReader parser = inputFactory.createXMLStreamReader(input);
                    while (parser.hasNext()) {
                        int event = parser.next();
                        switch (event) {
                        case XMLStreamConstants.START_ELEMENT: {
                            if ("map".equals(parser.getLocalName())) {
                                result.put("data", processXmlMap(parser));
                            }
                            break;
                        }
                        default:
                            break;
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(RestResponse.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(RestResponse.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
//                // If XML...
//                Document document = MiscUtil.getDocument(getResponseBody());
//                Element root = document.getDocumentElement();
//                if ("action-report".equalsIgnoreCase(root.getNodeName())) {
//                    // Default XML document type...
//                    // Add the Command Description
//                    result.put("description", root.getAttribute("description"));
//                    result.put("exit-code", root.getAttribute("exit-code"));
//
//                    // Add the messages
//                    List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>(2);
//                    result.put("messages", messages);
//
//                    // Iterate over each node looking for message-part
//                    NodeList nl = root.getChildNodes();
//                    int len = nl.getLength();
//                    Node child;
//                    for (int idx = 0; idx < len; idx++) {
//                        child = nl.item(idx);
//                        if ((child.getNodeType() == Node.ELEMENT_NODE) && (child.getNodeName().equals("message-part"))) {
//                            messages.add(processMessagePart(child));
//                        }
//                    }
//                } else {
//                    // Generate a generic Java structure from the XML
//                    result.put("data", getJavaFromXML(root));
//                }
            } else if (contentType.startsWith("application/json")) {
                // Decode JSON
                result.put("data", JSONUtil.jsonToJava(responseBody));
            } else {
                Logger.getLogger(RestResponse.class.getName()).log(Level.WARNING, "Unsupported Response Format: '" + contentType + "'!");
            }
        }

        // Return the populated result data structure
        return result;
    }

    /**
     * <p>
     * This method will create a Map<String, Object>. It will add all the attributes of the given root Element to the Map.
     * It will then walk any child Elements and add the children as a <code>List&lt;Map&lt;String, Object&gt;&gt;</code> for
     * each unique element name.
     * </p>
     */
    private Map<String, Object> getJavaFromXML(Element element) {
        // Create a new Map to store the properties and children.
        Map<String, Object> result = new HashMap<>(10);

        // Add all the attributes...
        NamedNodeMap attributes = element.getAttributes();
        int attLen = attributes.getLength();
        for (int attIdx = 0; attIdx < attLen; attIdx++) {
            Node attribute = attributes.item(attIdx);
            result.put(attribute.getNodeName(), attribute.getNodeValue());
        }

        // Now add any child Elements
        String childName;
        Node child;
        List<Map<String, Object>> childList;
        NodeList nl = element.getChildNodes();
        int len = nl.getLength();
        for (int idx = 0; idx < len; idx++) {
            child = nl.item(idx);
            if ((child.getNodeType() == Node.ELEMENT_NODE)) {
                // We found a child Element...
                childName = child.getNodeName();
                if (result.containsKey(childName)) {
                    // Already created, add to it
                    childList = (List<Map<String, Object>>) result.get(childName);
                } else {
                    // Not created yet, create it
                    childList = new ArrayList<>(5);
                    result.put(childName, childList);
                }
                // Add the child to the List
                childList.add(getJavaFromXML((Element) child));
            }
        }

        // Return the fully populated Map<String, Object>
        return result;
    }

    /**
     * <p>
     * This method returns a fully populated Map<String, Object> for the given "message-part" <code>Node</code>.
     * </p>
     */
    private Map<String, Object> processMessagePart(Node messageNode) {
        // Create a Map to hold all the Message info...
        Map<String, Object> message = new HashMap<>(5);

        // Pull off all the attributes from the message...
        NamedNodeMap attributes = messageNode.getAttributes();
        int attLen = attributes.getLength();
        for (int attIdx = 0; attIdx < attLen; attIdx++) {
            // "message" should be one of them... add them all
            Node attribute = attributes.item(attIdx);
            message.put(attribute.getNodeName(), attribute.getNodeValue());
        }

        // Now see if there are any child message-parts or child properties
        NodeList nl = messageNode.getChildNodes();
        int len = nl.getLength();
        Node child;
        boolean hasChildMessages = false;
        boolean hasProperty = false;
        List<Map<String, Object>> properties = null;
        List<Map<String, Object>> messages = null;
        for (int idx = 0; idx < len; idx++) {
            child = nl.item(idx);
            if ((child.getNodeType() == Node.ELEMENT_NODE)) {
                if (child.getNodeName().equals("message-part")) {
                    // Recursively add this new message-part child
                    if (!hasChildMessages) {
                        // Create a List to hold the messages.
                        messages = new ArrayList<>(2);
                        message.put("messages", messages);
                        hasChildMessages = true;
                    }
                    // Add the message
                    messages.add(processMessagePart(child));
                } else if (child.getNodeName().equals("property")) {
                    // Add this new property
                    if (!hasProperty) {
                        // Create a List to hold the properties.
                        properties = new ArrayList<>(10);
                        message.put("properties", properties);
                        hasProperty = true;
                    }
                    // Add the property
                    properties.add(processProperty(child));
                }
            }
        }

        // Return the populated message
        return message;
    }

    /**
     * <p>
     * This method returns a fully populated Map<String, Object> for the given "property" <code>Node</code>.
     * </p>
     */
    private Map<String, Object> processProperty(Node propertyNode) {
        // Create a Map to hold all the Message info...
        Map<String, Object> property = new HashMap<>(5);

        // Pull off all the attributes from the property...
        NamedNodeMap attributes = propertyNode.getAttributes();
        int attLen = attributes.getLength();
        for (int attIdx = 0; attIdx < attLen; attIdx++) {
            // "name" and "value" should be the only 2, but add them all...
            Node attribute = attributes.item(attIdx);
            property.put(attribute.getNodeName(), attribute.getNodeValue());
        }

        // Now see if there are any child properties
        NodeList nl = propertyNode.getChildNodes();
        int len = nl.getLength();
        Node child;
        boolean hasProperty = false;
        List<Map<String, Object>> properties = null;
        for (int idx = 0; idx < len; idx++) {
            child = nl.item(idx);
            if ((child.getNodeType() == Node.ELEMENT_NODE)) {
                if (child.getNodeName().equals("property")) {
                    // Add this new property
                    if (!hasProperty) {
                        // Create a List to hold the properties.
                        properties = new ArrayList<>(10);
                        property.put("properties", properties);
                        hasProperty = true;
                    }
                    // Add the property
                    properties.add(processProperty(child));
                }
            }
        }

        // Return the populated property data structure
        return property;
    }

    private static Map processXmlMap(XMLStreamReader parser) throws XMLStreamException {
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
                    Map value = processXmlMap(parser);
                    entry.put(key, value);
                } else if ("list".equals(parser.getLocalName())) {
                    List value = processXmlList(parser);
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

    private static List processXmlList(XMLStreamReader parser) throws XMLStreamException {
        List list = new ArrayList();
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
                            list.add(Double.parseDouble(text));
                        } else {
                            list.add(Long.parseLong(text));
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

    @Override
    public void close() {
        response.close();
    }

}
