/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.common;

import com.sun.enterprise.security.store.PasswordAdapter;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.jvnet.hk2.annotations.Contract;

/**
 * A contract to pass the Glassfish master password between the admin module and
 * the security module.
 *
 * @author Sudarsan Sridhar
 */
@Contract
public interface MasterPassword {

    /**
     * Create and return PasswordAdapter using the master password.
     *
     * @return PasswordAdapter using the master password. Never null.
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     */
    PasswordAdapter getMasterPasswordAdapter()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException;

    /**
     * @return null or master password.
     */
    char[] getMasterPassword();
}
