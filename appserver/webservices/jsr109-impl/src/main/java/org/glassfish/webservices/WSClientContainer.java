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

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.xml.ws.api.client.ServiceInterceptor;
import com.sun.xml.ws.api.server.Container;

import org.glassfish.internal.api.Globals;

public class WSClientContainer extends Container {

    ServiceReferenceDescriptor svcRef;
    private org.glassfish.webservices.SecurityService  secServ;

    public WSClientContainer(ServiceReferenceDescriptor ref) {
        svcRef = ref;
        if (Globals.getDefaultHabitat() != null) {
            secServ = Globals.get(org.glassfish.webservices.SecurityService.class);
        }
    }

    public <T> T getSPI(Class<T> spiType) {

        if (spiType == com.sun.xml.ws.assembler.metro.dev.ClientPipelineHook.class) {
            if (secServ != null) {
                return((T)(secServ.getClientPipelineHook(svcRef)));
            }
        }
        if((spiType == ServiceInterceptor.class)){
            return((T)(new PortCreationCallbackImpl(svcRef)));
        }
        return null;
    }
}
