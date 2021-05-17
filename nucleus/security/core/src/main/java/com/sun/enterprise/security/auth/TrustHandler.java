/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 *
 * Enables developers to provide custom implementation to enable sip containers
 * to determine if a network entity can be trusted.
 */
public interface TrustHandler {


    public void initialize(Properties props);
    /**
     * determines if the container can trust the network entity from which we received the message with P-Asserted-Identity
     * header. This method also validates if the identity that was used to secure(eg: SSL) the message is trusted.
     *
     * @param pAssertedValues P-Asserted-Identity header values
     * @param messageDirection "Incoming" if this method is invoked for a incoming request, "Outgoing" if the message is being sent out.
     * @param asserterAddress ipaddress/hostname of the network entity from which we received the SIP message
     * with P-Asserted-Identity header. Inorder to accept/use the values in P-Asserted-Identity
     * header the network entity should be a trusted.
     * @param securityid is the asserting security identity, if a secure connection is used then this
     * would be the java.security.cert.X509Certificate, else null.
     * @return true if we trust the networtid and the securityid.
     */
    public boolean isTrusted(String asserterAddress, String messageDirection,X509Certificate securityid, Principal[] pAssertedValues);


}
