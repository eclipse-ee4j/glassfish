/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.web.loader;

import java.beans.Introspector;
import java.lang.System.Logger;
import java.lang.ref.Reference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.glassfish.web.util.IntrospectionUtils;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.text.MessageFormat.format;
import static org.glassfish.web.loader.LogFacade.CHECK_THREAD_LOCALS_FOR_LEAKS;
import static org.glassfish.web.loader.LogFacade.CHECK_THREAD_LOCALS_FOR_LEAKS_KEY;
import static org.glassfish.web.loader.LogFacade.getString;

import java.lang.reflect.InaccessibleObjectException;

class ReferenceCleaner {
    private static final Logger LOG = LogFacade.getSysLogger(ReferenceCleaner.class);

    private final WebappClassLoader loader;

    ReferenceCleaner(WebappClassLoader loader) {
        this.loader = loader;
    }


    /**
     * Clears all references to the classloader, including it's classes.
     * <p>
     * WARN: The implementation is bound to JVM internals.
     *
     * @param resourceEntries
     */
    void clearReferences(Collection<ResourceEntry> resourceEntries) {

        // Clearing references should be done before setting started to
        // false, due to possible side effects.
        // In addition, set this classloader as the Thread's context classloader
        final ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);

            clearReferencesJdbc();
            checkThreadLocalsForLeaks();

            // Null out any static or final fields from loaded classes,
            // as a workaround for apparent garbage collection bugs
            if (resourceEntries != null) {
                clearReferencesStaticFinal(resourceEntries);
            }

            IntrospectionUtils.clear();
            ResourceBundle.clearCache(loader);
            Introspector.flushCaches();
        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }


    /** De-register any remaining JDBC drivers */
    private void clearReferencesJdbc() {
        LOG.log(TRACE, "clearReferencesJdbc()");
        DriverManager.drivers().filter(driver -> driver.getClass().getClassLoader() == loader)
            .forEach(this::deregisterDriver);
    }


    private void deregisterDriver(Driver driver) {
        try {
            DriverManager.deregisterDriver(driver);
            LOG.log(WARNING, LogFacade.CLEAR_JDBC, loader.getName(), driver.getClass());
        } catch (Exception e) {
            LOG.log(WARNING, getString(LogFacade.JDBC_REMOVE_FAILED, loader.getName()), e);
        }
    }


    /** Check for leaks triggered by ThreadLocals loaded by this class loader */
    private void checkThreadLocalsForLeaks() {
        LOG.log(TRACE, "checkThreadLocalsForLeaks()");
        try {
            // Make the fields in the Thread class that store ThreadLocals accessible
            final Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            setAccessible(threadLocalsField);

            final Field inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
            setAccessible(inheritableThreadLocalsField);

            // Make the underlying array of ThreadLoad.ThreadLocalMap.Entry objects accessible
            final Class<?> tlmClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            final Field tableField = tlmClass.getDeclaredField("table");
            setAccessible(tableField);
            final Method expungeStaleEntriesMethod = tlmClass.getDeclaredMethod("expungeStaleEntries");
            setAccessible(expungeStaleEntriesMethod);

            // How this works - expunge and check iterate over the same map,
            // so the check verifies the result. See logs.
            Thread[] threads = getThreads();
            for (Thread thread : threads) {
                if (thread == null) {
                    continue;
                }
                final Object threadLocalMap = threadLocalsField.get(thread);
                if (threadLocalMap != null) {
                    expungeStaleEntriesMethod.invoke(threadLocalMap);
                    checkThreadLocalMapForLeaks(threadLocalMap, tableField);
                }

                final Object inheritableMap = inheritableThreadLocalsField.get(thread);
                if (inheritableMap != null) {
                    expungeStaleEntriesMethod.invoke(inheritableMap);
                    checkThreadLocalMapForLeaks(inheritableMap, tableField);
                }
            }
        } catch (InaccessibleObjectException e) {
            // module java.base does not "opens java.lang"
            LOG.log(WARNING, getString(LogFacade.CHECK_THREAD_LOCALS_FOR_LEAKS_NOT_SUPPORTED, loader.getName()));
        } catch (Exception e) {
            LOG.log(WARNING, getString(LogFacade.CHECK_THREAD_LOCALS_FOR_LEAKS_FAIL, loader.getName()), e);
        }
    }


    /**
     * Get the set of current threads as an array.
     */
    private Thread[] getThreads() {
        // Get the current thread group
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        // Find the root thread group
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }

        int threadCountGuess = tg.activeCount() + 50;
        Thread[] threads = new Thread[threadCountGuess];
        int threadCountActual = tg.enumerate(threads);
        // Make sure we don't miss any threads
        while (threadCountActual == threadCountGuess) {
            threadCountGuess *=2;
            threads = new Thread[threadCountGuess];
            // Note tg.enumerate(Thread[]) silently ignores any threads that
            // can't fit into the array
            threadCountActual = tg.enumerate(threads);
        }

        return threads;
    }


    /**
     * Analyzes the given thread local map object. Also pass in the field that
     * points to the internal table to save re-calculating it on every
     * call to this method.
     */
    private void checkThreadLocalMapForLeaks(Object threadLocalMap, Field internalTableField)
        throws IllegalAccessException, NoSuchFieldException {
        Object[] table = (Object[]) internalTableField.get(threadLocalMap);
        // See Thread's code, it can be null as of JDK17
        if (table == null) {
            return;
        }
        for (Object element : table) {
            if (element == null) {
                continue;
            }
            final Object key = ((Reference<?>) element).get();
            final boolean keyLeak = isLeaked(key);

            final Field valueField = element.getClass().getDeclaredField("value");
            setAccessible(valueField);
            final Object value = valueField.get(element);
            final boolean valueLeak = isLeaked(value);
            if (!keyLeak && !valueLeak) {
                continue;
            }
            final String keyDescription = describe(key);
            if (keyLeak) {
                LOG.log(ERROR, CHECK_THREAD_LOCALS_FOR_LEAKS_KEY, loader.getName(), keyDescription);
            }
            if (valueLeak) {
                LOG.log(ERROR, CHECK_THREAD_LOCALS_FOR_LEAKS, loader.getName(), keyDescription, describe(value));
            }
        }
    }


    private void clearReferencesStaticFinal(Collection<ResourceEntry> resourceEntries) {
        LOG.log(TRACE, "clearReferencesStaticFinal(resourceEntries={0})", resourceEntries);
        Iterator<ResourceEntry> loadedClasses = resourceEntries.iterator();
        // Step 1: Enumerate all classes loaded by this WebappClassLoader
        // and trigger the initialization of any uninitialized ones.
        // This is to prevent the scenario where the initialization of
        // one class would call a previously cleared class in Step 2 below.
        while (loadedClasses.hasNext()) {
            final Class<?> clazz = loadedClasses.next().loadedClass;
            if (clazz == null) {
                continue;
            }
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        setAccessible(field);
                        field.get(null);
                        break;
                    }
                }
            } catch (Exception t) {
                LOG.log(TRACE, () -> format("Failed to clear references for {0}.", clazz), t);
            }
        }

        // Step 2: Clear all loaded classes
        loadedClasses = resourceEntries.iterator();
        while (loadedClasses.hasNext()) {
            final Class<?> clazz = loadedClasses.next().loadedClass;
            if (clazz == null) {
                continue;
            }
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    int mods = field.getModifiers();
                    if (field.isEnumConstant() || field.getType().isPrimitive() || field.getName().indexOf('$') != -1) {
                        continue;
                    }
                    if (Modifier.isStatic(mods)) {
                        try {
                            setAccessible(field);
                            if (Modifier.isFinal(mods)) {
                                if (!field.getType().getName().startsWith("java.")
                                    && !field.getType().getName().startsWith("javax.")) {
                                    nullInstance(field.get(null));
                                }
                            } else {
                                field.set(null, null);
                                LOG.log(TRACE, "Set field {0} to null in {1}", field.getName(), clazz);
                            }
                        } catch (Exception e) {
                            LOG.log(TRACE,
                                () -> format("Could not set field {0} to null in {1}", field.getName(), clazz), e);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.log(DEBUG, () -> format("Could not clean fields for {0}", clazz), e);
            }
        }
    }


    /**
     * @param object object to test, may be null
     * @return <code>true</code> if o has been loaded by the current classloader
     * or one of its descendants.
     */
    private boolean isLeaked(Object object) {
        if (object == null) {
            return false;
        }
        if (loader.equals(object)) {
            return true;
        }

        Class<?> clazz = object instanceof Class ? (Class<?>) object : object.getClass();
        ClassLoader cl = clazz.getClassLoader();
        while (cl != null) {
            if (cl == loader) {
                return true;
            }
            cl = cl.getParent();
        }

        if (object instanceof Iterable<?>) {
            for (Object entry : ((Iterable<?>) object)) {
                if (isLeaked(entry)) {
                    return true;
                }
            }
        }
        return false;
    }


    private void nullInstance(Object instance) {
        if (instance == null) {
            return;
        }
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            int mods = field.getModifiers();
            if (field.getType().isPrimitive() || (field.getName().indexOf("$") != -1)) {
                continue;
            }
            try {
                if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
                    // Doing something recursively is too risky
                    continue;
                }
                setAccessible(field);
                Object value = field.get(instance);
                if (value != null) {
                    Class<? extends Object> valueClass = value.getClass();
                    if (isLeaked(valueClass)) {
                        field.set(instance, null);
                        LOG.log(TRACE, "Set field {0}, to null in {1}", field.getName(), instance.getClass());
                    }
                }
            } catch (Exception t) {
                LOG.log(DEBUG, () -> format("Could not set field {0} to null in object instance of {1}",
                    field.getName(), instance.getClass()), t);
            }
        }
    }


    private static void setAccessible(final AccessibleObject acessible) {
        if (System.getSecurityManager() == null) {
            acessible.setAccessible(true);
        } else {
            PrivilegedAction<Void> action = () -> {
                acessible.setAccessible(true);
                return null;
            };
            AccessController.doPrivileged(action);
        }
    }


    private static String describe(Object object) {
        if (object == null) {
            return null;
        }
        StringBuilder b = new StringBuilder(128);
        b.append(getClass(object)).append(" (toString: ").append(toString(object)).append(')');
        return b.toString();
    }


    private static Class<?> getClass(Object object) {
        try {
            return object.getClass();
        } catch (Exception e) {
            LOG.log(DEBUG, "Getting class failed, using null.", e);
            return null;
        }
    }


    private static String toString(Object object) {
        try {
            return object.toString();
        } catch (NullPointerException e) {
            // toString should never throw NPE, but incorrect implementations do that.
            LOG.log(WARNING, "The object's toString failed, using 'unknown'.", e);
            return "unknown";
        } catch (Exception e) {
            LOG.log(DEBUG, "The object's toString failed, using 'unknown'.", e);
            return "unknown";
        }
    }
}
