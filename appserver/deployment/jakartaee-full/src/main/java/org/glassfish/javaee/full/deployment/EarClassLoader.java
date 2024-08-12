/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.full.deployment;

import com.sun.enterprise.loader.ASURLClassLoader;

import java.util.LinkedList;
import java.util.List;

import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.internal.api.DelegatingClassLoader;

/**
 * Simplistic class loader which will delegate to each module class loader in the order they were added to the instance
 *
 * @author Jerome Dochez
 */
public class EarClassLoader extends ASURLClassLoader {

    private List<ClassLoaderHolder> moduleClassLoaders = new LinkedList<>();
    boolean isPreDestroyCalled = false;

    public EarClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    public void addModuleClassLoader(String moduleName, ClassLoader cl) {
        moduleClassLoaders.add(new ClassLoaderHolder(moduleName, cl));
    }

    public ClassLoader getModuleClassLoader(String moduleName) {
        for (ClassLoaderHolder clh : moduleClassLoaders) {
            if (moduleName.equals(clh.moduleName)) {
                return clh.loader;
            }
        }

        return null;
    }

    @Override
    public void preDestroy() {
        if (isPreDestroyCalled) {
            return;
        }

        try {
            for (ClassLoaderHolder clh : moduleClassLoaders) {
                // destroy all the module classloaders
                if (!(clh.loader instanceof EarLibClassLoader) && !(clh.loader instanceof EarClassLoader) && !isRARCL(clh.loader)) {
                    try {
                        PreDestroy.class.cast(clh.loader).preDestroy();
                    } catch (Exception e) {
                        // ignore, the class loader does not need to be
                        // explicitly stopped.
                    }
                }
            }

            // destroy itself
            super.preDestroy();

            // now destroy embedded Connector CLs
            DelegatingClassLoader dcl = (DelegatingClassLoader) this.getParent();
            for (DelegatingClassLoader.ClassFinder cf : dcl.getDelegates()) {
                try {
                    PreDestroy.class.cast(cf).preDestroy();
                } catch (Exception e) {
                    // ignore, the class loader does not need to be
                    // explicitly stopped.
                }
            }

            // now destroy the EarLibClassLoader
            PreDestroy.class.cast(this.getParent().getParent()).preDestroy();

            moduleClassLoaders = null;
        } catch (Exception e) {
            // ignore, the class loader does not need to be explicitely stopped.
        }

        isPreDestroyCalled = true;
    }

    private boolean isRARCL(ClassLoader loader) {
        DelegatingClassLoader connectorCL = (DelegatingClassLoader) this.getParent();
        if (!(loader instanceof DelegatingClassLoader.ClassFinder)) {
            return false;
        }
        return connectorCL.getDelegates().contains(loader);
    }

    private static class ClassLoaderHolder {
        final ClassLoader loader;
        final String moduleName;

        private ClassLoaderHolder(String moduleName, ClassLoader loader) {
            this.loader = loader;
            this.moduleName = moduleName;
        }
    }

    @Override
    protected String getClassLoaderName() {
        return "EarClassLoader";
    }
}
