/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.common;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the action report as XML like this:
 * <br>
 * <!--
 *     Apologies for the formatting - it's necessary for the JavaDoc to be readable
 *     If you are using NetBeans, for example, click anywhere in this comment area to see
 *     the document example clearly in the JavaDoc preview
 * -->
 * <code>
 * <br>&lt;action-report description="xxx" exit-code="xxx" [failure-cause="xxx"]>
 * <br>&nbsp;&nbsp;&lt;message-part message="xxx">
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="xxx" value="xxx"/>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;message-part message="xxx" type="xxx">
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;/message-part>
 * <br>&nbsp;&nbsp;&lt/message-part>
 * <br>&nbsp;&nbsp;&lt;action-report ...> [for subactions]
 * <br>&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&lt;/action-report>
 * <br>&lt;/action-report>
 * </code>
 *
 * @author tjquinn
 */
@Service(name="xml")
@PerLookup
public class XMLActionReporter extends ActionReporter {

    @Override
    public void writeReport(OutputStream os)  {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.newDocument();
            d.appendChild(writeActionReport(d, this));
            writeXML(d, os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates a new Element representing the XML content describing an
     * action report.  Invokes itself recursively to capture information
     * about any subactions.
     * @param owningDocument Document which will own all generated XML content
     * @param report the ActionReporter to convert to XML content
     * @return Element for the specified ActionReporter (and any sub-reports)
     */
    private Element writeActionReport(Document owningDocument, ActionReporter report) {
        Element result = owningDocument.createElement("action-report");
        result.setAttribute("description", report.getActionDescription());
        result.setAttribute("exit-code", report.getActionExitCode().name());
        if (getFailureCause() != null) {
            result.setAttribute("failure-cause", getFailureCause().getLocalizedMessage());
        }

        writePart(result, report.getTopMessagePart(), null);
        for (ActionReporter subReport : report.getSubActionsReport()) {
            result.appendChild(writeActionReport(owningDocument, subReport));
        }
        return result;
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }

    private void writePart(Element actionReport, MessagePart part, String childType) {
        Document d = actionReport.getOwnerDocument();
        Element messagePart = d.createElement("message-part");
        actionReport.appendChild(messagePart);
        if (childType != null) {
            messagePart.setAttribute("type", childType);
        }

        for (Map.Entry prop : part.getProps().entrySet()) {
            Element p = d.createElement("property");
            messagePart.appendChild(p);
            p.setAttribute("name", prop.getKey().toString());
            Object value = prop.getValue();
            if (value instanceof List) {
                addListElement(p, (List)value);
            } else if (value instanceof Map) {
                addMapElement(p, (Map)value);
            } else {
                p.setAttribute("value", prop.getValue().toString());
            }
        }
        messagePart.setAttribute("message", part.getMessage());
        for (MessagePart subPart : part.getChildren()) {
            writePart(messagePart, subPart, subPart.getChildrenType());
        }
    }

    private void addListElement(Element parent, List list) {
        Document d = parent.getOwnerDocument();
        Element listElement = d.createElement("list");
        parent.appendChild(listElement);

        for (Object entry : list) {
            Element entryElement = d.createElement("entry");
            listElement.appendChild(entryElement);
            if (entry instanceof List) {
                addListElement(entryElement, (List) entry);
            } else if (entry instanceof Map) {
                addMapElement(entryElement, (Map) entry);
            } else {
                entryElement.setAttribute("value", entry.toString());
            }
        }
    }

    private void addMapElement(Element parent, Map map) {
        Document d = parent.getOwnerDocument();
        Element mapElement = d.createElement("map");
        parent.appendChild(mapElement);

        for (Map.Entry entry : (Set<Map.Entry>)map.entrySet()) {
            Element entryElement = d.createElement("entry");
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            mapElement.appendChild(entryElement);
            entryElement.setAttribute("key", key);

            if (value instanceof List) {
                addListElement(entryElement, (List) value);
            } else if (value instanceof Map) {
                addMapElement(entryElement, (Map) value);
            } else {
                entryElement.setAttribute("value", value.toString());
            }
        }
    }

    private void writeXML(Document doc, OutputStream os) throws TransformerConfigurationException, TransformerException {
        Source source = new DOMSource(doc);
        Result result = new StreamResult(os);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        transformer.transform(source, result);
    }
}
