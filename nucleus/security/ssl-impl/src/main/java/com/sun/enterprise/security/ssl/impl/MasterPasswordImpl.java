/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ssl.impl;

import com.sun.enterprise.security.store.IdentityManagement;
import com.sun.enterprise.security.store.PasswordAdapter;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.glassfish.security.common.MasterPassword;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * A contract to pass the Glassfish master password between the admin module and the security module.
 *
 * @author Sudarsan Sridhar
 */
@Service(name = "Security SSL Password Provider Service")
@Singleton
public class MasterPasswordImpl implements MasterPassword {

    @Inject
    @Optional
    private IdentityManagement idm;

    @Override
    public PasswordAdapter getMasterPasswordAdapter()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        char[] pw = idm == null ? null : idm.getMasterPassword();
        return new PasswordAdapter(pw);
    }


    @Override
    public char[] getMasterPassword() {
        return idm == null ? null : idm.getMasterPassword();
    }
}
