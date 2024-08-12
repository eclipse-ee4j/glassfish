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

package org.glassfish.admin.amx.util.stringifier;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import org.glassfish.admin.amx.util.StringUtil;

/**
 * Stringifies an X509CertificateStringifier.
 */
public final class X509CertificateStringifier implements Stringifier {

    public final static X509CertificateStringifier DEFAULT = new X509CertificateStringifier();

    public X509CertificateStringifier() {
    }


    private static byte[] getFingerprint(byte[] signature, String alg) {
        byte[] result = null;

        try {
            final MessageDigest md = MessageDigest.getInstance(alg);

            result = md.digest(signature);
        } catch (NoSuchAlgorithmException e) {
            result = signature;
            e.printStackTrace();
        }

        return (result);
    }


    /**
     * Static variant when direct call will suffice.
     */
    public static String stringify(final X509Certificate cert) {
        final StringBuffer buf = new StringBuffer();
        final String NL = "\n";

        buf.append("Issuer: " + cert.getIssuerDN().getName() + NL);
        buf.append("Issued to: " + cert.getSubjectX500Principal().getName() + NL);
        buf.append("Version: " + cert.getVersion() + NL);
        buf.append("Not valid before: " + cert.getNotBefore() + NL);
        buf.append("Not valid after: " + cert.getNotAfter() + NL);
        buf.append("Serial number: " + cert.getSerialNumber() + NL);
        buf.append("Signature algorithm: " + cert.getSigAlgName() + NL);
        buf.append("Signature algorithm OID: " + cert.getSigAlgOID() + NL);

        buf.append("Signature fingerprint (MD5): ");
        byte[] fingerprint = getFingerprint(cert.getSignature(), "MD5");
        buf.append(StringUtil.toHexString(fingerprint, ":") + NL);

        buf.append("Signature fingerprint (SHA1): ");
        fingerprint = getFingerprint(cert.getSignature(), "SHA1");
        buf.append(StringUtil.toHexString(fingerprint, ":") + NL);

        return (buf.toString());
    }


    @Override
    public String stringify(Object object) {
        return (stringify((X509Certificate) object));
    }
}
