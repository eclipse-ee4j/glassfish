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

import java.util.Map;
import jakarta.xml.soap.SOAPMessage;
import com.sun.xml.ws.api.message.Packet;


/**
 *
 */
public class PacketMapMessageInfo implements PacketMessageInfo {

    private SOAPAuthParam soapAuthParam;

    private Map infoMap;

    public PacketMapMessageInfo(Packet reqPacket, Packet resPacket) {
        soapAuthParam = new SOAPAuthParam(reqPacket, resPacket, 0);
    }

    @Override
    public Map getMap() {
        if (infoMap == null) {
            infoMap = soapAuthParam.getMap();
        }

        return infoMap;
    }


    @Override
    public Object getRequestMessage() {
        return soapAuthParam.getRequest();
    }


    @Override
    public Object getResponseMessage() {
        return soapAuthParam.getResponse();
    }


    @Override
    public void setRequestMessage(Object request) {
        soapAuthParam.setRequest((SOAPMessage) request);
    }


    @Override
    public void setResponseMessage(Object response) {
        soapAuthParam.setResponse((SOAPMessage) response);
    }

    @Override
    public Packet getRequestPacket() {
        return (Packet) soapAuthParam.getRequestPacket();
    }


    @Override
    public Packet getResponsePacket() {
        return (Packet) soapAuthParam.getResponsePacket();
    }


    @Override
    public void setRequestPacket(Packet p) {
        soapAuthParam.setRequestPacket(p);
    }


    @Override
    public void setResponsePacket(Packet p) {
        soapAuthParam.setResponsePacket(p);
    }
}
