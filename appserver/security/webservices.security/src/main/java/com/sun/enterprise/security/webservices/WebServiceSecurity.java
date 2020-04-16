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

/*
 * WebServiceSecurity.java
 *
 * Created on April 9, 2004, 2:28 PM
 */

package com.sun.enterprise.security.webservices;

import java.util.HashMap;
import java.util.Set;

import com.sun.enterprise.security.jauth.*;
import com.sun.enterprise.security.common.ClientSecurityContext;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
    
import java.security.Principal;
import javax.security.auth.Subject;
import javax.xml.soap.SOAPMessage;
import jakarta.servlet.http.HttpServletRequest;

import java.util.logging.*;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.security.jmac.provider.SOAPAuthParam;
import org.glassfish.internal.api.Globals;

/**
 *
 * Load Container auth spi.
 * This is for JAXRPC only.
 * @author  Harpreet Singh
 */

public class WebServiceSecurity {

    private static Logger _logger = LogUtils.getLogger();

    private static AppServerAuditManager auditManager = null;
            
    // keys to shared state (for things like session keys) in SOAPMessageCOntext
    private static final String SHARED_CLIENT_STATE = 
        "com.sun.enterprise.security.jauth.ClientHashMap";

    private static final String SHARED_SERVER_STATE = 
        "com.sun.enterprise.security.jauth.ServerHashMap";

    static  {
        if (Globals.getDefaultHabitat() != null) {
            auditManager = Globals.get(AppServerAuditManager.class);
        }
    }
    
    // when called by jaxrpc SystemHandlerDelegate
    public static boolean 
	validateRequest(javax.xml.rpc.handler.soap.SOAPMessageContext context, 
			ServerAuthContext sAC)
        throws AuthException 
    {
	boolean rvalue = true;
	SOAPAuthParam param = 
	    new SOAPAuthParam(context.getMessage(), null);

	// put sharedState in MessageContext for use by secureResponse
	HashMap sharedState = new HashMap();
	context.setProperty(SHARED_SERVER_STATE, sharedState);

	try {
	    rvalue = validateRequest(param, sharedState, sAC);
	} catch(PendingException pe){
            _logger.log(Level.FINE,
			"Container-auth: wss: Error validating request  ",pe);
	    context.setMessage(param.getResponse());
	    rvalue = false;
	} catch(FailureException fe){
            _logger.log(Level.FINE,
			"Container-auth: wss: Error validating request  ",fe);
	    context.setMessage(param.getResponse());
	    throw fe;
        }
	return rvalue;
    }

    private static boolean 
	validateRequest(AuthParam param, HashMap sharedState, 
			ServerAuthContext sAC)
        throws AuthException 
    {
	boolean rvalue = true;

        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
            "Container Auth: ServerAuthContext.validateRequest");
        }
	
	Subject subject = null;
	boolean firstAuthentication = true;
	SecurityContext sc = SecurityContext.getCurrent();
	if (sc == null || sc.didServerGenerateCredentials()) {
	    subject = new Subject();
	} else {
	    subject = sc.getSubject();
	    firstAuthentication = false;
	}

	sAC.validateRequest((AuthParam)param, subject, sharedState);

	if (rvalue && firstAuthentication) {
	    Set principalSet = subject.getPrincipals();
	    // must be at least one new principal to establish
	    // non-default security contex
	    if (principalSet != null && !principalSet.isEmpty()) {
		// define and add initiator to Subject - note that this may add
		// a second principal (of type PrincipalImpl) for initiator.
		String initiator = ((Principal)principalSet.iterator().next()).
		    getName();
		SecurityContext newSC = new SecurityContext(initiator,subject);
		SecurityContext.setCurrent(newSC);
	    }
	}

        return rvalue;
    }
    
    // when called by jaxrpc SystemHandlerDelegate
    public static void 
	secureResponse(javax.xml.rpc.handler.soap.SOAPMessageContext context, 
		       ServerAuthContext sAC)
        throws AuthException 
    {
	secureResponse(context.getMessage(),
		       (HashMap) context.getProperty(SHARED_SERVER_STATE),
		       sAC);
    }

    private static void 
	secureResponse(SOAPMessage response, HashMap sharedState,
		       ServerAuthContext sAC)
        throws AuthException
    {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
            "Container Auth: ServerAuthContext.secureResponse");
        }

	// subject may change if runAs identity differs from caller's.
	// Therefore, session state is saved in sharedState not subject
	SecurityContext sc = SecurityContext.getCurrent();
	Subject subject = sc.getSubject();

	SOAPAuthParam param = new SOAPAuthParam(null, response);

        try{
            sAC.secureResponse((AuthParam)param, subject, sharedState);
        } finally {
	    sAC.disposeSubject(subject,sharedState);
	}

        return;
    }



    // when called by jaxrpc Handler
    public static void 
	secureRequest(javax.xml.rpc.handler.soap.SOAPMessageContext context, 
		      ClientAuthContext cAC, boolean isAppClient)
        throws AuthException 
    {
	// put sharedState in MessageContext for use by validateResponse
	HashMap sharedState = new HashMap();
        sharedState.put(javax.xml.ws.handler.MessageContext.WSDL_SERVICE,
            context.getProperty(javax.xml.ws.handler.MessageContext.WSDL_SERVICE));
	context.setProperty(SHARED_CLIENT_STATE, sharedState);

	secureRequest
	    (context.getMessage(), sharedState, cAC, isAppClient);
    }

    private static void 
	secureRequest(SOAPMessage request, HashMap sharedState,
		      ClientAuthContext cAC, boolean isAppClient) 
        throws AuthException 
    {

        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
            "Container Auth: ClientAuthContext.secureRequest");
        }

	SOAPAuthParam param = new SOAPAuthParam(request, null);

	Subject subject = null;
	if (isAppClient) {
	    ClientSecurityContext sc = ClientSecurityContext.getCurrent();
	    if (sc != null) {
		subject = sc.getSubject();
	    }
	} else {
	    SecurityContext sc = SecurityContext.getCurrent();
	    if (sc != null && !sc.didServerGenerateCredentials()) {
		// make sure we don't use default unauthenticated subject, 
		// so that module cannot change this important (constant) 
		// subject.
		subject = sc.getSubject();
	    }
	}
	if (subject == null) subject = new Subject();
	
	cAC.secureRequest ( param, subject, sharedState);
    }
    
    // when called by jaxrpc Handler
    public static boolean 
	validateResponse(javax.xml.rpc.handler.soap.SOAPMessageContext context,
			 ClientAuthContext cAC)
        throws AuthException 
    {
	return validateResponse
	    (context.getMessage(),
	     (HashMap) context.getProperty(SHARED_CLIENT_STATE), cAC);
    }

    private static boolean 
	validateResponse(SOAPMessage response, HashMap sharedState, 
			 ClientAuthContext cAC) 
        throws AuthException 
    {
        boolean rvalue = true;

	// get a subject to be filled in with the principals of the responder
	Subject responderSubject = new Subject();

	SOAPAuthParam param = new SOAPAuthParam(null, response);

        try{
            cAC.validateResponse( param, responderSubject, sharedState);
        } catch(AuthException ae){
            _logger.log(Level.SEVERE, LogUtils.ERROR_RESPONSE_VALIDATION, ae);
	    rvalue = false;
            throw ae;
        } finally {
	    cAC.disposeSubject(responderSubject,sharedState);
	}
        
        return rvalue;
    }

    // when called by jaxrpc SystemHandlerDelegate
    public static void auditInvocation
    (javax.xml.rpc.handler.soap.SOAPMessageContext context, 
    WebServiceEndpoint endpoint, boolean status) {

	if ((auditManager != null) && auditManager.isAuditOn()) {

	    // TODO: replace the string literal with the correct constant
	    // MessageContextProperties.HTTP_SERVLET_REQUEST);

	    HttpServletRequest req = (HttpServletRequest)context.getProperty
		("com.sun.xml.rpc.server.http.HttpServletRequest");
       
	    String uri = null;

	    if( req != null ) {
		uri = req.getRequestURI();
	    }
	    
	    String epName = null;

	    if( endpoint != null ) {
		epName = endpoint.getEndpointName();
	    }

	    auditManager.webServiceInvocation
		( ((uri==null) ? "(no uri)" : uri), 
		  ((epName==null) ? "(no endpoint)" : epName), 
		  status);
	}
    }
}
