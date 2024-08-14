/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.webservices.client;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.assembler.metro.dev.ClientPipelineHook;
import com.sun.xml.ws.policy.PolicyMap;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.epicyro.services.AuthConfigRegistrationWrapper;

import static com.sun.xml.wss.provider.wsit.PipeConstants.ASSEMBLER_CONTEXT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.BINDING;
import static com.sun.xml.wss.provider.wsit.PipeConstants.CONTAINER;
import static com.sun.xml.wss.provider.wsit.PipeConstants.ENDPOINT_ADDRESS;
import static com.sun.xml.wss.provider.wsit.PipeConstants.NEXT_PIPE;
import static com.sun.xml.wss.provider.wsit.PipeConstants.POLICY;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SERVICE;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SERVICE_REF;
import static com.sun.xml.wss.provider.wsit.PipeConstants.WSDL_MODEL;

/**
 * This is used by WSClientContainer to return proper Jakarta Authentication security pipe to the StandAlonePipeAssembler and
 * TangoPipeAssembler
 */
public class ClientSecurityPipeCreator extends ClientPipelineHook {

    private ServiceReferenceDescriptor serviceReferenceDescriptor;

    public ClientSecurityPipeCreator() {
    }

    public ClientSecurityPipeCreator(ServiceReferenceDescriptor ref) {
        serviceReferenceDescriptor = ref;
    }

    @Override
    public Pipe createSecurityPipe(PolicyMap policyMap, ClientPipeAssemblerContext clientPipeAssemblerContext, Pipe tail) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(POLICY, policyMap);
        properties.put(WSDL_MODEL, clientPipeAssemblerContext.getWsdlModel());
        properties.put(SERVICE, clientPipeAssemblerContext.getService());
        properties.put(BINDING, clientPipeAssemblerContext.getBinding());
        properties.put(ENDPOINT_ADDRESS, clientPipeAssemblerContext.getAddress());

        if (serviceReferenceDescriptor != null) {
            properties.put(SERVICE_REF, serviceReferenceDescriptor);
        }
        properties.put(NEXT_PIPE, tail);
        properties.put(CONTAINER, clientPipeAssemblerContext.getContainer());
        properties.put(ASSEMBLER_CONTEXT, clientPipeAssemblerContext);

        ClientSecurityPipe clientSecurityPipe = new ClientSecurityPipe(properties, tail);

        AuthConfigRegistrationWrapper listenerWrapper =
            ClientPipeCloser.getInstance().lookupListenerWrapper(serviceReferenceDescriptor);

        // There is a 1-1 mapping between Service_Ref and a ListenerWrapper
        if (listenerWrapper != null) {
            // Override the listener that was created: if one was already registered
            listenerWrapper.incrementReference();
            clientSecurityPipe.getAuthenticationService().setRegistrationWrapper(listenerWrapper);
        } else {
            // Register a new listener
            ClientPipeCloser.getInstance().registerListenerWrapper(
                serviceReferenceDescriptor,
                clientSecurityPipe.getAuthenticationService().getRegistrationWrapper());
        }

        return clientSecurityPipe;
    }

}
