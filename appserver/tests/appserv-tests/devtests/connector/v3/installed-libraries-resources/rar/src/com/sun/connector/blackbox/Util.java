/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.blackbox;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.SecurityException;
import jakarta.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Set;

public class Util {
    static public PasswordCredential getPasswordCredential
            (final ManagedConnectionFactory mcf,
             final Subject subject, ConnectionRequestInfo info)
            throws ResourceException {

        if (subject == null) {
            if (info == null) {
                return null;
            } else {
                JdbcConnectionRequestInfo myinfo =
                        (JdbcConnectionRequestInfo) info;
                PasswordCredential pc =
                        new PasswordCredential(myinfo.getUser(),
                                myinfo.getPassword().toCharArray());
                pc.setManagedConnectionFactory(mcf);
                return pc;
            }
        } else {
            PasswordCredential pc =
                    (PasswordCredential) AccessController.doPrivileged
                            (new PrivilegedAction() {
                                public Object run() {
                                    Set creds = subject.getPrivateCredentials
                                            (PasswordCredential.class);
                                    Iterator iter = creds.iterator();
                                    while (iter.hasNext()) {
                                        PasswordCredential temp =
                                                (PasswordCredential) iter.next();
                                        if (temp.getManagedConnectionFactory().
                                                equals(mcf)) {
                                            return temp;
                                        }
                                    }
                                    return null;
                                }
                            });
            if (pc == null) {
                throw new SecurityException("No PasswordCredential found");
            } else {
                return pc;
            }
        }
    }

    static public boolean isEqual(String a, String b) {
        if (a == null) {
            return (b == null);
        } else {
            return a.equals(b);
        }
    }

    static public boolean isPasswordCredentialEqual(PasswordCredential a,
                                                    PasswordCredential b) {
        if (a == b) return true;
        if ((a == null) && (b != null)) return false;
        if ((a != null) && (b == null)) return false;
        if (!isEqual(a.getUserName(), b.getUserName())) return false;
        String p1 = null;
        String p2 = null;
        if (a.getPassword() != null) {
            p1 = new String(a.getPassword());
        }
        if (b.getPassword() != null) {
            p2 = new String(b.getPassword());
        }
        return (isEqual(p1, p2));
    }

}
