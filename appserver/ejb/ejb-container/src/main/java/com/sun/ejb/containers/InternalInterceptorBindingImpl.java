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

package com.sun.ejb.containers;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.deployment.*;
import org.glassfish.api.invocation.ComponentInvocation;
import java.util.Collection;

import org.glassfish.api.invocation.InvocationManager;
import com.sun.enterprise.container.common.spi.ManagedBeanManager;

public class InternalInterceptorBindingImpl  {

    private ServiceLocator services;

    public InternalInterceptorBindingImpl(ServiceLocator services) {
        this.services = services;
    }

    public void registerInterceptor(Object systemInterceptor) {

        InvocationManager invManager = services.getService(InvocationManager.class);

        ComponentInvocation currentInv = invManager.getCurrentInvocation();

        if(currentInv == null) {
            throw new IllegalStateException("no current invocation");
        } else if (currentInv.getInvocationType() !=
                       ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION) {
            throw new IllegalStateException
                        ("Illegal invocation type : " +  currentInv.getInvocationType() +
                         ".  This operation is only available from a web app context");
        }

        ComponentEnvManager compEnvManager = services.getService(ComponentEnvManager.class);

        JndiNameEnvironment env = compEnvManager.getCurrentJndiNameEnvironment();

        BundleDescriptor webBundle = (BundleDescriptor) env;

        ModuleDescriptor moduleDesc = webBundle.getModuleDescriptor();

        // Register interceptor for EJB components
        if( EjbContainerUtilImpl.isInitialized() ) {

            Collection<EjbBundleDescriptor> ejbBundles =
                    moduleDesc.getDescriptor().getExtensionsDescriptors(EjbBundleDescriptor.class);

            if( ejbBundles.size() == 1) {

                EjbBundleDescriptor ejbBundle = ejbBundles.iterator().next();
                for(EjbDescriptor ejb : ejbBundle.getEjbs()) {
                    BaseContainer container =
                        EjbContainerUtilImpl.getInstance().getContainer(ejb.getUniqueId());
                    container.registerSystemInterceptor(systemInterceptor);

                }
            }

        }

        // Register interceptor for any managed beans
        // TODO Handle 299-enabled case
        ManagedBeanManager managedBeanManager = services.getService(ManagedBeanManager.class,
                "ManagedBeanManagerImpl");
        managedBeanManager.registerRuntimeInterceptor(systemInterceptor, webBundle);
    }
}
