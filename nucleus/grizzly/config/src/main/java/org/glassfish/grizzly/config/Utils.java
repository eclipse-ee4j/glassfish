/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.grizzly.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLInputFactory;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.bootstrap.HK2Populator;
import org.glassfish.hk2.bootstrap.impl.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

/**
 * Created Dec 18, 2008
 *
 * @author <a href="mailto:justin.lee@sun.com">Justin Lee</a>
 */
public class Utils {
    private static final Logger LOG = System.getLogger(Utils.class.getName());

    public static ServiceLocator getServiceLocator(final String fileURL) {
        URL url = Utils.class.getClassLoader().getResource(fileURL);
        if (url == null) {
            try {
                url = new URL(fileURL);
            } catch (MalformedURLException e) {
                throw new GrizzlyConfigException(e.getMessage());
            }
        }
        ServiceLocator locator;
        try {
            locator = getServiceLocator(url.openStream(), url.toString());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return locator;
    }

    public static ServiceLocator getServiceLocator(final InputStream inputStream, String name) {
        try {
            final ServiceLocator locator = getNewServiceLocator(name);
            final ConfigParser parser = new ConfigParser(locator);
            XMLInputFactory xif = XMLInputFactory.class.getClassLoader() == null
                    ? XMLInputFactory.newFactory()
                    : XMLInputFactory.newFactory(XMLInputFactory.class.getName(),
                    XMLInputFactory.class.getClassLoader());
            final DomDocument document = parser.parse(xif.createXMLStreamReader(inputStream));

            ServiceLocatorUtilities.addOneConstant(locator, document);

            return locator;
        } catch (Exception e) {
            throw new GrizzlyConfigException(e.getMessage(), e);
        }
    }

    public static ServiceLocator getNewServiceLocator(String name) {
        ServiceLocator locator = null;

        ServiceLocatorFactory locatorFactory = ServiceLocatorFactory.getInstance();
        if (locatorFactory.find(name) == null) {
            ServiceLocator serviceLocator = locatorFactory.create(name);

            DynamicConfigurationService dcs = serviceLocator.getService(DynamicConfigurationService.class);
            DynamicConfiguration config = dcs.createDynamicConfiguration();

            config.commit();

            locator = locatorFactory.create(name);

            try {
                HK2Populator.populate(serviceLocator,
                        new ClasspathDescriptorFileFinder(Utils.class.getClassLoader()), null);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "HK2 Populator failed.", e);
            }
        }

        return locator == null ? locatorFactory.create(name) : locator;
    }

    public static String composeThreadPoolName(final NetworkListener networkListener) {
        return networkListener.getThreadPool() + '-' + networkListener.getPort();
    }

    /**
     * Load or create an Object with the specific service name and class name.
     *
     * @param locator the HK2 {@link ServiceLocator}
     * @param clazz the class as mapped within the {@link ServiceLocator}
     * @param name the service name
     * @param realClassName the class name of the service
     * @return a service matching based on name and realClassName input
     *  arguments.
     */
    public static <E> E newInstance(ServiceLocator locator, Class<E> clazz,
            final String name, final String realClassName) {
        return newInstance(locator, clazz, name, realClassName, null, null);
    }


    /**
     * Load or create an Object with the specific service name and class name.
     *
     * @param locator the HK2 {@link ServiceLocator}
     * @param clazz the class as mapped within the {@link ServiceLocator}
     * @param name the service name
     * @param realClassName the class name of the service
     * @return a service matching based on name and realClassName input
     *         arguments.
     */
    public static <E> E newInstance(ServiceLocator locator, Class<E> clazz, final String name,
        final String realClassName, Class<?>[] argTypes, Object[] args) {
        E instance = locator == null ? null : locator.getService(clazz, name);
        if (instance != null) {
            return instance;
        }
        try {
            if (argTypes == null || argTypes.length == 0) {
                return (E) newInstance(realClassName);
            }
            return (E) newInstance(realClassName, argTypes, args);
        } catch (Exception e) {
            LOG.log(Level.WARNING, () -> "Instance could not be initialized. Class=" + clazz.getName() + ", name="
                + name + ", realClassName=" + realClassName, e);
            return null;
        }
    }

    public static Object newInstance(String classname) throws Exception {
        return loadClass(classname).getDeclaredConstructor().newInstance();
    }

    public static Object newInstance(String classname,
                                     Class<?>[] argTypes,
                                     Object[] args) throws Exception {
        final Class<?> clazz = loadClass(classname);
        final Constructor c = clazz.getConstructor(argTypes);
        assert (c != null);
        return c.newInstance(args);
    }

    public static Class loadClass(String classname) throws ClassNotFoundException {
        Class clazz = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            try {
                clazz = cl.loadClass(classname);
            } catch (Exception ignored) {
                LOG.log(Level.DEBUG, "Loading class " + classname + " failed", ignored);
            }
        }
        if (clazz == null) {
            clazz = Utils.class.getClassLoader().loadClass(classname);
        }
        return clazz;
    }

    public static boolean isDebugVM() {
        boolean debugMode = false;
        final List<String> l = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String s : l) {
            if (s.trim().startsWith("-Xrunjdwp:") || s.contains("jdwp")) {
                debugMode = true;
                break;
            }
        }
        return debugMode;
    }
}
