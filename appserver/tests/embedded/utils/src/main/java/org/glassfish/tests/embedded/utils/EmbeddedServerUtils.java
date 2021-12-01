/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.utils;

import org.junit.Assert;
import org.glassfish.internal.embedded.LifecycleException;
import org.glassfish.internal.embedded.EmbeddedFileSystem;
import org.glassfish.internal.embedded.Server;

import java.io.File;

public class EmbeddedServerUtils {

    public static File getServerLocation() {
        System.out.println("setup started with gf installation " + System.getProperty("basedir"));
        File f = new File(System.getProperty("basedir"));
        f = new File(f, "target");
        f = new File(f, "dependency");
        f = new File(f, "glassfish7");
        f = new File(f, "glassfish");
        if (f.exists()) {
            System.out.println("Using gf at " + f.getAbsolutePath());
        } else {
            System.out.println("GlassFish not found at " + f.getAbsolutePath());
            Assert.assertTrue(f.exists());
        }
        return f;
    }
    public static File getDomainLocation(File serverLocation) {
        return getDomainLocation(serverLocation, "domain1");
    }

    public static File getDomainLocation(File serverLocation, String domainName) {

       // find the domain root.
        File f = new File(serverLocation,"domains");
        f = new File(f, domainName);
        Assert.assertTrue(f.exists());
        return f;
    }

    public static Server createServer(EmbeddedFileSystem fileSystem) throws Exception {
        try {
            Server.Builder builder = new Server.Builder("inplanted");
            builder.embeddedFileSystem(fileSystem);
            return builder.build();
        } catch(Exception e) {
            e.printStackTrace();
            if (fileSystem.autoDelete) {
                fileSystem.preDestroy();
            }
            throw e;
        }
    }

    public static void shutdownServer(Server server) throws Exception {
        System.out.println("shutdown initiated");
        if (server!=null) {
            try {
                server.stop();
            } catch (LifecycleException e) {
                e.printStackTrace();
                throw e;
            }
        }


    }

}
