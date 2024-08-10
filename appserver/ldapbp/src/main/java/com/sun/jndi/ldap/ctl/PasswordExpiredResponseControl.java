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

package com.sun.jndi.ldap.ctl;

import java.io.IOException;

import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAP response control for password expired
 * notification. The password expired control is received if password
 * needs to be changed when the user logs into newly created account.
 * The control is also received if the user needs to change the password
 * upon its reset.
 * <p>
 * This control should be checked whenever a LDAP bind operation is
 * performed as a result of operations on the context such as when a new
 * initial context is created or when {@link javax.naming.ldap.InitialLdapContext#reconnect(javax.naming.ldap.Control[]) InitialLdapContext.reconnect}
 * is called.
 * <p>
 * Note that if the password is not changed when the control is received
 * during the creation of the context, or after reconnecting, the subsequent
 * LDAP operations on the context will fail and the PasswordExpired control is
 * received.
 * <p>
 * The Password Expired control is defined in <tt>draft-vchu-ldap-pwd-policy-00.txt</tt>
 * <p>
 * The object identifier for Password Expired control is 2.16.840.1.113730.3.4.4
 * and the control has no value.
 * <p>
 *
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *     retrieveControls(ctx);
 *
 *     try {
 *         // Do some operations on the context
 *         ctx.lookup("");
 *     } catch (javax.naming.OperationNotSupportedException e) {
 *         retrieveControls(ctx);
 *     }
 *
 *
 *    public static void printControls(DirContext ctx)
 *        Control[] respControls;
 *
 *        // retrieve response controls
 *        if ((respControls = ctx.getResponseControls()) != null) {
 *            for (int i = 0; i < respControls.length; i++) {
 *
 *                // locate the password expired control
 *          if (respControls[i] instanceof PasswordExpiredResponseControl) {
 *                  System.out.println("Password has expired," +
 *                " please change the password");
 *              }
 *      }
 *    }
 *
 * </pre>
 *
 * @see PasswordExpiringResponseControl
 * @author Vincent Ryan
 */
public class PasswordExpiredResponseControl extends BasicControl {

    private static final long serialVersionUID = -4568118365564432308L;

    /**
     * The password expired control's assigned object identifier is
     * 2.16.840.1.113730.3.4.4.
     */
    public static final String OID = "2.16.840.1.113730.3.4.4";

    /**
     * Constructs a control to notify of password expiration.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    PasswordExpiredResponseControl(String id, boolean criticality,
        byte[] value) {

        super(id, criticality, null);
    }
}
