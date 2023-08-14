/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.jmac.soap.client;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.soap.*;

public class TestHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public void init() {
    }

    public boolean handleMessage(SOAPMessageContext context) {
        System.out.println("Calling client handler");
        
        try {
            boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            String prefix;
            if (outbound) {
                prefix = "OutboundHandler ";
                System.out.println("Calling outbound client handler");
            } else {
                prefix = "InboundHandler ";
                System.out.println("Calling inbound client handler");
            }
            
            SOAPElement paramElement = (SOAPElement) context.getMessage().getSOAPBody().getFirstChild().getFirstChild();
            paramElement.setValue(prefix + paramElement.getValue());
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void destroy() {
    }

    public void close(MessageContext context) {
    }
}
