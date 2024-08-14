/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.common.plugin;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import java.net.URL;
import java.net.URLClassLoader;

import org.glassfish.admingui.plugin.ConsolePluginService;
import org.glassfish.hk2.api.ServiceLocator;


/**
 *  <p>This <code>ClassLoader</code> makes it possible to access plugin
 * resources by finding the appropriate plugin module's
 * <code>ClassLoader</code> and loading resources from it.</p>
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
public class ConsoleClassLoader extends ClassLoader {

    // This is defined in the web module, but for now I don't want to depend
    // on that module to get the value of this variable.
    public static final String HABITAT_ATTRIBUTE = "org.glassfish.servlet.habitat";

    /**
     * <p> This constructor should not normally be used.  You should use
     *     the one that allows you to provide the parent
     *     <code>ClassLoader</code>.</p>
     */
    protected ConsoleClassLoader() {
        super();
    }

    /**
     * <p> This constructor creates an instance of this
     *     <code>ClassLoader</code> and will use the given
     *     <code>ClassLoader</code> as its parent <code>ClassLoader</code>.</p>
     *
     * @param        parent        The parent <code>ClassLoader</code>
     */
    public ConsoleClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * <p> This method will attempt to look for a module...</p>
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if(c!=null) return c;
        return super.findClass(name);
    }
     */

    /**
     * <p> In order for this method to find the Resource...
     *     </p>
     */
    public URL findResource(String name) {
//System.out.println("Find Resource: " + name);
        // Find module name
        int end = name.indexOf('/');
        int start = 0;
        while (end == start) {
            end = name.indexOf('/', ++start);
        }
        if (end == -1) {
            // Not a request for a module resource
            return null;
        }
        String moduleName = name.substring(start, end);
        name = name.substring(end + 1);
        if (start != 0) {
            // This means the original request was prefixed with a "/"
            name = "/" + name;
        }

        // Get the Module ClassLoader
        ClassLoader moduleCL = findModuleClassLoader(moduleName);
        if (moduleCL != null) {
            // Use the Module ClassLoader to find the resource
            if (moduleCL instanceof URLClassLoader) {
                URL url = ((URLClassLoader) moduleCL).findResource(name);
//System.out.println("findResource("+name+"), URL: " + url);
                return url;
            } else {
                return moduleCL.getResource(name);
            }
        }

        // Not found.
        return null;
    }

    /**
     * <p> This method find the <code>ClassLoader</code> associated with the
     *     named module.</p>
     */
    public static ClassLoader findModuleClassLoader(String moduleName) {
//System.out.println("Find module ClassLoader: " + moduleName);
        // Get the ServletContext
        ServletContext servletCtx = (ServletContext)
            (FacesContext.getCurrentInstance().getExternalContext()).getContext();

        // Get the Habitat from the ServletContext
        ServiceLocator habitat = (ServiceLocator) servletCtx.getAttribute(HABITAT_ATTRIBUTE);

        // Use the Habitat to find the ConsolePluginService and return the
        // correct ClassLoader for the requested module (or null)
        return habitat.<ConsolePluginService>getService(ConsolePluginService.class).
            getModuleClassLoader(moduleName);
    }

// FIXME: I need to finish implementing this class!  So far I only support getResource()

/*
    public Enumeration<URL> findResources(String name) throws IOException {

        for (ClassLoaderFacade classLoader : facadeSurrogates) {

            Enumeration<URL> enumerat = classLoader.getResources(name);
            if (enumerat!=null && enumerat.hasMoreElements()) {
                return enumerat;
            }
        }
        for (ClassLoader classLoader : surrogates) {
            Enumeration<URL> enumerat = classLoader.getResources(name);
            if (enumerat!=null && enumerat.hasMoreElements()) {
                return enumerat;
            }

        }
        return super.findResources(name);
    }
    */
}
