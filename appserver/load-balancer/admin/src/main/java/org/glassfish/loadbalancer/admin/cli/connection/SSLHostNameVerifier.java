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

package org.glassfish.loadbalancer.admin.cli.connection;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

import org.glassfish.loadbalancer.admin.cli.LbLogUtil;

/**
 *
 * @author sv96363
 */
public class SSLHostNameVerifier implements HostnameVerifier {

    /**
     * matches the hostname of the Load balancer to CN attribute of the
     * certificate obtained.
     * @param hostname hostname of the load balancer
     * @param session  SSL session information
     * @return true - if the LB host name and CN attribute in the certificate
     * matches, false otherwise
     */
    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (session != null) {
            Certificate[] certs = null;
            try {
                certs = session.getPeerCertificates();
            } catch (Exception e) {
            }
            if (certs == null) {
                String msg = LbLogUtil.getStringManager().getString("NoPeerCert", hostname);
                LbLogUtil.getLogger().warning(msg);
                return false;
            }
            for (int i = 0; i < certs.length; i++) {
                if (certs[i] instanceof X509Certificate) {
                    X500Principal prin =
                            ((X509Certificate) certs[i]).getSubjectX500Principal();
                    String hName = null;
                    String dn = prin.getName();
                    // Look for name of the cert in the CN attribute
                    int cnIdx = dn.indexOf("CN=");
                    if (cnIdx != -1) {
                        String cnStr = dn.substring(cnIdx, dn.length());
                        int commaIdx = cnStr.indexOf(",");
                        // if the CN is the last element in the string, then
                        // there won't be a ',' after that.
                        // The principal could be either CN=chandu.sfbay,C=US
                        // or C=US,CN=chandu.sfbay
                        if (commaIdx == -1) {
                            commaIdx = dn.length();
                        }
                        hName = dn.substring(cnIdx + 3, commaIdx);
                    }
                    if (hostname.equals(hName)) {
                        return true;
                    }
                } else {
                    String msg = LbLogUtil.getStringManager().getString("NotX905Cert", hostname);
                    LbLogUtil.getLogger().warning(msg);
                }
            }
            // Now, try to match if it matches the hostname from the SSLSession
            if (hostname.equals(session.getPeerHost())) {
                return true;
            }
        }
        if (session != null) {
            String msg = LbLogUtil.getStringManager().getString("NotCertMatch",
                    hostname, new String(session.getId()));
            LbLogUtil.getLogger().warning(msg);
        }
        return false;
    }
}
