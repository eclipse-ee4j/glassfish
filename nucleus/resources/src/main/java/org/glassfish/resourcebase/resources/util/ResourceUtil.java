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

package org.glassfish.resourcebase.resources.util;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.Module;
import org.glassfish.resourcebase.resources.api.GenericResourceInfo;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.resourcebase.resources.ResourceLoggingConstansts;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;


/**
 * @author Jagadish Ramu
 */
public class ResourceUtil {

    @LogMessagesResourceBundle
    public static final String LOGMESSAGE_RESOURCE = "org.glassfish.resourcebase.resources.LogMessages";

    @LoggerInfo(subsystem="RESOURCE", description="Nucleus Resource", publish=true)

    public static final String LOGGER = "jakarta.enterprise.resources.util";
    private static final Logger _logger = Logger.getLogger(LOGGER, LOGMESSAGE_RESOURCE);

    private static final String RESOURCES_XML_META_INF = "META-INF/glassfish-resources.xml";
    private static final String RESOURCES_XML_WEB_INF = "WEB-INF/glassfish-resources.xml";

    public static BindableResource getBindableResourceByName(Resources resources, String name) {
        Collection<BindableResource> typedResources = resources.getResources(BindableResource.class);
        if(typedResources != null)
        for(BindableResource resource : typedResources){
            if(resource.getJndiName().equals(name)){
                return resource;
            }
        }
        return null;
    }


    public static Resource getResourceByName(Class<? extends Resource> clazz, Resources resources,
        String name) {
      Collection<? extends Resource> typedResources = resources.getResources(clazz);
      if (typedResources != null)
        for (Resource resource : typedResources) {
          if (resource.getIdentity().equals(name)) {
            return resource;
          }
        }
      return null;
    }

    public static Resource getResourceByIdentity(Resources resources, String name) {
        for (Resource resource : resources.getResources()) {
          if (resource.getIdentity().equals(name)) {
            return resource;
          }
        }
        return null;
    }

    public static ResourceInfo getResourceInfo(BindableResource resource){

        if(resource.getParent() != null && resource.getParent().getParent() instanceof Application){
            Application application = (Application)resource.getParent().getParent();
            return new ResourceInfo(resource.getJndiName(), application.getName());
        }else if(resource.getParent() != null && resource.getParent().getParent() instanceof Module){
            Module module = (Module)resource.getParent().getParent();
            Application application = (Application)module.getParent();
            return new ResourceInfo(resource.getJndiName(), application.getName(), module.getName());
        }else{
            return new ResourceInfo(resource.getJndiName());
        }
    }

    public static boolean isValidEventType(Object instance) {
        return instance instanceof Resource;
    }

    /**
     * given a resource config bean, returns the resource name / jndi-name
     * @param resource
     * @return resource name / jndi-name
     */
    public static ResourceInfo getGenericResourceInfo(Resource resource){
        ResourceInfo resourceInfo = null;
        String resourceName = resource.getIdentity();
        resourceInfo = getGenericResourceInfo(resource, resourceName);
        return resourceInfo;
    }

    public static ResourceInfo getGenericResourceInfo(Resource resource, String resourceName){
        if(resource.getParent() != null && resource.getParent().getParent() instanceof Application){
            Application application = (Application)resource.getParent().getParent();
            return new ResourceInfo(resourceName, application.getName());
        }else if(resource.getParent() != null && resource.getParent().getParent() instanceof Module){
            Module module = (Module)resource.getParent().getParent();
            Application application = (Application)module.getParent();
            return new ResourceInfo(resourceName, application.getName(), module.getName());
        }else{
            return new ResourceInfo(resourceName);
        }
    }

    public static PoolInfo getPoolInfo(ResourcePool resource){

        if(resource.getParent() != null && resource.getParent().getParent() instanceof Application){
            Application application = (Application)resource.getParent().getParent();
            return new PoolInfo(resource.getName(), application.getName());
        }else if(resource.getParent() != null && resource.getParent().getParent() instanceof Module){
            Module module = (Module)resource.getParent().getParent();
            Application application = (Application)module.getParent();
            return new PoolInfo(resource.getName(), application.getName(), module.getName());
        }else{
            return new PoolInfo(resource.getName());
        }
    }

    public static String getActualModuleNameWithExtension(String moduleName) {
        String actualModuleName = moduleName;
        if(moduleName.endsWith("_war")){
            int index = moduleName.lastIndexOf("_war");
            actualModuleName = moduleName.substring(0, index) + ".war";
        }else if(moduleName.endsWith("_rar")){
            int index = moduleName.lastIndexOf("_rar");
            actualModuleName = moduleName.substring(0, index) + ".rar";
        }else if(moduleName.endsWith("_jar")){
            int index = moduleName.lastIndexOf("_jar");
            actualModuleName = moduleName.substring(0, index) + ".jar";
        }
        return actualModuleName;
    }

    //TODO ASR : checking for .jar / .rar / .war / .ear ?
    public static String getActualModuleName(String moduleName){
        if(moduleName != null){
            if(moduleName.endsWith(".jar") /*|| moduleName.endsWith(".war") */|| moduleName.endsWith(".rar")){
                moduleName = moduleName.substring(0,moduleName.length()-4);
            }
        }
        return moduleName;
    }

    /**
     * load and create an object instance
     * @param className class to load
     * @return instance of the class
     */
    public static Object loadObject(String className) {
        Object obj = null;
        Class c;

        try {
            c = Thread.currentThread().getContextClassLoader().loadClass(className);
            obj = c.newInstance();
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, ResourceLoggingConstansts.LOAD_CLASS_FAIL, className);
            _logger.log(Level.SEVERE, ResourceLoggingConstansts.LOAD_CLASS_FAIL_EXCEP, ex.getMessage());
        }
        return obj;
    }


    //TODO ASR : instead of explicit APIs, getScope() can return "none" or "app" or "module" enum value ?
    public static boolean isApplicationScopedResource(GenericResourceInfo resourceInfo){
        return resourceInfo != null && resourceInfo.getApplicationName() != null &&
                resourceInfo.getName() != null && resourceInfo.getName().startsWith(ResourceConstants.JAVA_APP_SCOPE_PREFIX);
    }

    public static boolean isModuleScopedResource(GenericResourceInfo resourceInfo){
        return resourceInfo != null && resourceInfo.getApplicationName() != null && resourceInfo.getModuleName() != null &&
                resourceInfo.getName() != null && resourceInfo.getName().startsWith(ResourceConstants.JAVA_MODULE_SCOPE_PREFIX);
    }
}
