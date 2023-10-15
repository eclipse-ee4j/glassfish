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

package com.sun.enterprise.security.jmac.provider;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import jakarta.xml.soap.SOAPMessage;
import java.util.HashMap;
import java.util.Map;

/**
  * SOAP authentication parameter.
  *
  * <p> An instance of SOAPAuthParam may be created with a null response object
  * (for example during a call to
  * <code>ServerAuthContext.validateRequest</code>).
  * If so, a response object may be created as necessary (by modules),
  * and set into the SOAPAuthParam via the <code>setResponse</code> method.
  *
  * <p> An instance of SOAPAuthParam may also be created with a null
  * request object (for example during a call to
  * <code>ServerAuthContext.secureResponse</code>).
  *
  * @version 1.12, 06/08/04
  */
public class SOAPAuthParam {

    private HashMap infoMap;

    private boolean requestInPacket;
    private boolean responseInPacket;

    private SOAPMessage request;
    private SOAPMessage response;

    private static Exception classLoadingException = checkForPacket();

    private static final String REQ_PACKET = "REQ_PACKET";
    private static final String RES_PACKET = "RES_PACKET";

    private static boolean REQUEST_PACKET = true;
    private static boolean RESPONSE_PACKET = false;

    private static Exception checkForPacket() {
        try {
            if (Class.forName("com.sun.xml.ws.api.message.Packet") != null &&
                Class.forName("com.sun.xml.ws.api.message.Messages") != null) {
                return null;
            }
        } catch (Exception e) {
            // silently disables packet support
            return e;
        }
        return null;
    }

    /**
     * Create a SOAPAuthParam.
     *
     * @param request the SOAP request object, which may be null.
     * @param response the SOAP response object, which may be null.
     */
    public SOAPAuthParam(SOAPMessage request, SOAPMessage response) {
        this.infoMap = null;
        this.request = request;
        this.response = response;
    }

    /**
     * Create a SOAPAuthParam (using Packets)
     *
     * @param request the request Packet, which may be null.
     * @param response the response Packet, which may be null.
     * @param dummy int serves only to disambiguate constructors
     */
    public SOAPAuthParam(Object request, Object response, int dummy) {
        if (classLoadingException != null) {
            throw new RuntimeException(classLoadingException);
        }

        if ((request == null || request instanceof Packet) &&
            (response == null || response instanceof Packet)) {
            this.infoMap = new HashMap();
            this.infoMap.put(REQ_PACKET,request);
            this.infoMap.put(RES_PACKET,response);
            this.requestInPacket = (request == null ? false : true);
            this.responseInPacket = (response == null ? false : true);
        } else {
            throw new RuntimeException("argument is not packet");
        }

    }

    /**
     * Get the SOAP request object.
     *
     * @return the SOAP request object, which may be null.
     */
    public Map getMap() {
        if (this.infoMap == null) {
            this.infoMap = new HashMap();
        }

        return this.infoMap;
    }

    /**
     * Get the SOAP request object.
     *
     * @return the SOAP request object, which may be null.
     */
    public SOAPMessage getRequest() {
        if (this.request == null) {

            Object p = getPacket(REQUEST_PACKET,true);

            if (p != null && this.requestInPacket) {

                // if packet is not null, get SOAP from packet
                // requestInPacket set to false as side-effect
                // since packet has been consumed.

                this.request = getSOAPFromPacket(REQUEST_PACKET,p);
            }
        }

        return this.request;
    }

    /**
     * Get the SOAP response object.
     *
     * @return the SOAP response object, which may be null.
     */
    public SOAPMessage getResponse() {
        if (this.response == null) {

            Object p = getPacket(RESPONSE_PACKET,false);

            if (p != null && this.responseInPacket) {

                // if packet is not null, get SOAP from packet
                // responseInPacket set to false as side-effect
                // since packet has been consumed.

                this.response = getSOAPFromPacket(RESPONSE_PACKET,p);
            }
        }

        return this.response;
    }

    /**
     * Set the SOAP request object.
     *
     * @param request the SOAP response object.
     */
    public void setRequest(SOAPMessage request) {
        Object p = getPacket(REQUEST_PACKET,false);
        if (p != null) {
            this.requestInPacket = putSOAPInPacket(request,p);
        }
        this.request = request;
    }

    /**
     * Set the SOAP response object.
     *
     * @param response the SOAP response object.
     */
    public void setResponse(SOAPMessage response) {

        // XXX previously, i.e. before wsit,
        // if a response had already been set (it is non-null),
        // this method would return with doing anything
        // The original response would not be overwritten.
        // that is no longer the case.

        Object p = getPacket(RESPONSE_PACKET,false);
        if (p != null) {
            this.responseInPacket = putSOAPInPacket(response,p);
        }
        this.response = response;
    }

    /**
     * Return the request Packet.
     *
     * @return the request Packet, which may be null.
     */
    public Object getRequestPacket() {
        if (classLoadingException != null) {
            throw new RuntimeException(classLoadingException);
        }

        return getPacket(REQUEST_PACKET,true);
    }

    /**
     * Return the response Packet.
     *
     * @return the response Packet, which may be null.
     */
    public Object getResponsePacket() {
        if (classLoadingException != null) {
            throw new RuntimeException(classLoadingException);
        }

        return getPacket(RESPONSE_PACKET,true);
    }

    /**
     * Set the request Packet.
     *
     * <p> has the side effect of resetting the SOAP request message.
     *
     * @param packet the request Packet
     */
    public void setRequestPacket(Object p) {
        if (classLoadingException != null) {
            throw new RuntimeException(classLoadingException);
        }

        if (p == null || p instanceof Packet) {
            getMap().put(REQ_PACKET,p);
            this.requestInPacket = (p == null ? false : true);
            this.request = null;
        } else {
            throw new RuntimeException("argument is not packet");
        }
    }

    /**
     * Set the response Packet.
     *
     * <p> has the side effect of resetting the SOAP response message.
     *
     * @param packet the response Packet
     */
    public void setResponsePacket(Object p) {
        if (classLoadingException != null) {
            throw new RuntimeException(classLoadingException);
        }

        if (p == null || p instanceof Packet) {
            getMap().put(RES_PACKET,p);
            this.responseInPacket = (p == null ? false : true);
            this.response = null;
        } else {
            throw new RuntimeException("argument is not packet");
        }
    }

    /**
     * Return the request Packet.
     *
     * @return the request Packet, which may be null.
     */
    private Object getPacket(boolean isRequestPacket, boolean putDesired) {
        Object p = (this.infoMap == null ?
            null : this.infoMap.get
            (isRequestPacket ? REQ_PACKET : RES_PACKET));

        if (putDesired) {
            SOAPMessage m = (isRequestPacket ? this.request : this.response);

            if (p != null && m != null) {

                // if SOAP request message has been read from packet
                // we may need to set it back in the packet before
                // returning the revised packet

                if (isRequestPacket) {
                    if (!this.requestInPacket) {
                        this.requestInPacket = putSOAPInPacket(m,p);
                    }
                } else {
                    if (!this.responseInPacket) {
                        this.responseInPacket = putSOAPInPacket(m,p);
                    }
                }
            }
        }

        return p;
    }

    private SOAPMessage getSOAPFromPacket(boolean isRequestPacket,Object p) {
        if (classLoadingException != null) {
            throw new RuntimeException(classLoadingException);
        }

        SOAPMessage s = null;
        if (p instanceof Packet) {
            Message m = ((Packet) p).getMessage();
            if (m != null) {
                try {
                    s = m.readAsSOAPMessage();
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (s != null) {
            // System.out.println("SOAPAuthParam.getSOAPFromPacket:");
            // printSOAP(s);
            if (isRequestPacket) {
                this.requestInPacket = false;
            } else {
                this.responseInPacket = false;
            }
        }
        return s;
    }

    private boolean putSOAPInPacket(SOAPMessage m, Object p) {
        if (m == null) {
            ((Packet)p).setMessage(null);
        } else {
            Message msg = Messages.create(m);
            ((Packet)p).setMessage(msg);
        }
        return true;
    }

    public static void printSOAP(SOAPMessage s) {
        try {
            if (s != null) {
                s.writeTo(System.out);
            } else {
                // System.out.println("SOAPMessage is empty");
            }
        } catch (Exception e) {
            // System.out.println("SOAPAuthParam.printSOAP exception!");
        }
    }
}
