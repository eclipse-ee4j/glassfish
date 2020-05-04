/*
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

package com.sun.appserv.connectors.internal.api;

import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.glassfish.api.admin.*;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Singleton;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.loader.ASURLClassLoader;
import com.sun.logging.LogDomains;
import java.util.Locale;

import jakarta.inject.Inject;


/**
 * Classloader util to create a new classloader for the provided .rar deploy directory.
 *
 * @author Jagadish Ramu
 */
@Service
@Singleton
public class ConnectorsClassLoaderUtil {

    @Inject
    private ClassLoaderHierarchy clh;

    //private static List<ConnectorClassFinder> systemRARClassLoaders;

    private Logger _logger = LogDomains.getLogger(ConnectorRuntime.class, LogDomains.RSR_LOGGER);

    @Inject
    private ServerEnvironment env;

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    Events events;


    private volatile boolean rarsInitializedInEmbeddedServerMode;

    public ConnectorClassFinder createRARClassLoader(String moduleDir, ClassLoader deploymentParent,
                                                     String moduleName, List<URI> appLibs)
            throws ConnectorRuntimeException {

        ClassLoader parent = null;

        //For standalone rar :
        //this is not a normal application and hence cannot use the provided parent during deployment.
        //setting the parent to connector-class-loader's parent (common class-loader) as this is a .rar
        //For embedded rar :
        //use the deploymentParent as the class-finder created won't be part of connector class loader
        //service hierarchy
        if(deploymentParent == null){
            parent = clh.getCommonClassLoader();
        }else{
            parent = deploymentParent;
        }
        return createRARClassLoader(parent, moduleDir, moduleName, appLibs);
    }

    private DelegatingClassLoader.ClassFinder getLibrariesClassLoader(final List<URI> appLibs)
            throws MalformedURLException, ConnectorRuntimeException {
        try {
            return (DelegatingClassLoader.ClassFinder) AccessController.doPrivileged(new PrivilegedExceptionAction(){
                public Object run() throws Exception {
                    return clh.getAppLibClassFinder(appLibs);
                }
            });
        } catch (PrivilegedActionException e) {
            _logger.log(Level.SEVERE, "error.creating.libraries.classloader", e);
            ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
            cre.initCause(e);
            throw cre;
        }
    }

    private ConnectorClassFinder createRARClassLoader(final ClassLoader parent, String moduleDir,
                                                      final String moduleName, List<URI> appLibs)
            throws ConnectorRuntimeException{
        ConnectorClassFinder cl = null;

        try{
            final DelegatingClassLoader.ClassFinder librariesCL = getLibrariesClassLoader(appLibs);
            cl = (ConnectorClassFinder)AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                        final ConnectorClassFinder ccf = new ConnectorClassFinder(parent, moduleName, librariesCL);
                        if (processEnv.getProcessType().isEmbedded()) {
                            events.register(new EventListener() {
                                public void event(Event event) {
                                    if (event.is(EventTypes.PREPARE_SHUTDOWN)) {
                                        ccf.done();
                                    }
                                }
                            });
                        }
                        return ccf;
                }
            });
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "error.creating.connector.classloader", ex);
            ConnectorRuntimeException cre = new ConnectorRuntimeException(ex.getMessage());
            cre.initCause(ex);
            throw cre;
        }

        File file = new File(moduleDir);
        try {
            cl.appendURL(file.toURI().toURL());
            appendJars(file, cl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return cl;
    }

    private boolean extractRar(String rarName, String destination){
        String rarFileName = rarName + ConnectorConstants.RAR_EXTENSION;
        return ConnectorsUtil.extractRar(destination+rarFileName, rarFileName, destination);
    }

    public Collection<ConnectorClassFinder> getSystemRARClassLoaders() throws ConnectorRuntimeException {
            //if (systemRARClassLoaders == null) {

            if (processEnv.getProcessType().isEmbedded() && !rarsInitializedInEmbeddedServerMode) {
                synchronized (ConnectorsClassLoaderUtil.class){
                    if(!rarsInitializedInEmbeddedServerMode){
                        String installDir = System.getProperty(ConnectorConstants.INSTALL_ROOT) + File.separator;
                        for (String jdbcRarName : ConnectorConstants.jdbcSystemRarNames) {
                            String rarPath = ConnectorsUtil.getSystemModuleLocation(jdbcRarName);
                            File rarDir = new File(rarPath);
                            if(!rarDir.exists()){
                                extractRar(jdbcRarName, installDir);
                            }
                        }
                        rarsInitializedInEmbeddedServerMode = true;
                    }
                }
            }

            List<ConnectorClassFinder> classLoaders = new ArrayList<ConnectorClassFinder>();
            for (String rarName : ConnectorsUtil.getSystemRARs()) {

                String location = ConnectorsUtil.getSystemModuleLocation(rarName);

                List<URI> libraries ;

                if (processEnv.getProcessType().isEmbedded()) {
                    libraries = new ArrayList<URI>();
                } else {
                    libraries = ConnectorsUtil.getInstalledLibrariesFromManifest(location, env);
                }

                ConnectorClassFinder ccf = createRARClassLoader(location, null, rarName, libraries);
                classLoaders.add(ccf);
            }
        //    systemRARClassLoaders = classLoaders;
        //}
        //return systemRARClassLoaders;
        return classLoaders;
    }


    public ConnectorClassFinder getSystemRARClassLoader(String rarName) throws ConnectorRuntimeException {
        if (ConnectorsUtil.belongsToSystemRA(rarName)) {
            DelegatingClassLoader dch = clh.getConnectorClassLoader(null);
            for (DelegatingClassLoader.ClassFinder cf : dch.getDelegates()) {
                if (cf instanceof ConnectorClassFinder) {
                    if (rarName.equals(((ConnectorClassFinder) cf).getResourceAdapterName())) {
                        return (ConnectorClassFinder) cf;
                    }
                }
            }
        }
        throw new ConnectorRuntimeException("No Classloader found for RA [ " + rarName + " ]");
    }

    private void appendJars(File moduleDir, ASURLClassLoader cl) throws MalformedURLException {
        //TODO for embedded rars -consider MANIFEST.MF's classpath attribute
        if (moduleDir.isDirectory()) {
            File[] list = moduleDir.listFiles();
            if(list != null) {
                for (File file : list) {
                    if (file.getName().toUpperCase(Locale.getDefault()).endsWith(".JAR")) {
                        cl.appendURL(file.toURI().toURL());
                    } else if (file.isDirectory()) {
                        appendJars(file, cl); //recursive add
                    }
                }
            }
        }
    }
}
