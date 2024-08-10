/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.weld.Container;
import org.jboss.weld.SimpleCDI;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class GlassFishWeldProvider implements CDIProvider {
    private static class GlassFishEnhancedWeld extends SimpleCDI {

        @Override
        protected BeanManagerImpl unsatisfiedBeanManager(String callerClassName) {

            /*
             * In certain scenarios we use flat deployment model (weld-se, weld-servlet). In that case
             * we return the only BeanManager we have.
             */
            if (Container.instance().beanDeploymentArchives().values().size() == 1) {
                return Container.instance().beanDeploymentArchives().values().iterator().next();
            }

            // To get the correct bean manager we need to determine the class loader of the calling class.
            // unfortunately we only have the class name so we need to find the root bda that has a class loader
            // that can successfully load the class.  This should give us the correct BDA which then can be used
            // to get the correct bean manager
            Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchives = Container.instance().beanDeploymentArchives();
            Set<Entry<BeanDeploymentArchive, BeanManagerImpl>> entries = beanDeploymentArchives.entrySet();
            for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : entries) {
                BeanDeploymentArchive beanDeploymentArchive = entry.getKey();
                if (beanDeploymentArchive instanceof RootBeanDeploymentArchive) {
                    RootBeanDeploymentArchive rootBeanDeploymentArchive = (RootBeanDeploymentArchive) beanDeploymentArchive;
                    ClassLoader moduleClassLoaderForBDA = rootBeanDeploymentArchive.getModuleClassLoaderForBDA();
                    try {
                        Class.forName(callerClassName, false, moduleClassLoaderForBDA);
                        // Successful so this is the BeanDeploymentArchive we want.
                        return entry.getValue();
                    } catch (Exception ignore) {
                    }
                }
            }

            return super.unsatisfiedBeanManager(callerClassName);
        }
    }

    @Override
    public CDI<Object> getCDI() {
        try {
            return new GlassFishEnhancedWeld();
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause();
            if (cause instanceof IllegalStateException) {
                return null;
            }
            throw throwable;
        }
    }

}
