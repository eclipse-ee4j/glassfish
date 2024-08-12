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

import org.apache.catalina.Request;
import org.apache.catalina.Response;

/**
 * Access log formatter using the <i>combined</i> access log format from Apache.
 */
public class CombinedAccessLogFormatterImpl extends CommonAccessLogFormatterImpl {

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
        super.appendLogEntry(request, response, charBuffer);

        ServletRequest req = request.getRequest();
        HttpServletRequest hreq = (HttpServletRequest) req;

        appendReferer(charBuffer, hreq);
        charBuffer.put(' ');

        appendUserAgent(charBuffer, hreq);
    }


    /*
     * Appends the value of the 'referer' header of the given request to
     * the given char buffer.
     */
    private void appendReferer(CharBuffer cb, HttpServletRequest hreq) {
        cb.put('"');
        String referer = hreq.getHeader("referer");
        if (referer == null) {
            referer = NULL_VALUE;
        }
        cb.put(referer);
        cb.put('"');
    }


    /*
     * Appends the value of the 'user-agent' header of the given request to
     * the given char buffer.
     */
    private void appendUserAgent(CharBuffer cb, HttpServletRequest hreq) {
        cb.put('"');
        String ua = hreq.getHeader("user-agent");
        if (ua == null) {
            ua = NULL_VALUE;
        }
        cb.put(ua);
        cb.put('"');
    }

}
