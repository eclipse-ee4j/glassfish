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

import java.security.cert.CertStore;

/**
 * Callback for CertStore.
 *
 * <p>
 * A CertStore is a generic repository for certificates. CertStores may be searched to locate public key certificates,
 * as well as to put together certificate chains. Such a search may be necessary when the caller needs to verify a
 * signature.
 *
 * @version %I%, %G%
 */
public class CertStoreCallback extends jakarta.security.auth.message.callback.CertStoreCallback {

    /**
     * Set the CertStore.
     *
     * @param certStore the certificate store, which may be null If null, the requester is assumed to already have access to
     * the relevant certificate and/or chain.
     */
    public void setStore(CertStore certStore) {
        setCertStore(certStore);
    }
}
