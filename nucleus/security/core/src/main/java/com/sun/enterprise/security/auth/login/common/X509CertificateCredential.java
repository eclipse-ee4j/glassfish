/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.login.common;

import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * This class holds the user certificate for the certificate realm and the realm name. This credential is added as a public
 * credential to the JAAS subject.
 */

public class X509CertificateCredential {
    private X509Certificate[] certChain;
    private String realm;
    private String alias;

    /**
     * Construct a credential with the specified X509Certificate certificate chain, realm name and alias.
     *
     * @param the X509Certificate.
     * @param the alias for the certificate
     * @param the realm name. The only value supported for now is "certificate".
     */

    public X509CertificateCredential(X509Certificate[] certChain, String alias, String realm) {
        this.certChain = certChain;
        this.alias = alias;
        this.realm = realm;
    }

    /**
     * Return the alias for the certificate.
     *
     * @return the alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Return the realm name.
     *
     * @return the realm name. Only value supported for now is "certificate".
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Return the chain of certificates.
     *
     * @return the chain of X509Certificates.
     */
    public X509Certificate[] getX509CertificateChain() {
        return certChain;
    }

    /**
     * Compare two instances of the credential and return true if they are the same and false otherwise.
     *
     * @return true if the instances are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof X509CertificateCredential) {
            X509CertificateCredential pc = (X509CertificateCredential) o;
            if (pc.getRealm().equals(realm) && pc.getAlias().equals(alias)) {
                X509Certificate[] certs = pc.getX509CertificateChain();
                for (int i = 0; i < certs.length; i++) {
                    if (!certs[i].equals(certChain[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Return the hashCode computed from the certificate, realm and alias.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(certChain) + realm.hashCode() + ((alias != null) ? alias.hashCode() : 0);
    }

    /**
     * String representation of the credential.
     */
    @Override
    public String toString() {
        String s = "Realm=" + realm;
        s = s + " alias=" + alias;
        StringBuffer certChainStr = new StringBuffer("");
        for (int i = 0; i < certChain.length; i++) {
            certChainStr.append(certChain[i].toString());
            certChainStr.append("\n");
        }
        s = s + " X509Certificate=" + certChainStr.toString();
        return s;
    }

}
