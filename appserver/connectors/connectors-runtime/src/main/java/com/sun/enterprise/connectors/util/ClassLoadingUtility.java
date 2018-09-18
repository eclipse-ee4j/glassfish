/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.util;

import com.sun.enterprise.connectors.ConnectorRuntime;

public class ClassLoadingUtility {

    /**
     * Get the current thread's context class loader which is set to
     * the CommonClassLoader by ApplicationServer
     *
     * @return the thread's context classloader if it exists;
     *         else the system class loader.
     */
    private static ClassLoader getClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return ClassLoader.getSystemClassLoader();
        }
    }

    /**
     * Loads the class with the common class loader.
     *
     * @param the class name
     * @return the loaded class
     * @throws if the class is not found.
     * @see getClassLoader()
     */
    public static Class loadClass(String className) throws ClassNotFoundException {
        try{
            return getClassLoader().loadClass(className);
        }catch(Exception e){
            //try loading the class using common classloader (connector classloader's parent chain has common classloader)
            return ConnectorRuntime.getRuntime().getConnectorClassLoader().loadClass(className);
        }
    }
}
