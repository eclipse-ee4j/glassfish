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

package org.glassfish.appclient.client.jws.boot;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * Class Path manager for Java Web Start-aware ACC running under Java runtime 1.6.
 *
 * @author tjquinn
 */
public class ClassPathManager16 extends ClassPathManager {

    /** Class object for the JNLPClassLoader class */
    private Class jnlpClassLoaderClass;

    /** Method object for the getJarFile method on JNLPClassLoader - only in 1.6 and later */
    private Method getJarFileMethod;

    /**
     *Returns a new instance of the class path manager for use under Java 1.6
     *@param loader the Java Web Start-provided class loader
     */
    protected ClassPathManager16(ClassLoader loader, boolean keepJWSClassLoader) {
        super(loader, keepJWSClassLoader);
        try {
            prepareIntrospectionInfo();
        } catch (Throwable thr) {
            throw new RuntimeException(thr);
        }
    }

    /**
     *Prepares the reflection-related private variables for later use in
     *locating classes in JARs.
     *@throws ClassNotFoundException if the JNLPClassLoader class cannot be found
     *@throws NoSuchMethodException if the getJarFile method cannot be found
     */
    private void prepareIntrospectionInfo() throws ClassNotFoundException, NoSuchMethodException {
        jnlpClassLoaderClass = getJNLPClassLoader().loadClass("com.sun.jnlp.JNLPClassLoader");
        getJarFileMethod = jnlpClassLoaderClass.getDeclaredMethod("getJarFile", URL.class);
        getJarFileMethod.setAccessible(true);
    }

    public ClassLoader getParentClassLoader() {
        return (keepJWSClassLoader() ? getJnlpClassLoader() : getJNLPClassLoader().getParent());
    }

    public File findContainingJar(URL resourceURL) throws IllegalArgumentException, URISyntaxException, MalformedURLException, IllegalAccessException, InvocationTargetException {
        File result = null;
        if (resourceURL != null) {
            /*
             *The URL will be similar to http://host:port/...path-in-server-namespace!resource-spec
             *Extract the part preceding the ! and ask the Java Web Start loader to
             *find the locally-cached JAR file corresponding to that part of the URL.
             */
            URI resourceURI = resourceURL.toURI();
            String ssp = resourceURI.getSchemeSpecificPart();
            String jarOnlySSP = ssp.substring(0, ssp.indexOf('!'));

            URL jarOnlyURL = new URL(jarOnlySSP).toURI().toURL();

            /*
             *Use introspection to invoke the method.  This avoids complications
             *in building the app server under Java 1.5 in which the JNLPClassLoader
             *does not provide the getJarFile method.
             */
            JarFile jarFile = (JarFile) getJarFileMethod.invoke(getJNLPClassLoader(), jarOnlyURL);
            if (jarFile == null) {
                throw new IllegalArgumentException(resourceURL.toExternalForm());
            }
            result = new File(jarFile.getName());
        }
        return result;
    }
}
