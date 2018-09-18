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

import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class Util {

    private static Logger logger = Logger.getLogger("apiscan.classfile"); // NOI18N

    private static final String myClassName = "Util"; // NOI18N

    /**
     * @param internalClsName is the name in internal format
     * @return the class name in external format, i,e. format used in reflection
     *         API (e.g. Class.forName())
     */
    public static String convertToExternalClassName(String internalClsName) {
        return internalClsName.replace('/', '.');
    }

    /**
     * @param externalClsName is the name in internal format
     * @return the class name in internal format, i,e. format used in byte code
     */
    public static String convertToInternalClassName(String externalClsName) {
        return externalClsName.replace('.', '/');
    }

    public static boolean isPrimitive(String className) {
        logger.entering(myClassName, "isPrimitive", new Object[]{className}); // NOI18N
        boolean result = ("B".equals(className) || // NOI18N
                "C".equals(className) || // NOI18N
                "D".equals(className) || // NOI18N
                "F".equals(className) || // NOI18N
                "I".equals(className) || // NOI18N
                "J".equals(className) || // NOI18N
                "S".equals(className) || // NOI18N
                "Z".equals(className)); // NOI18N
        logger.exiting(myClassName, "isPrimitive", result); // NOI18N
        return result;
    }
}
