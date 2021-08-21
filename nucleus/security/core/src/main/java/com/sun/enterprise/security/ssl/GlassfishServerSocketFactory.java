/*
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
import java.security.KeyStore;
import java.util.logging.Level;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;

import org.glassfish.grizzly.config.ssl.JSSE14SocketFactory;
import org.glassfish.internal.api.Globals;

/**
 *
 * @author Sudarsan Sridhar
 */
public class GlassfishServerSocketFactory extends JSSE14SocketFactory {

    private SSLUtils sslUtils;

    @Override
    protected KeyManager[] getKeyManagers(String algorithm, String keyAlias) throws Exception {
        if (sslUtils == null) {
            initSSLUtils();
        }
        String keystoreFile = (String) attributes.get("keystore");
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Keystore file= {0}", keystoreFile);
        }

        String keystoreType = (String) attributes.get("keystoreType");
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Keystore type= {0}", keystoreType);
        }
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
        if (sslUtils == null) {
            initSSLUtils();
        }
        return sslUtils.getTrustStore();
    }

    private void initSSLUtils() {
        if (sslUtils == null) {
            if (Globals.getDefaultHabitat() != null) {
                sslUtils = Globals.getDefaultHabitat().getService(SSLUtils.class);
            } else {
                sslUtils = new SSLUtils();
                sslUtils.postConstruct();
            }
        }
    }
}
