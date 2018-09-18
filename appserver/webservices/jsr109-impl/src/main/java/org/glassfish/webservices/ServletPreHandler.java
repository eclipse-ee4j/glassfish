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
import java.util.logging.Level;
import org.glassfish.api.invocation.InvocationManager;

import com.sun.enterprise.web.WebComponentInvocation;

import com.sun.xml.rpc.server.http.Implementor;
import com.sun.xml.rpc.server.http.MessageContextProperties;

/**
 * This handler is inserted first in the handler chain for an
 * servlet web service endpoint.  
 *
 * @author Kenneth Saks
 */
public class ServletPreHandler extends GenericHandler {

    private static Logger logger = LogUtils.getLogger();
    private final WsUtil wsUtil = new WsUtil();

    public ServletPreHandler() {}

    @Override
    public QName[] getHeaders() {
        return new QName[0];
    }

    @Override
    public boolean handleRequest(MessageContext context) {
        WebComponentInvocation inv = null;

        try {
            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
            InvocationManager invManager = wscImpl.getInvocationManager();
            Object obj = invManager.getCurrentInvocation();
            if (obj instanceof WebComponentInvocation) {
                inv = WebComponentInvocation.class.cast(obj);
                com.sun.xml.rpc.spi.runtime.Tie tie
                        = (com.sun.xml.rpc.spi.runtime.Tie) inv.getWebServiceTie();
                if (tie == null) {
                    Implementor implementor = (Implementor) context.getProperty(MessageContextProperties.IMPLEMENTOR);
                    tie = implementor.getTie();
                    inv.setWebServiceTie(tie);
                }
                inv.setWebServiceMethod(wsUtil.getInvMethod(tie, context));
            }

        } catch(Exception e) {
            logger.log(Level.WARNING, LogUtils.PRE_WEBHANDLER_ERROR, e.toString());
            wsUtil.throwSOAPFaultException(e.getMessage(), context);
        }
        return true;
    }
}
