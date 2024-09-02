/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.security.common;

import com.sun.enterprise.util.i18n.StringManager;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Util class for salted SHA processing.
 *
 * <P>Salted SHA (aka SSHA) is computed as follows:
 * <br> result = {SSHA}BASE64(SHA(password,salt),salt)
 *
 * <P>Methods are also provided to return partial results, such as
 * SHA( password , salt) without Base64 encoding.
 *
 */
public class SSHA
{
    private static final String SSHA_TAG = "{SSHA}";
    private static final String SSHA_256_TAG = "{SSHA256}";
    private static final String algoSHA = "SHA";
    private static final String algoSHA256 = "SHA-256";
    public static final String defaultAlgo = algoSHA256;

    //TODO V3 need to check if second arg is correct
    private static StringManager sm =
        StringManager.getManager(SSHA.class);


    /**
     * Compute a salted SHA hash.
     *
     * @param salt Salt bytes.
     * @param password Password bytes.
     * @return Byte array of length 20 bytes containing hash result.
     * @throws IllegalArgumentException Thrown if there is an error.
     *
     */
    public static byte[] compute(byte[] salt, byte[] password, String algo)
        throws IllegalArgumentException
    {

        byte[] buff = new byte[password.length + salt.length];
        System.arraycopy(password, 0, buff, 0, password.length);
        System.arraycopy(salt, 0, buff, password.length, salt.length);

        byte[] hash = null;

        boolean isSHA = false;
        if(algoSHA.equals(algo)) {
            isSHA = true;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algo);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        assert (md != null);
        md.reset();
        hash = md.digest(buff);

        if (!isSHA) {
            for (int i = 2; i <= 100; i++) {
                md.reset();
                md.update(hash);
                hash = md.digest();
            }
        }
        if (isSHA) {
            assert (hash.length == 20); // SHA output is 20 bytes
        }
        else {
            assert (hash.length == 32); //SHA-256 output is 32 bytes
        }
        return hash;
    }

    /**
     * Perform encoding of salt and computed hash.
     *
     * @param salt Salt bytes.
     * @param hash Result of prior compute() operation.
     * @return String Encoded string, as described in class documentation.
     *
     */
    public static String encode(byte[] salt, byte[] hash, String algo)
    {
        boolean isSHA = false;

        if (algoSHA.equals(algo)) {
            isSHA = true;
        }

        if (!isSHA) {
            assert (hash.length == 32);
        } else {
            assert (hash.length == 20);
        }

        int resultLength = 32;
        if (isSHA) {
            resultLength = 20;
        }

        byte[] res = new byte[resultLength+salt.length];
        System.arraycopy(hash, 0, res, 0, resultLength);
        System.arraycopy(salt, 0, res, resultLength, salt.length);

        String encoded = Base64.getEncoder().encodeToString(res);
        String out = SSHA_256_TAG + encoded;
        if(isSHA) {
            out = SSHA_TAG + encoded;
        }

        return out;
    }


    /**
     * Verifies a password.
     *
     * <P>The given password is verified against the provided encoded SSHA
     * result string.
     *
     * @param encoded Encoded SSHA value (e.g. output of computeAndEncode())
     * @param password Password bytes of the password to verify.
     * @returns True if given password matches encoded SSHA.
     * @throws IllegalArgumentException Thrown if there is an error.
     *
     */
    public static boolean verify(String encoded, byte[] password)
        throws IllegalArgumentException
    {
        byte[] hash = new byte[20];
        String algo = algoSHA256;
        if (encoded.startsWith(SSHA_TAG)) {
            algo = algoSHA;
        }
        byte[] salt = decode(encoded, hash, algo);
        return verify(salt, hash, password, algo);
    }


    /**
     * Verifies a password.
     *
     * <P>The given password is verified against the provided salt and hash
     * buffers.
     *
     * @param salt Salt bytes used in the hash result.
     * @param hash Hash result to compare against.
     * @param password Password bytes of the password to verify.
     * @returns True if given password matches encoded SSHA.
     * @throws IllegalArgumentException Thrown if there is an error.
     *
     */
    public static boolean verify(byte[] salt, byte[] hash, byte[] password, String algo)
        throws IllegalArgumentException
    {
        byte[] newHash = compute(salt, password, algo);
        return Arrays.equals(hash, newHash);
    }


    /**
     * Decodes an encoded SSHA string.
     *
     * @param encoded Encoded SSHA value (e.g. output of computeAndEncode())
     * @param hashResult A byte array which must contain 20 elements. Upon
     *      succesful return from method, it will be filled by the hash
     *      value decoded from the given SSHA string. Existing values are
     *      not used and will be overwritten.
     * @returns Byte array containing the salt obtained from the encoded SSHA
     *      string.
     * @throws IllegalArgumentException Thrown if there is an error.
     *
     */
    public static byte[] decode(String encoded, byte[] hashResult, String algo)
        throws IllegalArgumentException
    {
         boolean isSHA = false;

        if(algoSHA.equals(algo)) {
            isSHA = true;
        }

        if(isSHA) {
            assert (hashResult.length==20);
        }
        else {
            assert (hashResult.length == 32);
        }

        if (!encoded.startsWith(SSHA_TAG) && !encoded.startsWith(SSHA_256_TAG)) {
            String msg = sm.getString("ssha.badformat", encoded);
            throw new IllegalArgumentException(msg);
        }

        String ssha = encoded.substring(SSHA_256_TAG.length());
        if (isSHA) {
            ssha = encoded.substring(SSHA_TAG.length());
        }

        byte[] result = Base64.getDecoder().decode(ssha);
        int resultLength = 32;
        if (isSHA) {
            resultLength = 20;
        }
        assert (result.length > resultLength);

        byte[] salt = new byte[result.length - resultLength];

        System.arraycopy(result, 0, hashResult, 0, resultLength);
        System.arraycopy(result, resultLength, salt, 0, result.length-resultLength);

        return salt;
    }




}
