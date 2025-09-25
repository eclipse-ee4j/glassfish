/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018-2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.ejb.codegen.ClassGenerator;

import java.io.ByteArrayInputStream;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jboss.weld.serialization.spi.ProxyServices;

import javassist.ClassPool;
import javassist.CtClass;


/**
 * An implementation of the {@link ProxyServices}.
 * <p>
 * This implementation respects the classloader hierarchy used to load original beans.
 * If it is not an application classloader, uses the current thread classloader.
 * If it wasn't possible to detect any usable classloader, throws a {@link WeldProxyException}
 * <p>
 * Context: Weld generates proxies for beans from an application and for certain API artifacts
 * such as <code>UserTransaction</code>.
 *
 * @author Sivakumar Thyagarajan
 * @author David Matějček
 */
public class ProxyServicesImpl implements ProxyServices {

    private final ClassLoaderHierarchy classLoaderHierarchy;

    // probably shouldn't be static but moved to a singleton service
    private static Map<ClassLoader, ClassPool> classPoolMap = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * @param services immediately used to find a {@link ClassLoaderHierarchy} service
     */
    public ProxyServicesImpl(final ServiceLocator services) {
        classLoaderHierarchy = services.getService(ClassLoaderHierarchy.class);
    }

    /* This solution is not ideal - it creates the new class in the CL of the originalClass
       (most often server CL or bootstrap CL), while the previous solution created it in the app classloader.
    */
    @Override
    public Class<?> defineClass(final Class<?> originalClass, final String className, final byte[] classBytes,
        final int off, final int len) throws ClassFormatError {
        try {
            final String originalPackageName = originalClass.getPackageName();
            String modifiedClassName = originalClass.getName() + "_GlassFishWeldProxy";
            final ClassLoader originalClassCL = originalClass.getClassLoader();
            final ClassPool classPool = classPoolMap.computeIfAbsent(originalClassCL, cl -> new ClassPool());
            while (classPool.getOrNull(modifiedClassName) != null) {
                modifiedClassName += "_";
            }
            final CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classBytes, off, len));
            ctClass.setName(modifiedClassName);
            return ClassGenerator.defineClass(originalClass.getClassLoader(), originalClass, originalPackageName,
                    modifiedClassName, ctClass.toBytecode());
        } catch (final Exception e) {
            throw new WeldProxyException("Could not define class " + className, e);
        }
    }


    @Override
    public Class<?> defineClass(final Class<?> originalClass, final String className, final byte[] classBytes,
        final int off, final int len, final ProtectionDomain protectionDomain) throws ClassFormatError {
        return defineClass(originalClass, className, classBytes, off, len);
    }


    @Override
    public Class<?> loadClass(final Class<?> originalClass, final String classBinaryName)
        throws ClassNotFoundException {
        return getClassLoaderforBean(originalClass).loadClass(classBinaryName);
    }


    @Override
    public void cleanup() {
        // nothing to cleanup in this implementation.
    }


    /**
     * @param originalClass
     * @return ClassLoader probably usable with the bean.
     */
    private ClassLoader getClassLoaderforBean(final Class<?> originalClass) {
        // Get the ClassLoader that loaded the Bean. For Beans in an application,
        // this would be the application/module classloader. For other API
        // Bean classes, such as UserTransaction, this would be a non-application
        // classloader
        final ClassLoader originalClassLoader = originalClass.getClassLoader();
        if (isApplicationClassLoader(originalClassLoader)) {
            return originalClassLoader;
        }
        // fall back to the old behaviour of using thread class loader to get the application
        // or module classloader. We return this classloader for non-application
        // Beans, as Weld Proxies requires other Weld support classes (such as
        // JBoss Reflection API) that is exported through the weld-osgi-bundle.
        final ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
        if (threadCL != null) {
            return threadCL;
        }
        throw new WeldProxyException("Could not determine classloader for " + originalClass);
    }


    /**
     * Check if the ClassLoader of the Bean type being proxied is a GlassFish application
     * ClassLoader. The current logic checks if the common classloader appears as a parent in
     * the classloader hierarchy of the Bean's classloader.
     */
    private boolean isApplicationClassLoader(ClassLoader classLoader) {
        while (classLoader != null) {
            if (classLoader.equals(classLoaderHierarchy.getCommonClassLoader())) {
                return true;
            }
            classLoader = classLoader.getParent();
        }
        return false;
    }


}
