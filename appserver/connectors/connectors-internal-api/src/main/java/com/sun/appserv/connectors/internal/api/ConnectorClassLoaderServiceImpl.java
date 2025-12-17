/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.connectors.internal.api;

import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.List;
import java.util.logging.Logger;

import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.ConnectorClassLoaderService;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINEST;

/**
 * We support two policies: 1. All standalone RARs are available to all other applications. This is the Java EE 5
 * specific behavior. 2. An application has visbility to only those standalone RARs that it depends on. This is the new
 * behavior defined in Java EE 6 as well as JCA 1.6 spec.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class ConnectorClassLoaderServiceImpl implements ConnectorClassLoaderService {

    /**
     * This class loader is used when we have just a single connector class loader for all applications. In other words, we
     * make every standalone RARs available to all applications.
     */
    private volatile DelegatingClassLoader globalConnectorCL;

    @Inject
    private AppSpecificConnectorClassLoaderUtil appsSpecificCCLUtil;

    @Inject
    private Provider<ClassLoaderHierarchy> classLoaderHierarchyProvider;

    private Logger logger = LogDomains.getLogger(ConnectorClassLoaderServiceImpl.class, LogDomains.RSR_LOGGER);

    /**
     * provides connector-class-loader for the specified application If application is null, global connector class loader
     * will be provided
     *
     * @param appName application-name
     * @return class-loader
     */
    @Override
    public DelegatingClassLoader getConnectorClassLoader(String appName) {
        DelegatingClassLoader loader = null;

        // We do not have dependency on common-class-loader explicitly
        // and also cannot initialize globalConnectorCL during postConstruct via ClassLoaderHierarchy
        // which will result in circular dependency injection between kernel and connector module
        // Hence initializing globalConnectorCL lazily
        if (globalConnectorCL == null) {
            synchronized (ConnectorClassLoaderServiceImpl.class) {
                if (globalConnectorCL == null) {
                    // [parent is assumed to be common-class-loader in ConnectorClassLoaderUtil.createRARClassLoader() also]
                    final ClassLoader parent = getCommonClassLoader();

                    DelegatingClassLoader newGlobalConnectorCL = new DelegatingClassLoader(parent);

                    for (DelegatingClassLoader.ClassFinder classFinder : appsSpecificCCLUtil.getSystemRARClassLoaders()) {
                        newGlobalConnectorCL.addDelegate(classFinder);
                    }

                    for (DelegatingClassLoader.ClassFinder classFinder : appsSpecificCCLUtil.getSystemRARClassLoaders()) {
                        newGlobalConnectorCL.addDelegate(classFinder);
                    }

                    globalConnectorCL = newGlobalConnectorCL;
                }
            }
        }
        if (hasGlobalAccessForRARs(appName)) {
            loader = globalConnectorCL;
        } else {
            appsSpecificCCLUtil.detectReferredRARs(appName);
            loader = createConnectorClassLoaderForApplication(appName);
        }

        return loader;
    }

    private boolean hasGlobalAccessForRARs(String appName) {
        return
            appName == null ||
            appsSpecificCCLUtil.useGlobalConnectorClassLoader() ||
            appsSpecificCCLUtil.getRequiredResourceAdapters(appName).contains(ConnectorConstants.RAR_VISIBILITY_GLOBAL_ACCESS);
    }

    private ClassLoader getCommonClassLoader() {
        return classLoaderHierarchyProvider.get().getCommonClassLoader();
    }

    private DelegatingClassLoader createConnectorClassLoaderForApplication(String appName) {

        DelegatingClassLoader appSpecificConnectorClassLoader = new DelegatingClassLoader(getCommonClassLoader());

        // add system ra classloaders
        for (DelegatingClassLoader.ClassFinder classFinder : appsSpecificCCLUtil.getSystemRARClassLoaders()) {
            appSpecificConnectorClassLoader.addDelegate(classFinder);
        }

        for (String raName : appsSpecificCCLUtil.getRARsReferredByApplication(appName)) {
            addRarClassLoader(appName, appSpecificConnectorClassLoader, raName);
        }

        for (String raName : appsSpecificCCLUtil.getRequiredResourceAdapters(appName)) {
            addRarClassLoader(appName, appSpecificConnectorClassLoader, raName);
        }

        return appSpecificConnectorClassLoader;
    }

    private void addRarClassLoader(String appName, DelegatingClassLoader appSpecificConnectorClassLoader, String raName) {
        if (logger.isLoggable(FINEST)) {
            logger.finest("raName for app [ " + appName + " ] : " + raName);
        }

        DelegatingClassLoader.ClassFinder classFinder = getClassFinder(raName);

        if (classFinder != null) {
            appSpecificConnectorClassLoader.addDelegate(classFinder);
        }
    }

    private DelegatingClassLoader.ClassFinder getClassFinder(String raName) {
        List<DelegatingClassLoader.ClassFinder> delegates = globalConnectorCL.getDelegates();
        DelegatingClassLoader.ClassFinder classFinder = null;
        for (DelegatingClassLoader.ClassFinder cf : delegates) {
            if (raName.equals(((ConnectorClassFinder) cf).getResourceAdapterName())) {
                classFinder = cf;
                break;
            }
        }

        return classFinder;
    }

}
