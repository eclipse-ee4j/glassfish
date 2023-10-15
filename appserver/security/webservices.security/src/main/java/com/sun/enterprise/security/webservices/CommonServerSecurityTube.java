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

import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import javax.security.auth.Subject;
import jakarta.security.auth.message.config.*;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;

import jakarta.xml.ws.WebServiceException;

import com.sun.enterprise.security.jmac.provider.PacketMapMessageInfo;
import com.sun.enterprise.security.jmac.provider.PacketMessageInfo;
import com.sun.enterprise.security.jmac.provider.config.SoapAuthenticationService;
import com.sun.enterprise.util.LocalStringManagerImpl;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;


/**
 * This pipe is used to do Jakarta Authentication security
 */
public class CommonServerSecurityTube extends AbstractFilterTubeImpl {

    protected static final Logger _logger = LogUtils.getLogger();

    protected static final LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CommonServerSecurityTube.class);
    private final boolean isHttpBinding;
    private SoapAuthenticationService helper;

    // Introduced during Pipe to Tube conversion
    private ServerAuthContext sAC = null;
    private PacketMessageInfo info = null;
    private Subject serverSubject = null;

    public CommonServerSecurityTube(Map props, final Tube next, boolean isHttpBinding) {
        super(next);
        props.put(PipeConstants.SECURITY_PIPE, this);
        this.helper = new SoapAuthenticationService(PipeConstants.SOAP_LAYER, props, null);
        this.isHttpBinding = isHttpBinding;

    }


    protected CommonServerSecurityTube(CommonServerSecurityTube that, TubeCloner cloner) {
        super(that, cloner);
        // we can share the soapAuthenticationService for all pipes so that the remove
        // registration (in server side) can be done properly
        this.helper = that.helper;
        this.isHttpBinding = that.isHttpBinding;
    }

    /**
     * This method is called once in server side and at most one in client side.
     */
    @Override
    public void preDestroy() {
        helper.disable();
        /**
         Fix for bug 3932/4052
         */
        next.preDestroy();
    }


    @Override
    public NextAction processRequest(Packet request) {
        try {
            if (isHttpBinding) {
                return doInvoke(super.next, request);
            }

            AuthStatus status = AuthStatus.SUCCESS;
            info = new PacketMapMessageInfo(request, new Packet());
            // XXX at this time, we expect the server subject to be null
            serverSubject = (Subject) request.invocationProperties.get(PipeConstants.SERVER_SUBJECT);

            //could change the request packet
            sAC = helper.getServerAuthContext(info, serverSubject);
            Subject clientSubject = getClientSubject(request);
            final Packet validatedRequest;
            try {
                if (sAC != null) {
                    // client subject must not be null
                    // and when return status is SUCCESS, module
                    // must have called handler.handle(CallerPrincipalCallback)
                    status = sAC.validateRequest(info, clientSubject, serverSubject);
                }
            } catch (Exception e) {
                _logger.log(Level.SEVERE, LogUtils.ERROR_REQUEST_VALIDATION, e);
                WebServiceException wse = new WebServiceException(
                    localStrings.getLocalString("enterprise.webservice.cantValidateRequest",
                    "Cannot validate request for {0}",
                    new Object[]{helper.getModelName()}), e);

                //set status for audit
                status = AuthStatus.SEND_FAILURE;
                // if unable to determine if two-way will return empty response
                Packet ret = helper.getFaultResponse(info.getRequestPacket(), info.getResponsePacket(), wse);
                return doReturnWith(ret);

            } finally {
                validatedRequest = info.getRequestPacket();
                helper.auditInvocation(validatedRequest, status);
            }

            Packet response = null;
            if (status == AuthStatus.SUCCESS) {
                boolean authorized = false;
                try {
                    helper.authorize(validatedRequest);
                    authorized = true;

                } catch (Exception e) {
                    // not authorized, construct fault and proceded
                    response = helper.getFaultResponse(validatedRequest, info.getResponsePacket(), e);
                    return doReturnWith(response);
                }
                if (authorized) {

                    // only do doAdPriv if SecurityManager is in effect
                    if (System.getSecurityManager() == null) {
                        try {
                            // proceed to invoke the endpoint
                            return doInvoke(super.next, validatedRequest);
                        } catch (Exception e) {
                            _logger.log(Level.SEVERE, LogUtils.NEXT_PIPE, e);
                            response = helper.getFaultResponse(validatedRequest, info.getResponsePacket(), e);
                            return doReturnWith(response);
                        }
                    } else {
                        try {
                            final Tube next = super.next;
                            NextAction action = (NextAction) Subject.doAsPrivileged(clientSubject, new PrivilegedExceptionAction() {

                                @Override
                                public Object run() throws Exception {
                                    // proceed to invoke the endpoint
                                    return doInvoke(next, validatedRequest);
                                }
                            }, null);
                            return action;
                        } catch (PrivilegedActionException pae) {
                            Throwable cause = pae.getCause();
                            _logger.log(Level.SEVERE, LogUtils.NEXT_PIPE, cause);
                            response = helper.getFaultResponse(validatedRequest, info.getResponsePacket(), cause);
                            return doReturnWith(response);
                        }
                    }
                } else { //if not authorized
                    // not authorized, construct fault and proceded
                    response = helper.getFaultResponse(
                        validatedRequest,info.getResponsePacket(), new Exception("Client Not Authorized"));
                    return doReturnWith(response);
                }

            } else {
                // validateRequest did not return success
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "ws.status_validate_request", status);
                }
                // even for one-way mep, may return response with non-empty message
                response = info.getResponsePacket();
                return doReturnWith(response);
            }

        } catch (Throwable t) {
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }
    }

    @Override
    public NextAction processResponse(Packet response) {
        try{
            //could be oneway
            if((response == null) || (response.getMessage() == null)){
                return doReturnWith(response);
            }
            Packet resp = response;
            // secure response, including if it is a fault
            if (sAC != null && response.getMessage() != null) {
                info.setResponsePacket(response);
                resp = processResponse(info, sAC, serverSubject);
            }
            return doReturnWith(resp);
        }catch(Throwable t){
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }

    }


    // called when secureResponse is to be called
    private Packet processResponse(PacketMessageInfo info, ServerAuthContext sAC, Subject serverSubject)
        throws Exception {
        AuthStatus status;

        try {
            status = sAC.secureResponse(info, serverSubject);
        } catch (Exception e) {
            if (e instanceof AuthException) {
                if (_logger.isLoggable(Level.INFO)) {
                    _logger.log(Level.INFO, LogUtils.ERROR_RESPONSE_SECURING, e);
                }
            } else {
                _logger.log(Level.SEVERE, LogUtils.ERROR_RESPONSE_SECURING, e);
            }

            return helper.makeFaultResponse(info.getResponsePacket(),e);
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"ws.status_secure_response", status);
        }
        return info.getResponsePacket();

    }

    private static Subject getClientSubject(Packet p) {

        Subject s = null;

        if (p != null) {
            s =(Subject)
                p.invocationProperties.get(PipeConstants.CLIENT_SUBJECT);
        }
        if (s == null) {
            s = SoapAuthenticationService.getClientSubject();
            if (p != null) {
                p.invocationProperties.put(PipeConstants.CLIENT_SUBJECT,s);
            }
        }
        return s;
    }

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new CommonServerSecurityTube(this, cloner);
    }
}
