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

import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;

/**
 * A simple Http2 push promise based on Grizzly runtime.
 *
 * @author Shing Wai Chan
 */
public class HttpPushPromise {
    private HttpRequestPacket request;
    private boolean push = false;

    HttpPushPromise(HttpContent httpContent) {
        this.request = ((HttpResponsePacket)httpContent.getHttpHeader()).getRequest();
    }

    public String getQueryString() {
        return request.getQueryString();
    }

    public String getMethod() {
        return request.getMethod().toString();
    }

    public String getHeader(String key) {
        return request.getHeader(key);
    }
}
