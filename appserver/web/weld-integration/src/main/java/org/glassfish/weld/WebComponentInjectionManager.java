/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collection;

import org.glassfish.api.deployment.DeploymentContext;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.web.WebComponentDecorator;
import com.sun.enterprise.web.WebModule;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

/**
 * This is a decorator which calls Weld implemetation to do necessary injection of a web component. It is called by
 * {@link com.sun.web.server.J2EEInstanceListener} before a web component is put into service.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Roger.Kitain@Sun.COM
 */
@Service
public class WebComponentInjectionManager<T> implements WebComponentDecorator<T> {
    @Override
    @SuppressWarnings("unchecked")
    public void decorate(T webComponent, WebModule wm) {
        if (wm.getWebBundleDescriptor().hasExtensionProperty(WeldDeployer.WELD_EXTENSION)) {
            DeploymentContext deploymentContext = wm.getWebModuleConfig().getDeploymentContext();
            WeldBootstrap weldBootstrap = deploymentContext.getTransientAppMetaData(WeldDeployer.WELD_BOOTSTRAP,
                    org.jboss.weld.bootstrap.WeldBootstrap.class);

            DeploymentImpl deploymentImpl = deploymentContext.getTransientAppMetaData(WeldDeployer.WELD_DEPLOYMENT, DeploymentImpl.class);
            Collection<BeanDeploymentArchive> deployments = deploymentImpl.getBeanDeploymentArchives();
            BeanDeploymentArchive beanDeploymentArchive = deployments.iterator().next();
            BeanManager beanManager = weldBootstrap.getManager(beanDeploymentArchive);
            // PENDING : Not available in this Web Beans Release
            CreationalContext<T> ccontext = beanManager.createCreationalContext(null);
            @SuppressWarnings("rawtypes")
            Class<T> clazz = (Class<T>) webComponent.getClass();
            AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(clazz);
            InjectionTarget<T> injectionTarget = beanManager.createInjectionTarget(annotatedType);
            injectionTarget.inject(webComponent, ccontext);
        }
    }
}
