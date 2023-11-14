/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.jmac.http.soap.embedded;

import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.module.ClientAuthModule;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Endpoint;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;

public class SOAPEmbeddedTestClientAuthModule implements ClientAuthModule {
    private static final Logger LOG = System.getLogger(SOAPEmbeddedTestClientAuthModule.class.getName());

    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler,
        Map<String, Object> options) throws AuthException {
    }


    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return new Class[] {SOAPMessage.class};
    }


    @Override
    public AuthStatus secureRequest(MessageInfo messageInfo, Subject clientSubject) throws AuthException {
        LOG.log(Level.INFO, "secureRequest(messageInfo={0}, clientSubject={1})", messageInfo, clientSubject);

        QName serviceName = (QName) messageInfo.getMap().get(Endpoint.WSDL_SERVICE);
        LOG.log(Level.INFO, "serviceName={0}", serviceName);
        if (serviceName == null) {
            throw new AuthException("serviceName is null");
        }

        SOAPMessage reqMessage = (SOAPMessage) messageInfo.getRequestMessage();
        try {
            SoapMessageAuthModuleUtilities.prependSOAPMessage(reqMessage, "SecReq ");
        } catch (Exception ex) {
            throw new AuthException("", ex);
        }

        return AuthStatus.SUCCESS;
    }


    @Override
    public AuthStatus validateResponse(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject)
        throws AuthException {
        LOG.log(Level.INFO, "validateResponse(messageInfo={0}, clientSubject={1}, serviceSubject={2})", messageInfo,
            clientSubject, serviceSubject);
        SOAPMessage respMessage = (SOAPMessage) messageInfo.getResponseMessage();
        try {
            String value = SoapMessageAuthModuleUtilities.getValue(respMessage);
            LOG.log(Level.INFO, "Incoming value: {0}", value);
            if (value == null || !value.startsWith("SecResp ") || !value.contains("ValReq SecReq ")) {
                return AuthStatus.FAILURE;
            }
            SoapMessageAuthModuleUtilities.prependSOAPMessage(respMessage, "ValResp ");
        } catch (Exception ex) {
            throw new AuthException("", ex);
        }

        return AuthStatus.SUCCESS;
    }

}
