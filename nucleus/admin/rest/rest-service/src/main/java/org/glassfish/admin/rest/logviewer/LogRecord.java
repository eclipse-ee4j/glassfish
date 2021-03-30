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

package org.glassfish.admin.rest.logviewer;

import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.RestLogging;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * internal REST wrapper for a log record will be used to emit JSON easily with Jackson framework
 *
 * @author ludo
 */
public class LogRecord {

    long recordNumber;
    Date loggedDateTime;
    String loggedLevel;
    String productName;
    String loggerName;
    String nameValuePairs;
    String messageID;
    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String Message) {
        this.message = Message;
    }

    public Date getLoggedDateTime() {
        return loggedDateTime;
    }

    public void setLoggedDateTime(Date loggedDateTime) {
        this.loggedDateTime = loggedDateTime;
    }

    public String getLoggedLevel() {
        return loggedLevel;
    }

    public void setLoggedLevel(String loggedLevel) {
        this.loggedLevel = loggedLevel;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getNameValuePairs() {
        return nameValuePairs;
    }

    public void setNameValuePairs(String nameValuePairs) {
        this.nameValuePairs = nameValuePairs;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(long recordNumber) {
        this.recordNumber = recordNumber;
    }

    public String toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("recordNumber", recordNumber);
            obj.put("loggedDateTimeInMS", (loggedDateTime != null) ? loggedDateTime.getTime() : null);
            obj.put("loggedLevel", loggedLevel);
            obj.put("productName", productName);
            obj.put("loggerName", loggerName);
            obj.put("nameValuePairs", nameValuePairs);
            obj.put("messageID", messageID);
            obj.put("Message", message); //.replaceAll("\n", Matcher.quoteReplacement("\\\n")).replaceAll("\"", Matcher.quoteReplacement("\\\"")));
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        return obj.toString();
    }

    public String toXML() {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document d = db.newDocument();

            Element result = d.createElement("record");
            result.setAttribute("recordNumber", "" + recordNumber);
            result.setAttribute("loggedDateTimeInMS", (loggedDateTime != null) ? ("" + loggedDateTime.getTime()) : "");
            result.setAttribute("loggedLevel", loggedLevel);
            result.setAttribute("productName", productName);
            result.setAttribute("loggerName", loggerName);
            result.setAttribute("nameValuePairs", nameValuePairs);
            result.setAttribute("messageID", messageID);
            result.setNodeValue(message);
            d.appendChild(result);
            return xmlToString(d);

        } catch (ParserConfigurationException pex) {
            throw new RuntimeException(pex);
        }
    }

    private String xmlToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            //  e.printStackTrace();
        } catch (TransformerException e) {
            //  e.printStackTrace();
        }
        return null;
    }
}
