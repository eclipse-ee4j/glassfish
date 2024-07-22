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

package com.sun.enterprise.security.ssl;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.security.KeyStore;
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509KeyManager;

import org.glassfish.grizzly.config.ssl.SSLContextFactory;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * @author Sudarsan Sridhar
 */
public class GlassFishSSLContextFactory extends SSLContextFactory {

    private static final Logger LOG = System.getLogger(GlassFishSSLContextFactory.class.getName());

    private final ServiceLocator locator;
    private SSLUtils sslUtils;

    public GlassFishSSLContextFactory(ServiceLocator locator) {
        this.locator = Objects.requireNonNull(locator, "locator");
    }


    @Override
    public SSLContext create() throws IOException {
        sslUtils = locator.getService(SSLUtils.class);
        return super.create();
    }


    @Override
    protected KeyManager[] getKeyManagers(String algorithm, String keyAlias) throws Exception {
        String keystoreFile = getAttribute("keystore");
        LOG.log(Level.DEBUG, "Keystore file = {0}", keystoreFile);

        String keystoreType = getAttribute("keystoreType");
        LOG.log(Level.DEBUG, "Keystore type = {0}", keystoreType);
        KeyManager[] kMgrs = sslUtils.getKeyManagers(algorithm);
        if (keyAlias != null && keyAlias.length() > 0 && kMgrs != null) {
            for (int i = 0; i < kMgrs.length; i++) {
                kMgrs[i] = new J2EEKeyManager((X509KeyManager) kMgrs[i], keyAlias);
            }
        }
        return kMgrs;
    }


    @Override
    protected KeyStore getTrustStore() throws IOException {
        return sslUtils.getTrustStore();
    }
}
