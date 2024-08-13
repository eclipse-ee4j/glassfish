/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.ServiceFinder.ServiceIteratorProvider;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.server.spi.ComponentProvider;

/**
 * Goal of this finder is to be fast and efficient. It is hardcoded implementation.
 *
 * @author martinmares
 */
public class AdminJerseyServiceIteratorProvider extends ServiceIteratorProvider {

    private static final Map<String, String[]> services = new HashMap<String, String[]>();
    static {
        //        services.put(ContainerProvider.class.getName(), new String[] {
        //            "org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainerProvider"
        //        });
        services.put(AutoDiscoverable.class.getName(), new String[] {
                //            "org.glassfish.jersey.server.validation.internal.ValidationAutoDiscoverable",
                //                "org.glassfish.jersey.jsonp.internal.JsonProcessingAutoDiscoverable",
                "org.glassfish.jersey.server.filter.internal.ServerFiltersAutoDiscoverable",
                //                "org.glassfish.jersey.server.wadl.internal.WadlAutoDiscoverable"
        });
        services.put(ComponentProvider.class.getName(), new String[] {
                //            "org.glassfish.jersey.gf.cdi.CdiComponentProvider",
                //                "org.glassfish.jersey.gf.ejb.EjbComponentProvider"
        });
    }

    private static final ThreadLocal<Boolean> applyDefinedValues = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }

    };

    private static final ServiceIteratorProvider defaultProvider = new ServiceFinder.DefaultServiceIteratorProvider();

    public AdminJerseyServiceIteratorProvider() {
        applyDefinedValues.set(Boolean.TRUE);
    }

    public void disable() {
        applyDefinedValues.remove();
    }

    private String[] getServiceNames(Class service, String serviceName) {
        if (serviceName == null) {
            return services.get(service.getName());
        } else {
            return services.get(serviceName);
        }
    }

    @Override
    public <T> Iterator<Class<T>> createClassIterator(Class<T> service, String serviceName, ClassLoader loader,
            boolean ignoreOnClassNotFound) {
        final String[] values = getServiceNames(service, serviceName);
        if (!applyDefinedValues.get() || values == null) {
            return defaultProvider.createClassIterator(service, serviceName, loader, ignoreOnClassNotFound);
        } else {
            return new ClassIterator<Class<T>>(services.get(serviceName), loader, ignoreOnClassNotFound);
        }
    }

    @Override
    public <T> Iterator<T> createIterator(final Class<T> service, final String serviceName, final ClassLoader loader,
            final boolean ignoreOnClassNotFound) {
        final String[] values = getServiceNames(service, serviceName);
        if (!applyDefinedValues.get() || values == null) {
            return defaultProvider.createIterator(service, serviceName, loader, ignoreOnClassNotFound);
        } else {
            return new Iterator<T>() {

                Iterator<Class<T>> delegate = createClassIterator(service, serviceName, loader, ignoreOnClassNotFound);

                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public T next() {
                    try {
                        return delegate.next().newInstance();
                    } catch (Exception ex) {
                        throw new ServiceConfigurationError(ex.getLocalizedMessage(), ex);
                    }
                }

                @Override
                public void remove() {
                    delegate.remove();
                }
            };
        }
    }

    public static class ClassIterator<T extends Class> implements Iterator<T> {

        private final String[] names;
        private final ClassLoader classLoader;
        private final boolean ignoreOnClassNotFound;

        private int index = -1;
        private String clazzName;
        private T clazz;

        private ClassIterator(String[] names, ClassLoader classLoader, boolean ignoreOnClassNotFound) {
            this.names = names;
            this.classLoader = classLoader;
            this.ignoreOnClassNotFound = ignoreOnClassNotFound;
        }

        @Override
        public boolean hasNext() {
            if (clazzName != null) {
                return true;
            }
            if (names == null) {
                return false;
            }
            //Find next index
            while (clazzName == null) {
                if ((++index) >= names.length) {
                    return false;
                }
                clazzName = names[index];
                if (ignoreOnClassNotFound) {
                    try {
                        clazz = (T) Class.forName(clazzName, true, classLoader);
                    } catch (ClassNotFoundException ex) {
                        //Search for next one
                        clazzName = null;
                    }
                }
            }
            return true;
        }

        @Override
        public T next() {
            if (hasNext()) {
                try {
                    if (clazz != null) {
                        return clazz;
                    } else {
                        return (T) Class.forName(clazzName, true, classLoader);
                    }
                } catch (Exception ex) {
                    throw new ServiceConfigurationError(ex.getLocalizedMessage(), ex);
                } finally {
                    clazz = null;
                    clazzName = null;
                }
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
