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

package com.sun.s1asdev.security.certrealm.lm;

import com.sun.appserv.security.AppservCertificateLoginModule;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.security.auth.login.LoginException;

/**
 *
 * @author nasradu8
 */
public class CertificateLM extends AppservCertificateLoginModule {

    @Override
    protected void authenticateUser() throws LoginException {
        // Get the distinguished name from the X500Principal.
        String dname = getX500Principal().getName();
        StringTokenizer st = new StringTokenizer(dname, " \t\n\r\f,");
        _logger.log(Level.INFO, "Appname: " + getAppName() + " accessed by " + getX500Principal().getName());
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            // At this point, one has the application name and the DN of
            // the certificate. A suitable login decision can be made here.
            if (next.startsWith("CN=")) {
                String cname = next.substring(3);
                if (cname.equals("SSLTest")){
                        commitUserAuthentication(new String[]{getAppName() + ":alice-group"});
                        return;
                }
            }
        }
        throw new LoginException("No OU found.");
    }
}
