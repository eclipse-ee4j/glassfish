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

package org.glassfish.resourcebase.resources.naming;

import com.sun.logging.LogDomains;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.naming.ComponentNamingUtil;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.resourcebase.resources.ResourceLoggingConstansts;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;


/**
 * Resource naming service which helps to bind resources and internal resource objects
 * to appropriate namespace in JNDI. Supports "java:app", "java:module" and normal(physical)
 * names.
 * @author Jagadish Ramu
 *
 */
@Service
public class ResourceNamingService {

    //TODO ASR introduce contract for this service and refactor this service to connector-runtime ?
    @Inject
    private GlassfishNamingManager namingManager;

    @Inject
    private ComponentNamingUtil cnu;

    @Inject
    private ProcessEnvironment pe;

    @LogMessagesResourceBundle
    public static final String LOGMESSAGE_RESOURCE = "org.glassfish.resourcebase.resources.LogMessages";

    @LoggerInfo(subsystem="RESOURCE", description="Nucleus Resource", publish=true)

    public static final String LOGGER = "jakarta.enterprise.resources.naming";
    private static final Logger _logger = Logger.getLogger(LOGGER, LOGMESSAGE_RESOURCE);

    public static final String JAVA_APP_SCOPE_PREFIX = "java:app/";
    public static final String JAVA_COMP_SCOPE_PREFIX = "java:comp/";
    public static final String JAVA_MODULE_SCOPE_PREFIX = "java:module/";
    public static final String JAVA_GLOBAL_SCOPE_PREFIX = "java:global/";


    public void publishObject(GenericResourceInfo resourceInfo, String jndiName, Object object,boolean rebind)
            throws NamingException{
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = resourceInfo.getModuleName();
        moduleName = org.glassfish.resourcebase.resources.util.ResourceUtil.getActualModuleName(moduleName);

        if(!isGlobalName(resourceInfo.getName()) && applicationName != null && moduleName != null ){

            Object alreadyBoundObject = null;
            if(rebind){
                try{
                    namingManager.unbindModuleObject(applicationName, moduleName, getModuleScopedName(jndiName));
                }catch(NameNotFoundException e){
                    //ignore
                }
            }else{
                try {
                    alreadyBoundObject = namingManager.lookupFromModuleNamespace(applicationName,
                            moduleName, getModuleScopedName(jndiName), null);
                } catch (NameNotFoundException e) {
                    //ignore
                }
                if (alreadyBoundObject != null) {
                    throw new NamingException("Object already bound for jndiName " +
                            "[ " + jndiName + " ] of  module namespace [" + moduleName+ "] " +
                            "of application ["+applicationName+"] ");
                }
            }

            JNDIBinding bindings = new ModuleScopedResourceBinding(getModuleScopedName(jndiName), object);
            List<JNDIBinding> list = new ArrayList<JNDIBinding>();
            list.add(bindings);
            if(_logger.isLoggable(Level.FINEST)){
                debug("application=" + applicationName + ", module name=" +moduleName+", binding name=" + jndiName);
            }
            namingManager.bindToModuleNamespace(applicationName, moduleName, list);
        }else if(!isGlobalName(resourceInfo.getName()) && applicationName != null ) {

            Object alreadyBoundObject = null;
            if(rebind){
                try{
                    namingManager.unbindAppObject(applicationName, getAppScopedName(jndiName));
                }catch(NameNotFoundException e){
                    //ignore
                }
            }else{
                try {
                    alreadyBoundObject = namingManager.lookupFromAppNamespace(applicationName,
                            getAppScopedName(jndiName), null);
                } catch (NameNotFoundException e) {
                    //ignore
                }
                if (alreadyBoundObject != null) {
                    throw new NamingException("Object already bound for jndiName " +
                            "[ " + jndiName + " ] of application's namespace [" + applicationName + "]");
                }
            }

            JNDIBinding bindings = new ApplicationScopedResourceBinding(getAppScopedName(jndiName), object);
            List<JNDIBinding> list = new ArrayList<JNDIBinding>();
            list.add(bindings);
            if(_logger.isLoggable(Level.FINEST)){
                debug("application=" + applicationName + ", binding name=" + jndiName);
            }
            namingManager.bindToAppNamespace(applicationName, list);
            bindAppScopedNameForAppclient(object, jndiName, applicationName);
        }else{
            namingManager.publishObject(jndiName, object, true);
        }
    }

    public void publishObject(ResourceInfo resourceInfo, Object object,boolean rebind) throws NamingException{
        String jndiName = resourceInfo.getName();
        publishObject(resourceInfo, jndiName, object, rebind);
    }

    private void bindAppScopedNameForAppclient(Object object, String jndiName, String applicationName)
            throws NamingException {
        String internalGlobalJavaAppName =
                cnu.composeInternalGlobalJavaAppName(applicationName, getAppScopedName(jndiName));
        if(_logger.isLoggable(Level.FINEST)){
            debug("binding app-scoped-resource for appclient : " + internalGlobalJavaAppName);
        }
        namingManager.publishObject(internalGlobalJavaAppName, object, true);
    }


    public void unpublishObject(GenericResourceInfo resourceInfo, String jndiName) throws NamingException {
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = resourceInfo.getModuleName();
        moduleName = org.glassfish.resourcebase.resources.util.ResourceUtil.getActualModuleName(moduleName);

        if(!isGlobalName(resourceInfo.getName()) && applicationName != null && moduleName != null){
            namingManager.unbindModuleObject(applicationName, moduleName, getModuleScopedName(jndiName));
        }else if(!isGlobalName(resourceInfo.getName()) && applicationName != null) {
            namingManager.unbindAppObject(applicationName, getAppScopedName(jndiName));
            unbindAppScopedNameForAppclient(jndiName, applicationName);
        }else{
            namingManager.unpublishObject(jndiName);
        }
    }

    private void unbindAppScopedNameForAppclient(String jndiName, String applicationName) throws NamingException {
        String internalGlobalJavaAppName =
                cnu.composeInternalGlobalJavaAppName(applicationName, getAppScopedName(jndiName));
        namingManager.unpublishObject(internalGlobalJavaAppName);
    }

    private boolean isGlobalName(String jndiName) {
        return jndiName.startsWith(JAVA_GLOBAL_SCOPE_PREFIX) ||
                (!jndiName.startsWith(JAVA_APP_SCOPE_PREFIX) &&
                !jndiName.startsWith(JAVA_MODULE_SCOPE_PREFIX));
    }

    public Object lookup(GenericResourceInfo resourceInfo, String name, Hashtable env) throws NamingException{
        String applicationName = resourceInfo.getApplicationName();
        String moduleName = resourceInfo.getModuleName();
        moduleName = org.glassfish.resourcebase.resources.util.ResourceUtil.getActualModuleName(moduleName);

        if(!isGlobalName(resourceInfo.getName()) && applicationName != null && moduleName != null){
            return namingManager.lookupFromModuleNamespace(applicationName, moduleName, getModuleScopedName(name), env);
        }else if(!isGlobalName(resourceInfo.getName()) && applicationName != null) {
            if(pe.getProcessType().isServer() || pe.getProcessType().isEmbedded()){
                return namingManager.lookupFromAppNamespace(applicationName, getAppScopedName(name), env);
            }else{
                String internalGlobalJavaAppName =
                        cnu.composeInternalGlobalJavaAppName(applicationName, getAppScopedName(name));
                if(_logger.isLoggable(Level.FINEST)){
                    debug("appclient lookup : " + internalGlobalJavaAppName);
                }
                return namingManager.getInitialContext().lookup(internalGlobalJavaAppName);
            }
        }else{
            if(env != null){
                InitialContext ic = new InitialContext(env);
                return ic.lookup(name);
            }else{
                return namingManager.getInitialContext().lookup(name);
            }
        }
    }

    public Object lookup(GenericResourceInfo resourceInfo, String name) throws NamingException{
        return lookup(resourceInfo, name, null);
    }

    private String getModuleScopedName(String name){

/*
        if(!name.startsWith(JAVA_MODULE_SCOPE_PREFIX) && !name.startsWith(JAVA_GLOBAL_SCOPE_PREFIX)){
            return JAVA_MODULE_SCOPE_PREFIX+name;
        }
*/

        return name;
    }

    private String getAppScopedName(String name){

/*
        if(!name.startsWith(JAVA_APP_SCOPE_PREFIX) && !name.startsWith(JAVA_GLOBAL_SCOPE_PREFIX)){
            return JAVA_APP_SCOPE_PREFIX+name;
        }
*/

        return name;
    }

    private void debug(String message) {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("[ASR] [ResourceNamingService] : " + message);
        }
    }
}
