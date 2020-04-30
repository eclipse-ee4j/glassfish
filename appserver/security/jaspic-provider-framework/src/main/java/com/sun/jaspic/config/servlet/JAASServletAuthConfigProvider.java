/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jaspic.config.servlet;

import com.sun.jaspic.config.delegate.MessagePolicyDelegate;
import com.sun.jaspic.config.helper.AuthContextHelper;
import com.sun.jaspic.config.jaas.JAASAuthConfigProvider;
import java.util.Map;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Ron Monzillo
 */
public class JAASServletAuthConfigProvider extends JAASAuthConfigProvider {

    private static final String HTTP_SERVLET_LAYER = "HttpServlet";
    private static final String MANDATORY_KEY = "jakarta.security.auth.message.MessagePolicy.isMandatory";
    private static final String MANDATORY_AUTH_CONTEXT_ID = "mandatory";
    private static final String OPTIONAL_AUTH_CONTEXT_ID = "optional";
    private static final Class[] moduleTypes = new Class[] {ServerAuthModule.class};
    private static final Class[] messageTypes = new Class[] {HttpServletRequest.class, HttpServletResponse.class};
    final static MessagePolicy mandatoryPolicy =
            new MessagePolicy(new MessagePolicy.TargetPolicy[]{
                new MessagePolicy.TargetPolicy((MessagePolicy.Target[]) null,
                new MessagePolicy.ProtectionPolicy() {

                    public String getID() {
                        return MessagePolicy.ProtectionPolicy.AUTHENTICATE_SENDER;
                    }
                })}, true);
    final static MessagePolicy optionalPolicy =
            new MessagePolicy(new MessagePolicy.TargetPolicy[]{
                new MessagePolicy.TargetPolicy((MessagePolicy.Target[]) null,
                new MessagePolicy.ProtectionPolicy() {

                    public String getID() {
                        return MessagePolicy.ProtectionPolicy.AUTHENTICATE_SENDER;
                    }
                })}, false);

    public JAASServletAuthConfigProvider(Map properties, AuthConfigFactory factory) {
        super(properties, factory);
    }

    public MessagePolicyDelegate getMessagePolicyDelegate(String appContext) throws AuthException {

        return new MessagePolicyDelegate() {

            public MessagePolicy getRequestPolicy(String authContextID, Map properties) {
                MessagePolicy rvalue;
                if (MANDATORY_AUTH_CONTEXT_ID.equals(authContextID)) {
                    rvalue = mandatoryPolicy;
                } else {
                    rvalue = optionalPolicy;
                }
                return rvalue;
            }

            public MessagePolicy getResponsePolicy(String authContextID, Map properties) {
                return null;
            }

            public Class[] getMessageTypes() {
                return messageTypes;
            }

            public String getAuthContextID(MessageInfo messageInfo) {
                String rvalue;
                if (messageInfo.getMap().containsKey(MANDATORY_KEY)) {
                    rvalue = MANDATORY_AUTH_CONTEXT_ID;
                } else {
                    rvalue = OPTIONAL_AUTH_CONTEXT_ID;
                }
                return rvalue;
            }

            public boolean isProtected() {
                return true;
            }

        };
    }

    @Override
    protected Class[] getModuleTypes() {
        return moduleTypes;
    }

    @Override
    protected String getLayer() {
        return HTTP_SERVLET_LAYER;
    }

    @Override
    public AuthContextHelper getAuthContextHelper(String appContext, boolean returnNullContexts)
            throws AuthException {
        // overrides returnNullContexts to false (as required by Servlet Profile)
        return super.getAuthContextHelper(appContext,false);
    }
}
