/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package jaxwsfromwsdl.client;

import org.testng.annotations.*;
import org.testng.Assert;
import java.lang.reflect.*;
import java.io.*;

public class JaxwsFromWsdlTestNG {

    private Class cls;
    private Constructor ct;
    private Object obj;
    private Method meth;

    @BeforeTest
    public void loadClass() throws Exception {
        try {
            cls = Class.forName("jaxwsfromwsdl.client.AddNumbersClient");
            ct = cls.getConstructor();
            obj = ct.newInstance();
            // System.out.println ("class is loaded");
        } catch (Exception ex) {
            System.out.println("Got ex, class is not loaded.");
            throw new Exception(ex);
        }
        System.out.println("done for init");
    }

    @Test(groups = { "functional" })
    public void testAddNumbers_JaxwsFromWsdl() throws Exception {
        boolean result = false;
        try {
            meth = cls.getMethod("testAddNumbers");
            System.out.println("meth=" + meth.toString());
            System.out.println("cls=" + cls);
            System.out.println("ct=" + ct);
            System.out.println("obj=" + obj);
            result = (Boolean) meth.invoke(obj, (Object[]) null);
        } catch (Exception ex) {
            System.out.println("got unexpected exception.");
            throw new Exception(ex);
        }
        
        Assert.assertTrue(result);
    }

    @Test(dependsOnMethods = { "testAddNumbers_JaxwsFromWsdl" })
    public void testAddNumbersException_JaxwsFromWsdl() throws Exception {
        boolean result = false;
        try {
            meth = cls.getMethod("testAddNumbersException");
            result = (Boolean) meth.invoke(obj, (Object[]) null);
        } catch (Exception ex) {
            System.out.println("got unexpected exception");
            throw new Exception(ex);
        }
        Assert.assertTrue(result);
    }

}
