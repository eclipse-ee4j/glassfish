/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.jmac.soap;

import java.io.IOException;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

class Util {
    static String getValue(SOAPMessage message) throws SOAPException {
        SOAPBody body = message.getSOAPBody();
        SOAPElement paramElement = (SOAPElement) body.getFirstChild().getFirstChild();
        return paramElement.getValue();
    }

    static void prependSOAPMessage(SOAPMessage message, String prefix) throws IOException, SOAPException {
        SOAPBody body = message.getSOAPBody();
        SOAPElement paramElement = (SOAPElement) body.getFirstChild().getFirstChild();
        paramElement.setValue(prefix + paramElement.getValue());
    }
}
