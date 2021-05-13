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


import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import jakarta.security.auth.message.config.*;
import jakarta.security.auth.message.AuthStatus;
import jakarta.xml.ws.WebServiceException;

import com.sun.enterprise.security.jmac.provider.PacketMessageInfo;
import com.sun.enterprise.security.jmac.provider.PacketMapMessageInfo;
import com.sun.enterprise.security.jmac.provider.config.PipeHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;

import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;

import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import jakarta.xml.bind.JAXBElement;

/**
 * This pipe is used to do client side security for app server
 */
public class ClientSecurityPipe extends AbstractFilterPipeImpl implements SecureConversationInitiator {

    protected PipeHelper helper;

    protected static final Logger _logger = LogUtils.getLogger();

    protected static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ClientSecurityPipe.class);

    private static final String WSIT_CLIENT_AUTH_CONTEXT = "com.sun.xml.wss.provider.wsit.WSITClientAuthContext";

    public ClientSecurityPipe(Map props, Pipe next) {
        super(next);

        props.put(PipeConstants.SECURITY_PIPE, this);

        WSDLPort wsdlModel = (WSDLPort) props.get(PipeConstants.WSDL_MODEL);
        if (wsdlModel != null) {
            props.put(PipeConstants.WSDL_SERVICE, wsdlModel.getOwner().getName());
        }
        this.helper = new PipeHelper(PipeConstants.SOAP_LAYER, props, null);

    }


    protected ClientSecurityPipe(ClientSecurityPipe that, PipeCloner cloner) {
        super(that, cloner);
        this.helper = that.helper;
    }


    @Override
    public void preDestroy() {
        // Give the AuthContext a chance to cleanup
        // create a dummy request packet
        try {
            Packet request = new Packet();
            PacketMessageInfo info = new PacketMapMessageInfo(request, new Packet());
            Subject subj = getClientSubject(request);
            ClientAuthContext cAC = helper.getClientAuthContext(info, subj);
            if (cAC != null && WSIT_CLIENT_AUTH_CONTEXT.equals(cAC.getClass().getName())) {
                cAC.cleanSubject(info, subj);
            }
        } catch (Exception ex) {
            // ignore exceptions
        }
        helper.disable();
    }


    @Override
    public final Pipe copy(PipeCloner cloner) {
        return new ClientSecurityPipe(this, cloner);
    }


    public PipeHelper getPipeHelper() {
        return helper;
    }


    @Override
    public Packet process(Packet request) {
        /*
         * XXX should there be code like the following?
         * if(isHttpBinding) {
         * return next.process(request);
         * }
         */

        PacketMessageInfo info = new PacketMapMessageInfo(request, new Packet());
        info.getMap().put(jakarta.xml.ws.Endpoint.WSDL_SERVICE, helper.getProperty(PipeConstants.WSDL_SERVICE));

        AuthStatus status = AuthStatus.SEND_SUCCESS;
        Subject clientSubject = getClientSubject(request);
        ClientAuthContext cAC = null;
        try {
            cAC = helper.getClientAuthContext(info, clientSubject);
            if (cAC != null) {
                // proceed to process message sescurity
                status = cAC.secureRequest(info, clientSubject);
            }
        } catch (Exception e) {
            _logger.log(Level.SEVERE, LogUtils.ERROR_REQUEST_SECURING, e);
            throw new WebServiceException(localStrings.getLocalString("enterprise.webservice.cantSecureRequst",
                "Cannot secure request for {0}", new Object[] {helper.getModelName()}), e);
        }

        Packet response;

        if (status == AuthStatus.FAILURE) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "ws.status_secure_request", status);
            }
            response = info.getResponsePacket();
        } else {
            response = processSecureRequest(info, cAC, clientSubject);
        }

        // may return a security fault even if the MEP was one-way
        return response;
    }


    private Packet processSecureRequest(PacketMessageInfo info, ClientAuthContext cAC, Subject clientSubject)
        throws WebServiceException {
        // send the request
        Packet response = next.process(info.getRequestPacket());

        // check for response
        Message m = response.getMessage();
        if (m != null) {
            if (cAC != null) {
                AuthStatus status;
                info.setResponsePacket(response);
                try {
                    status = cAC.validateResponse(info, clientSubject, null);
                } catch (Exception e) {
                    throw new WebServiceException(localStrings.getLocalString("enterprise.webservice.cantValidateResponse",
                            "Cannot validate response for {0}",
                            new Object[]{helper.getModelName()}), e);
                }
                if (status == AuthStatus.SEND_CONTINUE) {
                    response = processSecureRequest(info, cAC, clientSubject);
                } else {
                    response = info.getResponsePacket();
                }
            }
        }
        return response;
    }


    private static Subject getClientSubject(Packet p) {
        Subject s = null;
        if (p != null) {
            s = (Subject) p.invocationProperties.get(PipeConstants.CLIENT_SUBJECT);
        }
        if (s == null) {
            s = PipeHelper.getClientSubject();
            if (p != null) {
                p.invocationProperties.put(PipeConstants.CLIENT_SUBJECT, s);
            }
        }
        return s;
    }


    @Override
    public JAXBElement startSecureConversation(Packet packet) throws WSSecureConversationException {
        PacketMessageInfo info = new PacketMapMessageInfo(packet, new Packet());
        JAXBElement token = null;
        try {
            // gets the subject from the packet (puts one there if not found)
            Subject clientSubject = getClientSubject(packet);

            // put MessageInfo in properties map, since MessageInfo
            // is not passed to getAuthContext, key idicates function
            HashMap map = new HashMap();
            map.put(PipeConstants.SECURITY_TOKEN, info);

            helper.getSessionToken(map, info, clientSubject);

            // helper returns token in map of msgInfo, using same key
            Object o = info.getMap().get(PipeConstants.SECURITY_TOKEN);

            if (o != null && o instanceof JAXBElement) {
                token = (JAXBElement) o;
            }

        } catch (Exception e) {

            if (e instanceof WSSecureConversationException) {
                throw (WSSecureConversationException) e;
            } else {
                throw new WSSecureConversationException("Secure Conversation failure: ", e);
            }
        }

        return token;
    }
}
