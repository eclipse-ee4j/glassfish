/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jvnet.hk2.annotations.Service;

/**
 * This is implementation of ResourceLoader interface. One instance of this class
 * is created for each bean deployment archive. This class ensures that resource
 * is loaded using class loader for that bean deployment archive.
 * 
 * This was needed to fix issue : http://java.net/jira/browse/GLASSFISH-17396
 *
 * @author kshitiz
 */
@Service
public class ResourceLoaderImpl implements ResourceLoader{

    private ClassLoader classLoader;

    public ResourceLoaderImpl(ClassLoader cl) {
        classLoader = cl;
    }

    @Override
    public Class<?> classForName(String name) {
        ClassLoader cl = getClassLoader();
        try {
            if (cl != null) {
                return cl.loadClass(name);
            } else {
                return Class.forName(name);
            }
        } catch (ClassNotFoundException e) {
            throw new ResourceLoadingException("Error loading class " + name, e);
        } catch (NoClassDefFoundError e) {
            throw new ResourceLoadingException("Error loading class " + name, e);
        } catch (TypeNotPresentException e) {
            throw new ResourceLoadingException("Error loading class " + name, e);
        }
    }

    @Override
    public URL getResource(String name) {
        ClassLoader cl = getClassLoader();
        if (cl != null) {
            return cl.getResource(name);
        } else {
            return ResourceLoaderImpl.class.getResource(name);
        }
    }

    @Override
    public Collection<URL> getResources(String name) {
        ClassLoader cl = getClassLoader();
        try {
            if (cl != null) {
                return getCollection(cl.getResources(name));
            } else {
                return getCollection((getClass().getClassLoader().getResources(name)));
            }
        } catch (IOException e) {
            throw new ResourceLoadingException("Error loading resource " + name, e);
        }
    }

    @Override
    public void cleanup() {
    }

    private ClassLoader getClassLoader(){
        if(classLoader != null){
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }

    private Collection<URL> getCollection(Enumeration<URL> resources) {
        ArrayList<URL> urls = new ArrayList<URL>();
        while(resources.hasMoreElements()){
            urls.add(resources.nextElement());
        }
        return urls;
    }

}
