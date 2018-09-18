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

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * *This is a factory for {@link ASMClassFile}. This is not a public class, as
 * I expect users to use {@link ClassFileLoaderFactory} interface. This class
 * internally uses the the standard Java ClassLoader to load the resource and
 * construct ASMClassFile object out of it.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class ASMClassFileLoader implements ClassFileLoader {

    private ClassLoader cl;
    private static String resourceBundleName = "com.sun.enterprise.tools.verifier.apiscan.LocalStrings";
    private static Logger logger = Logger.getLogger("apiscan.classfile", resourceBundleName); // NOI18N
    private final static String myClassName = "ASMClassFileLoader"; // NOI18N
    // cache of already loaded classes
    private Map<String, WeakReference<ClassFile>> loadedClassesCache =
            new HashMap<String, WeakReference<ClassFile>>();
    /**
     * Creates a new instance of ASMClassFileLoader.
     *
     * @param cp that will be used to create a new java.net.URLClassLoader. In
     *           subsequent load operations, this classloader will be used.
     */
    public ASMClassFileLoader(String cp) {
        ArrayList<URL> urls = new ArrayList<URL>();
        for (StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
             st.hasMoreTokens();) {
            String entry = st.nextToken();
            try {
                urls.add(new File(entry).toURI().toURL());
            } catch (MalformedURLException e) {
                logger.logp(Level.WARNING, myClassName, "init<>", getClass().getName() + ".exception1", new Object[]{entry});
                logger.log(Level.WARNING, "", e);
            }
        }
        //We do not want system class loader or even extension class loadera s our parent.
        //We want only boot class loader as our parent. Boot class loader is represented as null.
        cl = new URLClassLoader((URL[]) urls.toArray(new URL[0]), null);
    }

    /**
     * Creates a new instance of ASMClassFileLoader.
     *
     * @param cl is the classloader that will be used in subsequent load
     *           operations.
     */
    public ASMClassFileLoader(ClassLoader cl) {
        this.cl = cl;
    }

    //See corresponding method of ClassFileLoader
    public ClassFile load(String externalClassName) throws IOException {
        logger.entering("ASMClassFileLoader", "load", externalClassName); // NOI18N
        WeakReference<ClassFile> cachedCF = loadedClassesCache.get(externalClassName);
        if(cachedCF!=null){
            ClassFile cf = cachedCF.get();
            if(cf!=null){
                return cf;
            } else {
                logger.logp(Level.FINE, "ASMClassFileLoader", "load", // NOI18N
                        "{0} has been garbage collected from cache!", externalClassName); // NOI18N
            }
        }
        return load0(externalClassName);
    }

    private ClassFile load0(String externalClassName) throws IOException {
        //URLClassLoader library expects me to pass in internal form.
        String internalClassName = externalClassName.replace('.', '/');
        InputStream is = cl.getResourceAsStream(internalClassName + ".class");
        //URLClassLoader returns null if resource is not found.
        if (is == null) throw new IOException(
                "Not able to load " + internalClassName + ".class");
        try {
            ClassFile cf = new ASMClassFile(is);
            matchClassSignature(cf, externalClassName);
            loadedClassesCache.put(externalClassName,
                    new WeakReference<ClassFile>(cf));
            return cf;
        } finally {
            is.close();
        }

    }

    //This method is neede to be protected against users who are passing us
    //internal class names instead of external class names or
    //when the file actually represents some other class, but it isnot
    //available in in proper package hierarchy.
    private void matchClassSignature(ClassFile cf, String externalClassName)
            throws IOException {
        String nameOfLoadedClass = cf.getName();
        if (!nameOfLoadedClass.equals(externalClassName)) {
            throw new IOException(externalClassName + ".class represents " +
                    cf.getName() +
                    ". Perhaps your package name is incorrect or you passed the " +
                    "name using internal form instead of using external form.");
        }
    }
}
