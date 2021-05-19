/*
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

package com.sun.enterprise.server.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;

/**
 * @author sanshriv
 *
 */
public class LogEventImpl implements LogEvent {

    private String componentId = "";
    private String ecId = "";
    private String level = "";
    private int levelValue = 0;
    private String logger = "";
    private String message = "";
    private String messageId = "";
    private Map<String,Object> suppAttrs = new HashMap<String,Object>();
    private long threadId = 0L;
    private String threadName = "";
    private long timeMillis = 0L;
    private String timestamp = "";
    private String user = "";

    public LogEventImpl() {}

    public LogEventImpl(LogRecord rec) {
        level = rec.getLevel().getName();
        logger = rec.getLoggerName();
        message = rec.getMessage();
        threadId = rec.getThreadID();
        timeMillis = rec.getMillis();
        levelValue = rec.getLevel().intValue();
    }

    @Override
    public String getComponentId() {
        return componentId;
    }

    @Override
    public String getECId() {
        return ecId;
    }

    @Override
    public String getLevel() {
        return level;
    }

    @Override
    public int getLevelValue() {
        return levelValue;
    }

    @Override
    public String getLogger() {
        return logger;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public Map<String,Object> getSupplementalAttributes() {
        return suppAttrs;
    }

    @Override
    public long getThreadId() {
        return threadId;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    public long getTimeMillis() {
        return timeMillis;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String getUser() {
        return user;
    }

    /**
     * @param componentId the componentId to set
     */
    void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * @param ecId the ecId to set
     */
    void setECId(String ecId) {
        this.ecId = ecId;
    }

    /**
     * @param level the level to set
     */
    void setLevel(String level) {
        this.level = level;
    }

    /**
     * @param levelValue the levelValue to set
     */
    void setLevelValue(int levelValue) {
        this.levelValue = levelValue;
    }

    /**
     * @param logger the logger to set
     */
    void setLogger(String logger) {
        this.logger = logger;
    }

    /**
     * @param message the message to set
     */
    void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param messageId the messageId to set
     */
    void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * @param threadId the threadId to set
     */
    void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    /**
     * @param threadName the threadName to set
     */
    void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /**
     * @param timeMillis the timeMillis to set
     */
    void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    /**
     * @param timestamp the timestamp to set
     */
    void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @param user the user to set
     */
    void setUser(String user) {
        this.user = user;
    }

}
