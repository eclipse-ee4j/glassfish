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
 * ClassFileLoaderFactory.java
 *
 * Created on August 24, 2004, 5:56 PM
 */

package com.sun.enterprise.tools.verifier.apiscan.classfile;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for ClassFileLoader so that we can control the creation of
 * ClassFileLoaders. More over, this factory can be dynamically configured by
 * setting the Java class name of the actual ClassFileLoader type in the system
 * property apiscan.ClassFileLoader. See newInstance() method.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ClassFileLoaderFactory {
    private static String resourceBundleName = "com.sun.enterprise.tools.verifier.apiscan.LocalStrings";
    private static Logger logger = Logger.getLogger("apiscan.classfile", resourceBundleName); // NOI18N
    private final static String myClassName = "ClassFileLoaderFactory"; // NOI18N
    /**
     * a factory method to create ClassFileLoader instance. It decides which
     * kind of loader class to instantioate based on the class name supplied by
     * the system property ClassFileLoader. If there is no such property set, it
     * defaults to {@link BCELClassFileLoader}
     *
     * @param args Search path to be used by the ClassFileLoader. Depending on
     *             the type of the ClassFileLoader requested, the semantics of
     *             this argument varies.
     * @throws RuntimeException If it could not instantiate the loader type
     *                          requested. The actual error is wrapped in this
     *                          exception.
     */
    public static ClassFileLoader newInstance(Object[] args)
            throws RuntimeException {
        logger.entering(myClassName, "newInstance", args); // NOI18N
        String loaderClassName = System.getProperty("apiscan.ClassFileLoader");
        if (loaderClassName == null) {
            loaderClassName =
                    com.sun.enterprise.tools.verifier.apiscan.classfile.BCELClassFileLoader.class.getName();
            logger.logp(Level.FINE, myClassName, "newInstance", // NOI18N
                    "System Property apiscan.ClassFileLoader is null, so defaulting to " + // NOI18N
                    loaderClassName);
        }
        try {
            Class clazz = Class.forName(loaderClassName);
            Object o = null;
            Constructor[] constrs = clazz.getConstructors();
            for (int i = 0; i < constrs.length; ++i) {
                try {
                    o = constrs[i].newInstance(args);
                } catch (IllegalArgumentException e) {
                 //try another constr as this argument did not match the reqd types for this constr.
                }
            }
            if (o == null) throw new Exception(
                    "Could not find a suitable constructor for this argument types.");
            logger.exiting(myClassName, "<init>", o); // NOI18N
            return (ClassFileLoader) o;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "com.sun.enterprise.tools.verifier.apiscan.classfile.ClassFileLoaderFactory.exception1", e);
            throw new RuntimeException("Unable to instantiate a loader.", e);
        }
    }
}
