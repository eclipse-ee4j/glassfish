/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Internal REST wrapper for a log record will be used to emit JSON easily
 * with Jackson framework.
 *
 * @author ludo
 */
public class LogRecord {

    private long recordNumber;
    private OffsetDateTime loggedDateTime;
    private String loggedLevel;
    private String productName;
    private String loggerName;
    private String nameValuePairs;
    private String messageID;
    private String message;

    public LogRecord() { }

    public LogRecord(List<? extends Serializable> logRecord) {
        int fieldIndex = 0;

        this.recordNumber = (Long) logRecord.get(fieldIndex++);
        this.loggedDateTime = (OffsetDateTime) logRecord.get(fieldIndex++);
        this.loggedLevel = (String) logRecord.get(fieldIndex++);
        this.productName = (String) logRecord.get(fieldIndex++);
        this.loggerName = (String) logRecord.get(fieldIndex++);
        this.nameValuePairs = (String) logRecord.get(fieldIndex++);
        this.messageID = (String) logRecord.get(fieldIndex++);
        this.message = (String) logRecord.get(fieldIndex);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String Message) {
        this.message = Message;
    }

    public OffsetDateTime getLoggedDateTime() {
        return loggedDateTime;
    }

    public void setLoggedDateTime(OffsetDateTime loggedDateTime) {
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

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject()
                .put("recordNumber", recordNumber)
                .put("loggedDateTimeInMS",
                        loggedDateTime == null ? null : loggedDateTime.toInstant().toEpochMilli())
                .put("loggedLevel", loggedLevel)
                .put("productName", productName)
                .put("loggerName", loggerName)
                .put("nameValuePairs", nameValuePairs)
                .put("messageID", messageID)
                .put("Message", message);
    }

    public void writeXml(XMLStreamWriter writer) throws XMLStreamException {
        boolean hasMessage = message != null && !message.isEmpty();

        if (hasMessage) {
            writer.writeStartElement("record");
        } else {
            writer.writeEmptyElement("record");
        }

        writer.writeAttribute("recordNumber", Long.toString(recordNumber));
        writer.writeAttribute("loggedDateTimeInMS",
                loggedDateTime == null ? "" : Long.toString(loggedDateTime.toInstant().toEpochMilli()));
        writer.writeAttribute("loggedLevel", loggedLevel);
        writer.writeAttribute("productName", productName);
        writer.writeAttribute("loggerName", loggerName);
        writer.writeAttribute("nameValuePairs", nameValuePairs);
        writer.writeAttribute("messageID", messageID);

        if (hasMessage) {
            writer.writeCharacters(message);
            writer.writeEndElement();
        }
    }

    public void writeCsv(StringBuilder sb) {
        sb.append(recordNumber).append(",");
        sb.append(loggedDateTime == null ?
                  "" : Long.toString(loggedDateTime.toInstant().toEpochMilli())).append(",");
        sb.append(loggedLevel).append(",");
        sb.append(productName).append(",");
        sb.append(loggerName).append(",");
        sb.append(nameValuePairs).append(",");
        sb.append("\"").append(message.replace("\"", "\"\"")).append("\"");
    }
}
