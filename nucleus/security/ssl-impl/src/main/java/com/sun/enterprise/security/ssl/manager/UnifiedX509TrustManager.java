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

import java.util.HashSet;
import java.util.Iterator;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * This class combines an array of X509TrustManagers into one.
 * @author Shing Wai Chan
 **/
public class UnifiedX509TrustManager implements X509TrustManager {
    private X509TrustManager[] mgrs = null;
    private X509Certificate[] issuers = {};

    public UnifiedX509TrustManager(X509TrustManager[] mgrs) {
        if (mgrs == null) {
            throw new IllegalArgumentException("Null array of X509TrustManagers");
        }
        this.mgrs = mgrs;

        HashSet tset = new HashSet(); //for uniqueness
        for (int i = 0; i < mgrs.length; i++) {
            X509Certificate[] tcerts = mgrs[i].getAcceptedIssuers();
            if (tcerts != null && tcerts.length > 0) {
                for (int j = 0; j < tcerts.length; j++) {
                    tset.add(tcerts[j]);
                }
            }
        }
        issuers = new X509Certificate[tset.size()];
        Iterator iter = tset.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            issuers[i] = (X509Certificate)iter.next();
        }
    }

    // ---------- implements X509TrustManager -----------
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        CertificateException cex = null;
        for (int i = 0; i < mgrs.length; i++) {
            try {
                cex = null; //reset exception status
                mgrs[i].checkClientTrusted(chain, authType);
                break;
            } catch(CertificateException ex) {
                cex = ex;
            }
        }
        if (cex != null) {
            throw cex;
        }
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        CertificateException cex = null;
        for (int i = 0; i < mgrs.length; i++) {
            try {
                cex = null; //reset exception status
                mgrs[i].checkServerTrusted(chain, authType);
                break;
            } catch(CertificateException ex) {
                cex = ex;
            }
        }
        if (cex != null) {
            throw cex;
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return issuers;
    }
}
