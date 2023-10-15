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
 * This ClientAuthContext class manages AuthModules that may be used to secure requests made as a client. A caller
 * typically uses this class in the following manner:
 *
 * <ol>
 * <li>Retrieve an instance of this class via AuthConfig.getClientAuthContext.
 * <li>Invoke <i>secureRequest</i>. <br>
 * ClientAuthContext implementation invokes configured plug-in modules. Modules attach credentials to initial request
 * object (for example, a username and password), and/or secure the request (for example, sign and encrypt the request).
 * <li>Issue request.
 * <li>Receive response and pass it to <i>validateResponse</i>. <br>
 * ClientAuthContext implementation invokes configured plug-in modules. Modules verify or decrypt response as necessary.
 * <li>The <i>disposeSubject</i> method may be invoked if necessary to clean up any authentication state in the Subject.
 * </ol>
 *
 * <p>
 * An instance may reuse module instances it previously created. As a result a single module instance may be used to
 * issue different requests as different clients. It is the module implementation's responsibility to properly store and
 * restore any necessary state. A module that does not need to do so may remain completely stateless.
 *
 * <p>
 * Instances of this class have custom logic to determine what modules to invoke, and in what order. In addition, this
 * custom logic may control whether subsequent modules are invoked based on the success or failure of previously invoked
 * modules.
 *
 * <p>
 * The caller is responsible for passing in a state Map that can be used by underlying modules to save state across a
 * sequence of calls from <code>secureRequest</code> to <code>validateResponse</code> to <code>disposeSubject</code>.
 * The same Map instance must be passed to all methods in the call sequence. Furthermore, each call sequence should be
 * passed its own unique shared state Map instance.
 *
 * @version %I%, %G%
 * @see AuthConfig
 */
public interface ClientAuthContext extends AuthContext {

    /**
     * Secure a request message.
     *
     * <p>
     * Attach authentication credentials to an initial request, sign/encrypt a request, or respond to a server challenge,
     * for example.
     *
     * <p>
     * This method invokes configured modules to secure the request.
     *
     * @param param an authentication parameter that encapsulates the client request and server response objects.
     *
     * @param subject the subject may be used by configured modules to obtain Principals and credentials necessary to secure
     * the request, or null. If null, the module may use a CallbackHandler to obtain any information necessary to secure the
     * request.
     *
     * @param sharedState a Map for modules to save state across a sequence of calls from <code>secureRequest</code> to
     * <code>validateResponse</code> to <code>disposeSubject</code>.
     *
     * @exception AuthException if the operation failed.
     */
    void secureRequest(AuthParam param, Subject subject, Map sharedState) throws AuthException;

    /**
     * Validate received response.
     *
     * <p>
     * Validation may include verifying signature in response, or decrypting response contents, for example.
     *
     * <p>
     * This method invokes configured modules to validate the response.
     *
     * @param param an authentication parameter that encapsulates the client request and server response objects.
     *
     * @param subject the subject may be used by configured modules to store the Principals and credentials related to the
     * identity validated in the response.
     *
     * @param sharedState a Map for modules to save state across a sequence of calls from <code>secureRequest</code> to
     * <code>validateResponse</code> to <code>disposeSubject</code>.
     *
     * @exception AuthException if the operation failed.
     */
    void validateResponse(AuthParam param, Subject subject, Map sharedState) throws AuthException;

}
