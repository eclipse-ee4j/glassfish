/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.oracle.hk2.devtest.cdi.ejb1.BasicEjb;

/**
 * Has a main so it can be used to invoke the EJB from a client
 *
 * @author jwells
 *
 */
public class Main {
    private final static String BASIC_EJB_JNDI_NAME = "java:global/ejb1/EjbInjectedWithServiceLocator!" +
            BasicEjb.class.getName();

    private static int go() throws NamingException {
        Context context = new InitialContext();

        BasicEjb basic = (BasicEjb) context.lookup(BASIC_EJB_JNDI_NAME);

        boolean ret = basic.cdiManagerInjected();

        System.out.println("EJB#cdiManagerInjected invoked with result " + ret);

        return 0;
    }

    public static void main(String argc[]) {
        try {
            go();
            System.exit(0);
        }
        catch (Throwable th) {
            th.printStackTrace();
            System.exit(1);
        }

    }
}
