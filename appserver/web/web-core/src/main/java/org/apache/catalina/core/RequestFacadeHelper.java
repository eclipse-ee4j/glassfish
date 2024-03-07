/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.core;

import org.apache.catalina.Globals;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.SessionTracker;

import java.util.ResourceBundle;
import jakarta.servlet.ServletRequest;

/**
 * This class exposes some of the functionality of
 * org.apache.catalina.connector.Request and
 * org.apache.catalina.connector.Response.
 *
 * It is in this package for purpose of package visibility
 * of methods.
 *
 * @author Shing Wai Chan
 */
public class RequestFacadeHelper {
    //use the same resource properties as in org.apache.catalina.connector.RequestFacade

    private Request request;

    private Response response;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    public RequestFacadeHelper(Request request) {
        this.request = request;
        this.response = (Response)request.getResponse();
    }

    public static RequestFacadeHelper getInstance(ServletRequest srvRequest) {
        RequestFacadeHelper reqFacHelper =
           (RequestFacadeHelper) srvRequest.getAttribute(Globals.REQUEST_FACADE_HELPER);
        return reqFacHelper;
    }

    /**
     * Increment the depth of application dispatch
     */
    int incrementDispatchDepth() {
        validateRequest();
        return request.incrementDispatchDepth();
    }

    /**
     * Decrement the depth of application dispatch
     */
    int decrementDispatchDepth() {
        validateRequest();
        return request.decrementDispatchDepth();
    }

    /**
     * Check if the application dispatching has reached the maximum
     */
    boolean isMaxDispatchDepthReached() {
        validateRequest();
        return request.isMaxDispatchDepthReached();
    }

    void track(Session localSession) {
        validateRequest();
        SessionTracker sessionTracker = (SessionTracker)
            request.getNote(Globals.SESSION_TRACKER);
        if (sessionTracker != null) {
            sessionTracker.track(localSession);
        }
    }

    String getContextPath(boolean maskDefaultContextMapping) {
        validateRequest();
        return request.getContextPath(maskDefaultContextMapping);
    }

    void disableAsyncSupport() {
        validateRequest();
        request.disableAsyncSupport();
    }

    // --- for Response ---

    // START SJSAS 6374990
    boolean isResponseError() {
        validateResponse();
        return response.isError();
    }

    String getResponseMessage() {
        validateResponse();
        return response.getMessage();
    }

    int getResponseContentCount() {
        validateResponse();
        return response.getContentCount();
    }
    // END SJSAS 6374990

    void resetResponse() {
        validateResponse();
        response.setSuspended(false);
        response.setAppCommitted(false);
    }


    public void clear() {
        request = null;
        response = null;
    }

    private void validateRequest() {
        if (request == null) {
            throw new IllegalStateException(rb.getString(LogFacade.VALIDATE_REQUEST_EXCEPTION));
        }
    }

    private void validateResponse() {
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.VALIDATE_RESPONSE_EXCEPTION));
        }
    }
}
