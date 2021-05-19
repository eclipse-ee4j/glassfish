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

import java.util.Map;

/**
 * Event describing a log record being written to the log file.
 * This event is issued from the GFFileHandler. Interested parties
 * may register a LogEventListener with the GFFileHandler instance.
 * The GFFileHandler instance may be injected using hk2 mechanisms
 * to get a reference which can be used to register a LogEventListener.
 */
public interface LogEvent {

    /**
     * The formatted timestamp in the log event.
     * @return
     */
    public abstract String getTimestamp();

    /**
     * The message body including the stack trace of the associated Exception.
     * @return
     */
    public abstract String getMessage();

    /**
     * The name of the Level for this event.
     * @return
     */
    public abstract String getLevel();

    /**
     * Logger name identifying the source of this event.
     * @return
     */
    public abstract String getLogger();

    /**
     *
     * @return
     */
    public abstract int getLevelValue();

    /**
     * Integer value of the log Level.
     * @return
     */
    public abstract String getComponentId();

    /**
     * Raw timestamp in milliseconds.
     * @return
     */
    public abstract long getTimeMillis();

    /**
     * The message id for this log event.
     * @return
     */
    public abstract String getMessageId();

    /**
     * The thread ID where this log event originated.
     * @return
     */
    public abstract long getThreadId();

    /**
     * Thread name from where this log event originated.
     * @return
     */
    public abstract String getThreadName();

    /**
     * Current user Id executing this request during this log event.
     * @return
     */
    public abstract String getUser();

    /**
     * ECId for the current request for this log event.
     * @return
     */
    public abstract String getECId();

    /**
     * Optional name-value pairs associated with this log event.
     * @return
     */
    public abstract Map<String,Object> getSupplementalAttributes();

}
