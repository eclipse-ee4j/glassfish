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

/*
 * BCELClassFileLoader.java
 *
 * Created on August 17, 2004, 9:28 AM
 */

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.sun.org.apache.bcel.internal.util.ClassPath;

/**
 * Yet another factory for {@link BCELClassFile}. This is not a public class, as
 * I expect users to use {@link ClassFileLoaderFactory} interface. It differs
 * from {@link BCELClassFileLoader} in the sense that it loads classfiles using
 * bcel ClassPath class. Known Issues: Currently it ignores any INDEX
 * information if available in a Jar file. This is because BCEL provided class
 * ClassPath does not understand INDEX info. We should add this feature in
 * future.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class BCELClassFileLoader1 implements ClassFileLoader {

    private static String resourceBundleName = "com.sun.enterprise.tools.verifier.apiscan.LocalStrings";
    private static Logger logger = Logger.getLogger("apiscan.classfile", resourceBundleName); // NOI18N
    private ClassPath cp;

    /**
     * Creates a new instance of BCELClassFileLoader User should use {link
     * ClassFileLoaderFactory} to create new instance of a loader.
     *
     * @param classPath represents the search path that is used by this loader.
     *                  Please note that, it does not read the manifest entries
     *                  for any jar file specified in the classpath, so if the
     *                  jar files have optional package dependency, that must be
     *                  taken care of in the classpath by ther caller.
     */
    public BCELClassFileLoader1(String classPath) {
        logger.entering("BCELClassFileLoader1", "<init>(String)", classPath); // NOI18N
        cp = new ClassPath(classPath);
    }

    //See ClassFileLoader for description of this method.
    public ClassFile load(String externalClassName) throws IOException {
        logger.entering("BCELClassFileLoader1", "load", externalClassName); // NOI18N
        //BCEL library expects me to pass in internal form.
        String internalClassName = externalClassName.replace('.', '/');
        //don't call getInputStream as it first tries to load using the
        // getClass().getClassLoader().getResourceAsStream()
        //return cp.getInputStream(extClassName);
        InputStream is = cp.getClassFile(internalClassName, ".class") // NOI18N
                .getInputStream();
        try {
            ClassFile cf = new BCELClassFile(is, internalClassName + ".class"); // NOI18N
            matchClassSignature(cf, externalClassName);
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
            throw new IOException(
                    externalClassName + ".class represents " +
                    cf.getName() +
                    ". Perhaps your package name is incorrect or you passed the" +
                    " name using internal form instead of using external form.");
        }
    }
}
