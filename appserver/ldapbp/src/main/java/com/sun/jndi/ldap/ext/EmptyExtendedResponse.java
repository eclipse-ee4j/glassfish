/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.ext;

import javax.naming.ldap.ExtendedResponse;

/**
  * This class represents an LDAP extended operation response with
  * an OID and empty response value. The response comprises an optional
  * object identifier and an optional ASN.1 BER encoded value.
  * For extended responses which do not have value to return,
  * this class can be used.
  *<p>
  * @see javax.naming.ldap.ExtendedResponse
  * @see javax.naming.ldap.ExtendedRequest
  */

class EmptyExtendedResponse implements ExtendedResponse {

    /**
     * OID of the extended response
     * @serial
     */
    private String oid;

    private static final long serialVersionUID = -6096832546823615936L;

    EmptyExtendedResponse(String oid) {
        this.oid = oid;
    }

    /**
     * Retrieves the object identifier of the response.
     * The LDAP protocol specifies that the response object identifier is
     * optional.
     * If the server does not send it, the response will contain no ID
     * (i.e. null).
     *
     * @return    A possibly null object identifier string representing the LDAP
     *         <tt>ExtendedResponse.responseName</tt> component.
     */
    @Override
    public String getID() {
        return oid;
    }

    /**
     * Since the response has no defined value, null is always
     * returned.
     *
     * @return The null value.
     */
    @Override
    public byte[] getEncodedValue() {
        return null;
    }
}
