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
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

import jakarta.xml.soap.*;

public class OrderRequest {
    String url;

    public OrderRequest(String url){
        this.url = url;
    }

    public ConfirmationBean placeOrder(OrderBean orderBean) {
        ConfirmationBean cb = null;   

        try {
            SOAPConnectionFactory scf = 
                SOAPConnectionFactory.newInstance();
            SOAPConnection con = scf.createConnection();

            MessageFactory mf = MessageFactory.newInstance();
            SOAPMessage msg = mf.createMessage();

            // Access the SOAPBody object
            SOAPPart part = msg.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            SOAPBody body = envelope.getBody();

            // Create the appropriate elements and add them
            Name bodyName = envelope.createName("coffee-order", "PO",
                "http://sonata.coffeebreak.com");
            SOAPBodyElement order = body.addBodyElement(bodyName);

            // orderID
            Name orderIDName = envelope.createName("orderID");
            SOAPElement orderID = order.addChildElement(orderIDName);
            orderID.addTextNode(orderBean.getId());

            // customer
            Name childName = envelope.createName("customer");
            SOAPElement customer = order.addChildElement(childName);

            childName = envelope.createName("last-name");
            SOAPElement lastName = customer.addChildElement(childName);
            lastName.addTextNode(orderBean.getCustomer().getLastName());

            childName = envelope.createName("first-name");
            SOAPElement firstName = customer.addChildElement(childName);
            firstName.addTextNode(orderBean.getCustomer().getFirstName());

            childName = envelope.createName("phone-number");
            SOAPElement phoneNumber = customer.addChildElement(childName);
            phoneNumber.addTextNode(
                orderBean.getCustomer().getPhoneNumber());

            childName = envelope.createName("email-address");
            SOAPElement emailAddress = 
                customer.addChildElement(childName);
            emailAddress.addTextNode(
                orderBean.getCustomer().getEmailAddress());

            // address
            childName = envelope.createName("address");
            SOAPElement address = order.addChildElement(childName);

            childName = envelope.createName("street");
            SOAPElement street = address.addChildElement(childName);
            street.addTextNode(orderBean.getAddress().getStreet());

            childName = envelope.createName("city");
            SOAPElement city = address.addChildElement(childName);
            city.addTextNode(orderBean.getAddress().getCity());

            childName = envelope.createName("state");
            SOAPElement state = address.addChildElement(childName);
            state.addTextNode(orderBean.getAddress().getState());

            childName = envelope.createName("zip");
            SOAPElement zip = address.addChildElement(childName);
            zip.addTextNode(orderBean.getAddress().getZip());
    
            LineItemBean[] lineItems=orderBean.getLineItems();            
            for (int i=0;i < lineItems.length;i++) {
                LineItemBean lib = lineItems[i];

                childName = envelope.createName("line-item");
                SOAPElement lineItem = order.addChildElement(childName);

                childName = envelope.createName("coffeeName");
                SOAPElement coffeeName = 
                    lineItem.addChildElement(childName);
                coffeeName.addTextNode(lib.getCoffeeName());

                childName = envelope.createName("pounds");
                SOAPElement pounds = lineItem.addChildElement(childName);
                pounds.addTextNode(lib.getPounds().toString());

                childName = envelope.createName("price");
                SOAPElement price = lineItem.addChildElement(childName);
                price.addTextNode(lib.getPrice().toString());
            }

            // total
            childName = envelope.createName("total");
            SOAPElement total = 
                order.addChildElement(childName);
            total.addTextNode(orderBean.getTotal().toString()); 
              
            URL endpoint = new URL(url);
            SOAPMessage reply = con.call(msg, endpoint);
            con.close();

            // Extract content of reply
            // Extract order ID and ship date
            SOAPBody sBody = reply.getSOAPPart().getEnvelope().getBody();
            Iterator bodyIt = sBody.getChildElements();
            SOAPBodyElement sbEl = (SOAPBodyElement)bodyIt.next();
            Iterator bodyIt2 = sbEl.getChildElements();

            // Get orderID
            SOAPElement ID = (SOAPElement)bodyIt2.next();
            String id = ID.getValue();

            // Get ship date
            SOAPElement sDate = (SOAPElement)bodyIt2.next();
            String shippingDate = sDate.getValue();
            SimpleDateFormat df = 
                new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            Date date = df.parse(shippingDate);
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            cb = new ConfirmationBean(id, cal);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return cb;
    }
}
