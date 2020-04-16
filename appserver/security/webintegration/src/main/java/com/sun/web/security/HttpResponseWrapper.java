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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Request;


class HttpResponseWrapper extends HttpServletResponseWrapper 
        implements HttpResponse {
        
    private HttpResponse httpResponse;         

    HttpResponseWrapper(HttpResponse response,
            HttpServletResponse servletResponse) {
        super(servletResponse);
        httpResponse = response;
    }
    
    // ----- HttpResponse Methods -----
    public String getHeader(String name) {
        return httpResponse.getHeader(name);
    }
    
    public Collection<String> getHeaderNames() {
        return httpResponse.getHeaderNames();
    }
    
    public Collection<String> getHeaders(String name) {
        return httpResponse.getHeaders(name);
    }

    public void addSessionCookieInternal(final Cookie cookie) {
        httpResponse.addSessionCookieInternal(cookie);
    }
    
    public String getMessage() {
        return httpResponse.getMessage();
    }

    public int getStatus() {
        return httpResponse.getStatus();
    }
    
    public void reset(int status, String message) {
        httpResponse.reset(status, message);
    }
    
    // ----- Response Methods -----
    public Connector getConnector() {
        return httpResponse.getConnector();
    }
    
    public void setConnector(Connector connector) {
        httpResponse.setConnector(connector);
    }
    
    public int getContentCount() {
        return httpResponse.getContentCount();
    }
    
    public Context getContext() {
        return httpResponse.getContext();
    }

    public void setContext(Context context) {
        httpResponse.setContext(context);
    }

    public void setAppCommitted(boolean appCommitted) {
        httpResponse.setAppCommitted(appCommitted);
    }

    public boolean isAppCommitted() {
        return httpResponse.isAppCommitted();
    }

    public boolean getIncluded() {
        return httpResponse.getIncluded();
    }
    
    public void setIncluded(boolean included) {
        httpResponse.setIncluded(included);
    }

    public String getInfo() {
        return httpResponse.getInfo();
    }

    public Request getRequest() {
        return httpResponse.getRequest();
    }

    public void setRequest(Request request) {
        httpResponse.setRequest(request);
    }

    public ServletResponse getResponse() {
        return super.getResponse();
    }
    
    public OutputStream getStream() {
        return httpResponse.getStream();
    }

    public void setStream(OutputStream stream) {
        httpResponse.setStream(stream);
    }

    public void setSuspended(boolean suspended) {
        httpResponse.setSuspended(suspended);
    }

    public boolean isSuspended() {
        return httpResponse.isSuspended();
    }

    public void setError() {
        httpResponse.setError();
    }

    public boolean isError() {
        return httpResponse.isError();
    }

    public void setDetailMessage(String message) {
        httpResponse.setDetailMessage(message);
    }

    public String getDetailMessage() {
        return httpResponse.getDetailMessage();
    }
    
    public ServletOutputStream createOutputStream() throws IOException {
        return httpResponse.createOutputStream();
    }

    public void finishResponse() throws IOException {
        httpResponse.finishResponse();
    }
    
    public int getContentLength() {
        return httpResponse.getContentLength();
    }
    
    /* Delegate to HttpServletResponse
      public String getContentType() {
      return httpResponse.getContentType();
      }
      */

    public PrintWriter getReporter() throws IOException {
        return httpResponse.getReporter();
    }

    public void recycle() {
        httpResponse.recycle();
    }

    /* Delegate to HttpServletResponse
       public void resetBuffer() {
       httpResponse.resetBuffer();
       }
       */
    
    public void resetBuffer(boolean resetWriterStreamFlags) {
        httpResponse.resetBuffer(resetWriterStreamFlags);
    }
    
    public void sendAcknowledgement() throws IOException {
        httpResponse.sendAcknowledgement();
    }

    public String encode(String url) {
        return httpResponse.encode(url);
    }
}
