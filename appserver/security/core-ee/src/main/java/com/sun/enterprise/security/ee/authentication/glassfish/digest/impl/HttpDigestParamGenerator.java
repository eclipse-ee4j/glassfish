/*
 * Copyright 2021 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import static com.sun.enterprise.security.auth.digest.api.Constants.A2;
import static com.sun.enterprise.security.auth.digest.api.Constants.CNONCE;
import static com.sun.enterprise.security.auth.digest.api.Constants.DATA;
import static com.sun.enterprise.security.auth.digest.api.Constants.METHOD;
import static com.sun.enterprise.security.auth.digest.api.Constants.NONCE;
import static com.sun.enterprise.security.auth.digest.api.Constants.NONCE_COUNT;
import static com.sun.enterprise.security.auth.digest.api.Constants.QOP;
import static com.sun.enterprise.security.auth.digest.api.Constants.RESPONSE;
import static com.sun.enterprise.security.auth.digest.api.Constants.URI;
import static java.util.logging.Level.SEVERE;

/**
 * HttpDigestParamGenerator consumes Authorization header from HttpServlet request and generates Digest parameter
 * objects to be used by Digest validators.
 *
 * @author K.Venugopal@sun.com
 */
public final class HttpDigestParamGenerator extends DigestParameterGenerator {

    private StringTokenizer commaTokenizer;
    private String userName;
    private String realmName;
    private String nOnce;
    private String nc;
    private String cnonce;
    private String qop;
    private String uri;
    private String response;
    private String method;
    private byte[] entityBody;
    private String algorithm = "MD5";
    private DigestAlgorithmParameter secret;
    private DigestAlgorithmParameter key;

    public HttpDigestParamGenerator() {
    }

    @Override
    public DigestAlgorithmParameter[] generateParameters(AlgorithmParameterSpec param) throws InvalidAlgorithmParameterException {
        ServletInputStream sis = null;

        HttpServletRequest request = null;
        if (!(param instanceof HttpAlgorithmParameterImpl)) {
            throw new InvalidAlgorithmParameterException(param.getClass().toString());
        }
        request = ((HttpAlgorithmParameterImpl) param).getValue();
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }

        if (!authorization.startsWith("Digest ")) {
            return null;
        }
        authorization = authorization.substring(7).trim();

        commaTokenizer = new StringTokenizer(authorization, ",");
        method = request.getMethod();

        while (commaTokenizer.hasMoreTokens()) {
            String currentToken = commaTokenizer.nextToken();
            int equalSign = currentToken.indexOf('=');
            if (equalSign < 0) {
                return null;
            }
            String currentTokenName = currentToken.substring(0, equalSign).trim();
            String currentTokenValue = currentToken.substring(equalSign + 1).trim();
            if ("username".equals(currentTokenName)) {
                userName = removeQuotes(currentTokenValue);
            } else if ("realm".equals(currentTokenName)) {
                realmName = removeQuotes(currentTokenValue, true);
            } else if ("nonce".equals(currentTokenName)) {
                nOnce = removeQuotes(currentTokenValue);
            } else if ("nc".equals(currentTokenName)) {
                nc = currentTokenValue;
            } else if ("cnonce".equals(currentTokenName)) {
                cnonce = removeQuotes(currentTokenValue);
            } else if ("qop".equals(currentTokenName)) {
                qop = removeQuotes(currentTokenValue);
            } else if ("uri".equals(currentTokenName)) {
                uri = removeQuotes(currentTokenValue);
            } else if ("response".equals(currentTokenName)) {
                response = removeQuotes(currentTokenValue);
            }
        }

        if (userName == null || realmName == null || nOnce == null || uri == null || response == null) {
            return null;
        }

        if (qop == null) {
            qop = "auth";
        }

        if ("auth-int".equals(qop)) {
            try {
                sis = request.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while (true) {
                    byte[] data = new byte[1024];
                    int len = sis.read(data, 0, 1023);
                    if (len == -1) {
                        break;
                    }
                    bos.write(data, 0, len);
                }
                entityBody = bos.toByteArray();
            } catch (IOException ex) {
                Logger.getLogger("global").log(SEVERE, null, ex);
            } finally {
                try {
                    sis.close();
                } catch (IOException ex) {
                    Logger.getLogger("global").log(SEVERE, null, ex);
                }
            }
        }

        key = getA1();
        DigestAlgorithmParameter a2 = getA2();
        DigestAlgorithmParameterImpl p1 = new DigestAlgorithmParameterImpl(NONCE, nOnce.getBytes());
        DigestAlgorithmParameter[] list = null;
        if ("auth-int".equals(qop) || "auth".equals(qop)) {
            DigestAlgorithmParameterImpl p2 = new DigestAlgorithmParameterImpl(NONCE_COUNT, nc.getBytes());
            DigestAlgorithmParameterImpl p3 = new DigestAlgorithmParameterImpl(CNONCE, cnonce.getBytes());
            DigestAlgorithmParameterImpl p4 = new DigestAlgorithmParameterImpl(QOP, qop.getBytes());
            list = new DigestAlgorithmParameter[5];
            list[0] = p1;
            list[1] = p2;
            list[2] = p3;
            list[3] = p4;
            list[4] = a2;
        } else {
            list = new DigestAlgorithmParameter[2];
            list[0] = p1;
            list[1] = a2;
        }
        secret = new DigestAlgorithmParameterImpl(RESPONSE, response.getBytes());
        DigestAlgorithmParameter[] data = new DigestAlgorithmParameter[3];
        data[0] = new NestedDigestAlgoParamImpl(DATA, list);
        data[1] = secret;
        data[2] = key;

        return data;
    }

    protected DigestAlgorithmParameter getA1() {
        return new KeyDigestAlgoParamImpl(algorithm, userName, realmName);
    }

    protected com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter getA2() {
        DigestAlgorithmParameterImpl p1 = new DigestAlgorithmParameterImpl(METHOD, method.getBytes());
        DigestAlgorithmParameterImpl p2 = new DigestAlgorithmParameterImpl(URI, uri.getBytes());
        if ("auth".equals(qop)) {
            DigestAlgorithmParameterImpl[] list = new DigestAlgorithmParameterImpl[2];
            list[0] = p1;
            list[1] = p2;
            NestedDigestAlgoParamImpl a2 = new NestedDigestAlgoParamImpl(algorithm, A2, list);
            return a2;
        }
        if ("auth-int".equals(qop)) {
            AlgorithmParameterSpec[] list = new AlgorithmParameterSpec[3];
            DigestAlgorithmParameterImpl p3 = new DigestAlgorithmParameterImpl("enity-body", algorithm, entityBody);
            list[0] = p1;
            list[1] = p2;
            list[2] = p3;
            NestedDigestAlgoParamImpl a2 = new NestedDigestAlgoParamImpl(algorithm, A2, list);
            return a2;
        }
        return null;
    }

    protected static String removeQuotes(String quotedString) {
        return removeQuotes(quotedString, false);
    }

    protected static String removeQuotes(String quotedString, boolean quotesRequired) {
        // support both quoted and non-quoted
        if (quotedString.length() > 0 && quotedString.charAt(0) != '"' && !quotesRequired) {
            return quotedString;
        }

        if (quotedString.length() > 2) {
            return quotedString.substring(1, quotedString.length() - 1);
        }

        return "";
    }
}
