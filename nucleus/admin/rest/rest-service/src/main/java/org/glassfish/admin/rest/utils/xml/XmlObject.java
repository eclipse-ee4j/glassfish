/*
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

package org.glassfish.admin.rest.utils.xml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jasonlee
 */
public class XmlObject {
    private String name;
    private Object value;
    private Map<String, Object> children = new HashMap<String, Object>();

    public XmlObject(String name) {
        this(name, null);
    }

    public XmlObject(String name, Object value) {
        this.name = name.toLowerCase(Locale.US);
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    protected Document getDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public XmlObject put(String key, Object child) {
        if (child instanceof String) {
            children.put(key, child);
        } else if (child instanceof Number) {
            children.put(key, new XmlObject("Number", (Number) child));
        } else if (child instanceof XmlObject) {
            children.put(key, (XmlObject) child);
        }
        return this;
    }

    public Object remove(String key) {
        children.remove(key);
        return this;
    }

    public int childCount() {
        return children.size();
    }

    Node createNode(Document document) {
        Node node = document.createElement(getName());
        if (value != null) {
            node.setTextContent(value.toString());
        }
        Element element = (Element) node;
        for (Map.Entry<String, Object> child : children.entrySet()) {
            String key = child.getKey();
            Object value = child.getValue();
            if (value instanceof String) {
                element.setAttribute(key, value.toString());
            } else {
                XmlObject obj = (XmlObject) value;
                Node entryNode = document.createElement("entry");
                ((Element) entryNode).setAttribute("name", obj.getName());
                entryNode.appendChild(obj.createNode(document));
                node.appendChild(entryNode);
            }
            //            element.setAttribute(attribute.getKey(), attribute.getValue());
        }

        return node;
    }

    @Override
    public String toString() {
        return toString(-1);
    }

    public String toString(int indent) {
        Document document = getDocument();
        document.appendChild(createNode(document));
        try {
            Source source = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            if (indent > -1) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
            }
            transformer.transform(source, result);

            return stringWriter.getBuffer().toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
