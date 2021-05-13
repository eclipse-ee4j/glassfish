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

import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAP request control for authorization identity
 * control. This control is used to request that the server return the
 * authorization identity (in the LDAP bind response) resulting from the
 * accompanying LDAP bind operation. It is a <em>connection request control</em>
 * as described in {@link javax.naming.ldap.InitialLdapContext InitialLdapContext}
 * <p>
 * The Authorization Identity Bind Control is defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-weltman-ldapv3-auth-response-08.txt">draft-weltman-ldapv3-auth-response-08</a>.
 * <p>
 * The object identifier used for Authorization Identity control is
 * 2.16.840.1.113730.3.4.16 and the control has no value.
 * <p>
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an authorization identity bind control
 *     Control[] reqControls = new Control[]{
 *         new AuthorizationIDControl()
 *     };
 *
 *     // create an initial context using the supplied environment properties
 *     // and the supplied control
 *     LdapContext ctx = new InitialLdapContext(env, reqControls);
 *     Control[] respControls;
 *
 *     // retrieve response controls
 *     if ((respControls = ctx.getResponseControls()) != null) {
 *         for (int i = 0; i < respControls.length; i++) {
 *
 *             // locate the authorization identity response control
 *             if (respControls[i] instanceof AuthorizationIDResponseControl) {
 *                 System.out.println("My identity is " +
 *                     ((AuthorizationIDResponseControl) respControls[i])
 *                         .getAuthorizationID());
 *             }
 *         }
 *     }
 *
 * </pre>
 *
 * @see AuthorizationIDResponseControl
 * @see com.sun.jndi.ldap.ext.WhoAmIRequest
 * @author Vincent Ryan
 */
public class AuthorizationIDControl extends BasicControl {

    /**
     * The authorization identity control's assigned object identifier is
     * 2.16.840.1.113730.3.4.16.
     */
    public static final String OID = "2.16.840.1.113730.3.4.16";

    private static final long serialVersionUID = 2851964666449637092L;

    /**
     * Constructs a control to request the authorization identity.
     *
     * @param criticality The control's criticality setting.
     */
    public AuthorizationIDControl(boolean criticality) {
        super(OID, criticality, null);
    }
}
