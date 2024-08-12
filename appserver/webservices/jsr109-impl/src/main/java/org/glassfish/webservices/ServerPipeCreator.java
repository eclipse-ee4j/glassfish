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

package org.glassfish.webservices;


import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.ServerPipeAssemblerContext;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.assembler.metro.ServerPipelineHook;
import com.sun.xml.ws.policy.PolicyMap;

import jakarta.xml.ws.http.HTTPBinding;

import org.jvnet.hk2.annotations.Contract;

/**
 * This is used by JAXWSContainer to return proper 196 security and
 * app server monitoing pipes to the StandAlonePipeAssembler and
 * TangoPipeAssembler
 */
@Contract
public abstract class ServerPipeCreator extends ServerPipelineHook {

    protected WebServiceEndpoint endpoint;
    protected boolean isHttpBinding;

    protected ServerPipeCreator() {
    }


    public void init(WebServiceEndpoint ep) {
        endpoint = ep;
        isHttpBinding = ((HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding())) ? true : false);
    }


    @Override
    public Pipe createMonitoringPipe(ServerPipeAssemblerContext ctxt, Pipe tail) {
        return new MonitoringPipe(ctxt, tail, endpoint);
    }


    @Override
    public abstract Pipe createSecurityPipe(PolicyMap map, SEIModel sei, WSDLPort port, WSEndpoint owner, Pipe tail);

}
