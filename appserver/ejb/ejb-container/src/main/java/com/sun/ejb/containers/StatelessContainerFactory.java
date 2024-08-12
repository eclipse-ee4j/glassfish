/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import com.sun.ejb.Container;
import com.sun.ejb.ContainerFactory;
import com.sun.enterprise.security.SecurityManager;

import jakarta.inject.Singleton;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.jvnet.hk2.annotations.Service;

@Service(name = "StatelessContainerFactory")
@Singleton
public class StatelessContainerFactory extends BaseContainerFactory implements
        ContainerFactory {
    @Override
    public Container createContainer(EjbDescriptor ejbDescriptor,
                                     ClassLoader loader,
                                     DeploymentContext deployContext)
            throws Exception {
        SecurityManager sm = getSecurityManager(ejbDescriptor);
        StatelessSessionContainer slsbContainer = new StatelessSessionContainer(ejbDescriptor,
                loader, sm);
        slsbContainer.initializeHome();
        return slsbContainer;
    }
}
