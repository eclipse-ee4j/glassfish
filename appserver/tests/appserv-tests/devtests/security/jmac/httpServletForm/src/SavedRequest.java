/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.jmac.httpservletform;

import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;

final class SavedRequest implements Serializable {
    private String method = null;
    private String requestURI = null;
    private String queryString = null;

    SavedRequest(HttpServletRequest hreq) {
        method = hreq.getMethod();
        requestURI = hreq.getRequestURI();
        queryString = hreq.getQueryString();
    }

    String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }

    String getRequestURI() {
        return requestURI;
    }

    void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    String getQueryString() {
        return queryString;
    }

    void setQueryString(String queryString) {
        this.queryString = queryString;
    }
}
