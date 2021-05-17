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
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.PolicyMap;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMapKey;

import com.sun.xml.wss.provider.wsit.PipeConstants;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;


/**
 * This is used by JAXWSContainer to return proper 196 security and
 *  app server monitoing pipes to the StandAlonePipeAssembler and
 *  TangoPipeAssembler
 */
@Service
@Singleton
public class GFServerPipeCreator extends org.glassfish.webservices.ServerPipeCreator {

    private static final String SECURITY_POLICY_NAMESPACE_URI_SUBMISSION =
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    private static final String SECURITY_POLICY_NAMESPACE_URI_SPECVERSION=
            "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702";

    public GFServerPipeCreator(){
        super();
    }

    public void init(WebServiceEndpoint ep) {
        super.init(ep);
    }
    public Pipe createSecurityPipe(PolicyMap map, SEIModel sei,
            WSDLPort port, WSEndpoint owner, Pipe tail) {

    HashMap props = new HashMap();

    props.put(PipeConstants.POLICY,map);
    props.put(PipeConstants.SEI_MODEL,sei);
    props.put(PipeConstants.WSDL_MODEL,port);
    props.put(PipeConstants.ENDPOINT,owner);
    props.put(PipeConstants.SERVICE_ENDPOINT,endpoint);
    props.put(PipeConstants.NEXT_PIPE,tail);
        props.put(PipeConstants.CONTAINER, owner.getContainer());
        if (isSecurityEnabled(map, port)) {
        endpoint.setSecurePipeline();
        }

        return new CommonServerSecurityPipe(props, tail, isHttpBinding);
    }

//    @Override
//    public @NotNull
//    Tube createSecurityTube(ServerTubelineAssemblyContext ctxt) {
//        HashMap props = new HashMap();
//
//        /*TODO V3 enable
//    props.put(PipeConstants.POLICY,map);
//    props.put(PipeConstants.SEI_MODEL,sei);
//    props.put(PipeConstants.WSDL_MODEL,port);
//    props.put(PipeConstants.ENDPOINT,owner);
//    props.put(PipeConstants.SERVICE_ENDPOINT,endpoint);
//    props.put(PipeConstants.NEXT_PIPE,tail);
//        props.put(PipeConstants.CONTAINER, owner.getContainer());
//        if (isSecurityEnabled(map, port)) {
//        endpoint.setSecurePipeline();
//        }*/
//
//        return new CommonServerSecurityTube(props, ctxt.getTubelineHead(), isHttpBinding);
//    }
    /**
     * Checks to see whether WS-Security is enabled or not.
     *
     * @param policyMap policy map for {@link this} assembler
     * @param wsdlPort wsdl:port
     * @return true if Security is enabled, false otherwise
     */
    //TODO - this code has been copied from PipelineAssemblerFactoryImpl.java and needs
    //to be maintained in both places.  In the future, code needs to be moved somewhere
    //where it can be invoked from both places.
    public static  boolean isSecurityEnabled(PolicyMap policyMap, WSDLPort wsdlPort) {
        if (policyMap == null || wsdlPort == null)
            return false;

        try {
            PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                    wsdlPort.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

            if ((policy != null) && (policy.contains(SECURITY_POLICY_NAMESPACE_URI_SPECVERSION) ||
                    policy.contains(SECURITY_POLICY_NAMESPACE_URI_SUBMISSION))) {
                return true;
            }

            for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                PolicyMapKey operationKey = policyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                        wsdlPort.getName(),
                        wbo.getName());
                policy = policyMap.getOperationEffectivePolicy(operationKey);
                if ((policy != null) && (policy.contains(SECURITY_POLICY_NAMESPACE_URI_SPECVERSION) ||
                    policy.contains(SECURITY_POLICY_NAMESPACE_URI_SUBMISSION)))
                    return true;

                policy = policyMap.getInputMessageEffectivePolicy(operationKey);
                if ((policy != null) && (policy.contains(SECURITY_POLICY_NAMESPACE_URI_SPECVERSION) ||
                    policy.contains(SECURITY_POLICY_NAMESPACE_URI_SUBMISSION)))
                    return true;

                policy = policyMap.getOutputMessageEffectivePolicy(operationKey);
                if ((policy != null) && (policy.contains(SECURITY_POLICY_NAMESPACE_URI_SPECVERSION) ||
                    policy.contains(SECURITY_POLICY_NAMESPACE_URI_SUBMISSION)))
                    return true;

                policy = policyMap.getFaultMessageEffectivePolicy(operationKey);
                if ((policy != null) && (policy.contains(SECURITY_POLICY_NAMESPACE_URI_SPECVERSION) ||
                    policy.contains(SECURITY_POLICY_NAMESPACE_URI_SUBMISSION)))
                    return true;
            }
        } catch (PolicyException e) {
            return false;
        }

        return false;
    }

}
