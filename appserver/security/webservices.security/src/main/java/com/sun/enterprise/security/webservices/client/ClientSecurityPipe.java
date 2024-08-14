/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.webservices.client;

import com.sun.enterprise.security.ee.authentication.jakarta.callback.ClientContainerCallbackHandler;
import com.sun.enterprise.security.webservices.LogUtils;
import com.sun.enterprise.security.webservices.SoapAuthenticationService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.wss.provider.wsit.PacketMapMessageInfo;
import com.sun.xml.wss.provider.wsit.PacketMessageInfo;
import com.sun.xml.wss.provider.wsit.PipeConstants;

import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.config.ClientAuthContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.WebServiceException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import static com.sun.enterprise.security.webservices.LogUtils.ERROR_REQUEST_SECURING;
import static com.sun.xml.wss.provider.wsit.PipeConstants.CLIENT_SUBJECT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SECURITY_PIPE;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SECURITY_TOKEN;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SOAP_LAYER;
import static com.sun.xml.wss.provider.wsit.PipeConstants.WSDL_MODEL;
import static com.sun.xml.wss.provider.wsit.PipeConstants.WSDL_SERVICE;
import static jakarta.security.auth.message.AuthStatus.FAILURE;
import static jakarta.security.auth.message.AuthStatus.SEND_CONTINUE;
import static jakarta.security.auth.message.AuthStatus.SEND_SUCCESS;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * This pipe is used to do client side security for app server
 */
public class ClientSecurityPipe extends AbstractFilterPipeImpl implements SecureConversationInitiator {

    protected static final Logger _logger = LogUtils.getLogger();
    protected static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ClientSecurityPipe.class);
    private static final String WSIT_CLIENT_AUTH_CONTEXT = "com.sun.xml.wss.provider.wsit.WSITClientAuthContext";

    protected SoapAuthenticationService soapAuthenticationService;

    public ClientSecurityPipe(Map<String, Object> properties, Pipe next) {
        super(next);

        properties.put(SECURITY_PIPE, this);

        WSDLPort wsdlModel = (WSDLPort) properties.get(WSDL_MODEL);
        if (wsdlModel != null) {
            properties.put(WSDL_SERVICE, wsdlModel.getOwner().getName());
        }

        this.soapAuthenticationService = new SoapAuthenticationService(SOAP_LAYER, properties, new ClientContainerCallbackHandler());
    }

    protected ClientSecurityPipe(ClientSecurityPipe that, PipeCloner cloner) {
        super(that, cloner);
        this.soapAuthenticationService = that.soapAuthenticationService;
    }

    @Override
    public final Pipe copy(PipeCloner cloner) {
        return new ClientSecurityPipe(this, cloner);
    }

    public SoapAuthenticationService getAuthenticationService() {
        return soapAuthenticationService;
    }

    @Override
    public JAXBElement startSecureConversation(Packet packet) throws WSSecureConversationException {
        PacketMessageInfo info = new PacketMapMessageInfo(packet, new Packet());
        JAXBElement token = null;

        try {
            // Gets the subject from the packet (puts one there if not found)
            Subject clientSubject = getClientSubject(packet);

            // Put MessageInfo in properties map, since MessageInfo
            // is not passed to getAuthContext, key indicates function
            Map<String, Object> map = new HashMap<>();
            map.put(SECURITY_TOKEN, info);

            soapAuthenticationService.getSessionToken(map, info, clientSubject);

            // AuthenticationService returns token in map of msgInfo, using same key
            Object securityToken = info.getMap().get(SECURITY_TOKEN);

            if (securityToken != null && securityToken instanceof JAXBElement) {
                token = (JAXBElement) securityToken;
            }

        } catch (Exception e) {
            if (e instanceof WSSecureConversationException) {
                throw (WSSecureConversationException) e;
            }

            throw new WSSecureConversationException("Secure Conversation failure: ", e);
        }

        return token;
    }

    @Override
    public Packet process(Packet request) {
        /*
         * XXX should there be code like the following? if(isHttpBinding) { return next.process(request); }
         */

        PacketMessageInfo info = new PacketMapMessageInfo(request, new Packet());
        info.getMap().put(Endpoint.WSDL_SERVICE, soapAuthenticationService.getProperty(PipeConstants.WSDL_SERVICE));

        AuthStatus status = SEND_SUCCESS;
        Subject clientSubject = getClientSubject(request);
        ClientAuthContext clientAuthContext = null;
        try {
            clientAuthContext = soapAuthenticationService.getClientAuthContext(info, clientSubject);
            if (clientAuthContext != null) {
                // Proceed to process message security
                status = clientAuthContext.secureRequest(info, clientSubject);
            }
        } catch (Exception e) {
            _logger.log(SEVERE, ERROR_REQUEST_SECURING, e);
            throw new WebServiceException(localStrings.getLocalString("enterprise.webservice.cantSecureRequst",
                    "Cannot secure request for {0}", new Object[] { soapAuthenticationService.getModelName() }), e);
        }

        Packet response;

        if (status == FAILURE) {
            _logger.log(FINE, "ws.status_secure_request", status);
            response = info.getResponsePacket();
        } else {
            response = processSecureRequest(info, clientAuthContext, clientSubject);
        }

        // May return a security fault even if the MEP was one-way
        return response;
    }

    @Override
    public void preDestroy() {
        // Give the AuthContext a chance to cleanup and create a dummy request packet
        try {
            Packet request = new Packet();
            PacketMessageInfo info = new PacketMapMessageInfo(request, new Packet());
            Subject clientSubject = getClientSubject(request);

            ClientAuthContext clientAuthContext = soapAuthenticationService.getClientAuthContext(info, clientSubject);
            if (clientAuthContext != null && WSIT_CLIENT_AUTH_CONTEXT.equals(clientAuthContext.getClass().getName())) {
                clientAuthContext.cleanSubject(info, clientSubject);
            }
        } catch (Exception ex) {
            _logger.log(FINE, "Exception when pre-destroying the client security pipe", ex);
        }

        soapAuthenticationService.disable();
    }

    private Packet processSecureRequest(PacketMessageInfo info, ClientAuthContext clientAuthContext, Subject clientSubject) throws WebServiceException {
        // Send the request
        Packet response = next.process(info.getRequestPacket());

        // Check for response
        Message responseMessage = response.getMessage();
        if (responseMessage != null) {
            if (clientAuthContext != null) {
                AuthStatus status;
                info.setResponsePacket(response);
                try {
                    status = clientAuthContext.validateResponse(info, clientSubject, null);
                } catch (Exception e) {
                    throw new WebServiceException(localStrings.getLocalString("enterprise.webservice.cantValidateResponse",
                            "Cannot validate response for {0}", new Object[] { soapAuthenticationService.getModelName() }), e);
                }
                if (status == SEND_CONTINUE) {
                    response = processSecureRequest(info, clientAuthContext, clientSubject);
                } else {
                    response = info.getResponsePacket();
                }
            }
        }

        return response;
    }

    private static Subject getClientSubject(Packet packet) {
        Subject subject = null;
        if (packet != null) {
            subject = (Subject) packet.invocationProperties.get(CLIENT_SUBJECT);
        }

        if (subject == null) {
            subject = SoapAuthenticationService.getClientSubject();
            if (packet != null) {
                packet.invocationProperties.put(CLIENT_SUBJECT, subject);
            }
        }

        return subject;
    }

}
