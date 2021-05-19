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

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.LdapCtx;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Response Control for server-side sorting
 * of search results as defined in
 * <a href="http://www.ietf.org/rfc/rfc2891.txt">RFC-2891</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     SortResult ::= SEQUENCE {
 *        sortResult  ENUMERATED {
 *            success                   (0), -- results are sorted
 *            operationsError           (1), -- server internal failure
 *            timeLimitExceeded         (3), -- timelimit reached before
 *                                           -- sorting was completed
 *            strongAuthRequired        (8), -- refused to return sorted
 *                                           -- results via insecure
 *                                           -- protocol
 *            adminLimitExceeded       (11), -- too many matching entries
 *                                           -- for the server to sort
 *            noSuchAttribute          (16), -- unrecognized attribute
 *                                           -- type in sort key
 *            inappropriateMatching    (18), -- unrecognized or inappro-
 *                                           -- priate matching rule in
 *                                           -- sort key
 *            insufficientAccessRights (50), -- refused to return sorted
 *                                           -- results to this client
 *            busy                     (51), -- too busy to process
 *            unwillingToPerform       (53), -- unable to sort
 *            other                    (80)
 *            },
 *      attributeType [0] AttributeType OPTIONAL }
 *
 * </pre>
 *
 * @see SortControl
 * @author Vincent Ryan
 */
final public class SortResponseControl extends BasicControl {

    /**
     * The server-side sort response control's assigned object identifier
     * is 1.2.840.113556.1.4.474.
     */
    public static final String OID = "1.2.840.113556.1.4.474";

    /**
     * The sort result code.
     *
     * @serial
     */
    private int resultCode = 0;

    /**
     * The ID of the attribute that caused the sort to fail.
     *
     * @serial
     */
    private String badAttrId = null;

    private static final long serialVersionUID = -3732673799687266442L;

    /**
     * Constructs a new instance of SortResponseControl.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public SortResponseControl(String id, boolean criticality, byte[] value)
        throws IOException {

        super(id, criticality, value);

        // decode value
        if ((value != null) && (value.length > 0)) {
            BerDecoder ber = new BerDecoder(value, 0, value.length);

            ber.parseSeq(null);
            resultCode = ber.parseEnumeration();
            if ((ber.bytesLeft() > 0) && (ber.peekByte() == Ber.ASN_CONTEXT)) {
                badAttrId = ber.parseStringWithTag(Ber.ASN_CONTEXT, true, null);
            }
        }
    }

    /**
     * Determines if the search results have been successfully sorted.
     * If an error occurred during sorting a NamingException is thrown.
     *
     * @return    true if the search results have been sorted.
     */
    public boolean isSorted() {
        return (resultCode == 0); // a result code of zero indicates success
    }

    /**
     * Retrieves the LDAP result code of the sort operation.
     *
     * @return    The result code. A zero value indicates success.
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * Retrieves the ID of the attribute that caused the sort to fail.
     * Returns null if no ID was returned by the server.
     *
     * @return The possibly null ID of the bad attribute.
     */
    public String getAttributeID() {
        return badAttrId;
    }

    /**
     * Retrieves the NamingException appropriate for the result code.
     *
     * @return A NamingException or null if the result code indicates
     *         success.
     */
    public NamingException getException() {

        return LdapCtx.mapErrorCode(resultCode, null);
    }
}
