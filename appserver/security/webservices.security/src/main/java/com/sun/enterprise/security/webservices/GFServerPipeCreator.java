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

import static com.sun.enterprise.util.Utility.isAnyNull;
import static com.sun.xml.ws.policy.PolicyMap.createWsdlEndpointScopeKey;
import static com.sun.xml.ws.policy.PolicyMap.createWsdlOperationScopeKey;

import java.util.HashMap;
import java.util.Map;

import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.wss.provider.wsit.PipeConstants;

import jakarta.inject.Singleton;

/**
 * This is used by JAXWSContainer to return proper Jakarta Authentication security and app server monitoring pipes to
 * the StandAlonePipeAssembler and TangoPipeAssembler
 */
@Service
@Singleton
public class GFServerPipeCreator extends org.glassfish.webservices.ServerPipeCreator {

    private static final String SECURITY_POLICY_NAMESPACE_URI_SUBMISSION = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    private static final String SECURITY_POLICY_NAMESPACE_URI_SPECVERSION = "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702";

    public GFServerPipeCreator() {
        super();
    }

    @Override
    public void init(WebServiceEndpoint ep) {
        super.init(ep);
    }

    @Override
    public Pipe createSecurityPipe(PolicyMap policyMap, SEIModel sei, WSDLPort port, WSEndpoint owner, Pipe tail) {

        Map<String, Object> props = new HashMap<>();

        props.put(PipeConstants.POLICY, policyMap);
        props.put(PipeConstants.SEI_MODEL, sei);
        props.put(PipeConstants.WSDL_MODEL, port);
        props.put(PipeConstants.ENDPOINT, owner);
        props.put(PipeConstants.SERVICE_ENDPOINT, endpoint);
        props.put(PipeConstants.NEXT_PIPE, tail);
        props.put(PipeConstants.CONTAINER, owner.getContainer());
        if (isSecurityEnabled(policyMap, port)) {
            endpoint.setSecurePipeline();
        }

        return new CommonServerSecurityPipe(props, tail, isHttpBinding);
    }

    /**
     * Checks to see whether WS-Security is enabled or not.
     *
     * @param policyMap policy map for {@link this} assembler
     * @param wsdlPort wsdl:port
     * @return true if Security is enabled, false otherwise
     */
    // TODO - this code has been copied from PipelineAssemblerFactoryImpl.java and needs
    // to be maintained in both places. In the future, code needs to be moved somewhere
    // where it can be invoked from both places.
    public static boolean isSecurityEnabled(PolicyMap policyMap, WSDLPort wsdlPort) {
        if (isAnyNull(policyMap, wsdlPort)) {
            return false;
        }

        try {
            Policy policy =
                policyMap.getEndpointEffectivePolicy(
                    createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(), wsdlPort.getName()));

            if (isSecured(policy)) {
                return true;
            }

            for (WSDLBoundOperation operation : wsdlPort.getBinding().getBindingOperations()) {
                PolicyMapKey operationKey = createWsdlOperationScopeKey(wsdlPort.getOwner().getName(), wsdlPort.getName(), operation.getName());

                policy = policyMap.getOperationEffectivePolicy(operationKey);
                if (isSecured(policy)) {
                    return true;
                }

                policy = policyMap.getInputMessageEffectivePolicy(operationKey);
                if (isSecured(policy)) {
                    return true;
                }

                policy = policyMap.getOutputMessageEffectivePolicy(operationKey);
                if (isSecured(policy)) {
                    return true;
                }

                policy = policyMap.getFaultMessageEffectivePolicy(operationKey);
                if (isSecured(policy)) {
                    return true;
                }
            }
        } catch (PolicyException e) {
            return false;
        }

        return false;
    }

    private static boolean isSecured(Policy policy) {
        if (policy == null) {
            return false;
        }

        return policy.contains(SECURITY_POLICY_NAMESPACE_URI_SPECVERSION) || policy.contains(SECURITY_POLICY_NAMESPACE_URI_SUBMISSION);
    }

}
