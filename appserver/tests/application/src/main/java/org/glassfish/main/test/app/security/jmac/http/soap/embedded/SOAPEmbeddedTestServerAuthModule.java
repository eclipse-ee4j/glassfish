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
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.xml.soap.SOAPMessage;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

public class SOAPEmbeddedTestServerAuthModule implements ServerAuthModule {
    private static final Logger LOG = System.getLogger(SOAPEmbeddedTestServerAuthModule.class.getName());

    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler,
        Map<String, Object> options) throws AuthException {
    }


    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return new Class[] {SOAPMessage.class};
    }


    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject)
        throws AuthException {
        LOG.log(Level.DEBUG, "validateRequest(messageInfo={0}, clientSubject={1}, serviceSubject={2})", messageInfo,
            clientSubject, serviceSubject);
        SOAPMessage reqMessage = (SOAPMessage) messageInfo.getRequestMessage();
        try {
            String value = SoapMessageAuthModuleUtilities.getValue(reqMessage);
            LOG.log(Level.INFO, "Incoming value: {0}", value);
            if (value == null || !value.startsWith("SecReq ")) {
                return AuthStatus.FAILURE;
            }

            SoapMessageAuthModuleUtilities.prependSOAPMessage(reqMessage, "ValReq ");
        } catch (Exception ex) {
            throw new AuthException("", ex);
        }

        return AuthStatus.SUCCESS;
    }


    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        LOG.log(Level.INFO, "secureResponse(messageInfo={0}, serviceSubject={1})", messageInfo, serviceSubject);
        SOAPMessage respMessage = (SOAPMessage) messageInfo.getResponseMessage();
        try {
            String value = SoapMessageAuthModuleUtilities.getValue(respMessage);
            LOG.log(Level.INFO, "Incoming value: {0}", value);
            if (value == null || !value.endsWith("ValReq SecReq Sun")) {
                return AuthStatus.FAILURE;
            }
            SoapMessageAuthModuleUtilities.prependSOAPMessage(respMessage, "SecResp ");
        } catch (Exception ex) {
            throw new AuthException("", ex);
        }

        return AuthStatus.SUCCESS;
    }

}
