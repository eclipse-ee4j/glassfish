/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

import com.sun.enterprise.web.WebComponentDecorator;
import com.sun.enterprise.web.WebModule;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

import org.glassfish.api.deployment.DeploymentContext;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.weld.WeldDeployer.WELD_BOOTSTRAP;
import static org.glassfish.weld.WeldDeployer.WELD_DEPLOYMENT;
import static org.glassfish.weld.WeldDeployer.WELD_EXTENSION;

/**
 * This is a decorator which calls Weld implementation to do necessary injection of a web component. It is called by
 * {@link com.sun.web.server.J2EEInstanceListener} before a web component is put into service.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Roger.Kitain@Sun.COM
 */
@Service
public class WebComponentInjectionManager<T> implements WebComponentDecorator<T> {

    @Override
    @SuppressWarnings("unchecked")
    public void decorate(T webComponent, WebModule webModule) {
        if (webModule.getWebBundleDescriptor().hasExtensionProperty(WELD_EXTENSION)) {
            DeploymentContext deploymentContext = webModule.getWebModuleConfig().getDeploymentContext();

            BeanManager beanManager =
                deploymentContext.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class)
                                 .getManager(deploymentContext.getTransientAppMetaData(WELD_DEPLOYMENT, DeploymentImpl.class)
                                                              .getBeanDeploymentArchives()
                                                              .iterator().next());

            Class<T> beanClass = (Class<T>) webComponent.getClass();

            // PENDING : Not available in this Web Beans Release
            CreationalContext<T> creationalContext = beanManager.createCreationalContext(null);
            AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(beanClass);
            InjectionTargetFactory<T> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);

            injectionTargetFactory.createInjectionTarget(null).inject(webComponent, creationalContext);
        }
    }
}
