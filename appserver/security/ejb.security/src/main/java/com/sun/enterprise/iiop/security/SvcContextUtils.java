/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.iiop.security;

import com.sun.corba.ee.org.omg.CSI.MTCompleteEstablishContext;
import com.sun.corba.ee.org.omg.CSI.MTContextError;
import com.sun.corba.ee.org.omg.CSI.MTEstablishContext;
import com.sun.corba.ee.org.omg.CSI.MTMessageInContext;
import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the utility methods for dealing with service contexts.
 *
 * @author: Sekhar Vajjhala
 *
 */
public class SvcContextUtils {

    private static final Logger LOG = LogDomains.getLogger(SvcContextUtils.class, LogDomains.SECURITY_LOGGER, false);

    /**
     * Define minor codes for errors specified in section 4.5, "ContextError Values and Exceptions"
     *
     * Currently only MessageInContextMinor code is defined since this is the only used by the security interceptors.
     */
    public static final int MessageInContextMinor = 4;


    /**
     * Define mnemonic strings for SAS message types for debugging purposes.
     */
    private static final String EstablishContextName = "EstablishContext";
    private static final String CompleteEstablishName = "CompleteEstablishContext";
    private static final String MessageInContextName = "MessageInContext";
    private static final String ContextErrorName = "ContextError";

    /**
     * @return a mnemonic name for the message type based on the SASContextBody union discriminant
     */
    public static String getMsgname(short discr) {
        switch (discr) {
            case MTEstablishContext.value:
                return EstablishContextName;
            case MTContextError.value:
                return ContextErrorName;
            case MTCompleteEstablishContext.value:
                return CompleteEstablishName;
            case MTMessageInContext.value:
                return MessageInContextName;
            default:
                LOG.log(Level.SEVERE, "Invalid message type {0}, returning null.", discr);
                return null;
        }
    }
}
