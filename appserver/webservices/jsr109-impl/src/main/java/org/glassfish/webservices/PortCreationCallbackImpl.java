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

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.xml.ws.api.client.ServiceInterceptor;
import com.sun.xml.ws.developer.WSBindingProvider;

import jakarta.xml.ws.Binding;
import jakarta.xml.ws.soap.SOAPBinding;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is way port creation calls are going to be intercepted in JAXWS2.1
 */
public class PortCreationCallbackImpl extends ServiceInterceptor {

    private ServiceReferenceDescriptor ref;

    private static Logger logger = LogUtils.getLogger();

    public PortCreationCallbackImpl(ServiceReferenceDescriptor svcRef) {
        ref = svcRef;
    }

    public void postCreateProxy(WSBindingProvider bp, Class<?> serviceEndpointInterface) {

        ServiceRefPortInfo portInfo = ref.getPortInfoBySEI(serviceEndpointInterface.getName());
        if (portInfo!=null) {
            // Set MTOM for this port
            boolean mtomEnabled = false;
            if(portInfo.getMtomEnabled() != null &&
                Boolean.valueOf( portInfo.getMtomEnabled())) {
                mtomEnabled = true;
            }
            if (mtomEnabled) {
                Binding bType = bp.getBinding();
                // enable mtom valid only for SOAPBindings
                if(SOAPBinding.class.isAssignableFrom(bType.getClass())) {
                    ((SOAPBinding)bType).setMTOMEnabled(true);
                } else {
                    logger.log(Level.SEVERE, LogUtils.INVALID_MTOM, portInfo.getName());
                }
            }

            // Set stub properties
            Set properties = portInfo.getStubProperties();
            for(Iterator iter = properties.iterator(); iter.hasNext();) {
                NameValuePairDescriptor next = (NameValuePairDescriptor)
                    iter.next();
                bp.getRequestContext().put(next.getName(), next.getValue());

            }
        }
    }

    public void postCreateDispatch(WSBindingProvider bp) {}
}
