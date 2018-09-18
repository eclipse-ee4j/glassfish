/*
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

package org.glassfish.admin.amxtest;

import com.sun.appserv.management.client.TrustStoreTrustManager;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;


public final class TestClientTrustStoreTrustManager
        extends TrustStoreTrustManager {
    public TestClientTrustStoreTrustManager() {
        this(new File("./TestClient-TrustStore"), "changeme".toCharArray());
    }

    public TestClientTrustStoreTrustManager(
            final File trustStore,
            final char[] password) {
        super(trustStore, password);
    }

    protected boolean
    shouldAddToTrustStore(final Certificate c) {
        // we are testing; don't bother the user
        return (true);
    }

    protected void
    addCertificateToTrustStore(
            final String alias,
            final Certificate c)
            throws IOException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException {
        super.addCertificateToTrustStore(alias, c);
        System.out.println("added certificate to truststore: " + c);
    }
}
