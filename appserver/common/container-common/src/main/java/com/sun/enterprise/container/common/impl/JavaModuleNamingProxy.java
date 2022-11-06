/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.container.common.spi.ManagedBeanManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.invocation.ApplicationEnvironment;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_GLOBAL;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_MODULE;


@Service
@NamespacePrefixes({
    JNDI_CTX_JAVA_APP,
    JNDI_CTX_JAVA_MODULE,
    JavaModuleNamingProxy.JAVA_APP_NAME,
    JavaModuleNamingProxy.JAVA_MODULE_NAME,
    JavaModuleNamingProxy.JAVA_APP_SERVICE_LOCATOR
})
public class JavaModuleNamingProxy implements NamedNamingObjectProxy, PostConstruct {

    static final String JAVA_APP_NAME = JNDI_CTX_JAVA_APP + "AppName";
    static final String JAVA_MODULE_NAME = JNDI_CTX_JAVA_MODULE + "ModuleName";
    static final String JAVA_APP_SERVICE_LOCATOR = JNDI_CTX_JAVA_APP + "hk2/ServiceLocator";

    private static final Logger LOG = LogDomains.getLogger(JavaModuleNamingProxy.class, LogDomains.JNDI_LOGGER, false);

    @Inject
    private ServiceLocator habitat;

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    private ApplicationRegistry applicationRegistry;

    private ProcessEnvironment.ProcessType processType;

    private InitialContext ic;

    @Override
    public void postConstruct() {
        try {
            ic = new InitialContext();
        } catch (NamingException ne) {
            throw new RuntimeException("JavaModuleNamingProxy InitialContext creation failure", ne);
        }

        processType = processEnv.getProcessType();
    }


    @Override
    public Object handle(String name) throws NamingException {
        if (JAVA_APP_NAME.equals(name)) {
            return getAppName();
        } else if (JAVA_MODULE_NAME.equals(name)) {
            return getModuleName();
        } else if (JAVA_APP_SERVICE_LOCATOR.equals(name)) {
            return getAppServiceLocator();
        } else if (name.startsWith(JNDI_CTX_JAVA_MODULE) || name.startsWith(JNDI_CTX_JAVA_APP)) {
            // Check for any automatically defined portable EJB names under
            // java:module/ or java:app/.

            // If name is not found, return null instead
            // of throwing an exception.
            // The application can explicitly define environment dependencies within this
            // same namespace, so this will allow other name checking to take place.
            return getJavaModuleOrAppEJB(name);
        }
        // Return null if this proxy is not responsible for processing the name.
        return null;
    }


    private String getAppName() throws NamingException {
        ComponentEnvManager namingMgr = habitat.getService(ComponentEnvManager.class);
        String appName = null;
        if (namingMgr != null) {
            JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();
            BundleDescriptor bd = null;
            if (env instanceof EjbDescriptor) {
                bd = ((EjbDescriptor) env).getEjbBundleDescriptor();
            } else if (env instanceof BundleDescriptor) {
                bd = (BundleDescriptor) env;
            }

            if (bd != null) {
                Application app = bd.getApplication();
                appName = app.getAppName();
            } else {
                ApplicationEnvironment applicationEnvironment = namingMgr.getCurrentApplicationEnvironment();
                if (applicationEnvironment != null) {
                    appName = applicationEnvironment.getName();
                }
            }
        }

        if (appName == null) {
            throw new NamingException("Could not resolve " + JAVA_APP_NAME);
        }
        return appName;

    }


    private String getModuleName() throws NamingException {
        final ComponentEnvManager namingMgr = habitat.getService(ComponentEnvManager.class);
        if (namingMgr == null) {
            throw new NamingException("Could not resolve " + JAVA_MODULE_NAME + ", ComponentEnvManager is null.");
        }
        final JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();
        final BundleDescriptor bd;
        if (env instanceof EjbDescriptor) {
            bd = ((EjbDescriptor) env).getEjbBundleDescriptor();
        } else if (env instanceof BundleDescriptor) {
            bd = (BundleDescriptor) env;
        } else {
            bd = null;
        }
        if (bd == null) {
            throw new NamingException("Could not resolve " + JAVA_MODULE_NAME + ", descriptor is null.");
        }
        final String moduleName = bd.getModuleDescriptor().getModuleName();
        if (moduleName == null) {
            throw new NamingException("Could not resolve " + JAVA_MODULE_NAME + ", descriptor's module name is null");
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


    private Object getJavaModuleOrAppEJB(String name) {
        String newName = null;
        Object returnValue = null;
        if (habitat != null) {
            ComponentEnvManager namingMgr = habitat.getService(ComponentEnvManager.class);

            if (namingMgr != null) {
                JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();
                final BundleDescriptor bd;
                if (env instanceof EjbDescriptor) {
                    bd = ((EjbDescriptor) env).getEjbBundleDescriptor();
                } else if (env instanceof BundleDescriptor) {
                    bd = (BundleDescriptor) env;
                } else {
                    bd = null;
                }

                if (bd != null) {
                    final Application app = bd.getApplication();
                    final String appName = app.isVirtual() ? null : app.getAppName();
                    StringBuilder javaGlobalName = new StringBuilder(32).append(JNDI_CTX_JAVA_GLOBAL);
                    if (name.startsWith(JNDI_CTX_JAVA_APP)) {

                        // For portable EJB names relative to java:app, module
                        // name is already contained in the lookup string.  We just
                        // replace the logical java:app with the application name
                        // in the case of an .ear.  Otherwise, in the stand-alone
                        // module case the existing module-name already matches the global
                        // syntax.

                        if (appName != null) {
                            javaGlobalName.append(appName);
                            javaGlobalName.append('/');
                        }

                        // Replace java:app/ with the fully-qualified global portion
                        javaGlobalName.append(name.substring(JNDI_CTX_JAVA_APP.length()));

                    } else {

                        // For portable EJB names relative to java:module, only add
                        // the application name if it's an .ear, but always add
                        // the module name.

                        if (appName != null) {
                            javaGlobalName.append(appName);
                            javaGlobalName.append('/');
                        }

                        javaGlobalName.append(bd.getModuleDescriptor().getModuleName());
                        javaGlobalName.append('/');

                        // Replace java:module/ with the fully-qualified global portion
                        javaGlobalName.append(name.substring(JNDI_CTX_JAVA_MODULE.length()));
                    }

                    newName = javaGlobalName.toString();
                }
            }
        }

        if (newName != null) {
            try {
                if (processType == ProcessType.ACC) {
                    ManagedBeanManager mbMgr = habitat.getService(ManagedBeanManager.class);
                    try {
                        returnValue = mbMgr.getManagedBean(newName);
                    } catch (Exception e) {
                        NamingException ne = new NamingException("Error creating ACC managed bean " + newName);
                        ne.initCause(e);
                        throw ne;
                    }
                }

                if (returnValue == null) {
                    returnValue = ic.lookup(newName);
                }
            } catch (NamingException ne) {
                LOG.log(Level.FINE, newName + " Unable to map " + name + " to derived name " + newName, ne);
            }
        }
        return returnValue;
    }
}

