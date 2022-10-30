/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.resourcebase.resources.naming;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.naming.ComponentNamingUtil;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Service;

/**
 * Resource naming service which helps to bind resources and internal resource objects to appropriate namespace in JNDI.
 * Supports "java:app", "java:module" and normal(physical) names.
 *
 * @author Jagadish Ramu
 */
@Service
public final class ResourceNamingService {

    @LogMessagesResourceBundle
    public static final String LOGMESSAGE_RESOURCE = "org.glassfish.resourcebase.resources.LogMessages";

    @LoggerInfo(subsystem = "RESOURCE", description = "Nucleus Resource", publish = true)
    public static final String LOGGER = "jakarta.enterprise.resources.naming";
    private static final Logger LOG = Logger.getLogger(LOGGER, LOGMESSAGE_RESOURCE);

    // TODO ASR introduce contract for this service and refactor this service to connector-runtime ?
    @Inject
    private GlassfishNamingManager namingManager;

    @Inject
    private ComponentNamingUtil componentNamingUtil;

    @Inject
    private ProcessEnvironment processEnvironment;

    public void publishObject(final GenericResourceInfo resourceInfo, final SimpleJndiName jndiName,
        final Object object, final boolean rebind) throws NamingException {
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = ResourceUtil.getActualModuleName(resourceInfo.getModuleName());

        if (applicationName != null && moduleName != null && resourceInfo.getName().isJavaModule()) {

            Object alreadyBoundObject = null;
            if (rebind) {
                try {
                    namingManager.unbindModuleObject(applicationName, moduleName, jndiName);
                } catch (NameNotFoundException e) {
                    // ignore
                }
            } else {
                try {
                    alreadyBoundObject = namingManager.lookupFromModuleNamespace(applicationName, moduleName,
                        jndiName, null);
                } catch (NameNotFoundException e) {
                    // ignore
                }

                if (alreadyBoundObject != null) {
                    throw new NamingException(
                        "Object already bound for jndiName " + "[ " + jndiName + " ] of  module namespace ["
                            + moduleName + "] " + "of application [" + applicationName + "] ");
                }
            }

            JNDIBinding bindings = new ModuleScopedResourceBinding(jndiName, object);
            List<JNDIBinding> list = new ArrayList<>();
            list.add(bindings);
            LOG.log(Level.FINE, "applicationName={0}, moduleName={1}, jndiName={2}",
                new Object[] {applicationName, moduleName, jndiName});
            namingManager.bindToModuleNamespace(applicationName, moduleName, list);
        } else if (applicationName != null && (jndiName.isJavaApp() || jndiName.isJavaModule())) {
            Object alreadyBoundObject = null;
            if (rebind) {
                try {
                    namingManager.unbindAppObject(applicationName, jndiName);
                } catch (NameNotFoundException e) {
                    // ignore
                }
            } else {
                try {
                    alreadyBoundObject = namingManager.lookupFromAppNamespace(applicationName, jndiName, null);
                } catch (NameNotFoundException e) {
                    // ignore
                }
                if (alreadyBoundObject != null) {
                    throw new NamingException("Object already bound for jndiName " + "[ " + jndiName + " ] of application's namespace ["
                            + applicationName + "]");
                }
            }

            JNDIBinding bindings = new ApplicationScopedResourceBinding(jndiName, object);
            namingManager.bindToAppNamespace(applicationName, List.of(bindings));
            bindAppScopedNameForAppclient(object, jndiName, applicationName);
        } else {
            namingManager.publishObject(jndiName, object, true);
        }
    }

    public void publishObject(ResourceInfo resourceInfo, Object object, boolean rebind) throws NamingException {
        SimpleJndiName jndiName = resourceInfo.getName();
        publishObject(resourceInfo, jndiName, object, rebind);
    }

    private void bindAppScopedNameForAppclient(Object object, SimpleJndiName jndiName, String applicationName) throws NamingException {
        SimpleJndiName name = componentNamingUtil.composeInternalGlobalJavaAppName(applicationName, jndiName);
        namingManager.publishObject(name, object, true);
    }

    public void unpublishObject(GenericResourceInfo resourceInfo, SimpleJndiName jndiName) throws NamingException {
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = ResourceUtil.getActualModuleName(resourceInfo.getModuleName());

        if (!resourceInfo.getName().isJavaGlobal() && applicationName != null && moduleName != null) {
            namingManager.unbindModuleObject(applicationName, moduleName, jndiName);
        } else if (!resourceInfo.getName().isJavaGlobal() && applicationName != null) {
            namingManager.unbindAppObject(applicationName, jndiName);
            unbindAppScopedNameForAppclient(jndiName, applicationName);
        } else {
            namingManager.unpublishObject(jndiName);
        }
    }

    private void unbindAppScopedNameForAppclient(SimpleJndiName jndiName, String applicationName) throws NamingException {
        SimpleJndiName internalGlobalJavaAppName = componentNamingUtil.composeInternalGlobalJavaAppName(applicationName, jndiName);
        namingManager.unpublishObject(internalGlobalJavaAppName);
    }

    public <T> T lookup(GenericResourceInfo resourceInfo, SimpleJndiName name) throws NamingException {
        return lookup(resourceInfo, name, null);
    }

    public <T> T lookup(GenericResourceInfo resourceInfo, SimpleJndiName name, Hashtable env) throws NamingException {
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = ResourceUtil.getActualModuleName(resourceInfo.getModuleName());
        SimpleJndiName jndiName = resourceInfo.getName();
        if (applicationName != null && moduleName != null && (jndiName.isJavaApp() || jndiName.isJavaModule())) {
            return namingManager.lookupFromModuleNamespace(applicationName, moduleName, name, env);
        }
        if (applicationName != null && (jndiName.isJavaApp() || jndiName.isJavaModule())) {
            if (processEnvironment.getProcessType().isServer() || processEnvironment.getProcessType().isEmbedded()) {
                return namingManager.lookupFromAppNamespace(applicationName, name, env);
            }
            SimpleJndiName internalGlobalJavaAppName = componentNamingUtil.composeInternalGlobalJavaAppName(applicationName, name);
            LOG.log(Level.FINEST, "appclient lookup: {0}", internalGlobalJavaAppName);
            return (T) namingManager.getInitialContext().lookup(internalGlobalJavaAppName.toString());
        }
        if (env == null) {
            return (T) namingManager.getInitialContext().lookup(name.toString());
        }
        return (T) new InitialContext(env).lookup(name.toString());
    }

}