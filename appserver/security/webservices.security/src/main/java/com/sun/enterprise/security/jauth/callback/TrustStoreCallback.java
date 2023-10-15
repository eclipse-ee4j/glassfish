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

import java.security.KeyStore;

/**
 * Callback for trusted certificate KeyStore.
 *
 * <p>
 * A trusted certificate KeyStore may be used to determine whether a given certificate chain can be trusted.
 *
 * @version %I%, %G%
 */
public class TrustStoreCallback extends jakarta.security.auth.message.callback.TrustStoreCallback {

    /**
     * Set the trusted certificate KeyStore.
     *
     * @param trustStore the trusted certificate KeyStore, which must already be loaded.
     */
    public void setStore(KeyStore trustStore) {
        setTrustStore(trustStore);
    }
}
