/*
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

package com.sun.gjc.util;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.gjc.spi.ConnectionRequestInfoImpl;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.security.PasswordCredential;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

/**
 * SecurityUtils for Generic JDBC Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/07/22
 */
public class SecurityUtils {

    static private StringManager sm = StringManager.getManager(DataSourceObjectBuilder.class);

    /**
     * This method returns the <code>PasswordCredential</code> object, given the
     * <code>ManagedConnectionFactory</code>, subject and the
     * <code>ConnectionRequestInfo</code>. It first checks if the
     * <code>ConnectionRequestInfo</code> is null or not. If it is not null, it
     * constructs a <code>PasswordCredential</code> object with the user and
     * password fields from the <code>ConnectionRequestInfo</code> and returns this
     * <code>PasswordCredential</code> object. If the
     * <code>ConnectionRequestInfo</code> is null, it retrieves the
     * <code>PasswordCredential</code> objects from the <code>Subject</code>
     * parameter and returns the first <code>PasswordCredential</code> object which
     * contains a <code>ManagedConnectionFactory</code>, instance equivalent to the
     * <code>ManagedConnectionFactory</code>, parameter.
     *
     * @param mcf <code>ManagedConnectionFactory</code>
     * @param subject <code>Subject</code>
     * @param info <code>ConnectionRequestInfo</code>
     * @return <code>PasswordCredential</code>
     * @throws <code>ResourceException</code> generic exception if operation fails
     * @throws <code>SecurityException</code> if access to the <code>Subject</code>
     * instance is denied
     */
    public static PasswordCredential getPasswordCredential(final ManagedConnectionFactory mcf, final Subject subject,
            ConnectionRequestInfo info) throws ResourceException {

        if (info == null) {
            if (subject == null) {
                return null;
            } else {
                PasswordCredential pc = (PasswordCredential) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        Set passwdCredentialSet = subject.getPrivateCredentials(PasswordCredential.class);
                        Iterator iter = passwdCredentialSet.iterator();
                        while (iter.hasNext()) {
                            PasswordCredential temp = (PasswordCredential) iter.next();
                            if (temp.getManagedConnectionFactory().equals(mcf)) {
                                return temp;
                            }
                        }
                        return null;
                    }
                });
                if (pc == null) {
                    String msg = sm.getString("su.no_passwd_cred");
                    throw new jakarta.resource.spi.SecurityException(msg);
                } else {
                    return pc;
                }
            }
        } else {
            ConnectionRequestInfoImpl cxReqInfo = (ConnectionRequestInfoImpl) info;
            PasswordCredential pc = new PasswordCredential(cxReqInfo.getUser(), cxReqInfo.getPassword());
            pc.setManagedConnectionFactory(mcf);
            return pc;
        }
    }

    /**
     * Returns true if two strings are equal; false otherwise
     *
     * @param str1 <code>String</code>
     * @param str2 <code>String</code>
     * @return true if the two strings are equal false otherwise
     */
    static private boolean isEqual(String str1, String str2) {
        if (str1 == null) {
            return (str2 == null);
        }

        return str1.equals(str2);
    }

    /**
     * Returns true if two <code>PasswordCredential</code> objects are equal; false
     * otherwise
     *
     * @param pC1 <code>PasswordCredential</code>
     * @param pC2 <code>PasswordCredential</code>
     * @return true if the two PasswordCredentials are equal false otherwise
     */
    static public boolean isPasswordCredentialEqual(PasswordCredential pC1, PasswordCredential pC2) {
        if (pC1 == pC2)
            return true;
        if (pC1 == null || pC2 == null)
            return (pC1 == pC2);
        if (!isEqual(pC1.getUserName(), pC2.getUserName())) {
            return false;
        }

        return Arrays.equals(pC1.getPassword(), pC2.getPassword());
    }
}
