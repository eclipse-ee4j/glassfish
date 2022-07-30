/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.server.logging.parser;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sanshriv
 */
public final class ParsedLogRecord implements Serializable {

    private static final long serialVersionUID = -5509051668657749139L;

    private final String formattedLogRecord;

    private LocalDate date;
    private LocalTime time;
    private ZoneOffset timezone;
    // note: there are possible also custom levels
    private String level;
    private String productId;
    private String loggerName;
    private Long threadId;
    private String threadName;
    private Integer levelValue;
    private String message;
    private String messageKey;

    private final Map<String, String> supplementalAttributes = new HashMap<>(0);

    public ParsedLogRecord(String formattedContent) {
        formattedLogRecord = formattedContent;
    }


    public String getFormattedLogRecord() {
        return formattedLogRecord;
    }


    public OffsetDateTime getTimestamp() {
        if (date == null || time == null) {
            return null;
        }
        return OffsetDateTime.of(date, time, timezone);
    }


    public LocalTime getTime() {
        return time;
    }


    public LocalDate getDate() {
        return date;
    }


    public void setTimestamp(final OffsetDateTime timestamp) {
        this.date = timestamp.toLocalDate();
        this.time = timestamp.toLocalTime();
        this.timezone = timestamp.getOffset();
    }


    public void setTime(final LocalTime time) {
        this.time = time;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public String getMessageKey() {
        return messageKey;
    }


    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }


    public String getLevel() {
        return level;
    }


    public void setLogLevel(String level) {
        this.level = level;
    }


    public Integer getLevelValue() {
        return levelValue;
    }


    public void setLogLevelValue(Integer level) {
        this.levelValue = level;
    }


    public String getLogger() {
        return loggerName;
    }


    public void setLogger(String loggerName) {
        this.loggerName = loggerName;
    }


    public String getProductId() {
        return productId;
    }


    public void setProductId(String productId) {
        this.productId = productId;
    }


    public Long getThreadId() {
        return threadId;
    }


    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }


    public String getThreadName() {
        return threadName;
    }


    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }


    public Map<String, String> getSupplementalAttributes() {
        return supplementalAttributes;
    }


    public void setSupplementalValue(String key, String value) {
        this.supplementalAttributes.put(key, value);
    }


    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Log record: <").append(formattedLogRecord).append('>').append(System.lineSeparator());
        return buffer.toString();
    }
}
