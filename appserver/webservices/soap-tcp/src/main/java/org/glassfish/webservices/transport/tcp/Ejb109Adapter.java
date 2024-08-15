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

package org.glassfish.webservices.transport.tcp;

import com.sun.xml.ws.api.server.WSEndpoint;

import org.glassfish.webservices.AdapterInvocationInfo;
import org.glassfish.webservices.EjbRuntimeEndpointInfo;

/**
 * EJB SOAP/TCP WebService Adapter for GFv3
 *
 * @author Alexey Stashok
 */
public final class Ejb109Adapter extends TCP109Adapter {
    private final EjbRuntimeEndpointInfo ejbRuntimeEndpointInfo;
    private final AdapterInvocationInfo adapterInfo;

    public Ejb109Adapter(String name, String urlPattern, WSEndpoint endpoint,
            ServletFakeArtifactSet servletFakeArtifactSet,
            EjbRuntimeEndpointInfo ejbRuntimeEndpointInfo,
            AdapterInvocationInfo adapterInfo) {
        super(name, urlPattern, endpoint, servletFakeArtifactSet, true);
        this.ejbRuntimeEndpointInfo = ejbRuntimeEndpointInfo;
        this.adapterInfo = adapterInfo;
    }

    @Override
    protected void beforeHandle() {
    }

    @Override
    protected void postHandle() {
        ejbRuntimeEndpointInfo.releaseImplementor(adapterInfo.getInv());
    }


}
