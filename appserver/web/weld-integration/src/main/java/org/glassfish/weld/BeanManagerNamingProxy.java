/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/**
 * Proxy for java:comp/BeanManager lookups
 *
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(value = BeanManagerNamingProxy.BEAN_MANAGER_CONTEXT)
public class BeanManagerNamingProxy implements NamedNamingObjectProxy {

    @Inject
    private ComponentEnvManager compEnvManager;

    @Inject
    private InvocationManager invocationManager;

    @Inject
    private WeldDeployer weldDeployer;

    static final String BEAN_MANAGER_CONTEXT = "java:comp/BeanManager";

    public Object handle(String name) throws NamingException {

        Object beanManager = null;

        if (BEAN_MANAGER_CONTEXT.equals(name)) {
            try {

                // Use invocation context to find applicable BeanDeploymentArchive.
                ComponentInvocation inv = invocationManager.getCurrentInvocation();

                if (inv != null) {

                    JndiNameEnvironment componentEnv = compEnvManager.getJndiNameEnvironment(inv.getComponentId());

                    if (componentEnv != null) {

                        BundleDescriptor bundle = null;

                        if (componentEnv instanceof EjbDescriptor) {
                            bundle = (BundleDescriptor) ((EjbDescriptor) componentEnv).getEjbBundleDescriptor().getModuleDescriptor()
                                    .getDescriptor();

                        } else if (componentEnv instanceof WebBundleDescriptor) {
                            bundle = (BundleDescriptor) componentEnv;

                        }

                        if (bundle != null) {
                            BeanDeploymentArchive bda = weldDeployer.getBeanDeploymentArchiveForBundle(bundle);
                            if (bda != null) {
                                WeldBootstrap bootstrap = weldDeployer.getBootstrapForApp(bundle.getApplication());
                                //System.out.println("BeanManagerNamingProxy:: getting BeanManagerImpl for" + bda);
                                beanManager = bootstrap.getManager(bda);
                            }
                        }

                        if (beanManager == null) {
                            throw new IllegalStateException("Cannot resolve bean manager");
                        }

                    } else {
                        throw new IllegalStateException("No invocation context found");
                    }
                }

            } catch (Throwable t) {
                NamingException ne = new NamingException("Error retrieving java:comp/BeanManager");
                ne.initCause(t);
                throw ne;
            }
        }

        return beanManager;
    }

}
