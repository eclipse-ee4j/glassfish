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
 * ClientWSSHandler.java
 *
 * Created on June 1, 2004, 11:46 AM
 */

package com.sun.enterprise.security.webservices;

import com.sun.enterprise.security.SecurityServicesUtil;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.JAXRPCException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import java.util.logging.*;

//security apis
import com.sun.enterprise.security.jauth.*;
import com.sun.enterprise.security.jmac.provider.ClientAuthConfig;

/**
 * Client Side Handler to be invoked from the appclient
 * A similiar copy sans appserver specific logging mechanism should be made available
 * to standalone clients to use our
 * WSS infrastructure.
 * @author Harpreet Singh
 * @version
 * @since
 */
public class MessageLayerClientHandler implements Handler {

    private static Logger _logger= LogUtils.getLogger();

    // key to ClientAuthConfig in HandlerInfo
    public static final String CLIENT_AUTH_CONFIG = 
	"com.sun.enterprise.security.jmac.provider.ClientAuthConfig";

    // key to ClientAuthContext in SOAPMessageCOntext
    private static final String CLIENT_AUTH_CONTEXT = 
	"com.sun.enterprise.security.jauth.ClientAuthContext";

    private static String errMsg =
	"Client WSS Handler: MessageContext not of type SOAPMessageContext";

    private ClientAuthConfig config_ = null;

    private QName qname[] = null;

    private boolean isAppclientContainer = false;

    private QName serviceName = null;

    /** Creates a new instance of MessageLayerClientHandler */
    public MessageLayerClientHandler() {
	if(SecurityServicesUtil.getInstance().isACC()){
		isAppclientContainer = true;
	} else{
		isAppclientContainer = false;
	}
    }
    
    public boolean handleFault(MessageContext messageContext) {
        // no need to do any special processing
        if(_logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE, "wss-auth-client: ClientHandler does not handle" +
            " SOAP faults");
        }
        return true;
    }
    
    public boolean handleRequest(MessageContext messageContext) {
        if(!isSoapMessageContext(messageContext)){
            // cannot process this, as this is not a soap message context
            throw new JAXRPCException(errMsg);
        }
	if (config_ == null) {
	    return true;
	} 
	  
        // get the ClientAuthContext
	SOAPMessageContext smc = (SOAPMessageContext) messageContext;
	SOAPMessage request = smc.getMessage();
	ClientAuthContext cAC = config_.getAuthContext(null,request);
	if (cAC == null) {
	    return true;
	} 

	smc.setProperty(CLIENT_AUTH_CONTEXT, cAC);
	smc.setProperty(javax.xml.ws.handler.MessageContext.WSDL_SERVICE,
            serviceName);

        try{
	    WebServiceSecurity.secureRequest(smc,cAC,isAppclientContainer);
        } catch(Exception e){
            if (_logger.isLoggable(Level.WARNING)){
                _logger.log(Level.WARNING, LogUtils.ERROR_REQUEST_SECURING, e);
            }
            throw new JAXRPCException(e);
        }
        return true;
    }
    
    public boolean handleResponse(MessageContext messageContext) {
        boolean retValue;
        if(!isSoapMessageContext(messageContext)){
            // cannot process this, as this is not a soap message context
            throw new JAXRPCException(errMsg);
        }
	if (config_ == null) {
	    return true;
	} 

        // get the ClientAuthContext
        SOAPMessageContext smc = (SOAPMessageContext) messageContext;
	ClientAuthContext cAC = 
	    (ClientAuthContext) smc.getProperty(CLIENT_AUTH_CONTEXT);
	if (cAC == null) {
	    return true;
	} 

        try{
	    retValue = WebServiceSecurity.validateResponse(smc,cAC);
        }catch(Exception e){
            if (_logger.isLoggable(Level.WARNING)){
                _logger.log(Level.WARNING, LogUtils.ERROR_RESPONSE_VALIDATION, e);
            }
            throw new JAXRPCException(e);
        }
        
        return retValue;
    }
    
    // if headers contains the list of understood headers, then we need to make
    // sure that wsse is included; although if so, we need a way to make sure
    // that the provider honors this commitment.
    public javax.xml.namespace.QName[] getHeaders() {
        return qname;
    }
    
    public void destroy() {
        qname = null;
    }
    
    public void init(javax.xml.rpc.handler.HandlerInfo info) {
        // 109 mandates saving qnames in init
        qname = info.getHeaders();
	config_ = (ClientAuthConfig) info.getHandlerConfig().get(CLIENT_AUTH_CONFIG);
        serviceName = (QName)info.getHandlerConfig().get(javax.xml.ws.handler.MessageContext.WSDL_SERVICE);
    }

    /** 109 mandates that each MessageContext be checked to see if it is a
     * a SOAPMessageContext and whether the handler processes it
     */
    private boolean isSoapMessageContext(MessageContext mc){
        boolean retValue =
        (mc instanceof SOAPMessageContext)? true: false;
        if(!retValue && _logger.isLoggable(Level.WARNING)){
            _logger.log(Level.WARNING, LogUtils.NOT_SOAP);
        }
        return retValue;
    }

}
