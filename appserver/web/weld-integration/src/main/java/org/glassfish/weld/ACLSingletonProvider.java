/*
 * Copyright (c) 2021, 2024 Contributors to Eclipse Foundation.
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

import java.lang.System.Logger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.Globals;
import org.glassfish.javaee.full.deployment.EarLibClassLoader;
import org.glassfish.web.loader.WebappClassLoader;
import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider;

import static java.lang.System.getSecurityManager;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;

/**
 * Singleton provider that uses Application ClassLoader to differentiate between applications.
 * <p>
 * It is different from {@link org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider}.
 * <p>
 * We can't use TCCLSingletonProvider because thread's context class loader can be different for
 * different modules of a single application (ear).
 * To support Application Scoped beans, Weld needs to be bootstrapped per application as
 * opposed to per module. We rely on the fact that all these module class loaders have a common
 * parent which is per application. We use that parent ApplicationClassLoader to identify the
 * singleton scope.
 * <p>
 * This class assumes a certain delegation hierarchy of application class loaders. So, deployment
 * team should be aware of this class and change it if application class loader hierarchy changes.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ACLSingletonProvider extends SingletonProvider {

    private static final Logger LOG = System.getLogger(ACLSingletonProvider.class.getName());

    /**
     * Calls {@link SingletonProvider#initialize(SingletonProvider)} with
     * {@link ACLSingletonProvider} if there is an EAR support or with {@link TCCLSingletonProvider}
     * if EARs are not supported.
     */
    public static void initializeSingletonProvider() {
        boolean earSupport;
        try {
            Class.forName("org.glassfish.javaee.full.deployment.EarClassLoader");
            earSupport = true;
        } catch (ClassNotFoundException ignore) {
            earSupport = false;
        }
        SingletonProvider provider = earSupport ? new ACLSingletonProvider() : new TCCLSingletonProvider();
        SingletonProvider.initialize(provider);
        LOG.log(DEBUG, () -> "SingletonProvider initialized: " + provider);
    }

    @Override
    public <T> ACLSingleton<T> create(Class<? extends T> expectedType) {
        return new ACLSingleton<>();
    }

    private static class ACLSingleton<T> implements Singleton<T> {
        private static final Logger LOG = System.getLogger(ACLSingleton.class.getName());

        private final Map<ClassLoader, T> store = new ConcurrentHashMap<>();
        private final ClassLoader commonClassLoader = Globals.get(ClassLoaderHierarchy.class).getCommonClassLoader();

        // Can't assume bootstrap loader as null. That's more of a convention, but some JVMs do not
        // use null for bootstap loader
        private static ClassLoader bootstrapCL;

        static {
            PrivilegedAction<ClassLoader> action = () -> Object.class.getClassLoader();
            bootstrapCL = getSecurityManager() == null ? action.run() : AccessController.doPrivileged(action);
        }

        @Override
        public T get(String id) {
            ClassLoader appClassLoader = getClassLoader();
            T instance = store.get(appClassLoader);
            if (instance == null) {
                throw new IllegalStateException("Singleton not set for " + appClassLoader);
            }
            LOG.log(DEBUG, () -> "get(" + id + ") - found " + instance + ";  we use ear/war classloader instead of id.");
            return instance;
        }

        @Override
        public boolean isSet(String id) {
            return store.containsKey(getClassLoader());
        }

        @Override
        public void set(String id, T object) {
            LOG.log(DEBUG, () -> "set(id=" + id + ", object=" + object + "); we use ear/war classloader instead of id.");
            store.put(getClassLoader(), object);
        }

        @Override
        public void clear(String id) {
            LOG.log(DEBUG, () -> "clear(id=" + id + "); we use ear/war classloader instead of id.");
            store.remove(getClassLoader());
        }

        /**
         * This is the most significant method of this class. This is what distingushes it from TCCLSIngleton.
         *
         * <p>
         * It tries to obtain a class loader that's common to all modules of an application (ear). Since it runs in the context
         * of Java EE, it can assume that Thread's context class loader is always set as application class loader. In GlassFish,
         * the class loader can vary for each module of an Ear. Thread's context class loader is set depending on which module
         * is handling the request.
         *
         * <p>
         * But, fortunately all those embedded module class loaders have a common parent in their
         * delegation chain. That parent is of type EarLibClassLoader. So, this code walks up the delegation chain until it hits
         * either a EarLibClassLoader type of parent or bootstrapClassLoader. If former is the case, it returns that instance of
         * EarLibClassLoader. If latter is the case, it assumes that this is a standalone module and hence it returns the
         * thread's context class loader.
         *
         * @return a class loader that's common to all modules of a Jakarta EE application
         */
        private ClassLoader getClassLoader() {
            PrivilegedAction<ClassLoader> action = () -> currentThread().getContextClassLoader();
            ClassLoader contextClassLoader = getSecurityManager() == null ? action.run() : doPrivileged(action);

            if (contextClassLoader == null) {
                throw new RuntimeException("Thread's context class loader is null");
            }

            ClassLoader classLoader = contextClassLoader;
            ClassLoader appClassLoader = contextClassLoader;

            // Most of the time, the class loader of an application (whether it is a
            // standalone module or an ear) has a common class loader in their delegation chain.
            //
            // So, we can break the loop early for them.
            //
            // There are exceptions like hybrid application to this rule.
            // So, we have to walk up to bootstrapCL in worst case.
            while (classLoader != commonClassLoader && classLoader != bootstrapCL) {
                if (classLoader instanceof EarLibClassLoader) {
                    return classLoader;
                }

                if (classLoader instanceof WebappClassLoader) {
                    // We do this because it's possible for an app to change the thread's context class loader
                    appClassLoader = classLoader;
                }
                classLoader = getParent(classLoader);
            }

            return appClassLoader;
        }

        private ClassLoader getParent(ClassLoader classLoader) {
            LOG.log(TRACE, () -> "getParent(classLoader=" + classLoader + ")");
            PrivilegedAction<ClassLoader> action = classLoader::getParent;
            return getSecurityManager() == null ? action.run() : doPrivileged(action);
        }
    }
}
