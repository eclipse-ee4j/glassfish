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

import java.math.BigInteger;

import javax.security.auth.x500.X500Principal;

/**
 * Callback for private key and corresponding certificate chain.
 *
 * @version %I%, %G%
 */
public class PrivateKeyCallback extends jakarta.security.auth.message.callback.PrivateKeyCallback {

    /**
     * Marker interface for private key request types.
     */
    public interface Request extends jakarta.security.auth.message.callback.PrivateKeyCallback.Request {
    }

    /**
     * Request type for private keys that are identified via an alias.
     */
    public static class AliasRequest extends jakarta.security.auth.message.callback.PrivateKeyCallback.AliasRequest implements Request {

        /**
         * Construct an AliasRequest with an alias.
         *
         * <p>
         * The alias is used to directly identify the private key to be returned. The corresponding certificate chain for the
         * private key is also returned.
         *
         * <p>
         * If the alias is null, the handler of the callback relies on its own default.
         *
         * @param alias name identifier for the private key, or null.
         */
        public AliasRequest(String alias) {
            super(alias);
        }
    }

    /**
     * Request type for private keys that are identified via a SubjectKeyID
     */
    public static class SubjectKeyIDRequest extends jakarta.security.auth.message.callback.PrivateKeyCallback.SubjectKeyIDRequest
            implements Request {

        /**
         * Construct a SubjectKeyIDRequest with an subjectKeyID.
         *
         * <p>
         * The subjectKeyID is used to directly identify the private key to be returned. The corresponding certificate chain for
         * the private key is also returned.
         *
         * <p>
         * If the subjectKeyID is null, the handler of the callback relies on its own default.
         *
         * @param subjectKeyID identifier for the private key, or null.
         */
        public SubjectKeyIDRequest(byte[] subjectKeyID) {
            super(subjectKeyID);
        }
    }

    /**
     * Request type for private keys that are identified via an issuer/serial number.
     */
    public static class IssuerSerialNumRequest extends jakarta.security.auth.message.callback.PrivateKeyCallback.IssuerSerialNumRequest
            implements Request {

        /**
         * Constructs a IssuerSerialNumRequest with an issuer/serial number.
         *
         * <p>
         * The issuer/serial number are used to identify a public key certificate. The corresponding private key is returned in
         * the callback. The corresponding certificate chain for the private key is also returned.
         *
         * If the issuer/serialNumber parameters are null, the handler of the callback relies on its own defaults.
         *
         * @param issuer the X500Principal name of the certificate issuer, or null.
         *
         * @param serialNumber the serial number of the certificate, or null.
         */
        public IssuerSerialNumRequest(X500Principal issuer, BigInteger serialNumber) {
            super(issuer, serialNumber);
        }
    }

    /**
     * Constructs this PrivateKeyCallback with a private key Request object.
     *
     * <p>
     * The <i>request</i> object identifies the private key to be returned. The corresponding certificate chain for the
     * private key is also returned.
     *
     * <p>
     * If the <i>request</i> object is null, the handler of the callback relies on its own default.
     *
     * @param request identifier for the private key, or null.
     */
    public PrivateKeyCallback(Request request) {
        super(request);
    }

    /**
     * Get the Request object which identifies the private key to be returned.
     *
     * @return the Request object which identifies the private key to be returned, or null. If null, the handler of the
     * callback relies on its own default.
     */
    @Override
    public Request getRequest() {
        return (Request) super.getRequest();
    }
}
