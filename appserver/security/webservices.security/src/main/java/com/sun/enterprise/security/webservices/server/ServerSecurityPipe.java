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

package com.sun.enterprise.security.webservices.server;

import static com.sun.enterprise.security.webservices.LogUtils.ERROR_RESPONSE_SECURING;
import static com.sun.xml.wss.provider.wsit.PipeConstants.CLIENT_SUBJECT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SECURITY_PIPE;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SOAP_LAYER;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import com.sun.enterprise.security.ee.jmac.callback.ServerContainerCallbackHandler;
import com.sun.enterprise.security.webservices.LogUtils;
import com.sun.enterprise.security.webservices.SoapAuthenticationService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;
import com.sun.xml.wss.provider.wsit.PacketMapMessageInfo;
import com.sun.xml.wss.provider.wsit.PacketMessageInfo;
import com.sun.xml.wss.provider.wsit.PipeConstants;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.config.ServerAuthContext;
import jakarta.xml.ws.WebServiceException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.logging.Logger;
import javax.security.auth.Subject;

/**
 * This pipe uses Jakarta Authentication to authenticate messages / packages.
 */
@SuppressWarnings("deprecation")
public class ServerSecurityPipe extends AbstractFilterPipeImpl {

    protected static final Logger _logger = LogUtils.getLogger();
    protected static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ServerSecurityPipe.class);

    private static final String WSIT_SERVER_AUTH_CONTEXT = "com.sun.xml.wss.provider.wsit.WSITServerAuthContext";

    private final boolean isHttpBinding;

    private SoapAuthenticationService authenticationService;

    public ServerSecurityPipe(Map<String, Object> props, final Pipe next, boolean isHttpBinding) {
        super(next);
        props.put(SECURITY_PIPE, this);
        this.authenticationService = new SoapAuthenticationService(SOAP_LAYER, props, new ServerContainerCallbackHandler());
        this.isHttpBinding = isHttpBinding;
    }

    protected ServerSecurityPipe(ServerSecurityPipe that, PipeCloner cloner) {
        super(that, cloner);
        // We can share the soapAuthenticationService for all pipes so that the remove registration (in server side) can be
        // done properly
        this.authenticationService = that.authenticationService;
        this.isHttpBinding = that.isHttpBinding;
    }

    /**
     * This method is called once in server side and at most one in client side.
     */
    @Override
    public void preDestroy() {
        authenticationService.disable();

        try {
            Packet request = new Packet();
            PacketMessageInfo messageInfo = new PacketMapMessageInfo(request, new Packet());
            Subject subject = new Subject();

            ServerAuthContext serverAuthContext = authenticationService.getServerAuthContext(messageInfo, subject);
            if (serverAuthContext != null && WSIT_SERVER_AUTH_CONTEXT.equals(serverAuthContext.getClass().getName())) {
                serverAuthContext.cleanSubject(messageInfo, subject);
            }
        } catch (Exception ex) {
            // ignore exceptions
        }

        next.preDestroy();
    }

    /**
     * This is used in creating subsequent pipes.
     */
    @Override
    public Pipe copy(PipeCloner cloner) {
        return new ServerSecurityPipe(this, cloner);
    }

    @Override
    public Packet process(Packet request) {
        if (isHttpBinding) {
            return next.process(request);
        }

        Packet response;
        try {
            response = processRequest(request);
        } catch (Exception e) {
            _logger.log(FINE, "Failure in security pipe process", e);
            response = authenticationService.makeFaultResponse(null, e);
        }

        return response;
    }

    private Packet processRequest(Packet request) throws Exception {
        AuthStatus status = AuthStatus.SUCCESS;
        PacketMessageInfo info = new PacketMapMessageInfo(request, new Packet());

        // XXX at this time, we expect the server subject to be null
        Subject serverSubject = (Subject) request.invocationProperties.get(PipeConstants.SERVER_SUBJECT);

        // Could change the request packet
        ServerAuthContext serverAuthContext = authenticationService.getServerAuthContext(info, serverSubject);
        Subject clientSubject = getClientSubject(request);
        final Packet validatedRequest;
        try {
            if (serverAuthContext != null) {
                // Client subject must not be null and when return status is SUCCESS, module
                // must have called handler.handle(CallerPrincipalCallback)
                status = serverAuthContext.validateRequest(info, clientSubject, serverSubject);
            }
        } catch (Exception e) {
            _logger.log(SEVERE, LogUtils.ERROR_REQUEST_VALIDATION, e);
            WebServiceException wse = new WebServiceException(localStrings.getLocalString("enterprise.webservice.cantValidateRequest",
                    "Cannot validate request for {0}", new Object[] { authenticationService.getModelName() }), e);

            // Set status for audit
            status = AuthStatus.SEND_FAILURE;

            // If unable to determine if two-way will return empty response
            return authenticationService.getFaultResponse(info.getRequestPacket(), info.getResponsePacket(), wse);

        } finally {
            validatedRequest = info.getRequestPacket();
            authenticationService.auditInvocation(validatedRequest, status);
        }

        Packet response = null;
        if (status == AuthStatus.SUCCESS) {
            boolean authorized = false;
            try {
                authenticationService.authorize(validatedRequest);
                authorized = true;

            } catch (Exception e) {
                // Not authorized, construct fault and proceded
                response = authenticationService.getFaultResponse(validatedRequest, info.getResponsePacket(), e);
            }

            if (authorized) {
                // only do doAdPriv if SecurityManager is in effect
                if (System.getSecurityManager() == null) {
                    try {
                        // proceed to invoke the endpoint
                        response = next.process(validatedRequest);
                    } catch (Exception e) {
                        _logger.log(SEVERE, LogUtils.NEXT_PIPE, e);
                        response = authenticationService.getFaultResponse(validatedRequest, info.getResponsePacket(), e);
                    }
                } else {
                    try {
                        response = Subject.doAsPrivileged(clientSubject, new PrivilegedExceptionAction<Packet>() {
                            @Override
                            public Packet run() throws Exception {
                                // proceed to invoke the endpoint
                                return next.process(validatedRequest);
                            }
                        }, null);
                    } catch (PrivilegedActionException pae) {
                        Throwable cause = pae.getCause();
                        _logger.log(SEVERE, LogUtils.NEXT_PIPE, cause);
                        response = authenticationService.getFaultResponse(validatedRequest, info.getResponsePacket(), cause);
                    }
                }
            }

            // Pipes are not supposed to return a null response packet
            if (response == null) {
                WebServiceException wse = new WebServiceException(localStrings.getLocalString("enterprise.webservice.nullResponsePacket",
                        "Invocation of Service {0} returned null response packet", new Object[] { authenticationService.getModelName() }));
                response = authenticationService.getFaultResponse(validatedRequest, info.getResponsePacket(), wse);
                _logger.log(SEVERE, LogUtils.EXCEPTION_THROWN, wse);

            }

            // Secure response, including if it is a fault
            if (serverAuthContext != null && response.getMessage() != null) {
                info.setResponsePacket(response);
                response = processResponse(info, serverAuthContext, serverSubject);
            }

        } else {
            // ValidateRequest did not return success
            _logger.log(FINE, "ws.status_validate_request", status);

            // Even for one-way mep, may return response with non-empty message
            response = info.getResponsePacket();
        }

        return response;
    }

    // Called when secureResponse is to be called
    private Packet processResponse(PacketMessageInfo info, ServerAuthContext serverAuthContext, Subject serverSubject) throws Exception {
        AuthStatus status;

        try {
            status = serverAuthContext.secureResponse(info, serverSubject);
        } catch (Exception e) {
            if (e instanceof AuthException) {
                _logger.log(INFO, ERROR_RESPONSE_SECURING, e);
            } else {
                _logger.log(SEVERE, ERROR_RESPONSE_SECURING, e);
            }

            return authenticationService.makeFaultResponse(info.getResponsePacket(), e);
        }
        _logger.log(FINE, "ws.status_secure_response", status);

        return info.getResponsePacket();
    }

    private static Subject getClientSubject(Packet packet) {
        Subject clientSubject = null;

        if (packet != null) {
            clientSubject = (Subject) packet.invocationProperties.get(CLIENT_SUBJECT);
        }

        if (clientSubject == null) {
            clientSubject = SoapAuthenticationService.getClientSubject();
            if (packet != null) {
                packet.invocationProperties.put(CLIENT_SUBJECT, clientSubject);
            }
        }

        return clientSubject;
    }

}
