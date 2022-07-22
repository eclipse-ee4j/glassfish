/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.accesslog;

import java.nio.CharBuffer;
import java.time.OffsetDateTime;

import org.apache.catalina.Request;
import org.apache.catalina.Response;

/**
 * Abstract class defining an interface for appending access log entries to the
 * access log in a customized access log format.
 */
public abstract class AccessLogFormatter {

    private final AccessLogPattern pattern;

    public AccessLogFormatter(AccessLogPattern pattern) {
        this.pattern = pattern;
    }


    /**
     * Appends an access log entry line, with info obtained from the given
     * request and response objects, to the given CharBuffer.
     *
     * @param request The request object from which to obtain access log info
     * @param response The response object from which to obtain access log info
     * @param charBuffer The CharBuffer to which to append access log info
     */
    public abstract void appendLogEntry(Request request, Response response, CharBuffer charBuffer);


    /**
     * @return current timestamp
     */
    protected OffsetDateTime getTimestamp() {
        return OffsetDateTime.now();
    }


    public AccessLogPattern getPattern() {
        return pattern;
    }
}
