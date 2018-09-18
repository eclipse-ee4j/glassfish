/*
 * Copyright (c) 2000, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.jndi.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Request Control for Tree Delete as
 * defined in <tt>draft-armijo-ldap-treedelete-02.txt</tt>
 *
 * The control has no control value.
 *
 * @author Vincent Ryan
 */
final public class TreeDeleteControl extends BasicControl {

    /**
     * The tree delete control's assigned object identifier
     * is 1.2.840.113556.1.4.805.
     */
    public static final String OID = "1.2.840.113556.1.4.805";

    private static final long serialVersionUID = 1278332007778853814L;

    /**
     * Constructs a tree delete critical control.
     */
    public TreeDeleteControl() {
	super(OID, true, null);
    }

    /**
     * Constructs a tree delete control.
     *
     * @param	criticality The control's criticality setting.
     */
    public TreeDeleteControl(boolean criticality) {
	super(OID, criticality, null);
    }
}
