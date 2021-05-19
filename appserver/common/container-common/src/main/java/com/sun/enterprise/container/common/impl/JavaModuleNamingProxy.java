/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl;


import org.glassfish.api.invocation.ApplicationEnvironment;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;

import com.sun.enterprise.deployment.*;


import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;

import javax.naming.NamingException;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.ManagedBeanManager;
import com.sun.logging.LogDomains;

import javax.naming.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;


@Service
@NamespacePrefixes({JavaModuleNamingProxy.JAVA_APP_CONTEXT,
        JavaModuleNamingProxy.JAVA_APP_NAME,
        JavaModuleNamingProxy.JAVA_MODULE_CONTEXT,
        JavaModuleNamingProxy.JAVA_MODULE_NAME,
        JavaModuleNamingProxy.JAVA_APP_SERVICE_LOCATOR})
public class JavaModuleNamingProxy
        implements NamedNamingObjectProxy, PostConstruct {

    @Inject
    ServiceLocator habitat;

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    private ApplicationRegistry applicationRegistry;

    private ProcessEnvironment.ProcessType processType;


    private static Logger _logger = LogDomains.getLogger(JavaModuleNamingProxy.class,
            LogDomains.NAMING_LOGGER);

    private InitialContext ic;

    public void postConstruct() {
        try {
            ic = new InitialContext();
        } catch(NamingException ne) {
            throw new RuntimeException("JavaModuleNamingProxy InitialContext creation failure", ne);
        }

        processType = processEnv.getProcessType();
    }

    static final String JAVA_MODULE_CONTEXT
            = "java:module/";

    static final String JAVA_APP_CONTEXT
            = "java:app/";

    static final String JAVA_APP_NAME
            = "java:app/AppName";

    static final String JAVA_MODULE_NAME
            = "java:module/ModuleName";

    static final String JAVA_APP_SERVICE_LOCATOR
            = "java:app/hk2/ServiceLocator";

    public Object handle(String name) throws NamingException {

        // Return null if this proxy is not responsible for processing the name.
        Object returnValue = null;

        if( name.equals(JAVA_APP_NAME) ) {

            returnValue = getAppName();

        } else if( name.equals(JAVA_MODULE_NAME) ) {

            returnValue = getModuleName();

        } else if( name.equals(JAVA_APP_SERVICE_LOCATOR) ) {

            returnValue = getAppServiceLocator();

        } else if (name.startsWith(JAVA_MODULE_CONTEXT) || name.startsWith(JAVA_APP_CONTEXT)) {

            // Check for any automatically defined portable EJB names under
            // java:module/ or java:app/.

            // If name is not found, return null instead
            // of throwing an exception.
            // The application can explicitly define environment dependencies within this
            // same namespace, so this will allow other name checking to take place.
            returnValue = getJavaModuleOrAppEJB(name);
        }

        return returnValue;
    }

    private String getAppName() throws NamingException {

        ComponentEnvManager namingMgr =
                habitat.getService(ComponentEnvManager.class);

        String appName = null;

        if( namingMgr != null ) {
            JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();

            BundleDescriptor bd = null;

            if( env instanceof EjbDescriptor ) {
                bd = ((EjbDescriptor)env).getEjbBundleDescriptor();
            } else if( env instanceof BundleDescriptor ) {
                bd = (BundleDescriptor) env;
            }

            if( bd != null ) {

                Application app = bd.getApplication();

                appName = app.getAppName();
            }
            else {
                ApplicationEnvironment applicationEnvironment = namingMgr.getCurrentApplicationEnvironment();

                if (applicationEnvironment != null) {
                    appName = applicationEnvironment.getName();
                }
            }
        }

        if( appName == null ) {
            throw new NamingException("Could not resolve " + JAVA_APP_NAME);
        }

        return appName;

    }

    private String getModuleName() throws NamingException {

        ComponentEnvManager namingMgr =
                habitat.getService(ComponentEnvManager.class);

        String moduleName = null;

        if( namingMgr != null ) {
            JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();

            BundleDescriptor bd = null;

            if( env instanceof EjbDescriptor ) {
                bd = ((EjbDescriptor)env).getEjbBundleDescriptor();
            } else if( env instanceof BundleDescriptor ) {
                bd = (BundleDescriptor) env;
            }

            if( bd != null ) {
                moduleName = bd.getModuleDescriptor().getModuleName();
            }
        }

        if( moduleName == null ) {
            throw new NamingException("Could not resolve " + JAVA_MODULE_NAME);
        }

        return moduleName;

    }

    private ServiceLocator getAppServiceLocator() throws NamingException {

        String appName = getAppName();

        ApplicationInfo info = applicationRegistry.get(appName);

        if (info == null) {
            throw new NamingException("Could not resolve " + JAVA_APP_SERVICE_LOCATOR);
        }

        return info.getAppServiceLocator();

    }



    private Object getJavaModuleOrAppEJB(String name) throws NamingException {

        String newName = null;
        Object returnValue = null;

        if( habitat != null ) {
            ComponentEnvManager namingMgr =
                habitat.getService(ComponentEnvManager.class);

            if( namingMgr != null ) {
                JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();

                BundleDescriptor bd = null;

                if( env instanceof EjbDescriptor ) {
                    bd = ((EjbDescriptor)env).getEjbBundleDescriptor();
                } else if( env instanceof BundleDescriptor ) {
                    bd = (BundleDescriptor) env;
                }

                if( bd != null ) {
                    Application app = bd.getApplication();

                    String appName = null;

                    if ( !app.isVirtual() )  {
                        appName = app.getAppName();
                    }

                    String moduleName = bd.getModuleDescriptor().getModuleName();

                    StringBuffer javaGlobalName = new StringBuffer("java:global/");


                    if( name.startsWith(JAVA_APP_CONTEXT) ) {

                        // For portable EJB names relative to java:app, module
                        // name is already contained in the lookup string.  We just
                        // replace the logical java:app with the application name
                        // in the case of an .ear.  Otherwise, in the stand-alone
                        // module case the existing module-name already matches the global
                        // syntax.

                        if (appName != null) {
                            javaGlobalName.append(appName);
                            javaGlobalName.append("/");
                        }

                        // Replace java:app/ with the fully-qualified global portion
                        int javaAppLength = JAVA_APP_CONTEXT.length();
                        javaGlobalName.append(name.substring(javaAppLength));

                    } else {

                        // For portable EJB names relative to java:module, only add
                        // the application name if it's an .ear, but always add
                        // the module name.

                        if (appName != null) {
                            javaGlobalName.append(appName);
                            javaGlobalName.append("/");
                        }

                        javaGlobalName.append(moduleName);
                        javaGlobalName.append("/");

                        // Replace java:module/ with the fully-qualified global portion
                        int javaModuleLength = JAVA_MODULE_CONTEXT.length();
                        javaGlobalName.append(name.substring(javaModuleLength));
                    }

                    newName = javaGlobalName.toString();

                }
            }

        }

        if( newName != null ) {

            try {

                if( processType == ProcessType.ACC) {

                    ManagedBeanManager mbMgr = habitat.getService(ManagedBeanManager.class);

                    try {
                        returnValue = mbMgr.getManagedBean(newName);
                    } catch(Exception e) {
                        NamingException ne = new NamingException("Error creating ACC managed bean " + newName);
                        ne.initCause(e);
                        throw ne;
                    }

                }

                if( returnValue == null ) {
                    returnValue = ic.lookup(newName);
                }
            } catch(NamingException ne) {

                _logger.log(Level.FINE, newName + " Unable to map " + name + " to derived name " +
                        newName, ne);
            }
        }

        return returnValue;
    }

}



