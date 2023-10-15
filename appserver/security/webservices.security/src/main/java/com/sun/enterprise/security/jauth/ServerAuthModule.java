/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.security.auth.callback.CallbackHandler;

/**
 * This interface describes a module that can be configured for a ServerAuthContext. The main purpose of this module is
 * to validate client requests and to secure responses back to the client.
 *
 * <p>
 * A module implementation must assume it may be shared across different requests from different clients. It is the
 * module implementation's responsibility to properly store and restore any state necessary to associate new requests
 * with previous responses. A module that does not need to do so may remain completely stateless.
 *
 * <p>
 * Modules are passed a shared state Map that can be used to save state across a sequence of calls from
 * <code>validateRequest</code> to <code>secureResponse</code> to <code>disposeSubject</code>. The same Map instance is
 * guaranteed to be passed to all methods in the call sequence. Furthermore, it should be assumed that each call
 * sequence is passed its own unique shared state Map instance.
 *
 * @version %I%, %G%
 */
public interface ServerAuthModule {

    /**
     * Initialize this module with a policy to enforce, a CallbackHandler, and administrative options.
     *
     * <p>
     * Either the the request policy or the response policy (or both) must be non-null.
     *
     * @param requestPolicy the request policy this module is to enforce, which may be null.
     *
     * @param responsePolicy the response policy this module is to enforce, which may be null.
     *
     * @param handler CallbackHandler used to request information from the caller.
     *
     * @param options administrative options.
     */
    void initialize(AuthPolicy requestPolicy, AuthPolicy responsePolicy, CallbackHandler handler, Map options);

    /**
     * Authenticate a client request.
     *
     * <p>
     * The AuthParam input parameter encapsulates the client request and server response objects. This ServerAuthModule
     * validates the client request object (decrypts content and verifies a signature, for example).
     *
     * @param param an authentication parameter that encapsulates the client request and server response objects.
     *
     * @param subject the subject may be used by configured modules to store and Principals and credentials validated in the
     * request.
     *
     * @param sharedState a Map for modules to save state across a sequence of calls from <code>validateRequest</code> to
     * <code>secureResponse</code> to <code>disposeSubject</code>.
     *
     * @exception PendingException if the operation is pending (for example, when a module issues a challenge). The module
     * must have updated the response object in the AuthParam.
     *
     * @exception FailureException if the authentication failed. The module must have updated the response object in the
     * AuthParam.
     *
     * @exception AuthException if the operation failed.
     */
    void validateRequest(AuthParam param, Subject subject, Map sharedState) throws AuthException;

    /**
     * Secure the response to the client (sign and encrypt the response, for example).
     *
     * @param param an authentication parameter that encapsulates the client request and server response objects.
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
     * Dispose of the Subject.
     *
     * <p>
     * Remove Principals or credentials from the Subject object that were stored during <code>validateRequest</code>.
     *
     * @param subject the Subject instance to be disposed.
     *
     * @param sharedState a Map for modules to save state across a sequence of calls from <code>validateRequest</code> to
     * <code>secureResponse</code> to <code>disposeSubject</code>.
     *
     * @exception AuthException if the operation failed.
     */
    void disposeSubject(Subject subject, Map sharedState) throws AuthException;
}
