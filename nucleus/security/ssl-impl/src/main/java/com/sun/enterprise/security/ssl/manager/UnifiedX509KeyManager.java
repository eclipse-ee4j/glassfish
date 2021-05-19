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

package com.sun.enterprise.security.ssl.manager;

import java.net.Socket;
import java.util.ArrayList;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509KeyManager;

/**
 * This class combines an array of X509KeyManagers into one.
 * @author Shing Wai Chan
 **/
public class UnifiedX509KeyManager implements X509KeyManager /* extends X509ExtendedKeyManager*/ {
    private X509KeyManager[] mgrs = null;
    private String[] tokenNames = null;

    /**
     * @param mgrs
     * @param tokenNames Array of tokenNames with order corresponding to mgrs
     */
    public UnifiedX509KeyManager(X509KeyManager[] mgrs, String[] tokenNames) {
        if (mgrs == null || tokenNames == null) {
            throw new IllegalArgumentException("Null array of X509KeyManagers or tokenNames");
        }
        if (mgrs.length != tokenNames.length) {
            throw new IllegalArgumentException("Size of X509KeyManagers array and tokenNames array do not match.");
        }
        this.mgrs = mgrs;
        this.tokenNames = tokenNames;
    }

    // ---------- implements X509KeyManager ----------
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
            Socket socket) {
        String alias = null;
        for (int i = 0; i < mgrs.length; i++) {
            alias = mgrs[i].chooseClientAlias(keyType, issuers, socket);
            if (alias != null) {
                break;
            }
        }
        return alias;
    }

    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {
        String alias = null;
        for (int i = 0; i < mgrs.length; i++) {
            alias = mgrs[i].chooseServerAlias(keyType, issuers, socket);
            if (alias != null) {
                break;
            }
        }
        return alias;
    }

    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] chain = null;
        for (int i = 0; i < mgrs.length; i++) {
            chain = mgrs[i].getCertificateChain(alias);
            if (chain != null) {
                break;
            }
        }
        return chain;
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        ArrayList clientAliases = new ArrayList();
        for (int i = 0; i < mgrs.length; i++) {
            String[] clAliases = mgrs[i].getClientAliases(keyType, issuers);
            if (clAliases != null && clAliases.length > 0) {
                for (int j = 0; j < clAliases.length; j++) {
                    clientAliases.add(clAliases[j]);
                }
            }
        }

        return (clientAliases.size() == 0) ? null :
            (String[])clientAliases.toArray(new String[clientAliases.size()]);
    }

    public PrivateKey getPrivateKey(String alias) {
        PrivateKey privKey  = null;
        for (int i = 0; i < mgrs.length; i++) {
            privKey = mgrs[i].getPrivateKey(alias);
            if (privKey != null) {
                break;
            }
        }
        return privKey;
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        ArrayList serverAliases = new ArrayList();
        for (int i = 0; i < mgrs.length; i++) {
            String[] serAliases = mgrs[i].getServerAliases(keyType, issuers);
            if (serAliases != null && serAliases.length > 0) {
                for (int j = 0; j < serAliases.length; j++) {
                    serverAliases.add(serAliases[j]);
                }
            }
        }

        return (serverAliases.size() == 0) ? null :
            (String[])serverAliases.toArray(new String[serverAliases.size()]);
    }

    // ---------- end of implements X509KeyManager ----------

    public X509KeyManager[] getX509KeyManagers() {
        X509KeyManager[] kmgrs = new X509KeyManager[mgrs.length];
        System.arraycopy(mgrs, 0, kmgrs, 0, mgrs.length);
        return kmgrs;
    }

    public String[] getTokenNames() {
        String[] tokens = new String[tokenNames.length];
        System.arraycopy(tokenNames, 0, tokens, 0, tokenNames.length);
        return tokens;
    }


    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return chooseClientAlias(keyType, issuers, null);
    }

    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return chooseServerAlias(keyType, issuers,null);
    }
}
