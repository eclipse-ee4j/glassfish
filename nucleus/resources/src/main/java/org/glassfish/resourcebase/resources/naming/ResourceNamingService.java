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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.naming.ComponentNamingUtil;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
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

    private static final Logger LOG = System.getLogger(ResourceNamingService.class.getName());

    // TODO ASR introduce contract for this service and refactor this service to connector-runtime ?
    @Inject
    private GlassfishNamingManager namingManager;

    @Inject
    private ComponentNamingUtil componentNamingUtil;

    @Inject
    private ProcessEnvironment processEnvironment;

    public void publishObject(GenericResourceInfo resourceInfo, Object object, boolean rebind) throws NamingException {
        publishObject(resourceInfo, resourceInfo.getName(), object, rebind);
    }

    public void publishObject(final GenericResourceInfo resourceInfo, final SimpleJndiName jndiName,
        final Object object, final boolean rebind) throws NamingException {
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = ResourceUtil.getActualModuleName(resourceInfo.getModuleName());
        SimpleJndiName resJndiName = resourceInfo.getName();
        LOG.log(Level.DEBUG, "publishObject: applicationName={0}, moduleName={1}, jndiName={2}, resourceJndiName={3}",
            applicationName, moduleName, jndiName, resJndiName);
        if (applicationName != null && moduleName != null && resJndiName.isJavaModule()) {

            Object alreadyBoundObject = null;
            if (rebind) {
                // FIXME: It would be better to implement rebind in JavaNamespace
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
            namingManager.bindToModuleNamespace(applicationName, moduleName, list);
        } else if (applicationName != null && resJndiName.isJavaApp()) {
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
                    throw new NamingException("Object already bound for jndiName " + "[ " + jndiName
                        + " ] of application's namespace [" + applicationName + "]");
                }
            }

            JNDIBinding binding = new ApplicationScopedResourceBinding(jndiName, object);
            namingManager.bindToAppNamespace(applicationName, List.of(binding));
            bindAppScopedNameForAppclient(object, jndiName, applicationName);
        } else {
            namingManager.publishObject(jndiName, object, true);
        }
    }

    private void bindAppScopedNameForAppclient(Object object, SimpleJndiName jndiName, String applicationName) throws NamingException {
        SimpleJndiName name = componentNamingUtil.composeInternalGlobalJavaAppName(applicationName, jndiName);
        namingManager.publishObject(name, object, true);
    }

    public void unpublishObject(GenericResourceInfo resourceInfo) throws NamingException {
        unpublishObject(resourceInfo, resourceInfo.getName());
    }

    public void unpublishObject(GenericResourceInfo resourceInfo, SimpleJndiName jndiName) throws NamingException {
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = ResourceUtil.getActualModuleName(resourceInfo.getModuleName());
        SimpleJndiName resJndiName = resourceInfo.getName();
        LOG.log(Level.DEBUG, "unpublishObject: applicationName={0}, moduleName={1}, jndiName={2}, resourceJndiName={3}",
            applicationName, moduleName, jndiName, resJndiName);
        if (applicationName != null && moduleName != null && resJndiName.isJavaModule()) {
            namingManager.unbindModuleObject(applicationName, moduleName, jndiName);
        } else if (applicationName != null && resJndiName.isJavaApp()) {
            namingManager.unbindAppObject(applicationName, jndiName);
            unbindAppScopedNameForAppclient(jndiName, applicationName);
        } else {
            namingManager.unpublishObject(jndiName);
        }
    }

    private void unbindAppScopedNameForAppclient(SimpleJndiName jndiName, String applicationName) throws NamingException {
        SimpleJndiName internalGlobalJndiName = componentNamingUtil.composeInternalGlobalJavaAppName(applicationName, jndiName);
        namingManager.unpublishObject(internalGlobalJndiName);
    }

    public <T> T lookup(GenericResourceInfo resourceInfo, SimpleJndiName jndiName) throws NamingException {
        return lookup(resourceInfo, jndiName, null);
    }

    public <T> T lookup(GenericResourceInfo resourceInfo, SimpleJndiName jndiName, Hashtable env) throws NamingException {
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = ResourceUtil.getActualModuleName(resourceInfo.getModuleName());
        SimpleJndiName resJndiName = resourceInfo.getName();
        LOG.log(Level.DEBUG, "lookup: applicationName={0}, moduleName={1}, jndiName={2}, resourceJndiName={3}",
            applicationName, moduleName, jndiName, resJndiName);
        if (applicationName != null && moduleName != null && resJndiName.isJavaModule()) {
            return namingManager.lookupFromModuleNamespace(applicationName, moduleName, jndiName, env);
        } else if (applicationName != null && resJndiName.isJavaApp()) {
            if (processEnvironment.getProcessType().isServer() || processEnvironment.getProcessType().isEmbedded()) {
                return namingManager.lookupFromAppNamespace(applicationName, jndiName, env);
            }
            SimpleJndiName internalGlobalJndiName = componentNamingUtil
                .composeInternalGlobalJavaAppName(applicationName, jndiName);
            LOG.log(Level.DEBUG, "internalGlobalJndiName={0}", internalGlobalJndiName);
            return (T) namingManager.lookup(internalGlobalJndiName);
        } else if (env == null || env.isEmpty()) {
            return (T) namingManager.lookup(jndiName);
        } else {
            // probably some remote context, corba.
            return (T) new InitialContext(env).lookup(jndiName.toString());
        }
    }

}