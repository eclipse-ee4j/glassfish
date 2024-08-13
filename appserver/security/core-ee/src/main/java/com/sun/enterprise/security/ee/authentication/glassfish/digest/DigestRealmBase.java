/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.ee.authentication.glassfish.digest;

import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;
import com.sun.enterprise.security.auth.digest.api.Key;
import com.sun.enterprise.security.auth.digest.api.Password;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.ee.authentication.glassfish.digest.impl.DigestProcessor;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

import static com.sun.enterprise.security.auth.digest.api.Constants.A1;
import static com.sun.enterprise.security.auth.digest.api.Constants.RESPONSE;
import static java.util.logging.Level.SEVERE;

/**
 * Base class for all realms wanting to support Digest based authentication.
 *
 * @author K.Venugopal@sun.com
 */
public abstract class DigestRealmBase extends Realm implements DigestRealm {

    public DigestRealmBase() {
    }

    protected boolean validate(final Password passwd, DigestAlgorithmParameter[] params) {
        try {
            return new DigestValidatorImpl().validate(passwd, params);
        } catch (NoSuchAlgorithmException ex) {
            _logger.log(Level.SEVERE, "invalid.digest.algo", ex);
        }
        return false;
    }

    private static class DigestValidatorImpl extends DigestProcessor {

        private DigestAlgorithmParameter data;
        private DigestAlgorithmParameter clientResponse;
        private DigestAlgorithmParameter key;
        private final String algorithm = "MD5";

        DigestValidatorImpl() {

        }

        @Override
        protected final boolean validate(Password passwd, DigestAlgorithmParameter[] params) throws NoSuchAlgorithmException {

            for (DigestAlgorithmParameter dap : params) {
                if (A1.equals(dap.getName()) && dap instanceof Key) {
                    key = dap;
                } else if (RESPONSE.equals(dap.getName())) {
                    clientResponse = dap;
                } else {
                    data = dap;
                }
            }
            setPassword(passwd);

            try {
                byte[] p1 = valueOf(key);
                byte[] p2 = valueOf(data);
                java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                bos.write(p1);
                bos.write(":".getBytes());
                bos.write(p2);

                MessageDigest md = MessageDigest.getInstance(algorithm);
                byte[] derivedKey = null;
                byte[] dk = md.digest(bos.toByteArray());
                String tmp = encode(dk);
                // new MD5Encoder().encode(dk);
                derivedKey = tmp.getBytes();
                byte[] suppliedKey = clientResponse.getValue();
                boolean result = true;
                if (derivedKey.length == suppliedKey.length) {
                    for (int i = 0; i < derivedKey.length; i++) {
                        if (!(derivedKey[i] == suppliedKey[i])) {
                            result = false;
                            break;
                        }
                    }
                } else {
                    result = false;
                }
                return result;
            } catch (IOException ex) {
                Object[] msg = new String[1];
                msg[0] = ex.getMessage();
                _logger.log(SEVERE, "digest.error", msg);
            }

            return false;
        }
    }
}
