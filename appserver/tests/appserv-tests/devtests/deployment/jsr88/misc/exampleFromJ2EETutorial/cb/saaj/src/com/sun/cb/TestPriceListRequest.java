/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import jakarta.xml.soap.*;
import java.util.*;

import java.net.*;

public class TestPriceListRequest {

    public static void main(String [] args) {

        try {
            SOAPConnectionFactory scf =
                SOAPConnectionFactory.newInstance();
            SOAPConnection con = scf.createConnection();

                 MessageFactory mf = MessageFactory.newInstance();
            SOAPMessage msg = mf.createMessage();

            // Access the SOABBody object
            SOAPPart part = msg.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            SOAPBody body = envelope.getBody();

            // create SOAPBodyElement request
            Name bodyName = envelope.createName("request-prices",
                "RequestPrices", "http://sonata.coffeebreak.com");
            SOAPBodyElement requestPrices =
                body.addBodyElement(bodyName);

            Name requestName = envelope.createName("request");
            SOAPElement request =
                requestPrices.addChildElement(requestName);
            request.addTextNode("Send updated price list.");

            msg.saveChanges();

            // create the endpoint and send the message
            URL endpoint = new URL(
                 URLHelper.getSaajURL() + "/getPriceList");
            SOAPMessage response = con.call(msg, endpoint);
            con.close();

            // get contents of response
            Vector list = new Vector();

            SOAPBody responseBody =
                response.getSOAPPart().getEnvelope().getBody();
            Iterator it1 = responseBody.getChildElements();
            // get price-list element
            while (it1.hasNext()) {
                SOAPBodyElement bodyEl = (SOAPBodyElement)it1.next();
                Iterator it2 = bodyEl.getChildElements();
                // get coffee elements
                while (it2.hasNext()) {
                    SOAPElement child2 = (SOAPElement)it2.next();
                    Iterator it3 = child2.getChildElements();
                    // get coffee-name and price elements
                    while (it3.hasNext()) {
                        SOAPElement child3 = (SOAPElement)it3.next();
                        String value = child3.getValue();
                        list.addElement(value);
                    }
                }
            }
            for (int i = 0; i < list.size(); i = i + 2) {
                System.out.print(list.elementAt(i) + "        ");
                System.out.println(list.elementAt(i + 1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
