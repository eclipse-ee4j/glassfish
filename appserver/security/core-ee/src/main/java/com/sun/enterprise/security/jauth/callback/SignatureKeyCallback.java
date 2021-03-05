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

import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.x500.X500Principal;

/**
 * Callback for Signing Key.
 *
 * @version 1.8, 03/03/04
 */
public class SignatureKeyCallback implements Callback {

    private PrivateKey key;
    private X500Principal authority;
    private Certificate[] chain;

    /**
     * Constructs this SignatureKeyCallback with an authority.
     *
     * <p>
     * Both a PrivateKey and corresponding certificate chain will be returned. The <i>authority</i> input parameter
     * specifies the X500Principal name of the root CA certificate returned in the chain. An authority does not have to be
     * specified.
     *
     * @param authority the X500Principal name of the root CA certificate returned in the requested chain, or null
     */
    public SignatureKeyCallback(X500Principal authority) {
        this.authority = authority;
    }

    /**
     * Get the authority.
     *
     * @return the authority, or null
     */
    public X500Principal getAuthority() {
        return authority;
    }

    /**
     * Set the requested signing key.
     *
     * @param key the signing key
     * @param chain the corresponding certificate chain
     */
    public void setKey(PrivateKey key, Certificate[] chain) {
        this.key = key;
        this.chain = chain;
    }

    /**
     * Get the requested signing key.
     *
     * @return the signing key
     */
    public PrivateKey getKey() {
        return key;
    }

    /**
     * Get the certificate chain.
     *
     * @return the certificate chain
     */
    public Certificate[] getChain() {
        return chain;
    }
}
