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

package org.glassfish.tests.embedded.cooked;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.embedded.Server;
import org.glassfish.internal.embedded.EmbeddedFileSystem;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import com.sun.enterprise.config.serverbeans.VirtualServer;
import org.glassfish.grizzly.config.dom.NetworkListener;

/**
 * @author Jerome Dochez
 */
public class ExistingConfigurationTest {

    @Test
    public void setupServer() throws Exception {

        Server server=null;

        System.out.println("setup started with gf installation " + System.getProperty("basedir"));
        File f = new File(System.getProperty("basedir"));
        f = new File(f, "target");
        f = new File(f, "dependency");
        f = new File(f, "glassfish9");
        f = new File(f, "glassfish");
        if (f.exists()) {
            System.out.println("Using gf at " + f.getAbsolutePath());
        } else {
            System.out.println("GlassFish not found at " + f.getAbsolutePath());
            Assert.assertTrue(f.exists());
        }
        try {
            EmbeddedFileSystem.Builder efsb = new EmbeddedFileSystem.Builder();
            efsb.installRoot(f, false);
            // find the domain root.
            f = new File(f,"domains");
            f = new File(f, "domain1");
            f = new File(f, "config");
            f = new File(f, "domain.xml");
            Assert.assertTrue(f.exists());
            efsb.configurationFile(f);

            Server.Builder builder = new Server.Builder("inplanted");
            builder.embeddedFileSystem(efsb.build());
            server = builder.build();

            ServiceLocator habitat = server.getHabitat();
            Collection<VirtualServer> vss = habitat.getAllServices(VirtualServer.class);
            Assert.assertTrue(vss.size()>0);
            for (VirtualServer vs : vss ) {
                System.out.println("Virtual Server " + vs.getId());
            }
            Collection<NetworkListener> nls = habitat.getAllServices(NetworkListener.class);
            for (NetworkListener nl : nls) {
                System.out.println("Network listener " + nl.getPort());
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (server!=null) {
                server.stop();
            }
        }
    }
}
