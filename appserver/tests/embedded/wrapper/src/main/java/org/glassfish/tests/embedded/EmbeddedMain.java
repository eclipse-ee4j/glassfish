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

package org.glassfish.tests.embedded;

import org.glassfish.internal.embedded.*;

import java.io.File;


/** A 'wrapper' class that is used by QA to run tests on embedded. This class is used to start the domain
 * in embedded mode from asadmin cli
 *
 */

public class EmbeddedMain {
    public static void main(String[] args) {

       String installRoot = System.getenv("S1AS_HOME");
       if (installRoot == null) {
           System.out.println("Environment variable S1AS_HOME not defined - it must point to the glassfish install root");
           return;
       }
       String instanceRoot = installRoot + "/domains/domain1";
       String domainXml = instanceRoot + "/config/domain.xml";

        System.setProperty("com.sun.aas.instanceRootURI", "file:" + instanceRoot);
        //System.setProperty("com.sun.aas.installRoot", installRoot );
//        System.setProperty("com.sun.aas.instanceRoot", instanceRoot );

       EmbeddedFileSystem efs =
          new EmbeddedFileSystem.Builder().
          installRoot(new File(installRoot), true).
          instanceRoot(new File(instanceRoot)).
          configurationFile(new File(domainXml), false).autoDelete(false).build();

        Server.Builder builder = new Server.Builder("asadmin");

        Server server = builder.embeddedFileSystem(efs).build();
        server.addContainer(ContainerBuilder.Type.all);
    }
}


