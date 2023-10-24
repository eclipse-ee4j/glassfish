/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.ejb.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.glassfish.tests.embedded.ejb.SampleEjb;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

/**
 * this test will use the ejb API testing.
 *
 * @author Jerome Dochez
 */
public class EmbeddedTest {

    public static void main(String[] args) {
        EmbeddedTest test = new EmbeddedTest();
        System.setProperty("basedir", System.getProperty("user.dir"));
        test.test();
    }

    public File getDeployableArtifact() {
        File f = new File(System.getProperty("basedir"), "target");
        f = new File(f, "classes");
        return f;
    }

    @Test
    public void test() {
        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, getDeployableArtifact());

        try {
            EJBContainer c = EJBContainer.createEJBContainer(p);
            try {
                Context ic = c.getContext();
                try {
                    System.out.println("Looking up EJB...");
                    SampleEjb ejb = (SampleEjb) ic.lookup("java:global/classes/SampleEjb");
                    if (ejb!=null) {
                        System.out.println("Invoking EJB...");
                        System.out.println(ejb.saySomething());
                        Assertions.assertEquals(ejb.saySomething(), "Hello World");
                    }
                } catch (Exception e) {
                    System.out.println("ERROR calling EJB:");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                System.out.println("Done calling EJB");
            } finally {
                c.close();
            }
        } catch(Exception e) {
            System.out.println("Error setting up EJB container");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
