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

import java.io.IOException;

import javax.naming.ldap.BasicControl;

/**
 * This class implements the LDAPv3 Response Control for directory
 * synchronization as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-armijo-ldap-dirsync-01.txt">draft-armijo-ldap-dirsync-01.txt</a>.
 *
 * The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     realReplControlValue ::= SEQUENCE {
 *              flag                    INTEGER
 *              maxReturnlength         INTEGER
 *              cookie                  OCTET STRING
 *     }
 *
 * </pre>
 *
 * @see DirSyncControl
 * @author Vincent Ryan
 */
final public class DirSyncResponseControl extends BasicControl {

    /**
     * The dir-sync response control's assigned object identifier
     * is 1.2.840.113556.1.4.841.
     */
    public static final String OID = "1.2.840.113556.1.4.841";

    /**
     * If flag is set to a non-zero value, it implies that there is more
     * data to retrieve.
     *
     * @serial
     */
    private int flag;

    /**
     * The maximum length (in bytes) returned in a control response.
     *
     * @serial
     */
    private int maxReturnLength;

    /**
     * A server-generated cookie.
     *
     * @serial
     */
    private byte[] cookie = new byte[0];

    private static final long serialVersionUID = -4497924817230713114L;

    /**
     * Constructs a paged-results response control.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    public DirSyncResponseControl(String id, boolean criticality,
        byte[] value) throws IOException {

        super(id, criticality, value);

        // decode value
        if ((value != null) && (value.length > 0)) {
            BerDecoder ber = new BerDecoder(value, 0, value.length);

            ber.parseSeq(null);
            flag = ber.parseInt();
            maxReturnLength = ber.parseInt();
            cookie = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
        }
    }

    /**
     * Retrieves the more-data flag.
     *
     * @return The more-data flag.
     */
    public int getFlag() {
        return flag;
    }

    /**
     * Determines if more data is available or not.
     *
     * @return true if more data is available.
     */
    public boolean hasMoreData() {
        return flag != 0;
    }

    /**
     * Retrieves the maximum length (in bytes) returned in a control response.
     *
     * @return The length.
     */
    public int getMaxReturnLength() {
        return maxReturnLength;
    }

    /**
     * Retrieves the server-generated cookie. It is used by the client in
     * subsequent searches.
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
