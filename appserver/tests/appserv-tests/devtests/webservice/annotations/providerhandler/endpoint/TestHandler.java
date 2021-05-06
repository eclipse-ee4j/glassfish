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

package endpoint;

import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.soap.*;

public class TestHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    String postConstString = "NOT_INITIALIZED";
    @PostConstruct
    public void init() {
        postConstString = "PROPERLY_INITIALIZED";
    }

    @Resource(name="stringValue")
    String injectedString = "undefined";

    public boolean handleMessage(SOAPMessageContext context) {
        try {
            if ("PROPERLY_INITIALIZED".equals(postConstString)) {
                System.out.println("postConstString = " + postConstString);
            } else {
                System.out.println("Handler PostConstruct not called property");
                System.out.println("postConstString = " + postConstString);
                return false;
            }
            if ("undefined".equals(injectedString)) {
                System.out.println("Handler not injected property");
                return false;
            } else {
                System.out.println("injectedString = " + injectedString);
            }
            SOAPMessageContext smc = (SOAPMessageContext) context;
            SOAPMessage message = smc.getMessage();
            SOAPBody body = message.getSOAPBody();

            SOAPElement paramElement =
                (SOAPElement) body.getFirstChild().getFirstChild();
            paramElement.setValue(injectedString + " " + paramElement.getValue());
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        System.out.println("VIJ's TEST HANDLER CALLED");
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void destroy() {}

    public void close(MessageContext context) {}

}
