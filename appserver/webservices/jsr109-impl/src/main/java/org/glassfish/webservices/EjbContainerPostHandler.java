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

import java.lang.reflect.Method;

import java.rmi.UnmarshalException;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.api.EJBInvocation;

/**
 * This handler is inserted last in the handler chain for an
 * ejb web service endpoint.  It ensures that the application handlers
 * did not change anything in the soap message that would change the
 * dispatch method.
 *
 * @author Kenneth Saks
 */
public class EjbContainerPostHandler extends GenericHandler {

    private final WsUtil wsUtil = new WsUtil();

    public EjbContainerPostHandler() {}

    @Override
    public QName[] getHeaders() {
        return new QName[0];
    }

    @Override
    public boolean handleRequest(MessageContext context) {
        EJBInvocation inv = null;
        try {
            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
            InvocationManager invManager = wscImpl.getInvocationManager();
            Object obj = invManager.getCurrentInvocation();
            if (obj instanceof EJBInvocation) {
                inv = EJBInvocation.class.cast(obj);
                Method webServiceMethodInPreHandler = inv.getWebServiceMethod();
                if (webServiceMethodInPreHandler != null) {
                    // Now that application handlers have run, do another method
                    // lookup and compare the results with the original one.  This
                    // ensures that the application handlers have not changed
                    // the message context in any way that would impact which
                    // method is invoked.
                    Method postHandlerMethod = wsUtil.getInvMethod(
                            (com.sun.xml.rpc.spi.runtime.Tie) inv.getWebServiceTie(), context);
                    if (!webServiceMethodInPreHandler.equals(postHandlerMethod)) {
                        throw new UnmarshalException("Original method " + webServiceMethodInPreHandler
                                + " does not match post-handler method ");
                    }
                }
            }
        } catch(Exception e) {
            wsUtil.throwSOAPFaultException(e.getMessage(),
                                           context);
        }
        return true;
    }
}
