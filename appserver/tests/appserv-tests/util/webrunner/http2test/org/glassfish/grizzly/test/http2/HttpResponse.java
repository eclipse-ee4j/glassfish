/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.test.http2;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http2.Http2Stream;

/**
 * A simple Http2 response based on Grizzly runtime.
 *
 * @author Shing Wai Chan
 */
public class HttpResponse {
    private HttpContent httpContent;
    private HttpResponsePacket response;
    private boolean push = false;
    private HttpPushPromise pushPromise;
    private Map<String, List<String>> headerMap = new HashMap<>();
    private Map<String, String> trailerMap = null;

    HttpResponse(HttpContent httpContent, Map<String, String> trailerMap) {
        this.httpContent = httpContent;
        this.response = (HttpResponsePacket)httpContent.getHttpHeader();
        this.push = Http2Stream.getStreamFor(response).isPushStream();
        MimeHeaders headers = response.getHeaders();
        for (String name : headers.names()) {
            List<String> list = new ArrayList<>();
            for (String value : headers.values(name)) {
                list.add(value);
            }
            list = Collections.unmodifiableList(list);
            headerMap.put(name, list);
        }
        headerMap = Collections.unmodifiableMap(headerMap);
        this.trailerMap = Collections.unmodifiableMap(trailerMap);
        if (this.push) {
            pushPromise = new HttpPushPromise(httpContent);
        }
    }

    public int getStatus() {
        return response.getStatus();
    }

    public long getContentLength() {
        return response.getContentLength();
    }

    public String getHeader(String key) {
        return response.getHeader(key);
    }

    public Map<String, List<String>> getHeaders() {
        return headerMap;
    }

    public Map<String, String> getTrailerFields() {
        return trailerMap;
    }

    public String getBody() {
        return httpContent.getContent().toStringContent();
    }

    public String getBody(String charset) {
        return httpContent.getContent().toStringContent(Charset.forName(charset));
    }

    public boolean isPush() {
        return push;
    }

    public HttpPushPromise getHttpPushPromise() {
        return pushPromise;
    }
}
