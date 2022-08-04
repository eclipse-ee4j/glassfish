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

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.CharBuffer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.catalina.HttpResponse;
import org.apache.catalina.Request;
import org.apache.catalina.Response;

/**
 * Access log formatter using the <i>common</i> access log format from Apache.
 */
public class CommonAccessLogFormatterImpl extends AccessLogFormatter {

    protected static final String NULL_VALUE = "-";


    /**
     * Constructor.
     */
    public CommonAccessLogFormatterImpl() {
        super(getAccessLogPattern());
    }


    /**
     * Appends an access log entry line, with info obtained from the given
     * request and response objects, to the given CharBuffer.
     *
     * @param request The request object from which to obtain access log info
     * @param response The response object from which to obtain access log info
     * @param charBuffer The CharBuffer to which to append access log info
     */
    @Override
    public void appendLogEntry(Request request, Response response, CharBuffer charBuffer) {
        ServletRequest req = request.getRequest();
        HttpServletRequest hreq = (HttpServletRequest) req;

        appendClientName(charBuffer, req);
        charBuffer.put(' ');

        appendClientId(charBuffer, req);
        charBuffer.put(' ');

        appendAuthUserName(charBuffer, hreq);
        charBuffer.put(' ');

        appendCurrentDate(charBuffer);
        charBuffer.put(' ');

        appendRequestInfo(charBuffer, hreq);
        charBuffer.put(' ');

        appendResponseStatus(charBuffer, response);
        charBuffer.put(' ');

        appendResponseLength(charBuffer, response);
        charBuffer.put(' ');
    }


    private static AccessLogPattern getAccessLogPattern() {
        // 21/Dec/2009:07:42:45 -0800
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/LLL/uuuu:HH:mm:ss Z", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault());
        AccessLogPattern pattern = new AccessLogPattern(dateTimeFormatter, false, null);
        return pattern;
    }


    /**
     * Appends the client host name of the given request to the given char buffer.
     */
    private void appendClientName(CharBuffer cb, ServletRequest req) {
        String value = req.getRemoteHost();
        if (value == null) {
            value = NULL_VALUE;
        }
        cb.put(value);
    }


    /**
     * Appends the client's RFC 1413 identity to the given char buffer..
     */
    private void appendClientId(CharBuffer cb, ServletRequest req) {
        cb.put(NULL_VALUE); // unsupported
    }


    /**
     * Appends the authenticated user (if any) to the given char buffer.
     */
    private void appendAuthUserName(CharBuffer cb, HttpServletRequest hreq) {
        String user = hreq.getRemoteUser();
        if (user == null) {
            user = NULL_VALUE;
        }
        cb.put(user);
    }


    /**
     * Appends the current date to the given char buffer.
     */
    private void appendCurrentDate(CharBuffer cb) {
        cb.put("[");
        cb.put(getPattern().getDateTimeFormatter().format(getTimestamp()));
        cb.put("]");
    }


    /**
     * Appends info about the given request to the given char buffer.
     */
    private void appendRequestInfo(CharBuffer cb, HttpServletRequest hreq) {
        cb.put('\"');
        cb.put(hreq.getMethod());
        cb.put(' ');
        String uri = hreq.getRequestURI();
        if (uri == null) {
            uri = "NULL-HTTP-URI";
        }
        cb.put(uri);
        if (hreq.getQueryString() != null) {
            cb.put('?');
            cb.put(hreq.getQueryString());
        }
        cb.put(' ');
        cb.put(hreq.getProtocol());
        cb.put('\"');
    }


    /**
     * Appends the response status to the given char buffer.
     */
    private void appendResponseStatus(CharBuffer cb, Response response) {
        cb.put(String.valueOf(((HttpResponse) response).getStatus()));
    }


    /**
     * Appends the content length of the given response to the given char
     * buffer.
     */
    private void appendResponseLength(CharBuffer cb, Response response) {
        cb.put(Integer.toString(response.getContentCount()));
    }
}
