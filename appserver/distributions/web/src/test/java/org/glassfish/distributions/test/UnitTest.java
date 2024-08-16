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

package org.glassfish.distributions.test;

import jakarta.ejb.EJBException;
import jakarta.ejb.embeddable.EJBContainer;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

import org.glassfish.distributions.test.ejb.SampleEjb;
import org.junit.Test;

public class UnitTest {

    @Test
    public void test() {

        // Calculate test-classes location
        String cname = "org/glassfish/distributions/test/UnitTest.class";
        URL source = UnitTest.class.getClassLoader().getResource(cname);
        String dir = source.getPath().substring(0, source.getPath().length()-cname.length());

        Map<String, File> p = new HashMap<String, File>();
        p.put(EJBContainer.MODULES, new File(dir));
        EJBContainer c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            SampleEjb ejb = (SampleEjb) ic.lookup("java:global/classes/SampleEjb");
            if (ejb!=null) {
                System.out.println("Invoking EJB...");
                System.out.println(ejb.saySomething());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done calling EJB");

        try {
            System.out.println("Creating another container without closing...");
            EJBContainer c0 = EJBContainer.createEJBContainer();
            if (c0 != null)
                throw new RuntimeException("Create another container");
        } catch (EJBException e) {
            System.out.println("Caught expected: " + e.getMessage());
        }

        c.close();
        System.out.println("Creating container after closing the previous...");
        c = EJBContainer.createEJBContainer(p);
        c.close();
    }
}
