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
import com.sun.jndi.ldap.BerEncoder;

import java.io.IOException;

import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Control for paged-results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2696.txt">RFC-2696</a>.
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
 * @see PagedResultsResponseControl
 * @author Vincent Ryan
 */
final public class PagedResultsControl extends BasicControl {

    /**
     * The paged-results control's assigned object identifier
     * is 1.2.840.113556.1.4.319.
     */
    public static final String OID = "1.2.840.113556.1.4.319";

    /**
     * The number of entries to return in a page.
     *
     * @serial
     */
    private int pageSize;

    /**
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = new byte[0];

    private static final long serialVersionUID = -8771840635877430549L;

    /**
     * Constructs a paged-results critical control.
     *
     * @param    pageSize    The number of entries to return in a page.
     * @exception IOException If a BER encoding error occurs.
     *
     */
    public PagedResultsControl(int pageSize) throws IOException {
        super(OID, true, null);
        this.pageSize = pageSize;
        super.value = setEncodedValue();
    }

    /**
     * Constructs a paged-results control.
     * <p>
     * A sequence of paged-results can be abandoned by setting the pageSize
     * to zero and setting the cookie to the last cookie received from the
     * server.
     *
     * @param    pageSize    The number of entries to return in a page.
     * @param    cookie        A server-generated cookie.
     * @param    criticality    The control's criticality setting.
     * @exception IOException If a BER encoding error occurs.
     */
    public PagedResultsControl(int pageSize, byte[] cookie,
        boolean criticality) throws IOException {

        super(OID, criticality, null);
        this.pageSize = pageSize;
        this.cookie = cookie;
        super.value = setEncodedValue();
    }

    /**
     * Sets the ASN.1 BER encoded value of the paged-results control.
     * The result is the raw BER bytes including the tag and length of
     * the control's value. It does not include the controls OID or criticality.
     *
     * @return A possibly null byte array representing the ASN.1 BER encoded
     *         value of the LDAP sort control.
     * @exception IOException If a BER encoding error occurs.
     */
    private byte[] setEncodedValue() throws IOException {

        // build the ASN.1 encoding
        BerEncoder ber = new BerEncoder(32);

        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
        ber.encodeInt(pageSize);
        ber.encodeOctetString(cookie, Ber.ASN_OCTET_STR);
        ber.endSeq();

        return ber.getTrimmedBuf();
    }
}
