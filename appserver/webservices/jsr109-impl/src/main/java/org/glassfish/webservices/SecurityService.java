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

package org.glassfish.webservices;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.web.WebModule;
import com.sun.xml.ws.assembler.metro.dev.ClientPipelineHook;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author Kumar
 */
@Contract
public interface SecurityService {

    boolean doSecurity(HttpServletRequest hreq, EjbRuntimeEndpointInfo ejbEndpoint, String realmName, WebServiceContextImpl context);
    Principal getUserPrincipal(boolean isWeb);
    boolean isUserInRole(WebModule webModule, Principal principal, String servletName, String role);

    void resetSecurityContext();
    void resetPolicyContext();
    ClientPipelineHook getClientPipelineHook(ServiceReferenceDescriptor ref);
}
