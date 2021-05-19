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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import javax.xml.transform.*;

import java.util.*;
import java.io.*;

public class ConfirmationServlet extends HttpServlet {
    static MessageFactory fac = null;

    static {
        try {
            fac = MessageFactory.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void init(ServletConfig servletConfig)
            throws ServletException {
        super.init(servletConfig);
    }

    public void doPost( HttpServletRequest req,
        HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // Get all the headers from the HTTP request
            MimeHeaders headers = getHeaders(req);

            // Get the body of the HTTP request
            InputStream is = req.getInputStream();

            // Now internalize the contents of a HTTP request
            // and create a SOAPMessage
            SOAPMessage msg = fac.createMessage(headers, is);

            SOAPMessage reply = null;
            reply = onMessage(msg);

            if (reply != null) {

                /*
                 * Need to call saveChanges because we're
                 * going to use the MimeHeaders to set HTTP
                 * response information. These MimeHeaders
                 * are generated as part of the save.
                 */
                if (reply.saveRequired()) {
                    reply.saveChanges();
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                putHeaders(reply.getMimeHeaders(), resp);

                // Write out the message on the response stream
                OutputStream os = resp.getOutputStream();
                reply.writeTo(os);
                os.flush();
            } else {
                resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Exception ex) {
            throw new ServletException("SAAJ POST failed: " +
                ex.getMessage());
        }
    }

    static MimeHeaders getHeaders(HttpServletRequest req) {

        Enumeration enum = req.getHeaderNames();
        MimeHeaders headers = new MimeHeaders();

        while (enum.hasMoreElements()) {
            String headerName = (String)enum.nextElement();
            String headerValue = req.getHeader(headerName);

            StringTokenizer values =
                new StringTokenizer(headerValue, ",");
            while (values.hasMoreTokens()) {
                headers.addHeader(headerName,
                    values.nextToken().trim());
            }
        }
        return headers;
    }

    static void putHeaders(MimeHeaders headers,
            HttpServletResponse res) {

        Iterator it = headers.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader header = (MimeHeader)it.next();

            String[] values = headers.getHeader(header.getName());
            if (values.length == 1) {
                res.setHeader(header.getName(),
                    header.getValue());
            } else {
                StringBuffer concat = new StringBuffer();
                int i = 0;
                while (i < values.length) {
                    if (i != 0) {
                        concat.append(',');
                    }
                    concat.append(values[i++]);
                }
                res.setHeader(header.getName(), concat.toString());
            }
        }
    }

    // This is the application code for handling the message.

    public SOAPMessage onMessage(SOAPMessage message) {

        SOAPMessage confirmation = null;

        try {
            // Retrieve orderID from message received
            SOAPBody sentSB =
                message.getSOAPPart().getEnvelope().getBody();
            Iterator sentIt = sentSB.getChildElements();
            SOAPBodyElement sentSBE =
                (SOAPBodyElement)sentIt.next();
            Iterator sentIt2 = sentSBE.getChildElements();
            SOAPElement sentSE = (SOAPElement)sentIt2.next();

            // Get the orderID text to put in confirmation
            String sentID = sentSE.getValue();

            // Create the confirmation message
            confirmation = fac.createMessage();
            SOAPPart sp = confirmation.getSOAPPart();
            SOAPEnvelope env = sp.getEnvelope();
            SOAPBody sb = env.getBody();

            Name newBodyName = env.createName("confirmation",
                "Confirm", "http://sonata.coffeebreak.com");
            SOAPBodyElement confirm =
                sb.addBodyElement(newBodyName);

            // Create the orderID element for confirmation
            Name newOrderIDName = env.createName("orderId");
            SOAPElement newOrderNo =
                confirm.addChildElement(newOrderIDName);
            newOrderNo.addTextNode(sentID);

            // Create ship-date element
            Name shipDateName = env.createName("ship-date");
            SOAPElement shipDate =
                confirm.addChildElement(shipDateName);

            // Create the shipping date
            Date today = new Date();
            long msPerDay = 1000 * 60 * 60 * 24;
            long msTarget = today.getTime();
            long msSum = msTarget + (msPerDay * 2);
            Date result = new Date();
            result.setTime(msSum);
            String sd = result.toString();
            shipDate.addTextNode(sd);

            confirmation.saveChanges();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return confirmation;
    }
}

