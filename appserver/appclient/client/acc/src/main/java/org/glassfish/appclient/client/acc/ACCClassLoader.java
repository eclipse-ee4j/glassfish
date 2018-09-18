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

package org.glassfish.appclient.client.acc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.cert.Certificate;

import org.glassfish.appclient.common.ClientClassLoaderDelegate;

/**
 *
 * @author tjquinn
 */
public class ACCClassLoader extends URLClassLoader {

    private static final String AGENT_LOADER_CLASS_NAME =
            "org.glassfish.appclient.client.acc.agent.ACCAgentClassLoader";
    private static ACCClassLoader instance = null;

    private ACCClassLoader shadow = null;

    private boolean shouldTransform = false;
    
    private final List<ClassFileTransformer> transformers =
            Collections.synchronizedList(
                new ArrayList<ClassFileTransformer>());

    
    private ClientClassLoaderDelegate clientCLDelegate;
    
    public static synchronized ACCClassLoader newInstance(ClassLoader parent,
            final boolean shouldTransform) {
        if (instance != null) {
            throw new IllegalStateException("already set");
        }
        final ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        final boolean currentCLWasAgentCL = currentCL.getClass().getName().equals(
                    AGENT_LOADER_CLASS_NAME);
        final ClassLoader parentForACCCL = currentCLWasAgentCL ? currentCL.getParent() : currentCL;
        
        instance = AccessController.doPrivileged(new PrivilegedAction<ACCClassLoader>() {

            @Override
            public ACCClassLoader run() {
                return new ACCClassLoader(userClassPath(), parentForACCCL, shouldTransform);
            }
            
        });
        
        if (currentCLWasAgentCL) {
            try {
                adjustACCAgentClassLoaderParent(instance);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return instance;
    }

    public static ACCClassLoader instance() {
        return instance;
    }

    private static void adjustACCAgentClassLoaderParent(final ACCClassLoader instance)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if (systemClassLoader.getClass().getName().equals(AGENT_LOADER_CLASS_NAME)) {
            final Field jwsLoaderParentField = ClassLoader.class.getDeclaredField("parent");
            jwsLoaderParentField.setAccessible(true);
            jwsLoaderParentField.set(systemClassLoader, instance);
            System.setProperty("org.glassfish.appclient.acc.agentLoaderDone", "true");
        
        }
    }
    
    private static URL[] userClassPath() {
        final URI GFSystemURI = GFSystemURI();
        final List<URL> result = classPathToURLs(System.getProperty("java.class.path"));
        for (ListIterator<URL> it = result.listIterator(); it.hasNext();) {
            final URL url = it.next();
            try {
                if (url.toURI().equals(GFSystemURI)) {
                    it.remove();
                }
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }

        result.addAll(classPathToURLs(System.getenv("APPCPATH")));

        return result.toArray(new URL[result.size()]);
    }

    private static URI GFSystemURI() {
        try {
            Class agentClass = Class.forName("org.glassfish.appclient.client.acc.agent.AppClientContainerAgent");
            return agentClass.getProtectionDomain().getCodeSource().getLocation().toURI().normalize();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<URL> classPathToURLs(final String classPath) {
        if (classPath == null) {
            return Collections.EMPTY_LIST;
        }
        final List<URL> result = new ArrayList<URL>();
        try {
            for (String classPathElement : classPath.split(File.pathSeparator)) {
                result.add(new File(classPathElement).toURI().normalize().toURL());
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ACCClassLoader(ClassLoader parent, final boolean shouldTransform) {
        super(new URL[0], parent);
        this.shouldTransform = shouldTransform;
        
        clientCLDelegate = new ClientClassLoaderDelegate(this);
    }
//
//    public ACCClassLoader(URL[] urls) {
//        super(urls);
//    }

    public ACCClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        
        clientCLDelegate = new ClientClassLoaderDelegate(this);
    }

//    public ACCClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
//        super(urls, parent, factory);
//    }

    private ACCClassLoader(URL[] urls, ClassLoader parent, boolean shouldTransform) {
        this(urls, parent);
        this.shouldTransform = shouldTransform;
    }
    
    public synchronized void appendURL(final URL url) {
        addURL(url);
        if (shadow != null) {
            shadow.addURL(url);
        }
    }

    public void addTransformer(final ClassFileTransformer xf) {
        transformers.add(xf);
    }

    public void setShouldTransform(final boolean shouldTransform) {
        this.shouldTransform = shouldTransform;
    }

    synchronized ACCClassLoader shadow() {
        if (shadow == null) {
            shadow = AccessController.doPrivileged(new PrivilegedAction<ACCClassLoader>() {

                @Override
                public ACCClassLoader run() {
                    return new ACCClassLoader(getURLs(), getParent());
                }
                
            });
        }
        return shadow;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if ( ! shouldTransform) {
            return super.findClass(name);
        }
        final ACCClassLoader s = shadow();
        final Class<?> c = s.findClassUnshadowed(name);
        return copyClass(c);
    }

    private Class<?> copyClass(final Class c) throws ClassNotFoundException {
        final String name = c.getName();
        final ProtectionDomain pd = c.getProtectionDomain();
        byte[] bytecode = readByteCode(name);

        for (ClassFileTransformer xf : transformers) {
            try {
                bytecode = xf.transform(this, name, null, pd, bytecode);
            } catch (IllegalClassFormatException ex) {
                throw new ClassNotFoundException(name, ex);
            }
        }
        return defineClass(name, bytecode, 0, bytecode.length, pd);
    }

    private Class<?> findClassUnshadowed(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    private byte[] readByteCode(final String className) throws ClassNotFoundException {
        final String resourceName = className.replace('.', '/') + ".class";
        InputStream is = getResourceAsStream(resourceName);
        if (is == null) {
            throw new ClassNotFoundException(className);
        }
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8196];
            int bytesRead;
            while ( (bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(className, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new ClassNotFoundException(className, e);
            }
        }
    }    

    
    @Override
    protected PermissionCollection getPermissions(CodeSource codesource) {

        if (System.getSecurityManager() == null)
            return super.getPermissions(codesource);
        
        //when security manager is enabled, find the declared permissions        
        if (clientCLDelegate.getCachedPerms(codesource) != null)
            return clientCLDelegate.getCachedPerms(codesource);

        return clientCLDelegate.getPermissions(codesource, 
                super.getPermissions(codesource));
    }
    
    
    public void processDeclaredPermissions() throws IOException  {

        if (clientCLDelegate == null)
            clientCLDelegate = new ClientClassLoaderDelegate(this);
    }    
    
}
