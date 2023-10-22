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

package com.sun.enterprise.security.ee.authentication.glassfish.digest.impl;

import static com.sun.enterprise.security.auth.digest.api.Constants.A1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;
import com.sun.enterprise.security.auth.digest.api.NestedDigestAlgoParam;
import com.sun.enterprise.security.auth.digest.api.Password;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

/**
 * supports creation and validation of digest.
 *
 * @author K.Venugopal@sun.com
 */
public abstract class DigestProcessor {

    public DigestProcessor() {
    }

    private Password passwd;

    private Logger _logger = LogDomains.getLogger(DigestProcessor.class, LogDomains.SECURITY_LOGGER);
    private static final StringManager sm = StringManager.getManager(DigestProcessor.class);
    private static final char[] hexadecimal = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private final MD5Encoder md5Encoder = new MD5Encoder();

    /**
     *
     *
     * @param passwd password to be used for digest calculation.
     * @param params digest parameter
     * @throws java.security.NoSuchAlgorithmException
     * @return
     */

    public String createDigest(Password passwd, DigestAlgorithmParameter[] params) throws NoSuchAlgorithmException {
        try {
            com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter data = null;
            com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter clientResponse = null;
            com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter key = null;
            this.passwd = passwd;
            for (DigestAlgorithmParameter dap : params) {
                if (A1.equals(dap.getName()) && dap instanceof com.sun.enterprise.security.auth.digest.api.Key) {
                    key = dap;
                } else {
                    data = dap;
                }
            }
            byte[] p1 = valueOf(key);
            byte[] p2 = valueOf(data);
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            bos.write(p1);
            bos.write(":".getBytes());
            bos.write(p2);

            java.security.MessageDigest md = java.security.MessageDigest.getInstance(key.getAlgorithm());
            byte[] derivedKey = null;
            byte[] dk = md.digest(bos.toByteArray());
            java.lang.String tmp = getMd5Encoder().encode(dk);
            // new MD5Encoder().encode(dk);
            return tmp;
        } catch (IOException ex) {
            Object[] parm = new String[1];
            parm[1] = ex.getMessage();
            _logger.log(Level.SEVERE, "create.digest.error", parm);
            _logger.log(Level.FINE, "", ex);
        }
        return null;
    }

    /**
     *
     * @param passwd
     * @param params
     * @throws java.security.NoSuchAlgorithmException
     * @return
     */
    protected abstract boolean validate(Password passwd, DigestAlgorithmParameter[] params) throws NoSuchAlgorithmException;

    /**
     *
     * @param param
     * @throws java.security.NoSuchAlgorithmException
     * @return
     */
    protected final byte[] valueOf(DigestAlgorithmParameter param) throws NoSuchAlgorithmException {
        if (param instanceof KeyDigestAlgoParamImpl) {
            return valueOf((KeyDigestAlgoParamImpl) param);
        }
        if (param instanceof NestedDigestAlgoParam) {
            return valueOf((NestedDigestAlgoParam) param);
        }
        if (param.getAlgorithm() == null || param.getAlgorithm().length() == 0) {
            return param.getValue();
        }
        MessageDigest md = MessageDigest.getInstance(param.getAlgorithm());
        md.update(param.getValue());
        byte[] dk = md.digest();
        String tmp = getMd5Encoder().encode(dk);
        // new MD5Encoder().encode(dk);
        return tmp.getBytes();
    }

    /**
     *
     * @param passwd
     */
    protected void setPassword(Password passwd) {
        this.passwd = passwd;
    }

    private byte[] valueOf(KeyDigestAlgoParamImpl param) throws java.security.NoSuchAlgorithmException {

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();

        if (passwd.getType() != Password.PLAIN_TEXT) {
            return passwd.getValue();
        }
        try {
            bos.write(param.getUsername().getBytes());
            bos.write(param.getDelimiter());
            bos.write(param.getRealmName().getBytes());
            bos.write(param.getDelimiter());
            bos.write(passwd.getValue());
            MessageDigest md = MessageDigest.getInstance(param.getAlgorithm());
            byte[] dk = md.digest(bos.toByteArray());
            String tmp = getMd5Encoder().encode(dk);
            // new MD5Encoder().encode(dk);
            return tmp.getBytes();
        } catch (IOException ex) {
            _logger.log(java.util.logging.Level.SEVERE, "digest.param.error", ex);
        }

        return null;
    }

    private byte[] valueOf(NestedDigestAlgoParam param) throws NoSuchAlgorithmException {

        ByteArrayOutputStream bos = null;
        AlgorithmParameterSpec[] datastore = param.getNestedParams();
        bos = new ByteArrayOutputStream();
        for (int i = 0; i < datastore.length; i++) {
            DigestAlgorithmParameter dataP = (DigestAlgorithmParameter) datastore[i];
            byte[] tmpData = valueOf(dataP);
            bos.write(tmpData, 0, tmpData.length);
            if (param.getDelimiter() != null && i + 1 < datastore.length) {
                bos.write(param.getDelimiter(), 0, param.getDelimiter().length);
            }
        }
        if (hasAlgorithm(param)) {
            MessageDigest md = MessageDigest.getInstance(param.getAlgorithm());
            byte[] dk = md.digest(bos.toByteArray());
            String tmp = getMd5Encoder().encode(dk);
            // new MD5Encoder().encode(dk);
            return tmp.getBytes();
        }
        return bos.toByteArray();
    }

    public MD5Encoder getMd5Encoder() {
        return md5Encoder;
    }

    public String encode(byte[] dk) {
        return getMd5Encoder().encode(dk);
    }

    private boolean hasAlgorithm(DigestAlgorithmParameter param) {
        if (param.getAlgorithm() == null || param.getAlgorithm().length() == 0) {
            return false;
        }
        return true;
    }

    static class MD5Encoder {

        /**
         * Encodes the 128 bit (16 bytes) MD5 into a 32 character String.
         *
         * @param binaryData Array containing the digest
         * @return Encoded MD5, or null if encoding failed
         */
        public String encode(byte[] binaryData) {

            if (binaryData.length != 16) {
                return null;
            }

            char[] buffer = new char[32];

            for (int i = 0; i < 16; i++) {
                int low = binaryData[i] & 0x0f;
                int high = (binaryData[i] & 0xf0) >> 4;
                buffer[i * 2] = hexadecimal[high];
                buffer[i * 2 + 1] = hexadecimal[low];
            }

            return new String(buffer);

        }
    }
}
