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

package com.sun.enterprise.iiop.security;

import com.sun.corba.ee.org.omg.CSI.MTCompleteEstablishContext;
import com.sun.corba.ee.org.omg.CSI.MTContextError;
import com.sun.corba.ee.org.omg.CSI.MTEstablishContext;
import com.sun.corba.ee.org.omg.CSI.MTMessageInContext;
import java.util.logging.*;
import com.sun.logging.*;

/**
 * This class contains the utility methods for dealing with service contexts.
 *
 * @author: Sekhar Vajjhala
 *
 */

public class SvcContextUtils {

    private static java.util.logging.Logger _logger = null;
    static {
        _logger = LogDomains.getLogger(SvcContextUtils.class, LogDomains.SECURITY_LOGGER);
    }
    /**
     * Define minor codes for errors specified in section 4.5, "ContextError Values and Exceptions"
     *
     * Currently only MessageInContextMinor code is defined since this is the only used by the security interceptors.
     */

    public static final int MessageInContextMinor = 4;

    /**
     * Hard code the value of 15 for SecurityAttributeService until it is defined in IOP.idl. sc.context_id =
     * SecurityAttributeService.value;
     */
    private static final int SECURITY_ATTRIBUTE_SERVICE_ID = 15;

    /**
     * Define mnemonic strings for SAS message types for debugging purposes.
     */

    private static final String EstablishContextName = "EstablishContext";
    private static final String CompleteEstablishName = "CompleteEstablishContext";
    private static final String MessageInContextName = "MessageInContext";
    private static final String ContextErrorName = "ContextError";

    /**
     * returns a mnemonic name for the message type based on the SASContextBody union discriminant
     */

    public static String getMsgname(short discr) {

        String name = null;

        switch (discr) {

        case MTEstablishContext.value:
            name = EstablishContextName;
            break;

        case MTContextError.value:
            name = ContextErrorName;
            break;

        case MTCompleteEstablishContext.value:
            name = CompleteEstablishName;
            break;

        case MTMessageInContext.value:
            name = MessageInContextName;
            break;

        default:
            _logger.log(Level.SEVERE, "iiop.unknown_msgtype");
            break;
        }
        return name;
    }
}
