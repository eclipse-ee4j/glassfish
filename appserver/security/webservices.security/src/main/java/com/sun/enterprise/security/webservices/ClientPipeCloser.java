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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.glassfish.epicyro.services.AuthConfigRegistrationWrapper;

import com.sun.enterprise.deployment.ServiceReferenceDescriptor;

public class ClientPipeCloser {

    private Map<ServiceReferenceDescriptor, AuthConfigRegistrationWrapper> svcRefListenerMap =
        Collections.synchronizedMap(new WeakHashMap<ServiceReferenceDescriptor, AuthConfigRegistrationWrapper>());

    private ClientPipeCloser() {}

    private static final ClientPipeCloser INSTANCE = new ClientPipeCloser();

    public static  ClientPipeCloser getInstance() {
        return INSTANCE;
    }

    public void registerListenerWrapper(ServiceReferenceDescriptor desc, AuthConfigRegistrationWrapper wrapper) {
        svcRefListenerMap.put(desc,wrapper);
    }

    public AuthConfigRegistrationWrapper lookupListenerWrapper(ServiceReferenceDescriptor desc) {
        AuthConfigRegistrationWrapper listenerWrapper = svcRefListenerMap.get(desc);
        return listenerWrapper;
    }

    public void removeListenerWrapper(AuthConfigRegistrationWrapper wrapper) {
       ServiceReferenceDescriptor entryToRemove = null;

       for (ServiceReferenceDescriptor svc : svcRefListenerMap.keySet()) {
           AuthConfigRegistrationWrapper wrp = svcRefListenerMap.get(svc);
           if (wrp == wrapper) {
              entryToRemove = svc;
              break;
           }
       }
       if (entryToRemove != null) {
          svcRefListenerMap.remove(entryToRemove);
       }
    }

    public void cleanupClientPipe(ServiceReferenceDescriptor desc) {
        AuthConfigRegistrationWrapper listenerWrapper = svcRefListenerMap.get(desc);
        if (listenerWrapper != null) {
            listenerWrapper.disable();
        }
        svcRefListenerMap.remove(desc);
    }
}
