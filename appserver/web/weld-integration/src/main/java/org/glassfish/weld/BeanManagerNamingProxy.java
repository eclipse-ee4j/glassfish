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
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import jakarta.inject.Inject;

import java.lang.System.Logger;

import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.SimpleJndiName;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jvnet.hk2.annotations.Service;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;


/**
 * Proxy for java:comp/BeanManager lookups
 *
 * @author Ken Saks
 */
@Service
@NamespacePrefixes(BeanManagerNamingProxy.BEAN_MANAGER_CONTEXT)
public class BeanManagerNamingProxy implements NamedNamingObjectProxy {
    private static final Logger LOG = System.getLogger(BeanManagerNamingProxy.class.getName());

    static final String BEAN_MANAGER_CONTEXT = SimpleJndiName.JNDI_CTX_JAVA_COMPONENT + "BeanManager";

    @Inject
    private ComponentEnvManager compEnvManager;
    @Inject
    private InvocationManager invocationManager;
    @Inject
    private WeldDeployer weldDeployer;


    @Override
    public Object handle(String name) throws NamingException {
        LOG.log(TRACE, "handle(name={0})", name);
        if (!BEAN_MANAGER_CONTEXT.equals(name)) {
            throw new BeanManagerException("this proxy doesn't handle " + name + ", but just " + BEAN_MANAGER_CONTEXT);
        }
        try {
            // Use invocation context to find applicable BeanDeploymentArchive.
            ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
            if (componentInvocation == null) {
                throw new BeanManagerException("current invocation context is null!");
            }
            final String componentId = componentInvocation.getComponentId();
            final JndiNameEnvironment componentEnv = compEnvManager.getJndiNameEnvironment(componentId);
            if (componentEnv == null) {
                throw new BeanManagerException("no descriptor found for componentId=" + componentId);
            }
            final BundleDescriptor descriptor = getBundleDescriptor(componentEnv);
            final BeanManagerImpl beanManager = getBeanManager(descriptor);
            if (beanManager == null) {
                throw new BeanManagerException("no bean manager found for descriptor.class=" + descriptor.getClass());
            }
            return beanManager;
        } catch (BeanManagerException e) {
            LOG.log(DEBUG, e.getMessage(), e);
            throw e;
        } catch (Throwable t) {
            BeanManagerException e = new BeanManagerException(t);
            // it is here as a trace because some clients swallow exceptions.
            LOG.log(DEBUG, e.getMessage(), e);
            throw e;
        }
    }


    private BundleDescriptor getBundleDescriptor(JndiNameEnvironment descriptor) throws BeanManagerException {
        LOG.log(TRACE, "getBundleDescriptor(descriptor.class={0})", descriptor.getClass());
        if (descriptor instanceof EjbDescriptor) {
            EjbDescriptor ejbDescriptor = (EjbDescriptor) descriptor;
            return (BundleDescriptor) ejbDescriptor.getEjbBundleDescriptor().getModuleDescriptor().getDescriptor();
        } else if (descriptor instanceof WebBundleDescriptor) {
            return (BundleDescriptor) descriptor;
        } else {
            throw new BeanManagerException("no descriptor found for descriptor.class=" + descriptor.getClass());
        }
    }


    private BeanManagerImpl getBeanManager(BundleDescriptor descriptor) throws BeanManagerException {
        LOG.log(TRACE, "getBeanManager(descriptor.class={0})", descriptor.getClass());
        // This one is first because it is quite common.
        BeanDeploymentArchive beanDeploymentArchive = weldDeployer.getBeanDeploymentArchiveForBundle(descriptor);
        if (beanDeploymentArchive == null) {
            throw new BeanManagerException(
                "no CDI bean deployment archive found for descriptor.name=" + descriptor.getName());
        }
        Application application = descriptor.getApplication();
        if (application == null) {
            throw new BeanManagerException(
                "descriptor with name " + descriptor.getName() + " doesn't contain any application.");
        }
        WeldBootstrap weldBootstrap = weldDeployer.getBootstrapForApp(application);
        if (weldBootstrap == null) {
            throw new BeanManagerException("no WeldBootstrap found for application.name=" + application.getName());
        }
        return weldBootstrap.getManager(beanDeploymentArchive);
    }


    private static class BeanManagerException extends NamingException {

        private static final long serialVersionUID = 6867680921109852571L;

        BeanManagerException(final String reason) {
            super("Cannot handle the " + BEAN_MANAGER_CONTEXT + ", because " + reason);
        }

        BeanManagerException(final Throwable cause) {
            super("Cannot handle the " + BEAN_MANAGER_CONTEXT + ".");
            initCause(cause);
        }
    }
}
