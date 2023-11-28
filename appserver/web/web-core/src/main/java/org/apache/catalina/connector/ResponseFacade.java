/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.connector;

import static org.apache.catalina.LogFacade.NULL_RESPONSE_OBJECT;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.apache.catalina.LogFacade;
import org.apache.catalina.security.SecurityUtil;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Facade class that wraps a Coyote response object. All methods are delegated to the wrapped response.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 */
public class ResponseFacade implements HttpServletResponse {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    // ----------------------------------------------- Class/Instance Variables

    /**
     * The wrapped response.
     */
    protected Response response;

    // ----------------------------------------------------------- DoPrivileged

    private final class SetContentTypePrivilegedAction implements PrivilegedAction<Void> {

        private String contentType;

        public SetContentTypePrivilegedAction(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public Void run() {
            response.setContentType(contentType);
            return null;
        }
    }

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public ResponseFacade(Response response) {
        this.response = response;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Prevent cloning the facade.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Clear facade.
     */
    public void clear() {
        response = null;
    }

    public void finish() {
        checkResponseNull();

        response.setSuspended(true);
    }

    public boolean isFinished() {
        checkResponseNull();

        return response.isSuspended();
    }

    // ------------------------------------------------ ServletResponse Methods

    @Override
    public String getCharacterEncoding() {
        checkResponseNull();

        return response.getCharacterEncoding();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        checkResponseNull();

        ServletOutputStream sos = response.getOutputStream();
        if (isFinished()) {
            response.setSuspended(true);
        }

        return sos;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        checkResponseNull();

        try {
            PrintWriter writer = response.getWriter();
            if (isFinished()) {
                response.setSuspended(true);
            }

            return writer;
        } catch (UnsupportedCharsetException e) {
            // Servlet 6 states we should throw an UnsupportedEncodingException, but our backend
            // naturally throws an UnsupportedCharsetException.
            UnsupportedEncodingException unsupportedEncodingException = new UnsupportedEncodingException();
            unsupportedEncodingException.initCause(e);
            throw unsupportedEncodingException;
        }
    }

    @Override
    public void setContentLength(int len) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        if (SecurityUtil.isPackageProtectionEnabled()) {
            AccessController.doPrivileged(new SetContentTypePrivilegedAction(type));
        } else {
            response.setContentType(type);
        }
    }

    @Override
    public void setBufferSize(int size) {
        checkResponseNull();
        checkCommitted();

        response.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        checkResponseNull();

        return response.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        checkResponseNull();

        if (isFinished()) {
            return;
        }

        if (SecurityUtil.isPackageProtectionEnabled()) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws IOException {
                        response.setAppCommitted(true);

                        response.flushBuffer();
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                Exception ex = e.getException();
                if (ex instanceof IOException) {
                    throw (IOException) ex;
                }
            }
        } else {
            response.setAppCommitted(true);
            response.flushBuffer();
        }
    }

    @Override
    public void resetBuffer() {
        checkResponseNull();
        checkCommitted();

        response.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        checkResponseNull();

        return (response.isAppCommitted());
    }

    @Override
    public void reset() {
        checkResponseNull();
        checkCommitted();

        response.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        checkResponseNull();

        return response.getLocale();
    }

    @Override
    public void addCookie(Cookie cookie) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        checkResponseNull();

        return response.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        checkResponseNull();

        return response.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        checkResponseNull();

        return response.encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        checkResponseNull();
        checkCommitted();

        response.setAppCommitted(true);
        response.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        checkResponseNull();
        checkCommitted();

        response.setAppCommitted(true);
        response.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        checkResponseNull();
        checkCommitted();

        response.setAppCommitted(true);
        response.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        checkResponseNull();

        if (isCommitted()) {
            return;
        }

        response.setStatus(sc);
    }

    @Override
    public String getContentType() {
        checkResponseNull();

        return response.getContentType();
    }

    @Override
    public void setCharacterEncoding(String arg0) {
        checkResponseNull();

        response.setCharacterEncoding(arg0);
    }

    @Override
    public int getStatus() {
        checkResponseNull();

        return response.getStatus();
    }

    @Override
    public String getHeader(String name) {
        checkResponseNull();

        return response.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        checkResponseNull();

        return response.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        checkResponseNull();

        return response.getHeaderNames();
    }

    @Override
    public Supplier<Map<String, String>> getTrailerFields() {
        checkResponseNull();

        return response.getTrailerFields();
    }

    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        checkResponseNull();

        response.setTrailerFields(supplier);
    }

    private void checkResponseNull() {
        if (response == null) {
            throw new IllegalStateException(rb.getString(NULL_RESPONSE_OBJECT));
        }
    }

    private void checkCommitted() {
        if (isCommitted()) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
        // TODO TODO SERVLET 6.1
        // TODO Auto-generated method stub
    }

}
