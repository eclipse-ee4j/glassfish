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

import javax.xml.namespace.QName;

import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;

import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.api.EJBInvocation;

/**
 * This handler is inserted first in the handler chain for an
 * ejb web service endpoint.  It performs security authorization 
 * before any of the application handlers are invoked, as required
 * by JSR 109.
 *
 * @author Kenneth Saks
 */
public class EjbContainerPreHandler extends GenericHandler {

    private static final Logger logger = LogUtils.getLogger();
    private final WsUtil wsUtil = new WsUtil();

    public EjbContainerPreHandler() {}

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
                Method method = wsUtil.getInvMethod(
                        (com.sun.xml.rpc.spi.runtime.Tie) inv.getWebServiceTie(), context);
                inv.setWebServiceMethod(method);
                if (!inv.authorizeWebService(method)) {
                    throw new Exception(format(logger.getResourceBundle().getString(LogUtils.CLIENT_UNAUTHORIZED),
                            method.toString()));
                }
            }
        } catch(Exception e) {
            wsUtil.throwSOAPFaultException(e.getMessage(), context);
        }
        return true;
    }

    private String format(String key, String ... values){
        return MessageFormat.format(key, (Object [])values);
    }
}
