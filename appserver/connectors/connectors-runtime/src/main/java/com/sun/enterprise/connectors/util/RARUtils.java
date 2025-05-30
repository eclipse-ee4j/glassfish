/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.util;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.common.util.GlassfishUrlClassLoader;

/**
 * This is a utility class to obtain the properties of a
 * RA JavaBean housed in a RAR module/deployment dir, without exploding the RAR
 * contents. This method would be used by the admin-gui to configure
 * RA properties during RA deployment to a cluster.
 *
 * @author Sivakumar Thyagarajan
 */
public class RARUtils {

    private final static Logger _logger = LogDomains.getLogger(RARUtils.class, LogDomains.RSR_LOGGER);
    private static StringManager localStrings = StringManager.getManager(RARUtils.class);

    /**
     * A valid resource adapter java bean property should either be one of the
     * following
     * 1. A Java primitive or a primitve wrapper
     * 2. A String
     */
    public static boolean isValidRABeanConfigProperty(Class clz) {
        return (clz.isPrimitive() || clz.equals(String.class)
                        || isPrimitiveWrapper(clz));
    }

    /**
     * Determines if a class is one of the eight java primitive wrapper classes
     */
    private static boolean isPrimitiveWrapper(Class clz) {
        return (clz.equals(Boolean.class) || clz.equals(Character.class)
                 || clz.equals(Byte.class) || clz.equals(Short.class)
                 || clz.equals(Integer.class) || clz.equals(Long.class)
                 || clz.equals(Float.class) || clz.equals(Double.class));
    }

   /**
     * Prepares the name/value pairs for ActivationSpec. <p>
     * Rule: <p>
     * 1. The name/value pairs are the union of activation-config on
     *    standard DD (message-driven) and runtime DD (mdb-resource-adapter)
     * 2. If there are duplicate property settings, the value in runtime
     *    activation-config will overwrite the one in the standard
     *    activation-config.
     */
    public static Set<EnvironmentProperty> getMergedActivationConfigProperties(EjbMessageBeanDescriptor msgDesc) {

        Set<EnvironmentProperty> mergedProps = new HashSet<>();
        Set<String> runtimePropNames = new HashSet<>();

        Set<EnvironmentProperty> runtimeProps = msgDesc.getRuntimeActivationConfigProperties();
        if(runtimeProps != null){
            for (EnvironmentProperty entry : runtimeProps) {
                mergedProps.add(entry);
                String propName = entry.getName();
                runtimePropNames.add(propName);
            }
        }

        Set<EnvironmentProperty> standardProps = msgDesc.getActivationConfigProperties();
        if(standardProps != null){
            for (EnvironmentProperty entry : standardProps) {
                String propName = entry.getName();
                if (runtimePropNames.contains(propName)) {
                    continue;
                }
                mergedProps.add(entry);
            }
        }

        return mergedProps;

    }

    public static Class<?> loadClassFromRar(String rarName, String beanClassName) throws ConnectorRuntimeException{
        String rarLocation = getRarLocation(rarName);
        return loadClass(rarLocation, beanClassName);
    }

    /**
     * given the rar name, location of rar will be returned
     * @param rarName resource-adapter name
     * @return location of resource-adapter
     */
    private static String getRarLocation(String rarName) throws ConnectorRuntimeException{
        return ConnectorsUtil.getLocation(rarName);
    }


    /**
     * given the location of .rar (archive / exploded dir), the specified class will be loaded
     * @param pathToDeployableUnit location of .rar (archive / exploded dir)
     * @param beanClassName class that has to be loaded
     * @return loaded class
     * @throws ConnectorRuntimeException when unable to load the class
     */
    private static Class<?> loadClass(String pathToDeployableUnit, String beanClassName) throws ConnectorRuntimeException {
        Class<?> cls = null;

        ClassLoader cl = getClassLoader(pathToDeployableUnit);

        try {
            //Only if RA is a 1.5 RAR, we need to get RA JavaBean properties, else
            //return an empty map.

            if (beanClassName != null && !beanClassName.isBlank()) {
                cls = cl.loadClass(beanClassName);
            }
            return cls;
        } catch (ClassNotFoundException e) {
            _logger.info(e.getMessage());
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Unable to find class while trying to read connector " +
                    "descriptor to get resource-adapter properties", e);
            }
            ConnectorRuntimeException cre = new ConnectorRuntimeException("unable to find class : " + beanClassName);
            cre.setStackTrace(e.getStackTrace());
            throw cre;
        }
    }

    /**
     * based on the provided file type (dir or archive) appropriate class-loader will be selected
     * @param file file (dir/ archive)
     * @return classloader that is capable of loading the .rar
     * @throws ConnectorRuntimeException when unable to load the .rar
     */
    private static ClassLoader getClassLoader(String file) throws ConnectorRuntimeException {
        File f = new File(file);
        validateRARLocation(f);
        try {
            ClassLoader commonCL = ConnectorRuntime.getRuntime().getClassLoaderHierarchy().getCommonClassLoader();
            if (f.isDirectory()) {
                List<URL> urls = new ArrayList<>();
                urls.add(f.toURI().toURL());
                appendURLs(urls, f);
                return new GlassfishUrlClassLoader("ResourceAdapterDir(" + f.getName() + ")", urls.toArray(URL[]::new), commonCL);
            }
            return new ConnectorRARClassLoader(file, commonCL);
        } catch (IOException ioe) {
            throw new ConnectorRuntimeException("unable to read connector descriptor from " + file, ioe);
        }
    }

    private static void appendURLs(List<URL> urls, File f) throws MalformedURLException {
        File[] files = f.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toUpperCase(Locale.getDefault()).endsWith(".JAR")) {
                    urls.add(file.toURI().toURL());
                } else if (file.isDirectory()) {
                    appendURLs(urls, file);
                }
            }
        }
    }

    /**
     * check whether the provided location is valid
     * @param f location where the .rar is present
     * @throws ConnectorRuntimeException
     */
    private static void validateRARLocation(File f) throws ConnectorRuntimeException {
        if (!f.exists()) {
            String i18nMsg = localStrings.getString(
                    "rar_archive_not_found", f);
            throw new ConnectorRuntimeException(i18nMsg);
        }
    }


}
