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

import javax.security.auth.callback.Callback;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;

/**
 * Callback for Kerberos Key.
 *
 * @version 1.4, 03/03/04
 */
public class KerberosKeyCallback implements Callback {

    private KerberosPrincipal owner;
    private KerberosKey key;

    /**
     * Constructs this KerberosSubjectCallback with a KerberosPrincipal.
     *
     * <p>
     * The <i>owner</i> input parameter specifies the owner of the KerberosKey to be returned.
     *
     * @param owner the owner of the KerberosKey to be returned
     */
    public KerberosKeyCallback(KerberosPrincipal owner) {
        this.owner = owner;
    }

    /**
     * Get the owner.
     *
     * @return the owner
     */
    public KerberosPrincipal getOwner() {
        return owner;
    }

    /**
     * Set the requested Kerberos key.
     *
     * @param key the Kerberos key
     */
    public void setKey(KerberosKey key) {
        this.key = key;
    }

    /**
     * Get the requested Kerberos key.
     *
     * @return the Kerberos key
     */
    public KerberosKey getKey() {
        return key;
    }
}
