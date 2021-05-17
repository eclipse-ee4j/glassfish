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

package com.sun.enterprise.security.jmac.provider;

import com.sun.enterprise.security.jauth.*;
import java.util.ArrayList;
import javax.security.auth.callback.CallbackHandler;

import com.sun.enterprise.deployment.runtime.common.MessageSecurityDescriptor;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;

import jakarta.xml.soap.SOAPMessage;

/**
 * This class is the client container's interface to the AuthConfig subsystem
 * to get AuthContext objects on which to invoke message layer authentication
 * providers. It is not intended to be layer or web services specific (see
 * getMechanisms method at end).
 */
public class ClientAuthConfig extends BaseAuthConfig {

    private ClientAuthConfig(ClientAuthContext defaultContext) {
    super(defaultContext);
    }

    private ClientAuthConfig (ArrayList descriptors, ArrayList authContexts) {
    super(descriptors,authContexts);
    }

    public static ClientAuthConfig getConfig
    (String authLayer, MessageSecurityBindingDescriptor binding,
     CallbackHandler cbh) throws AuthException {
    ClientAuthConfig rvalue = null;
    String provider = null;
    ArrayList descriptors = null;
    ClientAuthContext defaultContext = null;
    if (binding != null) {
        String layer = binding.getAttributeValue
        (MessageSecurityBindingDescriptor.AUTH_LAYER);
        if (authLayer != null && layer.equals(authLayer)) {
        provider = binding.getAttributeValue
            (MessageSecurityBindingDescriptor.PROVIDER_ID);
        descriptors = binding.getMessageSecurityDescriptors();
        }
    }
    if (descriptors == null || descriptors.size() == 0) {
        defaultContext = getAuthContext(authLayer,provider,null,null,cbh);
        if (defaultContext != null) {
        rvalue = new ClientAuthConfig(defaultContext);
        }
    } else {
        boolean hasPolicy = false;
        ArrayList authContexts = new ArrayList();
        for (int i = 0; i < descriptors.size(); i++) {
        MessageSecurityDescriptor msd =
            (MessageSecurityDescriptor) descriptors.get(i);
        AuthPolicy requestPolicy =
            getAuthPolicy(msd.getRequestProtectionDescriptor());
        AuthPolicy responsePolicy =
            getAuthPolicy(msd.getResponseProtectionDescriptor());
         if (requestPolicy.authRequired()||responsePolicy.authRequired()) {
            authContexts.add
            (getAuthContext
             (authLayer,provider,requestPolicy,responsePolicy,cbh));
            hasPolicy = true;
        } else {
            authContexts.add(null);
        }
        }
        if (hasPolicy) {
        rvalue = new ClientAuthConfig(descriptors,authContexts);
        }
    }
    return rvalue;
    }

    private static ClientAuthContext getAuthContext
    (String layer, String provider, AuthPolicy requestPolicy,
     AuthPolicy responsePolicy,CallbackHandler cbh) throws AuthException {
    AuthConfig authConfig = AuthConfig.getAuthConfig();
    return authConfig.getClientAuthContext
        (layer,provider,requestPolicy,responsePolicy,cbh);
    }

    public ClientAuthContext getAuthContext
    (jakarta.xml.ws.handler.soap.SOAPMessageContext context) {
    return (ClientAuthContext) getContext(context);
    }

}
