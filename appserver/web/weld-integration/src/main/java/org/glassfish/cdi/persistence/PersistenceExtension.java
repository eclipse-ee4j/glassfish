/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.cdi.persistence;

import com.sun.enterprise.container.common.impl.ComponentEnvManagerImpl;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.persistence.EntityManager;

import java.lang.annotation.Annotation;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;

import static com.sun.enterprise.util.AnnotationUtil.createAnnotationInstance;

public class PersistenceExtension implements Extension  {

    @SuppressWarnings("unchecked")
    public void afterBean(final @Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        var container = Globals.get(InvocationManager.class)
                                  .getCurrentInvocation()
                                  .getContainer();

        if (container instanceof ApplicationInfo applicationInfo) {

            var descriptors = applicationInfo.getMetaData(PersistenceUnitsDescriptor.class)
                                             .getPersistenceUnitDescriptors();

            for (PersistenceUnitDescriptor descriptor : descriptors) {

                var bean = afterBeanDiscovery.addBean().addType(EntityManager.class);

                for (String qualifierClassName : descriptor.getQualifiers()) {
                    bean.addQualifier(createAnnotationInstance(loadClass(qualifierClassName)));
                }

                if (descriptor.getScope() != null) {
                    bean.scope((Class<? extends Annotation>)loadClass(descriptor.getScope()));
                }

                bean.createWith(e -> createEntityManager(descriptor.getName()));
            }
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private EntityManager createEntityManager(String unitName) {
        return
            Globals.get(ComponentEnvManagerImpl.class)
                   .createFactoryForEntityManager(
                       new EntityManagerReferenceDescriptor(unitName))
                   .create(null);

    }

}
