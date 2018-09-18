/*
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

import org.junit.Test;
import org.junit.Assert;
import org.glassfish.tests.embedded.ejb.SampleEjb;
import org.glassfish.internal.embedded.*;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.*;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * this test will use the ejb API testing.
 *
 * @author Jerome Dochez
 */
public class EmbeddedTest {

    @Test
    public void test() throws Exception {
        Server.Builder builder = new Server.Builder("simple");
        Server server = builder.build();
        File f = new File(System.getProperty("basedir"), "target");
        f = new File(f, "classes");

        ScatteredArchive archive = new ScatteredArchive.Builder("simple",f).buildJar();
        server.addContainer(ContainerBuilder.Type.ejb);
        try {
            server.start();
            String appName = null;
            try {
                appName = server.getDeployer().deploy(archive, null);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            assert(appName!=null);
            try {
                System.out.println("Looking up EJB...");
                SampleEjb ejb = (SampleEjb) (new InitialContext()).lookup("java:global/simple/SampleEjb");
                if (ejb!=null) {
                    System.out.println("Invoking EJB...");
                    System.out.println(ejb.saySomething());
                    Assert.assertEquals(ejb.saySomething(), "Hello World");
                }
            } catch (Exception e) {
                System.out.println("ERROR calling EJB:");
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            server.getDeployer().undeploy(appName, null);
        } finally {
            server.stop();
        }
    }
}
