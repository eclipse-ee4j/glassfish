/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.jauth.callback;

/**
 * Callback for private key and corresponding certificate chain.
 *
 * @version %I%, %G%
 */
public class SecretKeyCallback extends jakarta.security.auth.message.callback.SecretKeyCallback {

    /**
     * Marker interface for private key request types.
     */
    public interface Request extends jakarta.security.auth.message.callback.SecretKeyCallback.Request {
    }

    /**
     * Request type for secret keys that are identified via an alias.
     */
    public static class AliasRequest extends jakarta.security.auth.message.callback.SecretKeyCallback.AliasRequest implements Request {

        /**
         * Construct an AliasRequest with an alias.
         *
         * <p>
         * The alias is used to directly identify the secret key to be returned.
         *
         * <p>
         * If the alias is null, the handler of the callback relies on its own default.
         *
         * @param alias name identifier for the secret key, or null.
         */
        public AliasRequest(String alias) {
            super(alias);
        }
    }

    /**
     * Constructs this SecretKeyCallback with a secret key Request object.
     *
     * <p>
     * The <i>request</i> object identifies the secret key to be returned.
     *
     * If the alias is null, the handler of the callback relies on its own default.
     *
     * @param request request object identifying the secret key, or null.
     */
    public SecretKeyCallback(Request request) {
        super(request);
    }

    /**
     * Get the Request object which identifies the secret key to be returned.
     *
     * @return the Request object which identifies the private key to be returned, or null. If null, the handler of the
     * callback relies on its own deafult.
     */
    @Override
    public Request getRequest() {
        return (Request) super.getRequest();
    }
}
