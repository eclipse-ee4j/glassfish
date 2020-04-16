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

package com.sun.enterprise.security.authorize;

import java.security.SecurityPermission;
import jakarta.security.jacc.PolicyContextHandler;

/**
 * This class is created by the container and handed over to the 
 * JACC provider. This lets the jacc provider to use the information
 * in making authorization decisions, if it wishes to do so.
 * @author Harpreet Singh
 * @author Shing Wai Chan
 */
public class PolicyContextHandlerImpl implements PolicyContextHandler {

    public static final String HTTP_SERVLET_REQUEST =
        "jakarta.servlet.http.HttpServletRequest";
    public static final String SOAP_MESSAGE = "javax.xml.soap.SOAPMessage";
    public static final String ENTERPRISE_BEAN = "javax.ejb.EnterpriseBean";
    public static final String EJB_ARGUMENTS = "javax.ejb.arguments";
    public static final String SUBJECT = "javax.security.auth.Subject.container";
    public static final String REUSE = "java.security.Policy.supportsReuse";

    private static PolicyContextHandlerImpl pchimpl = null;

    private ThreadLocal thisHandlerData = new ThreadLocal();

    private PolicyContextHandlerImpl() {
    }

    private synchronized static PolicyContextHandlerImpl _getInstance() {
        if (pchimpl == null) {
            pchimpl = new PolicyContextHandlerImpl();
        }
        return pchimpl;
    }

    public static PolicyContextHandler getInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("setPolicy"));
        }
    
        return _getInstance();
    }

    public boolean supports(String key) {
	String[] s = getKeys();
	for (int i = 0; i<s.length; i++) {
	    if (s[i].equalsIgnoreCase(key)) {
		return true;
            }
	}
        return false;
    }
    
    public String[] getKeys(){
	String[] s = {
	    HTTP_SERVLET_REQUEST,
	    SOAP_MESSAGE,
	    ENTERPRISE_BEAN,
	    SUBJECT,
	    EJB_ARGUMENTS,
            REUSE
	};
	return s;
    }
    
    public Object getContext(String key, Object data) {
        // ignore data Object
        return getHandlerData().get(key);
    }

    public HandlerData getHandlerData() {
        HandlerData handlerData = (HandlerData)thisHandlerData.get();
        if (handlerData == null) {
            handlerData = HandlerData.getInstance();
            thisHandlerData.set(handlerData);
        }
        return handlerData;
    }

    public void reset() {
         HandlerData handlerData = (HandlerData)thisHandlerData.get();
         if (handlerData != null) {
            handlerData.reset();
         }
         thisHandlerData.set(null);
    }
}
