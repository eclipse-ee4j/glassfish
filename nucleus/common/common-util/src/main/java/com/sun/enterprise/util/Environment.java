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

package com.sun.enterprise.util;


/**
 * Environment class that contains information about
 * from where this appserver instance is being invoked from
 */
public class Environment {

    private static String ENV_CLASS_NAME
         = "com.sun.enterprise.ee.util.OpenEnvironment";

    protected Environment() {
    }

    public static Environment obtain() {
        Environment e = new Environment();
        try {
            Object obj = java.security.AccessController.doPrivileged
                (new java.security.PrivilegedExceptionAction() {
                public java.lang.Object run() throws Exception {
                    return Class.forName(ENV_CLASS_NAME).newInstance();
                }
            });
            if (obj != null) {
                e = (Environment) obj;
            }
        } catch (Exception ex) {
        }
        return e;
    }

    public void activateEnvironment() {
        return;
    }
}
