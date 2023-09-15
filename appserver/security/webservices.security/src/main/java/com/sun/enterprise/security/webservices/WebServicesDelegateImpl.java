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

package com.sun.enterprise.security.webservices;

import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.security.jauth.AuthParam;
import com.sun.enterprise.security.jmac.WebServicesDelegate;
import com.sun.enterprise.security.jmac.provider.PacketMessageInfo;
import com.sun.enterprise.security.jmac.provider.SOAPAuthParam;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.glassfish.api.invocation.ComponentInvocation;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.epicyro.services.AuthConfigRegistrationWrapper;

import jakarta.inject.Singleton;
import jakarta.security.auth.message.MessageInfo;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

/**
 *
 * @author kumar.jayanti
 */
@Service
@Singleton
public class WebServicesDelegateImpl implements WebServicesDelegate {

    protected static final Logger _logger = LogUtils.getLogger();

    private static final String DEFAULT_WEBSERVICES_PROVIDER = "com.sun.xml.wss.provider.wsit.WSITAuthConfigProvider";

    @Override
    public MessageSecurityBindingDescriptor getBinding(ServiceReferenceDescriptor serviceReferenceDescriptor, Map properties) {
        MessageSecurityBindingDescriptor binding = null;
        WSDLPort port = (WSDLPort) properties.get("WSDL_MODEL");
        QName portName = null;
        if (port != null) {
            portName = port.getName();
        }

        if (portName != null) {
            ServiceRefPortInfo portInfo = serviceReferenceDescriptor.getPortInfoByPort(portName);
            if (portInfo != null) {
                binding = portInfo.getMessageSecurityBinding();
            }
        }

        return binding;
    }

    @Override
    public void removeListener(AuthConfigRegistrationWrapper listener) {
        // TODO:V3 convert the pipes to Tubes.
        ClientPipeCloser.getInstance().removeListenerWrapper(listener);
    }

    @Override
    public String getDefaultWebServicesProvider() {
        return DEFAULT_WEBSERVICES_PROVIDER;
    }

    @Override
    public String getAuthContextID(MessageInfo messageInfo) {
        if (!(messageInfo instanceof PacketMessageInfo)) {
            return getOpName((SOAPMessage) messageInfo.getRequestMessage());
        }

        // Make this more efficient by operating on packet
        String authContextID = null;
        PacketMessageInfo pmi = (PacketMessageInfo) messageInfo;

        Packet requestPacket = pmi.getRequestPacket();
        if (requestPacket != null) {
            Message message = requestPacket.getMessage();
            if (message != null) {
                WSDLPort port = (WSDLPort) messageInfo.getMap().get("WSDL_MODEL");
                if (port != null) {
                    WSDLBoundOperation boundOperation = message.getOperation(port);
                    if (boundOperation != null) {
                        QName name = boundOperation.getName();
                        if (name != null) {
                            authContextID = name.getLocalPart();
                        }
                    }
                }
            }
        }

        return authContextID;
    }

    @Override
    public AuthParam newSOAPAuthParam(MessageInfo messageInfo) {
        return new SOAPAuthParam((SOAPMessage) messageInfo.getRequestMessage(), (SOAPMessage) messageInfo.getResponseMessage());
    }

    private String getOpName(SOAPMessage message) {
        if (message == null) {
            return null;
        }

        String opName = null;

        // First look for a SOAPAction header.
        // this is what .net uses to identify the operation

        MimeHeaders headers = message.getMimeHeaders();
        if (headers != null) {
            String[] actions = headers.getHeader("SOAPAction");
            if (actions != null && actions.length > 0) {
                opName = actions[0];
                if (opName != null && opName.equals("\"\"")) {
                    opName = null;
                }
            }
        }

        // If that doesn't work then we default to trying the name
        // of the first child element of the SOAP envelope.

        if (opName == null) {
            Name name = getName(message);
            if (name != null) {
                opName = name.getLocalName();
            }
        }

        return opName;
    }

    private Name getName(SOAPMessage message) {
        Name name = null;
        SOAPPart soap = message.getSOAPPart();
        if (soap != null) {
            try {
                SOAPEnvelope envelope = soap.getEnvelope();
                if (envelope != null) {
                    SOAPBody body = envelope.getBody();
                    if (body != null) {
                        Iterator it = body.getChildElements();
                        while (it.hasNext()) {
                            Object o = it.next();
                            if (o instanceof SOAPElement) {
                                name = ((SOAPElement) o).getElementName();
                                break;
                            }
                        }
                    }
                }
            } catch (SOAPException se) {
                _logger.log(Level.FINE, "WSS: Unable to get SOAP envelope", se);
            }
        }

        return name;
    }

    @Override
    public Object getSOAPMessage(ComponentInvocation inv) {
        return null;
    }

}
