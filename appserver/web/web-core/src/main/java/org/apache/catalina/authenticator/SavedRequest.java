/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.apache.catalina.authenticator;

import jakarta.servlet.http.Cookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.glassfish.grizzly.http.util.ByteChunk;

/**
 * Object that saves the critical information from a request so that form-based authentication can reproduce it once the
 * user has been authenticated.
 *
 * <p>
 * <b>IMPLEMENTATION NOTE</b> - It is assumed that this object is accessed only from the context of a single thread, so
 * no synchronization around internal collection classes is performed.
 *
 * <p>
 * <b>FIXME</b> - Currently, this object has no mechanism to save or restore the data content of the request, although
 * it does save request parameters so that a POST transaction can be faithfully duplicated.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:28 $
 */

public final class SavedRequest {

    /**
     * The set of Cookies associated with this Request.
     */
    private ArrayList<Cookie> cookies = new ArrayList<>();

    /**
     * The set of Headers associated with this Request. Each key is a header name, while the value is a ArrayList containing
     * one or more actual values for this header. The values are returned as an Iterator when you ask for them.
     */
    private HashMap<String, ArrayList<String>> headers = new HashMap<>();

    /**
     * The request method used on this Request.
     */
    private String method;

    /**
     * The set of Locales associated with this Request.
     */
    private ArrayList<Locale> locales = new ArrayList<>();

    /**
     * The set of request parameters associated with this Request. Each entry is keyed by the parameter name, pointing at a
     * String array of the corresponding values.
     */
    private HashMap<String, String[]> parameters = new HashMap<>();

    /**
     * The query string associated with this Request.
     */
    private String queryString;

    /**
     * The request URI associated with this Request.
     */
    private String requestURI;

    /**
     * The body of this request.
     */
    private ByteChunk body;

    /**
     * The content type of the request, used if this is a POST.
     */
    private String contentType;

    private int contentLength = -1;

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Iterator<Cookie> getCookies() {
        return (cookies.iterator());
    }

    public void addHeader(String name, String value) {
        ArrayList<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<>();
            headers.put(name, values);
        }
        values.add(value);
    }

    public Iterator<String> getHeaderNames() {
        return (headers.keySet().iterator());
    }

    public Iterator<String> getHeaderValues(String name) {
        List<String> values = headers.get(name);
        if (values == null) {
            return (new ArrayList<String>()).iterator();
        }

        return values.iterator();
    }

    public void addLocale(Locale locale) {
        locales.add(locale);
    }

    public Iterator<Locale> getLocales() {
        return (locales.iterator());
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void addParameter(String name, String values[]) {
        parameters.put(name, values);
    }

    public Iterator<String> getParameterNames() {
        return (parameters.keySet().iterator());
    }

    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    public String getQueryString() {
        return (this.queryString);
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRequestURI() {
        return (this.requestURI);
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public ByteChunk getBody() {
        return body;
    }

    public void setBody(ByteChunk body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getContentLenght() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
}
