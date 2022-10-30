/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import jakarta.inject.Inject;

import java.util.logging.Logger;

import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.SimpleJndiName;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINEST;

/**
 * Proxy for java:comp/BeanManager lookups
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(BeanManagerNamingProxy.BEAN_MANAGER_CONTEXT)
public class BeanManagerNamingProxy implements NamedNamingObjectProxy {
    private static final Logger LOG = Logger.getLogger(BeanManagerNamingProxy.class.getName());

    static final String BEAN_MANAGER_CONTEXT = SimpleJndiName.JNDI_CTX_JAVA_COMPONENT + "BeanManager";

    @Inject
    private ComponentEnvManager compEnvManager;
    @Inject
    private InvocationManager invocationManager;
    @Inject
    private WeldDeployer weldDeployer;


    @Override
    public Object handle(String name) throws NamingException {
        LOG.log(FINEST, "handle(name={0})", name);
        if (!BEAN_MANAGER_CONTEXT.equals(name)) {
            return null;
        }
        try {
            // Use invocation context to find applicable BeanDeploymentArchive.
            ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
            if (componentInvocation == null) {
                return null;
            }
            final String componentId = componentInvocation.getComponentId();
            final JndiNameEnvironment componentEnv = compEnvManager.getJndiNameEnvironment(componentId);
            if (componentEnv == null) {
                throw new IllegalStateException("No invocation context found for componentId=" + componentId);
            }
            final BundleDescriptor descriptor = getBundleDescriptor(componentEnv);
            final BeanManagerImpl beanManager = getBeanManager(descriptor);
            if (beanManager == null) {
                throw new IllegalStateException("No bean manager found for descriptor.class=" + descriptor.getClass());
            }
            return beanManager;
        } catch (Throwable t) {
            NamingException ne = new NamingException("Error retrieving " + BEAN_MANAGER_CONTEXT);
            ne.initCause(t);
            throw ne;
        }
    }


    private BundleDescriptor getBundleDescriptor(JndiNameEnvironment componentEnv) {
        LOG.log(FINEST, "getBundleDescriptor(componentEnv.class={0})", componentEnv.getClass());
        if (componentEnv instanceof EjbDescriptor) {
            EjbDescriptor ejbDescriptor = (EjbDescriptor) componentEnv;
            return (BundleDescriptor) ejbDescriptor.getEjbBundleDescriptor().getModuleDescriptor().getDescriptor();
        } else if (componentEnv instanceof WebBundleDescriptor) {
            return (BundleDescriptor) componentEnv;
        } else {
            throw new IllegalStateException(
                "No descriptor found for jndiNameEnvironment.class=" + componentEnv.getClass());
        }
    }


    private BeanManagerImpl getBeanManager(BundleDescriptor descriptor) {
        LOG.log(FINEST, "getBeanManager(descriptor.class={0})", descriptor.getClass());
        BeanDeploymentArchive beanDeploymentArchive = weldDeployer.getBeanDeploymentArchiveForBundle(descriptor);
        if (beanDeploymentArchive == null) {
            throw new IllegalStateException("No archive found for descriptor.class=" + descriptor.getClass());
        }
        return weldDeployer.getBootstrapForApp(descriptor.getApplication()).getManager(beanDeploymentArchive);
    }

}
