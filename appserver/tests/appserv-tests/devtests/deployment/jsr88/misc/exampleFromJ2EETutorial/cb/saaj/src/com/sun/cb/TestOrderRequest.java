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

import java.net.*;
import java.io.*;
import java.util.*;

import jakarta.xml.soap.*;

public class TestOrderRequest {
    public static void main(String [] args) {
        try {
            SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
            SOAPConnection con = scf.createConnection();

            MessageFactory mf = MessageFactory.newInstance();

            SOAPMessage msg = mf.createMessage();

            // Access the SOABBody object
            SOAPPart part = msg.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            SOAPBody body = envelope.getBody();

            // Create the appropriate elements and add them

            Name bodyName = envelope.createName("coffee-order", "PO",
                            "http://sonata.coffeebreak.com");
            SOAPBodyElement order = body.addBodyElement(bodyName);

            // orderID
            Name orderIDName = envelope.createName("orderID");
            SOAPElement orderID =
                order.addChildElement(orderIDName);
            orderID.addTextNode("1234");

            // customer
            Name childName = envelope.createName("customer");
            SOAPElement customer = order.addChildElement(childName);

            childName = envelope.createName("last-name");
            SOAPElement lastName = customer.addChildElement(childName);
            lastName.addTextNode("Pental");

            childName = envelope.createName("first-name");
            SOAPElement firstName = customer.addChildElement(childName);
            firstName.addTextNode("Ragni");

            childName = envelope.createName("phone-number");
            SOAPElement phoneNumber = customer.addChildElement(childName);
            phoneNumber.addTextNode("908 983-6789");

            childName = envelope.createName("email-address");
            SOAPElement emailAddress =
                customer.addChildElement(childName);
            emailAddress.addTextNode("ragnip@aol.com");

            // address
            childName = envelope.createName("address");
            SOAPElement address = order.addChildElement(childName);

            childName = envelope.createName("street");
            SOAPElement street = address.addChildElement(childName);
            street.addTextNode("9876 Central Way");

            childName = envelope.createName("city");
            SOAPElement city = address.addChildElement(childName);
            city.addTextNode("Rainbow");

            childName = envelope.createName("state");
            SOAPElement state = address.addChildElement(childName);
            state.addTextNode("CA");

            childName = envelope.createName("zip");
            SOAPElement zip = address.addChildElement(childName);
            zip.addTextNode("99999");

            // line-item 1
            childName = envelope.createName("line-item");
            SOAPElement lineItem = order.addChildElement(childName);

            childName = envelope.createName("coffeeName");
            SOAPElement coffeeName = lineItem.addChildElement(childName);
            coffeeName.addTextNode("arabica");

            childName = envelope.createName("pounds");
            SOAPElement pounds = lineItem.addChildElement(childName);
            pounds.addTextNode("2");

            childName = envelope.createName("price");
            SOAPElement price = lineItem.addChildElement(childName);
            price.addTextNode("10.95");

            // line-item 2
            childName = envelope.createName("coffee-name");
            SOAPElement coffeeName2 = lineItem.addChildElement(childName);
            coffeeName2.addTextNode("espresso");

            childName = envelope.createName("pounds");
            SOAPElement pounds2 = lineItem.addChildElement(childName);
            pounds2.addTextNode("3");

            childName = envelope.createName("price");
            SOAPElement price2 = lineItem.addChildElement(childName);
            price2.addTextNode("10.95");

            // total
            childName = envelope.createName("total");
            SOAPElement total = order.addChildElement(childName);
            total.addTextNode("21.90");

            URL endpoint = new URL(
                URLHelper.getSaajURL() + "/orderCoffee");
            SOAPMessage reply = con.call(msg, endpoint);
            con.close();

            // extract content of reply
            //Extracting order ID and ship date
            SOAPBody sBody = reply.getSOAPPart().getEnvelope().getBody();
            Iterator bodyIt = sBody.getChildElements();
            SOAPBodyElement sbEl = (SOAPBodyElement)bodyIt.next();
            Iterator bodyIt2 = sbEl.getChildElements();

            // get orderID
            SOAPElement ID = (SOAPElement)bodyIt2.next();
            String id = ID.getValue();

            // get ship date
            SOAPElement sDate = (SOAPElement)bodyIt2.next();
            String shippingDate = sDate.getValue();

            System.out.println("");
            System.out.println("");
            System.out.println("Confirmation for order #" + id);
            System.out.print("Your order will be shipped on ");
            System.out.println(shippingDate);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
