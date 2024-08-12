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
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ContainerResolver;

/**
 * App Server container resolver for registering client side
 * security pipe
 * @author Bhakti Mehta
 */
public class WSContainerResolver extends ContainerResolver {


    private static final ThreadLocal<ServiceReferenceDescriptor> refs;

    static {
        refs = new ThreadLocal<ServiceReferenceDescriptor>();
        WSContainerResolver resolver = new WSContainerResolver();
        ContainerResolver.setInstance(resolver);
    }

    private  WSContainerResolver() {
    }


    public static void set(ServiceReferenceDescriptor ref) {
        refs.set(ref);
    }

    public static void unset() {
        refs.set(null);
    }


    @Override
    public Container getContainer() {
        ServiceReferenceDescriptor svcRef = refs.get();

        if (svcRef != null) {
            return new WSClientContainer(svcRef);
        } else {
            return Container.NONE;
        }
    }
}
