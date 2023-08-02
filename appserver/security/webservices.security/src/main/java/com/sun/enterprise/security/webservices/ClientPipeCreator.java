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

import java.util.HashMap;

import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.assembler.metro.dev.ClientPipelineHook;
//import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.policy.PolicyMap;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.pipe.Tube;

/**
 * This is used by WSClientContainer to return proper 196 security pipe
 * to the StandAlonePipeAssembler and TangoPipeAssembler
 */
public class ClientPipeCreator extends ClientPipelineHook {

    private ServiceReferenceDescriptor svcRef = null;

    public ClientPipeCreator(){
    }

    public ClientPipeCreator(ServiceReferenceDescriptor ref){
        svcRef = ref;
    }

    @Override
    public Pipe createSecurityPipe(PolicyMap map,
            ClientPipeAssemblerContext ctxt, Pipe tail) {
        HashMap propBag = new HashMap();
        propBag.put(PipeConstants.POLICY, map);
        propBag.put(PipeConstants.WSDL_MODEL, ctxt.getWsdlModel());
        propBag.put(PipeConstants.SERVICE, ctxt.getService());
        propBag.put(PipeConstants.BINDING, ctxt.getBinding());
        propBag.put(PipeConstants.ENDPOINT_ADDRESS, ctxt.getAddress());
        if (svcRef != null) {
            propBag.put(PipeConstants.SERVICE_REF, svcRef);
        }
    propBag.put(PipeConstants.NEXT_PIPE,tail);
        propBag.put(PipeConstants.CONTAINER,ctxt.getContainer());
        propBag.put(PipeConstants.ASSEMBLER_CONTEXT, ctxt);
        ClientSecurityPipe ret = new ClientSecurityPipe(propBag, tail);
        org.omnifaces.eleos.services.AuthConfigRegistrationWrapper listenerWrapper = ClientPipeCloser.getInstance().lookupListenerWrapper(svcRef);
        //there is a 1-1 mapping between Service_Ref and a ListenerWrapper
        if (listenerWrapper != null) {
            //override the listener that was created by the ConfigHelper CTOR :if one was already registered
            listenerWrapper.incrementReference();
            ret.getPipeHelper().setRegistrationWrapper(listenerWrapper);
        } else {
            //register a new listener
            ClientPipeCloser.getInstance().registerListenerWrapper(
                    svcRef, ret.getPipeHelper().getRegistrationWrapper());
        }

        return ret;
    }

//    @Override
//    public @NotNull
//    Tube createSecurityTube(ClientTubelineAssemblyContext ctxt) {
//
//
//        HashMap propBag = new HashMap();
//        /*TODO V3 enable
//        propBag.put(PipeConstants.POLICY, map);
//        propBag.put(PipeConstants.WSDL_MODEL, ctxt.getWsdlModel());
//        propBag.put(PipeConstants.SERVICE, ctxt.getService());
//        propBag.put(PipeConstants.BINDING, ctxt.getBinding());
//        propBag.put(PipeConstants.ENDPOINT_ADDRESS, ctxt.getAddress());
//        propBag.put(PipeConstants.SERVICE_REF, svcRef);
//    propBag.put(PipeConstants.NEXT_PIPE,tail);
//        propBag.put(PipeConstants.CONTAINER,ctxt.getContainer());
//         */
//        ClientSecurityTube ret = new ClientSecurityTube(propBag, ctxt.getTubelineHead());
//        AuthConfigRegistrationWrapper listenerWrapper = ClientPipeCloser.getInstance().lookupListenerWrapper(svcRef);
//        //there is a 1-1 mapping between Service_Ref and a ListenerWrapper
//        if (listenerWrapper != null) {
//            //override the listener that was created by the ConfigHelper CTOR :if one was already registered
//            listenerWrapper.incrementReference();
//            ret.getPipeHelper().setRegistrationWrapper(listenerWrapper);
//        } else {
//            //register a new listener
//            ClientPipeCloser.getInstance().registerListenerWrapper(
//                    svcRef, ret.getPipeHelper().getRegistrationWrapper());
//        }
//
//        return ret;
//
//    }

}
