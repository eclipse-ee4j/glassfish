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

package com.sun.enterprise.security.webservices.server;

import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;

import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.webservices.ServerPipeCreator;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.Utility.isAnyNull;
import static com.sun.xml.ws.policy.PolicyMap.createWsdlEndpointScopeKey;
import static com.sun.xml.ws.policy.PolicyMap.createWsdlOperationScopeKey;
import static com.sun.xml.wss.provider.wsit.PipeConstants.CONTAINER;
import static com.sun.xml.wss.provider.wsit.PipeConstants.ENDPOINT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.NEXT_PIPE;
import static com.sun.xml.wss.provider.wsit.PipeConstants.POLICY;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SEI_MODEL;
import static com.sun.xml.wss.provider.wsit.PipeConstants.SERVICE_ENDPOINT;
import static com.sun.xml.wss.provider.wsit.PipeConstants.WSDL_MODEL;

/**
 * This is used by JAXWSContainer to return proper Jakarta Authentication security and app server monitoring pipes to
 * the StandAlonePipeAssembler and TangoPipeAssembler
 */
@Service
@Singleton
public class ServerSecurityPipeCreator extends ServerPipeCreator {

    private static final String SECURITY_POLICY_NAMESPACE_URI_SUBMISSION = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    private static final String SECURITY_POLICY_NAMESPACE_URI_SPECVERSION = "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702";

    @Override
    public Pipe createSecurityPipe(PolicyMap policyMap, SEIModel seiModel, WSDLPort port, WSEndpoint ownerEndpoint, Pipe tail) {

        Map<String, Object> properties = new HashMap<>();

        properties.put(POLICY, policyMap);
        properties.put(SEI_MODEL, seiModel);
        properties.put(WSDL_MODEL, port);
        properties.put(ENDPOINT, ownerEndpoint);
        properties.put(SERVICE_ENDPOINT, endpoint);
        properties.put(NEXT_PIPE, tail);
        properties.put(CONTAINER, ownerEndpoint.getContainer());

        if (isSecurityEnabled(policyMap, port)) {
            endpoint.setSecurePipeline();
        }

        return new ServerSecurityPipe(properties, tail, isHttpBinding);
    }

    /**
     * Checks to see whether WS-Security is enabled or not.
     *
     * @param policyMap policy map for {@link this} assembler
     * @param wsdlPort wsdl:port
     * @return true if Security is enabled, false otherwise
     */
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
