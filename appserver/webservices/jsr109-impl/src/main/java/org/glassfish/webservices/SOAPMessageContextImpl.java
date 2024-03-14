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

package org.glassfish.webservices;

import java.util.Set;
import java.util.Map;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBContext;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;

import jakarta.xml.soap.SOAPMessage;

/**
 * Implementation of SOAPMessageContext
 */
public class SOAPMessageContextImpl implements SOAPMessageContext {

    private Packet packet = null;
    private SOAPMessage message = null;
    private static final Logger logger = LogUtils.getLogger();

    public SOAPMessageContextImpl(Packet pkt) {
        this.packet = pkt;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
        this.message = null;
    }

    public SOAPMessage getMessage() {

        if (message != null) {
            return message;
        }

        SOAPMessage soapMsg = null;
        try {
            //before converting to SOAPMessage, make a copy.  We don't want to consume
            //the original message
            if (packet.getMessage() != null) {
                Message mutable = packet.getMessage().copy();
                soapMsg = mutable.readAsSOAPMessage();
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, LogUtils.ERROR_OCCURED, e);
        }

        //store the message so we don't have to convert again
        message = soapMsg;

        return soapMsg;
    }

    public void setMessage(SOAPMessage newMsg) {
        message = newMsg;

        //keep the com.sun.xml.ws.api.message.Message in the packet consistent with the
        //SOAPMessage we are storing here.
        packet.setMessage(Messages.create(newMsg));
    }

    public Object[] getHeaders(QName header, JAXBContext jaxbContext, boolean allRoles) {
        // this is a dummy impl; we do not use it at all
        return null;
    }

    public Set<String> getRoles() {
        // this is a dummy impl; we do not use it at all
        return null;
    }

    public Scope getScope(String name) {
        // this is a dummy impl; we do not use it at all
        return null;
    }

    public void setScope(String name, Scope scope) {
        // this is a dummy impl; we do not use it at all
        return;
    }

    public boolean isAlreadySoap() {
        // In jaxws-rearch, only SOAP messages come here
        // So always return true
        return true;
    }

    /* java.util.Map methods below here */

    public void clear() {
        // We just clear whatever we set; we do not clear jaxws's properties'
        packet.invocationProperties.clear();
    }

    public boolean containsKey(Object obj) {
        // First check our property bag
        if(packet.supports(obj)) {
            return packet.containsKey(obj);
        }
        return packet.invocationProperties.containsKey(obj);
    }

    public boolean containsValue(Object obj) {
        return packet.invocationProperties.containsValue(obj);
    }

    public Set<Entry<String, Object>> entrySet() {
        return packet.invocationProperties.entrySet();
    }

    public Object get(Object obj) {
        if(packet.supports(obj)) {
            return packet.get(obj);
        }
        return packet.invocationProperties.get(obj);
    }

    public boolean isEmpty() {
        return packet.invocationProperties.isEmpty();
    }

    public Set<String> keySet() {
        return packet.invocationProperties.keySet();
    }

    public Object put(String str, Object obj) {
        return packet.invocationProperties.put(str, obj);
    }

    public void putAll(Map<? extends String, ? extends Object> map) {
        packet.invocationProperties.putAll(map);
    }

    public Object remove(Object obj) {
        return packet.invocationProperties.remove(obj);
    }

    public int size() {
        return packet.invocationProperties.size();
    }

    public Collection<Object> values() {
        return packet.invocationProperties.values();
    }
}
