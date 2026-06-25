/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
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
package com.sun.web.security.realmadapter.digest;

import com.sun.enterprise.security.AppCNonceCacheMap;
import com.sun.enterprise.security.CNonceCacheFactory;
import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;
import com.sun.enterprise.security.auth.digest.api.Key;
import com.sun.enterprise.security.auth.login.DigestCredentials;
import com.sun.enterprise.security.ee.authentication.glassfish.digest.impl.DigestParameterGenerator;
import com.sun.enterprise.security.ee.authentication.glassfish.digest.impl.HttpAlgorithmParameterImpl;
import com.sun.enterprise.security.ee.authentication.glassfish.digest.impl.NestedDigestAlgoParamImpl;

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;

import java.security.InvalidAlgorithmParameterException;
import java.util.logging.Logger;

import org.glassfish.security.common.CNonceCache;
import org.glassfish.security.common.NonceInfo;

import static com.sun.enterprise.security.auth.digest.api.Constants.A1;
import static com.sun.enterprise.security.ee.authentication.glassfish.digest.impl.DigestParameterGenerator.HTTP_DIGEST;
import static java.util.logging.Level.WARNING;

public class Digester {

    private static final Logger LOG = Logger.getLogger(Digester.class.getName());

    public Digester(String realmName, String appName, Provider<AppCNonceCacheMap> appCNonceCacheMapProvider, Provider<CNonceCacheFactory> cNonceCacheFactoryProvider) {
        this.realmName = realmName;
        this.appName = appName;
        this.appCNonceCacheMapProvider = appCNonceCacheMapProvider;
        this.cNonceCacheFactoryProvider = cNonceCacheFactoryProvider;
    }

    private String realmName;
    private String appName;

    private Provider<AppCNonceCacheMap> appCNonceCacheMapProvider;
    private Provider<CNonceCacheFactory> cNonceCacheFactoryProvider;

    private CNonceCacheFactory cNonceCacheFactory;
    private CNonceCache cnonces;
    private AppCNonceCacheMap haCNonceCacheMap;

    public DigestCredentials generateDigestCredentials(HttpServletRequest httpServletRequest) {
        try {
            DigestAlgorithmParameter[] digestParameters = generateDigestParameters(httpServletRequest);
            validateDigestParameters(digestParameters);

            Key key = findDigestKey(digestParameters);

            return new DigestCredentials(realmName, key.getUsername(), digestParameters);
        } catch (Exception le) {
            LOG.log(WARNING, "WEB9102: Web Login Failed", le);
        }

        return null;
    }

    private DigestAlgorithmParameter[] generateDigestParameters(HttpServletRequest httpServletRequest) throws InvalidAlgorithmParameterException {
        return DigestParameterGenerator
            .getInstance(HTTP_DIGEST)
            .generateParameters(new HttpAlgorithmParameterImpl(httpServletRequest));
    }

    private void validateDigestParameters(DigestAlgorithmParameter[] digestParameters) {
        if (cnonces == null) {
            synchronized (this) {
                if (haCNonceCacheMap == null) {
                    haCNonceCacheMap = appCNonceCacheMapProvider.get();
                }
                if (haCNonceCacheMap != null) {
                    // Get the initialized HA CNonceCache
                    cnonces = haCNonceCacheMap.get(appName);
                }

                if (cnonces == null) {
                    if (cNonceCacheFactory == null) {
                        cNonceCacheFactory = cNonceCacheFactoryProvider.get();
                    }
                    // create a Non-HA CNonce Cache
                    cnonces = cNonceCacheFactory.createCNonceCache(appName, null, null, null);
                }
            }

        }

        String cnonce = null;
        String nc = null;

        for (DigestAlgorithmParameter digestParameter : digestParameters) {
            if (digestParameter instanceof NestedDigestAlgoParamImpl) {
                NestedDigestAlgoParamImpl np = (NestedDigestAlgoParamImpl) digestParameter;

                DigestAlgorithmParameter[] nestedParameters = (DigestAlgorithmParameter[]) np.getNestedParams();
                for (DigestAlgorithmParameter nestedParameter : nestedParameters) {
                    if ("cnonce".equals(nestedParameter.getName())) {
                        cnonce = new String(nestedParameter.getValue());
                    } else if ("nc".equals(nestedParameter.getName())) {
                        nc = new String(nestedParameter.getValue());
                    }
                    if (cnonce != null && nc != null) {
                        break;
                    }
                }
                if (cnonce != null && nc != null) {
                    break;
                }
            }

            if ("cnonce".equals(digestParameter.getName())) {
                cnonce = new String(digestParameter.getValue());
            } else if ("nc".equals(digestParameter.getName())) {
                nc = new String(digestParameter.getValue());
            }
        }

        long currentTime = System.currentTimeMillis();
        long count = getCount(nc);

        NonceInfo nonceInfo;
        synchronized (cnonces) {
            nonceInfo = cnonces.get(cnonce);
        }
        if (nonceInfo == null) {
            nonceInfo = new NonceInfo();
        } else if (count <= nonceInfo.getCount()) {
            throw new RuntimeException("Invalid Request : Possible Replay Attack detected ?");
        }

        nonceInfo.setCount(count);
        nonceInfo.setTimestamp(currentTime);
        synchronized (cnonces) {
            cnonces.put(cnonce, nonceInfo);
        }
    }

    private long getCount(String nc) {
        try {
            return Long.parseLong(nc, 16);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }
    }

    private Key findDigestKey(DigestAlgorithmParameter[] digestParameters) {
        for (DigestAlgorithmParameter digestParameter : digestParameters) {
            if (A1.equals(digestParameter.getName()) && digestParameter instanceof Key) {
                return (Key) digestParameter;
            }
        }

        throw new RuntimeException("No key found in parameters");
    }

}
