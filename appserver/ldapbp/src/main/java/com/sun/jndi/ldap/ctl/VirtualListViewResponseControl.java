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
import com.sun.jndi.ldap.LdapCtx;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Response Control for virtual-list-view
 * as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-ietf-ldapext-ldapv3-vlv-04.txt">draft-ietf-ldapext-ldapv3-vlv-04.txt</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     VirtualListViewResponse ::= SEQUENCE {
 *         targetPosition   INTEGER (0 .. maxInt),
 *         contentCount     INTEGER (0 .. maxInt),
 *         virtualListViewResult ENUMERATED {
 *             success                   (0),
 *             operatonsError            (1),
 *             unwillingToPerform       (53),
 *             insufficientAccessRights (50),
 *             busy                     (51),
 *             timeLimitExceeded         (3),
 *             adminLimitExceeded       (11),
 *             sortControlMissing       (60),
 *             offsetRangeError         (61),
 *             other                    (80)
 *         },
 *         contextID     OCTET STRING OPTIONAL
 *     }
 *
 * </pre>
 *
 * @see VirtualListViewControl
 * @see ResponseControlFactory
 * @author Vincent Ryan
 */
final public class VirtualListViewResponseControl extends BasicControl {

    /**
     * The virtual-list-view control's assigned object identifier
     * is 2.16.840.1.113730.3.4.10.
     */
    public static final String OID = "2.16.840.1.113730.3.4.10";

    /**
     * The position of the target entry in the list.
     *
     * @serial
     */
    private int targetOffset;

    /**
     * The current number of entries in the list.
     *
     * @serial
     */
    private int listSize;

    /**
     * The result code of the view operation.
     *
     * @serial
     */
    private int resultCode;

    /**
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = null;

    private static final long serialVersionUID = 7199084985976520165L;

    /**
     * Constructs a new instance of VirtualListViewResponseControl.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public VirtualListViewResponseControl(String id, boolean criticality, byte[] value) throws IOException {
        super(id, criticality, value);

        // decode value
        if ((value != null) && (value.length > 0)) {
            BerDecoder ber = new BerDecoder(value, 0, value.length);

            ber.parseSeq(null);
            targetOffset = ber.parseInt();
            listSize = ber.parseInt();
            resultCode = ber.parseEnumeration();

            if ((ber.bytesLeft() > 0) && (ber.peekByte() == Ber.ASN_OCTET_STR)){
                cookie = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
            }
        }
    }

    /**
     * Retrieves the LDAP result code of the view operation.
     *
     * @return    The result code. A zero value indicates success.
     */
    public int getResultCode() {
        return resultCode;
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

    /**
     * Retrieves the server's estimate of the current position of the
     * target entry in the list.
     *
     * @return The position of the target entry in the list.
     */
    public int getTargetOffset() {
        return targetOffset;
    }

    /**
     * Retrieves the server's estimate of the current number of entries
     * in the list.
     *
     * @return The current number of entries in the list.
     */
    public int getListSize() {
        return listSize;
    }

    /**
     * Retrieves the server-generated cookie (if supplied).
     *
     * @return A server-generated cookie or null if unavailable.
     */
    public byte[] getContextID() {
        return cookie;
    }
}
