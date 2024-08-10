/*
 * Copyright (c) 1999, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;

import java.io.IOException;

import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Response Control for
 * paged-results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2696">RFC-2696</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     realSearchControlValue ::= SEQUENCE {
 *         size      INTEGER (0..maxInt),
 *                           -- requested page size from client
 *                           -- result set size estimate from server
 *         cookie    OCTET STRING
 *     }
 *
 * </pre>
 *
 * @see PagedResultsControl
 * @author Vincent Ryan
 */
final public class PagedResultsResponseControl extends BasicControl {

    /**
     * The paged-results response control's assigned object identifier
     * is 1.2.840.113556.1.4.319.
     */
    public static final String OID = "1.2.840.113556.1.4.319";

    /**
     * An estimate of the number of entries in the search result.
     *
     * @serial
     */
    private int resultSize;

    /**
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = new byte[0];

    private static final long serialVersionUID = 4004691067488246793L;

    /**
     * Constructs a paged-results response control.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public PagedResultsResponseControl(String id, boolean criticality,
        byte[] value) throws IOException {

        super(id, criticality, value);

        // decode value
        if ((value != null) && (value.length > 0)) {
            BerDecoder ber = new BerDecoder(value, 0, value.length);

            ber.parseSeq(null);
            resultSize = ber.parseInt();
            cookie = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
        }
    }

    /**
     * Retrieves (an estimate of) the number of entries in the search result.
     *
     * @return The number of entries in the search result, or zero if unknown.
     */
    public int getResultSize() {
        return resultSize;
    }

    /**
     * Retrieves the server-generated cookie. Null is returned when there are
     * no more entries for the server to return.
     *
     * @return A possibly null server-generated cookie.
     */
    public byte[] getCookie() {
        if (cookie.length == 0) {
            return null;
        } else {
            return cookie;
        }
    }
}
