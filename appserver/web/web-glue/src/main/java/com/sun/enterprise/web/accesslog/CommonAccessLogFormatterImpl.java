/*
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

import org.apache.catalina.HttpResponse;
import org.apache.catalina.Request;
import org.apache.catalina.Response;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Access log formatter using the <i>common</i> access log format from
 * Apache.
 */
public class CommonAccessLogFormatterImpl extends AccessLogFormatter {

    protected static final String NULL_VALUE = "-";


    /**
     * Constructor.
     */
    public CommonAccessLogFormatterImpl() {

        super();

        final TimeZone timeZone = tz;
        dayFormatter = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat f = new SimpleDateFormat("dd");
                f.setTimeZone(timeZone);
                return f;
            }
        };
        monthFormatter = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat f = new SimpleDateFormat("MM");
                f.setTimeZone(timeZone);
                return f;
            }
        };
        yearFormatter = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat f = new SimpleDateFormat("yyyy");
                f.setTimeZone(timeZone);
                return f;
            }
        };
        timeFormatter = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                f.setTimeZone(timeZone);
                return f;
            }
        };
    }


    /**
     * Appends an access log entry line, with info obtained from the given
     * request and response objects, to the given CharBuffer.
     *
     * @param request The request object from which to obtain access log info
     * @param response The response object from which to obtain access log info
     * @param charBuffer The CharBuffer to which to append access log info
     */
    public void appendLogEntry(Request request,
                               Response response,
                               CharBuffer charBuffer) {

        ServletRequest req = request.getRequest();
        HttpServletRequest hreq = (HttpServletRequest) req;

        appendClientName(charBuffer, req);
        charBuffer.put(SPACE);

        appendClientId(charBuffer, req);
        charBuffer.put(SPACE);

        appendAuthUserName(charBuffer, hreq);
        charBuffer.put(SPACE);

        appendCurrentDate(charBuffer);
        charBuffer.put(SPACE);

        appendRequestInfo(charBuffer, hreq);
        charBuffer.put(SPACE);

        appendResponseStatus(charBuffer, response);
        charBuffer.put(SPACE);

        appendResponseLength(charBuffer, response);
        charBuffer.put(SPACE);
    }


    /*
     * Appends the client host name of the given request to the given char
     * buffer.
     */
    private void appendClientName(CharBuffer cb, ServletRequest req) {
        String value = req.getRemoteHost();
        if (value == null) {
            value = NULL_VALUE;
        }
        cb.put(value);
    }


    /*
     * Appends the client's RFC 1413 identity to the given char buffer..
     */
    private void appendClientId(CharBuffer cb, ServletRequest req) {
        cb.put(NULL_VALUE); // unsupported
    }


    /*
     * Appends the authenticated user (if any) to the given char buffer.
     */
    private void appendAuthUserName(CharBuffer cb, HttpServletRequest hreq) {
        String user = hreq.getRemoteUser();
        if (user == null) {
            user = NULL_VALUE;
        }
        cb.put(user);
    }


    /*
     * Appends the current date to the given char buffer.
     */
    private void appendCurrentDate(CharBuffer cb) {
        Date date = getDate();
        cb.put("[");
        cb.put(dayFormatter.get().format(date));           // Day
        cb.put('/');
        cb.put(lookup(monthFormatter.get().format(date))); // Month
        cb.put('/');
        cb.put(yearFormatter.get().format(date));          // Year
        cb.put(':');
        cb.put(timeFormatter.get().format(date));          // Time
        cb.put(SPACE);
        cb.put(timeZone);                                  // Time Zone
        cb.put("]");
    }


    /*
     * Appends info about the given request to the given char buffer.
     */
    private void appendRequestInfo(CharBuffer cb, HttpServletRequest hreq) {
        cb.put("\"");
        cb.put(hreq.getMethod());
        cb.put(SPACE);
        String uri = hreq.getRequestURI();
        if (uri == null) {
            uri = "NULL-HTTP-URI";
        }
        cb.put(uri);
        if (hreq.getQueryString() != null) {
            cb.put('?');
            cb.put(hreq.getQueryString());
        }
        cb.put(SPACE);
        cb.put(hreq.getProtocol());
        cb.put("\"");
    }


    /*
     * Appends the response status to the given char buffer.
     */
    private void appendResponseStatus(CharBuffer cb, Response response) {
        cb.put(String.valueOf(((HttpResponse) response).getStatus()));
    }


    /*
     * Appends the content length of the given response to the given char
     * buffer.
     */
    private void appendResponseLength(CharBuffer cb, Response response) {
        cb.put("" + response.getContentCount());
    }
}
