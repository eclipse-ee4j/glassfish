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

package com.sun.enterprise.security.webservices;

import static com.sun.enterprise.security.webservices.PipeConstants.ASSEMBLER_CONTEXT;
import static com.sun.enterprise.security.webservices.PipeConstants.BINDING;
import static com.sun.enterprise.security.webservices.PipeConstants.CONTAINER;
import static com.sun.enterprise.security.webservices.PipeConstants.ENDPOINT_ADDRESS;
import static com.sun.enterprise.security.webservices.PipeConstants.NEXT_PIPE;
import static com.sun.enterprise.security.webservices.PipeConstants.POLICY;
import static com.sun.enterprise.security.webservices.PipeConstants.SERVICE;
import static com.sun.enterprise.security.webservices.PipeConstants.SERVICE_REF;
import static com.sun.enterprise.security.webservices.PipeConstants.WSDL_MODEL;

import java.util.HashMap;
import java.util.Map;

import org.omnifaces.eleos.services.AuthConfigRegistrationWrapper;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
//import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.assembler.metro.dev.ClientPipelineHook;
import com.sun.xml.ws.policy.PolicyMap;

/**
 * This is used by WSClientContainer to return proper 196 security pipe to the StandAlonePipeAssembler and
 * TangoPipeAssembler
 */
public class ClientPipeCreator extends ClientPipelineHook {

    private ServiceReferenceDescriptor serviceReferenceDescriptor;

    public ClientPipeCreator() {
    }

    public ClientPipeCreator(ServiceReferenceDescriptor ref) {
        serviceReferenceDescriptor = ref;
    }

    @Override
    public Pipe createSecurityPipe(PolicyMap policyMap, ClientPipeAssemblerContext clientPipeAssemblerContext, Pipe tail) {
        Map<String, Object> propBag = new HashMap<>();
        propBag.put(POLICY, policyMap);
        propBag.put(WSDL_MODEL, clientPipeAssemblerContext.getWsdlModel());
        propBag.put(SERVICE, clientPipeAssemblerContext.getService());
        propBag.put(BINDING, clientPipeAssemblerContext.getBinding());
        propBag.put(ENDPOINT_ADDRESS, clientPipeAssemblerContext.getAddress());
        if (serviceReferenceDescriptor != null) {
            propBag.put(SERVICE_REF, serviceReferenceDescriptor);
        }
        propBag.put(NEXT_PIPE, tail);
        propBag.put(CONTAINER, clientPipeAssemblerContext.getContainer());
        propBag.put(ASSEMBLER_CONTEXT, clientPipeAssemblerContext);

        ClientSecurityPipe clientSecurityPipe = new ClientSecurityPipe(propBag, tail);
        AuthConfigRegistrationWrapper listenerWrapper =
            ClientPipeCloser.getInstance().lookupListenerWrapper(serviceReferenceDescriptor);

        // There is a 1-1 mapping between Service_Ref and a ListenerWrapper
        if (listenerWrapper != null) {
            // Override the listener that was created by the ConfigHelper CTOR :if one was already registered
            listenerWrapper.incrementReference();
            clientSecurityPipe.getPipeHelper().setRegistrationWrapper(listenerWrapper);
        } else {
            // Register a new listener
            ClientPipeCloser.getInstance().registerListenerWrapper(
                serviceReferenceDescriptor,
                clientSecurityPipe.getPipeHelper().getRegistrationWrapper());
        }

        return clientSecurityPipe;
    }

}
