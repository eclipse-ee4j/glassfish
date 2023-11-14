/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.main.test.app.security.jmac.http.soap.embedded;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.w3c.dom.Node;

class SoapMessageAuthModuleUtilities {

    private static final Logger LOG = System.getLogger(SoapMessageAuthModuleUtilities.class.getName());

    static String getValue(SOAPMessage message) throws SOAPException {
        LOG.log(Level.DEBUG, "getValue(message={0})", message);
        SOAPBody body = message.getSOAPBody();
        Node firstBodyChild = body.getFirstChild();
        LOG.log(Level.DEBUG, "First body child text content: {0}", firstBodyChild.getTextContent());
        return firstBodyChild.getTextContent();
    }

    static void prependSOAPMessage(SOAPMessage message, String prefix) throws SOAPException {
        LOG.log(Level.DEBUG, "prependSOAPMessage(message={0}, prefix={1})", message, prefix);
        SOAPBody body = message.getSOAPBody();
        Node firstBodyChild = body.getFirstChild();
        LOG.log(Level.DEBUG, "First body child text content: {0}", firstBodyChild.getTextContent());
        firstBodyChild.setTextContent(prefix + firstBodyChild.getTextContent());
    }
}
