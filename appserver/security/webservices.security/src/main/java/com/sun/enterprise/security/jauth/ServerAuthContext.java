/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.jauth;

import java.util.Map;

import javax.security.auth.Subject;

/**
 * This ServerAuthContext class manages AuthModules that may be used to validate client requests. A caller typically
 * uses this class in the following manner:
 *
 * <ol>
 * <li>Retrieve an instance of this class via AuthConfig.getServerAuthContext.
 * <li>Receive initial client request and pass it to <i>validateRequest</i>. <br>
 * Configured plug-in modules validate credentials present in request (for example, decrypt and verify a signature). If
 * credentials valid and sufficient, return. Otherwise throw an AuthException.
 * <li>Authentication complete. <br>
 * Perform authorization check on authenticated identity and, if successful, dispatch to requested service application.
 * <li>Service application finished.
 * <li>Invoke <i>secureResponse</i>. <br>
 * Configured modules secure response (sign and encrypt it, for example).
 * <li>Send final response to client.
 * <li>The <i>disposeSubject</i> method may be invoked it necessary to clean up any authentication state in the Subject.
 * </ol>
 *
 * <p>
 * An instance may reuse module instances it previous created. As a result a single module instance may be used to
 * process different requests from different clients. It is the module implementation's responsibility to properly store
 * and restore any state necessary to associate new requests with previous responses. A module that does not need to do
 * so may remain completely stateless.
 *
 * <p>
 * Instances of this class have custom logic to determine what modules to invoke, and in what order. In addition, this
 * custom logic may control whether subsequent modules are invoked based on the success or failure of previously invoked
 * modules.
 *
 * <p>
 * The caller is responsible for passing in a state Map that can be used by underlying modules to save state across a
 * sequence of calls from <code>validateRequest</code> to <code>secureResponse</code> to <code>disposeSubject</code>.
 * The same Map instance must be passed to all methods in the call sequence. Furthermore, each call sequence should be
 * passed its own unique shared state Map instance.
 *
 * @version %I%, %G%
 * @see AuthConfig
 */
public interface ServerAuthContext extends AuthContext {

    /**
     * Authenticate a client request. (decrypt the message and verify a signature, for exmaple).
     *
     * <p>
     * This method invokes configured modules to authenticate the request.
     *
     * @param param an authentication parameter that encapsulates the client request and server response objects.
     *
     * @param subject the subject may be used by configured modules to store and Principals and credentials validated in the
     * request.
     *
     * @param sharedState a Map for modules to save state across a sequence of calls from <code>validateRequest</code> to
     * <code>secureResponse</code> to <code>disposeSubject</code>.
     *
     * @exception AuthException if the operation failed.
     */
    void validateRequest(AuthParam param, Subject subject, Map sharedState) throws AuthException;

    /**
     * Secure the response to the client (sign and encrypt the response, for example).
     *
     * <p>
     * This method invokes configured modules to secure the response.
     *
     * @param param an authentication parameter that encapsulates the client request and server response objects
     *
     * @param subject the subject may be used by configured modules to obtain credentials needed to secure the response, or
     * null. If null, the module may use a CallbackHandler to obtain the necessary information.
     *
     * @param sharedState a Map for modules to save state across a sequence of calls from <code>validateRequest</code> to
     * <code>secureResponse</code> to <code>disposeSubject</code>.
     *
     * @exception AuthException if the operation failed.
     */
    void secureResponse(AuthParam param, Subject subject, Map sharedState) throws AuthException;


    /**
     * modules manage sessions used by calling container to determine if it should delegate session management (including
     * the mapping of requests to authentication results established from previous requests) to the underlying
     * authentication modules of the context.
     * <p>
     * When this method returns true, the container should call validate on every request, and as such may depend on the
     * invoked modules to determine when a request pertains to an existing authentication session.
     * <p>
     * When this method returns false, the container may employ is own session management functionality, and may use this
     * functionality to recognize when an exiting request is to be interpretted in the context of an existing authentication
     * session.
     *
     * @return true if the context should be allowed to manage sessions, and false if session management (if it is to occur)
     * must be performed by the container.
     *
     * @exception AuthException if the operation failed.
     */
    boolean managesSessions(Map sharedState) throws AuthException;

}
