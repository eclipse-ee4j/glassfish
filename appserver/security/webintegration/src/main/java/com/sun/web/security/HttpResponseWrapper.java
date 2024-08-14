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

package com.sun.web.security;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Request;

class HttpResponseWrapper extends HttpServletResponseWrapper implements HttpResponse {

    private final HttpResponse httpResponse;

    HttpResponseWrapper(HttpResponse response, HttpServletResponse servletResponse) {
        super(servletResponse);
        httpResponse = response;
    }


    // ----- HttpResponse Methods -----
    @Override
    public String getHeader(String name) {
        return httpResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return httpResponse.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return httpResponse.getHeaders(name);
    }

    @Override
    public void addSessionCookieInternal(final Cookie cookie) {
        httpResponse.addSessionCookieInternal(cookie);
    }

    @Override
    public String getMessage() {
        return httpResponse.getMessage();
    }

    @Override
    public int getStatus() {
        return httpResponse.getStatus();
    }

    @Override
    public void reset(int status, String message) {
        httpResponse.reset(status, message);
    }

    @Override
    public void setError(int status, String message) {
        httpResponse.setError(status, message);
    }



    // ----- Response Methods -----
    @Override
    public Connector getConnector() {
        return httpResponse.getConnector();
    }

    @Override
    public void setConnector(Connector connector) {
        httpResponse.setConnector(connector);
    }

    @Override
    public int getContentCount() {
        return httpResponse.getContentCount();
    }

    @Override
    public Context getContext() {
        return httpResponse.getContext();
    }

    @Override
    public void setContext(Context context) {
        httpResponse.setContext(context);
    }

    @Override
    public void setAppCommitted(boolean appCommitted) {
        httpResponse.setAppCommitted(appCommitted);
    }

    @Override
    public boolean isAppCommitted() {
        return httpResponse.isAppCommitted();
    }

    @Override
    public boolean getIncluded() {
        return httpResponse.getIncluded();
    }

    @Override
    public void setIncluded(boolean included) {
        httpResponse.setIncluded(included);
    }

    @Override
    public String getInfo() {
        return httpResponse.getInfo();
    }

    @Override
    public Request getRequest() {
        return httpResponse.getRequest();
    }

    @Override
    public void setRequest(Request request) {
        httpResponse.setRequest(request);
    }

    @Override
    public ServletResponse getResponse() {
        return super.getResponse();
    }

    @Override
    public OutputStream getStream() {
        return httpResponse.getStream();
    }

    @Override
    public void setStream(OutputStream stream) {
        httpResponse.setStream(stream);
    }

    @Override
    public void setSuspended(boolean suspended) {
        httpResponse.setSuspended(suspended);
    }

    @Override
    public boolean isSuspended() {
        return httpResponse.isSuspended();
    }

    @Override
    public void setError() {
        httpResponse.setError();
    }

    @Override
    public boolean isError() {
        return httpResponse.isError();
    }

    @Override
    public void setDetailMessage(String message) {
        httpResponse.setDetailMessage(message);
    }

    @Override
    public String getDetailMessage() {
        return httpResponse.getDetailMessage();
    }

    @Override
    public ServletOutputStream createOutputStream() throws IOException {
        return httpResponse.createOutputStream();
    }

    @Override
    public void finishResponse() throws IOException {
        httpResponse.finishResponse();
    }

    @Override
    public int getContentLength() {
        return httpResponse.getContentLength();
    }


    /*
     * Delegate to HttpServletResponse public String getContentType() { return httpResponse.getContentType(); }
     */

    @Override
    public PrintWriter getReporter() throws IOException {
        return httpResponse.getReporter();
    }

    @Override
    public void recycle() {
        httpResponse.recycle();
    }


    /*
     * Delegate to HttpServletResponse public void resetBuffer() { httpResponse.resetBuffer(); }
     */

    @Override
    public void resetBuffer(boolean resetWriterStreamFlags) {
        httpResponse.resetBuffer(resetWriterStreamFlags);
    }

    @Override
    public void sendAcknowledgement() throws IOException {
        httpResponse.sendAcknowledgement();
    }

    @Override
    public String encode(String url) {
        return httpResponse.encode(url);
    }

}
