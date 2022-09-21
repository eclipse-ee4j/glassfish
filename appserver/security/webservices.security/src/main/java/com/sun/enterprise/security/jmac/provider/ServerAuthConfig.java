/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.jmac.provider;

import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityDescriptor;
import com.sun.enterprise.security.jauth.AuthConfig;
import com.sun.enterprise.security.jauth.AuthException;
import com.sun.enterprise.security.jauth.AuthPolicy;
import com.sun.enterprise.security.jauth.ServerAuthContext;

import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import java.util.ArrayList;

import javax.security.auth.callback.CallbackHandler;


/**
 * This class is the client container's interface to the AuthConfig subsystem
 * to get AuthContext objects on which to invoke message layer authentication
 * providers. It is not intended to be layer or web services specific (see
 * getMechanisms method at end).
 */
public class ServerAuthConfig extends BaseAuthConfig {

    private ServerAuthConfig(ServerAuthContext defaultContext) {
        super(defaultContext);
    }


    private ServerAuthConfig(ArrayList<MessageSecurityDescriptor> descriptors, ArrayList<ServerAuthContext> authContexts) {
        super(descriptors, authContexts);
    }


    public static ServerAuthConfig getConfig(
        String authLayer,
        MessageSecurityBindingDescriptor binding,
        CallbackHandler cbh) throws AuthException {

        ServerAuthConfig rvalue = null;
        String provider = null;
        ArrayList<MessageSecurityDescriptor> descriptors = null;
        ServerAuthContext defaultContext = null;
        if (binding != null) {
            String layer = binding.getAttributeValue(MessageSecurityBindingDescriptor.AUTH_LAYER);
            if (authLayer != null && layer.equals(authLayer)) {
                provider = binding.getAttributeValue(MessageSecurityBindingDescriptor.PROVIDER_ID);
                descriptors = binding.getMessageSecurityDescriptors();
            }
        }
        if (descriptors == null || descriptors.isEmpty()) {
            defaultContext = getAuthContext(authLayer,provider,null,null,cbh);
            if (defaultContext != null) {
                rvalue = new ServerAuthConfig(defaultContext);
            }
        } else {
            boolean hasPolicy = false;
            ArrayList<ServerAuthContext> authContexts = new ArrayList<>();
            for (MessageSecurityDescriptor msd : descriptors) {
                AuthPolicy requestPolicy = getAuthPolicy(msd.getRequestProtectionDescriptor());
                AuthPolicy responsePolicy = getAuthPolicy(msd.getResponseProtectionDescriptor());
                if (requestPolicy.authRequired() || responsePolicy.authRequired()) {
                    authContexts.add(getAuthContext(authLayer, provider, requestPolicy, responsePolicy, cbh));
                    hasPolicy = true;
                } else {
                    authContexts.add(null);
                }
            }
            if (hasPolicy) {
                rvalue = new ServerAuthConfig(descriptors,authContexts);
            }
        }
        return rvalue;
    }


    private static ServerAuthContext getAuthContext(String layer, String provider, AuthPolicy requestPolicy,
        AuthPolicy responsePolicy, CallbackHandler cbh) throws AuthException {
        AuthConfig authConfig = AuthConfig.getAuthConfig();
        return authConfig.getServerAuthContext(layer, provider, requestPolicy, responsePolicy, cbh);
    }


    public ServerAuthContext getAuthContext(SOAPMessageContext context) {
        return (ServerAuthContext) getContext(context);
    }
}
