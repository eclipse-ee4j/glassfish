/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import static java.lang.System.getSecurityManager;
import static java.security.AccessController.doPrivileged;

import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jboss.weld.serialization.spi.ProxyServices;

/**
 * An implementation of the <code>ProxyServices</code> Service.
 *
 * This implementation uses the thread context classloader (the application classloader) as the classloader for loading
 * the bean proxies. The classloader that loaded the Bean must be used to load and define the bean proxy to handle Beans
 * with package-private constructor as discussed in WELD-737.
 *
 * Weld proxies today have references to some internal weld implementation classes such as javassist and
 * org.jboss.weld.proxy.* packages. These classes are temporarily re-exported through the weld-integration-fragment
 * bundle so that when the bean proxies when loaded using the application classloader will have visibility to these
 * internal implementation classes.
 *
 * As a fix for WELD-737, Weld may use the Bean's classloader rather than asking the ProxyServices service
 * implementation. Weld also plans to remove the dependencies of the bean proxy on internal implementation classes. When
 * that happens we can remove the weld-integration-fragment workaround and the ProxyServices implementation
 *
 * @author Sivakumar Thyagarajan
 */
public class ProxyServicesImpl implements ProxyServices {

    ClassLoaderHierarchy classLoaderHierarchy;

    public ProxyServicesImpl(ServiceLocator services) {
        classLoaderHierarchy = services.getService(ClassLoaderHierarchy.class);
    }

    @Override
    public ClassLoader getClassLoader(final Class<?> proxiedBeanType) {
        if (getSecurityManager() == null) {
            return getClassLoaderforBean(proxiedBeanType);
        }
        return doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return getClassLoaderforBean(proxiedBeanType);
            }
        });
    }

    /**
     * Gets the ClassLoader associated with the Bean. Weld generates Proxies for Beans from an application/BDA and for
     * certain API artifacts such as <code>UserTransaction</code>.
     *
     * @param proxiedBeanType
     * @return
     */
    private ClassLoader getClassLoaderforBean(Class<?> proxiedBeanType) {
        // Get the ClassLoader that loaded the Bean. For Beans in an application,
        // this would be the application/module classloader. For other API
        // Bean classes, such as UserTransaction, this would be a non-application
        // classloader
        ClassLoader proxyClassLoader = proxiedBeanType.getClassLoader();

        //Check if this is an application classloader
        boolean isAppCL = isApplicationClassLoader(proxyClassLoader);
        if (!isAppCL) {
            proxyClassLoader = _getClassLoader();
            //fall back to the old behaviour of using TCL to get the application
            //or module classloader. We return this classloader for non-application
            //Beans, as Weld Proxies requires other Weld support classes (such as
            //JBoss Reflection API) that is exported through the weld-osgi-bundle.
        }

        return proxyClassLoader;
    }

    /**
     * Check if the ClassLoader of the Bean type being proxied is a GlassFish application ClassLoader. The current logic
     * checks if the common classloader appears as a parent in the classloader hierarchy of the Bean's classloader.
     */
    private boolean isApplicationClassLoader(ClassLoader prxCL) {
        boolean isAppCL = false;
        while (prxCL != null) {
            if (prxCL.equals(classLoaderHierarchy.getCommonClassLoader())) {
                isAppCL = true;
                break;
            }
            prxCL = prxCL.getParent();
        }
        return isAppCL;
    }

    private ClassLoader _getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public Class<?> loadBeanClass(final String className) {
        try {
            if (getSecurityManager() == null) {
                return Class.forName(className, true, _getClassLoader());
            }

            return (Class<?>) doPrivileged(new PrivilegedExceptionAction<>() {
                @Override
                public Object run() throws Exception {
                    return Class.forName(className, true, _getClassLoader());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void cleanup() {
        // nothing to cleanup in this implementation.
    }

}
