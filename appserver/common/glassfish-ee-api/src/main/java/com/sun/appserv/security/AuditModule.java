/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

/*
 * AuditModule.java
 *
 * Created on July 27, 2003, 11:32 PM
 */

package com.sun.appserv.security;

import com.sun.enterprise.security.BaseAuditModule;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Base class that should be extended by all classes that wish to provide their own Audit support.
 * <p>
 * Note that the methods inherited from BaseAuditModule are repeated here so developers see the whole API available to
 * their custom AuditModule implementations by looking just at this one abstract class.
 *
 * @author Harpreet Singh
 * @version
 */
public abstract class AuditModule extends BaseAuditModule {

    /**
     * Invoked post web authorization request.
     *
     * @param user the username for whom the authorization was performed
     * @param req the HttpRequest object for the web request
     * @param type the permission type, hasUserDataPermission or hasResourcePermission.
     * @param success the status of the web authorization request
     */
    public void webInvocation(String user, HttpServletRequest req, String type, boolean success) {
    }

    /**
     * Invoked post ejb authorization request.
     *
     * @param user the username for whom the authorization was performed
     * @param ejb the ejb name for which this authorization was performed
     * @param method the method name for which this authorization was performed
     * @param success the status of the ejb authorization request
     */
    public void ejbInvocation(String user, String ejb, String method, boolean success) {
    }

    /**
     * Invoked during validation of the web service request
     *
     * @param uri The URL representation of the web service endpoint
     * @param endpoint The name of the endpoint representation
     * @param success the status of the web service request validation
     */
    public void webServiceInvocation(String uri, String endpoint, boolean success) {
    }

    /**
     * Invoked during validation of the web service request
     *
     * @param endpoint The representation of the web service endpoint
     * @param success the status of the web service request validation
     */
    public void ejbAsWebServiceInvocation(String endpoint, boolean success) {
    }
}
