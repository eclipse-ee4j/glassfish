/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.jauth;

import jakarta.security.auth.message.MessageInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An HTTP Servlet authentication parameter that encapsulates HTTP Servlet request and response objects.
 *
 * <p>
 * HttpServletAuthParam may be created with null request or response objects. The following table describes when it is
 * appropriate to pass null:
 *
 * <pre>
 *                                        Request   Response
 *                                        -------   --------
 *
 * ClientAuthModule.secureRequest         non-null  null
 * ClientAuthModule.validateResponse      null      non-null
 *
 * ServerAuthModule.validateRequest       non-null  null
 * ServerAuthModule.secureResponse        null      non-null
 * </pre>
 *
 * <p>
 * As noted above, in the case of <code>ServerAuthModule.validateRequest</code> the module receives a null response
 * object. If the implementation of <code>validateRequest</code> encounters an authentication error, it may construct
 * the appropriate response object itself and set it into the HttpServletAuthParam via the <code>setResponse</code>
 * method.
 *
 * @version %I%, %G%
 */
public class HttpServletAuthParam implements AuthParam {

    private HttpServletRequest request;
    private HttpServletResponse response;
    // private static final MessageLayer layer =
    // new MessageLayer(MessageLayer.HTTP_SERVLET);

    /**
     * Create an HttpServletAuthParam with HTTP request and response objects.
     *
     * @param request the HTTP Servlet request object, or null.
     * @param response the HTTP Servlet response object, or null.
     */
    public HttpServletAuthParam(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Create an HttpServletAuthParam with MessageInfo object.
     *
     * @param messageInfo
     *
     */
    public HttpServletAuthParam(MessageInfo messageInfo) {
        this.request = (HttpServletRequest) messageInfo.getRequestMessage();
        this.response = (HttpServletResponse) messageInfo.getResponseMessage();
    }

    /**
     * Get the HTTP Servlet request object.
     *
     * @return the HTTP Servlet request object, or null.
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    /**
     * Get the HTTP Servlet response object.
     *
     * @return the HTTP Servlet response object, or null.
     */
    public HttpServletResponse getResponse() {
        return this.response;
    }

    /**
     * Set a new HTTP Servlet response object.
     *
     * <p>
     * If a response has already been set (it is non-null), this method returns. The original response is not overwritten.
     *
     * @param response the HTTP Servlet response object.
     *
     * @exception IllegalArgumentException if the specified response is null.
     */
    public void setResponse(HttpServletResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("invalid null response");
        }

        if (this.response == null) {
            this.response = response;
        }
    }

    /**
     * Get a MessageLayer instance that identifies HttpServlet as the message layer.
     *
     * @return a MessageLayer instance that identifies HttpServlet as the message layer.
     */
    // public MessageLayer getMessageLayer() {
    // return layer;
    // };

    /**
     * Get the operation related to the encapsulated HTTP Servlet request and response objects.
     *
     * @return the operation related to the encapsulated request and response objects, or null.
     */
    public String getOperation() {
        return null;
    }
}
